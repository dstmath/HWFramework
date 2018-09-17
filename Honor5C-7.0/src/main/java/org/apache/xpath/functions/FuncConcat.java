package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHMessages;

public class FuncConcat extends FunctionMultiArgs {
    static final long serialVersionUID = 1737228885202314413L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        StringBuffer sb = new StringBuffer();
        sb.append(this.m_arg0.execute(xctxt).str());
        sb.append(this.m_arg1.execute(xctxt).str());
        if (this.m_arg2 != null) {
            sb.append(this.m_arg2.execute(xctxt).str());
        }
        if (this.m_args != null) {
            for (Expression execute : this.m_args) {
                sb.append(execute.execute(xctxt).str());
            }
        }
        return new XString(sb.toString());
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum < 2) {
            reportWrongNumberArgs();
        }
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XPATHMessages.createXPATHMessage("gtone", null));
    }
}
