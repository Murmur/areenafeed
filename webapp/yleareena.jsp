<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.Areena2Converter,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// Call new YLE AREENA and convert results to rss feed
// /areenafeed/yleareena.jsp?url=http://areena.yle.fi/ohjelma/81151ac914654bb332319ed30bf7522e/feed/rss
// /areenafeed/yleareena.jsp?url=ohjelma/81151ac914654bb332319ed30bf7522e/feed/rss&title=Muumilaakson+tarinoita
//
request.setCharacterEncoding("UTF-8");

Map<String,String> params = new HashMap<String,String>();
params.put("url", request.getParameter("url"));

AppContext.setInstance(new ServletAppContext(application));

Areena2Converter converter = new Areena2Converter();
List<MediaFile> items = converter.createItems(params);

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

request.setAttribute("title", "YLE Areena :: " + title);
request.setAttribute("feedImageUrl", "http://areena.yle.fi/images/logo_areena.png");

%><jsp:directive.include file="WEB-INF/rss-reply.jsp" />