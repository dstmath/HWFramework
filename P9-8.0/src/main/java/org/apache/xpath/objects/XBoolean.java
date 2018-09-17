package org.apache.xpath.objects;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.XPath;
import org.apache.xpath.compiler.Keywords;

public class XBoolean extends XObject {
    public static final XBoolean S_FALSE = new XBooleanStatic(false);
    public static final XBoolean S_TRUE = new XBooleanStatic(true);
    static final long serialVersionUID = -2964933058866100881L;
    private final boolean m_val;

    public XBoolean(boolean b) {
        this.m_val = b;
    }

    public XBoolean(Boolean b) {
        this.m_val = b.booleanValue();
        setObject(b);
    }

    public int getType() {
        return 1;
    }

    public String getTypeString() {
        return "#BOOLEAN";
    }

    public double num() {
        return this.m_val ? 1.0d : XPath.MATCH_SCORE_QNAME;
    }

    public boolean bool() {
        return this.m_val;
    }

    public String str() {
        return this.m_val ? Keywords.FUNC_TRUE_STRING : Keywords.FUNC_FALSE_STRING;
    }

    public Object object() {
        if (this.m_obj == null) {
            setObject(new Boolean(this.m_val));
        }
        return this.m_obj;
    }

    public boolean equals(XObject obj2) {
        if (obj2.getType() == 4) {
            return obj2.equals(this);
        }
        try {
            return this.m_val == obj2.bool();
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }
}
