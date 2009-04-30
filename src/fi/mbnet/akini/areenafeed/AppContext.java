package fi.mbnet.akini.areenafeed;

/**
 * Static container of the AppContextI instance.
 * @see: youtube.jsp and other .jsp pages
 */
public class AppContext {
	private static AppContextI instance;
	
	public static synchronized void setInstance(AppContextI ctx) {
		instance = ctx;
	}
	
	public static synchronized AppContextI getInstance() {
		if (instance == null) {
			// create mockup instance
			instance = new AppContextI() {
				public void log(String message) { }
				public void log(String message, Throwable ex) { }
			};
		}
		return instance; 
	}

	// interface of the AppContext implementation
	public static interface AppContextI {
		public void log(String message);
		public void log(String message, Throwable ex);
	}
	
}
