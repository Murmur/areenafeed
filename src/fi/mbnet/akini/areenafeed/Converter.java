package fi.mbnet.akini.areenafeed;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Use Java Reflection to instantiate an appropriate converter at runtime.
 */
public class Converter {
	private Object obj;
	
	/**
	 * Create converter
	 * @param classname		classname of the converter implementation
	 */
	public Converter(String classname) {
		try {
			this.obj = Class.forName(classname).newInstance();
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}
	
	public List<MediaFile> createItems(Map<String,String> conversionParams) throws IOException {
		try {
			Method method = obj.getClass().getMethod("createItems", new Class[] { Map.class });
			Object retobj = method.invoke(obj, new Object[] { conversionParams });
			@SuppressWarnings("unchecked")
			List<MediaFile> items = (List<MediaFile>)retobj;
			return items;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		}
	}
	
}
