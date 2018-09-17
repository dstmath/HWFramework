package org.apache.xalan.processor;

import java.util.ArrayList;
import java.util.List;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.KeyDeclaration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorKey extends XSLTElementProcessor {
    static final long serialVersionUID = 4285205417566822979L;

    ProcessorKey() {
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        KeyDeclaration kd = new KeyDeclaration(handler.getStylesheet(), handler.nextUid());
        kd.setDOMBackPointer(handler.getOriginatingNode());
        kd.setLocaterInfo(handler.getLocator());
        setPropertiesFromAttributes(handler, rawName, attributes, kd);
        handler.getStylesheet().setKey(kd);
    }

    void setPropertiesFromAttributes(StylesheetHandler handler, String rawName, Attributes attributes, ElemTemplateElement target) throws SAXException {
        int i;
        XSLTAttributeDef attrDef;
        XSLTElementDef def = getElemDef();
        List processedDefs = new ArrayList();
        int nAttrs = attributes.getLength();
        for (i = 0; i < nAttrs; i++) {
            String attrUri = attributes.getURI(i);
            String attrLocalName = attributes.getLocalName(i);
            attrDef = def.getAttributeDef(attrUri, attrLocalName);
            if (attrDef == null) {
                handler.error(attributes.getQName(i) + "attribute is not allowed on the " + rawName + " element!", null);
            } else {
                if (attributes.getValue(i).indexOf("key(") >= 0) {
                    handler.error(XSLMessages.createMessage(XSLTErrorResources.ER_INVALID_KEY_CALL, null), null);
                }
                processedDefs.add(attrDef);
                attrDef.setAttrValue(handler, attrUri, attrLocalName, attributes.getQName(i), attributes.getValue(i), target);
            }
        }
        for (XSLTAttributeDef attrDef2 : def.getAttributes()) {
            if (!(attrDef2.getDefault() == null || processedDefs.contains(attrDef2))) {
                attrDef2.setDefAttrValue(handler, target);
            }
            if (attrDef2.getRequired() && !processedDefs.contains(attrDef2)) {
                handler.error(XSLMessages.createMessage(XSLTErrorResources.ER_REQUIRES_ATTRIB, new Object[]{rawName, attrDef2.getName()}), null);
            }
        }
    }
}
