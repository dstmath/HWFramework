package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class XUnresolvedVariableSimple extends XObject {
    static final long serialVersionUID = -1224413807443958985L;

    public XUnresolvedVariableSimple(ElemVariable obj) {
        super(obj);
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XObject xobj = ((ElemVariable) this.m_obj).getSelect().getExpression().execute(xctxt);
        xobj.allowDetachToRelease(false);
        return xobj;
    }

    public int getType() {
        return XObject.CLASS_UNRESOLVEDVARIABLE;
    }

    public String getTypeString() {
        return "XUnresolvedVariableSimple (" + object().getClass().getName() + ")";
    }
}
