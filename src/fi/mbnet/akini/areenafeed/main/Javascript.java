package fi.mbnet.akini.areenafeed.main;

import java.io.*;
import javax.script.*;

import fi.mbnet.akini.util.*;
import fi.mbnet.akini.io.UnicodeReader;

/**
 * Run javascript script.
 */
public class Javascript {

	public static void main(String[] args) throws Exception {
		String script = args[0];
		args[0] = ""; 
		ParamMap<String,String> params = CreateRSS.parseArguments(args);
 
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

    	engine.put("params", params);
        engine.put("ConvertUtil", new ConvertUtil());
        engine.put("DateTimeUtil", new DateTimeUtil());
        engine.put("StringUtil", new StringUtil());
        engine.put("XMLUtil", new XMLUtil());
        
        Reader reader = new UnicodeReader(new FileInputStream(script), null);
    	try {
            engine.eval(reader);
		} finally {
	        reader.close();
		}
	}

}
