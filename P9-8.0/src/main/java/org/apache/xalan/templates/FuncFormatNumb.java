package org.apache.xalan.templates;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.Function3Args;
import org.apache.xpath.functions.WrongNumberArgsException;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

public class FuncFormatNumb extends Function3Args {
    static final long serialVersionUID = -8869935264870858636L;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        ElemTemplateElement templElem = (ElemTemplateElement) xctxt.getNamespaceContext();
        StylesheetRoot ss = templElem.getStylesheetRoot();
        double num = getArg0().execute(xctxt).num();
        String patternStr = getArg1().execute(xctxt).str();
        if (patternStr.indexOf(164) > 0) {
            ss.error(XSLTErrorResources.ER_CURRENCY_SIGN_ILLEGAL);
        }
        try {
            DecimalFormatSymbols dfs;
            DecimalFormat formatter;
            DecimalFormat formatter2;
            Expression arg2Expr = getArg2();
            if (arg2Expr != null) {
                dfs = ss.getDecimalFormatComposed(new QName(arg2Expr.execute(xctxt).str(), xctxt.getNamespaceContext()));
                if (dfs == null) {
                    warn(xctxt, XSLTErrorResources.WG_NO_DECIMALFORMAT_DECLARATION, new Object[]{dfName});
                    formatter = null;
                } else {
                    formatter = new DecimalFormat();
                    formatter.setDecimalFormatSymbols(dfs);
                    formatter.applyLocalizedPattern(patternStr);
                }
            } else {
                formatter = null;
            }
            if (formatter == null) {
                try {
                    dfs = ss.getDecimalFormatComposed(new QName(""));
                    if (dfs != null) {
                        formatter2 = new DecimalFormat();
                        formatter2.setDecimalFormatSymbols(dfs);
                        formatter2.applyLocalizedPattern(patternStr);
                    } else {
                        DecimalFormatSymbols dfs2 = new DecimalFormatSymbols(Locale.US);
                        try {
                            dfs2.setInfinity(Constants.ATTRVAL_INFINITY);
                            dfs2.setNaN("NaN");
                            formatter2 = new DecimalFormat();
                        } catch (Exception e) {
                            dfs = dfs2;
                            templElem.error(XSLTErrorResources.ER_MALFORMED_FORMAT_STRING, new Object[]{patternStr});
                            return XString.EMPTYSTRING;
                        }
                        try {
                            formatter2.setDecimalFormatSymbols(dfs2);
                            if (patternStr != null) {
                                formatter2.applyLocalizedPattern(patternStr);
                                dfs = dfs2;
                            }
                        } catch (Exception e2) {
                            dfs = dfs2;
                            templElem.error(XSLTErrorResources.ER_MALFORMED_FORMAT_STRING, new Object[]{patternStr});
                            return XString.EMPTYSTRING;
                        }
                    }
                } catch (Exception e3) {
                }
            } else {
                formatter2 = formatter;
            }
            return new XString(formatter2.format(num));
        } catch (Exception e4) {
            templElem.error(XSLTErrorResources.ER_MALFORMED_FORMAT_STRING, new Object[]{patternStr});
            return XString.EMPTYSTRING;
        }
    }

    public void warn(XPathContext xctxt, String msg, Object[] args) throws TransformerException {
        xctxt.getErrorListener().warning(new TransformerException(XSLMessages.createWarning(msg, args), (SAXSourceLocator) xctxt.getSAXLocator()));
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum > 3 || argNum < 2) {
            reportWrongNumberArgs();
        }
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createMessage("ER_TWO_OR_THREE", null));
    }
}
