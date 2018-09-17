package org.xml.sax;

public interface Locator {
    int getColumnNumber();

    int getLineNumber();

    String getPublicId();

    String getSystemId();
}
