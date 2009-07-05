/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;

import fi.mbnet.akini.util.XMLUtil;


/**
 * Yle Areena html/rss page.
 */
public class Areena2MediaDAO extends MediaDAO {
	private String[] monthNames;
	
	private Document doc; // rss.xml feed
	private final static String[] rssLink = XMLUtil.toPathArray("link");
	private final static String[] rssTitle = XMLUtil.toPathArray("title");
	private final static String[] rssDesc = XMLUtil.toPathArray("description");
	private final static String[] rssPubDate = XMLUtil.toPathArray("pubDate");
	private final static String[] rssThumbnail= XMLUtil.toPathArray("enclosure");
	
	/**
	 * Parse video items from the html page buffer.
	 * @param buffer
	 */
	public Areena2MediaDAO(String buffer) {
		super(buffer);
		
		// 0=jan,1=feb,..,11=dec
		SimpleDateFormat sdfLong = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);		
		DateFormatSymbols sdfSymbols = sdfLong.getDateFormatSymbols();
		monthNames = sdfSymbols.getShortMonths();
	}
	
	/**
	 * Parse video items from rss xml.
	 * @param reader
	 */
	public Areena2MediaDAO(Reader reader) {
		this((String)null);
		doc = XMLUtil.createDocument(reader);
	}
	
	@Override
	public String getMediaPageStartTag() { return null; }

	@Override
	public String getMediaPageEndTag() { return null; }
	
	
	/**
	 * Parse video items from the buffer.
	 * @return
	 */
	public List<MediaFile> list() {
		return (doc != null ?  listFeed() : listHtml());
	}
	
	private List<MediaFile> listFeed() {
		List<MediaFile> list = new ArrayList<MediaFile>(32);
		
		List<Element> elements = XMLUtil.getElements(doc, XMLUtil.toPathArray("/channel/item"));
		if (elements == null || elements.size() < 1)
			return list;
		
		for(Element elem : elements) {
			MediaFile item = new MediaFile();

			item.setType(MediaDAO.VIDEO);
			item.setChannel("Yle Areena");
			item.setSubtitle("");
			
			item.setMediaPageUrl( XMLUtil.getText(elem, rssLink) );
			item.setThumbnailUrl( getThumbnailUrl(elem) );
			
			// drop "<![CDATA[" and "]]" tags, try to parse plain text part
			String desc = XMLUtil.getText(elem, rssDesc).trim();
			if (desc.startsWith("<![CDATA[")) desc = desc.substring(9);
			if (desc.endsWith("]]")) desc = desc.substring(0, desc.length()-2);
			item.setDescription(desc);
			
			// <pubDate>Sun, 21 Jul 2009 21:00:00 +0000</pubDate> -> 21 July 2009 21:00:00 EET
			// change timestamp to EET timezone
			item.setPublished(null);
			String sDate = XMLUtil.getText(elem, rssPubDate).trim();
			if (sDate != null && !sDate.equals("")) {
				int idx = sDate.indexOf(',');
				if (idx > 0) sDate = sDate.substring(idx+1).trim();
				
				int year, month, day;
				year = month = day = -1;
				String[] parts = sDate.split(" ");
				if (parts != null && parts.length >= 3) {
					day = Integer.parseInt(parts[0]);
					year= Integer.parseInt(parts[2]);

					for(int tempIdx=0; tempIdx < monthNames.length; tempIdx++) {
						if (parts[1].equalsIgnoreCase(monthNames[tempIdx])) {
							month = tempIdx+1; // 1...n
							break;
						}
					}
				}
				
				String sTZ = null;
				int hour,min,sec;
				hour = min = sec = 0;
				if (parts != null && parts.length >= 4) {
					String[] times = parts[3].split(":");
					hour = Integer.parseInt(times[0]);
					min  = Integer.parseInt(times[1]);
					sec  = Integer.parseInt(times[2]);

					if (parts.length >= 5)
						sTZ = parts[4];
				}


				if (year >= 0 && month >= 0 && day >= 0) {
					Calendar cal = (sTZ != null ? 
						Calendar.getInstance(TimeZone.getTimeZone("GMT"+sTZ)) :
						Calendar.getInstance() );

					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, month-1); // jan=0, feb=1, ..
					cal.set(Calendar.DAY_OF_MONTH, day);
				
					cal.set(Calendar.HOUR_OF_DAY, hour);
					cal.set(Calendar.MINUTE, min);				
					cal.set(Calendar.SECOND, sec);
					cal.set(Calendar.MILLISECOND, 0);
					
					item.setPublished(cal);
				}
			}
			
			// drop "<![CDATA[" and "]]" tags, try to parse plain text part
			String title = XMLUtil.getText(elem, rssTitle).trim();
			if (title.startsWith("<![CDATA[")) title = title.substring(9);
			if (title.endsWith("]]")) title = title.substring(0, title.length()-2);
			item.setTitle(title);

			// Additional fields are set later in a second step
			item.setMediaFileUrl("");
			
			list.add(item);
		}
		
		return list;
	}
	
	private List<MediaFile> listHtml() {
		//FIXME: todo html parser
		//List<MediaFile> list = new ArrayList<MediaFile>(32);
		//return list;
		throw new IllegalArgumentException("Html parser not implemented, use Areena rss url.");
	}
	
	@Override
	public void updateAdditionalFields(MediaFile item, String buffer) {
		if (item.getThumbnailUrl() == null)
			item.setThumbnailUrl("");

		// videos are read using a RTMPDump-yle utility
		item.setMediaFileUrl(item.getMediaPageUrl());
	}

	private String getThumbnailUrl(Element rssItem) {
		List<Element> elements = XMLUtil.getElements(rssItem, rssThumbnail);
		if (elements == null || elements.size() < 1)
			return null;
		
		for(Element elem : elements) {
			String mimeType = elem.getAttribute("type");
			if (mimeType == null) continue;
			if (mimeType.equalsIgnoreCase("image/jpeg") ||
						mimeType.equalsIgnoreCase("image/jpg") ||
						mimeType.equalsIgnoreCase("image/png"))
				return elem.getAttribute("url");
		}
		
		return null;
	}
}
