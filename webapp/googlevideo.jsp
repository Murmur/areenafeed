<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.GoogleVideoConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call Google videohtml and convert to rss
// /areenafeed/googlevideo.jsp?title=Google+videos&desc=Google+videos&url=
// /areenafeed/googlevideo.jsp?title=Google+videos&desc=Google+videos&url=videosearch?hl=en%26emb=1%26aq=f%26q=paramore
// /areenafeed/googlevideo.jsp?title=Google+videos&desc=Google+videos&url=videosearch?hl=en%26emb=1%26aq=f%26q=madonna%2BH%2526M

// /areenafeed/googlevideo.jsp?descMadonna+videos&keyword=Madonna+H%26M
// /areenafeed/googlevideo.jsp?title=Google+videos&desc=Google+videos&keyword=Y%C3%B6
// /areenafeed/googlevideo.jsp?medialink=e26&desc=Jenni+Vartiainen+videos&keyword=Jenni+Vartiainen
//     url      : any video.google.com html link
//     keyword  : search keyword (utf-8)
//     title    : rss title (optional)
//     desc     : rss description
//     medialink: <url, e26> medialink format, default url
//		url=original link to .flv video
//		e26=escape & characters to %26 (youtube.com links only)
//		VLC streaming seems only to work with e26 escaping

request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("keyword", request.getParameter("keyword"));
params.put("medialink", request.getParameter("medialink"));

String title = request.getParameter("title");
if (title == null) {
	title = params.get("keyword");
	if (title == null) title = "";
}
String desc  = request.getParameter("desc");
if (desc == null) desc = "";

AppContext.setInstance(new ServletAppContext(application));

GoogleVideoConverter converter = new GoogleVideoConverter();
List<MediaFile> items = converter.createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

request.setAttribute("title", "Google Video :: " + title);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);
request.setAttribute("feedImageUrl", "http://video.google.com/img/logo_video.gif?hl=en");
request.setAttribute("items", items);

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />