/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;
import java.io.*;
import fi.mbnet.akini.io.*;
import fi.mbnet.akini.util.StringUtil;

/**
 * Read videos from Apple Trailers 
 */
public class AppleTrailersConverter {
	public static final String ROOT_URL = "http://www.apple.com";
	
	private Map<String,String> params;	// conversion params
	
	public AppleTrailersConverter() { }
	
	public String getRootURL() {
		return ROOT_URL;
	}
	
	/**
	 * Read video items 
	 * @param conversionParams
	 * @return
	 * @throws IOException
	 */
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		params = conversionParams;
		
		Map<String,String> urlParams = new HashMap<String,String>(4);

		//** small, medium, large
		//** 480p, 720p, 1080p
		// hd480p, hd720p, hd1080p 
		// hd480, hd720, hd1080 
		// 480, 720, 1080
		String quality = params.get("quality");
		if (StringUtil.isEmpty(quality)) {
			quality = "medium";
		} else {
			if (quality.contains("480")) quality = "480p";
			else if (quality.contains("720")) quality = "720p";
			else if (quality.contains("1080")) quality = "1080p";
		}
		
		String sUrl = params.get("url");
		if (sUrl == null || sUrl.equals("")) {
			// use keyword (NOT IMPLEMENTED)
			//String stemp = params.get("keyword");
			//sUrl = ROOT_URL + "/videos/search:" + ConvertUtil.URLEncode(stemp);
		} else if ( sUrl.toLowerCase().startsWith("http://") ||
				sUrl.toLowerCase().startsWith("https://") ) {
			// this is a fully qualified url
		} else {
			// concatenate to root url
			sUrl = ROOT_URL + (sUrl.charAt(0) != '/' ? "/" : "") + sUrl;
		}

		HttpClientReader hcr = new HttpClientReader();		
		try {
			String body = null;

			// 1**) parse search results to get fields
			int statusCode = hcr.get(null, null, sUrl, urlParams, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// add leading and trailing tags back to a buffer after we split a stream
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo("<div id=\"trailergrid\">", "</div>", true);
				body = new String(cbuf);
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new AppleTrailersMediaDAO(body, ROOT_URL, quality);
			List<MediaFile> items = mediaDAO.list();

			// 2**) parse mediapage
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();
				
				try {
					statusCode = hcr.get(null, null, item.getMediaPageUrl(), null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						StreamSplitter splitter = new StreamSplitter(hcr);
						char[] cbuf = splitter.splitFromTo(mediaDAO.getMediaPageStartTag(), mediaDAO.getMediaPageEndTag(), true);					
						body = new String(cbuf);
					} else {
						throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
					}
				} catch (Exception ex) {
					AppContext.getInstance().log(null, ex);
					item.setTitle("IOError, item not updated");
					continue;
				}

				mediaDAO.updateAdditionalFields(item, body);
			}
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}

