package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncSubstring extends Function3Args {
    static final long serialVersionUID = -5996676095024715502L;

    /* JADX WARNING: Removed duplicated region for block: B:13:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005c  */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int i;
        double d;
        XMLString xMLString;
        XMLString xstr = this.m_arg0.execute(xPathContext).xstr();
        double num = this.m_arg1.execute(xPathContext).num();
        int length = xstr.length();
        if (length <= 0) {
            return XString.EMPTYSTRING;
        }
        if (Double.isNaN(num)) {
            d = -1000000.0d;
        } else {
            d = (double) Math.round(num);
            if (d > XPath.MATCH_SCORE_QNAME) {
                i = ((int) d) - 1;
                if (this.m_arg2 == null) {
                    int round = ((int) (((double) Math.round(this.m_arg2.num(xPathContext))) + d)) - 1;
                    if (round < 0) {
                        round = 0;
                    } else if (round > length) {
                        round = length;
                    }
                    if (i <= length) {
                        length = i;
                    }
                    xMLString = xstr.substring(length, round);
                } else {
                    if (i <= length) {
                        length = i;
                    }
                    xMLString = xstr.substring(length);
                }
                return (XString) xMLString;
            }
        }
        i = 0;
        if (this.m_arg2 == null) {
        }
        return (XString) xMLString;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i < 2) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("ER_TWO_OR_THREE", null));
    }
}
