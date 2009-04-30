/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.util;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * Common conversion functions
 */
public class ConvertUtil {

	public static String URLEncode(String s) {
		try {
			return URLEncode(s, "UTF-8");
		} catch (Exception ex) {
			return null; // should never happen
		}
	}

	public static String URLEncode(String s, String enc) 
			throws UnsupportedEncodingException {
		if (s == null) return null;
		return URLEncoder.encode(s, enc);		
	}

	public static String URLDecode(String s, String enc) 
			throws UnsupportedEncodingException {
		if (s == null) return null;
		return URLDecoder.decode(s, enc);
	}

	/**
	 * URLDecode using UTF-8 charset
	 * @param s
	 * @return
	 */
	public static String URLDecode(String s) {
		try {
			return URLDecode(s, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * HTML encode
	 * @param s
	 * @return
	 */
	public static String HTMLEncode(String s) {
		if (s == null) return null;
		return XMLEncode(s.toCharArray(), false, false);	
	}

	/**
	 * XML encode string, replacing reserved chars to 
	 * appropriate entity names ( "&" -> "&amp;" ).
	 * @param s
	 * @return
	 */
	public static String XMLEncode(String s) {
		if (s == null) return null;
		return XMLEncode(s.toCharArray(), false);	
	}
	
	/**
	 * XMLEscape char array.
	 * @param s
	 * @param useApos true : convert ' char to "&apos;" name
	 * 				  false: convert ' char to "&#39;" name
	 * 				  "&apos;" is recognized by XML specs, but
	 *                not HTML specs.
	 * @return
	 */
	public static String XMLEncode(char[] s, boolean useApos) {
		return XMLEncode(s, useApos, false);
	}
	
	public static String XMLEncode(char[] s, boolean useApos, boolean keepNewlines) {
		int len = (s != null ? s.length : 0);
		
		StringBuilder str = new StringBuilder(len+10);
		
		for (int i = 0; i < len; i++) {
			char ch = s[i];
			switch (ch) {
			case '<': {    str.append("&lt;");     break; }
			case '>': {    str.append("&gt;");     break; }
			case '&': {    str.append("&amp;");    break; }
			case '"': {    str.append("&quot;");   break; }
			case '\'': {   
				if (useApos) str.append("&apos;");
				else str.append("&#39;");
				break;  }
//            case '€': {    str.append("&#8364;"); break; }
			case '\r':
			case '\n':
			case '\t':
			case '\f': {
				if (keepNewlines) {
					str.append(ch);
				} else {
					str.append("&#");
					str.append(Integer.toString(ch));
					str.append(';');
				}
				break; }
			default: {
				str.append(ch);
				}
			}
		}
		return str.toString();
	}
	
	/**
	 * XMLDecode entity names to real string values.
	 * @param s
	 * @return
	 */
	public static String XMLDecode(String s) {
		// return str if no escaped chars found
		int i = s.indexOf('&');
		if (i < 0)
			return s;

		StringBuilder str = new StringBuilder();
		if (i > 0)
			str.append(s.substring(0, i)); // leftside str
		
		int len = (s != null ? s.length() : 0);
		while(i < len) {
			char ch = s.charAt(i);
			if (ch == '&') {
				String entity = null;
				int endIdx;
				for(endIdx=i+1; endIdx < len; endIdx++) {
					if (s.charAt(endIdx) == ';') {
						entity = s.substring(i, endIdx+1);
						break;
					}
				}
			
				if (entity != null) {
					entity = entity.toLowerCase();
					if      (entity.equals("&lt;")) 	str.append('<');
					else if (entity.equals("&gt;")) 	str.append('>');
					else if (entity.equals("&amp;")) 	str.append('&');
					else if (entity.equals("&quot;")) 	str.append('"');
					else if (entity.equals("&apos;")) 	str.append('\'');
					else if (entity.charAt(1) == '#' && entity.charAt(2) == 'x') {
						// entity=&#x2019; unicode apostrophe ’
						// entity=&#x201c; unicode hyphen “
						int val = Integer.parseInt( entity.substring(3, entity.length()-1), 16 );
						str.append((char)val);
					} else if (entity.charAt(1) == '#' && Character.isDigit(entity.charAt(2))) {
						// entity=&#1234;
						int val = Integer.parseInt( entity.substring(2, entity.length()-1) );
						str.append((char)val);
					} else {
						str.append(entity); // unknown entity, write as-is
					}
				} else {
					str.append(s.substring(i, endIdx)); // invalid entity, write as-is
				}
				i=endIdx+1; // go to next char after entity
			} else {
				str.append(ch);
				i++; // go to next char
			}			
		}
		return str.toString();
	}	
	
}
