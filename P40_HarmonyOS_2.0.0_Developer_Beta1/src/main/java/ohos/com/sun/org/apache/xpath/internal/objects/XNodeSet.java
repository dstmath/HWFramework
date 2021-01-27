package ohos.com.sun.org.apache.xpath.internal.objects;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.axes.NodeSequence;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeIterator;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public class XNodeSet extends NodeSequence {
    static final EqualComparator S_EQ = new EqualComparator();
    static final GreaterThanComparator S_GT = new GreaterThanComparator();
    static final GreaterThanOrEqualComparator S_GTE = new GreaterThanOrEqualComparator();
    static final LessThanComparator S_LT = new LessThanComparator();
    static final LessThanOrEqualComparator S_LTE = new LessThanOrEqualComparator();
    static final NotEqualComparator S_NEQ = new NotEqualComparator();
    static final long serialVersionUID = 1916026368035639667L;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 4;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#NODESET";
    }

    public DTMIterator iterRaw() {
        return this;
    }

    public void release(DTMIterator dTMIterator) {
    }

    protected XNodeSet() {
    }

    public XNodeSet(DTMIterator dTMIterator) {
        if (dTMIterator instanceof XNodeSet) {
            XNodeSet xNodeSet = (XNodeSet) dTMIterator;
            setIter(xNodeSet.m_iter);
            this.m_dtmMgr = xNodeSet.m_dtmMgr;
            this.m_last = xNodeSet.m_last;
            if (!xNodeSet.hasCache()) {
                xNodeSet.setShouldCacheNodes(true);
            }
            setObject(xNodeSet.getIteratorCache());
            return;
        }
        setIter(dTMIterator);
    }

    public XNodeSet(XNodeSet xNodeSet) {
        setIter(xNodeSet.m_iter);
        this.m_dtmMgr = xNodeSet.m_dtmMgr;
        this.m_last = xNodeSet.m_last;
        if (!xNodeSet.hasCache()) {
            xNodeSet.setShouldCacheNodes(true);
        }
        setObject(xNodeSet.m_obj);
    }

    public XNodeSet(DTMManager dTMManager) {
        this(-1, dTMManager);
    }

    public XNodeSet(int i, DTMManager dTMManager) {
        super(new NodeSetDTM(dTMManager));
        this.m_dtmMgr = dTMManager;
        if (-1 != i) {
            ((NodeSetDTM) this.m_obj).addNode(i);
            this.m_last = 1;
            return;
        }
        this.m_last = 0;
    }

    public double getNumberFromNode(int i) {
        return this.m_dtmMgr.getDTM(i).getStringValue(i).toDouble();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() {
        int item = item(0);
        if (item != -1) {
            return getNumberFromNode(item);
        }
        return Double.NaN;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double numWithSideEffects() {
        int nextNode = nextNode();
        if (nextNode != -1) {
            return getNumberFromNode(nextNode);
        }
        return Double.NaN;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return item(0) != -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean boolWithSideEffects() {
        return nextNode() != -1;
    }

    public XMLString getStringFromNode(int i) {
        if (-1 != i) {
            return this.m_dtmMgr.getDTM(i).getStringValue(i);
        }
        return XString.EMPTYSTRING;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
        int item = item(0);
        if (item != -1) {
            this.m_dtmMgr.getDTM(item).dispatchCharactersEvents(item, contentHandler, false);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public XMLString xstr() {
        int item = item(0);
        return item != -1 ? getStringFromNode(item) : XString.EMPTYSTRING;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void appendToFsb(FastStringBuffer fastStringBuffer) {
        ((XString) xstr()).appendToFsb(fastStringBuffer);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        int item = item(0);
        return item != -1 ? getStringFromNode(item).toString() : "";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        if (this.m_obj == null) {
            return this;
        }
        return this.m_obj;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public NodeIterator nodeset() throws TransformerException {
        return new DTMNodeIterator(iter());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public NodeList nodelist() throws TransformerException {
        DTMNodeList dTMNodeList = new DTMNodeList(this);
        SetVector(((XNodeSet) dTMNodeList.getDTMIterator()).getVector());
        return dTMNodeList;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public DTMIterator iter() {
        try {
            return hasCache() ? cloneWithReset() : this;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public XObject getFresh() {
        try {
            return hasCache() ? (XObject) cloneWithReset() : this;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public NodeSetDTM mutableNodeset() {
        if (this.m_obj instanceof NodeSetDTM) {
            return (NodeSetDTM) this.m_obj;
        }
        NodeSetDTM nodeSetDTM = new NodeSetDTM(iter());
        setObject(nodeSetDTM);
        setCurrentPos(0);
        return nodeSetDTM;
    }

    public boolean compare(XObject xObject, Comparator comparator) throws TransformerException {
        boolean z;
        boolean z2;
        boolean z3;
        int type = xObject.getType();
        if (4 == type) {
            DTMIterator iterRaw = iterRaw();
            DTMIterator iterRaw2 = ((XNodeSet) xObject).iterRaw();
            Vector vector = null;
            boolean z4 = false;
            while (true) {
                int nextNode = iterRaw.nextNode();
                if (-1 != nextNode) {
                    XMLString stringFromNode = getStringFromNode(nextNode);
                    if (vector == null) {
                        while (true) {
                            int nextNode2 = iterRaw2.nextNode();
                            if (-1 == nextNode2) {
                                break;
                            }
                            XMLString stringFromNode2 = getStringFromNode(nextNode2);
                            if (comparator.compareStrings(stringFromNode, stringFromNode2)) {
                                break;
                            }
                            if (vector == null) {
                                vector = new Vector();
                            }
                            vector.addElement(stringFromNode2);
                        }
                    } else {
                        int size = vector.size();
                        int i = 0;
                        while (true) {
                            if (i >= size) {
                                break;
                            } else if (comparator.compareStrings(stringFromNode, (XMLString) vector.elementAt(i))) {
                                break;
                            } else {
                                i++;
                            }
                        }
                    }
                    z4 = true;
                } else {
                    iterRaw.reset();
                    iterRaw2.reset();
                    return z4;
                }
            }
        } else if (1 == type) {
            return comparator.compareNumbers(bool() ? 1.0d : XPath.MATCH_SCORE_QNAME, xObject.num());
        } else if (2 == type) {
            DTMIterator iterRaw3 = iterRaw();
            double num = xObject.num();
            while (true) {
                int nextNode3 = iterRaw3.nextNode();
                if (-1 != nextNode3) {
                    if (comparator.compareNumbers(getNumberFromNode(nextNode3), num)) {
                        z3 = true;
                        break;
                    }
                } else {
                    z3 = false;
                    break;
                }
            }
            iterRaw3.reset();
            return z3;
        } else if (5 == type) {
            XMLString xstr = xObject.xstr();
            DTMIterator iterRaw4 = iterRaw();
            while (true) {
                int nextNode4 = iterRaw4.nextNode();
                if (-1 != nextNode4) {
                    if (comparator.compareStrings(getStringFromNode(nextNode4), xstr)) {
                        z2 = true;
                        break;
                    }
                } else {
                    z2 = false;
                    break;
                }
            }
            iterRaw4.reset();
            return z2;
        } else if (3 != type) {
            return comparator.compareNumbers(num(), xObject.num());
        } else {
            XMLString xstr2 = xObject.xstr();
            DTMIterator iterRaw5 = iterRaw();
            while (true) {
                int nextNode5 = iterRaw5.nextNode();
                if (-1 != nextNode5) {
                    if (comparator.compareStrings(getStringFromNode(nextNode5), xstr2)) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            iterRaw5.reset();
            return z;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean lessThan(XObject xObject) throws TransformerException {
        return compare(xObject, S_LT);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean lessThanOrEqual(XObject xObject) throws TransformerException {
        return compare(xObject, S_LTE);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean greaterThan(XObject xObject) throws TransformerException {
        return compare(xObject, S_GT);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean greaterThanOrEqual(XObject xObject) throws TransformerException {
        return compare(xObject, S_GTE);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        try {
            return compare(xObject, S_EQ);
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean notEquals(XObject xObject) throws TransformerException {
        return compare(xObject, S_NEQ);
    }
}
