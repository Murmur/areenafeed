<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ 
    page pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.io.*,
		fi.mbnet.akini.areenafeed.AppContext,
		fi.mbnet.akini.areenafeed.ServletAppContext,
		fi.mbnet.akini.areenafeed.MediaFile,
		fi.mbnet.akini.util.*
	"
%><%
// this is called by main .jsp pages,
// read variables from request attributes.

String defaultThumbnailUrl = (String)request.getAttribute("defaultThumbnailUrl");
if (defaultThumbnailUrl == null) defaultThumbnailUrl = "";

%><?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/">

<channel>
	<title><%= ConvertUtil.XMLEncode( (String)request.getAttribute("title") ) %></title>
	<link><%= ConvertUtil.XMLEncode( (String)request.getAttribute("feedUrl") ) %></link>
	<c:if test="<%= request.getAttribute(\"feedImageUrl\") != null %>" >
	  <image>
		<url><%= (String)request.getAttribute("feedImageUrl") %></url>
	  </image>
	</c:if>
	<description><%= ConvertUtil.XMLEncode( (String)request.getAttribute("desc") ) %></description>

<c:forEach var="item" items="${items}">
<jsp:useBean id="item" type="fi.mbnet.akini.areenafeed.MediaFile" />
	<item>
		<title><%= ConvertUtil.XMLEncode(item.getTitleAndPublished()) %></title>
		<link><%= ConvertUtil.XMLEncode(item.getMediaPageUrl()) %></link>
		<pubDate><%= (item.getPublished() != null ? DateTimeUtil.formatRSSDateTime(item.getPublished()) : "") %></pubDate>
		<description>
<![CDATA[
	<img src="<%= ConvertUtil.XMLEncode(item.getThumbnailUrl()) %>" align="right" border="0" vspace="0" hspace="4" />
	<p><%= ConvertUtil.XMLEncode(item.getDescription()) %></p>
]]>
		</description>
		<media:thumbnail url="<%= ConvertUtil.XMLEncode( StringUtil.isEmpty(item.getThumbnailUrl()) ? defaultThumbnailUrl : item.getThumbnailUrl() ) %>" />
		<media:title><%= ConvertUtil.XMLEncode(item.getTitle()) %></media:title>
		<enclosure url="<%= ConvertUtil.XMLEncode(item.getMediaFileUrl()) %>" />
	</item>
</c:forEach>

</channel>
</rss>