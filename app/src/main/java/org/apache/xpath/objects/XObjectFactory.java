package org.apache.xpath.objects;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.OneStepIterator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class XObjectFactory {
    public static XObject create(Object val) {
        if (val instanceof XObject) {
            return (XObject) val;
        }
        if (val instanceof String) {
            return new XString((String) val);
        }
        if (val instanceof Boolean) {
            return new XBoolean((Boolean) val);
        }
        if (val instanceof Double) {
            return new XNumber((Double) val);
        }
        return new XObject(val);
    }

    public static XObject create(Object val, XPathContext xctxt) {
        if (val instanceof XObject) {
            return (XObject) val;
        }
        if (val instanceof String) {
            return new XString((String) val);
        }
        if (val instanceof Boolean) {
            return new XBoolean((Boolean) val);
        }
        if (val instanceof Number) {
            return new XNumber((Number) val);
        }
        DTMAxisIterator iter;
        DTMIterator iterator;
        if (val instanceof DTM) {
            DTM dtm = (DTM) val;
            try {
                int dtmRoot = dtm.getDocument();
                iter = dtm.getAxisIterator(13);
                iter.setStartNode(dtmRoot);
                iterator = new OneStepIterator(iter, 13);
                iterator.setRoot(dtmRoot, xctxt);
                return new XNodeSet(iterator);
            } catch (Exception ex) {
                throw new WrappedRuntimeException(ex);
            }
        } else if (val instanceof DTMAxisIterator) {
            iter = (DTMAxisIterator) val;
            try {
                iterator = new OneStepIterator(iter, 13);
                iterator.setRoot(iter.getStartNode(), xctxt);
                return new XNodeSet(iterator);
            } catch (Exception ex2) {
                throw new WrappedRuntimeException(ex2);
            }
        } else if (val instanceof DTMIterator) {
            return new XNodeSet((DTMIterator) val);
        } else {
            if (val instanceof Node) {
                return new XNodeSetForDOM((Node) val, (DTMManager) xctxt);
            }
            if (val instanceof NodeList) {
                return new XNodeSetForDOM((NodeList) val, xctxt);
            }
            if (val instanceof NodeIterator) {
                return new XNodeSetForDOM((NodeIterator) val, xctxt);
            }
            return new XObject(val);
        }
    }
}
