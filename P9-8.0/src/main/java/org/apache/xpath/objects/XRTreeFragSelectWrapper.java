package org.apache.xpath.objects;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class XRTreeFragSelectWrapper extends XRTreeFrag implements Cloneable {
    static final long serialVersionUID = -6526177905590461251L;

    public XRTreeFragSelectWrapper(Expression expr) {
        super(expr);
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        ((Expression) this.m_obj).fixupVariables(vars, globalsSize);
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XObject m_selected = ((Expression) this.m_obj).execute(xctxt);
        m_selected.allowDetachToRelease(this.m_allowRelease);
        if (m_selected.getType() == 3) {
            return m_selected;
        }
        return new XString(m_selected.str());
    }

    public void detach() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_DETACH_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }

    public double num() throws TransformerException {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NUM_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }

    public XMLString xstr() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_XSTR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }

    public String str() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_STR_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }

    public int getType() {
        return 3;
    }

    public int rtf() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }

    public DTMIterator asNodeIterator() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_RTF_NOT_SUPPORTED_XRTREEFRAGSELECTWRAPPER, null));
    }
}
