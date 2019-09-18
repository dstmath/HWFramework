package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XML11Char;
import org.xml.sax.SAXException;

public class ElemAttribute extends ElemElement {
    static final long serialVersionUID = 8817220961566919187L;

    public int getXSLToken() {
        return 48;
    }

    public String getNodeName() {
        return "attribute";
    }

    /* access modifiers changed from: protected */
    public String resolvePrefix(SerializationHandler rhandler, String prefix, String nodeNamespace) throws TransformerException {
        if (prefix == null) {
            return prefix;
        }
        if (prefix.length() != 0 && !prefix.equals("xmlns")) {
            return prefix;
        }
        String prefix2 = rhandler.getPrefix(nodeNamespace);
        if (prefix2 != null && prefix2.length() != 0 && !prefix2.equals("xmlns")) {
            return prefix2;
        }
        if (nodeNamespace.length() > 0) {
            return rhandler.getNamespaceMappings().generateNextPrefix();
        }
        return "";
    }

    /* access modifiers changed from: protected */
    public boolean validateNodeName(String nodeName) {
        if (nodeName != null && !nodeName.equals("xmlns")) {
            return XML11Char.isXML11ValidQName(nodeName);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void constructNode(String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer) throws TransformerException {
        if (nodeName != null && nodeName.length() > 0) {
            SerializationHandler rhandler = transformer.getSerializationHandler();
            String val = transformer.transformToString(this);
            try {
                String localName = QName.getLocalPart(nodeName);
                if (prefix == null || prefix.length() <= 0) {
                    rhandler.addAttribute("", localName, nodeName, "CDATA", val, true);
                } else {
                    rhandler.addAttribute(nodeNamespace, localName, nodeName, "CDATA", val, true);
                }
            } catch (SAXException e) {
            }
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        int type = newChild.getXSLToken();
        if (!(type == 9 || type == 17 || type == 28 || type == 30 || type == 42 || type == 50 || type == 78)) {
            switch (type) {
                case 35:
                case 36:
                case 37:
                    break;
                default:
                    switch (type) {
                        case Constants.ELEMNAME_APPLY_IMPORTS:
                        case Constants.ELEMNAME_VARIABLE:
                        case Constants.ELEMNAME_COPY_OF:
                        case Constants.ELEMNAME_MESSAGE:
                            break;
                        default:
                            error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
                            break;
                    }
            }
        }
        return super.appendChild(newChild);
    }

    public void setName(AVT v) {
        if (!v.isSimple() || !v.getSimpleString().equals("xmlns")) {
            super.setName(v);
            return;
        }
        throw new IllegalArgumentException();
    }
}
