package org.apache.xpath.objects;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPath;
import org.apache.xpath.axes.NodeSequence;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XNodeSet extends NodeSequence {
    static final EqualComparator S_EQ = new EqualComparator();
    static final GreaterThanComparator S_GT = new GreaterThanComparator();
    static final GreaterThanOrEqualComparator S_GTE = new GreaterThanOrEqualComparator();
    static final LessThanComparator S_LT = new LessThanComparator();
    static final LessThanOrEqualComparator S_LTE = new LessThanOrEqualComparator();
    static final NotEqualComparator S_NEQ = new NotEqualComparator();
    static final long serialVersionUID = 1916026368035639667L;

    protected XNodeSet() {
    }

    public XNodeSet(DTMIterator val) {
        if (val instanceof XNodeSet) {
            XNodeSet nodeSet = (XNodeSet) val;
            setIter(nodeSet.m_iter);
            this.m_dtmMgr = nodeSet.m_dtmMgr;
            this.m_last = nodeSet.m_last;
            if (!nodeSet.hasCache()) {
                nodeSet.setShouldCacheNodes(true);
            }
            setObject(nodeSet.getIteratorCache());
            return;
        }
        setIter(val);
    }

    public XNodeSet(XNodeSet val) {
        setIter(val.m_iter);
        this.m_dtmMgr = val.m_dtmMgr;
        this.m_last = val.m_last;
        if (!val.hasCache()) {
            val.setShouldCacheNodes(true);
        }
        setObject(val.m_obj);
    }

    public XNodeSet(DTMManager dtmMgr) {
        this(-1, dtmMgr);
    }

    public XNodeSet(int n, DTMManager dtmMgr) {
        super(new NodeSetDTM(dtmMgr));
        this.m_dtmMgr = dtmMgr;
        if (-1 != n) {
            ((NodeSetDTM) this.m_obj).addNode(n);
            this.m_last = 1;
            return;
        }
        this.m_last = 0;
    }

    public int getType() {
        return 4;
    }

    public String getTypeString() {
        return "#NODESET";
    }

    public double getNumberFromNode(int n) {
        return this.m_dtmMgr.getDTM(n).getStringValue(n).toDouble();
    }

    public double num() {
        int node = item(0);
        return node != -1 ? getNumberFromNode(node) : Double.NaN;
    }

    public double numWithSideEffects() {
        int node = nextNode();
        return node != -1 ? getNumberFromNode(node) : Double.NaN;
    }

    public boolean bool() {
        return item(0) != -1;
    }

    public boolean boolWithSideEffects() {
        return nextNode() != -1;
    }

    public XMLString getStringFromNode(int n) {
        if (-1 != n) {
            return this.m_dtmMgr.getDTM(n).getStringValue(n);
        }
        return XString.EMPTYSTRING;
    }

    public void dispatchCharactersEvents(ContentHandler ch) throws SAXException {
        int node = item(0);
        if (node != -1) {
            this.m_dtmMgr.getDTM(node).dispatchCharactersEvents(node, ch, false);
        }
    }

    public XMLString xstr() {
        int node = item(0);
        return node != -1 ? getStringFromNode(node) : XString.EMPTYSTRING;
    }

    public void appendToFsb(FastStringBuffer fsb) {
        ((XString) xstr()).appendToFsb(fsb);
    }

    public String str() {
        int node = item(0);
        return node != -1 ? getStringFromNode(node).toString() : "";
    }

    public Object object() {
        if (this.m_obj == null) {
            return this;
        }
        return this.m_obj;
    }

    public NodeIterator nodeset() throws TransformerException {
        return new DTMNodeIterator(iter());
    }

    public NodeList nodelist() throws TransformerException {
        DTMNodeList nodelist = new DTMNodeList(this);
        SetVector(((XNodeSet) nodelist.getDTMIterator()).getVector());
        return nodelist;
    }

    public DTMIterator iterRaw() {
        return this;
    }

    public void release(DTMIterator iter) {
    }

    public DTMIterator iter() {
        try {
            if (hasCache()) {
                return cloneWithReset();
            }
            return this;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse.getMessage());
        }
    }

    public XObject getFresh() {
        try {
            if (hasCache()) {
                return (XObject) cloneWithReset();
            }
            return this;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse.getMessage());
        }
    }

    public NodeSetDTM mutableNodeset() {
        if (this.m_obj instanceof NodeSetDTM) {
            return this.m_obj;
        }
        NodeSetDTM mnl = new NodeSetDTM(iter());
        setObject(mnl);
        setCurrentPos(0);
        return mnl;
    }

    public boolean compare(XObject obj2, Comparator comparator) throws TransformerException {
        boolean result = false;
        int type = obj2.getType();
        DTMIterator list1;
        XMLString s2;
        int node;
        if (4 == type) {
            list1 = iterRaw();
            DTMIterator list2 = ((XNodeSet) obj2).iterRaw();
            Vector vector = null;
            while (true) {
                int node1 = list1.nextNode();
                if (-1 != node1) {
                    XMLString s1 = getStringFromNode(node1);
                    if (vector == null) {
                        while (true) {
                            int node2 = list2.nextNode();
                            if (-1 == node2) {
                                break;
                            }
                            s2 = getStringFromNode(node2);
                            if (comparator.compareStrings(s1, s2)) {
                                result = true;
                                break;
                            }
                            if (vector == null) {
                                vector = new Vector();
                            }
                            vector.addElement(s2);
                        }
                    } else {
                        int n = vector.size();
                        for (int i = 0; i < n; i++) {
                            if (comparator.compareStrings(s1, (XMLString) vector.elementAt(i))) {
                                result = true;
                                break;
                            }
                        }
                    }
                } else {
                    list1.reset();
                    list2.reset();
                    return result;
                }
            }
        } else if (1 == type) {
            return comparator.compareNumbers(bool() ? 1.0d : XPath.MATCH_SCORE_QNAME, obj2.num());
        } else if (2 == type) {
            list1 = iterRaw();
            double num2 = obj2.num();
            do {
                node = list1.nextNode();
                if (-1 == node) {
                    break;
                }
            } while (!comparator.compareNumbers(getNumberFromNode(node), num2));
            result = true;
            list1.reset();
            return result;
        } else if (5 == type) {
            s2 = obj2.xstr();
            list1 = iterRaw();
            do {
                node = list1.nextNode();
                if (-1 == node) {
                    break;
                }
            } while (!comparator.compareStrings(getStringFromNode(node), s2));
            result = true;
            list1.reset();
            return result;
        } else if (3 == type) {
            s2 = obj2.xstr();
            list1 = iterRaw();
            do {
                node = list1.nextNode();
                if (-1 == node) {
                    break;
                }
            } while (!comparator.compareStrings(getStringFromNode(node), s2));
            result = true;
            list1.reset();
            return result;
        } else {
            return comparator.compareNumbers(num(), obj2.num());
        }
    }

    public boolean lessThan(XObject obj2) throws TransformerException {
        return compare(obj2, S_LT);
    }

    public boolean lessThanOrEqual(XObject obj2) throws TransformerException {
        return compare(obj2, S_LTE);
    }

    public boolean greaterThan(XObject obj2) throws TransformerException {
        return compare(obj2, S_GT);
    }

    public boolean greaterThanOrEqual(XObject obj2) throws TransformerException {
        return compare(obj2, S_GTE);
    }

    public boolean equals(XObject obj2) {
        try {
            return compare(obj2, S_EQ);
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }

    public boolean notEquals(XObject obj2) throws TransformerException {
        return compare(obj2, S_NEQ);
    }
}
