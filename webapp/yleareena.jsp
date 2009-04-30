<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.AreenaConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call YLE AREENA and convert results to rss feed
// /areenafeed/yleareena.jsp?quality=lo&medialink=url&keyword=Ajankohtainen+Kakkonen
// /areenafeed/yleareena.jsp?quality=lo&medialink=url&cid=164618&pid=215452
// /areenafeed/yleareena.jsp?quality=lo&medialink=url&media=video&filter=4,1&cid=164553&title=Lastenohjelmat
// 
//     keyword  : search keyword, use either keyword or cid
//     cid      : category id (optional)
//     pid      : program id (optional)
//
//     quality  : <lo,hi> video quality, default lo
//     medialink: <url,asxref,asxrefmms> type of mediafile url, default url
//		url=original link to media.asx url
//		asxref=wmv link from the asx page
//		asxrefmms=wmv link from the asx page, force mms:// link
//		VLC streaming seems only to work with mms media link
//     media    : <video,audio,all> select by media type, default all
//     filter   : optional query filter passed to YLE Areena query url
//              4,1=videos only, finnish
//              4,2=videos only, swedish
request.setCharacterEncoding("UTF-8");

String qualityText;
String quality = request.getParameter("quality"); // lo,hi
if (quality == null || quality.equalsIgnoreCase("lo")) {
	quality = "lo";
	qualityText = "low";
} else {
	quality = "hi";
	qualityText = "high";
}

Map<String,String> params = new HashMap<String,String>();
params.put("keyword", request.getParameter("keyword"));
params.put("cid", request.getParameter("cid"));
params.put("pid", request.getParameter("pid"));
params.put("quality", quality);
params.put("medialink", request.getParameter("medialink"));
params.put("media", request.getParameter("media"));
params.put("filter", request.getParameter("filter"));

AppContext.setInstance(new ServletAppContext(application));

AreenaConverter converter = new AreenaConverter();
List<MediaFile> items = converter.createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

String title = request.getParameter("title");
if (title == null) {
	title = params.get("keyword");
	if (title == null) title = "";
}
String desc  = request.getParameter("desc");
if (desc == null) desc = "";

request.setAttribute("items", items);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);

request.setAttribute("title", "YLE Areena :: " + title);
request.setAttribute("feedImageUrl", "http://areena.yle.fi/themes/YLE/img/ntvr/logo.gif");

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />