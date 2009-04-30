<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="application/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.YoutubeConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call Youtube html and convert to rss
// /areenafeed/youtube.jsp?title=Little+Big+Planet&desc=LBP+Creator+Spotlight&url=view_play_list?p=5798677ED93EFEF2
// /areenafeed/youtube.jsp?title=nqtv%27s+channel&desc=Remi+Gaillard+videos&url=profile?user=nqtv%26view=videos
// /areenafeed/youtube.jsp?title=Playstation+channel&desc=Official+Playstation+videos&url=profile?user=PlayStation%26view=videos
// /areenafeed/youtube.jsp?desc=Madonna+videos&url=results?aq=f%26search_query=Madonna
// /areenafeed/youtube.jsp?url=browse?s=mphd&title=HD+videos&desc=
// /areenafeed/youtube.jsp?desc=Iron+Maiden+videos&keyword=Iron+Maiden
// /areenafeed/youtube.jsp?desc=Led+Zeppelin+videos&keyword=Led+Zeppelin
// /areenafeed/youtube.jsp?title=The+Onion&url=profile?user=TheOnion%26view=videos

//     url      : any youtube.com html link
//     keyword  : search keyword
//     title    : rss title (optional)
//     desc     : rss description
//     medialink: <url, e26> medialink format, default url
//		url=original link to .flv video
//		e26=escape & characters to %26
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

YoutubeConverter converter = new YoutubeConverter();
List<MediaFile> items = converter.createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

request.setAttribute("title", "Youtube :: " + title);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);
request.setAttribute("feedImageUrl", "http://youtube.com/img/pic_youtubelogo_123x63.gif");
request.setAttribute("items", items);

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />