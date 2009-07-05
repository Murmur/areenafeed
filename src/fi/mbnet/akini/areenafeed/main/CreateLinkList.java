/**
 * Copyright: Tietovalo (http://)
 * @author Aki Nieminen
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed.main;

import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import fi.mbnet.akini.areenafeed.MediaFile;
//import fi.mbnet.akini.util.ConvertUtil;
//import fi.mbnet.akini.util.DateTimeUtil;
import fi.mbnet.akini.util.StringUtil;
import fi.mbnet.akini.util.ParamMap;

/**
 * Run commandline parser
 * java.exe -cp ./webapp/WEB-INF/lib/areenafeed.jar fi.mbnet.akini.areenafeed.main.CreateLinkList 
 * 		"class=fi.mbnet.akini.areenafeed.Areena2Converter"
 * 		"output=rss-simple1.txt" 
 * 		"url=ohjelma/81151ac914654bb332319ed30bf7522e/feed/rss" 
 */
public class CreateLinkList {
	//private ParamMap<String,String> params;

	public void setParams(ParamMap<String,String> params) {
		//this.params = params;
	}
	
	private void createSimpleDocument(Writer owriter, List<MediaFile> items) throws IOException {
		final String NL = System.getProperty("line.separator", "\r\n");
		
		// loop items
		for(MediaFile item : items) {
			owriter.write(item.getMediaFileUrl() + " ");
			owriter.write(item.getTitleAndPublished() + NL);
		}
	}
	
	/**
	 */
	public static void main(String[] args) throws Exception {
		ParamMap<String,String> params = CreateRSS.parseArguments(args);

		// fix DOS charset problem, use utf-8 url encoded commandline arg.
		// NOTE: windows dos prompt must use double-% escaping.
		//   T%%C3%%A4n%%C3%%A4%%C3%%A4n+otsikoissa -> T‰n‰‰n otsikoissa
		//String keyword = params.remove("keywordenc");
		//if (keyword != null && !keyword.equals(""))
		//	params.put("keyword", ConvertUtil.URLDecode(keyword));

		String output = params.remove("output"); // output filename.xml
		
		Object obj = Class.forName( params.remove("class") ).newInstance();
		Method method = obj.getClass().getMethod("createItems", new Class[] { Map.class });
		Object retobj = method.invoke(obj, new Object[] { params });
		@SuppressWarnings("unchecked")
		List<MediaFile> items = (List<MediaFile>)retobj;
		
		OutputStreamWriter osw = null;
		if (StringUtil.isEmpty(output)) {
			osw = new OutputStreamWriter(System.out, "UTF-8");
		} else {
			FileOutputStream fos = null;
			File f = new File(output);
			fos = new FileOutputStream(f, false);
			final byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
			fos.write(bom); // UTF8 bom marker
			osw = new OutputStreamWriter(fos, "UTF-8");
		}
		
		try {
			CreateLinkList app = new CreateLinkList();
			app.setParams(params);
			app.createSimpleDocument(osw, items);
		} finally {
			try { osw.flush(); } catch (Exception e) { }
			try { osw.close(); } catch (Exception e) { }
		}
	}

}
