package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemText;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xml.utils.XMLCharacterRecognizer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ProcessorCharacters extends XSLTElementProcessor {
    static final long serialVersionUID = 8632900007814162650L;
    private StringBuffer m_accumulator = new StringBuffer();
    protected Node m_firstBackPointer = null;
    private ElemText m_xslTextElement;

    public void startNonText(StylesheetHandler handler) throws SAXException {
        if (this == handler.getCurrentProcessor()) {
            handler.popProcessor();
        }
        int nChars = this.m_accumulator.length();
        if ((nChars > 0 && !(this.m_xslTextElement == null && (XMLCharacterRecognizer.isWhiteSpace(this.m_accumulator) ^ 1) == 0)) || handler.isSpacePreserve()) {
            ElemTemplateElement elem = new ElemTextLiteral();
            elem.setDOMBackPointer(this.m_firstBackPointer);
            elem.setLocaterInfo(handler.getLocator());
            try {
                elem.setPrefixes(handler.getNamespaceSupport());
                elem.setDisableOutputEscaping(this.m_xslTextElement != null ? this.m_xslTextElement.getDisableOutputEscaping() : false);
                elem.setPreserveSpace(true);
                char[] chars = new char[nChars];
                this.m_accumulator.getChars(0, nChars, chars, 0);
                elem.setChars(chars);
                handler.getElemTemplateElement().appendChild(elem);
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        this.m_accumulator.setLength(0);
        this.m_firstBackPointer = null;
    }

    public void characters(StylesheetHandler handler, char[] ch, int start, int length) throws SAXException {
        this.m_accumulator.append(ch, start, length);
        if (this.m_firstBackPointer == null) {
            this.m_firstBackPointer = handler.getOriginatingNode();
        }
        if (this != handler.getCurrentProcessor()) {
            handler.pushProcessor(this);
        }
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        startNonText(handler);
        handler.getCurrentProcessor().endElement(handler, uri, localName, rawName);
        handler.popProcessor();
    }

    void setXslTextElement(ElemText xslTextElement) {
        this.m_xslTextElement = xslTextElement;
    }
}
