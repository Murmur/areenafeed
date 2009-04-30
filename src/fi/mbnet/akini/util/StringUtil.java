/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.util;

public class StringUtil {
	public static final String lineSeparator = System.getProperty("line.separator", "\r\n");
	public static final String fileSeparator = System.getProperty("file.separator", "/");
	public static final String CRLF = "\r\n";	// windows linebreak: 13+10
	public static final String LF   = "\n";	// unix linebreak: 10

	public static boolean isEmpty(String value) {
		return (value == null || value.equals(""));
	}
	
}
