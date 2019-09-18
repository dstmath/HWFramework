package org.apache.xml.serializer;

import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public final class AttributesImplSerializer extends AttributesImpl {
    private static final int MAX = 12;
    private static final int MAXMinus1 = 11;
    private final StringBuffer m_buff = new StringBuffer();
    private final Hashtable m_indexFromQName = new Hashtable();

    public final int getIndex(String qname) {
        int index;
        if (super.getLength() < 12) {
            return super.getIndex(qname);
        }
        Integer i = (Integer) this.m_indexFromQName.get(qname);
        if (i == null) {
            index = -1;
        } else {
            index = i.intValue();
        }
        return index;
    }

    public final void addAttribute(String uri, String local, String qname, String type, String val) {
        int index = super.getLength();
        super.addAttribute(uri, local, qname, type, val);
        if (index >= 11) {
            if (index == 11) {
                switchOverToHash(12);
            } else {
                Integer i = new Integer(index);
                this.m_indexFromQName.put(qname, i);
                this.m_buff.setLength(0);
                StringBuffer stringBuffer = this.m_buff;
                stringBuffer.append('{');
                stringBuffer.append(uri);
                stringBuffer.append('}');
                stringBuffer.append(local);
                this.m_indexFromQName.put(this.m_buff.toString(), i);
            }
        }
    }

    private void switchOverToHash(int numAtts) {
        for (int index = 0; index < numAtts; index++) {
            String qName = super.getQName(index);
            Integer i = new Integer(index);
            this.m_indexFromQName.put(qName, i);
            String uri = super.getURI(index);
            String local = super.getLocalName(index);
            this.m_buff.setLength(0);
            StringBuffer stringBuffer = this.m_buff;
            stringBuffer.append('{');
            stringBuffer.append(uri);
            stringBuffer.append('}');
            stringBuffer.append(local);
            this.m_indexFromQName.put(this.m_buff.toString(), i);
        }
    }

    public final void clear() {
        int len = super.getLength();
        super.clear();
        if (12 <= len) {
            this.m_indexFromQName.clear();
        }
    }

    public final void setAttributes(Attributes atts) {
        super.setAttributes(atts);
        int numAtts = atts.getLength();
        if (12 <= numAtts) {
            switchOverToHash(numAtts);
        }
    }

    public final int getIndex(String uri, String localName) {
        int index;
        if (super.getLength() < 12) {
            return super.getIndex(uri, localName);
        }
        this.m_buff.setLength(0);
        StringBuffer stringBuffer = this.m_buff;
        stringBuffer.append('{');
        stringBuffer.append(uri);
        stringBuffer.append('}');
        stringBuffer.append(localName);
        Integer i = (Integer) this.m_indexFromQName.get(this.m_buff.toString());
        if (i == null) {
            index = -1;
        } else {
            index = i.intValue();
        }
        return index;
    }
}
