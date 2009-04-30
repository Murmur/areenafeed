<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="application/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.VimeoConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call vimeo.com and convert to rss
// /areenafeed/vimeo.jsp?title=Documentary+films&quality=lo&url=channels/documentaryfilm/videos/rss

//     url      : any html link
//     keyword  : search keyword
//     title    : rss title (optional)
//     desc     : rss description
//     quality  : <lo,hi> video quality, default lo

request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("keyword", request.getParameter("keyword"));
params.put("quality", request.getParameter("quality"));

String title = request.getParameter("title");
if (title == null) {
	title = params.get("keyword");
	if (title == null) title = "";
}
String desc  = request.getParameter("desc");
if (desc == null) desc = "";

AppContext.setInstance(new ServletAppContext(application));

VimeoConverter converter = new VimeoConverter();
List<MediaFile> items = converter.createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

request.setAttribute("title", "Vimeo :: " + title);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);
request.setAttribute("feedImageUrl", "http://bitcast.vimeo.com/vimeo/assets/images/logo.gif");
request.setAttribute("items", items);

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />