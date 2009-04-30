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
import fi.mbnet.akini.util.ConvertUtil;

/**
 * Youtube html page, parse distinct mediapage urls.
 */
public class YoutubeMediaDAO extends MediaDAO {
	private final String START_ITEM = "href=\"/watch?";
	private final String END_ITEM   = "</a>";

	private final String MEDIAPAGE_START_TAG = "<head>";
	private final String MEDIAPAGE_END_TAG   = "<div id=\"watch-category\">";

	private final String TITLE = "id=\"watch-vid-title\"";
	private final String DESC  = "class=\"description\">";
	private final String MEDIAFILEURL = "var fullscreenUrl";
	private final String PUBLISHED = "class=\"watch-video-added post-date\">";
	
	private String[] monthNames;
	
	//private String rootUrl;
	
	/**
	 * Parse video items from the html page buffer.
	 * @param buffer
	 */
	public YoutubeMediaDAO(String buffer, String rootUrl) {
		super(buffer);
		//this.rootUrl = rootUrl;
		
		// 0=january,1=february,..,11=december
		SimpleDateFormat sdfLong = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.LONG, Locale.US);		
		DateFormatSymbols sdfSymbols = sdfLong.getDateFormatSymbols();
		monthNames = sdfSymbols.getMonths();
	}
	
	@Override
	public String getMediaPageStartTag() { return MEDIAPAGE_START_TAG; }

	@Override
	public String getMediaPageEndTag() { return MEDIAPAGE_END_TAG; }
	
	
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

		// loop all href="/watch? mediaPage urls
		while(startIdx >= 0) {
			// <a id="video-url-ZiRgYBHoAoU" href="/watch?v=ZiRgYBHoAoU&feature=PlayList&p=5798677ED93EFEF2&index=2"   rel="nofollow"><img title="LittleBigPlanet : Little Big Computer"    src="http://i3.ytimg.com/vi/ZiRgYBHoAoU/default.jpg" class="vimg120" qlicon="ZiRgYBHoAoU" alt="LittleBigPlanet : Little Big Computer"></a>
			startIdx = buffer.indexOf(START_ITEM, endIdx);
			if (startIdx < 0) break;
			endIdx   = buffer.indexOf(END_ITEM, startIdx+START_ITEM.length());
			if (endIdx < 0) break;
			endIdx += END_ITEM.length();

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

			String thumbUrl = getThumbnailUrl(startIdx, endIdx);
			
			if (updateOld) {
				if (thumbUrl != null && !thumbUrl.equals(""))
					item.setThumbnailUrl(thumbUrl);
			} else {
				item.setMediaPageUrl(url);
				item.setType(MediaDAO.VIDEO);
				item.setChannel("Youtube");
				item.setSubtitle("");
				
				item.setThumbnailUrl(thumbUrl);
				
				// Additional fields are set later in a second step
				item.setTitle("");
				item.setDescription("");
				item.setMediaFileUrl("");
				item.setPublished(null);

				list.add(item);				
			}
		}
		
		return list;
	}
	
	/**
	 * Update additional fields from the subpage buffer.
	 * @param item
	 * @param buffer
	 */
	@Override
	public void updateAdditionalFields(MediaFile item, String buffer) {
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;

		// update thumbnail if link is empty, use blindly default naming standard
		if (item.getThumbnailUrl() == null || item.getThumbnailUrl().equals("")) {
			// mediaPageUrl=http://www.youtube.com/watch?v=40nBZg16PkA
			// thumbnailUrl=http://i4.ytimg.com/vi/40nBZg16PkA/default.jpg
			String stemp = item.getMediaPageUrl();
			int idx = stemp.indexOf('?');
			if (idx > 0) {
				stemp = "&" + stemp.substring(idx+1) + "&";
				int idxStart = stemp.indexOf("&v=");
				if (idxStart >= 0) {
					idx = stemp.indexOf('&', idxStart+3);
					stemp = stemp.substring(idxStart+3, idx);
					item.setThumbnailUrl(YoutubeConverter.THUMB_URL + "/" + stemp + "/default.jpg");
				}
			}
		}
		
		
		int itemStartIdx = 0; // inclusive
		int itemEndIdx   = buffer.length(); // exclusive
		
		// <div id="watch-vid-title" class="title"> <h1 >LittleBigPlanet : Little Big Computer</h1> </div>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, TITLE, "</h1");
		if (indicies != null) {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "<h1", null);
			if (indicies != null) {
				indicies = getFieldBoundaries(indicies[0], indicies[1], ">", null);
				String stemp = ConvertUtil.XMLDecode(buffer.substring(indicies[0], indicies[1]).trim());
				item.setTitle(stemp);
			}
		}
		
		// <span  class="description">i have made an "electronic" 8bit calculator...</span>
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, DESC, "</span>");
		if (indicies != null)
			item.setDescription( buffer.substring(indicies[0], indicies[1]).trim() );

		// var fullscreenUrl = '/watch_fullscreen?fs=1&fexp=900035&vq=None&video_id=8KY6zM8VWz0&l=48&sk=WZDlR09XT0RfhNF3nXyLvZXLEr2qVgvSR&fmt_map=18%2F512000%2F9%2F0%2F115%2C34%2F0%2F9%2F0%2F115&t=vjVQa1PpcFMRF5-2ABLjUAA0gpOlpXDesDAyR1R5bYE%3D&hl=en&plid=AARjm6ixeEczzcdOAAACgAAAAAA&cr=US&title=Little Big Planet Best Online Levels - Crazy Coaster';
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, MEDIAFILEURL, ";");
		if (indicies != null) {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "video_id=", "&title=");
			if (indicies != null)
				item.setMediaFileUrl(YoutubeConverter.ROOT_URL + "/get_video?video_id=" + buffer.substring(indicies[0], indicies[1]).trim() ); 
		}
		
		// <span class="watch-video-added post-date">November 02, 2008</span><br/
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, PUBLISHED, "</span>");		
		if (indicies != null) {
			String sDate = buffer.substring(indicies[0], indicies[1]).trim().toLowerCase();
			if (!sDate.equals("")) {
				int idx = sDate.indexOf(' ');
				String sMonth = sDate.substring(0, idx).trim();
				sDate = sDate.substring(idx+1);
				
				int month = -1;
				for(int tempIdx=0; tempIdx < monthNames.length; tempIdx++) {
					if (sMonth.equalsIgnoreCase(monthNames[tempIdx])) {
						month = tempIdx+1; // 1...n
						break;
					}
				}
				
				int day = -1;
				int year = -1;
				idx = sDate.indexOf(',');
				if (idx > 0) {
					try {
						day  = Integer.parseInt( sDate.substring(0, idx).trim() );
						year = Integer.parseInt( sDate.substring(idx+1).trim() ); 
					} catch (Exception ex) { }
				}

				Calendar cal = null;
				if (year > 0 && month > 0 && day > 0) {
					cal = Calendar.getInstance(); // use system timezone
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, month-1); // jan=0, feb=1, ..
					cal.set(Calendar.DAY_OF_MONTH, day);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);				
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
				}
				
				item.setPublished(cal);
			}
		}
		
	}

	private String getThumbnailUrl(int itemStartIdx, int itemEndIdx) {
		// <a id="video-url-ZiRgYBHoAoU" href="/watch?v=ZiRgYBHoAoU&feature=PlayList&p=5798677ED93EFEF2&index=2"   rel="nofollow"><img title="LittleBigPlanet : Little Big Computer"    src="http://i3.ytimg.com/vi/ZiRgYBHoAoU/default.jpg" class="vimg120" qlicon="ZiRgYBHoAoU" alt="LittleBigPlanet : Little Big Computer"></a>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, "<img", ">");
		if (indicies == null) return null;

		// use either src= or thumb= attribute
		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "thumb=\"", "\"");		
		if (valueIndicies == null)
			valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "src=\"", "\"");
		
		if (valueIndicies == null)
			return null;
		
		return buffer.substring(valueIndicies[0], valueIndicies[1]);
	}

	private String getMediaPageUrl(int itemStartIdx, int itemEndIdx) {
		// <a id="video-url-ZiRgYBHoAoU" href="/watch?v=ZiRgYBHoAoU&feature=PlayList&p=5798677ED93EFEF2&index=2"... 
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, "href=\"", "\"");		
		if (indicies == null) return "";

		String value = buffer.substring(indicies[0], indicies[1]);
		int idx = value.indexOf('?');
		if (idx < 0) return "";

		String urlPart1 = value.substring(0, idx+1) + "v="; // "/watch?=v"
		
		value = "&" + value.substring(idx+1) + "&"; // take videoPageId (v=xxxxx)
		idx = value.indexOf("&v=");
		if (idx < 0) return "";
		idx+=3;
		
		int idxEnd = value.indexOf('&', idx);
		
		return YoutubeConverter.ROOT_URL + urlPart1 + value.substring(idx, idxEnd);
	}

}
