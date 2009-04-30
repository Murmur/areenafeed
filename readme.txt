http://koti.mbnet.fi/akini/ps3/areenafeed.html

Convert YLE Areena content to rss feed.
Convert Youtube to rss feed.
Convert GoogleVideo to rss feed.
Convert Vimeo.com to rss feed.
Convert theonion.com to rss feed.
Convert CT24 Czech TV to rss feed

Use cases
============
Publish web videos to Playstation 3 using PS3MediaServer dlna.

Example urls:
http://localhost:8080/areenafeed/yleareena.jsp?quality=hi&medialink=asxrefmms&media=video&keyword=Reinikainen
http://localhost:8080/areenafeed/yleareena.jsp?quality=lo&medialink=asxrefmms&cid=164618&pid=215452
http://localhost:8080/areenafeed/youtube.jsp?title=Playstation+channel&desc=Official+Playstation+videos&url=profile?user=PlayStation%26view=videos
http://localhost:8080/areenafeed/googlevideo.jsp?medialink=e26&desc=Jenni+Vartiainen+videos&keyword=Jenni+Vartiainen
http://localhost:8080/areenafeed/vimeo.jsp?title=Documentary+films&quality=lo&url=channels/documentaryfilm/videos/rss
http://localhost:8080/areenafeed/areenafeed/theonion.jsp?title=Most+Recent&url=content/ajax/onn/list/8/0/mostrecent
http://localhost:8080/areenafeed/areenafeed/ct24cz.jsp?medialink=asxrefmms&title=Programmes&maxitems=5&url=vysilani/?streamtype=WM2

Offline example rss feeds (obsolete, do not use)
==============================
Here is three offline rss documents after a conversion.
	rss_video.xml

You can use any website source and create similar rss documents.
PS3MS should read it and publish content to Playstation 3 XMB menu.


Create rss documents offline (obsolete, do not use)
==============================
Use commandline to create offline documents.

@REM Create rss-simple documents
set keyword=
set keywordenc=t%%C3%%A4n%%C3%%A4%%C3%%A4n+otsikoissa
C:\projects\areenafeed>java -cp ./webapp/WEB-INF/lib/areenafeed.jar fi.mbnet.akini.areenafeed.AreenaConverterMain "keyword=%keyword%" "keywordenc=%keywordenc%"  quality=hi medialink=asxrefmms media=video  1>rss-simple1.xml

set keyword=Urheiluruutu
set keywordenc=
C:\projects\areenafeed>java -cp ./webapp/WEB-INF/lib/areenafeed.jar fi.mbnet.akini.areenafeed.AreenaConverterMain "keyword=%keyword%" "keywordenc=%keywordenc%"  quality=hi medialink=asxrefmms media=video  1>rss-simple2.xml


Installation
=================
* Tomcat, Jetty or other jsp engine
* JSTL v1.1 tag library
* areenafeed.war web application archive
* Java JRE6 virtual machine

Deploy areenafeed.war package to jsp engine or use a commandline converter.


User guide and documentation
================================
See webapp/index.html for more information

