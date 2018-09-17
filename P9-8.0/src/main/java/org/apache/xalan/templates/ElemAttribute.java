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

    protected String resolvePrefix(SerializationHandler rhandler, String prefix, String nodeNamespace) throws TransformerException {
        if (prefix == null) {
            return prefix;
        }
        if (prefix.length() != 0 && !prefix.equals("xmlns")) {
            return prefix;
        }
        prefix = rhandler.getPrefix(nodeNamespace);
        if (prefix != null && prefix.length() != 0 && !prefix.equals("xmlns")) {
            return prefix;
        }
        if (nodeNamespace.length() > 0) {
            return rhandler.getNamespaceMappings().generateNextPrefix();
        }
        return "";
    }

    protected boolean validateNodeName(String nodeName) {
        if (nodeName == null || nodeName.equals("xmlns")) {
            return false;
        }
        return XML11Char.isXML11ValidQName(nodeName);
    }

    void constructNode(String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer) throws TransformerException {
        if (nodeName != null && nodeName.length() > 0) {
            SerializationHandler rhandler = transformer.getSerializationHandler();
            String val = transformer.transformToString(this);
            try {
                String localName = QName.getLocalPart(nodeName);
                if (prefix == null || prefix.length() <= 0) {
                    rhandler.addAttribute("", localName, nodeName, "CDATA", val, true);
                    return;
                }
                rhandler.addAttribute(nodeNamespace, localName, nodeName, "CDATA", val, true);
            } catch (SAXException e) {
            }
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        switch (newChild.getXSLToken()) {
            case 9:
            case 17:
            case 28:
            case 30:
            case 35:
            case 36:
            case 37:
            case 42:
            case 50:
            case Constants.ELEMNAME_APPLY_IMPORTS /*72*/:
            case Constants.ELEMNAME_VARIABLE /*73*/:
            case Constants.ELEMNAME_COPY_OF /*74*/:
            case Constants.ELEMNAME_MESSAGE /*75*/:
            case Constants.ELEMNAME_TEXTLITERALRESULT /*78*/:
                break;
            default:
                error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
                break;
        }
        return super.appendChild(newChild);
    }

    public void setName(AVT v) {
        if (v.isSimple() && v.getSimpleString().equals("xmlns")) {
            throw new IllegalArgumentException();
        }
        super.setName(v);
    }
}
