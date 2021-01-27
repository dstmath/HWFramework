package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.javax.xml.transform.TransformerException;

public class XNumber extends XObject {
    static final long serialVersionUID = -2720400709619020193L;
    double m_val;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 2;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#NUMBER";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean isStableNumber() {
        return true;
    }

    public XNumber(double d) {
        this.m_val = d;
    }

    public XNumber(Number number) {
        this.m_val = number.doubleValue();
        setObject(number);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() {
        return this.m_val;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public double num(XPathContext xPathContext) throws TransformerException {
        return this.m_val;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return !Double.isNaN(this.m_val) && this.m_val != XPath.MATCH_SCORE_QNAME;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        String str;
        if (Double.isNaN(this.m_val)) {
            return "NaN";
        }
        if (Double.isInfinite(this.m_val)) {
            return this.m_val > XPath.MATCH_SCORE_QNAME ? Constants.ATTRVAL_INFINITY : "-Infinity";
        }
        String d = Double.toString(this.m_val);
        int length = d.length();
        int i = length - 2;
        if (d.charAt(i) == '.' && d.charAt(length - 1) == '0') {
            String substring = d.substring(0, i);
            return substring.equals("-0") ? "0" : substring;
        }
        int indexOf = d.indexOf(69);
        if (indexOf < 0) {
            int i2 = length - 1;
            return d.charAt(i2) == '0' ? d.substring(0, i2) : d;
        }
        int parseInt = Integer.parseInt(d.substring(indexOf + 1));
        if (d.charAt(0) == '-') {
            d = d.substring(1);
            indexOf--;
            str = LanguageTag.SEP;
        } else {
            str = "";
        }
        int i3 = indexOf - 2;
        if (parseInt >= i3) {
            return str + d.substring(0, 1) + d.substring(2, indexOf) + zeros(parseInt - i3);
        }
        while (d.charAt(indexOf - 1) == '0') {
            indexOf--;
        }
        if (parseInt > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(d.substring(0, 1));
            int i4 = parseInt + 2;
            sb.append(d.substring(2, i4));
            sb.append(".");
            sb.append(d.substring(i4, indexOf));
            return sb.toString();
        }
        return str + "0." + zeros(-1 - parseInt) + d.substring(0, 1) + d.substring(2, indexOf);
    }

    private static String zeros(int i) {
        if (i < 1) {
            return "";
        }
        char[] cArr = new char[i];
        for (int i2 = 0; i2 < i; i2++) {
            cArr[i2] = '0';
        }
        return new String(cArr);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        if (this.m_obj == null) {
            setObject(new Double(this.m_val));
        }
        return this.m_obj;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        int type = xObject.getType();
        if (type != 4) {
            return type == 1 ? xObject.bool() == bool() : this.m_val == xObject.num();
        }
        try {
            return xObject.equals((XObject) this);
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        xPathVisitor.visitNumberLiteral(expressionOwner, this);
    }
}
