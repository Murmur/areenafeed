<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.CT24CZConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call CT24 Czech tv
// /areenafeed/ct24cz.jsp?medialink=url&title=Programmes&maxitems=3&url=vysilani/?streamtype=WM2
// 
//     title    : rss feed title
//     desc     : rss feed description
//     url      : webstream page
//     maxitems : max number of items to read 1...n or empty to read all
//     medialink: <url,asxref,asxrefmms> type of mediafile url, default url
//		url=original link to media.asx url
//		asxref=wmv link from the asx page
//		asxrefmms=wmv link from the asx page, force mms:// link
//		VLC streaming seems only to work with mms media link
request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("medialink", request.getParameter("medialink"));
params.put("maxitems", request.getParameter("maxitems"));

AppContext.setInstance(new ServletAppContext(application));

List<MediaFile> items = new CT24CZConverter().createItems(params);

String feedURL = request.getRequestURL().toString();
if (request.getQueryString() != null)
	feedURL += "?" + request.getQueryString();

String title = request.getParameter("title");
if (title == null) title = "";

String desc  = request.getParameter("desc");
if (desc == null) desc = "";

request.setAttribute("items", items);
request.setAttribute("desc", desc);
request.setAttribute("feedUrl", feedURL);

request.setAttribute("title", "CT24 :: " + title); // "\u010CT24"
request.setAttribute("feedImageUrl", "http://img2.ct24.cz/gfx/logo_ct24_2.gif");
request.setAttribute("defaultThumbnailUrl", "http://img2.ct24.cz/gfx/logo_ct24_2.gif");

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />