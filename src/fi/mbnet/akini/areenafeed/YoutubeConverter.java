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
 * Convert rss video links from Youtube html page 
 */
public class YoutubeConverter {
	public static final String ROOT_URL = "http://www.youtube.com";
	public static final String THUMB_URL = "http://i4.ytimg.com/vi"; // default thumbnail server

	private Map<String,String> params;	// conversion params

	public YoutubeConverter() { }
	
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
			sUrl = ROOT_URL + "/results?aq=f&search_query=" + ConvertUtil.URLEncode(stemp);
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

			// 1**) parse body from html reply
			int statusCode = hcr.get(null, null, sUrl, null, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// split chunk what we need for content parsing
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo("</head>", "</body>", true);
				body = new String(cbuf);				
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new YoutubeMediaDAO(body, ROOT_URL);
			List<MediaFile> items = mediaDAO.list();

			String medialink = params.get("medialink");
			if (medialink != null && medialink.equals("")) medialink = null;
			
			// catch IO errors and continue loop
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

				// resolve medialinkUrl
				//    url = use original url
				//	  e26 = escape all & characters to %26
				if (medialink != null && !medialink.equalsIgnoreCase("url") 
							&& !item.getMediaFileUrl().equals("")) {
					if (medialink.equalsIgnoreCase("e26")) {
						String stemp = item.getMediaFileUrl().replaceAll("&", "%26");
						item.setMediaFileUrl(stemp);
					}
				}				
				
				if (item.getTitle() == null || item.getTitle().equals("")) {
					item.setTitle("No Title");
					item.setDescription("Some videos require Youtube login session, currently login not supported.");
				}
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}
