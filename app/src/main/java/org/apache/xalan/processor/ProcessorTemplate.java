package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.xml.sax.SAXException;

class ProcessorTemplate extends ProcessorTemplateElem {
    static final long serialVersionUID = -8457812845473603860L;

    ProcessorTemplate() {
    }

    protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        super.appendAndPush(handler, elem);
        elem.setDOMBackPointer(handler.getOriginatingNode());
        handler.getStylesheet().setTemplate((ElemTemplate) elem);
    }
}
