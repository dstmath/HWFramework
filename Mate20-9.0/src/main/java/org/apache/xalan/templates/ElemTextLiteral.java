package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.xml.sax.SAXException;

public class ElemTextLiteral extends ElemTemplateElement {
    static final long serialVersionUID = -7872620006767660088L;
    private char[] m_ch;
    private boolean m_disableOutputEscaping = false;
    private boolean m_preserveSpace;
    private String m_str;

    public void setPreserveSpace(boolean v) {
        this.m_preserveSpace = v;
    }

    public boolean getPreserveSpace() {
        return this.m_preserveSpace;
    }

    public void setChars(char[] v) {
        this.m_ch = v;
    }

    public char[] getChars() {
        return this.m_ch;
    }

    public synchronized String getNodeValue() {
        if (this.m_str == null) {
            this.m_str = new String(this.m_ch);
        }
        return this.m_str;
    }

    public void setDisableOutputEscaping(boolean v) {
        this.m_disableOutputEscaping = v;
    }

    public boolean getDisableOutputEscaping() {
        return this.m_disableOutputEscaping;
    }

    public int getXSLToken() {
        return 78;
    }

    public String getNodeName() {
        return "#Text";
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        try {
            SerializationHandler rth = transformer.getResultTreeHandler();
            if (this.m_disableOutputEscaping) {
                rth.processingInstruction("javax.xml.transform.disable-output-escaping", "");
            }
            rth.characters(this.m_ch, 0, this.m_ch.length);
            if (this.m_disableOutputEscaping) {
                rth.processingInstruction("javax.xml.transform.enable-output-escaping", "");
            }
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }
}
