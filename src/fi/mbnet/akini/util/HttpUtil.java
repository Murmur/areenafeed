/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Common HttpUtil functions
 */
public class HttpUtil {

	/**
	 * Get charset attribute from contenType header value or
	 * default value if not found.
 	 * "text/plain; charset=UTF-8" -> "UTF-8"
	 * @return  charset atttribute
	 */
	public static String getCharset(String contentType, String defaultCharset) {
		if (contentType == null)
			return defaultCharset;
	    int iStart = contentType.toLowerCase().indexOf("charset=");
	    if (iStart >= 0) {
	    	iStart += 8; // + "charset=" 
	    	int iEnd = contentType.indexOf(';', iStart);
	    	String cs = (iEnd > 0 ?
	    			contentType.substring(iStart, iEnd) :
	    			contentType.substring(iStart) );
	    	return new String(cs);
	    }
		return defaultCharset;
	}
	
	/**
	 * Parse querystring from the given map.
	 * example: key1=value1&key2=this+is+value2
	 * @param charset
	 * @param params
	 * @return	querystring or empty string
	 */
	public static String parseURLParameters(String charset, Map<String,String> params) {
		if (params == null || params.size() < 1)
			return "";

		StringBuilder sb = new StringBuilder();
    	
		try {
			Iterator<Map.Entry<String,String>> iter = params.entrySet().iterator();
	    	while(iter.hasNext()) {
	    		Map.Entry<String,String> e = iter.next();
				sb.append(e.getKey());
				sb.append("=");
				String val = e.getValue();
				if (val != null)
					sb.append( URLEncoder.encode(e.getValue(), charset) );
				sb.append("&");
	    	}
		} catch (UnsupportedEncodingException ex) {
			return ""; // should not never happen
		}
		return sb.substring(0, sb.length()-1);				
	}
	
}
