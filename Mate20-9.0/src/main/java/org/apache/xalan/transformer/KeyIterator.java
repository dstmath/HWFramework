package org.apache.xalan.transformer;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.KeyDeclaration;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.OneStepIteratorForward;

public class KeyIterator extends OneStepIteratorForward {
    static final long serialVersionUID = -1349109910100249661L;
    private Vector m_keyDeclarations;
    private QName m_name;

    public QName getName() {
        return this.m_name;
    }

    public Vector getKeyDeclarations() {
        return this.m_keyDeclarations;
    }

    KeyIterator(QName name, Vector keyDeclarations) {
        super(16);
        this.m_keyDeclarations = keyDeclarations;
        this.m_name = name;
    }

    public short acceptNode(int testNode) {
        boolean foundKey;
        KeyIterator ki = (KeyIterator) this.m_lpi;
        XPathContext xctxt = ki.getXPathContext();
        Vector keys = ki.getKeyDeclarations();
        QName name = ki.getName();
        try {
            int nDeclarations = keys.size();
            foundKey = false;
            int i = 0;
            while (i < nDeclarations) {
                try {
                    KeyDeclaration kd = (KeyDeclaration) keys.elementAt(i);
                    if (!kd.getName().equals(name)) {
                        int i2 = testNode;
                    } else {
                        foundKey = true;
                        try {
                            double score = kd.getMatch().getMatchScore(xctxt, testNode);
                            kd.getMatch();
                            if (score != Double.NEGATIVE_INFINITY) {
                                return 1;
                            }
                        } catch (TransformerException e) {
                        }
                    }
                    i++;
                } catch (TransformerException e2) {
                    int i3 = testNode;
                }
            }
            int i4 = testNode;
        } catch (TransformerException e3) {
            int i5 = testNode;
            foundKey = false;
        }
        if (foundKey) {
            return 2;
        }
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_XSLKEY_DECLARATION, new Object[]{name.getLocalName()}));
    }
}
