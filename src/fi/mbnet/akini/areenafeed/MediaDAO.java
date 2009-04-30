/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.List;

/**
 * 
 */
public abstract class MediaDAO {
	public static final int VIDEO = 0;
	public static final int AUDIO = 1;	
	
	protected String buffer;
	
	/**
	 * Create page buffer where all mediapage links are read from.
	 * @param buffer
	 */
	public MediaDAO(String buffer) {
		this.buffer = buffer;
	}
	
	/**
	 * Get start delimiter of the mediapage content.
	 * This is used by converter class to split stream.
	 * @return
	 */
	public abstract String getMediaPageStartTag();
	
	/**
	 * Get end delimiter of the mediapage content.
	 * This is used by converter class to split stream.
	 * @return
	 */
	public abstract String getMediaPageEndTag();
	
	
	/**
	 * List mediafile urls from the buffer.
	 * Buffer was given in a constructor.
	 * @return
	 */
	public abstract List<MediaFile> list();
	
	/**
	 * Update fields from the mediapage buffer.
	 * @param item
	 * @param body
	 */
	public abstract void updateAdditionalFields(MediaFile item, String buffer);	
	
	/**
	 * Get index boundaries within a buffer, boundary is set inside the delimiters.
	 * Example:
	 *    buffer="aaaBBBccc", startDelim="aaa", endDelim="ccc" -> int[0]=3, int[1]=6
	 * @param itemStartIdx	start boundary within buffer
	 * @param itemEndIdx	end boundary within buffer
	 * @param START_FIELD	delimiter
	 * @param END_FIELD		delimiter
	 * @return	int array: [0]=startOfIndex inclusive, [1]endOfIndex exclusive. 
	 * 			or NULL if field not found
	 */
	protected int[] getFieldBoundaries(int itemStartIdx, int itemEndIdx,
				String START_FIELD, String END_FIELD) {
		/*int startIdx;
		if (START_FIELD == null) {
			startIdx = itemStartIdx;
		} else {
			startIdx = buffer.indexOf(START_FIELD, itemStartIdx);
			if (startIdx < 0 || startIdx >= itemEndIdx) return null;
			startIdx = startIdx + START_FIELD.length();
		}
		
		int endIdx;
		if (END_FIELD == null) {
			endIdx = itemEndIdx;
		} else {
			endIdx   = buffer.indexOf(END_FIELD, startIdx);
			if (endIdx < 0 || endIdx >= itemEndIdx) return null;
		}
		
		return new int[] { startIdx, endIdx };*/
		return getFieldBoundaries(buffer, itemStartIdx, itemEndIdx, START_FIELD, END_FIELD);
	}
	
	protected static int[] getFieldBoundaries(String databuffer, int itemStartIdx, int itemEndIdx,
				String START_FIELD, String END_FIELD) {
		int startIdx;
		if (START_FIELD == null) {
			startIdx = itemStartIdx;
		} else {
			startIdx = databuffer.indexOf(START_FIELD, itemStartIdx);
			if (startIdx < 0 || startIdx >= itemEndIdx) return null;
			startIdx = startIdx + START_FIELD.length();
		}
		
		int endIdx;
		if (END_FIELD == null) {
			endIdx = itemEndIdx;
		} else {
			endIdx   = databuffer.indexOf(END_FIELD, startIdx);
			if (endIdx < 0 || endIdx >= itemEndIdx) return null;
		}
		
		return new int[] { startIdx, endIdx };
	}	
	
	/**
	 * Get field value from the buffer
	 * @param itemStartIdx
	 * @param itemEndIdx
	 * @param START_FIELD
	 * @param END_FIELD
	 * @return	field value or empty string
	 */
	protected String getFieldValue(int itemStartIdx, int itemEndIdx,
					String START_FIELD, String END_FIELD) {
		int[] indicies = getFieldBoundaries(itemStartIdx, itemEndIdx, START_FIELD, END_FIELD);
		return (indicies != null ? buffer.substring(indicies[0], indicies[1]) : "");
	}
	
}
