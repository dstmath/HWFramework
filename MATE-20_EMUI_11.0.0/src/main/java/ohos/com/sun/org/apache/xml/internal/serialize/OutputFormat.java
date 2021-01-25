package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.UnsupportedEncodingException;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.html.HTMLDocument;

public class OutputFormat {
    private boolean _allowJavaNames;
    private String[] _cdataElements;
    private String _doctypePublic;
    private String _doctypeSystem;
    private String _encoding;
    private EncodingInfo _encodingInfo;
    private int _indent;
    private String _lineSeparator;
    private int _lineWidth;
    private String _mediaType;
    private String _method;
    private String[] _nonEscapingElements;
    private boolean _omitComments;
    private boolean _omitDoctype;
    private boolean _omitXmlDeclaration;
    private boolean _preserve;
    private boolean _preserveEmptyAttributes;
    private boolean _standalone;
    private boolean _stripComments;
    private String _version;

    public static class DTD {
        public static final String HTMLPublicId = "-//W3C//DTD HTML 4.01//EN";
        public static final String HTMLSystemId = "http://www.w3.org/TR/html4/strict.dtd";
        public static final String XHTMLPublicId = "-//W3C//DTD XHTML 1.0 Strict//EN";
        public static final String XHTMLSystemId = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    }

    public static class Defaults {
        public static final String Encoding = "UTF-8";
        public static final int Indent = 4;
        public static final int LineWidth = 72;
    }

    public OutputFormat() {
        this._indent = 0;
        this._encoding = "UTF-8";
        this._encodingInfo = null;
        this._allowJavaNames = false;
        this._omitXmlDeclaration = false;
        this._omitDoctype = false;
        this._omitComments = false;
        this._stripComments = false;
        this._standalone = false;
        this._lineSeparator = "\n";
        this._lineWidth = 72;
        this._preserve = false;
        this._preserveEmptyAttributes = false;
    }

    public OutputFormat(String str, String str2, boolean z) {
        this._indent = 0;
        this._encoding = "UTF-8";
        this._encodingInfo = null;
        this._allowJavaNames = false;
        this._omitXmlDeclaration = false;
        this._omitDoctype = false;
        this._omitComments = false;
        this._stripComments = false;
        this._standalone = false;
        this._lineSeparator = "\n";
        this._lineWidth = 72;
        this._preserve = false;
        this._preserveEmptyAttributes = false;
        setMethod(str);
        setEncoding(str2);
        setIndenting(z);
    }

    public OutputFormat(Document document) {
        this._indent = 0;
        this._encoding = "UTF-8";
        this._encodingInfo = null;
        this._allowJavaNames = false;
        this._omitXmlDeclaration = false;
        this._omitDoctype = false;
        this._omitComments = false;
        this._stripComments = false;
        this._standalone = false;
        this._lineSeparator = "\n";
        this._lineWidth = 72;
        this._preserve = false;
        this._preserveEmptyAttributes = false;
        setMethod(whichMethod(document));
        setDoctype(whichDoctypePublic(document), whichDoctypeSystem(document));
        setMediaType(whichMediaType(getMethod()));
    }

    public OutputFormat(Document document, String str, boolean z) {
        this(document);
        setEncoding(str);
        setIndenting(z);
    }

    public String getMethod() {
        return this._method;
    }

    public void setMethod(String str) {
        this._method = str;
    }

    public String getVersion() {
        return this._version;
    }

    public void setVersion(String str) {
        this._version = str;
    }

    public int getIndent() {
        return this._indent;
    }

    public boolean getIndenting() {
        return this._indent > 0;
    }

    public void setIndent(int i) {
        if (i < 0) {
            this._indent = 0;
        } else {
            this._indent = i;
        }
    }

    public void setIndenting(boolean z) {
        if (z) {
            this._indent = 4;
            this._lineWidth = 72;
            return;
        }
        this._indent = 0;
        this._lineWidth = 0;
    }

    public String getEncoding() {
        return this._encoding;
    }

    public void setEncoding(String str) {
        this._encoding = str;
        this._encodingInfo = null;
    }

    public void setEncoding(EncodingInfo encodingInfo) {
        this._encoding = encodingInfo.getIANAName();
        this._encodingInfo = encodingInfo;
    }

    public EncodingInfo getEncodingInfo() throws UnsupportedEncodingException {
        if (this._encodingInfo == null) {
            this._encodingInfo = Encodings.getEncodingInfo(this._encoding, this._allowJavaNames);
        }
        return this._encodingInfo;
    }

    public void setAllowJavaNames(boolean z) {
        this._allowJavaNames = z;
    }

    public boolean setAllowJavaNames() {
        return this._allowJavaNames;
    }

    public String getMediaType() {
        return this._mediaType;
    }

