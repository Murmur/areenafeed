<%@ 
    page contentType="text/xml; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="fi.mbnet.akini.areenafeed.*,
		fi.mbnet.akini.util.*
	"
%><%
// Get library version
// /areenafeed/version.jsp
//request.setCharacterEncoding("UTF-8");

Version version = new Version();

%><?xml version="1.0" encoding="UTF-8"?>
<version>
	<title><%= ConvertUtil.XMLEncode(version.getTitle()) %></title>
	<description><%= ConvertUtil.XMLEncode(version.getDescription()) %></description>
	<version><%= ConvertUtil.XMLEncode(version.getVersion()) %></version>
	<date><%= ConvertUtil.XMLEncode(version.getVersionDate()) %></date>
	<vendor><%= ConvertUtil.XMLEncode(version.getVendor()) %></vendor>
	<url><%= ConvertUtil.XMLEncode(version.getVendorUrl()) %></url>
</version>