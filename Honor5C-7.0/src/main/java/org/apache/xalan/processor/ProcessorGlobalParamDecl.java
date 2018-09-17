package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemParam;
import org.apache.xalan.templates.ElemTemplateElement;
import org.xml.sax.SAXException;

class ProcessorGlobalParamDecl extends ProcessorTemplateElem {
    static final long serialVersionUID = 1900450872353587350L;

    ProcessorGlobalParamDecl() {
    }

    protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        handler.pushElemTemplateElement(elem);
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        ElemParam v = (ElemParam) handler.getElemTemplateElement();
        handler.getStylesheet().appendChild((ElemTemplateElement) v);
        handler.getStylesheet().setParam(v);
        super.endElement(handler, uri, localName, rawName);
    }
}
