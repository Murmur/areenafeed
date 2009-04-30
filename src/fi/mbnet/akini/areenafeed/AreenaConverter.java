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
 * Read video/audio items from Yle Areena html pages. 
 * http://areena.yle.fi
 * 
 * FIXME: tee abstrakti converter-luokka josta perit‰‰n loput.
 * 			setRootUrl, setDescription, setTitle, createItems
 * 			siirr‰ vendor_url pois t‰st‰ tiedostosta omaan constiin.
 * FIXME: google.video k‰ytt‰‰ eri videosaitteja, miten tehd‰‰n eri saittin
 *        mediasivujen parserointi. Jokaiselle oma Converter+MediaDAO luokka?
 *        Sitten ei perit‰ GoogleConvertteri‰ Youtube-konvertterista ja 
 *        mediasivun parserointi olava public metodi.
 */
public class AreenaConverter {
	public static final String ROOT_URL = "http://areena.yle.fi";
	//public static final String DESCRIPTION = "YLE Areena rss converter";
	//public static final String VENDOR_URL = "http://koti.mbnet.fi/akini/";
	
	
	// Conversion parameters:
	//		keyword=Ajankohtainen Kakkonen
	//		quality=<lo,hi> video quality
	//		medialink=<url,asxref,asxrefmms> mediafile link
	//      filter=4,1  videos only,finnish language filter
	//		media=<video,audio,all> select by media type
	private Map<String,String> params;	// conversion params
	
	public AreenaConverter() { }
	
	public String getRootURL() {
		return ROOT_URL;
	}
	
	/**
	 * Read video and/or audio items from Yle Areena html pages. 
	 * @param conversionParams
	 * @return
	 * @throws IOException
	 */
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		params = conversionParams;
		
		Map<String,String> urlParams = new HashMap<String,String>(4);
		urlParams.put("l", "3"); // format of result list

		String stemp = params.get("keyword");
		if (stemp != null) urlParams.put("keyword", stemp);

		stemp = params.get("cid");
		if (stemp != null) urlParams.put("cid", stemp);
		
		stemp = params.get("pid");
		if (stemp != null) urlParams.put("pid", stemp);

		stemp = params.get("filter");
		if (stemp != null) urlParams.put("filter", stemp);

		HttpClientReader hcr = new HttpClientReader();		
		try {
			String body = null;

			// 1**) parse search results to get fields
			int statusCode = hcr.get(null, null, ROOT_URL + "/hae", urlParams, null);
			if (statusCode >= 200 && statusCode <= 299) {
				// add leading and trailing tags back to a buffer after we split a stream
				StreamSplitter splitter = new StreamSplitter(hcr);
				char[] cbuf = splitter.splitFromTo("<ul class=\"text-description\"><li class=", "</li></ul>", true);
				if (cbuf.length > 0)
					body = "<li class=" + new String(cbuf) + "</li>";
			} else {
				throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
			}

			MediaDAO mediaDAO = new AreenaMediaDAO(body, ROOT_URL, params.get("quality"));
			List<MediaFile> items = mediaDAO.list();

			// 2**) loop each video file and update additional fields
			String medialink = params.get("medialink");
			if (medialink != null && medialink.equals("")) medialink = null;
			
			int selectByType;
			stemp = params.get("media");
			if ("video".equalsIgnoreCase(stemp)) selectByType = 0;
			else if ("audio".equalsIgnoreCase(stemp)) selectByType = 1;
			else selectByType = -1;
			
			Iterator<MediaFile> iter = items.iterator();
			while(iter.hasNext()) {
				MediaFile item = iter.next();
				
				if (selectByType >= 0 && item.getType() != selectByType) {
					iter.remove();
					continue;
				}
				
				try {
					statusCode = hcr.get(null, null, item.getMediaPageUrl(), null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						StreamSplitter splitter = new StreamSplitter(hcr);
						//char[] cbuf = splitter.splitFromTo("<div class=\"insert subsection with-line-top\">", "<div class=\"content fragment2\">", true);
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

				// 3**) resolve medialinkUrl
				//    url   = http://www.yle.fi/java/areena/dispatcher/1569519.asx?bitrate=1000000
				//	  asxref= http://mediak.yle.fi/media/areena/1/56/95/1569521_957849.wmv
				//    asxrefmms= mms://mediak.yle.fi/media/areena/1/56/95/1569521_957849.wmv
				if (medialink != null && !medialink.equalsIgnoreCase("url") 
								&& !item.getMediaFileUrl().equals("")) {
					statusCode = hcr.get(null, null, item.getMediaFileUrl(), null, null);
					if (statusCode >= 200 && statusCode <= 299) {
						body = new String(hcr.readAll());
					} else {
						throw new IOException("HttpConnection failed, reason " + hcr.getResponseStatusMessage());
					}
					
					((AreenaMediaDAO)mediaDAO).updateMediaFileUrl(item, body, medialink);
				}
			}
			
			return items;
		} finally {
			hcr.close();
		}
	}
	
}

