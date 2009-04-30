/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.areenafeed;

/**
 * Library version details 
 */
public class Version {
	private String title;
	private String desc;
	private String version;
	private String versionDate;
	private String vendor;
	private String url;
	
	/**
	 * Create version record.
	 */
	public Version() {
		Package pkg = this.getClass().getPackage();
		
		title  = pkg.getImplementationTitle().trim();
		desc   = "RSS converter";//AreenaConverter.DESCRIPTION;
		vendor = pkg.getImplementationVendor().trim();
		url    = "http://koti.mbnet.fi/akini/"; //AreenaConverter.VENDOR_URL;
		
		// x.y.z (yyyy-MM-dd hh:mm:ss)
		String stemp = pkg.getImplementationVersion().trim();
		int idx = stemp.indexOf('(');
		if (idx > 0) {
			version = stemp.substring(0, idx).trim();
			int idxEnd = stemp.indexOf(')', idx+1);
			versionDate = (idxEnd > 0 ? stemp.substring(idx+1, idxEnd) : "");
		} else {
			version = stemp;
			versionDate = "";
		}
	}
	
	public String getTitle() { return title; }
	public String getDescription() { return desc; }
	public String getVersion() { return version; }
	public String getVersionDate() { return versionDate; }
	public String getVendor() { return vendor; }
	public String getVendorUrl() { return url; }
}
