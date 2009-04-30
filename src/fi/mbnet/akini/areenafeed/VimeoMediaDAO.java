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
 * Vimeo html/rss page.
 */
public class VimeoMediaDAO extends MediaDAO {
	private String[] monthNames;
	
	private Document doc; // rss.xml feed
	private final static String[] rssLink = XMLUtil.toPathArray("link");
	private final static String[] rssDesc = XMLUtil.toPathArray("description");
	private final static String[] rssPubDate = XMLUtil.toPathArray("pubDate");
	private final static String[] rssThumbnail= XMLUtil.toPathArray("media:content/media:thumbnail/@url");
	
	private final static String[] xmlTitle = XMLUtil.toPathArray("xml/video/caption");
	private final static String[] xmlThumbnail = XMLUtil.toPathArray("xml/video/thumbnail");
	private final static String[] xmlTime = XMLUtil.toPathArray("xml/request_signature_expires");
	private final static String[] xmlSig = XMLUtil.toPathArray("xml/request_signature");
	private final static String[] xmlClipId = XMLUtil.toPathArray("xml/video/nodeId");
	
	private boolean isHDQuality; // lo,hi
	
	/**
	 * Parse video items from the html page buffer.
	 * @param buffer
	 */
	public VimeoMediaDAO(String buffer, String quality) {
		super(buffer);
		isHDQuality = "hi".equalsIgnoreCase(quality);
		
		// 0=jan,1=feb,..,11=dec
		SimpleDateFormat sdfLong = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);		
		DateFormatSymbols sdfSymbols = sdfLong.getDateFormatSymbols();
		monthNames = sdfSymbols.getShortMonths();
	}
	
	/**
	 * Parse video items from rss xml.
	 * @param reader
	 */
	public VimeoMediaDAO(Reader reader, String quality) {
		this((String)null, quality);
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
			item.setChannel("Vimeo");
			item.setSubtitle("");
			
			item.setMediaPageUrl( getMediaPageUrl(XMLUtil.getText(elem, rssLink)) );
			item.setThumbnailUrl( XMLUtil.getText(elem, rssThumbnail) );
			
			// drop "<![CDATA[" and "]]" tags, try to parse plain text part
			String desc = XMLUtil.getText(elem, rssDesc).trim();
			if (desc.startsWith("<![CDATA["))desc = desc.substring(9);
			if (desc.endsWith("]]"))desc = desc.substring(0, desc.length()-2);

			//<p><a href="http://vimeo.com/357754" title="WET GRASS HD"><img src="http://30.media.vimeo.com/d1/5/11/90/05/11900577/11900577_160x120.jpg" alt="WET GRASS HD" /></a></p>
			//<p>a quick experiment,</p>
			//<p>Cast: <a href="http://vimeo.com/indirect" style="color: #2786c2; text-decoration: none;">IK</a></p>
			buffer = desc;
			int indicies[] = getFieldBoundaries(0, buffer.length(), "</p>", "<p>");
			if (indicies != null) {
				indicies = getFieldBoundaries(indicies[1]-3, buffer.length(), "<p>", "</p>");
				if (indicies != null) {
					desc = buffer.substring(indicies[0], indicies[1]).trim();
					if (desc.endsWith("<br>")) desc = desc.substring(0, desc.length()-4);
					else if (desc.endsWith("<br/>")) desc = desc.substring(0, desc.length()-5);
					else if (desc.endsWith("<br />")) desc = desc.substring(0, desc.length()-6);
				}
			}			
			item.setDescription(desc);
			
			// <pubDate>Sun, 22 Jun 2008 10:50:10 -0400</pubDate> -> 22 June 2008 17:50 EET
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
			
			// Additional fields are set later in a second step
			item.setTitle("");
			item.setMediaFileUrl("");
			
			list.add(item);
		}
		
		return list;
	}
	
	private List<MediaFile> listHtml() {
		//FIXME: todo html parser
		List<MediaFile> list = new ArrayList<MediaFile>(32);
		return list;
	}
	
	/**
	 * Update additional fields from xml document.
	 * @param item
	 * @param buffer
	 */
	public void updateAdditionalFields(MediaFile item, Reader reader) {
		doc = XMLUtil.createDocument(reader);
		
		item.setTitle( XMLUtil.getText(doc, xmlTitle) );
		
		if (item.getThumbnailUrl() == null) {
			// http://images.vimeo.com/84/89/22/84892264/84892264_640x360.jpg
			// -> http://images.vimeo.com/84/89/22/84892264/84892264_160x120.jpg
			String sTemp = XMLUtil.getText(doc, xmlThumbnail);
			if (sTemp == null) sTemp = "";
			if (!sTemp.equals("")) {
				int idx = sTemp.lastIndexOf("_");
				if (idx > 0)
					sTemp = sTemp.substring(0, idx) + "_160x120.jpg";
			}
			item.setThumbnailUrl(sTemp);
		}

		String sTime = XMLUtil.getText(doc, xmlTime);
		String sSig  = XMLUtil.getText(doc, xmlSig);
		String sClipId= XMLUtil.getText(doc, xmlClipId);

		//http://www.vimeo.com/moogaloop/play/clip:690607/51b0976ae9eeff3f2df9400e8894bc44/1237046400/?q=hd		
		item.setMediaFileUrl(VimeoConverter.ROOT_URL + "/moogaloop/play/clip:" + sClipId 
				+ "/" + sSig + "/" + sTime 
				+ (isHDQuality ? "/?q=hd" : "")
		);
	}
	
	@Override
	public void updateAdditionalFields(MediaFile item, String buffer) {
		throw new IllegalArgumentException("Not Implemented");
	}

	private String getMediaPageUrl(String url) {
		// original : http://vimeo.com/channels/documentaryfilm#2244465
		// change to: http://vimeo.com/2244465
		for(int idx=url.length()-1; idx > 0; idx--) {
			if (!Character.isDigit(url.charAt(idx))) {
				String clipId = url.substring(idx+1);
				url = VimeoConverter.ROOT_URL + "/" + clipId;
				break;
			}
		}
		return url;
	}
	
}
