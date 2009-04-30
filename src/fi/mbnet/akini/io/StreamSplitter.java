/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.io;

import java.io.*;
import java.util.*;

/**
 * Split substring from the stream.
 */
public class StreamSplitter {
	private Reader isreader; 
	
	public StreamSplitter(Reader reader) {
		isreader = reader;
	}
	
	/**
	 * Set source stream where data is read from.
	 * @param reader
	 */
	public void setReader(Reader reader) {
		isreader = reader; 
	}

	/**
	 * Split substring from the stream. Delimiters are case-sensitive.
	 * @param fromDelim		start of split delimiter
	 * @param toDelim		end of split delimiter
	 * @param excludeDelimiters	set true, if delimiters are excluded in a returned array
	 * @return	split array or char[0] if match not found
	 * @throws IOException
	 */
	public char[] splitFromTo(String fromDelim, String toDelim, boolean excludeDelimiters)
				throws IOException {
		ExCharArrayWriter buffer = new ExCharArrayWriter(32*1024);

		boolean splitFound=false;
		int delimIdx, delimLen;

		// find start of split, skip leading characters
		if (fromDelim != null && fromDelim.length() > 0) {
			delimLen = fromDelim.length();
			delimIdx = 0;
			while(delimIdx < delimLen) {
				int ic = isreader.read();
				if (ic < 0) break; // EOF
				else if ((char)ic == fromDelim.charAt(delimIdx)) delimIdx++;
				else delimIdx = 0;
			}
				
			// see if we have a full match
			if (delimIdx >= delimLen) {
				splitFound = true;
				if (!excludeDelimiters)
					buffer.write(fromDelim);
			}
		} else {
			splitFound = true;
		}

		// write characters until end of split is found,
		// if end is not found then discard entire buffer.
		if (splitFound && (toDelim == null || toDelim.length() < 0)) {
			// write all remaining characters
			excludeDelimiters = false;
			char[] cbuf = new char[16*1024];
			int read;
	        while( (read = isreader.read(cbuf)) != -1)
	        	buffer.write(cbuf, 0, read);
		} else if (splitFound) {
			splitFound = false; // reset flag
			delimLen = toDelim.length();
			delimIdx = 0;
			while(delimIdx < delimLen) {
				int ic = isreader.read();
				if (ic < 0) break; // EOF
				buffer.append((char)ic);
				if ((char)ic == toDelim.charAt(delimIdx)) delimIdx++;
				else delimIdx = 0;
			}
			
			// see if we have a full match, toDelimiter was
			// already written to a buffer.
			splitFound = (delimIdx >= delimLen);
		}

		if (splitFound) {
			if (!excludeDelimiters) {
				return buffer.toCharArray();
			} else {
				// remove toDelimiter from the buffer
				char[] cbuf = buffer.getInternalBuffer();
				return Arrays.copyOf(cbuf, buffer.size() - toDelim.length());
			}
		} else {
			return new char[0]; // split not found
		}
	}
	
}
