package org.apache.xml.serializer;

final class ElemContext {
    final int m_currentElemDepth;
    ElemDesc m_elementDesc;
    String m_elementLocalName;
    String m_elementName;
    String m_elementURI;
    boolean m_isCdataSection;
    boolean m_isRaw;
    private ElemContext m_next;
    final ElemContext m_prev;
    boolean m_startTagOpen;

    ElemContext() {
        this.m_elementDesc = null;
        this.m_elementLocalName = null;
        this.m_elementName = null;
        this.m_elementURI = null;
        this.m_isRaw = false;
        this.m_startTagOpen = false;
        this.m_prev = this;
        this.m_currentElemDepth = 0;
    }

    private ElemContext(ElemContext previous) {
        this.m_elementDesc = null;
        this.m_elementLocalName = null;
        this.m_elementName = null;
        this.m_elementURI = null;
        this.m_isRaw = false;
        this.m_startTagOpen = false;
        this.m_prev = previous;
        this.m_currentElemDepth = previous.m_currentElemDepth + 1;
    }

    final ElemContext pop() {
        return this.m_prev;
    }

    final ElemContext push() {
        ElemContext frame = this.m_next;
        if (frame == null) {
            frame = new ElemContext(this);
            this.m_next = frame;
        }
        frame.m_startTagOpen = true;
        return frame;
    }

    final ElemContext push(String uri, String localName, String qName) {
        ElemContext frame = this.m_next;
        if (frame == null) {
            frame = new ElemContext(this);
            this.m_next = frame;
        }
        frame.m_elementName = qName;
        frame.m_elementLocalName = localName;
        frame.m_elementURI = uri;
        frame.m_isCdataSection = false;
        frame.m_startTagOpen = true;
        return frame;
    }
}
