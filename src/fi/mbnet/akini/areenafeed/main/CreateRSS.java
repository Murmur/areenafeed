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
import fi.mbnet.akini.util.ConvertUtil;
import fi.mbnet.akini.util.DateTimeUtil;
import fi.mbnet.akini.util.StringUtil;
import fi.mbnet.akini.util.ParamMap;

/**
 * Run commandline parser
 * java.exe -cp ./webapp/WEB-INF/lib/areenafeed.jar fi.mbnet.akini.areenafeed.main.CreateRSS 
 * 		"class=fi.mbnet.akini.areenafeed.Areena2Converter"
 * 		"output=rss-simple1.xml" 
 * 		"url=ohjelma/81151ac914654bb332319ed30bf7522e/feed/rss" 
 * 		"title=Muumilaakson tarinoita"   
 */
public class CreateRSS {
	private ParamMap<String,String> params;

	public void setParams(ParamMap<String,String> params) {
		this.params = params;
	}
	
	private void createSimpleDocument(Writer owriter, List<MediaFile> items) throws IOException {
		final String NL = System.getProperty("line.separator", "\r\n");
		
		// write header
		owriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL);
		owriter.write("<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\">" + NL);
		owriter.write("<channel>" + NL);
		owriter.write(" <title>" + ConvertUtil.XMLEncode(params.getProperty("title", "", true)) + "</title>" + NL);
		owriter.write(" <link>http://</link>" + NL);
		owriter.write(" <description>" + ConvertUtil.XMLEncode(params.getProperty("description", "", true)) + "</description>" + NL);
		owriter.write("" + NL);
		
		// loop items
		for(MediaFile item : items) {
			owriter.write("  <item>" + NL);
			owriter.write("    <title>" + ConvertUtil.XMLEncode(item.getTitleAndPublished()) + "</title>" + NL);
			owriter.write("    <link>" + ConvertUtil.XMLEncode(item.getMediaPageUrl()) + "</link>" + NL);
			owriter.write("    <pubDate>" + (item.getPublished() != null ? DateTimeUtil.formatRSSDateTime(item.getPublished()) : "") + "</pubDate>" + NL);
			owriter.write("    <description>" + ConvertUtil.XMLEncode(item.getDescription()) + "</description>" + NL);
			owriter.write("    <media:thumbnail url=\"" + ConvertUtil.XMLEncode(item.getThumbnailUrl()) + "\" />" + NL);
			owriter.write("    <media:title>" + ConvertUtil.XMLEncode(item.getTitle()) + "</media:title>" + NL);
			owriter.write("    <enclosure url=\"" + ConvertUtil.XMLEncode(item.getMediaFileUrl()) + "\" />" + NL);
			owriter.write("  </item>" + NL);
		}
		
		// write footer
		owriter.write("" + NL);		
		owriter.write("</channel>" + NL);
		owriter.write("</rss>" + NL);
	}
	
	/**
	 */
	public static void main(String[] args) throws Exception {
		ParamMap<String,String> params = parseArguments(args);

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
			CreateRSS app = new CreateRSS();
			app.setParams(params);
			app.createSimpleDocument(osw, items);
		} finally {
			try { osw.flush(); } catch (Exception e) { }
			try { osw.close(); } catch (Exception e) { }
		}
	}

	/**
	 * Parse key-value pairs to hashmap.
	 * @param args
	 * @return
	 */
	public static ParamMap<String,String> parseArguments(String[] args) {
		ParamMap<String,String> params = new ParamMap<String,String>();
		
		if (args == null || args.length < 1) return params;
		
		// split key-value pairs
		for(String val : args) {
			int idx = val.indexOf('='); // "mykey=myvalue"
			if (idx < 0) {
				params.put(val, null);
			} else {
				params.put(val.substring(0, idx), val.substring(idx+1));
			}
		}	
		
		return params;
	}

}
