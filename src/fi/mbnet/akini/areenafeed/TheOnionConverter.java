/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;
import java.io.*;

import fi.mbnet.akini.io.*;


/**
 * Convert rss video links from theonion.com html pages.
 * Use clipnabber.com to translate mediaPageUrl to mediaFileUrl.
 */
public class TheOnionConverter {
	public static final String ROOT_URL = "http://www.theonion.com";

	private Map<String,String> params;	// conversion params

	public TheOnionConverter() { }
	
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

			// 1**) parse body from html/rss reply
			int statusCode = hcr.get(null, null, sUrl, null, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// split chunk what we need for content parsing
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo(null, null, true);
				body = new String(cbuf);				
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new TheOnionMediaDAO(body);
			List<MediaFile> items = mediaDAO.list();

			ClipNabber clipNabber = new ClipNabber();
			
			// catch IO errors and continue loop
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();

				try {
					// Use ClipNabber to resolve mediaFileUrl
					String sMediaPageUrl = clipNabber.encodeUrl( item.getMediaPageUrl() );
					statusCode = hcr.get(null, null, ClipNabber.URL + sMediaPageUrl, null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						StreamSplitter splitter = new StreamSplitter(hcr);
						char[] cbuf = splitter.splitFromTo(null, null, true);					
						body = new String(cbuf);
					} else {
						throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
					}
				} catch (Exception ex) {
					AppContext.getInstance().log(null, ex);
					item.setTitle("IOError, item not updated");
					continue;
				}
				
				item.setMediaFileUrl( clipNabber.getMediaFileUrl(body) );
				mediaDAO.updateAdditionalFields(item, body);

				if (item.getTitle() == null || item.getTitle().equals(""))
					item.setTitle("No Title");
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}
