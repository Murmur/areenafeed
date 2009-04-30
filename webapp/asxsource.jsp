<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="application/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.ASXSourceConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Parse Advanced Stream Redirector (.asx) refs to rss feed
// /areenafeed/asxsource.jsp?title=ZDF&url=http://wstreaming.zdf.de/zdf/56/090217_anstalt_nad.asx

//     url      : any .asx link (mandatory)
//     title    : rss title (optional)
//     desc     : rss description (optional)
//     medialink: <asxref,asxrefmms> type of mediafile url, default asxref
//		asxref=link from the asx page
//		asxrefmms=wmv link from the asx page, force mms:// link
//		VLC streaming seems only to work with mms media link

request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("medialink", request.getParameter("medialink"));

String title = request.getParameter("title");
if (title == null) {
	title = params.get("keyword");
	if (title == null) title = "";
}
String desc  = request.getParameter("desc");
if (desc == null) desc = "";

AppContext.setInstance(new ServletAppContext(application));

List<MediaFile> items = new ASXSourceConverter().createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

request.setAttribute("title", title);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);
request.setAttribute("items", items);
//request.setAttribute("feedImageUrl", "");

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />