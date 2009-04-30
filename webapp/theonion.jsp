<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="application/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.TheOnionConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call theonion.com and convert to rss
// /areenafeed/theonion.jsp?title=Most+Recent&url=content/ajax/onn/list/8/0/mostrecent
// /areenafeed/theonion.jsp?title=Most+Popular&url=content/ajax/onn/list/8/0/mostpopular

//     url      : any html link that have video subpage links
//     keyword  : search keyword (NOT IMPLEMENTED)
//     title    : rss title (optional)
//     desc     : rss description

request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("keyword", request.getParameter("keyword"));

String title = request.getParameter("title");
if (title == null) {
	title = params.get("keyword");
	if (title == null) title = "";
}
String desc  = request.getParameter("desc");
if (desc == null) desc = "";

AppContext.setInstance(new ServletAppContext(application));

List<MediaFile> items = new TheOnionConverter().createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

request.setAttribute("title", "The Onion :: " + title);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);
request.setAttribute("feedImageUrl", "http://www.theonion.com/content/themes/onion/assets/logos/onion_tiny.png");
request.setAttribute("items", items);

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />