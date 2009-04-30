/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Areena video item
 */
public class MediaFile {
	private int type; // 0=video, 1=audio
	
	private String thumbnailUrl;
	private String mediaPageUrl;
	private String mediaFileUrl;
	private String title;
	private String subtitle;
	private String description;
	//private List<String> categories; //TODO: implement later
	private String channel;
	private Calendar published; // UTC calendar
	
	public void setType(int type) { this.type = type; }
	public int getType() { return type; }
	public boolean isVideo() { return type == MediaDAO.VIDEO; }
	public boolean isAudio() { return type == MediaDAO.AUDIO; }
	
	public void setThumbnailUrl(String url) { thumbnailUrl = url; }
	public String getThumbnailUrl() { return thumbnailUrl; }
	
	public void setMediaPageUrl(String url) { mediaPageUrl = url; }
	public String getMediaPageUrl() { return mediaPageUrl; }
	
	public void setMediaFileUrl(String url) { mediaFileUrl = url; }
	public String getMediaFileUrl() { return mediaFileUrl; }
	
	public void setTitle(String value) { title = value; }
	public String getTitle() { return title; }
	
	public void setSubtitle(String value) { subtitle = value; }
	public String getSubtitle() { return subtitle; }
	
	public void setDescription(String value) { description = value; }
	public String getDescription() { return description; }
	
	public void setChannel(String value) { channel = value; }
	public String getChannel() { return channel; }
	
	public void setPublished(Calendar cal) { published = cal; }
	public Calendar getPublished() { return published; }
	
	/**
	 * Get combined title and published datetime (locale dependent mask).
	 * @return
	 */
	public String getTitleAndPublished() {
		if (published != null) {
			// "27.2.2009 13:45" or "27.2.2009" if timestamp on 00:00:00 
			return getTitle() + ", " + formatShortDateTime(published);
		} else {
			return getTitle();
		}
	}
	
	/**
	 * Returns system's locale and timezone datetime string.
	 * @param cal
	 * @return
	 */
	private String formatShortDateTime(Calendar cal) {
		DateFormat sdf;
		if (cal.get(Calendar.HOUR_OF_DAY) > 0 ||
				cal.get(Calendar.MINUTE) > 0 ||
				cal.get(Calendar.SECOND) > 0 ) {
			sdf = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		} else {
			sdf = DateFormat.getDateInstance(DateFormat.SHORT);	
		}
		//sdf.setTimeZone(cal.getTimeZone());
		return sdf.format(cal.getTime());	   
	}
		
}
