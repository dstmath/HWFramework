package org.apache.xml.utils;

import org.xml.sax.SAXException;

public class StopParseException extends SAXException {
    static final long serialVersionUID = 210102479218258961L;

    StopParseException() {
        super("Stylesheet PIs found, stop the parse");
    }
}
