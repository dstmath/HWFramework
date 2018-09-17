package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.TreeWalker2Result;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMTreeWalker;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.xml.sax.SAXException;

public class ElemCopyOf extends ElemTemplateElement {
    static final long serialVersionUID = -7433828829497411127L;
    public XPath m_selectExpression = null;

    public void setSelect(XPath expr) {
        this.m_selectExpression = expr;
    }

    public XPath getSelect() {
        return this.m_selectExpression;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        ComposeState cstate = sroot.getComposeState();
        this.m_selectExpression.fixupVariables(cstate.getVariableNames(), cstate.getGlobalsSize());
    }

    public int getXSLToken() {
        return 74;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_COPY_OF_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        try {
            XPathContext xctxt = transformer.getXPathContext();
            XObject value = this.m_selectExpression.execute(xctxt, xctxt.getCurrentNode(), (PrefixResolver) this);
            SerializationHandler handler = transformer.getSerializationHandler();
            if (value != null) {
                String s;
                switch (value.getType()) {
                    case 1:
                    case 2:
                    case 3:
                        s = value.str();
                        handler.characters(s.toCharArray(), 0, s.length());
                        return;
                    case 4:
                        DTMIterator nl = value.iter();
                        DTMTreeWalker tw = new TreeWalker2Result(transformer, handler);
                        while (true) {
                            int pos = nl.nextNode();
                            if (-1 != pos) {
                                DTM dtm = xctxt.getDTMManager().getDTM(pos);
                                short t = dtm.getNodeType(pos);
                                if (t == (short) 9) {
                                    for (int child = dtm.getFirstChild(pos); child != -1; child = dtm.getNextSibling(child)) {
                                        tw.traverse(child);
                                    }
                                } else if (t == (short) 2) {
                                    SerializerUtils.addAttribute(handler, pos);
                                } else {
                                    tw.traverse(pos);
                                }
                            } else {
                                return;
                            }
                        }
                    case 5:
                        SerializerUtils.outputResultTreeFragment(handler, value, transformer.getXPathContext());
                        return;
                    default:
                        s = value.str();
                        handler.characters(s.toCharArray(), 0, s.length());
                        return;
                }
                throw new TransformerException(se);
            }
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        return null;
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs) {
            this.m_selectExpression.getExpression().callVisitors(this.m_selectExpression, visitor);
        }
        super.callChildVisitors(visitor, callAttrs);
    }
}
