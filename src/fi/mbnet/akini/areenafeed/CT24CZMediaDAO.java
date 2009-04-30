/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;

/**
 * CT24 Czech tv
 */
public class CT24CZMediaDAO extends MediaDAO {
	private final String MEDIAPAGE_START_TAG = "<div id=\"webCast\" ";
	private final String MEDIAPAGE_END_TAG   = "<p class=\"programmeLink\">";
	
	private final String PRG_LIST = "<div id=\"programmeList\">";
	
	private final String DESC_LIVE  = "<div id=\"liveProgrammeDescription\">";
	private final String DESC= "<div id=\"programmeDescription\">";
	
	private final String MEDIAFILEURL = "<embed id=\"vPlayer\"";
	
	private String pageUrl; // mediaPage url
	
	public CT24CZMediaDAO(String buffer, String pageUrl) {
		super(buffer);
		this.pageUrl= pageUrl;
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

		final String channel = "\u010CT24"; // CT24

		MediaFile item = new MediaFile();
		item.setType(MediaDAO.VIDEO);
		item.setChannel(channel);
		item.setSubtitle("");
		item.setThumbnailUrl("");
		
		// first item is read from current mediaPageUrl, url given by a user
		item.setMediaPageUrl(pageUrl);

		list.add(item);

		// loop previous programmes from the right-side list, 
		// add to list in recent to oldest ordering
		// <li> <a href="/vysilani/2009/03/22/209411058100012-05:05-prizma/">05:05 yyyy</a> </li>
		// <li> <a href="/vysilani/2009/03/22/209411030920322-05:32-zajimavosti-z-regionu/">05:32 xxx</a> </li>
		int indicies[] = getFieldBoundaries(0, buffer.length(), PRG_LIST, null);
		if (indicies != null) {
			while(true) {
				indicies = getFieldBoundaries(indicies[0], indicies[1], "<a href=\"", "\"");
				if (indicies == null) break;

				String sUrl = buffer.substring(indicies[0], indicies[1]).trim();
				indicies[0] = indicies[1];
				indicies[1] = buffer.length();
				
				if (sUrl.equals("")) continue; // skip empty link
				
				item = new MediaFile();
				item.setType(MediaDAO.VIDEO);
				item.setChannel(channel);
				item.setSubtitle("");
				item.setThumbnailUrl("");

				// append domain
				if (!sUrl.toLowerCase().startsWith("http://") && 
							!sUrl.toLowerCase().startsWith("https://")) {
					sUrl = CT24CZConverter.ROOT_URL + (sUrl.charAt(0) != '/' ? "/" : "") + sUrl;
				}
				
				item.setMediaPageUrl(sUrl);
				
				list.add(1, item); // reverse ordering to get a recent to oldest ordering
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

		item.setMediaFileUrl( getMediaFileUrl() );
		
		String[] titlePublishedDesc = getMediaTitleAndPublishedAndDescription();
		item.setTitle(titlePublishedDesc[0]);
		item.setDescription(titlePublishedDesc[2]);

		// time is CET+1h 
		Calendar cal = null;
		if (!titlePublishedDesc[1].equals("")) {
			// 21. 3. 2009 11:05
			String sDate = titlePublishedDesc[1].trim();
			cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Prague"));
			
			int idxA = sDate.indexOf('.');
			int day = Integer.parseInt( sDate.substring(0, idxA).trim() );
			int idxB = sDate.indexOf('.', idxA+1);
			int month = Integer.parseInt( sDate.substring(idxA+1, idxB).trim() );
			
			sDate = sDate.substring(idxB+1).trim();
			idxA = sDate.indexOf(' ');
			int year = Integer.parseInt( sDate.substring(0, idxA).trim() );
			
			idxB = sDate.indexOf(':', idxA+1);
			int hour = Integer.parseInt( sDate.substring(idxB-2, idxB).trim() );
			int min  = Integer.parseInt( sDate.substring(idxB+1, idxB+3).trim() );
			
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month-1); // jan=0, feb=1, ..
			cal.set(Calendar.DAY_OF_MONTH, day);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, min);				
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		item.setPublished(cal);
	}
	
	public void updateMediaFileUrl(MediaFile item, String buffer, String medialink) {
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return;

		// jump to first entry position
		String lcBuffer = buffer.toLowerCase();
		int refIdx = lcBuffer.indexOf("<entry>");
		if (refIdx < 0) return;
		refIdx = lcBuffer.indexOf("<ref", refIdx);
		if (refIdx < 0) return;

		// <REF HREF="http://someserver.com/videofile.wmv"></REF>
		int[] indicies = getFieldBoundaries(refIdx, buffer.length(), "=\"", "\"");		
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

	private String[] getMediaTitleAndPublishedAndDescription() {
		String values[] = new String[3];
		values[0] = ""; // title
		values[1] = ""; // datetime string, original html value
		values[2] = ""; // description

		// live   : <div id="liveProgrammeDescription">....</div>
		// archive: <div id="programmeDescription">....</div>
		boolean isLive=false;
		
		int[] indicies = getFieldBoundaries(0, buffer.length(), DESC, "</div>");
		if (indicies == null) {
			isLive = true;
			indicies = getFieldBoundaries(0, buffer.length(), DESC_LIVE, "</div>");			
		}
		if (indicies == null) return values;

		// live   : <h3>Film 2009</h3>
		// archive: <h2>FILM 2009</h2>
		int valueIndicies[] = getFieldBoundaries(indicies[0], indicies[1], 
				isLive ? "<h3>" : "<h2>", 
				isLive ? "</h3>" : "</h2");		
		if (valueIndicies == null) return values;

		values[0] = buffer.substring(valueIndicies[0], valueIndicies[1]) + (isLive ? " (live)" : "");
		
		
		// live   : <p>21. 3. 2009 11:05</p>
		// archive: <p id="programmeTime"><strong>21. 3. 2009 11:05</strong></p>
		valueIndicies = getFieldBoundaries(valueIndicies[1], indicies[1], 
				isLive ? "<p>" : "<strong>", 
				isLive ? "</p>" : "</strong");		
		if (valueIndicies != null)
			values[1] = buffer.substring(valueIndicies[0], valueIndicies[1]);
		else
			valueIndicies = new int[] { indicies[0], indicies[1] }; // NPE failsafe
		
		// live   : <p>xxx live desc</p>
		// archive: <p>xxx archive desc</p>
		valueIndicies = getFieldBoundaries(valueIndicies[1], indicies[1], "<p>", "</p>"); 		
		if (valueIndicies != null)
			values[2] = buffer.substring(valueIndicies[0], valueIndicies[1]);
		
		return values;		
	}

	private String getMediaFileUrl() {
		// <embed id="vPlayer"....src="http://xxx.asx"....</embed>
		int[] indicies = getFieldBoundaries(0, buffer.length(), MEDIAFILEURL, "</embed>");
		if (indicies == null) return "";
		
		int[] valueIndicies = getFieldBoundaries(indicies[0], indicies[1], "src=\"", "\"");
		if (valueIndicies == null) return "";

		return buffer.substring(valueIndicies[0], valueIndicies[1]).trim();		
	}
	
}
