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
 * Convert rss video links from new Flash-based areena.yle.fi
 * 
 * todo: html parser, atm only rss urls are supported
 */
public class Areena2Converter {
	public static final String ROOT_URL = "http://areena.yle.fi";
	//http://areena.yle.fi/video/294218	

	private Map<String,String> params;	// conversion params

	public Areena2Converter() { }
	
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
			throw new IllegalArgumentException("url parameter not found");
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
			boolean isRSShtml = false;
			String body = null;

			// 1**) parse body from html/rss reply
			int statusCode = hcr.get(null, null, sUrl, null, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// split chunk what we need for content parsing
				isRSS = hcr.getResponseContentType().toLowerCase().contains("application/xml") ||
					hcr.getResponseContentType().toLowerCase().contains("application/rss");

				char[] cbuf;
				if (isRSS) {
					// Use XML-DOM to parse rss document
					body = null;
				} else {
					// Use html page
					StreamSplitter splitter = new StreamSplitter(hcr);
					cbuf = splitter.splitFromTo(null, null, true);
					body = new String(cbuf).trim();
					
					// sometimes Yle gives rss feed as "text/html" document					
					if (body.startsWith("<?xml") || body.startsWith("<rss "))
						isRSS = isRSShtml = true;
				}
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = (isRSS ?  
					isRSShtml ? new Areena2MediaDAO(new StringReader(body)) : new Areena2MediaDAO(hcr) :  
					new Areena2MediaDAO(body) );
			List<MediaFile> items = mediaDAO.list();

			// catch IO errors and continue loop
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();
				mediaDAO.updateAdditionalFields(item, null);
				if (item.getTitle() == null || item.getTitle().equals(""))
					item.setTitle("No Title");
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}
