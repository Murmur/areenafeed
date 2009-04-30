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
 * Read CT24 Czech TV feed 
 * 		http://www.ct24.cz/vysilani/?streamtype=WM1 
 * 		http://www.ct24.cz/vysilani/?streamtype=WM2
 * 		http://www.ct24.cz/vysilani/?streamtype=WM3
 */
public class CT24CZConverter {
	public static final String ROOT_URL = "http://www.ct24.cz";
	
	// Conversion parameters:
	private Map<String,String> params;	// conversion params
	
	public CT24CZConverter() { }
	
	public String getRootURL() {
		return ROOT_URL;
	}
	
	/**
	 * Read video url from the html pages. 
	 * @param conversionParams
	 * @return
	 * @throws IOException
	 */
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		params = conversionParams;
		
		Map<String,String> urlParams = new HashMap<String,String>(4);

		String sUrl = params.get("url");
		if (sUrl == null || sUrl.equals("")) {
			sUrl = "vysilani/?streamtype=WM2"; // default medium quality url (WM1,WM2,WM3)
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

			// 1**) call http page
			int statusCode = hcr.get(null, null, sUrl, urlParams, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// add leading and trailing tags back to a buffer after we split a stream
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo("<div id=\"webCast\" ", "<!-- Indexy -->", true);
				body = new String(cbuf);
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new CT24CZMediaDAO(body, sUrl);
			List<MediaFile> items = mediaDAO.list();

			// drop extra items
			String sMaxItems = params.get("maxitems");
			if (sMaxItems != null && !sMaxItems.equals("")) {
				int maxItems = Integer.parseInt(sMaxItems);
				if (maxItems < items.size())
					items = items.subList(0, maxItems);
			}
			
			// 2**) Loop each video file and update additional fields
			String medialink = params.get("medialink");
			if (medialink != null && medialink.equals("")) medialink = null;
			
			boolean bReadUrl=false;
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();

				// reuse current buffer for the first item
				if (bReadUrl) {
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
				} else {
					bReadUrl = true; // call mediaPageUrl for 2nd...n items
				}
				
				mediaDAO.updateAdditionalFields(item, body);

				// 3**) resolve medialinkUrl from .asx page
				if (medialink != null && !medialink.equalsIgnoreCase("url") 
								&& !item.getMediaFileUrl().equals("")) {
					statusCode = hcr.get(null, null, item.getMediaFileUrl(), null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						body = new String(hcr.readAll());
					} else {
						throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
					}
					
					((CT24CZMediaDAO)mediaDAO).updateMediaFileUrl(item, body, medialink);
				}
			}
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}

