package org.apache.xalan.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.AVT;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringToIntTable;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPath;
import org.xml.sax.SAXException;

public class XSLTAttributeDef {
    static final int ERROR = 1;
    static final int FATAL = 0;
    static final String S_FOREIGNATTR_SETTER = "setForeignAttr";
    static final int T_AVT = 3;
    static final int T_AVT_QNAME = 18;
    static final int T_CDATA = 1;
    static final int T_CHAR = 6;
    static final int T_ENUM = 11;
    static final int T_ENUM_OR_PQNAME = 16;
    static final int T_EXPR = 5;
    static final int T_NCNAME = 17;
    static final int T_NMTOKEN = 13;
    static final int T_NUMBER = 7;
    static final int T_PATTERN = 4;
    static final int T_PREFIXLIST = 20;
    static final int T_PREFIX_URLLIST = 15;
    static final int T_QNAME = 9;
    static final int T_QNAMES = 10;
    static final int T_QNAMES_RESOLVE_NULL = 19;
    static final int T_SIMPLEPATTERNLIST = 12;
    static final int T_STRINGLIST = 14;
    static final int T_URL = 2;
    static final int T_YESNO = 8;
    static final int WARNING = 2;
    static final XSLTAttributeDef m_foreignAttr = new XSLTAttributeDef("*", "*", 1, false, false, 2);
    private String m_default;
    private StringToIntTable m_enums;
    int m_errorType = 2;
    private String m_name;
    private String m_namespace;
    private boolean m_required;
    String m_setterString = null;
    private boolean m_supportsAVT;
    private int m_type;

