/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.io;

import java.net.*;
import java.util.*;
import java.io.*;

import fi.mbnet.akini.util.HttpUtil;

/**
 * Implements stream access to http request-response
 * body data. Some of the response header values are stored
 * for later use.
 */
public class HttpClientReader extends Reader {
	private HttpURLConnection httpUrl;
	private InputStreamReader isreader;

	// if reply does not have charset header then use this value
	private String defaultResCharset = "UTF-8";
	
	// keep properties from http response
	private int resStatusCode;
	private String resStatusMessage;
	private String resContentType;
	private int resContentLength;
	
    private HttpURLConnection createConnection(String urlName) 
				throws IOException {
    	HttpURLConnection httpUrl;
    	URL url = new URL(urlName);
    	httpUrl = (HttpURLConnection)url.openConnection();
    	return httpUrl;
    }

    /**
     * Default response charset if reply does not 
     * contain a charset header. Default is UTF-8.
     * @param charset
     */
    public void setDefaultResponseCharset(String charset) {
    	defaultResCharset = charset;
    }
    
    public String getResponseContentType() {
    	return resContentType;
    }
    
    public int getResponseContentLength() {
    	return resContentLength;
    }
    
    public int getResponseStatusCode() {
    	return resStatusCode;
    }

    public String getResponseStatusMessage() {
    	return resStatusMessage;
    }

	/**
	 * Use POST method and prepare stream for reading.
	 * @param reqContentType Content-Type value, default
	 * 		is "application/x-www-form-urlencoded; charset=UTF-8"
	 * @param charset	charset, default is "UTF-8"
	 * @param url		url address
	 * @param params	url params, name=value pairs or null
	 * @param headers	optional request headers or null
	 * @return	response statuscode, 2xx=OK 
	 * @throws IOException
	 */
	public int post(String reqContentType, String charset, 
				String url, Map<String,String> params, Map<String,String> headers) throws IOException {
		// clear previous data
		resetResponseProperties();
       	close();
		
		try {
			if (reqContentType == null) {
				if (charset == null) charset = "UTF-8";
				reqContentType = "application/x-www-form-urlencoded; charset=" + charset;
			} else {
				if (charset == null) charset = HttpUtil.getCharset(reqContentType, "UTF-8");
			}

			httpUrl = createConnection(url);
			httpUrl.setRequestMethod("POST");
			httpUrl.setUseCaches(false);
			httpUrl.setDoOutput(true);
	
			// write optional request header
	        if (headers != null && headers.size() > 0) {
	        	Iterator<Map.Entry<String,String>> iter = headers.entrySet().iterator();
	        	while(iter.hasNext()) {
	        		Map.Entry<String,String> e = iter.next();
	        		httpUrl.setRequestProperty(e.getKey(), e.getValue() );
	        	}
	        }
			httpUrl.setRequestProperty("Content-Type", reqContentType);
	
			// parse body part, name-value pairs
			byte[] body = HttpUtil.parseURLParameters(charset, params).getBytes("ISO-8859-1");
			
			// write body bytes to outputstream
			if (body != null && body.length > 0) {
				httpUrl.setRequestProperty("Content-Length", String.valueOf(body.length));
				httpUrl.getOutputStream().write(body);
			} else {
				httpUrl.setRequestProperty("Content-Length", "0");
			}
			httpUrl.getOutputStream().flush();
	
			// prepare inputstream for reading, response body
			// is read using one of the read() methods.
			InputStream is = httpUrl.getInputStream();
			
			resStatusCode = httpUrl.getResponseCode();
			resStatusMessage = httpUrl.getResponseMessage();
	        resContentType = httpUrl.getContentType();
	        resContentLength = httpUrl.getContentLength();
	        String resCharset = HttpUtil.getCharset(resContentType, defaultResCharset);
	
	        isreader = new InputStreamReader(is, resCharset);
	        
			return resStatusCode;
		} catch(Exception ex) {
			close();
			if (ex instanceof IOException) throw (IOException)ex;
			throw new IOException(ex);
		}
	}

