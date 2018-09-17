package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemAttributeSet;
import org.apache.xalan.templates.ElemTemplateElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorAttributeSet extends XSLTElementProcessor {
    static final long serialVersionUID = -6473739251316787552L;

    ProcessorAttributeSet() {
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        ElemTemplateElement eat = new ElemAttributeSet();
        eat.setLocaterInfo(handler.getLocator());
        try {
            eat.setPrefixes(handler.getNamespaceSupport());
            eat.setDOMBackPointer(handler.getOriginatingNode());
            setPropertiesFromAttributes(handler, rawName, attributes, eat);
            handler.getStylesheet().setAttributeSet(eat);
            handler.getElemTemplateElement().appendChild(eat);
            handler.pushElemTemplateElement(eat);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        handler.popElemTemplateElement();
    }
}
