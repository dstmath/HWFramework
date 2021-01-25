package ohos.com.sun.org.apache.xpath.internal.objects;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.javax.xml.transform.TransformerException;

public class XRTreeFragSelectWrapper extends XRTreeFrag implements Cloneable {
    static final long serialVersionUID = -6526177905590461251L;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 3;
    }

    public XRTreeFragSelectWrapper(Expression expression) {
        super(expression);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        ((Expression) this.m_obj).fixupVariables(vector, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        XObject execute = ((Expression) this.m_obj).execute(xPathContext);
        execute.allowDetachToRelease(this.m_allowRelease);
        if (execute.getType() == 3) {
            return execute;
        }
        return new XString(execute.str());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() throws TransformerException {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public XMLString xstr() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int rtf() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XRTreeFrag
    public DTMIterator asNodeIterator() {
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER", null));
    }
}
