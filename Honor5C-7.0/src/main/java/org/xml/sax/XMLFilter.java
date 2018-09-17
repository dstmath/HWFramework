package org.xml.sax;

public interface XMLFilter extends XMLReader {
    XMLReader getParent();

    void setParent(XMLReader xMLReader);
}
