/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;

/**
 * TheOnion html page.
 */
public class TheOnionMediaDAO extends MediaDAO {
	private final String START_ITEM = "<a ";
	private final String END_ITEM   = "</a>";
	
	/**
	 * Parse video items from the html page buffer.
	 * @param buffer
	 */
	public TheOnionMediaDAO(String buffer) {
		super(buffer);
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
		
		if (buffer == null || buffer.length() < 1) 
			return list;

		int startIdx = 0; // inclusive
		int endIdx   = 0; // exclusive

		// loop all content/video/* mediaPage urls
		while(startIdx >= 0) {
			// loop all "<a href..." links, take content/video/* links
			startIdx = buffer.indexOf(START_ITEM, endIdx);
			if (startIdx < 0) break;
			endIdx   = buffer.indexOf(END_ITEM, startIdx+START_ITEM.length());
			if (endIdx < 0) break;
			endIdx += END_ITEM.length();

			String ahref = buffer.substring(startIdx, endIdx);

			String title = null;
			String thumbUrl = null;
			if (ahref.contains("class=\"thumbnail\"")) {
				thumbUrl = getThumbnailUrl(startIdx, endIdx);
			} else {
				title = getTitle(startIdx, endIdx);
			}

			String url = getMediaPageUrl(startIdx, endIdx);
			if (url == null || url.equals(""))
				continue; // unknown video link, skip
			
			// is distinct link, we don't want duplicates in a list.
			// Take thumbnail from the last link.
			boolean updateOld = false;
			MediaFile item = null;			
			for (int tempIdx = 0; tempIdx < list.size(); tempIdx++) {
				MediaFile tempItem = list.get(tempIdx);
				if (tempItem.getMediaPageUrl().equals(url)) {
					updateOld = true;
					item = tempItem;
					break;
				}
			}
			if (item == null) item = new MediaFile();

			if (updateOld) {
				if (thumbUrl != null && !thumbUrl.equals(""))
					item.setThumbnailUrl(thumbUrl);
				if (title != null && !title.equals(""))
					item.setTitle(title);
			} else {
				item.setMediaPageUrl(url);
				item.setTitle(title);
				item.setThumbnailUrl(thumbUrl);

				item.setType(MediaDAO.VIDEO);
				item.setChannel("TheOnion");
				item.setSubtitle("");
				
				// Additional fields are set later in a second step
				item.setDescription("");
				item.setMediaFileUrl("");
				item.setPublished(null);

				list.add(item);				
			}
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
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;

		item.setDescription(item.getTitle());

		// mediaFileUrl was already updated in Converter class
		String sUrl = item.getMediaFileUrl();
		
		// try to parse "yyyy/mm/dd" from url
		int endIdx = sUrl.lastIndexOf('/');
		int startIdx = endIdx-10;
		if (startIdx > 0) {
			String sDate = sUrl.substring(startIdx, endIdx);
			if (sDate.charAt(4) == '/' && sDate.charAt(7) == '/') {
				try {
					int year = Integer.parseInt(sDate.substring(0, 4));
					int month= Integer.parseInt(sDate.substring(5, 7));
					int day  = Integer.parseInt(sDate.substring(8));
					
					Calendar cal = Calendar.getInstance(); // use system timezone
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, month-1); // jan=0, feb=1, ..
					cal.set(Calendar.DAY_OF_MONTH, day);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);				
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					
					item.setPublished(cal);
				} catch (Exception ex) { }
			}
		}
	}

	private String getThumbnailUrl(int itemStartIdx, int itemEndIdx) {
		// <a class="thumbnail" href="/content/video/experts_agree_giant_razor_clawed" ><img src="http://www.theonion.com/content/files/images/GIANT_CRABS_tabs.tabs.jpg" alt="Giant Crabs" title="Giant Crabs"  width="125" /></a>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, "<img ", ">");
		if (indicies == null) return null;

		indicies = getFieldBoundaries(indicies[0], indicies[1], "src=\"", "\"");
		if (indicies == null)
			return null;
		
		return buffer.substring(indicies[0], indicies[1]);
	}

	private String getMediaPageUrl(int itemStartIdx, int itemEndIdx) {
		// <a class="thumbnail" href="/content/video/experts_agree_giant_razor_clawed" ><img src="http://www.theonion.com/content/files/images/GIANT_CRABS_tabs.tabs.jpg" alt="Giant Crabs" title="Giant Crabs"  width="125" /></a>
		// <a class="title" href="/content/video/experts_agree_giant_razor_clawed" >Experts Agree Giant, Razor-Clawed Bioengineered Crabs Pose No Threat</a>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, "href=\"", "\"");		
		if (indicies == null) return "";

		String value = buffer.substring(indicies[0], indicies[1]);
		int idx = value.indexOf("/content/video/");
		if (idx < 0) return "";

		String urlPart1 = value.substring(idx); // /content/video/xxxxxx

		if (urlPart1.equals("/content/video/")) return "";
		else if (urlPart1.indexOf('&') > 0) return "";
		else if (urlPart1.indexOf('?') > 0) return "";
		
		return TheOnionConverter.ROOT_URL + urlPart1;
	}

	private String getTitle(int itemStartIdx, int itemEndIdx) {
		// <a class="title" href="/content/video/experts_agree_giant_razor_clawed" >Experts Agree Giant, Razor-Clawed Bioengineered Crabs Pose No Threat</a>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, ">", "<");		
		if (indicies == null) return "";
		return buffer.substring(indicies[0], indicies[1]);
	}
	
}
