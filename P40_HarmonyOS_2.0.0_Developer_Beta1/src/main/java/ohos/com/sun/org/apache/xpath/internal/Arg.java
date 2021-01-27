package ohos.com.sun.org.apache.xpath.internal;

import java.util.Objects;
import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;

public class Arg {
    private String m_expression;
    private boolean m_isFromWithParam;
    private boolean m_isVisible;
    private QName m_qname;
    private XObject m_val;

    public final QName getQName() {
        return this.m_qname;
    }

    public final void setQName(QName qName) {
        this.m_qname = qName;
    }

    public final XObject getVal() {
        return this.m_val;
    }

    public final void setVal(XObject xObject) {
        this.m_val = xObject;
    }

    public void detach() {
        XObject xObject = this.m_val;
        if (xObject != null) {
            xObject.allowDetachToRelease(true);
            this.m_val.detach();
        }
    }

    public String getExpression() {
        return this.m_expression;
    }

    public void setExpression(String str) {
        this.m_expression = str;
    }

    public boolean isFromWithParam() {
        return this.m_isFromWithParam;
    }

    public boolean isVisible() {
        return this.m_isVisible;
    }

    public void setIsVisible(boolean z) {
        this.m_isVisible = z;
    }

    public Arg() {
        this.m_qname = new QName("");
        this.m_val = null;
        this.m_expression = null;
        this.m_isVisible = true;
        this.m_isFromWithParam = false;
    }

    public Arg(QName qName, String str, boolean z) {
        this.m_qname = qName;
        this.m_val = null;
        this.m_expression = str;
        this.m_isFromWithParam = z;
        this.m_isVisible = !z;
    }

    public Arg(QName qName, XObject xObject) {
        this.m_qname = qName;
        this.m_val = xObject;
        this.m_isVisible = true;
        this.m_isFromWithParam = false;
        this.m_expression = null;
    }

    public int hashCode() {
        return Objects.hashCode(this.m_qname);
    }

    public boolean equals(Object obj) {
        if (obj instanceof QName) {
            return this.m_qname.equals(obj);
        }
        return super.equals(obj);
    }

    public Arg(QName qName, XObject xObject, boolean z) {
        this.m_qname = qName;
        this.m_val = xObject;
        this.m_isFromWithParam = z;
        this.m_isVisible = !z;
        this.m_expression = null;
    }
}
