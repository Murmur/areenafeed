/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import java.util.*;
import java.io.*;
import fi.mbnet.akini.util.*;


/**
 * AreenaConverter commandline.
 * Create rss-simple.xml document.
 */
public class AreenaConverterMain {
	private Map<String,String> params;

	public void setParams(Map<String,String> params) {
		this.params = params;
	}
	
	private void createSimpleDocument(Writer owriter, List<MediaFile> items) throws IOException {
		final String NL = System.getProperty("line.separator", "\r\n");
		
		String quality = params.get("quality"); // lo,hi
		String qualityText = (quality == null || quality.equalsIgnoreCase("lo") ? "low" : "high");
		
		// write header
		owriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL);
		owriter.write("<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\">" + NL);
		owriter.write("<channel>" + NL);
		owriter.write(" <title>YLE Areena :: " + ConvertUtil.XMLEncode(params.get("keyword")) + "</title>" + NL);
		owriter.write(" <link>http://</link>" + NL);
		owriter.write(" <description>" + ConvertUtil.XMLEncode(params.get("keyword")) + " with a " + qualityText + " quality.</description>" + NL);
		owriter.write("" + NL);
		
		// loop items
		for(MediaFile item : items) {
			owriter.write("  <item>" + NL);
			owriter.write("    <title>" + ConvertUtil.XMLEncode(item.getTitle()) + " " + ConvertUtil.XMLEncode(item.getSubtitle()) + "</title>" + NL);
			owriter.write("    <link>" + ConvertUtil.XMLEncode(item.getMediaFileUrl()) + "</link>" + NL);
			owriter.write("    <description>" + ConvertUtil.XMLEncode(item.getDescription()) + "</description>" + NL);
			owriter.write("    <media:thumbnail url=\"" + ConvertUtil.XMLEncode(item.getThumbnailUrl()) + "\" />" + NL);
			owriter.write("  </item>" + NL);
		}
		
		// write footer
		owriter.write("" + NL);		
		owriter.write("</channel>" + NL);
		owriter.write("</rss>" + NL);
	}
	
//******************************************	
//******************************************
	
	/**
	 * Run program
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Map<String,String> params = parseArguments(args);

		// fix DOS charset problem, use utf-8 url encoded commandline arg.
		// NOTE: windows dos prompt must use double-% escaping.
		//   T%%C3%%A4n%%C3%%A4%%C3%%A4n+otsikoissa -> T‰n‰‰n otsikoissa
		String keyword = params.remove("keywordenc");
		if (keyword != null && !keyword.equals(""))
			params.put("keyword", ConvertUtil.URLDecode(keyword));
		
		AreenaConverter converter = new AreenaConverter();
		List<MediaFile> items = converter.createItems(params);

		OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8");

		try {
			AreenaConverterMain app = new AreenaConverterMain();
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
	private static Map<String,String> parseArguments(String[] args) {
		Map<String,String> params = new HashMap<String,String>();
		
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
