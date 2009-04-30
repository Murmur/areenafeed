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

import fi.mbnet.akini.util.StringUtil;
import fi.mbnet.akini.util.ConvertUtil;

/**
 * Parse html pages
 */
public class AppleTrailersMediaDAO extends MediaDAO {
	//private final String MEDIAPAGE_START_TAG = "<head>";
	//private final String MEDIAPAGE_END_TAG   = "<div class=\"clearer\">";

	private final String ITEM_START = "<a href=\"";
	private final String ITEM_END   = "</h3>";
	private final String THUMBNAILURL = "<img src=\"";
	
	private final String DESC = "<meta name=\"Description\"";

	private String rootUrl, quality;
	
	private String[] monthNames;	
	
	/**
	 * Parse video items from the areena html page buffer.
	 * @param buffer
	 * @param rootUrl
	 */
	public AppleTrailersMediaDAO(String buffer, String rootUrl, String quality) {
		super(buffer);
		this.rootUrl = rootUrl;
		this.quality = quality;
		
		// 0=january,1=february,..,11=december
		SimpleDateFormat sdfLong = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.LONG, Locale.US);		
		DateFormatSymbols sdfSymbols = sdfLong.getDateFormatSymbols();
		monthNames = sdfSymbols.getMonths();
	}
	
	@Override
	public String getMediaPageStartTag() { return null; }// return MEDIAPAGE_START_TAG; }

	@Override
	public String getMediaPageEndTag() { return null; } //return MEDIAPAGE_END_TAG; }
	
	
	/**
	 * Parse video items from the buffer.
	 * @return
	 */
	@Override
	public List<MediaFile> list() {
		List<MediaFile> list = new ArrayList<MediaFile>(32);

		if (buffer == null || buffer.length() < 1) 
			return list;
		
		int[] indicies = new int[2];
		indicies[0] = 0;
		indicies[1] = buffer.length();
		while(true) {
			// <a href="/trailers/independent/battleforterra/"><img src="http://images.apple.com/moviesxml/s/independent/posters/battleforterra_200903131425.jpg" 
			//				width="134" height="193" alt="Battle For Terra"></a>
			// <h3><a href="/trailers/independent/battleforterra/">Battle For Terra</a></h3>
			indicies = getFieldBoundaries(indicies[0], indicies[1], ITEM_START, ITEM_END);
			if (indicies == null) break;

			MediaFile item = new MediaFile();
			item.setType(MediaDAO.VIDEO);
			item.setChannel("Apple Trailers");
			item.setPublished(null);
			
			item.setTitle( getTitle(indicies[0], indicies[1]) );
			item.setMediaPageUrl( getMediaPageUrl(indicies[0], indicies[1]) );
			item.setThumbnailUrl( getThumbnailUrl(indicies[0], indicies[1]) );

			item.setPublished(null);
			
			list.add(item);
			
			indicies[0] = indicies[1];
			indicies[1] = buffer.length();
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
		item.setSubtitle("");
		item.setDescription("");
		item.setMediaFileUrl("");

		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;
		
		item.setDescription( getDescriptionFromPage() );

		// clip buffer
		int startIdx = buffer.indexOf("div id=\"container\">");
		if (startIdx < 0) return;
		buffer = buffer.substring(startIdx);
		
		if (StringUtil.isEmpty(item.getTitle()))
			item.setTitle(getTitleFromPage());

		if (StringUtil.isEmpty(item.getThumbnailUrl()))
			item.setThumbnailUrl(getThumbnailUrlFromPage());

		if (item.getPublished() == null)
			item.setPublished( getPublishedFromPage() );
		
		item.setMediaFileUrl( getMediaFileUrlFromPage() );
	}

	private String getTitle(int startIdx, int endIdx) {
		// <a href="/trailers/independent/battleforterra/"><img src="http://images.apple.com/moviesxml/s/independent/posters/battleforterra_200903131425.jpg" 
		//				width="134" height="193" alt="Battle For Terra"></a>
		// <h3><a href="/trailers/independent/battleforterra/">Battle For Terra</a></h3>
		int[] indicies = getFieldBoundaries(startIdx, endIdx, "<h3>", "</a>");
		if (indicies == null) return "";
		indicies = getFieldBoundaries(indicies[0], endIdx, "\">", "</a>");
		return (indicies != null ? 
				ConvertUtil.XMLDecode(buffer.substring(indicies[0], indicies[1])) : "");	
	}

	private String getMediaPageUrl(int startIdx, int endIdx) {
		// <a href="/trailers/independent/battleforterra/">...</h3>
		int[] indicies = getFieldBoundaries(startIdx, endIdx, null, "\"");
		return (indicies != null ? rootUrl + buffer.substring(indicies[0], indicies[1]) : "");	
	}

	private String getThumbnailUrl(int startIdx, int endIdx) {
		// <a href="/trailers/independent/battleforterra/"><img src="http://images.apple.com/moviesxml/s/independent/posters/battleforterra_200903131425.jpg" 
		//	...</h3>
		int[] indicies = getFieldBoundaries(startIdx, endIdx, THUMBNAILURL, "\"");
		return (indicies != null ? 
				buffer.substring(indicies[0], indicies[1]) : "");	
	}


	private String getDescriptionFromPage() {
		// <meta name="Description" content="The film tells...">
		// <META NAME="description" CONTENT="Watch...">
		int endOfHeader = buffer.indexOf("</head>");

		int[] indicies = getFieldBoundaries(0, endOfHeader, DESC, "\">");
		if (indicies == null) {
			// use temporary lowrcase buffer
			String tempBuffer = buffer.substring(0, endOfHeader).toLowerCase();
			indicies = MediaDAO.getFieldBoundaries(tempBuffer, 0, endOfHeader, DESC.toLowerCase(), "\">");
			if (indicies == null) return "";
			indicies = MediaDAO.getFieldBoundaries(tempBuffer, indicies[0], indicies[1], "content=\"", null);
		} else {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "content=\"", null);
		}
		
		return (indicies != null ? 
				ConvertUtil.XMLDecode(buffer.substring(indicies[0], indicies[1])) : "");	
	}

	private String getTitleFromPage() {
		//<div id="header" class="clearfix">
		//<h1>Where the Wild Things Are</h1>...</div>		
		int[] indicies = getFieldBoundaries(0, buffer.length(), "id=\"header\"", "</div>");
		if (indicies == null) return "";
		indicies = getFieldBoundaries(indicies[0], indicies[1], "<h1>", "</h1>");
		return (indicies != null ? 
				ConvertUtil.XMLDecode(buffer.substring(indicies[0], indicies[1])) : "");	
	}
	
	private Calendar getPublishedFromPage() {
		//<div id="header" class="clearfix">
		//  <h1>Where the Wild Things Are</h1>
		//  <strong class="notyetrated">In theaters: October 16, 2009</strong>
		//  <span>Copyright &copy; 2009 Warner Bros. Pictures</span>
		//</div>
		int[] indicies = getFieldBoundaries(0, buffer.length(), "id=\"header\"", "</div>");
		if (indicies == null) return null;
		indicies = getFieldBoundaries(indicies[0], indicies[1], "theaters:", "<");
		if (indicies == null) return null;
		
		String sDate = buffer.substring(indicies[0], indicies[1]).trim();

		int idx = sDate.indexOf(' ');
		if (idx < 0) return null;

		try {
			String sMonth = sDate.substring(0, idx);
			sDate = sDate.substring(idx+1);
	
			int month = -1;
			int day = -1;
			int year = -1;
			
			for(int tempIdx=0; tempIdx < monthNames.length; tempIdx++) {
				if (sMonth.equalsIgnoreCase(monthNames[tempIdx])) {
					month = tempIdx+1; // 1...n
					break;
				}
			}
			
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
			return cal;
		} catch (Exception ex) {
			return null;
		}
	}
	

	private String getThumbnailUrlFromPage() {
		//<div id="poster">
		//   <span class="enlarge"></span>
		//   <img src="http://images.apple.com/trailers/wb/images/wherethewildthingsare_200903251126.jpg" width="134" height="193" alt="Where the Wild Things Are Poster" class="left">	
		//</div>		
		int[] indicies = getFieldBoundaries(0, buffer.length(), "id=\"poster\"", "</div>");
		if (indicies == null) return "";
		indicies = getFieldBoundaries(indicies[0], indicies[1], "img src=\"", "\"");
		return (indicies != null ? 
				buffer.substring(indicies[0], indicies[1]) : "");	
	}

	private String getMediaFileUrlFromPage() {
		int[] indicies = getFieldBoundaries(0, buffer.length(), "class=\"trailer-content\"", null);
		if (indicies == null) return "";

		// lookup an appropriate video link by quality parameter
		// <li><a href="http://movies.apple.com/movies/universal/fastandfurious/fastandfurious-fte2_h.320.mov?width=320&amp;height=180" class="small"><img src="http://movies.apple.com/trailers/images/hud_button_text_small.png" width="43" height="17" alt="Small"></a></li>
	    // <li><a href="http://movies.apple.com/movies/universal/fastandfurious/fastandfurious-fte2_h.480.mov?width=480&amp;height=272" class="medium"><img src="http://movies.apple.com/trailers/images/hud_button_text_medium.png" width="43" height="17" alt="Medium"></a></li>
	    // <li><a class="hd" href="http://movies.apple.com/movies/universal/fastandfurious/fastandfurious-fte2_480p.mov" class="480p"><img src="http://movies.apple.com/trailers/images/hud_button_text_480p.png" width="43" height="17" alt="480p"></a></li>
		int[] linkIndicies = new int[] { 0, indicies[0] }; // fix first "< href" lookup index 
		int[] valueIndicies = null;
		while(true) {
			indicies[0] = linkIndicies[1]; // lookup next "<a href" index
			indicies[1] = buffer.length();
			
			linkIndicies = getFieldBoundaries(indicies[0], indicies[1], "<a ", ">");
			if (linkIndicies == null) break;

			int[] classIndicies = getFieldBoundaries(linkIndicies[0], linkIndicies[1], "class=\"", "\"");
			if (classIndicies == null) continue;
			String classValue = buffer.substring(classIndicies[0], classIndicies[1]);
			if (classValue.equalsIgnoreCase("ipod")) continue; // skip ipod link
			if (classValue.equalsIgnoreCase("hd")) {
				// lookup second class attribute
				classIndicies = getFieldBoundaries(classIndicies[1], linkIndicies[1], "class=\"", "\"");
				if (classIndicies == null) continue;
				classValue = buffer.substring(classIndicies[0], classIndicies[1]);				
			}

			// default to first video link
			if (valueIndicies == null) valueIndicies = linkIndicies;

			if (classValue.equalsIgnoreCase(quality)) {
				valueIndicies = linkIndicies;
				break;
			}
		}

		if (valueIndicies != null) {
			// NOTE: apple is using html entitys in a url encoding.
			indicies = getFieldBoundaries(valueIndicies[0], valueIndicies[1], "href=\"", "\"");
			return (indicies != null ? 
					ConvertUtil.XMLDecode(ConvertUtil.URLDecode(buffer.substring(indicies[0], indicies[1]))) : "");
		} else {
			return "";
		}
	}
	
}
