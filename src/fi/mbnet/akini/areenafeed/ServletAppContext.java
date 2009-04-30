/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

import javax.servlet.ServletContext;

/**
 * Static Application context to provide logging interface.
 */
public class ServletAppContext implements AppContext.AppContextI {
	private ServletContext ctx;

	public ServletAppContext(ServletContext ctx) {
		this.ctx = ctx;
	}
	
	// implement interface
	public void log(String message) { ctx.log(message); }
	public void log(String message, Throwable ex) { ctx.log(message, ex); }
		
}
