/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;

/**
 * YLE Areena
 * Buffer-based Data Access Object
 */
public class AreenaMediaDAO extends MediaDAO {
	private final String START_ITEM = "<li class=";
	//private final String VIDEO_ITEM = "<li class=\"video\">";
	//private final String AUDIO_ITEM = "<li class=\"audio\">";
	private final String END_ITEM   = "</li>";
	
	private final String MEDIAPAGE_START_TAG = "<div class=\"insert subsection with-line-top\">";
	private final String MEDIAPAGE_END_TAG   = "<div class=\"content fragment2\">";
	
	private final String END = "</span>";
	private final String THUMBNAILURL  = "<span class=\"clip-info thumbnail\">";
	private final String MEDIAPAGEURL  = "<span class=\"clip-info link-to-media\">";
	private final String TITLE = "<span class=\"clip-info title\">";
	private final String DESC  = "<span class=\"clip-info description\">";
	private final String CHANNEL = "<span class=\"clip-info origin-channel\">";

	// additional fields
	private final String PUBLISHED = "<strong class=\"bigger\">Julkaistu Areenassa:</strong>";
	private final String MEDIAFILEURL = "<strong>Ohjelman suora osoite:</strong>";
	
	private String rootUrl, quality;
	
	/**
	 * Parse video items from the areena html page buffer.
	 * @param buffer
	 * @param rootUrl
	 * @param quality
	 */
	public AreenaMediaDAO(String buffer, String rootUrl, String quality) {
		super(buffer);
		this.rootUrl = rootUrl; // http://areena.yle.fi
		this.quality = quality; // lo,hi
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
		List<MediaFile> list = new ArrayList<MediaFile>(32);

		if (buffer == null || buffer.length() < 1) 
			return list;
		
		//NOTE: is Areena.html a well-formatted xhtml, we could use SAX/DOM parser?
		int startIdx = 0; // inclusive
		int endIdx   = 0; // exclusive

		while(startIdx >= 0) {
			// "video"> or "audio"> tag
			startIdx = buffer.indexOf(START_ITEM, endIdx);
			if (startIdx < 0) break;
			endIdx   = buffer.indexOf(END_ITEM, startIdx+START_ITEM.length());
			if (endIdx < 0) break;
			endIdx += END_ITEM.length();
			
			MediaFile item = new MediaFile();
			item.setType( getType(startIdx, endIdx) );
			if (item.getType() < 0) continue; // skip unknown item
			
			item.setThumbnailUrl( getThumbnailUrl(startIdx, endIdx) );
			
			String[] values = getMediaPageUrlAndSubtitle(startIdx, endIdx);
			item.setMediaPageUrl(values[0]);
			item.setSubtitle(values[1]);
			
			item.setTitle( getTitle(startIdx, endIdx) );
			item.setDescription( getFieldValue(startIdx, endIdx, DESC, END).trim() );
			item.setChannel( getChannel(startIdx, endIdx) );

			// Additional MediaFileUrl and Published fields are set later 
			// in a second step, @see AreenaConverter.java
			item.setMediaFileUrl("");
			item.setPublished(null);

			list.add(item);
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
		item.setMediaFileUrl("");
		item.setPublished(null);

		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;
		
		int itemStartIdx = 0; // inclusive
		int itemEndIdx   = buffer.length(); // exclusive

		// <strong>Ohjelman suora osoite:</strong> <a href="http://www.yle.fi/java/areena/dispatcher/1879443.asx?bitrate=1">http://www.yle.fi/java/areena/dispatcher/1879443.asx?bitrate=1</a>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, MEDIAFILEURL, "</a>");
		if (indicies != null) {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "<a href=\"", "\"");
			if (indicies != null)
				item.setMediaFileUrl( buffer.substring(indicies[0], indicies[1]).trim() );
		}
		
		// <strong class="bigger">Julkaistu Areenassa:</strong> 18.02.2009 klo 01:22<br/>
		indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, PUBLISHED, "<");
		if (indicies != null) {
			String sDatetime = buffer.substring(indicies[0], indicies[1]).trim();
			int idx = sDatetime.indexOf("klo");
			if (idx > 0) {
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Helsinki")); // UTC+2

				String[] parts = sDatetime.substring(0, idx).split("\\.");
				cal.set(Calendar.YEAR, Integer.parseInt(parts[2].trim()));
				cal.set(Calendar.MONTH, Integer.parseInt(parts[1].trim())-1); // jan=0, feb=1, ..
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0].trim()));
				
				parts = sDatetime.substring(idx+3).split(":");
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0].trim()));
				cal.set(Calendar.MINUTE, Integer.parseInt(parts[1].trim()));				
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);

				item.setPublished(cal);	
			}
		}
	}
	
	public void updateMediaFileUrl(MediaFile item, String buffer, String medialink) {
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;
		
		// <REF HREF="http://mediak.yle.fi/media/areena/1/56/95/1569521_957849.wmv"></REF>
		int[] indicies = getFieldBoundaries(0, buffer.length(), "=\"", "\"");		
		if (indicies == null) return;
		
		String value = buffer.substring(indicies[0], indicies[1]).trim();
		
		if (medialink.equalsIgnoreCase("asxref")) {
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

	private int getType(int itemStartIdx, int itemEndIdx) {
		// -1=unknown, 0=video, 1=audio
		// <li class="video"> or <li class="audio">
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, "<li class=\"", "\"");
		if (indicies == null) return -1;
		
		String value = buffer.substring(indicies[0], indicies[1]).trim();
		if (value.equalsIgnoreCase("video")) return 0;
		else if (value.equalsIgnoreCase("audio")) return 1;
		else return -1;
	}

	private String getThumbnailUrl(int itemStartIdx, int itemEndIdx) {
		// <span class="clip-info thumbnail"><img src="http://www.yle.fi/areena_kuvat/1/87/94/1879444_1095036.jpg" width="120" height="67" alt="A2 17" /></span>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, THUMBNAILURL, END);
		if (indicies == null) return "";
		
		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "<img src=\"", "\"");
		if (valueIndicies == null) return "";
		
		return buffer.substring(valueIndicies[0], valueIndicies[1]).trim();
	}

	private String[] getMediaPageUrlAndSubtitle(int itemStartIdx, int itemEndIdx) {
		String values[] = new String[2];
		values[0] = "";
		values[1] = "";

		// <span class="clip-info link-to-media"><a href="/toista?id=1879443">A2 17</a></span>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, MEDIAPAGEURL, END);
		if (indicies == null) return values;

		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "<a href=\"", "\"");
		if (valueIndicies == null) return values;
		values[0] = buffer.substring(valueIndicies[0], valueIndicies[1]); // mediaPageURL
		if (!values[0].equals(""))
			values[0] = rootUrl + values[0] + (quality !=  null ? "&quality=" + quality : "");
		
		valueIndicies = getFieldBoundaries(valueIndicies[1], indicies[1], ">", "<");
		if (valueIndicies != null)
			values[1] = buffer.substring(valueIndicies[0], valueIndicies[1]); // subtitle		

		values[0] = values[0].trim();
		values[1] = values[1].trim();
		return values;		
	}
	
	private String getTitle(int itemStartIdx, int itemEndIdx) {
		// <span class="clip-info title"><a href="/toista?id=1879443">Ajankohtainen kakkonen</a></span>
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, TITLE, END);
		if (indicies == null) return "";
		
		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "<a href=\"", "\"");
		if (valueIndicies == null) return "";

		valueIndicies = getFieldBoundaries(valueIndicies[1], indicies[1], ">", "<");
		if (valueIndicies == null) return "";

		return buffer.substring(valueIndicies[0], valueIndicies[1]).trim();		
	}

	private String getChannel(int itemStartIdx, int itemEndIdx) {
		String value = getFieldValue(itemStartIdx, itemEndIdx, CHANNEL, END);
		return (value.startsWith("Esitetty:") ? value.substring(9).trim() : value).trim();
	}

}
