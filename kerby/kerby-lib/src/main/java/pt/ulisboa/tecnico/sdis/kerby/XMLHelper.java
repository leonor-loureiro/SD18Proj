package pt.ulisboa.tecnico.sdis.kerby;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class XMLHelper {

	private static final DatatypeFactory DATATYPE_FACTORY;
	private static final TransformerFactory TRANSFORMER_FACTORY;

	/** XML transformer property name for XML indentation amount. */
	private static final String XML_INDENT_AMOUNT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";
	/** XML indentation amount to use (default=0). */
	private static final Integer XML_INDENT_AMOUNT_VALUE = 2;

	// one-time initialization and clean-up
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
			TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(XMLHelper.class.getSimpleName() + " initialization failed!", e);
		}
	}

	// Namespace utilities ---------------------------------------------------

	private static final String reversePackageName(final String packageName) {
		if (packageName == null || packageName.length() == 0)
			throw new IllegalArgumentException("Package name to reverse cannot be empty!");
		String[] part = packageName.split("\\.");
		StringBuilder builder = new StringBuilder();
		for (int i = part.length - 1; i > 0; i--) {
			builder.append(part[i]);
			if (i != 1)
				builder.append(".");
		}
		return builder.toString();
	}

	public static String xmlNamespaceFromJavaPackage(final String packageName) {
		String reverse = reversePackageName(packageName);
		return "http://" + reverse + "/";
	}

	// DOM navigation --------------------------------------------------------

	/**
	 * Return direct child element with specified name. If not found, returns null.
	 */
	public static Element getDirectChild(Element parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			// TODO replace instanceof with W3C DOM check node type call
			if (child instanceof Element && name.equals(child.getNodeName())) {
				return (Element) child;
			}
		}
		return null;
	}

	/**
	 * Return direct child of document with specified name. If not found, returns
	 * null.
	 */
	public static Element getDirectChild(Document document, String name) {
		return getDirectChild(document.getDocumentElement(), name);
	}

	// Textual representation ------------------------------------------------

	public static void printXML(Source xmlSource, PrintStream out) throws TransformerException {
		// transform the (DOM) Source into a StreamResult
		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(XML_INDENT_AMOUNT_PROPERTY, XML_INDENT_AMOUNT_VALUE.toString());
		StreamResult result = new StreamResult(out);
		transformer.transform(xmlSource, result);
	}

	// Date processing -------------------------------------------------------

	public static XMLGregorianCalendar dateToXML(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		return DATATYPE_FACTORY.newXMLGregorianCalendar(gc);
	}

	public static Date xmlToDate(XMLGregorianCalendar xgc) {
		return xgc.toGregorianCalendar().getTime();
	}

	// Generic view marshal --------------------------------------------------

	/** Marshal i.e. convert view object to XML result with tag name. */
	public static <V> void viewToXMLResult(Class<V> viewClass, V view, Result xmlResult, QName tagName)
			throws JAXBException {
		// create a JAXBContext
		JAXBContext jaxb = JAXBContext.newInstance(viewClass.getPackage().getName());

		// create XML element (a complex type cannot be instantiated by itself)
		JAXBElement<V> jaxbElementMarshal = new JAXBElement<>(tagName, viewClass, view);

		// create a Marshaller and marshal
		Marshaller m = jaxb.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // indent
		m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // omit xml declaration
		m.marshal(jaxbElementMarshal, xmlResult);
	}

	/**
	 * Marshal view object to XML document with tag name (in-memory tree, following
	 * the Document Object Model).
	 */
	public static <V> Document viewToXML(Class<V> viewClass, V view, QName tagName) throws JAXBException {
		return (Document) viewToXMLNode(viewClass, view, tagName);
	}

	/**
	 * Marshal view object to XML node with tag name (in-memory tree, following the
	 * Document Object Model).
	 */
	public static <V> Node viewToXMLNode(Class<V> viewClass, V view, QName tagName) throws JAXBException {
		DOMResult domResult = new DOMResult();
		viewToXMLResult(viewClass, view, domResult, tagName);
		return domResult.getNode();
	}

	/** Marshal view object to a byte array of XML with a tag name. */
	public static <V> byte[] viewToXMLBytes(Class<V> viewClass, V view, QName tagName) throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(baos);
		viewToXMLResult(viewClass, view, streamResult, tagName);
		return baos.toByteArray();
	}

	// Generic view unmarshal ------------------------------------------------

	/** Unmarshal i.e. convert XML source provided by caller to a view object. */
	public static <V> V xmlSourceToView(Class<V> viewClass, Source xmlSource) throws JAXBException {
		JAXBContext jaxb = JAXBContext.newInstance(viewClass);
		Unmarshaller u = jaxb.createUnmarshaller();
		// unmarshal, get element and cast to expected type
		JAXBElement<V> element = (JAXBElement<V>) u.unmarshal(xmlSource, viewClass);
		return element.getValue();
	}

	/**
	 * Unmarshal XML document (in-memory tree, following the Document Object Model)
	 * to a view object.
	 */
	public static <V> V xmlToView(Class<V> viewClass, Document document) throws JAXBException {
		return xmlNodeToView(viewClass, document);
	}

	/**
	 * Unmarshal XML node (in-memory tree, following the Document Object Model) to a
	 * view object.
	 */
	public static <V> V xmlNodeToView(Class<V> viewClass, Node node) throws JAXBException {
		DOMSource domSource = new DOMSource(node);
		return xmlSourceToView(viewClass, domSource);
	}

	/** Unmarshal byte array to a view object. */
	public static <V> V xmlBytesToView(Class<V> viewClass, byte[] bytes) throws JAXBException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		StreamSource streamSource = new StreamSource(bais);
		return xmlSourceToView(viewClass, streamSource);
	}

}
