package org.apache.xpath;

import org.apache.xml.utils.QName;
import org.apache.xpath.objects.XObject;

public class Arg {
    private String m_expression;
    private boolean m_isFromWithParam;
    private boolean m_isVisible;
    private QName m_qname;
    private XObject m_val;

    public final QName getQName() {
        return this.m_qname;
    }

    public final void setQName(QName name) {
        this.m_qname = name;
    }

    public final XObject getVal() {
        return this.m_val;
    }

    public final void setVal(XObject val) {
        this.m_val = val;
    }

    public void detach() {
        if (this.m_val != null) {
            this.m_val.allowDetachToRelease(true);
            this.m_val.detach();
        }
    }

    public String getExpression() {
        return this.m_expression;
    }

    public void setExpression(String expr) {
        this.m_expression = expr;
    }

    public boolean isFromWithParam() {
        return this.m_isFromWithParam;
    }

    public boolean isVisible() {
        return this.m_isVisible;
    }

    public void setIsVisible(boolean b) {
        this.m_isVisible = b;
    }

    public Arg() {
        this.m_qname = new QName("");
        this.m_val = null;
        this.m_expression = null;
        this.m_isVisible = true;
        this.m_isFromWithParam = false;
    }

    public Arg(QName qname, String expression, boolean isFromWithParam) {
        this.m_qname = qname;
        this.m_val = null;
        this.m_expression = expression;
        this.m_isFromWithParam = isFromWithParam;
        this.m_isVisible = isFromWithParam ^ 1;
    }

    public Arg(QName qname, XObject val) {
        this.m_qname = qname;
        this.m_val = val;
        this.m_isVisible = true;
        this.m_isFromWithParam = false;
        this.m_expression = null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof QName) {
            return this.m_qname.equals(obj);
        }
        return super.equals(obj);
    }

    public Arg(QName qname, XObject val, boolean isFromWithParam) {
        this.m_qname = qname;
        this.m_val = val;
        this.m_isFromWithParam = isFromWithParam;
        this.m_isVisible = isFromWithParam ^ 1;
        this.m_expression = null;
    }
}
