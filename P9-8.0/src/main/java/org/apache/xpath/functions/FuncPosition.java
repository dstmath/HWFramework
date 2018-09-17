package org.apache.xpath.functions;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncPosition extends Function {
    static final long serialVersionUID = -9092846348197271582L;
    private boolean m_isTopLevel;

    public void postCompileStep(Compiler compiler) {
        this.m_isTopLevel = compiler.getLocationPathDepth() == -1;
    }

    public int getPositionInContextNodeList(XPathContext xctxt) {
        SubContextList iter = this.m_isTopLevel ? null : xctxt.getSubContextList();
        if (iter != null) {
            return iter.getProximityPosition(xctxt);
        }
        DTMIterator cnl = xctxt.getContextNodeList();
        if (cnl == null) {
            return -1;
        }
        if (cnl.getCurrentNode() == -1) {
            if (cnl.getCurrentPos() == 0) {
                return 0;
            }
            try {
                cnl = cnl.cloneWithReset();
                int currentNode = xctxt.getContextNode();
                int n;
                do {
                    n = cnl.nextNode();
                    if (-1 == n) {
                        break;
                    }
                } while (n != currentNode);
            } catch (CloneNotSupportedException cnse) {
                throw new WrappedRuntimeException(cnse);
            }
        }
        return cnl.getCurrentPos();
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return new XNumber((double) getPositionInContextNodeList(xctxt));
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }
}
