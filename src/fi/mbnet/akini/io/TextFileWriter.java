package fi.mbnet.akini.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import fi.mbnet.akini.util.StringUtil;

public class TextFileWriter {

	private BufferedWriter writer;
	private FileOutputStream fos;
	
	public TextFileWriter(String filename, boolean append, boolean utf8) throws IOException {
		File f = new File(filename);
		fos = new FileOutputStream(f, append);

		// write UTF8 BOM mark if file is empty
		if (utf8 && (!append || f.length() < 1)) {
			byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
			fos.write(bom);
		}

		writer = utf8 ? new BufferedWriter( new OutputStreamWriter(fos, "UTF-8") ) :
			new BufferedWriter( new OutputStreamWriter(fos) );
	}
	
	public void println(String data) throws IOException {
		write(data);
		write(StringUtil.lineSeparator);
	}
	
	public void print(String data) throws IOException {
		writer.write(data);
	}
	
	public void write(String data) throws IOException {
		writer.write(data);
	}

	public void close() {
		try { writer.flush(); } catch(Exception ex) { }
		try { writer.close(); fos.close(); } catch (Exception ex) { }
	}

}
