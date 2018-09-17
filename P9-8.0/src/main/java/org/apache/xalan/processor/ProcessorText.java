package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemText;
import org.xml.sax.SAXException;

public class ProcessorText extends ProcessorTemplateElem {
    static final long serialVersionUID = 5170229307201307523L;

    protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        ((ProcessorCharacters) handler.getProcessorFor(null, "text()", "text")).setXslTextElement((ElemText) elem);
        handler.getElemTemplateElement().appendChild(elem);
        elem.setDOMBackPointer(handler.getOriginatingNode());
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        ((ProcessorCharacters) handler.getProcessorFor(null, "text()", "text")).setXslTextElement(null);
    }
}
