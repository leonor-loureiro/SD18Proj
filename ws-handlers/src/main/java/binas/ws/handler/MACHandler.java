package binas.ws.handler;

import javax.crypto.Mac;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.Key;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class MACHandler implements SOAPHandler<SOAPMessageContext> {

    /**
     * Message authentication code algorithm.
     */
    private static final String MAC_ALGO       = "HmacSHA256";

    // Header names
    private static final String REQ_MAC_HEADER = "MACHeader";

    // Namespace
    private static final String SERVER_NS      = "urn:binas.server.authentication";
    private static final String SOAP_PREFIX    = "b";

    //Context
    private static final String CONTEXT_SESSION_KEY = "context_session_key";

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            handleOUT(context);
        } else {
            handleIN(context);
        }
        return true;
    }
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }

    private void handleIN(SOAPMessageContext context) {
        myVerifyMAC(context.getMessage(), extractSessionKey(context));
    }

    private void handleOUT(SOAPMessageContext context) {
        appendMAC(context.getMessage(), myMakeMAC(context.getMessage(),extractSessionKey(context)));
    }

    private Key extractSessionKey(SOAPMessageContext context) {
        return (Key) context.get(CONTEXT_SESSION_KEY);
    }

    private void myVerifyMAC(SOAPMessage msg, Key sessionKey) {
       if ( !verifyMAC(myMakeMAC(msg, sessionKey), extractMac(msg)) )
           throw new RuntimeException("Invalid MAC");
    }

    private byte[] extractMac(SOAPMessage msg) {
        try {
            SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            Name name = envelope.createName(REQ_MAC_HEADER, SOAP_PREFIX, SERVER_NS);
            Iterator childElements = header.getChildElements(name);

            if (!childElements.hasNext()) {
                return null;
            }

            String res = ((SOAPElement) childElements.next()).getValue();

            return parseBase64Binary(res);

        } catch (SOAPException e) {
            throw new RuntimeException("Error extracting MAC from message");
        }
    }

    private void appendMAC(SOAPMessage message, byte[] mac) {
        String macString = printBase64Binary(mac);
        SOAPHeader header;
        try {
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            // add header
            header = envelope.getHeader();
            if (header == null)
                header = envelope.addHeader();

            // add header element (name, namespace prefix, namespace)
            Name name = envelope.createName(REQ_MAC_HEADER, SOAP_PREFIX, SERVER_NS);
            SOAPHeaderElement macHeader = header.addHeaderElement(name);

            macHeader.addTextNode(macString);

        } catch (SOAPException e) {
            throw new RuntimeException("Error whilst appending MAC to SOAPMessage");
        }
    }

    private byte[] myMakeMAC(SOAPMessage msg, Key key) {
        byte[] mac;

        try {
            mac = makeMAC(SOAPMessageToByteArray(msg), key);
        } catch (Exception e) {
            throw new RuntimeException("Error on MAC creation");
        }
        return mac;
    }

    private byte[] makeMAC(byte[] bytes, Key key) throws Exception {

        Mac cipher = Mac.getInstance(MAC_ALGO);
        cipher.init(key);

        return cipher.doFinal(bytes);
    }

    private boolean verifyMAC(byte[] cipherDigest, byte[] bytes) {
        return Arrays.equals(cipherDigest, bytes);
    }

    private static byte[] SOAPMessageToByteArray(SOAPMessage msg) throws Exception {
        return parseBase64Binary(msg.getSOAPBody().getTextContent());
    }
}
