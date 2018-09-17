package org.apache.xml.serializer;

import java.io.IOException;
import java.io.Writer;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.xml.sax.SAXException;

public class ToXMLStream extends ToStream {
    private CharInfo m_xmlcharInfo;

    public ToXMLStream() {
        this.m_xmlcharInfo = CharInfo.getCharInfo(CharInfo.XML_ENTITIES_RESOURCE, "xml");
        this.m_charInfo = this.m_xmlcharInfo;
        initCDATA();
        this.m_prefixMap = new NamespaceMappings();
    }

    public void CopyFrom(ToXMLStream xmlListener) {
        setWriter(xmlListener.m_writer);
        setEncoding(xmlListener.getEncoding());
        setOmitXMLDeclaration(xmlListener.getOmitXMLDeclaration());
        this.m_ispreserve = xmlListener.m_ispreserve;
        this.m_preserves = xmlListener.m_preserves;
        this.m_isprevtext = xmlListener.m_isprevtext;
        this.m_doIndent = xmlListener.m_doIndent;
        setIndentAmount(xmlListener.getIndentAmount());
        this.m_startNewLine = xmlListener.m_startNewLine;
        this.m_needToOutputDocTypeDecl = xmlListener.m_needToOutputDocTypeDecl;
        setDoctypeSystem(xmlListener.getDoctypeSystem());
        setDoctypePublic(xmlListener.getDoctypePublic());
        setStandalone(xmlListener.getStandalone());
        setMediaType(xmlListener.getMediaType());
        this.m_encodingInfo = xmlListener.m_encodingInfo;
        this.m_spaceBeforeClose = xmlListener.m_spaceBeforeClose;
        this.m_cdataStartCalled = xmlListener.m_cdataStartCalled;
    }

    /* JADX WARNING: Missing block: B:22:0x008a, code:
            if (getDoctypeSystem() != null) goto L_0x0079;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startDocumentInternal() throws SAXException {
        if (this.m_needToCallStartDocument) {
            super.startDocumentInternal();
            this.m_needToCallStartDocument = false;
            if (!this.m_inEntityRef) {
                this.m_needToOutputDocTypeDecl = true;
                this.m_startNewLine = false;
                String version = getXMLVersion();
                if (!getOmitXMLDeclaration()) {
                    String standalone;
                    String encoding = Encodings.getMimeEncoding(getEncoding());
                    if (this.m_standaloneWasSpecified) {
                        standalone = " standalone=\"" + getStandalone() + "\"";
                    } else {
                        standalone = "";
                    }
                    try {
                        Writer writer = this.m_writer;
                        writer.write("<?xml version=\"");
                        writer.write(version);
                        writer.write("\" encoding=\"");
                        writer.write(encoding);
                        writer.write(34);
                        writer.write(standalone);
                        writer.write("?>");
                        if (this.m_doIndent) {
                            if (!this.m_standaloneWasSpecified && getDoctypePublic() == null) {
                            }
                            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
                        }
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }
                }
            }
        }
    }

    public void endDocument() throws SAXException {
        flushPending();
        if (this.m_doIndent && (this.m_isprevtext ^ 1) != 0) {
            try {
                outputLineSep();
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
        flushWriter();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    public void startPreserving() throws SAXException {
        this.m_preserves.push(true);
        this.m_ispreserve = true;
    }

    public void endPreserving() throws SAXException {
        this.m_ispreserve = this.m_preserves.isEmpty() ? false : this.m_preserves.pop();
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (!this.m_inEntityRef) {
            flushPending();
            if (target.equals("javax.xml.transform.disable-output-escaping")) {
                startNonEscaping();
            } else if (target.equals("javax.xml.transform.enable-output-escaping")) {
                endNonEscaping();
            } else {
                try {
                    if (this.m_elemContext.m_startTagOpen) {
                        closeStartTag();
                        this.m_elemContext.m_startTagOpen = false;
                    } else if (this.m_needToCallStartDocument) {
                        startDocumentInternal();
                    }
                    if (shouldIndent()) {
                        indent();
                    }
                    Writer writer = this.m_writer;
                    writer.write("<?");
                    writer.write(target);
                    if (data.length() > 0 && (Character.isSpaceChar(data.charAt(0)) ^ 1) != 0) {
                        writer.write(32);
                    }
                    int indexOfQLT = data.indexOf("?>");
                    if (indexOfQLT >= 0) {
                        if (indexOfQLT > 0) {
                            writer.write(data.substring(0, indexOfQLT));
                        }
                        writer.write("? >");
                        if (indexOfQLT + 2 < data.length()) {
                            writer.write(data.substring(indexOfQLT + 2));
                        }
                    } else {
                        writer.write(data);
                    }
                    writer.write(63);
                    writer.write(62);
                    this.m_startNewLine = true;
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
            if (this.m_tracer != null) {
                super.fireEscapingEvent(target, data);
            }
        }
    }

    public void entityReference(String name) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
        }
        try {
            if (shouldIndent()) {
                indent();
            }
            Writer writer = this.m_writer;
            writer.write(38);
            writer.write(name);
            writer.write(59);
            if (this.m_tracer != null) {
                super.fireEntityReference(name);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void addUniqueAttribute(String name, String value, int flags) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            try {
                String patchedName = patchName(name);
                Writer writer = this.m_writer;
                if ((flags & 1) <= 0 || !this.m_xmlcharInfo.onlyQuotAmpLtGt) {
                    writer.write(32);
                    writer.write(patchedName);
                    writer.write("=\"");
                    writeAttrString(writer, value, getEncoding());
                    writer.write(34);
                    return;
                }
                writer.write(32);
                writer.write(patchedName);
                writer.write("=\"");
                writer.write(value);
                writer.write(34);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void addAttribute(String uri, String localName, String rawName, String type, String value, boolean xslAttribute) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            if (!(!addAttributeAlways(uri, localName, rawName, type, value, xslAttribute) || (xslAttribute ^ 1) == 0 || (rawName.startsWith("xmlns") ^ 1) == 0)) {
                String prefixUsed = ensureAttributesNamespaceIsDeclared(uri, localName, rawName);
                if (!(prefixUsed == null || rawName == null || (rawName.startsWith(prefixUsed) ^ 1) == 0)) {
                    rawName = prefixUsed + ":" + localName;
                }
            }
            addAttributeAlways(uri, localName, rawName, type, value, xslAttribute);
            return;
        }
        String msg = Utils.messages.createMessage(MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, new Object[]{localName});
        try {
            ErrorListener errHandler = super.getTransformer().getErrorListener();
            if (errHandler == null || this.m_sourceLocator == null) {
                System.out.println(msg);
            } else {
                errHandler.warning(new TransformerException(msg, this.m_sourceLocator));
            }
        } catch (TransformerException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String elemName) throws SAXException {
        endElement(null, null, elemName);
    }

    public void namespaceAfterStartElement(String prefix, String uri) throws SAXException {
        if (this.m_elemContext.m_elementURI == null && SerializerBase.getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(prefix)) {
            this.m_elemContext.m_elementURI = uri;
        }
        startPrefixMapping(prefix, uri, false);
    }

    protected boolean pushNamespace(String prefix, String uri) {
        try {
            if (this.m_prefixMap.pushNamespace(prefix, uri, this.m_elemContext.m_currentElemDepth)) {
                startPrefixMapping(prefix, uri);
                return true;
            }
        } catch (SAXException e) {
        }
        return false;
    }

    public boolean reset() {
        if (super.reset()) {
            return true;
        }
        return false;
    }

    private void resetToXMLStream() {
    }

    private String getXMLVersion() {
        String xmlVersion = getVersion();
        if (xmlVersion == null || xmlVersion.equals(SerializerConstants.XMLVERSION10)) {
            return SerializerConstants.XMLVERSION10;
        }
        if (xmlVersion.equals(SerializerConstants.XMLVERSION11)) {
            return SerializerConstants.XMLVERSION11;
        }
        String msg = Utils.messages.createMessage(MsgKey.ER_XML_VERSION_NOT_SUPPORTED, new Object[]{xmlVersion});
        try {
            ErrorListener errHandler = super.getTransformer().getErrorListener();
            if (errHandler == null || this.m_sourceLocator == null) {
                System.out.println(msg);
                return SerializerConstants.XMLVERSION10;
            }
            errHandler.warning(new TransformerException(msg, this.m_sourceLocator));
            return SerializerConstants.XMLVERSION10;
        } catch (Exception e) {
        }
    }
}
