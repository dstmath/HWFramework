package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.org.xml.sax.SAXException;

public class StopParseException extends SAXException {
    static final long serialVersionUID = 210102479218258961L;

    StopParseException() {
        super("Stylesheet PIs found, stop the parse");
    }
}
