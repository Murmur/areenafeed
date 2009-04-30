/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

/**
 * http://www.clipnabber.com is a site that can parse dozens of
 * video website links to an internal videostream link.
 */
public class ClipNabber {
	public static final String URL = "http://clipnabber.com/gethint.php?sid=987331491210487&mode=1&url=";

	private String buffer;
	
	/**
	 * Escape url to ClipNabber friendly format.
	 * @param url
	 * @return
	 */
	public String encodeUrl(String url) {
		if (url == null) return null;
		url = url.replace("&", "%26");
		url = url.replace("#", "%23");
		return url;		
	}
	
	/**
	 * Parse mediafile url from the ClipNabber html response.
	 * @param buffer
	 * @return
	 */
	public String getMediaFileUrl(String buffer) {
		this.buffer = buffer;
		if (buffer == null || buffer.length() < 1)
			return null;
		
		// <p>&gt;&gt; <a href='http://videos.theonion.com/onion_video/2009/03/12/GIANT_CRABS.flv' ><strong>FLV Download Link</strong></a> &lt;&lt;  
		String sUrl = null;
		int[] indicies = getFieldBoundaries(0, buffer.length(), "&gt;&gt;", "&lt;&lt;");
		if (indicies != null) {
			indicies = getFieldBoundaries(indicies[0], indicies[1], "<a href='", "'");
			if (indicies == null)
				indicies = getFieldBoundaries(indicies[0], indicies[1], "<a href=\"", "\"");
			if (indicies != null)
				sUrl = buffer.substring(indicies[0], indicies[1]);
		}
		
		return sUrl;
	}

	private int[] getFieldBoundaries(int itemStartIdx, int itemEndIdx,
				String START_FIELD, String END_FIELD) {
		// NOTE: this method is copied from MediaDAO class.
		int startIdx = buffer.indexOf(START_FIELD, itemStartIdx);
		if (startIdx < 0 || startIdx >= itemEndIdx) return null;
		startIdx = startIdx + START_FIELD.length();
	
		int endIdx;
		if (END_FIELD == null) {
			endIdx = itemEndIdx;
		} else {
			endIdx = buffer.indexOf(END_FIELD, startIdx);
			if (endIdx < 0 || endIdx >= itemEndIdx) return null;
		}
	
		return new int[] { startIdx, endIdx };
	}
	
}