	/**
	 * Use GET method and prepare stream for reading.
	 * @param reqContentType Content-Type value, default is "text/plain; charset=UTF-8"
	 * @param charset	charset, default is "UTF-8"
	 * @param url		url address
	 * @param params	url params, name=value pairs or null
	 * @param headers	optional request headers or null
	 * @return	response statuscode, 2xx=OK 
	 * @throws IOException
	 */
	public int get(String reqContentType, String charset, 
				String url, Map<String,String> params, Map<String,String> headers) throws IOException {
		// clear previous data
		resetResponseProperties();
       	close();
		
		try {
			if (reqContentType == null) {
				if (charset == null) charset = "UTF-8";
				reqContentType = "text/plain; charset=" + charset;
			} else {
				if (charset == null) charset = HttpUtil.getCharset(reqContentType, "UTF-8");
			}

			// parse url params and concatenate to URL
			String urlParams = HttpUtil.parseURLParameters(charset, params);

			if (urlParams != null && urlParams.length() > 0) {
				if (url.indexOf('?') < 1) {
					url = url + "?" + urlParams;
				} else {
					char lastChar = url.charAt(url.length()-1);
					if (lastChar == '?') url = url + urlParams;
					else if (lastChar == '&') url = url + urlParams;
					else url = url + "&" + urlParams;
				}
			}
			
			httpUrl = createConnection(url);
			httpUrl.setRequestMethod("GET");
			httpUrl.setUseCaches(false);

			// write optional request header
			// TODO: should we urlencode keys+values?
	        if (headers != null && headers.size() > 0) {
	        	Iterator<Map.Entry<String,String>> iter = headers.entrySet().iterator();
	        	while(iter.hasNext()) {
	        		Map.Entry<String,String> e = iter.next();
	        		httpUrl.setRequestProperty(e.getKey(), e.getValue() );
	        	}
	        }
			httpUrl.setRequestProperty("Content-Type", reqContentType);
			
			// prepare inputstream for reading, response body
			// is read using one of the read() methods.
			InputStream is = httpUrl.getInputStream();
			
			resStatusCode = httpUrl.getResponseCode();
			resStatusMessage = httpUrl.getResponseMessage();
	        resContentType = httpUrl.getContentType();
	        resContentLength = httpUrl.getContentLength();
	        String resCharset = HttpUtil.getCharset(resContentType, defaultResCharset);
	
	        isreader = new InputStreamReader(is, resCharset);
	        
			return resStatusCode;
		} catch(Exception ex) {
			close();
			if (ex instanceof IOException) throw (IOException)ex;
			throw new IOException(ex);
		}
	}	
	
	/**
	 * Close internal socket streams. Response properties are still
	 * accessible after this call.
	 */
	@Override
	public void close() throws IOException {
		if (isreader != null) {
			try { isreader.close(); } catch (Exception ex) { }
	    	isreader = null;
		}
		if (httpUrl != null) {
			try { httpUrl.disconnect(); } catch (Exception ex) { }
			httpUrl = null;
		}
	}

	private void resetResponseProperties() {
		isreader = null;
		resStatusCode = 0;
		resContentType = null;
		resContentLength = 0;		
	}
	
	@Override
	public void finalize() throws Throwable {
		close(); // make sure resources are closed
	}

	@Override
	public int read(char[] cbuf, int offset, int length) throws IOException {
		return isreader.read(cbuf, offset, length);
	}
	
	/**
	 * Read response body.
	 * @return	characters or empty array
	 * @throws IOException
	 */
	public char[] readAll() throws IOException {
		CharArrayWriter caw = new CharArrayWriter(32 * 1024);

		char[] cbuf = new char[16*1024];
		int read;
        while( (read = isreader.read(cbuf)) != -1)
        	caw.write(cbuf, 0, read);

        // close socket resources on EOF, this ensures we don't
        // keep it open too long if user/.jsp page does not
        // explicitly close this stream.
        if (read < 0) close();
        
        return caw.toCharArray();
	}

}
