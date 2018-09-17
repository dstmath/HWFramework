package org.apache.xpath.objects;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.axes.NodeSequence;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XNodeSet extends NodeSequence {
    static final EqualComparator S_EQ = null;
    static final GreaterThanComparator S_GT = null;
    static final GreaterThanOrEqualComparator S_GTE = null;
    static final LessThanComparator S_LT = null;
    static final LessThanOrEqualComparator S_LTE = null;
    static final NotEqualComparator S_NEQ = null;
    static final long serialVersionUID = 1916026368035639667L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xpath.objects.XNodeSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xpath.objects.XNodeSet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.objects.XNodeSet.<clinit>():void");
    }

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
        return node != -1 ? getStringFromNode(node).toString() : SerializerConstants.EMPTYSTRING;
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
                                break;
                            }
                            if (vector == null) {
                                vector = new Vector();
                            }
                            vector.addElement(s2);
                        }
                        result = true;
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
            return comparator.compareNumbers(bool() ? Constants.XSLTVERSUPPORTED : 0.0d, obj2.num());
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
