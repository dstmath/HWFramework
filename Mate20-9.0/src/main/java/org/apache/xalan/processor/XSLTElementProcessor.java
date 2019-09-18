package org.apache.xalan.processor;

import java.util.ArrayList;
import java.util.List;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.IntStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XSLTElementProcessor extends ElemTemplateElement {
    static final long serialVersionUID = 5597421564955304421L;
    private XSLTElementDef m_elemDef;
    private IntStack m_savedLastOrder;

    XSLTElementProcessor() {
    }

    /* access modifiers changed from: package-private */
    public XSLTElementDef getElemDef() {
        return this.m_elemDef;
    }

    /* access modifiers changed from: package-private */
    public void setElemDef(XSLTElementDef def) {
        this.m_elemDef = def;
    }

    public InputSource resolveEntity(StylesheetHandler handler, String publicId, String systemId) throws SAXException {
        return null;
    }

    public void notationDecl(StylesheetHandler handler, String name, String publicId, String systemId) {
    }

    public void unparsedEntityDecl(StylesheetHandler handler, String name, String publicId, String systemId, String notationName) {
    }

    public void startNonText(StylesheetHandler handler) throws SAXException {
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        if (this.m_savedLastOrder == null) {
            this.m_savedLastOrder = new IntStack();
        }
        this.m_savedLastOrder.push(getElemDef().getLastOrder());
        getElemDef().setLastOrder(-1);
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        if (this.m_savedLastOrder != null && !this.m_savedLastOrder.empty()) {
            getElemDef().setLastOrder(this.m_savedLastOrder.pop());
        }
        if (!getElemDef().getRequiredFound()) {
            handler.error(XSLTErrorResources.ER_REQUIRED_ELEM_NOT_FOUND, new Object[]{getElemDef().getRequiredElem()}, null);
        }
    }

    public void characters(StylesheetHandler handler, char[] ch, int start, int length) throws SAXException {
        handler.error(XSLTErrorResources.ER_CHARS_NOT_ALLOWED, null, null);
    }

    public void ignorableWhitespace(StylesheetHandler handler, char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(StylesheetHandler handler, String target, String data) throws SAXException {
    }

    public void skippedEntity(StylesheetHandler handler, String name) throws SAXException {
    }

    /* access modifiers changed from: package-private */
    public void setPropertiesFromAttributes(StylesheetHandler handler, String rawName, Attributes attributes, ElemTemplateElement target) throws SAXException {
        setPropertiesFromAttributes(handler, rawName, attributes, target, true);
    }

    /* access modifiers changed from: package-private */
    public Attributes setPropertiesFromAttributes(StylesheetHandler handler, String rawName, Attributes attributes, ElemTemplateElement target, boolean throwError) throws SAXException {
        String attrUri;
        int nAttrs;
        boolean isCompatibleMode;
        int i;
        List errorDefs;
        List processedDefs;
        List processedDefs2;
        StylesheetHandler stylesheetHandler = handler;
        Attributes attributes2 = attributes;
        XSLTElementDef def = getElemDef();
        AttributesImpl undefines = null;
        boolean isCompatibleMode2 = (handler.getStylesheet() != null && handler.getStylesheet().getCompatibleMode()) || !throwError;
        if (isCompatibleMode2) {
            undefines = new AttributesImpl();
        }
        AttributesImpl undefines2 = undefines;
        List processedDefs3 = new ArrayList();
        List errorDefs2 = new ArrayList();
        int nAttrs2 = attributes.getLength();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= nAttrs2) {
                break;
            }
            String attrUri2 = attributes2.getURI(i3);
            if (attrUri2 == null || attrUri2.length() != 0 || (!attributes2.getQName(i3).startsWith(Constants.ATTRNAME_XMLNS) && !attributes2.getQName(i3).equals("xmlns"))) {
                attrUri = attrUri2;
            } else {
                attrUri = "http://www.w3.org/XML/1998/namespace";
            }
            String attrLocalName = attributes2.getLocalName(i3);
            XSLTAttributeDef attrDef = def.getAttributeDef(attrUri, attrLocalName);
            if (attrDef != null) {
                processedDefs2 = processedDefs3;
                XSLTAttributeDef attrDef2 = attrDef;
                String attrLocalName2 = attrLocalName;
                nAttrs = nAttrs2;
                errorDefs = errorDefs2;
                isCompatibleMode = isCompatibleMode2;
                i = i3;
                if (handler.getStylesheetProcessor() == null) {
                    System.out.println("stylesheet processor null");
                }
                if (attrDef2.getName().compareTo("*") != 0 || !handler.getStylesheetProcessor().isSecureProcessing()) {
                    String qName = attributes2.getQName(i);
                    String value = attributes2.getValue(i);
                    processedDefs = processedDefs2;
                    if (attrDef2.setAttrValue(stylesheetHandler, attrUri, attrLocalName2, qName, value, target)) {
                        processedDefs.add(attrDef2);
                    } else {
                        errorDefs.add(attrDef2);
                    }
                    i2 = i + 1;
                    processedDefs3 = processedDefs;
                    errorDefs2 = errorDefs;
                    isCompatibleMode2 = isCompatibleMode;
                    nAttrs2 = nAttrs;
                    attributes2 = attributes;
                } else {
                    stylesheetHandler.error(XSLTErrorResources.ER_ATTR_NOT_ALLOWED, new Object[]{attributes2.getQName(i), rawName}, null);
                }
            } else if (!isCompatibleMode2) {
                stylesheetHandler.error(XSLTErrorResources.ER_ATTR_NOT_ALLOWED, new Object[]{attributes2.getQName(i3), rawName}, null);
                nAttrs = nAttrs2;
                errorDefs = errorDefs2;
                isCompatibleMode = isCompatibleMode2;
                processedDefs = processedDefs3;
                i = i3;
                i2 = i + 1;
                processedDefs3 = processedDefs;
                errorDefs2 = errorDefs;
                isCompatibleMode2 = isCompatibleMode;
                nAttrs2 = nAttrs;
                attributes2 = attributes;
            } else {
                processedDefs2 = processedDefs3;
                XSLTAttributeDef xSLTAttributeDef = attrDef;
                String str = attrLocalName;
                isCompatibleMode = isCompatibleMode2;
                i = i3;
                nAttrs = nAttrs2;
                errorDefs = errorDefs2;
                undefines2.addAttribute(attrUri, attrLocalName, attributes2.getQName(i3), attributes2.getType(i3), attributes2.getValue(i3));
            }
            processedDefs = processedDefs2;
            i2 = i + 1;
            processedDefs3 = processedDefs;
            errorDefs2 = errorDefs;
            isCompatibleMode2 = isCompatibleMode;
            nAttrs2 = nAttrs;
            attributes2 = attributes;
        }
        List processedDefs4 = processedDefs3;
        int i4 = nAttrs2;
        List errorDefs3 = errorDefs2;
        boolean z = isCompatibleMode2;
        for (XSLTAttributeDef attrDef3 : def.getAttributes()) {
            if (attrDef3.getDefault() == null || processedDefs4.contains(attrDef3)) {
                ElemTemplateElement elemTemplateElement = target;
            } else {
                attrDef3.setDefAttrValue(stylesheetHandler, target);
            }
            if (attrDef3.getRequired() && !processedDefs4.contains(attrDef3) && !errorDefs3.contains(attrDef3)) {
                stylesheetHandler.error(XSLMessages.createMessage(XSLTErrorResources.ER_REQUIRES_ATTRIB, new Object[]{rawName, attrDef3.getName()}), null);
            }
        }
        ElemTemplateElement elemTemplateElement2 = target;
        return undefines2;
    }
}
