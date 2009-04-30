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
 * Convert rss video links from Google video html page 
 */
public class GoogleVideoConverter {
	public static final String ROOT_URL = "http://video.google.com";

	private Map<String,String> params;	// conversion params

	public GoogleVideoConverter() { }
	
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
			sUrl = ROOT_URL + "/videosearch?hl=en&emb=1&aq=f&q=" + ConvertUtil.URLEncode(stemp);
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
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo("<body", "</body>", true);
				body = new String(cbuf);
				//char[] cbuf = splitter.splitFromTo("class=\"video\"", "<div class=\"div-footer\">", true);
				//if (cbuf.length > 0)
				//	body = "class=\"video\"" + new String(cbuf) + "class=\"video\"";				
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			String medialink = params.get("medialink");
			if (medialink != null && medialink.equals("")) medialink = null;

			GoogleVideoMediaDAO googleMediaDAO = new GoogleVideoMediaDAO(body, null);
			List<MediaFile> items = googleMediaDAO.list();

			YoutubeMediaDAO youtubeMediaDAO = null;
			
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();
			
				MediaDAO mediaDAO = null;
				
				// google links to several video sites, handle known sites with appropriate mediapage parser.
				String channel = item.getChannel();
				if (channel.equalsIgnoreCase("youtube.com")) {
					if (youtubeMediaDAO == null) youtubeMediaDAO = new YoutubeMediaDAO(null, null);
					mediaDAO = youtubeMediaDAO;
				} else if (channel.equalsIgnoreCase("video.google.com")) {
					mediaDAO = googleMediaDAO;
				} else {
					iter.remove();	// drop unknown channels (=site)
					continue;
				}

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
				
				// resolve medialinkUrl, apply to youtube.com links only
				//    url = use original url
				//	  e26 = escape all & characters to %26
				if (item.getChannel().equalsIgnoreCase("youtube.com") &&
						medialink != null && !medialink.equalsIgnoreCase("url") 
							&& !item.getMediaFileUrl().equals("")) {
					if (medialink.equalsIgnoreCase("e26")) {
						String stemp = item.getMediaFileUrl().replaceAll("&", "%26");
						item.setMediaFileUrl(stemp);
					}
				}			
				
				if (item.getTitle() == null || item.getTitle().equals("")) {
					item.setTitle("No Title");
					item.setDescription("Some videos may require a login session, currently login not supported.");
				}
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}	
	
}
