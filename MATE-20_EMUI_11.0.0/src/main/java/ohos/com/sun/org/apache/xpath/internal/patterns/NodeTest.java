package ohos.com.sun.org.apache.xpath.internal.patterns;

import java.io.PrintStream;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class NodeTest extends Expression {
    public static final XNumber SCORE_NODETEST = new XNumber(-0.5d);
    public static final XNumber SCORE_NONE = new XNumber(Double.NEGATIVE_INFINITY);
    public static final XNumber SCORE_NSWILD = new XNumber(-0.25d);
    public static final XNumber SCORE_OTHER = new XNumber(0.5d);
    public static final XNumber SCORE_QNAME = new XNumber((double) XPath.MATCH_SCORE_QNAME);
    public static final int SHOW_BYFUNCTION = 65536;
    public static final String SUPPORTS_PRE_STRIPPING = "http://xml.apache.org/xpath/features/whitespace-pre-stripping";
    public static final String WILD = "*";
    static final long serialVersionUID = -5736721866747906182L;
    private boolean m_isTotallyWild;
    protected String m_name;
    String m_namespace;
    XNumber m_score;
    protected int m_whatToShow;

    public static int getNodeTypeTest(int i) {
        if ((i & 1) != 0) {
            return 1;
        }
        if ((i & 2) != 0) {
            return 2;
        }
        if ((i & 4) != 0) {
            return 3;
        }
        if ((i & 256) != 0) {
            return 9;
        }
        if ((i & 1024) != 0) {
            return 11;
        }
        if ((i & 4096) != 0) {
            return 13;
        }
        if ((i & 128) != 0) {
            return 8;
        }
        if ((i & 64) != 0) {
            return 7;
        }
        if ((i & 512) != 0) {
            return 10;
        }
        if ((i & 32) != 0) {
            return 6;
        }
        if ((i & 16) != 0) {
            return 5;
        }
        if ((i & 2048) != 0) {
            return 12;
        }
        return (i & 8) != 0 ? 4 : 0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getWhatToShow() {
        return this.m_whatToShow;
    }

    public void setWhatToShow(int i) {
        this.m_whatToShow = i;
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public void setNamespace(String str) {
        this.m_namespace = str;
    }

    public String getLocalName() {
        String str = this.m_name;
        return str == null ? "" : str;
    }

    public void setLocalName(String str) {
        this.m_name = str;
    }

    public NodeTest(int i, String str, String str2) {
        initNodeTest(i, str, str2);
    }

    public NodeTest(int i) {
        initNodeTest(i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!isSameClass(expression)) {
            return false;
        }
        NodeTest nodeTest = (NodeTest) expression;
        String str = nodeTest.m_name;
        if (str != null) {
            String str2 = this.m_name;
            if (str2 == null || !str.equals(str2)) {
                return false;
            }
        } else if (this.m_name != null) {
            return false;
        }
        String str3 = nodeTest.m_namespace;
        if (str3 != null) {
            String str4 = this.m_namespace;
            if (str4 == null || !str3.equals(str4)) {
                return false;
            }
        } else if (this.m_namespace != null) {
            return false;
        }
        if (this.m_whatToShow == nodeTest.m_whatToShow && this.m_isTotallyWild == nodeTest.m_isTotallyWild) {
            return true;
        }
        return false;
    }

    public NodeTest() {
    }

    public void initNodeTest(int i) {
        this.m_whatToShow = i;
        calcScore();
    }

    public void initNodeTest(int i, String str, String str2) {
        this.m_whatToShow = i;
        this.m_namespace = str;
        this.m_name = str2;
        calcScore();
    }

    public XNumber getStaticScore() {
        return this.m_score;
    }

    public void setStaticScore(XNumber xNumber) {
        this.m_score = xNumber;
    }

    /* access modifiers changed from: protected */
    public void calcScore() {
        if (this.m_namespace == null && this.m_name == null) {
            this.m_score = SCORE_NODETEST;
        } else {
            String str = this.m_namespace;
            if ((str == "*" || str == null) && this.m_name == "*") {
                this.m_score = SCORE_NODETEST;
            } else if (this.m_namespace == "*" || this.m_name != "*") {
                this.m_score = SCORE_QNAME;
            } else {
                this.m_score = SCORE_NSWILD;
            }
        }
        this.m_isTotallyWild = this.m_namespace == null && this.m_name == "*";
    }

    public double getDefaultScore() {
        return this.m_score.num();
    }

    public static void debugWhatToShow(int i) {
        Vector vector = new Vector();
        if ((i & 2) != 0) {
            vector.addElement("SHOW_ATTRIBUTE");
        }
        if ((i & 4096) != 0) {
            vector.addElement("SHOW_NAMESPACE");
        }
        if ((i & 8) != 0) {
            vector.addElement("SHOW_CDATA_SECTION");
        }
        if ((i & 128) != 0) {
            vector.addElement("SHOW_COMMENT");
        }
        if ((i & 256) != 0) {
            vector.addElement("SHOW_DOCUMENT");
        }
        if ((i & 1024) != 0) {
            vector.addElement("SHOW_DOCUMENT_FRAGMENT");
        }
        if ((i & 512) != 0) {
            vector.addElement("SHOW_DOCUMENT_TYPE");
        }
        if ((i & 1) != 0) {
            vector.addElement("SHOW_ELEMENT");
        }
        if ((i & 32) != 0) {
            vector.addElement("SHOW_ENTITY");
        }
        if ((i & 16) != 0) {
            vector.addElement("SHOW_ENTITY_REFERENCE");
        }
        if ((i & 2048) != 0) {
            vector.addElement("SHOW_NOTATION");
        }
        if ((i & 64) != 0) {
            vector.addElement("SHOW_PROCESSING_INSTRUCTION");
        }
        if ((i & 4) != 0) {
            vector.addElement("SHOW_TEXT");
        }
        int size = vector.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (i2 > 0) {
                System.out.print(" | ");
            }
            System.out.print(vector.elementAt(i2));
        }
        if (size == 0) {
            PrintStream printStream = System.out;
            printStream.print("empty whatToShow: " + i);
        }
        System.out.println();
    }

    private static final boolean subPartMatch(String str, String str2) {
        return str == str2 || (str != null && (str2 == "*" || str.equals(str2)));
    }

    private static final boolean subPartMatchNS(String str, String str2) {
        return str == str2 || (str != null && (str.length() <= 0 ? str2 == null : !(str2 != "*" && !str.equals(str2))));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i) throws TransformerException {
        DTM dtm = xPathContext.getDTM(i);
        short nodeType = dtm.getNodeType(i);
        int i2 = this.m_whatToShow;
        if (i2 == -1) {
            return this.m_score;
        }
        int i3 = (1 << (nodeType - 1)) & i2;
        if (i3 == 1 || i3 == 2) {
            return (this.m_isTotallyWild || (subPartMatchNS(dtm.getNamespaceURI(i), this.m_namespace) && subPartMatch(dtm.getLocalName(i), this.m_name))) ? this.m_score : SCORE_NONE;
        }
        if (i3 == 4 || i3 == 8) {
            return this.m_score;
        }
        if (i3 == 64) {
            return subPartMatch(dtm.getNodeName(i), this.m_name) ? this.m_score : SCORE_NONE;
        }
        if (i3 == 128) {
            return this.m_score;
        }
        if (i3 == 256 || i3 == 1024) {
            return SCORE_OTHER;
        }
        if (i3 != 4096) {
            return SCORE_NONE;
        }
        return subPartMatch(dtm.getLocalName(i), this.m_name) ? this.m_score : SCORE_NONE;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i, DTM dtm, int i2) throws TransformerException {
        int i3 = this.m_whatToShow;
        if (i3 == -1) {
            return this.m_score;
        }
        int nodeType = i3 & (1 << (dtm.getNodeType(i) - 1));
        if (nodeType == 1 || nodeType == 2) {
            return (this.m_isTotallyWild || (subPartMatchNS(dtm.getNamespaceURI(i), this.m_namespace) && subPartMatch(dtm.getLocalName(i), this.m_name))) ? this.m_score : SCORE_NONE;
        }
        if (nodeType == 4 || nodeType == 8) {
            return this.m_score;
        }
        if (nodeType == 64) {
            return subPartMatch(dtm.getNodeName(i), this.m_name) ? this.m_score : SCORE_NONE;
        }
        if (nodeType == 128) {
            return this.m_score;
        }
        if (nodeType == 256 || nodeType == 1024) {
            return SCORE_OTHER;
        }
        if (nodeType != 4096) {
            return SCORE_NONE;
        }
        return subPartMatch(dtm.getLocalName(i), this.m_name) ? this.m_score : SCORE_NONE;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext, xPathContext.getCurrentNode());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        assertion(false, "callVisitors should not be called for this object!!!");
    }
}