    public void setMediaType(String str) {
        this._mediaType = str;
    }

    public void setDoctype(String str, String str2) {
        this._doctypePublic = str;
        this._doctypeSystem = str2;
    }

    public String getDoctypePublic() {
        return this._doctypePublic;
    }

    public String getDoctypeSystem() {
        return this._doctypeSystem;
    }

    public boolean getOmitComments() {
        return this._omitComments;
    }

    public void setOmitComments(boolean z) {
        this._omitComments = z;
    }

    public boolean getOmitDocumentType() {
        return this._omitDoctype;
    }

    public void setOmitDocumentType(boolean z) {
        this._omitDoctype = z;
    }

    public boolean getOmitXMLDeclaration() {
        return this._omitXmlDeclaration;
    }

    public void setOmitXMLDeclaration(boolean z) {
        this._omitXmlDeclaration = z;
    }

    public boolean getStandalone() {
        return this._standalone;
    }

    public void setStandalone(boolean z) {
        this._standalone = z;
    }

    public String[] getCDataElements() {
        return this._cdataElements;
    }

    public boolean isCDataElement(String str) {
        if (this._cdataElements == null) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = this._cdataElements;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].equals(str)) {
                return true;
            }
            i++;
        }
    }

    public void setCDataElements(String[] strArr) {
        this._cdataElements = strArr;
    }

    public String[] getNonEscapingElements() {
        return this._nonEscapingElements;
    }

    public boolean isNonEscapingElement(String str) {
        if (this._nonEscapingElements == null) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = this._nonEscapingElements;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].equals(str)) {
                return true;
            }
            i++;
        }
    }

    public void setNonEscapingElements(String[] strArr) {
        this._nonEscapingElements = strArr;
    }

    public String getLineSeparator() {
        return this._lineSeparator;
    }

    public void setLineSeparator(String str) {
        if (str == null) {
            this._lineSeparator = "\n";
        } else {
            this._lineSeparator = str;
        }
    }

    public boolean getPreserveSpace() {
        return this._preserve;
    }

    public void setPreserveSpace(boolean z) {
        this._preserve = z;
    }

    public int getLineWidth() {
        return this._lineWidth;
    }

    public void setLineWidth(int i) {
        if (i <= 0) {
            this._lineWidth = 0;
        } else {
            this._lineWidth = i;
        }
    }

    public boolean getPreserveEmptyAttributes() {
        return this._preserveEmptyAttributes;
    }

    public void setPreserveEmptyAttributes(boolean z) {
        this._preserveEmptyAttributes = z;
    }

    public char getLastPrintable() {
        return (getEncoding() == null || !getEncoding().equalsIgnoreCase("ASCII")) ? (char) 65535 : 255;
    }

    public static String whichMethod(Document document) {
        if (document instanceof HTMLDocument) {
            return "html";
        }
        for (Node firstChild = document.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() != 1) {
                if (firstChild.getNodeType() == 3) {
                    String nodeValue = firstChild.getNodeValue();
                    for (int i = 0; i < nodeValue.length(); i++) {
                        if (!(nodeValue.charAt(i) == ' ' || nodeValue.charAt(i) == '\n' || nodeValue.charAt(i) == '\t' || nodeValue.charAt(i) == '\r')) {
                            return "xml";
                        }
                    }
                    continue;
                }
            } else if (firstChild.getNodeName().equalsIgnoreCase("html")) {
                return "html";
            } else {
                if (firstChild.getNodeName().equalsIgnoreCase(Constants.ELEMNAME_ROOT_STRING)) {
                    return Method.FOP;
                }
                return "xml";
            }
        }
        return "xml";
    }

    public static String whichDoctypePublic(Document document) {
        DocumentType doctype = document.getDoctype();
        if (doctype != null) {
            try {
                return doctype.getPublicId();
            } catch (Error unused) {
            }
        }
        if (document instanceof HTMLDocument) {
            return "-//W3C//DTD XHTML 1.0 Strict//EN";
        }
        return null;
    }

    public static String whichDoctypeSystem(Document document) {
        DocumentType doctype = document.getDoctype();
        if (doctype != null) {
            try {
                return doctype.getSystemId();
            } catch (Error unused) {
            }
        }
        if (document instanceof HTMLDocument) {
            return "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
        }
        return null;
    }

    public static String whichMediaType(String str) {
        if (str.equalsIgnoreCase("xml")) {
            return "text/xml";
        }
        if (str.equalsIgnoreCase("html") || str.equalsIgnoreCase("xhtml")) {
            return "text/html";
        }
        if (str.equalsIgnoreCase("text")) {
            return "text/plain";
        }
        if (str.equalsIgnoreCase(Method.FOP)) {
            return "application/pdf";
        }
        return null;
    }
}
