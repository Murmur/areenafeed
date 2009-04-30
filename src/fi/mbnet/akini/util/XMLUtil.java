/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.util;

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
//import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;

/**
 * XML util
 */
public class XMLUtil {

	/**
	 * Create document from given string content.
	 * @param data	content of xml document
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Document createDocument(String data) throws IllegalArgumentException {
		return createDocument(data.toCharArray());
	}

	/**
	 * Create document from given array.
	 * @param data
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Document createDocument(char[] data) throws IllegalArgumentException {
		Reader reader = new CharArrayReader(data);
		return createDocument(reader);
	}

	public static Document createDocument(Reader reader) throws IllegalArgumentException {
		// init DOM builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setValidating(false);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource inputSource = new InputSource(reader);
			Document doc = builder.parse(inputSource);
			return doc;		
		} catch (Exception ex) {
			IllegalArgumentException iae = 
				new IllegalArgumentException(ex.getMessage());
			iae.initCause(ex);
			throw iae;
		}
	}

	/**
	 * Split string to array of path array.
	 * @param path  "/root/elementA/elementB"
	 * @return
	 */
	public static String[] toPathArray(String path) {
		if (path.charAt(0) == '/') path = path.substring(1);
		return path.split("/");
	}
	
	/**
	 * Return array of elements from given path.
	 * @param root	parent element
	 * @param path path array ("/ElementName")
	 * @return array of elements or null
	 */
	public static List<Element> getElements(Element root, String[] path) {
		NodeList nodes = root.getElementsByTagName(path[0]);
		return getElements(nodes, path);		
	}

	/**
	 * Return array of elements from given path.
	 * @param doc
	 * @param path		path array ("/root/items/item")
	 * @return
	 */
	public static List<Element> getElements(Document doc, String[] path) {
		NodeList nodes = doc.getElementsByTagName(path[0]);
		return getElements(nodes, path);		
	}

	public static List<Element> getElements(NodeList nodes, String[] path) {
		if (nodes == null) return null;
		
		for(int i=1; i < path.length; i++) {
			Element element = (Element)nodes.item(0);
			if (element == null) break;
			nodes = element.getElementsByTagName(path[i]);
		}
		if ( (nodes == null) || (nodes.getLength() < 1) ) return null;

		List<Element> list = new ArrayList<Element>();
		for(int i=0; i < nodes.getLength(); i++) {
			Element element = (Element)nodes.item(i);
			list.add(element);
		}

		return (list.size() < 1 ? null : list);
	}

	/**
	 * Get textvalue from element path or attribute
	 * @param doc
	 * @param path	path ("/root/channel/name") or
	 * 				attribute ("/root/channel/@id")
	 * @return
	 */
	public static String getText(Document doc, String[] path) {
		NodeList nodes = doc.getElementsByTagName(path[0]);
		Element element = (Element)nodes.item(0);
		return getText(element, path, 1);
	}

	/**
	 * Get textvalue from element path or attribute.
	 * @param doc
	 * @param path
	 * @return
	 */
	public static String getText(Document doc, String path) {
		return getText(doc, toPathArray(path));
	}

	/**
	 * Get textvalue from path or attribute. This returns text from first
	 * node found or NULL if path if unknown
	 * @param element	xml element to be used as a root node
	 * @param path		/elementA/elementB
	 * 					/elementA/elementB/@attributeName
	 * @return
	 */
	public static String getText(Element element, String path) {
		return getText(element, toPathArray(path));
	}
	
	public static String getText(Element rootElem, String[] path) {
		NodeList nodes = rootElem.getElementsByTagName(path[0]);
		if (nodes == null || nodes.getLength() < 1) {
			// failsafe if first item is @attribute identifier
			// then read attribute value from rootElement.
			boolean isAttrText = path[0].charAt(0) == '@';
			if (!isAttrText) return null;
			Attr attr = rootElem.getAttributeNode(path[0].substring(1));
	    	return (attr != null ? attr.getValue(): null);
		}
		Element element = (Element)nodes.item(0);
		return getText(element, path, 1);
	}

	/**
	 * Get textvalue from path or attribute. This is 
	 * called by other getText() methods. Endusers usually
	 * should not call this directly.
	 * @param element
	 * @param path
	 * @param pathOffset
	 * @return
	 */
	public static String getText(Element element, String[] path, int pathOffset) {
		int len = path.length;
		boolean isAttrText = path[len-1].charAt(0) == '@';
		if (isAttrText)
			len--; // last item is @attribute identifier
		
		// start path from given offset index
		for(int i=pathOffset; i < len; i++) {
			if (element == null) return null;
			NodeList nodes = element.getElementsByTagName(path[i]);
			element = (Element)nodes.item(0);
		}
		
		if (isAttrText) {
			if (element == null) return null;
	    	Attr attr = element.getAttributeNode(path[len].substring(1));
	    	return (attr != null ? attr.getValue(): null);
		} else {
			return getSimpleText(element);
		}		
	}
	
	/**
	 * Get text from element. 
	 * This concatenates all "#text" child nodes.
	 * @param element
	 * @return
	 */
	public static String getText(Element element) { 
		return getSimpleText(element);
	}

	/**
	 * Get textvalue from element. Loop all #text chidlnoes.
	 * This is used by getText() methods, endusers usually
	 * should not call this directly.
	 */
	private static String getSimpleText(Element element) {
		if (element == null) return null;
		StringBuilder sb = new StringBuilder();
		NodeList nodes = element.getChildNodes();
		Node node;
		for(int i=0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			if (node.getNodeType() == Node.TEXT_NODE)
				sb.append(node.getNodeValue());
		}
		return sb.toString().trim();
	}
	
	/**
	 * Lowercase element and attribute names.
	 * @param doc
	 * @return	return xml document as string
	 */
	public static String toLowerCase(Document doc) {
		StringBuilder sbuf = new StringBuilder(32*1024);
		Element elem = doc.getDocumentElement(); // root element
		toLowerCase(elem, 0, sbuf);
		return sbuf.toString().trim();
	}
	
	private static void toLowerCase(Element elem, int level, StringBuilder sbuf) {
		sbuf.append(StringUtil.lineSeparator);
		
		String indent="";
		for(int idx=0; idx < level; idx++) indent += " ";

		// start element and attributes
		String name = elem.getNodeName().toLowerCase();
		sbuf.append(indent + "<" + name);
		NamedNodeMap attributes = elem.getAttributes();
		if (attributes != null && attributes.getLength() > 0) {
			for(int idx=0; idx < attributes.getLength(); idx++) {
				sbuf.append(" " + attributes.item(idx).getNodeName().toLowerCase());
				sbuf.append("=\"" + ConvertUtil.XMLEncode(attributes.item(idx).getNodeValue()) + "\"" );
			}
		}

		// value
		sbuf.append(">");
		sbuf.append( ConvertUtil.XMLEncode(getSimpleText(elem)) );
		
		// loop child elements
		int elemCount=0;
		level++;
		NodeList nodes = elem.getChildNodes();
		for(int idx=0; idx < nodes.getLength(); idx++) {
			if (nodes.item(idx) instanceof Element) {
				elemCount++;
				Element childElem = (Element)nodes.item(idx);
				toLowerCase(childElem, level, sbuf);
			}
		}
		if (elemCount > 0) sbuf.append(StringUtil.lineSeparator + indent);

		// close element
		sbuf.append("</" + name + ">");
	}
	
}
