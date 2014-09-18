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
http://code.google.com/p/ps3mediaserver/


Example urls:
http://localhost:8080/areenafeed/yleareena.jsp?quality=hi&medialink=asxrefmms&media=video&keyword=Reinikainen
http://localhost:8080/areenafeed/yleareena.jsp?quality=lo&medialink=asxrefmms&cid=164618&pid=215452
http://localhost:8080/areenafeed/youtube.jsp?title=Playstation+channel&desc=Official+Playstation+videos&url=profile?user=PlayStation%26view=videos
http://localhost:8080/areenafeed/googlevideo.jsp?medialink=e26&desc=Jenni+Vartiainen+videos&keyword=Jenni+Vartiainen
http://localhost:8080/areenafeed/vimeo.jsp?title=Documentary+films&quality=lo&url=channels/documentaryfilm/videos/rss
http://localhost:8080/areenafeed/areenafeed/theonion.jsp?title=Most+Recent&url=content/ajax/onn/list/8/0/mostrecent
http://localhost:8080/areenafeed/areenafeed/ct24cz.jsp?medialink=asxrefmms&title=Programmes&maxitems=5&url=vysilani/?streamtype=WM2


Create rss documents offline
==============================
Use commandline to create offline documents.
see createSimpleDocument.bat example script.


Use javascript
==============================
Use commandline to run javascript script to parse adhoc output format.
see callJavascript.bat example script.



Installation
=================
* Tomcat, Jetty or other jsp engine
* JSTL v1.1 tag library
* Java JRE6 virtual machine
* areenafeed.war web application archive

Deploy areenafeed.war package to jsp engine or use a commandline converter.


User guide and documentation
================================
See webapp/index.html for more information
See webapp/installation.txt for more Tomcat6 installation
See http://code.google.com/p/ps3mediaserver/ for PS3MS mediaserver
