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
 * Convert .asx response to VLC compatible rss feed.
 */
public class ASXSourceConverter {
	private Map<String,String> params;	// conversion params

	public ASXSourceConverter() { }
	
	/**
	 * Read video items from html pages. 
	 * @param conversionParams
	 * @return
	 * @throws IOException
	 */
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		params = conversionParams;
		
		String sUrl = params.get("url");
		if (sUrl == null || sUrl.equals(""))
			return new ArrayList<MediaFile>(0);			
		
		//FIXME: handle file://somefile.asx source
		
		HttpClientReader hcr = new HttpClientReader();
		try {
			// 1**) parse body from html/rss reply
			char[] cbuf;
			int statusCode = hcr.get(null, null, sUrl, null, null);
			if (statusCode >= 200 && statusCode <= 299) {
				cbuf = hcr.readAll();
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new ASXSourceMediaDAO(cbuf);
			List<MediaFile> items = mediaDAO.list();

			// 2**) loop each video file and update additional fields
			String medialink = params.get("medialink");
			if (medialink != null && medialink.equals("")) medialink = null;

			// catch IO errors and continue loop
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();

				if (medialink != null)
					((ASXSourceMediaDAO)mediaDAO).updateMediaFileUrl(item, medialink);

				if (item.getTitle() == null || item.getTitle().equals(""))
					item.setTitle("No Title");
			}			
			
			return items;
		} finally {
			hcr.close();
		}
	}	
}
