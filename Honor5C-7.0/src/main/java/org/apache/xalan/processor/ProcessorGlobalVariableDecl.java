package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemVariable;
import org.xml.sax.SAXException;

class ProcessorGlobalVariableDecl extends ProcessorTemplateElem {
    static final long serialVersionUID = -5954332402269819582L;

    ProcessorGlobalVariableDecl() {
    }

    protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        handler.pushElemTemplateElement(elem);
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        ElemVariable v = (ElemVariable) handler.getElemTemplateElement();
        handler.getStylesheet().appendChild((ElemTemplateElement) v);
        handler.getStylesheet().setVariable(v);
        super.endElement(handler, uri, localName, rawName);
    }
}
