package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemApplyImport;
import org.apache.xalan.templates.ElemApplyTemplates;
import org.apache.xalan.templates.ElemAttribute;
import org.apache.xalan.templates.ElemCallTemplate;
import org.apache.xalan.templates.ElemComment;
import org.apache.xalan.templates.ElemCopy;
import org.apache.xalan.templates.ElemCopyOf;
import org.apache.xalan.templates.ElemElement;
import org.apache.xalan.templates.ElemExsltFuncResult;
import org.apache.xalan.templates.ElemExsltFunction;
import org.apache.xalan.templates.ElemFallback;
import org.apache.xalan.templates.ElemLiteralResult;
import org.apache.xalan.templates.ElemMessage;
import org.apache.xalan.templates.ElemNumber;
import org.apache.xalan.templates.ElemPI;
import org.apache.xalan.templates.ElemParam;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemText;
import org.apache.xalan.templates.ElemTextLiteral;
import org.apache.xalan.templates.ElemValueOf;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.Stylesheet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessorExsltFunction extends ProcessorTemplateElem {
    static final long serialVersionUID = 2411427965578315332L;

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        String msg = "";
        if (!(handler.getElemTemplateElement() instanceof Stylesheet)) {
            msg = "func:function element must be top level.";
            handler.error(msg, new SAXException(msg));
        }
        super.startElement(handler, uri, localName, rawName, attributes);
        if (attributes.getValue("name").indexOf(":") <= 0) {
            msg = "func:function name must have namespace";
            handler.error(msg, new SAXException(msg));
        }
    }

    protected void appendAndPush(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        super.appendAndPush(handler, elem);
        elem.setDOMBackPointer(handler.getOriginatingNode());
        handler.getStylesheet().setTemplate((ElemTemplate) elem);
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        validate(handler.getElemTemplateElement(), handler);
        super.endElement(handler, uri, localName, rawName);
    }

    public void validate(ElemTemplateElement elem, StylesheetHandler handler) throws SAXException {
        String msg = "";
        while (elem != null) {
            if (!(!(elem instanceof ElemExsltFuncResult) || elem.getNextSiblingElem() == null || ((elem.getNextSiblingElem() instanceof ElemFallback) ^ 1) == 0)) {
                msg = "func:result has an illegal following sibling (only xsl:fallback allowed)";
                handler.error(msg, new SAXException(msg));
            }
            if (((elem instanceof ElemApplyImport) || (elem instanceof ElemApplyTemplates) || (elem instanceof ElemAttribute) || (elem instanceof ElemCallTemplate) || (elem instanceof ElemComment) || (elem instanceof ElemCopy) || (elem instanceof ElemCopyOf) || (elem instanceof ElemElement) || (elem instanceof ElemLiteralResult) || (elem instanceof ElemNumber) || (elem instanceof ElemPI) || (elem instanceof ElemText) || (elem instanceof ElemTextLiteral) || (elem instanceof ElemValueOf)) && (ancestorIsOk(elem) ^ 1) != 0) {
                msg = "misplaced literal result in a func:function container.";
                handler.error(msg, new SAXException(msg));
            }
            ElemTemplateElement nextElem = elem.getFirstChildElem();
            while (nextElem == null) {
                nextElem = elem.getNextSiblingElem();
                if (nextElem == null) {
                    elem = elem.getParentElem();
                }
                if (elem != null) {
                    if (elem instanceof ElemExsltFunction) {
                    }
                }
                return;
            }
            elem = nextElem;
        }
    }

    boolean ancestorIsOk(ElemTemplateElement child) {
        while (child.getParentElem() != null && ((child.getParentElem() instanceof ElemExsltFunction) ^ 1) != 0) {
            ElemTemplateElement parent = child.getParentElem();
            if ((parent instanceof ElemExsltFuncResult) || (parent instanceof ElemVariable) || (parent instanceof ElemParam) || (parent instanceof ElemMessage)) {
                return true;
            }
            child = parent;
        }
        return false;
    }
}
