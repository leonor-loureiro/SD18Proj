package pt.ulisboa.tecnico.sdis.kerby;

import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.viewToXML;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.viewToXMLBytes;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.xmlBytesToView;
import static pt.ulisboa.tecnico.sdis.kerby.XMLHelper.xmlNodeToView;

import java.util.Arrays;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

/**
 * Class that handles ciphered views in different formats. Ciphered views are used
 * to transport ciphered Kerberos tickets, authenticators, etc.
 * 
 * @author Miguel Pardal
 *
 */
public class CipherClerk {

	// creation --------------------------------------------------------------

	/** Create CipheredView from arguments. */
	public CipheredView cipherBuild(byte[] data) {
		CipheredView view = new CipheredView();
		view.setData(data);
		return view;
	}

	/** Create a textual representation of the CipheredView. */
	public String cipherToString(CipheredView view) {
		if (view == null)
			return "null";

		StringBuilder builder = new StringBuilder();
		builder.append("CipheredView [data=");
		builder.append(Arrays.toString(view.getData()));
		builder.append("]");
		return builder.toString();
	}

	// serialization ---------------------------------------------------------

	/**
	 * Marshal ciphered view to XML document.
	 */
	public Node cipherToXMLNode(CipheredView view, String cipherTagName) throws JAXBException {
		return viewToXML(CipheredView.class, view, new QName(cipherTagName));
	}

	/** Marshal ciphered view to XML bytes. */
	public byte[] cipherToXMLBytes(CipheredView view, String cipherTagName) throws JAXBException {
		return viewToXMLBytes(CipheredView.class, view, new QName(cipherTagName));
	}

	/**
	 * Unmarshal ciphered view from XML document.
	 */
	public CipheredView cipherFromXMLNode(Node xml) throws JAXBException {
		return xmlNodeToView(CipheredView.class, xml);
	}

	/** Unmarshal byte array to a ciphered view object. */
	public CipheredView cipherFromXMLBytes(byte[] bytes) throws JAXBException {
		return xmlBytesToView(CipheredView.class, bytes);
	}

}
