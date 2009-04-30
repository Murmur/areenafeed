/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;
import java.io.*;

import fi.mbnet.akini.io.*;
import fi.mbnet.akini.util.ConvertUtil;


/**
 * Convert rss video links from video.com html and rss pages
 */
public class VimeoConverter {
	public static final String ROOT_URL = "http://www.vimeo.com";
	//http://www.vimeo.com/moogaloop/load/clip:690607/local?ver=4	

	private Map<String,String> params;	// conversion params

	public VimeoConverter() { }
	
	/**
	 * Read video items from html pages. 
	 * @param conversionParams
	 * @return
	 * @throws IOException
	 */
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		params = conversionParams;
		
		String sUrl = params.get("url");
		if (sUrl == null || sUrl.equals("")) {
			// use keyword
			String stemp = params.get("keyword");
			sUrl = ROOT_URL + "/videos/search:" + ConvertUtil.URLEncode(stemp);
		} else if ( sUrl.toLowerCase().startsWith("http://") ||
				sUrl.toLowerCase().startsWith("https://") ) {
			// this is a fully qualified url
		} else {
			// concatenate to root url
			sUrl = ROOT_URL + (sUrl.charAt(0) != '/' ? "/" : "") + sUrl;
		}
		
		HttpClientReader hcr = new HttpClientReader();		
		try {
			boolean isRSS = false;
			String body = null;

			// 1**) parse body from html/rss reply
			int statusCode = hcr.get(null, null, sUrl, null, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// split chunk what we need for content parsing
				isRSS = hcr.getResponseContentType().toLowerCase().contains("application/rss");

				char[] cbuf;
				if (isRSS) {
					// Use XML-DOM to parse rss document
					body = null;
				} else {
					// Use html page
					StreamSplitter splitter = new StreamSplitter(hcr);
					cbuf = splitter.splitFromTo("<body", "</body>", true);
					body = new String(cbuf);				
				}
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = (isRSS ? 
					new VimeoMediaDAO(hcr, params.get("quality")) : 
					new VimeoMediaDAO(body, params.get("quality")) 
			);
			List<MediaFile> items = mediaDAO.list();

			// catch IO errors and continue loop
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();

				try {
					// Call XML url: http://www.vimeo.com/moogaloop/load/clip:690607/local?ver=4
					String sClipId = item.getMediaPageUrl(); // http://vimeo.com/690607
					sClipId = sClipId.substring(sClipId.lastIndexOf('/')+1);
					
					statusCode = hcr.get(null, null, ROOT_URL 
							+ "/moogaloop/load/clip:" + sClipId + "/local?ver=4", null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						// ok, parse fields from xml document
					} else {
						throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
					}
				} catch (Exception ex) {
					AppContext.getInstance().log(null, ex);
					item.setTitle("IOError, item not updated");
					continue;
				}
				
				((VimeoMediaDAO)mediaDAO).updateAdditionalFields(item, hcr);

				if (item.getTitle() == null || item.getTitle().equals("")) {
					item.setTitle("No Title");
					//item.setDescription("");
				}
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}
