package org.apache.xalan.templates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.NodeSorter;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.IntStack;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;

public class ElemForEach extends ElemTemplateElement implements ExpressionOwner {
    static final boolean DEBUG = false;
    static final long serialVersionUID = 6018140636363583690L;
    public boolean m_doc_cache_off = false;
    protected Expression m_selectExpression = null;
    protected Vector m_sortElems = null;
    protected XPath m_xpath = null;

    public void setSelect(XPath xpath) {
        this.m_selectExpression = xpath.getExpression();
        this.m_xpath = xpath;
    }

    public Expression getSelect() {
        return this.m_selectExpression;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        int length = getSortElemCount();
        for (int i = 0; i < length; i++) {
            getSortElem(i).compose(sroot);
        }
        Vector vnames = sroot.getComposeState().getVariableNames();
        if (this.m_selectExpression != null) {
            this.m_selectExpression.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
        } else {
            this.m_selectExpression = getStylesheetRoot().m_selectDefault.getExpression();
        }
    }

    public void endCompose(StylesheetRoot sroot) throws TransformerException {
        int length = getSortElemCount();
        for (int i = 0; i < length; i++) {
            getSortElem(i).endCompose(sroot);
        }
        super.endCompose(sroot);
    }

    public int getSortElemCount() {
        return this.m_sortElems == null ? 0 : this.m_sortElems.size();
    }

    public ElemSort getSortElem(int i) {
        return (ElemSort) this.m_sortElems.elementAt(i);
    }

    public void setSortElem(ElemSort sortElem) {
        if (this.m_sortElems == null) {
            this.m_sortElems = new Vector();
        }
        this.m_sortElems.addElement(sortElem);
    }

    public int getXSLToken() {
        return 28;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_FOREACH_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        transformer.pushCurrentTemplateRuleIsNull(true);
        try {
            transformSelectedNodes(transformer);
        } finally {
            transformer.popCurrentTemplateRuleIsNull();
        }
    }

    protected ElemTemplateElement getTemplateMatch() {
        return this;
    }

    public DTMIterator sortNodes(XPathContext xctxt, Vector keys, DTMIterator sourceNodes) throws TransformerException {
        NodeSorter sorter = new NodeSorter(xctxt);
        sourceNodes.setShouldCacheNodes(true);
        sourceNodes.runTo(-1);
        xctxt.pushContextNodeList(sourceNodes);
        try {
            sorter.sort(sourceNodes, keys, xctxt);
            sourceNodes.setCurrentPos(0);
            return sourceNodes;
        } finally {
            xctxt.popContextNodeList();
        }
    }

    public void transformSelectedNodes(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        DTMIterator sourceNodes = this.m_selectExpression.asIterator(xctxt, sourceNode);
        try {
            Vector keys;
            if (this.m_sortElems == null) {
                keys = null;
            } else {
                keys = transformer.processSortKeys(this, sourceNode);
            }
            if (keys != null) {
                sourceNodes = sortNodes(xctxt, keys, sourceNodes);
            }
            xctxt.pushCurrentNode(-1);
            IntStack currentNodes = xctxt.getCurrentNodeStack();
            xctxt.pushCurrentExpressionNode(-1);
            IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();
            xctxt.pushSAXLocatorNull();
            xctxt.pushContextNodeList(sourceNodes);
            transformer.pushElemTemplateElement(null);
            DTM dtm = xctxt.getDTM(sourceNode);
            int docID = sourceNode & DTMManager.IDENT_DTM_DEFAULT;
            while (true) {
                int child = sourceNodes.nextNode();
                if (-1 == child) {
                    break;
                }
                currentNodes.setTop(child);
                currentExpressionNodes.setTop(child);
                if ((DTMManager.IDENT_DTM_DEFAULT & child) != docID) {
                    dtm = xctxt.getDTM(child);
                    docID = child & DTMManager.IDENT_DTM_DEFAULT;
                }
                int nodeType = dtm.getNodeType(child);
                for (ElemTemplateElement t = this.m_firstChild; t != null; t = t.m_nextSibling) {
                    xctxt.setSAXLocator(t);
                    transformer.setCurrentElement(t);
                    t.execute(transformer);
                }
                if (this.m_doc_cache_off) {
                    xctxt.getSourceTreeManager().removeDocumentFromCache(dtm.getDocument());
                    xctxt.release(dtm, false);
                }
            }
        } finally {
            xctxt.popSAXLocator();
            xctxt.popContextNodeList();
            transformer.popElemTemplateElement();
            xctxt.popCurrentExpressionNode();
            xctxt.popCurrentNode();
            sourceNodes.detach();
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        if (64 != newChild.getXSLToken()) {
            return super.appendChild(newChild);
        }
        setSortElem((ElemSort) newChild);
        return newChild;
    }

    public void callChildVisitors(XSLTVisitor visitor, boolean callAttributes) {
        if (callAttributes && this.m_selectExpression != null) {
            this.m_selectExpression.callVisitors(this, visitor);
        }
        int length = getSortElemCount();
        for (int i = 0; i < length; i++) {
            getSortElem(i).callVisitors(visitor);
        }
        super.callChildVisitors(visitor, callAttributes);
    }

    public Expression getExpression() {
        return this.m_selectExpression;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_selectExpression = exp;
    }

    private void readObject(ObjectInputStream os) throws IOException, ClassNotFoundException {
        os.defaultReadObject();
        this.m_xpath = null;
    }
}
