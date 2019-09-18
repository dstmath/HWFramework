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
    static final XSLTAttributeDef m_foreignAttr;
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

    static {
        XSLTAttributeDef xSLTAttributeDef = new XSLTAttributeDef("*", "*", 1, false, false, 2);
        m_foreignAttr = xSLTAttributeDef;
    }

    /* access modifiers changed from: package-private */
    public String getNamespace() {
        return this.m_namespace;
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return this.m_name;
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        return this.m_type;
    }

    private int getEnum(String key) {
        return this.m_enums.get(key);
    }

    private String[] getEnumNames() {
        return this.m_enums.keys();
    }

    /* access modifiers changed from: package-private */
    public String getDefault() {
        return this.m_default;
    }

    /* access modifiers changed from: package-private */
    public void setDefault(String def) {
        this.m_default = def;
    }

    /* access modifiers changed from: package-private */
    public boolean getRequired() {
        return this.m_required;
    }

    /* access modifiers changed from: package-private */
    public boolean getSupportsAVT() {
        return this.m_supportsAVT;
    }

    /* access modifiers changed from: package-private */
    public int getErrorType() {
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

    /* access modifiers changed from: package-private */
    public AVT processAVT(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            AVT avt = new AVT(handler, uri, name, rawName, value, owner);
            return avt;
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: package-private */
    public Object processCDATA(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (!getSupportsAVT()) {
            return value;
        }
        try {
            AVT avt = new AVT(handler, uri, name, rawName, value, owner);
            return avt;
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: package-private */
    public Object processCHAR(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        String str = value;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(stylesheetHandler, uri, name, rawName, str, owner);
                if (!avt.isSimple() || value.length() == 1) {
                    return avt;
                }
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_TCHAR, new Object[]{name, str}, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (value.length() == 1) {
            return new Character(str.charAt(0));
        } else {
            handleError(stylesheetHandler, XSLTErrorResources.INVALID_TCHAR, new Object[]{name, str}, null);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Object processENUM(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        AVT avt = null;
        if (getSupportsAVT()) {
            try {
                AVT avt2 = new AVT(handler, uri, name, rawName, value, owner);
                avt = avt2;
                if (!avt.isSimple()) {
                    return avt;
                }
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        int retVal = getEnum(value);
        if (retVal == -10000) {
            handleError(handler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, value, getListOfEnums().toString()}, null);
            return null;
        } else if (getSupportsAVT()) {
            return avt;
        } else {
            return new Integer(retVal);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v0, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v1, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v2, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v3, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v4, resolved type: org.apache.xml.utils.QName} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v5, resolved type: java.lang.Integer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: org.apache.xalan.templates.AVT} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v6, resolved type: java.lang.Integer} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public Object processENUM_OR_PQNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        String str = value;
        Object objToReturn = null;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(stylesheetHandler, uri, name, rawName, str, owner);
                if (!avt.isSimple()) {
                    return avt;
                }
                objToReturn = avt;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        int key = getEnum(str);
        if (key == -10000) {
            try {
                QName qname = new QName(str, (PrefixResolver) stylesheetHandler, true);
                if (objToReturn == null) {
                    objToReturn = qname;
                }
                if (qname.getPrefix() == null) {
                    StringBuffer enumNamesList = getListOfEnums();
                    enumNamesList.append(" <qname-but-not-ncname>");
                    handleError(stylesheetHandler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, str, enumNamesList.toString()}, null);
                    return null;
                }
            } catch (IllegalArgumentException ie) {
                StringBuffer enumNamesList2 = getListOfEnums();
                enumNamesList2.append(" <qname-but-not-ncname>");
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, str, enumNamesList2.toString()}, ie);
                return null;
            } catch (RuntimeException re) {
                StringBuffer enumNamesList3 = getListOfEnums();
                enumNamesList3.append(" <qname-but-not-ncname>");
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_ENUM, new Object[]{name, str, enumNamesList3.toString()}, re);
                return null;
            }
        } else if (objToReturn == null) {
            objToReturn = new Integer(key);
        }
        return objToReturn;
    }

    /* access modifiers changed from: package-private */
    public Object processEXPR(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return handler.createXPath(value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: package-private */
    public Object processNMTOKEN(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(stylesheetHandler, uri, name, rawName, value, owner);
                if (!avt.isSimple() || XML11Char.isXML11ValidNmtoken(value)) {
                    return avt;
                }
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_NMTOKEN, new Object[]{name, value}, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (XML11Char.isXML11ValidNmtoken(value)) {
            return value;
        } else {
            handleError(stylesheetHandler, XSLTErrorResources.INVALID_NMTOKEN, new Object[]{name, value}, null);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Object processPATTERN(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        try {
            return handler.createMatchPatternXPath(value, owner);
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: package-private */
    public Object processNUMBER(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(stylesheetHandler, uri, name, rawName, value, owner);
                AVT avt2 = avt;
                try {
                    if (avt2.isSimple()) {
                        Double.valueOf(value);
                    }
                    return avt2;
                } catch (TransformerException e) {
                    te = e;
                    AVT avt3 = avt2;
                    throw new SAXException(te);
                } catch (NumberFormatException e2) {
                    nfe = e2;
                    AVT avt4 = avt2;
                    handleError(stylesheetHandler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe);
                    return null;
                }
            } catch (TransformerException e3) {
                te = e3;
                throw new SAXException(te);
            } catch (NumberFormatException e4) {
                nfe = e4;
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe);
                return null;
            }
        } else {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException nfe) {
                NumberFormatException numberFormatException = nfe;
                handleError(stylesheetHandler, XSLTErrorResources.INVALID_NUMBER, new Object[]{name, value}, nfe);
                return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Object processQNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
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

    /* access modifiers changed from: package-private */
    public Object processAVT_QNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        String str = value;
        try {
            AVT avt = new AVT(stylesheetHandler, uri, name, rawName, str, owner);
            AVT avt2 = avt;
            if (avt2.isSimple()) {
                int indexOfNSSep = str.indexOf(58);
                if (indexOfNSSep < 0 || XML11Char.isXML11ValidNCName(str.substring(0, indexOfNSSep))) {
                    String localName = indexOfNSSep < 0 ? str : str.substring(indexOfNSSep + 1);
                    if (localName == null || localName.length() == 0 || !XML11Char.isXML11ValidNCName(localName)) {
                        handleError(stylesheetHandler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, str}, null);
                        return null;
                    }
                } else {
                    handleError(stylesheetHandler, XSLTErrorResources.INVALID_QNAME, new Object[]{name, str}, null);
                    return null;
                }
            }
            return avt2;
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: package-private */
    public Object processNCNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        StylesheetHandler stylesheetHandler = handler;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(stylesheetHandler, uri, name, rawName, value, owner);
                AVT avt2 = avt;
                try {
                    if (!avt2.isSimple() || XML11Char.isXML11ValidNCName(value)) {
                        return avt2;
                    }
                    handleError(stylesheetHandler, XSLTErrorResources.INVALID_NCNAME, new Object[]{name, value}, null);
                    return null;
                } catch (TransformerException e) {
                    te = e;
                    throw new SAXException(te);
                }
            } catch (TransformerException e2) {
                te = e2;
                throw new SAXException(te);
            }
        } else if (XML11Char.isXML11ValidNCName(value)) {
            return value;
        } else {
            handleError(stylesheetHandler, XSLTErrorResources.INVALID_NCNAME, new Object[]{name, value}, null);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Vector processQNAMES(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nQNames = tokenizer.countTokens();
        Vector qnames = new Vector(nQNames);
        for (int i = 0; i < nQNames; i++) {
            qnames.addElement(new QName(tokenizer.nextToken(), (PrefixResolver) handler));
        }
        return qnames;
    }

    /* access modifiers changed from: package-private */
    public final Vector processQNAMESRNU(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
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

    /* access modifiers changed from: package-private */
    public Vector processSIMPLEPATTERNLIST(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
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

    /* access modifiers changed from: package-private */
    public StringVector processSTRINGLIST(StylesheetHandler handler, String uri, String name, String rawName, String value) {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        for (int i = 0; i < nStrings; i++) {
            strings.addElement(tokenizer.nextToken());
        }
        return strings;
    }

    /* access modifiers changed from: package-private */
    public StringVector processPREFIX_URLLIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        int i = 0;
        while (i < nStrings) {
            String prefix = tokenizer.nextToken();
            String url = handler.getNamespaceForPrefix(prefix);
            if (url != null) {
                strings.addElement(url);
                i++;
            } else {
                throw new SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, new Object[]{prefix}));
            }
        }
        return strings;
    }

    /* access modifiers changed from: package-private */
    public StringVector processPREFIX_LIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
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

    /* access modifiers changed from: package-private */
    public Object processURL(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        if (!getSupportsAVT()) {
            return value;
        }
        try {
            AVT avt = new AVT(handler, uri, name, rawName, value, owner);
            return avt;
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    private Boolean processYESNO(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        if (value.equals("yes") || value.equals("no")) {
            return new Boolean(value.equals("yes"));
        }
        handleError(handler, XSLTErrorResources.INVALID_BOOLEAN, new Object[]{name, value}, null);
        return null;
    }

    /* access modifiers changed from: package-private */
    public Object processValue(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
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

    /* access modifiers changed from: package-private */
    public void setDefAttrValue(StylesheetHandler handler, ElemTemplateElement elem) throws SAXException {
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r5[0] = r4.getClass();
        r8 = r15.getClass().getMethod(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007f, code lost:
        handleError(r10, org.apache.xalan.res.XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE, new java.lang.Object[]{"name", getName()}, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0090, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0091, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0092, code lost:
        r10.error(org.apache.xalan.res.XSLTErrorResources.ER_FAILED_CALLING_METHOD, new java.lang.Object[]{r0}, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009b, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x007e A[ExcHandler: InvocationTargetException (r4v5 'nsme' java.lang.reflect.InvocationTargetException A[CUSTOM_DECLARE]), Splitter:B:7:0x001d] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0091 A[ExcHandler: IllegalAccessException (r2v2 'iae' java.lang.IllegalAccessException A[CUSTOM_DECLARE]), Splitter:B:7:0x001d] */
    public boolean setAttrValue(StylesheetHandler handler, String attrUri, String attrLocalName, String attrRawName, String attrValue, ElemTemplateElement elem) throws SAXException {
        Method meth;
        Object[] args;
        if (attrRawName.equals("xmlns") || attrRawName.startsWith(Constants.ATTRNAME_XMLNS)) {
            return true;
        }
        String setterString = getSetterMethodName();
        if (setterString != null) {
            try {
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
                    Class[] argTypes = {getPrimativeClass(value)};
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
