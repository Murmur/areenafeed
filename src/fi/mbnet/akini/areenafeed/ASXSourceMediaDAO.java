/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;
import org.w3c.dom.*;

import fi.mbnet.akini.util.XMLUtil;


/**
 * Vimeo html/rss page.
 */
public class ASXSourceMediaDAO extends MediaDAO {
	private Document doc; // asx.xml feed
	private final static String[] xmlTitle = XMLUtil.toPathArray("title");
	private final static String[] xmlRef = XMLUtil.toPathArray("ref/@href");
	private final static String[] xmlThumbnail = XMLUtil.toPathArray("thumbnail");	
	private final static String[] xmlDesc = XMLUtil.toPathArray("abstract");	
	
	/**
	 * Parse video items from rss xml.
	 * @param reader
	 */
	public ASXSourceMediaDAO(char[] cbuf) {
		super(null);

		//NOTE: quick hack to skip UTF-8 BOM marker char
		int startIdx=0;
		for(int idx=0; idx < cbuf.length; idx++) {
			if (cbuf[idx] == '<') break;
			startIdx++;
		}
		if (startIdx > 0)
			cbuf = Arrays.copyOfRange(cbuf, startIdx, cbuf.length);
		
		// XML is case-sensitive but quite many .asx documents  
		// use a random case. We must lowercase a document.
		doc = XMLUtil.createDocument(cbuf);
		doc = XMLUtil.createDocument( XMLUtil.toLowerCase(doc) );
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
		List<MediaFile> list = new ArrayList<MediaFile>(32);
		
		List<Element> elements = XMLUtil.getElements(doc, XMLUtil.toPathArray("/asx/entry"));
		if (elements == null || elements.size() < 1)
			return list;

		int elemIdx=0;
		for(Element elem : elements) {
			elemIdx++;
			MediaFile item = new MediaFile();

			item.setType(MediaDAO.VIDEO);
			item.setChannel("");
			item.setSubtitle("");

			String sTemp = XMLUtil.getText(elem, xmlTitle);
			item.setTitle( sTemp != null && !sTemp.equals("") ? sTemp.trim() : "Item " + elemIdx );

			sTemp = XMLUtil.getText(elem, xmlDesc);
			item.setDescription( sTemp != null ? sTemp.trim() : "" );

			sTemp = XMLUtil.getText(elem, xmlRef);
			item.setMediaFileUrl( sTemp != null ? sTemp.trim() : "" );
			item.setMediaPageUrl(item.getMediaFileUrl());
			
			// this is not an official .asx element, but you may create
			// own .asx "playlist" and give a thumbnail for each clip.
			sTemp = XMLUtil.getText(elem, xmlThumbnail); 
			item.setThumbnailUrl( sTemp != null ? sTemp.trim() : "" );

			// Additional fields are set later in a second step
			item.setPublished(null);
			
			list.add(item);
		}
		
		return list;
	}
	
	/**
	 * Update additional fields from xml document.
	 * @param item
	 * @param buffer
	 */
	@Override
	public void updateAdditionalFields(MediaFile item, String buffer) {
		// do nothing
	}
	
	public void updateMediaFileUrl(MediaFile item, String medialink) {
		String value = item.getMediaFileUrl();
		
		if (value == null) {
			value = "";
		} else if (medialink.equalsIgnoreCase("asxref")) {
			// use value as is
		} else if (medialink.equalsIgnoreCase("asxrefmms")) {
			// convert to mms:// address, this is VLC streaming fix from YLEAreena site
			int idx = value.indexOf(":");
			if (idx > 0)
				value = "mms" + value.substring(idx);
		} else {
			// "url" or unknown medialink type, do not update field
			return;
		}
		item.setMediaFileUrl(value);
	}

}
