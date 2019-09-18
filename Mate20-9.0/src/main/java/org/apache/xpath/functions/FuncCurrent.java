package org.apache.xpath.functions;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.PredicatedNodeTest;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.patterns.StepPattern;

public class FuncCurrent extends Function {
    static final long serialVersionUID = 5715316804877715008L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        SubContextList subContextList = xctxt.getCurrentNodeList();
        int currentNode = -1;
        if (subContextList == null) {
            currentNode = xctxt.getContextNode();
        } else if (subContextList instanceof PredicatedNodeTest) {
            currentNode = ((PredicatedNodeTest) subContextList).getLocPathIterator().getCurrentContextNode();
        } else if (subContextList instanceof StepPattern) {
            throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSOR_ERROR, null));
        }
        return new XNodeSet(currentNode, xctxt.getDTMManager());
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }
}
