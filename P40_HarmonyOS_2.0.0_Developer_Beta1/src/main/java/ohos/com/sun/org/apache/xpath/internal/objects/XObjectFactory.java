package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.OneStepIterator;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeIterator;

public class XObjectFactory {
    public static XObject create(Object obj) {
        if (obj instanceof XObject) {
            return (XObject) obj;
        }
        if (obj instanceof String) {
            return new XString((String) obj);
        }
        if (obj instanceof Boolean) {
            return new XBoolean((Boolean) obj);
        }
        if (obj instanceof Double) {
            return new XNumber((Double) obj);
        }
        return new XObject(obj);
    }

    public static XObject create(Object obj, XPathContext xPathContext) {
        XNodeSetForDOM xNodeSetForDOM;
        XObject xNodeSet;
        if (obj instanceof XObject) {
            xNodeSet = (XObject) obj;
        } else if (obj instanceof String) {
            return new XString((String) obj);
        } else {
            if (obj instanceof Boolean) {
                return new XBoolean((Boolean) obj);
            }
            if (obj instanceof Number) {
                return new XNumber((Number) obj);
            }
            if (obj instanceof DTM) {
                DTM dtm = (DTM) obj;
                try {
                    int document = dtm.getDocument();
                    DTMAxisIterator axisIterator = dtm.getAxisIterator(13);
                    axisIterator.setStartNode(document);
                    OneStepIterator oneStepIterator = new OneStepIterator(axisIterator, 13);
                    oneStepIterator.setRoot(document, xPathContext);
                    xNodeSet = new XNodeSet(oneStepIterator);
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            } else if (obj instanceof DTMAxisIterator) {
                DTMAxisIterator dTMAxisIterator = (DTMAxisIterator) obj;
                try {
                    OneStepIterator oneStepIterator2 = new OneStepIterator(dTMAxisIterator, 13);
                    oneStepIterator2.setRoot(dTMAxisIterator.getStartNode(), xPathContext);
                    xNodeSet = new XNodeSet(oneStepIterator2);
                } catch (Exception e2) {
                    throw new WrappedRuntimeException(e2);
                }
            } else if (obj instanceof DTMIterator) {
                return new XNodeSet((DTMIterator) obj);
            } else {
                if (obj instanceof Node) {
                    xNodeSetForDOM = new XNodeSetForDOM((Node) obj, xPathContext);
                } else if (obj instanceof NodeList) {
                    xNodeSetForDOM = new XNodeSetForDOM((NodeList) obj, xPathContext);
                } else if (!(obj instanceof NodeIterator)) {
                    return new XObject(obj);
                } else {
                    xNodeSetForDOM = new XNodeSetForDOM((NodeIterator) obj, xPathContext);
                }
                return xNodeSetForDOM;
            }
        }
        return xNodeSet;
    }
}
