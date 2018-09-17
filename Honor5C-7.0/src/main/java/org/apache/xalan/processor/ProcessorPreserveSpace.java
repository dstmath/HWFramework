package org.apache.xalan.processor;

import java.util.Vector;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.WhiteSpaceInfo;
import org.apache.xpath.XPath;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorPreserveSpace extends XSLTElementProcessor {
    static final long serialVersionUID = -5552836470051177302L;

    ProcessorPreserveSpace() {
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        Stylesheet thisSheet = handler.getStylesheet();
        WhitespaceInfoPaths paths = new WhitespaceInfoPaths(thisSheet);
        setPropertiesFromAttributes(handler, rawName, attributes, paths);
        Vector xpaths = paths.getElements();
        for (int i = 0; i < xpaths.size(); i++) {
            WhiteSpaceInfo wsi = new WhiteSpaceInfo((XPath) xpaths.elementAt(i), false, thisSheet);
            wsi.setUid(handler.nextUid());
            thisSheet.setPreserveSpaces(wsi);
        }
        paths.clearElements();
    }
}
