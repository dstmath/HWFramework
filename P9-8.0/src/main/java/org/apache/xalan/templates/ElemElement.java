package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class ElemElement extends ElemUse {
    static final long serialVersionUID = -324619535592435183L;
    protected AVT m_name_avt = null;
    protected AVT m_namespace_avt = null;

    public void setName(AVT v) {
        this.m_name_avt = v;
    }

    public AVT getName() {
        return this.m_name_avt;
    }

    public void setNamespace(AVT v) {
        this.m_namespace_avt = v;
    }

    public AVT getNamespace() {
        return this.m_namespace_avt;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_name_avt != null) {
            this.m_name_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_namespace_avt != null) {
            this.m_namespace_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
    }

    public int getXSLToken() {
        return 46;
    }

    public String getNodeName() {
        return "element";
    }

    protected String resolvePrefix(SerializationHandler rhandler, String prefix, String nodeNamespace) throws TransformerException {
        return prefix;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        SerializationHandler rhandler = transformer.getSerializationHandler();
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        String nodeName = this.m_name_avt == null ? null : this.m_name_avt.evaluate(xctxt, sourceNode, this);
        String prefix = null;
        String nodeNamespace = "";
        if (nodeName != null && (this.m_name_avt.isSimple() ^ 1) != 0 && (XML11Char.isXML11ValidQName(nodeName) ^ 1) != 0) {
            transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{"name", nodeName});
            nodeName = null;
        } else if (nodeName != null) {
            prefix = QName.getPrefixPart(nodeName);
            if (this.m_namespace_avt != null) {
                nodeNamespace = this.m_namespace_avt.evaluate(xctxt, sourceNode, this);
                if (nodeNamespace == null || (prefix != null && prefix.length() > 0 && nodeNamespace.length() == 0)) {
                    transformer.getMsgMgr().error(this, XSLTErrorResources.ER_NULL_URI_NAMESPACE);
                } else {
                    prefix = resolvePrefix(rhandler, prefix, nodeNamespace);
                    if (prefix == null) {
                        prefix = "";
                    }
                    nodeName = prefix.length() > 0 ? prefix + ":" + QName.getLocalPart(nodeName) : QName.getLocalPart(nodeName);
                }
            } else {
                try {
                    nodeNamespace = getNamespaceForPrefix(prefix);
                    if (nodeNamespace == null && prefix.length() == 0) {
                        nodeNamespace = "";
                    } else if (nodeNamespace == null) {
                        transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_COULD_NOT_RESOLVE_PREFIX, new Object[]{prefix});
                        nodeName = null;
                    }
                } catch (Exception e) {
                    transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_COULD_NOT_RESOLVE_PREFIX, new Object[]{prefix});
                    nodeName = null;
                }
            }
        }
        constructNode(nodeName, prefix, nodeNamespace, transformer);
    }

    void constructNode(String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer) throws TransformerException {
        try {
            boolean shouldAddAttrs;
            SerializationHandler rhandler = transformer.getResultTreeHandler();
            if (nodeName == null) {
                shouldAddAttrs = false;
            } else {
                if (prefix != null) {
                    rhandler.startPrefixMapping(prefix, nodeNamespace, true);
                }
                rhandler.startElement(nodeNamespace, QName.getLocalPart(nodeName), nodeName);
                super.execute(transformer);
                shouldAddAttrs = true;
            }
            transformer.executeChildTemplates((ElemTemplateElement) this, shouldAddAttrs);
            if (nodeName != null) {
                rhandler.endElement(nodeNamespace, QName.getLocalPart(nodeName), nodeName);
                if (prefix != null) {
                    rhandler.endPrefixMapping(prefix);
                }
            }
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs) {
            if (this.m_name_avt != null) {
                this.m_name_avt.callVisitors(visitor);
            }
            if (this.m_namespace_avt != null) {
                this.m_namespace_avt.callVisitors(visitor);
            }
        }
        super.callChildVisitors(visitor, callAttrs);
    }
}
