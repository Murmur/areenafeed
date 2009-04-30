/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.io;

import java.io.CharArrayWriter;

/**
 * Publish direct access to an internal buffer.
 * Some use-cases may require direct access to minimize 
 * unnecessary data copying. 
 */
public class ExCharArrayWriter extends CharArrayWriter {

	public ExCharArrayWriter(int initialSize) {
		super(initialSize);
	}
	
	/**
	 * Return direct access to an internal buffer. Array
	 * is longer than the actual characters written to it.
	 * Use count() method to return the current size of the buffer.
	 * @return
	 */
	public char[] getInternalBuffer() {
		return super.buf;
	}
	
}
