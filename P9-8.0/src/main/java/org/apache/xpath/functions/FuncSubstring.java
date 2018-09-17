package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHMessages;

public class FuncSubstring extends Function3Args {
    static final long serialVersionUID = -5996676095024715502L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XMLString s1 = this.m_arg0.execute(xctxt).xstr();
        double start = this.m_arg1.execute(xctxt).num();
        int lenOfS1 = s1.length();
        if (lenOfS1 <= 0) {
            return XString.EMPTYSTRING;
        }
        int startIndex;
        XMLString substr;
        if (Double.isNaN(start)) {
            start = -1000000.0d;
            startIndex = 0;
        } else {
            start = (double) Math.round(start);
            startIndex = start > XPath.MATCH_SCORE_QNAME ? ((int) start) - 1 : 0;
        }
        if (this.m_arg2 != null) {
            int end = ((int) (((double) Math.round(this.m_arg2.num(xctxt))) + start)) - 1;
            if (end < 0) {
                end = 0;
            } else if (end > lenOfS1) {
                end = lenOfS1;
            }
            if (startIndex > lenOfS1) {
                startIndex = lenOfS1;
            }
            substr = s1.substring(startIndex, end);
        } else {
            if (startIndex > lenOfS1) {
                startIndex = lenOfS1;
            }
            substr = s1.substring(startIndex);
        }
        return (XString) substr;
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum < 2) {
            reportWrongNumberArgs();
        }
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XPATHMessages.createXPATHMessage("ER_TWO_OR_THREE", null));
    }
}
