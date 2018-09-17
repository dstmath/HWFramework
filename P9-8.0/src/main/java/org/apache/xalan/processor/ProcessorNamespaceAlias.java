package org.apache.xalan.processor;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.NamespaceAlias;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorNamespaceAlias extends XSLTElementProcessor {
    static final long serialVersionUID = -6309867839007018964L;

    ProcessorNamespaceAlias() {
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        String resultNS;
        NamespaceAlias na = new NamespaceAlias(handler.nextUid());
        setPropertiesFromAttributes(handler, rawName, attributes, na);
        String prefix = na.getStylesheetPrefix();
        if (prefix.equals("#default")) {
            prefix = "";
            na.setStylesheetPrefix(prefix);
        }
        na.setStylesheetNamespace(handler.getNamespaceForPrefix(prefix));
        prefix = na.getResultPrefix();
        if (prefix.equals("#default")) {
            prefix = "";
            na.setResultPrefix(prefix);
            resultNS = handler.getNamespaceForPrefix(prefix);
            if (resultNS == null) {
                handler.error(XSLTErrorResources.ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX_FOR_DEFAULT, null, null);
            }
        } else {
            resultNS = handler.getNamespaceForPrefix(prefix);
            if (resultNS == null) {
                handler.error(XSLTErrorResources.ER_INVALID_NAMESPACE_URI_VALUE_FOR_RESULT_PREFIX, new Object[]{prefix}, null);
            }
        }
        na.setResultNamespace(resultNS);
        handler.getStylesheet().setNamespaceAlias(na);
        handler.getStylesheet().appendChild((ElemTemplateElement) na);
    }
}
