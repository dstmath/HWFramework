package org.apache.xpath.patterns;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

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

    public int getWhatToShow() {
        return this.m_whatToShow;
    }

    public void setWhatToShow(int what) {
        this.m_whatToShow = what;
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public void setNamespace(String ns) {
        this.m_namespace = ns;
    }

    public String getLocalName() {
        return this.m_name == null ? "" : this.m_name;
    }

    public void setLocalName(String name) {
        this.m_name = name;
    }

    public NodeTest(int whatToShow, String namespace, String name) {
        initNodeTest(whatToShow, namespace, name);
    }

    public NodeTest(int whatToShow) {
        initNodeTest(whatToShow);
    }

    public boolean deepEquals(Expression expr) {
        if (!isSameClass(expr)) {
            return false;
        }
        NodeTest nt = (NodeTest) expr;
        if (nt.m_name != null) {
            if (this.m_name == null || !nt.m_name.equals(this.m_name)) {
                return false;
            }
        } else if (this.m_name != null) {
            return false;
        }
        if (nt.m_namespace != null) {
            if (this.m_namespace == null || !nt.m_namespace.equals(this.m_namespace)) {
                return false;
            }
        } else if (this.m_namespace != null) {
            return false;
        }
        if (this.m_whatToShow == nt.m_whatToShow && this.m_isTotallyWild == nt.m_isTotallyWild) {
            return true;
        }
        return false;
    }

    public void initNodeTest(int whatToShow) {
        this.m_whatToShow = whatToShow;
        calcScore();
    }

    public void initNodeTest(int whatToShow, String namespace, String name) {
        this.m_whatToShow = whatToShow;
        this.m_namespace = namespace;
        this.m_name = name;
        calcScore();
    }

    public XNumber getStaticScore() {
        return this.m_score;
    }

    public void setStaticScore(XNumber score) {
        this.m_score = score;
    }

    protected void calcScore() {
        boolean z = false;
        if (this.m_namespace == null && this.m_name == null) {
            this.m_score = SCORE_NODETEST;
        } else if ((this.m_namespace == "*" || this.m_namespace == null) && this.m_name == "*") {
            this.m_score = SCORE_NODETEST;
        } else if (this.m_namespace == "*" || this.m_name != "*") {
            this.m_score = SCORE_QNAME;
        } else {
            this.m_score = SCORE_NSWILD;
        }
        if (this.m_namespace == null && this.m_name == "*") {
            z = true;
        }
        this.m_isTotallyWild = z;
    }

    public double getDefaultScore() {
        return this.m_score.num();
    }

    public static int getNodeTypeTest(int whatToShow) {
        if ((whatToShow & 1) != 0) {
            return 1;
        }
        if ((whatToShow & 2) != 0) {
            return 2;
        }
        if ((whatToShow & 4) != 0) {
            return 3;
        }
        if ((whatToShow & DTMFilter.SHOW_DOCUMENT) != 0) {
            return 9;
        }
        if ((whatToShow & 1024) != 0) {
            return 11;
        }
        if ((whatToShow & 4096) != 0) {
            return 13;
        }
        if ((whatToShow & 128) != 0) {
            return 8;
        }
        if ((whatToShow & 64) != 0) {
            return 7;
        }
        if ((whatToShow & 512) != 0) {
            return 10;
        }
        if ((whatToShow & 32) != 0) {
            return 6;
        }
        if ((whatToShow & 16) != 0) {
            return 5;
        }
        if ((whatToShow & DTMFilter.SHOW_NOTATION) != 0) {
            return 12;
        }
        if ((whatToShow & 8) != 0) {
            return 4;
        }
        return 0;
    }

    public static void debugWhatToShow(int whatToShow) {
        Vector v = new Vector();
        if ((whatToShow & 2) != 0) {
            v.addElement("SHOW_ATTRIBUTE");
        }
        if ((whatToShow & 4096) != 0) {
            v.addElement("SHOW_NAMESPACE");
        }
        if ((whatToShow & 8) != 0) {
            v.addElement("SHOW_CDATA_SECTION");
        }
        if ((whatToShow & 128) != 0) {
            v.addElement("SHOW_COMMENT");
        }
        if ((whatToShow & DTMFilter.SHOW_DOCUMENT) != 0) {
            v.addElement("SHOW_DOCUMENT");
        }
        if ((whatToShow & 1024) != 0) {
            v.addElement("SHOW_DOCUMENT_FRAGMENT");
        }
        if ((whatToShow & 512) != 0) {
            v.addElement("SHOW_DOCUMENT_TYPE");
        }
        if ((whatToShow & 1) != 0) {
            v.addElement("SHOW_ELEMENT");
        }
        if ((whatToShow & 32) != 0) {
            v.addElement("SHOW_ENTITY");
        }
        if ((whatToShow & 16) != 0) {
            v.addElement("SHOW_ENTITY_REFERENCE");
        }
        if ((whatToShow & DTMFilter.SHOW_NOTATION) != 0) {
            v.addElement("SHOW_NOTATION");
        }
        if ((whatToShow & 64) != 0) {
            v.addElement("SHOW_PROCESSING_INSTRUCTION");
        }
        if ((whatToShow & 4) != 0) {
            v.addElement("SHOW_TEXT");
        }
        int n = v.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                System.out.print(" | ");
            }
            System.out.print(v.elementAt(i));
        }
        if (n == 0) {
            System.out.print("empty whatToShow: " + whatToShow);
        }
        System.out.println();
    }

    private static final boolean subPartMatch(String p, String t) {
        if (p == t) {
            return true;
        }
        if (p != null) {
            return t != "*" ? p.equals(t) : true;
        } else {
            return false;
        }
    }

    private static final boolean subPartMatchNS(String p, String t) {
        if (p == t) {
            return true;
        }
        if (p == null) {
            return false;
        }
        if (p.length() > 0) {
            if (t != "*") {
                return p.equals(t);
            }
            return true;
        } else if (t != null) {
            return false;
        } else {
            return true;
        }
    }

    public XObject execute(XPathContext xctxt, int context) throws TransformerException {
        DTM dtm = xctxt.getDTM(context);
        short nodeType = dtm.getNodeType(context);
        if (this.m_whatToShow == -1) {
            return this.m_score;
        }
        switch (this.m_whatToShow & (1 << (nodeType - 1))) {
            case 1:
            case 2:
                XObject xObject = (this.m_isTotallyWild || (subPartMatchNS(dtm.getNamespaceURI(context), this.m_namespace) && subPartMatch(dtm.getLocalName(context), this.m_name))) ? this.m_score : SCORE_NONE;
                return xObject;
            case 4:
            case 8:
                return this.m_score;
            case 64:
                return subPartMatch(dtm.getNodeName(context), this.m_name) ? this.m_score : SCORE_NONE;
            case 128:
                return this.m_score;
            case DTMFilter.SHOW_DOCUMENT /*256*/:
            case 1024:
                return SCORE_OTHER;
            case 4096:
                return subPartMatch(dtm.getLocalName(context), this.m_name) ? this.m_score : SCORE_NONE;
            default:
                return SCORE_NONE;
        }
    }

    public XObject execute(XPathContext xctxt, int context, DTM dtm, int expType) throws TransformerException {
        if (this.m_whatToShow == -1) {
            return this.m_score;
        }
        switch (this.m_whatToShow & (1 << (dtm.getNodeType(context) - 1))) {
            case 1:
            case 2:
                XObject xObject = (this.m_isTotallyWild || (subPartMatchNS(dtm.getNamespaceURI(context), this.m_namespace) && subPartMatch(dtm.getLocalName(context), this.m_name))) ? this.m_score : SCORE_NONE;
                return xObject;
            case 4:
            case 8:
                return this.m_score;
            case 64:
                return subPartMatch(dtm.getNodeName(context), this.m_name) ? this.m_score : SCORE_NONE;
            case 128:
                return this.m_score;
            case DTMFilter.SHOW_DOCUMENT /*256*/:
            case 1024:
                return SCORE_OTHER;
            case 4096:
                return subPartMatch(dtm.getLocalName(context), this.m_name) ? this.m_score : SCORE_NONE;
            default:
                return SCORE_NONE;
        }
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return execute(xctxt, xctxt.getCurrentNode());
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        assertion(false, "callVisitors should not be called for this object!!!");
    }
}
