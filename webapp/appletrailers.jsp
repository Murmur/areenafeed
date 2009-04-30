<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.AppleTrailersConverter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Apple trailers
// /areenafeed/appletrailers.jsp?title=Science+Fiction&url=trailers/genres/science_fiction/
// 
//     title    : rss feed title
//     desc     : rss feed description
//     url      : webstream page
//     quality  : small,medium,large, 480p,720p,1080p (default medium)

request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));
params.put("quality", request.getParameter("quality"));

AppContext.setInstance(new ServletAppContext(application));

List<MediaFile> items = new AppleTrailersConverter().createItems(params);

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

request.setAttribute("title", "Apple Trailers :: " + title);
request.setAttribute("feedImageUrl", "http://itunes.apple.com/images/rss/badge.gif");
//request.setAttribute("defaultThumbnailUrl", "");

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />