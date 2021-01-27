package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.Writer;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.SAXException;

public final class ToXMLStream extends ToStream {
    private static CharInfo m_xmlcharInfo = CharInfo.getCharInfoInternal(CharInfo.XML_ENTITIES_RESOURCE, "xml");
    boolean m_cdataTagOpen = false;

    public ToXMLStream() {
        this.m_charInfo = m_xmlcharInfo;
        initCDATA();
        this.m_prefixMap = new NamespaceMappings();
    }

    public void CopyFrom(ToXMLStream toXMLStream) {
        this.m_writer = toXMLStream.m_writer;
        setEncoding(toXMLStream.getEncoding());
        setOmitXMLDeclaration(toXMLStream.getOmitXMLDeclaration());
        this.m_ispreserve = toXMLStream.m_ispreserve;
        this.m_preserves = toXMLStream.m_preserves;
        this.m_isprevtext = toXMLStream.m_isprevtext;
        this.m_doIndent = toXMLStream.m_doIndent;
        setIndentAmount(toXMLStream.getIndentAmount());
        this.m_startNewLine = toXMLStream.m_startNewLine;
        this.m_needToOutputDocTypeDecl = toXMLStream.m_needToOutputDocTypeDecl;
        setDoctypeSystem(toXMLStream.getDoctypeSystem());
        setDoctypePublic(toXMLStream.getDoctypePublic());
        setStandalone(toXMLStream.getStandalone());
        setMediaType(toXMLStream.getMediaType());
        this.m_maxCharacter = toXMLStream.m_maxCharacter;
        this.m_encodingInfo = toXMLStream.m_encodingInfo;
        this.m_spaceBeforeClose = toXMLStream.m_spaceBeforeClose;
        this.m_cdataStartCalled = toXMLStream.m_cdataStartCalled;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void startDocumentInternal() throws SAXException {
        String str;
        if (this.m_needToCallStartDocument) {
            super.startDocumentInternal();
            this.m_needToCallStartDocument = false;
            if (!this.m_inEntityRef) {
                this.m_needToOutputDocTypeDecl = true;
                this.m_startNewLine = false;
                if (!getOmitXMLDeclaration()) {
                    String mimeEncoding = Encodings.getMimeEncoding(getEncoding());
                    String version = getVersion();
                    if (version == null) {
                        version = "1.0";
                    }
                    if (this.m_standaloneWasSpecified) {
                        str = " standalone=\"" + getStandalone() + "\"";
                    } else {
                        str = "";
                    }
                    try {
                        Writer writer = this.m_writer;
                        writer.write("<?xml version=\"");
                        writer.write(version);
                        writer.write("\" encoding=\"");
                        writer.write(mimeEncoding);
                        writer.write(34);
                        writer.write(str);
                        writer.write("?>");
                        if (!this.m_doIndent) {
                            return;
                        }
                        if (this.m_standaloneWasSpecified || getDoctypePublic() != null || getDoctypeSystem() != null || this.m_isStandalone) {
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
        if (this.m_doIndent && !this.m_isprevtext) {
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

    public void processingInstruction(String str, String str2) throws SAXException {
        if (!this.m_inEntityRef) {
            flushPending();
            if (str.equals("javax.xml.transform.disable-output-escaping")) {
                startNonEscaping();
            } else if (str.equals("javax.xml.transform.enable-output-escaping")) {
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
                    writer.write(str);
                    if (str2.length() > 0 && !Character.isSpaceChar(str2.charAt(0))) {
                        writer.write(32);
                    }
                    int indexOf = str2.indexOf("?>");
                    if (indexOf >= 0) {
                        if (indexOf > 0) {
                            writer.write(str2.substring(0, indexOf));
                        }
                        writer.write("? >");
                        int i = indexOf + 2;
                        if (i < str2.length()) {
                            writer.write(str2.substring(i));
                        }
                    } else {
                        writer.write(str2);
                    }
                    writer.write(63);
                    writer.write(62);
                    if (this.m_elemContext.m_currentElemDepth <= 0 && this.m_isStandalone) {
                        writer.write(this.m_lineSep, 0, this.m_lineSepLen);
                    }
                    this.m_startNewLine = true;
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
            if (this.m_tracer != null) {
                super.fireEscapingEvent(str, str2);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void entityReference(String str) throws SAXException {
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
            writer.write(str);
            writer.write(59);
            if (this.m_tracer != null) {
                super.fireEntityReference(str);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addUniqueAttribute(String str, String str2, int i) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            try {
                String patchName = patchName(str);
                Writer writer = this.m_writer;
                if ((i & 1) <= 0 || !m_xmlcharInfo.onlyQuotAmpLtGt) {
                    writer.write(32);
                    writer.write(patchName);
                    writer.write("=\"");
                    writeAttrString(writer, str2, getEncoding());
                    writer.write(34);
                    return;
                }
                writer.write(32);
                writer.write(patchName);
                writer.write("=\"");
                writer.write(str2);
                writer.write(34);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) throws SAXException {
        String ensureAttributesNamespaceIsDeclared;
        if (this.m_elemContext.m_startTagOpen) {
            if (addAttributeAlways(str, str2, str3, str4, str5, z) && !z && !str3.startsWith("xmlns") && (ensureAttributesNamespaceIsDeclared = ensureAttributesNamespaceIsDeclared(str, str2, str3)) != null && !str3.startsWith(ensureAttributesNamespaceIsDeclared)) {
                str3 = ensureAttributesNamespaceIsDeclared + ":" + str2;
            }
            addAttributeAlways(str, str2, str3, str4, str5, z);
            return;
        }
        String createMessage = Utils.messages.createMessage("ER_ILLEGAL_ATTRIBUTE_POSITION", new Object[]{str2});
        try {
            ErrorListener errorListener = super.getTransformer().getErrorListener();
            if (errorListener == null || this.m_sourceLocator == null) {
                System.out.println(createMessage);
            } else {
                errorListener.warning(new TransformerException(createMessage, this.m_sourceLocator));
            }
        } catch (Exception unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        endElement(null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
        if (this.m_elemContext.m_elementURI == null && getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(str)) {
            this.m_elemContext.m_elementURI = str2;
        }
        startPrefixMapping(str, str2, false);
    }

    /* access modifiers changed from: protected */
    public boolean pushNamespace(String str, String str2) {
        try {
            if (!this.m_prefixMap.pushNamespace(str, str2, this.m_elemContext.m_currentElemDepth)) {
                return false;
            }
            startPrefixMapping(str, str2);
            return true;
        } catch (SAXException unused) {
            return false;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToXMLStream();
        return true;
    }

    private void resetToXMLStream() {
        this.m_cdataTagOpen = false;
    }

    private String getXMLVersion() {
        String version = getVersion();
        if (version == null || version.equals("1.0")) {
            return "1.0";
        }
        if (version.equals(SerializerConstants.XMLVERSION11)) {
            return SerializerConstants.XMLVERSION11;
        }
        String createMessage = Utils.messages.createMessage(MsgKey.ER_XML_VERSION_NOT_SUPPORTED, new Object[]{version});
        try {
            ErrorListener errorListener = super.getTransformer().getErrorListener();
            if (errorListener == null || this.m_sourceLocator == null) {
                System.out.println(createMessage);
                return "1.0";
            }
            errorListener.warning(new TransformerException(createMessage, this.m_sourceLocator));
            return "1.0";
        } catch (Exception unused) {
            return "1.0";
        }
    }
}
