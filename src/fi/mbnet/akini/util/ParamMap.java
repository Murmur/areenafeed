/**
 * Copyright: Tietovalo (http://)
 * @author Aki Nieminen
 * @version $Id$
 */
package fi.mbnet.akini.util;

import java.util.*;

/**
 * 
 */
public class ParamMap<K,V> extends HashMap<K,V> {
	
	public String getProperty(String key) {
		return getProperty(key, null, false);
	}
	
	public String getProperty(String key, String def, boolean useDefIfEmpty) {
		String value = (String)get(key);
		if (value == null || value.equals("")) return def;
		return value;		
	}
	
}
