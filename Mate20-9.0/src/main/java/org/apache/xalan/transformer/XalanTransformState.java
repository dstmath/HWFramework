package org.apache.xalan.transformer;

import javax.xml.transform.Transformer;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class XalanTransformState implements TransformState {
    DTMIterator m_contextNodeList = null;
    ElemTemplateElement m_currentElement = null;
    Node m_currentNode = null;
    int m_currentNodeHandle = -1;
    ElemTemplate m_currentTemplate = null;
    boolean m_elemPending = false;
    int m_matchedNode = -1;
    ElemTemplate m_matchedTemplate = null;
    Node m_node = null;
    TransformerImpl m_transformer = null;

    public void setCurrentNode(Node n) {
        this.m_node = n;
    }

    public void resetState(Transformer transformer) {
        if (transformer != null && (transformer instanceof TransformerImpl)) {
            this.m_transformer = (TransformerImpl) transformer;
            this.m_currentElement = this.m_transformer.getCurrentElement();
            this.m_currentTemplate = this.m_transformer.getCurrentTemplate();
            this.m_matchedTemplate = this.m_transformer.getMatchedTemplate();
            int currentNodeHandle = this.m_transformer.getCurrentNode();
            this.m_currentNode = this.m_transformer.getXPathContext().getDTM(currentNodeHandle).getNode(currentNodeHandle);
            this.m_matchedNode = this.m_transformer.getMatchedNode();
            this.m_contextNodeList = this.m_transformer.getContextNodeList();
        }
    }

    public ElemTemplateElement getCurrentElement() {
        if (this.m_elemPending) {
            return this.m_currentElement;
        }
        return this.m_transformer.getCurrentElement();
    }

    public Node getCurrentNode() {
        if (this.m_currentNode != null) {
            return this.m_currentNode;
        }
        return this.m_transformer.getXPathContext().getDTM(this.m_transformer.getCurrentNode()).getNode(this.m_transformer.getCurrentNode());
    }

    public ElemTemplate getCurrentTemplate() {
        if (this.m_elemPending) {
            return this.m_currentTemplate;
        }
        return this.m_transformer.getCurrentTemplate();
    }

    public ElemTemplate getMatchedTemplate() {
        if (this.m_elemPending) {
            return this.m_matchedTemplate;
        }
        return this.m_transformer.getMatchedTemplate();
    }

    public Node getMatchedNode() {
        if (this.m_elemPending) {
            return this.m_transformer.getXPathContext().getDTM(this.m_matchedNode).getNode(this.m_matchedNode);
        }
        return this.m_transformer.getXPathContext().getDTM(this.m_transformer.getMatchedNode()).getNode(this.m_transformer.getMatchedNode());
    }

    public NodeIterator getContextNodeList() {
        if (this.m_elemPending) {
            return new DTMNodeIterator(this.m_contextNodeList);
        }
        return new DTMNodeIterator(this.m_transformer.getContextNodeList());
    }

    public Transformer getTransformer() {
        return this.m_transformer;
    }
}
