/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import fi.mbnet.akini.util.ConvertUtil;

/**
 * GoogleVideo html page, parse distinct mediapage urls.
 */
public class GoogleVideoMediaDAO extends MediaDAO {
	private final String MEDIAPAGE_START_TAG = "<div id=\"video-tab-container\">";
	private final String MEDIAPAGE_END_TAG   = "</body>";

	// frontpage fields
	private final String START_ITEM = "class=\"video\"";
	private final String END_ITEM   = "class=\"video\"";
	
	//private final String SITE = "class=\"site\"";
	private final String MEDIAPAGEURL = "class=\"canonical_url\"";
	private final String THUMBNAILURL = "class=\"thumbnail-img\"";
	
	private final String TITLE = "<span id=\"details-title\">";
	private final String DESC  = "<p id=\"details-desc\">";
	private final String MEDIAFILEURL = "'/googleplayer.swf?videoUrl\\x3d";
	private final String PUBLISHED = "<span id=\"duration-and-date\">";

	// searchpage fields
	private final String START_ITEMB = "class=\"rl-thumbnail-inner\"";
	
	private String[] monthNames;
	
	/**
	 * Parse video items from the html page buffer.
	 * @param buffer
	 */
	public GoogleVideoMediaDAO(String buffer, String rootUrl) {
		super(buffer);
		
		// 0=jan,1=feb,..,11=dec
		SimpleDateFormat sdfLong = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.LONG, Locale.US);		
		DateFormatSymbols sdfSymbols = sdfLong.getDateFormatSymbols();
		monthNames = sdfSymbols.getShortMonths();		
	}
	
	@Override
	public String getMediaPageStartTag() { return MEDIAPAGE_START_TAG; }

	@Override
	public String getMediaPageEndTag() { return MEDIAPAGE_END_TAG; }
	
	/**
	 * Parse video items from the buffer.
	 * @return
	 */
	@Override
	public List<MediaFile> list() {
		if (buffer.indexOf(START_ITEMB) > 0)
			return listSearchpage();
		else
			return listFrontpage();
	}
	
	public List<MediaFile> listFrontpage() {
		List<MediaFile> list = new ArrayList<MediaFile>(32);

		if (buffer == null || buffer.length() < 1) 
			return list;

		// put delimiters so that while-loop can find all videoslots
		// from video.google.com frontpage.
		buffer = "class=\"video\"" + buffer + "class=\"video\"";		
		
		int startIdx = 0; // inclusive
		int endIdx   = 0; // exclusive

		while(startIdx >= 0) {
			// take chunk from class="video" to class="video" tags
			startIdx = buffer.indexOf(START_ITEM, endIdx);
			if (startIdx < 0) break;
			endIdx   = buffer.indexOf(END_ITEM, startIdx+START_ITEM.length());
			if (endIdx < 0) break;
			endIdx -= 1; // backstep one character, endTag is a start of next chunk
			
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
				item.setChannel( getSite(item.getMediaPageUrl()) );
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
	
	public List<MediaFile> listSearchpage() {
		List<MediaFile> list = new ArrayList<MediaFile>(32);

		if (buffer == null || buffer.length() < 1) 
			return list;

		int startIdx = 0; // inclusive
		int endIdx   = 0; // exclusive

		while(startIdx >= 0) {
			// <div class="rl-thumbnail-inner" onclick="return resultClick('0');"><a href="http://www.youtube.com/watch?v=2XdTEDFi5Qo" target="_top"><img class="thumbnail-img" src="http://2.gvt0.com/vi/2XdTEDFi5Qo/default.jpg"></a></div>
			startIdx = buffer.indexOf(START_ITEMB, endIdx);
			if (startIdx < 0) break;
			endIdx   = buffer.indexOf("</a>", startIdx+START_ITEM.length());
			if (endIdx < 0) break;
			endIdx += 5; // move index forward
			
			// get mediaPageUrl
			int[] indicies = getFieldBoundaries(startIdx, endIdx, "href=\"", "\"");		
			if (indicies == null) continue;

			String url = buffer.substring(indicies[0], indicies[1]).trim(); // mediaPageUrl
			if (url == null || url.equals(""))
				continue; // unknown video link, skip

			// google.video.com links may not have a domain
			String site = getSite(url);
			if (site == null || site.equals(""))
				continue; // unknown video link, skip

			if (site.equals("video.google.com") && url.startsWith("/videoplay?"))
				url = GoogleVideoConverter.ROOT_URL + url;
			
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

			// update thumbnailUrl from "<img..." tag
			String thumbUrl;
			indicies = getFieldBoundaries(indicies[1], endIdx, "<img", null);
			if (indicies == null) continue;
			
			indicies = getFieldBoundaries(indicies[0], indicies[1], "src=\"", "\"");
			if (indicies == null) thumbUrl = "";
			else thumbUrl = buffer.substring(indicies[0], indicies[1]).trim();
			
			if (updateOld) {
				if (thumbUrl != null && !thumbUrl.equals(""))
					item.setThumbnailUrl(thumbUrl);
			} else {
				item.setMediaPageUrl(url);
				item.setType(MediaDAO.VIDEO);
				item.setChannel(site);
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
	 * Update video fields from the mediapage buffer.
	 */
	@Override
	public void updateAdditionalFields(MediaFile item, String buffer) {
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;
		
		int itemStartIdx = 0; // inclusive
		int itemEndIdx   = buffer.length(); // exclusive
		
		// <span id="details-title">Flo-Rida-You spin me right round</span>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, TITLE, "</span");
		if (indicies != null)
			item.setTitle( ConvertUtil.XMLDecode(buffer.substring(indicies[0], indicies[1]).trim()) );
		
		// <p id="details-desc">lolololol</p>
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, DESC, "</p>");
		if (indicies != null)
			item.setDescription( buffer.substring(indicies[0], indicies[1]).trim() );

		// '/googleplayer.swf?videoUrl\x3dhttp://v1.cache4.googlevideo.com/videoplayback%3Fid%3Dad7e34b8941e688a%26itag%3D5%26ip%3D0.0.0.0%26ipbits%3D0%26expire%3D1235504579%26sparams%3Dip,ipbits,expire,id,itag%26signature%3D37D3061579FD37F85034AEC9AF831AA544472849.6F7B1F86A7CBA94F6F7603F0F966FACFB5CA8B64%26key%3Ddk%26begin%3D0%26len%3D3553449%26docid%3D4602171665328041876\x26thumbnailUrl\x3dhttp://2.gvt0.com/ThumbnailServer2%3Fapp%3Dvss%26contentid%3Dad7e34b8941e688a%26offsetms%3D600000%26itag%3Dw320%26hl%3Den%26sigh%3DGNOOhXo7Y_U8wG67hzizzybgIAw\x26docid\x3d4602171665328041876\x26hl\x3den\x26autoplay\x3d1',
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, MEDIAFILEURL, "%26docid%3D");
		if (indicies != null) {
			String stemp = buffer.substring(indicies[0], indicies[1]).trim();
			item.setMediaFileUrl( ConvertUtil.URLDecode(stemp) ); 
		}
		
		// <span id="duration-and-date">- 03:26<span class="date"> - Feb 5, 2009</span></span><br>
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, PUBLISHED, "</span");		
		if (indicies != null) {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "\"date\">", null);			
			if (indicies != null) {
				String sDate = buffer.substring(indicies[0], indicies[1]).trim().toLowerCase();
				if (sDate.length() > 0 && sDate.charAt(0) == '-')
					sDate = sDate.substring(1).trim();
				
				int idx = sDate.indexOf(',');

				int year = -1;
				int month = -1;
				int day  = -1;
				try {
					if (idx > 0) {
						year = Integer.parseInt( sDate.substring(idx+1).trim() );
						day  = Integer.parseInt( sDate.substring(idx-2, idx).trim() );
						sDate =  sDate.substring(0, idx-2).trim(); // "Feb"
						for(int tempIdx=0; tempIdx < monthNames.length; tempIdx++) {
							if (sDate.equalsIgnoreCase(monthNames[tempIdx])) {
								month = tempIdx+1; // 1...n
								break;
							}
						}
					}
				} catch (Exception ex) { }
				
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
		// <img class="thumbnail-img" src="http://2.gvt0.com/vi/9-oIPCH7ba4/default.jpg" width="96" height="72" title="Heath Ledger Won 2009 Oscar Best Supporting Actor Award"/>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, THUMBNAILURL, ">");
		if (indicies == null) return null;
		
		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "src=\"", "\"");
		if (valueIndicies == null) return null;
		
		return buffer.substring(valueIndicies[0], valueIndicies[1]);
	}

	private String getMediaPageUrl(int itemStartIdx, int itemEndIdx) {
		// <div class="canonical_url" style="display: none">http://www.youtube.com/watch?v=9-oIPCH7ba4</div> 
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, MEDIAPAGEURL, "</div>");		
		if (indicies == null) return "";

		indicies = getFieldBoundaries(indicies[0], indicies[1], ">", null);		
		if (indicies == null) return "";
		
		return buffer.substring(indicies[0], indicies[1]);
	}
	
	private String getSite(String url) {
		// take videos from known sites.
		// youtube.com = http://www.youtube.com/watch?v=2XdTEDFi5Qo
		// video.google.com = "/videoplay?docid=963593285556132098&...
		// video.google.com = "http://video.google.com/videoplay?docid=963593285556132098&...		
		url = url.toLowerCase();
		if (url.indexOf("youtube.com/watch?") >= 0) return "youtube.com";
		else if (url.startsWith("/videoplay?")) return "video.google.com";
		else if (url.indexOf("video.google.com/videoplay?") >= 0) return "video.google.com";		
		return "";
	}


}
