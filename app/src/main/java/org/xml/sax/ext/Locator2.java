package org.xml.sax.ext;

import org.xml.sax.Locator;

public interface Locator2 extends Locator {
    String getEncoding();

    String getXMLVersion();
}
