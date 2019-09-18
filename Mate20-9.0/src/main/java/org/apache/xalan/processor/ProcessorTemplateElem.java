package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessorTemplateElem extends XSLTElementProcessor {
    static final long serialVersionUID = 8344994001943407235L;

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        super.startElement(handler, uri, localName, rawName, attributes);
        try {
            ElemTemplateElement elem = null;
            try {
                elem = (ElemTemplateElement) getElemDef().getClassObject().newInstance();
                elem.setDOMBackPointer(handler.getOriginatingNode());
                elem.setLocaterInfo(handler.getLocator());
                elem.setPrefixes(handler.getNamespaceSupport());
            } catch (InstantiationException ie) {
                handler.error(XSLTErrorResources.ER_FAILED_CREATING_ELEMTMPL, null, ie);
            } catch (IllegalAccessException iae) {
                handler.error(XSLTErrorResources.ER_FAILED_CREATING_ELEMTMPL, null, iae);
            }
            setPropertiesFromAttributes(handler, rawName, attributes, elem);
            appendAndPush(handler, elem);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: protected */
    public void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        ElemTemplateElement parent = handler.getElemTemplateElement();
        if (parent != null) {
            parent.appendChild(elem);
            handler.pushElemTemplateElement(elem);
        }
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        super.endElement(handler, uri, localName, rawName);
        handler.popElemTemplateElement().setEndLocaterInfo(handler.getLocator());
    }
}
