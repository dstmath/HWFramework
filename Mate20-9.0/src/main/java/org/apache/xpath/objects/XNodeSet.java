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
        super((Object) new NodeSetDTM(dtmMgr));
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
        if (node != -1) {
            return getNumberFromNode(node);
        }
        return Double.NaN;
    }

    public double numWithSideEffects() {
        int node = nextNode();
        if (node != -1) {
            return getNumberFromNode(node);
        }
        return Double.NaN;
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
            return (NodeSetDTM) this.m_obj;
        }
        NodeSetDTM mnl = new NodeSetDTM(iter());
        setObject(mnl);
        setCurrentPos(0);
        return mnl;
    }

    public boolean compare(XObject obj2, Comparator comparator) throws TransformerException {
        boolean result = false;
        int type = obj2.getType();
        if (4 == type) {
            DTMIterator list1 = iterRaw();
            DTMIterator list2 = ((XNodeSet) obj2).iterRaw();
            Vector node2Strings = null;
            while (true) {
                int nextNode = list1.nextNode();
                int node1 = nextNode;
                if (-1 != nextNode) {
                    XMLString s1 = getStringFromNode(node1);
                    if (node2Strings == null) {
                        while (true) {
                            int nextNode2 = list2.nextNode();
                            int node2 = nextNode2;
                            if (-1 == nextNode2) {
                                break;
                            }
                            XMLString s2 = getStringFromNode(node2);
                            if (comparator.compareStrings(s1, s2)) {
                                result = true;
                                break;
                            }
                            if (node2Strings == null) {
                                node2Strings = new Vector();
                            }
                            node2Strings.addElement(s2);
                        }
                    } else {
                        int n = node2Strings.size();
                        int i = 0;
                        while (true) {
                            if (i >= n) {
                                break;
                            } else if (comparator.compareStrings(s1, (XMLString) node2Strings.elementAt(i))) {
                                result = true;
                                break;
                            } else {
                                i++;
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
            DTMIterator list12 = iterRaw();
            double num2 = obj2.num();
            while (true) {
                int nextNode3 = list12.nextNode();
                int node = nextNode3;
                if (-1 != nextNode3) {
                    if (comparator.compareNumbers(getNumberFromNode(node), num2)) {
                        result = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            list12.reset();
            return result;
        } else if (5 == type) {
            XMLString s22 = obj2.xstr();
            DTMIterator list13 = iterRaw();
            while (true) {
                int nextNode4 = list13.nextNode();
                int node3 = nextNode4;
                if (-1 != nextNode4) {
                    if (comparator.compareStrings(getStringFromNode(node3), s22)) {
                        result = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            list13.reset();
            return result;
        } else if (3 != type) {
            return comparator.compareNumbers(num(), obj2.num());
        } else {
            XMLString s23 = obj2.xstr();
            DTMIterator list14 = iterRaw();
            while (true) {
                int nextNode5 = list14.nextNode();
                int node4 = nextNode5;
                if (-1 != nextNode5) {
                    if (comparator.compareStrings(getStringFromNode(node4), s23)) {
                        result = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            list14.reset();
            return result;
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