    XSLTAttributeDef(String namespace, String name, int type, boolean required, boolean supportsAVT, int errorType) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = type;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
    }

    XSLTAttributeDef(String namespace, String name, int type, boolean supportsAVT, int errorType, String defaultVal) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = type;
        this.m_required = false;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_default = defaultVal;
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? 16 : 11;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(2);
        this.m_enums.put(k1, v1);
        this.m_enums.put(k2, v2);
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? 16 : 11;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(3);
        this.m_enums.put(k1, v1);
        this.m_enums.put(k2, v2);
        this.m_enums.put(k3, v3);
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3, String k4, int v4) {
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? 16 : 11;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(4);
        this.m_enums.put(k1, v1);
        this.m_enums.put(k2, v2);
        this.m_enums.put(k3, v3);
        this.m_enums.put(k4, v4);
    }

    String getNamespace() {
        return this.m_namespace;
    }

    String getName() {
        return this.m_name;
    }

    int getType() {
        return this.m_type;
    }

    private int getEnum(String key) {
        return this.m_enums.get(key);
    }

    private String[] getEnumNames() {
        return this.m_enums.keys();
    }

    String getDefault() {
        return this.m_default;
    }

    void setDefault(String def) {
        this.m_default = def;
    }

    boolean getRequired() {
        return this.m_required;
    }

    boolean getSupportsAVT() {
        return this.m_supportsAVT;
    }

    int getErrorType() {
        return this.m_errorType;
    }

    public String getSetterMethodName() {
        if (this.m_setterString == null) {
            if (m_foreignAttr == this) {
                return S_FOREIGNATTR_SETTER;
            }
            if (this.m_name.equals("*")) {
                this.m_setterString = "addLiteralResultAttribute";
                return this.m_setterString;
            }
            StringBuffer outBuf = new StringBuffer();
            outBuf.append("set");
            if (this.m_namespace != null && this.m_namespace.equals("http://www.w3.org/XML/1998/namespace")) {
                outBuf.append("Xml");
            }
            int n = this.m_name.length();
            int i = 0;
            while (i < n) {
                char c = this.m_name.charAt(i);
                if ('-' == c) {
                    i++;
                    c = Character.toUpperCase(this.m_name.charAt(i));
                } else if (i == 0) {
                    c = Character.toUpperCase(c);
                }
                outBuf.append(c);
                i++;
            }
            this.m_setterString = outBuf.toString();
        }
        return this.m_setterString;
    }

    AVT processAVT(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return new AVT(handler, uri, name, rawName, value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    Object processCDATA(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (!getSupportsAVT()) {
            return value;
        }
        try {
            return new AVT(handler, uri, name, rawName, value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    Object processCHAR(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(handler, uri, name, rawName, value, owner);
                if (!avt.isSimple() || value.length() == 1) {
                    return avt;
                }
                handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[]{name, value}, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (value.length() == 1) {
            return new Character(value.charAt(0));
        } else {
            handleError(handler, XSLTErrorResources.INVALID_TCHAR, new Object[]{name, value}, null);
            return null;
        }
    }

    Object processENUM(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        AVT avt;
        TransformerException te;
        if (getSupportsAVT()) {
            try {
                avt = new AVT(handler, uri, name, rawName, value, owner);
                try {
                    if (!avt.isSimple()) {
                        return avt;
                    }
                } catch (TransformerException e) {
                    te = e;
                }
            } catch (TransformerException e2) {
                te = e2;
                avt = null;
                throw new SAXException(te);
            }
        }
        avt = null;
        int retVal = getEnum(value);
        if (retVal == -10000) {
            StringBuffer enumNamesList = getListOfEnums();
            handleError(handler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, value, enumNamesList.toString()}, null);
            return null;
        } else if (getSupportsAVT()) {
            return avt;
        } else {
            return new Integer(retVal);
        }
    }

    Object processENUM_OR_PQNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        Object objToReturn = null;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(handler, uri, name, rawName, value, owner);
                if (!avt.isSimple()) {
                    return avt;
                }
                objToReturn = avt;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        int key = getEnum(value);
        if (key == -10000) {
            try {
                QName qname = new QName(value, (PrefixResolver) handler, true);
                if (objToReturn == null) {
                    objToReturn = qname;
                }
                if (qname.getPrefix() == null) {
                    getListOfEnums().append(" <qname-but-not-ncname>");
                    handleError(handler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, value, enumNamesList.toString()}, null);
                    return null;
                }
            } catch (IllegalArgumentException ie) {
                getListOfEnums().append(" <qname-but-not-ncname>");
                handleError(handler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, value, enumNamesList.toString()}, ie);
                return null;
            } catch (RuntimeException re) {
                getListOfEnums().append(" <qname-but-not-ncname>");
                handleError(handler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, value, enumNamesList.toString()}, re);
                return null;
            }
        } else if (objToReturn == null) {
            objToReturn = new Integer(key);
        }
        return objToReturn;
    }

    Object processEXPR(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return handler.createXPath(value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    Object processNMTOKEN(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(handler, uri, name, rawName, value, owner);
                if (!avt.isSimple() || (XML11Char.isXML11ValidNmtoken(value) ^ 1) == 0) {
                    return avt;
                }
                handleError(handler, XSLTErrorResources.INVALID_NMTOKEN, new Object[]{name, value}, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (XML11Char.isXML11ValidNmtoken(value)) {
            return value;
        } else {
            handleError(handler, XSLTErrorResources.INVALID_NMTOKEN, new Object[]{name, value}, null);
            return null;
        }
    }

    Object processPATTERN(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return handler.createMatchPatternXPath(value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    Object processNUMBER(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        TransformerException te;
        NumberFormatException nfe;
        if (getSupportsAVT()) {
            AVT avt;
            try {
                avt = new AVT(handler, uri, name, rawName, value, owner);
                try {
                    if (avt.isSimple()) {
                        Double valueOf = Double.valueOf(value);
                    }
                    return avt;
                } catch (TransformerException e) {
                    te = e;
                    throw new SAXException(te);
                } catch (NumberFormatException e2) {
                    nfe = e2;
                    handleError(handler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe);
                    return null;
                }
            } catch (TransformerException e3) {
                te = e3;
                avt = null;
                throw new SAXException(te);
            } catch (NumberFormatException e4) {
                nfe = e4;
                avt = null;
                handleError(handler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe);
                return null;
            }
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException nfe2) {
            handleError(handler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe2);
            return null;
        }
    }

    Object processQNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return new QName(value, (PrefixResolver) handler, true);
        } catch (IllegalArgumentException ie) {
            handleError(handler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, value}, ie);
            return null;
        } catch (RuntimeException re) {
            handleError(handler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, value}, re);
            return null;
        }
    }

    Object processAVT_QNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        TransformerException te;
        AVT avt;
        try {
            avt = new AVT(handler, uri, name, rawName, value, owner);
            try {
                if (avt.isSimple()) {
                    int indexOfNSSep = value.indexOf(58);
                    if (indexOfNSSep < 0 || XML11Char.isXML11ValidNCName(value.substring(0, indexOfNSSep))) {
                        String localName;
                        if (indexOfNSSep < 0) {
                            localName = value;
                        } else {
                            localName = value.substring(indexOfNSSep + 1);
                        }
                        if (localName == null || localName.length() == 0 || (XML11Char.isXML11ValidNCName(localName) ^ 1) != 0) {
                            handleError(handler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, value}, null);
                            return null;
                        }
                    }
                    handleError(handler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, value}, null);
                    return null;
                }
                return avt;
            } catch (TransformerException e) {
                te = e;
                throw new SAXException(te);
            }
        } catch (TransformerException e2) {
            te = e2;
            avt = null;
            throw new SAXException(te);
        }
    }

    Object processNCNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        TransformerException te;
        if (getSupportsAVT()) {
            AVT avt;
            try {
                avt = new AVT(handler, uri, name, rawName, value, owner);
                try {
                    if (!avt.isSimple() || (XML11Char.isXML11ValidNCName(value) ^ 1) == 0) {
                        return avt;
                    }
                    handleError(handler, XSLTErrorResources.INVALID_NCNAME, new Object[]{name, value}, null);
                    return null;
                } catch (TransformerException e) {
                    te = e;
                    throw new SAXException(te);
                }
            } catch (TransformerException e2) {
                te = e2;
                avt = null;
                throw new SAXException(te);
            }
        } else if (XML11Char.isXML11ValidNCName(value)) {
            return value;
        } else {
            handleError(handler, XSLTErrorResources.INVALID_NCNAME, new Object[]{name, value}, null);
            return null;
        }
    }

    Vector processQNAMES(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nQNames = tokenizer.countTokens();
        Vector qnames = new Vector(nQNames);
        for (int i = 0; i < nQNames; i++) {
            qnames.addElement(new QName(tokenizer.nextToken(), (PrefixResolver) handler));
        }
        return qnames;
    }

    final Vector processQNAMESRNU(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nQNames = tokenizer.countTokens();
        Vector qnames = new Vector(nQNames);
        String defaultURI = handler.getNamespaceForPrefix("");
        for (int i = 0; i < nQNames; i++) {
            String tok = tokenizer.nextToken();
            if (tok.indexOf(58) == -1) {
                qnames.addElement(new QName(defaultURI, tok));
            } else {
                qnames.addElement(new QName(tok, (PrefixResolver) handler));
            }
        }
        return qnames;
    }

    Vector processSIMPLEPATTERNLIST(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
            int nPatterns = tokenizer.countTokens();
            Vector patterns = new Vector(nPatterns);
            for (int i = 0; i < nPatterns; i++) {
                patterns.addElement(handler.createMatchPatternXPath(tokenizer.nextToken(), owner));
            }
            return patterns;
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    StringVector processSTRINGLIST(StylesheetHandler handler, String uri, String name, String rawName, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        for (int i = 0; i < nStrings; i++) {
            strings.addElement(tokenizer.nextToken());
        }
        return strings;
    }

    StringVector processPREFIX_URLLIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        int i = 0;
        while (i < nStrings) {
            String url = handler.getNamespaceForPrefix(tokenizer.nextToken());
            if (url != null) {
                strings.addElement(url);
                i++;
            } else {
                throw new SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, new Object[]{prefix}));
            }
        }
        return strings;
    }

    StringVector processPREFIX_LIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        int i = 0;
        while (i < nStrings) {
            String prefix = tokenizer.nextToken();
            String url = handler.getNamespaceForPrefix(prefix);
            if (prefix.equals("#default") || url != null) {
                strings.addElement(prefix);
                i++;
            } else {
                throw new SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, new Object[]{prefix}));
            }
        }
        return strings;
    }

    Object processURL(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (!getSupportsAVT()) {
            return value;
        }
        try {
            return new AVT(handler, uri, name, rawName, value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    private Boolean processYESNO(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        boolean z;
        boolean z2 = true;
        if (value.equals("yes")) {
            z = true;
        } else {
            z = value.equals("no");
        }
        if (z) {
            if (!value.equals("yes")) {
                z2 = false;
            }
            return new Boolean(z2);
        }
        handleError(handler, XSLTErrorResources.INVALID_BOOLEAN, new Object[]{name, value}, null);
        return null;
    }

    Object processValue(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        switch (getType()) {
            case 1:
                return processCDATA(handler, uri, name, rawName, value, owner);
            case 2:
                return processURL(handler, uri, name, rawName, value, owner);
            case 3:
                return processAVT(handler, uri, name, rawName, value, owner);
            case 4:
                return processPATTERN(handler, uri, name, rawName, value, owner);
            case 5:
                return processEXPR(handler, uri, name, rawName, value, owner);
            case 6:
                return processCHAR(handler, uri, name, rawName, value, owner);
            case 7:
                return processNUMBER(handler, uri, name, rawName, value, owner);
            case 8:
                return processYESNO(handler, uri, name, rawName, value);
            case 9:
                return processQNAME(handler, uri, name, rawName, value, owner);
            case 10:
                return processQNAMES(handler, uri, name, rawName, value);
            case 11:
                return processENUM(handler, uri, name, rawName, value, owner);
            case 12:
                return processSIMPLEPATTERNLIST(handler, uri, name, rawName, value, owner);
            case 13:
                return processNMTOKEN(handler, uri, name, rawName, value, owner);
            case 14:
                return processSTRINGLIST(handler, uri, name, rawName, value);
            case 15:
                return processPREFIX_URLLIST(handler, uri, name, rawName, value);
            case 16:
                return processENUM_OR_PQNAME(handler, uri, name, rawName, value, owner);
            case 17:
                return processNCNAME(handler, uri, name, rawName, value, owner);
            case 18:
                return processAVT_QNAME(handler, uri, name, rawName, value, owner);
            case 19:
                return processQNAMESRNU(handler, uri, name, rawName, value);
            case 20:
                return processPREFIX_LIST(handler, uri, name, rawName, value);
            default:
                return null;
        }
    }

    void setDefAttrValue(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
        setAttrValue(handler, getNamespace(), getName(), getName(), getDefault(), elem);
    }

    private Class getPrimativeClass(Object obj) {
        if (obj instanceof XPath) {
            return XPath.class;
        }
        Class cl = obj.getClass();
        if (cl == Double.class) {
            cl = Double.TYPE;
        }
        if (cl == Float.class) {
            cl = Float.TYPE;
        } else if (cl == Boolean.class) {
            cl = Boolean.TYPE;
        } else if (cl == Byte.class) {
            cl = Byte.TYPE;
        } else if (cl == Character.class) {
            cl = Character.TYPE;
        } else if (cl == Short.class) {
            cl = Short.TYPE;
        } else if (cl == Integer.class) {
            cl = Integer.TYPE;
        } else if (cl == Long.class) {
            cl = Long.TYPE;
        }
        return cl;
    }

    private StringBuffer getListOfEnums() {
        StringBuffer enumNamesList = new StringBuffer();
        String[] enumValues = getEnumNames();
        for (int i = 0; i < enumValues.length; i++) {
            if (i > 0) {
                enumNamesList.append(' ');
            }
            enumNamesList.append(enumValues[i]);
        }
        return enumNamesList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00aa A:{Splitter: B:8:0x001e, ExcHandler: java.lang.IllegalAccessException (r4_0 'iae' java.lang.IllegalAccessException)} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008f A:{Splitter: B:8:0x001e, ExcHandler: java.lang.reflect.InvocationTargetException (r7_0 'nsme' java.lang.reflect.InvocationTargetException)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:28:0x007f, code:
            r1[0] = r10.getClass();
            r5 = r21.getClass().getMethod(r9, r1);
     */
    /* JADX WARNING: Missing block: B:29:0x008f, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x0090, code:
            handleError(r16, org.apache.xalan.res.XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE, new java.lang.Object[]{"name", getName()}, r7);
     */
    /* JADX WARNING: Missing block: B:31:0x00a9, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:32:0x00aa, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:33:0x00ab, code:
            r16.error(org.apache.xalan.res.XSLTErrorResources.ER_FAILED_CALLING_METHOD, new java.lang.Object[]{r9}, r4);
     */
    /* JADX WARNING: Missing block: B:34:0x00ba, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean setAttrValue(StylesheetHandler handler, String attrUri, String attrLocalName, String attrRawName, String attrValue, ElemTemplateElement elem) throws SAXException {
        if (!attrRawName.equals("xmlns")) {
            if (!attrRawName.startsWith(Constants.ATTRNAME_XMLNS)) {
                String setterString = getSetterMethodName();
                if (setterString != null) {
                    try {
                        Method meth;
                        Object[] args;
                        if (setterString.equals(S_FOREIGNATTR_SETTER)) {
                            if (attrUri == null) {
                                attrUri = "";
                            }
                            Class sclass = attrUri.getClass();
                            meth = elem.getClass().getMethod(setterString, new Class[]{sclass, sclass, sclass, sclass});
                            args = new Object[]{attrUri, attrLocalName, attrRawName, attrValue};
                        } else {
                            Object value = processValue(handler, attrUri, attrLocalName, attrRawName, attrValue, elem);
                            if (value == null) {
                                return false;
                            }
                            Class[] argTypes = new Class[]{getPrimativeClass(value)};
                            meth = elem.getClass().getMethod(setterString, argTypes);
                            args = new Object[]{value};
                        }
                        meth.invoke(elem, args);
                    } catch (NoSuchMethodException nsme) {
                        if (!setterString.equals(S_FOREIGNATTR_SETTER)) {
                            handler.error(XSLTErrorResources.ER_FAILED_CALLING_METHOD, new Object[]{setterString}, nsme);
                            return false;
                        }
                    } catch (IllegalAccessException iae) {
                    } catch (InvocationTargetException nsme2) {
                    }
                }
                return true;
            }
        }
        return true;
    }

    private void handleError(StylesheetHandler handler, String msg, Object[] args, Exception exc) throws SAXException {
        switch (getErrorType()) {
            case 0:
            case 1:
                handler.error(msg, args, exc);
                return;
            case 2:
                handler.warn(msg, args);
                return;
            default:
                return;
        }
    }
}
