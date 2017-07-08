package org.apache.xml.utils;

import java.io.Serializable;
import javax.xml.transform.SourceLocator;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class SAXSourceLocator extends LocatorImpl implements SourceLocator, Serializable {
    static final long serialVersionUID = 3181680946321164112L;
    Locator m_locator;

    public SAXSourceLocator(Locator locator) {
        this.m_locator = locator;
        setColumnNumber(locator.getColumnNumber());
        setLineNumber(locator.getLineNumber());
        setPublicId(locator.getPublicId());
        setSystemId(locator.getSystemId());
    }

    public SAXSourceLocator(SourceLocator locator) {
        this.m_locator = null;
        setColumnNumber(locator.getColumnNumber());
        setLineNumber(locator.getLineNumber());
        setPublicId(locator.getPublicId());
        setSystemId(locator.getSystemId());
    }

    public SAXSourceLocator(SAXParseException spe) {
        setLineNumber(spe.getLineNumber());
        setColumnNumber(spe.getColumnNumber());
        setPublicId(spe.getPublicId());
        setSystemId(spe.getSystemId());
    }

    public String getPublicId() {
        return this.m_locator == null ? super.getPublicId() : this.m_locator.getPublicId();
    }

    public String getSystemId() {
        return this.m_locator == null ? super.getSystemId() : this.m_locator.getSystemId();
    }

    public int getLineNumber() {
        return this.m_locator == null ? super.getLineNumber() : this.m_locator.getLineNumber();
    }

    public int getColumnNumber() {
        return this.m_locator == null ? super.getColumnNumber() : this.m_locator.getColumnNumber();
    }
}
