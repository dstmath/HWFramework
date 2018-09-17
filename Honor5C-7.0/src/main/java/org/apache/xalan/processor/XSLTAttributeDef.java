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
import org.apache.xpath.compiler.Keywords;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.jaxp.JAXPPrefixResolver;
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
    static final XSLTAttributeDef m_foreignAttr = null;
    private String m_default;
    private StringToIntTable m_enums;
    int m_errorType;
    private String m_name;
    private String m_namespace;
    private boolean m_required;
    String m_setterString;
    private boolean m_supportsAVT;
    private int m_type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xalan.processor.XSLTAttributeDef.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xalan.processor.XSLTAttributeDef.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xalan.processor.XSLTAttributeDef.<clinit>():void");
    }

    XSLTAttributeDef(String namespace, String name, int type, boolean required, boolean supportsAVT, int errorType) {
        this.m_errorType = WARNING;
        this.m_setterString = null;
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = type;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
    }

    XSLTAttributeDef(String namespace, String name, int type, boolean supportsAVT, int errorType, String defaultVal) {
        this.m_errorType = WARNING;
        this.m_setterString = null;
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = type;
        this.m_required = false;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_default = defaultVal;
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2) {
        this.m_errorType = WARNING;
        this.m_setterString = null;
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? T_ENUM_OR_PQNAME : T_ENUM;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(WARNING);
        this.m_enums.put(k1, v1);
        this.m_enums.put(k2, v2);
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3) {
        this.m_errorType = WARNING;
        this.m_setterString = null;
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? T_ENUM_OR_PQNAME : T_ENUM;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(T_AVT);
        this.m_enums.put(k1, v1);
        this.m_enums.put(k2, v2);
        this.m_enums.put(k3, v3);
    }

    XSLTAttributeDef(String namespace, String name, boolean required, boolean supportsAVT, boolean prefixedQNameValAllowed, int errorType, String k1, int v1, String k2, int v2, String k3, int v3, String k4, int v4) {
        this.m_errorType = WARNING;
        this.m_setterString = null;
        this.m_namespace = namespace;
        this.m_name = name;
        this.m_type = prefixedQNameValAllowed ? T_ENUM_OR_PQNAME : T_ENUM;
        this.m_required = required;
        this.m_supportsAVT = supportsAVT;
        this.m_errorType = errorType;
        this.m_enums = new StringToIntTable(T_PATTERN);
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
            if (this.m_name.equals(PsuedoNames.PSEUDONAME_OTHER)) {
                this.m_setterString = "addLiteralResultAttribute";
                return this.m_setterString;
            }
            StringBuffer outBuf = new StringBuffer();
            outBuf.append("set");
            if (this.m_namespace != null && this.m_namespace.equals(JAXPPrefixResolver.S_XMLNAMESPACEURI)) {
                outBuf.append("Xml");
            }
            int n = this.m_name.length();
            int i = FATAL;
            while (i < n) {
                char c = this.m_name.charAt(i);
                if ('-' == c) {
                    i += T_CDATA;
                    c = Character.toUpperCase(this.m_name.charAt(i));
                } else if (i == 0) {
                    c = Character.toUpperCase(c);
                }
                outBuf.append(c);
                i += T_CDATA;
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
        String str;
        Object[] objArr;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(handler, uri, name, rawName, value, owner);
                if (!avt.isSimple() || value.length() == T_CDATA) {
                    return avt;
                }
                str = XSLTErrorResources.INVALID_TCHAR;
                objArr = new Object[WARNING];
                objArr[FATAL] = name;
                objArr[T_CDATA] = value;
                handleError(handler, str, objArr, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (value.length() == T_CDATA) {
            return new Character(value.charAt(FATAL));
        } else {
            str = XSLTErrorResources.INVALID_TCHAR;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, null);
            return null;
        }
    }

    Object processENUM(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        TransformerException te;
        AVT avt;
        if (getSupportsAVT()) {
            try {
                avt = new AVT(handler, uri, name, rawName, value, owner);
                try {
                    if (!avt.isSimple()) {
                        return avt;
                    }
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
        avt = null;
        int retVal = getEnum(value);
        if (retVal == StringToIntTable.INVALID_KEY) {
            StringBuffer enumNamesList = getListOfEnums();
            String str = XSLTErrorResources.INVALID_ENUM;
            Object[] objArr = new Object[T_AVT];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            objArr[WARNING] = enumNamesList.toString();
            handleError(handler, str, objArr, null);
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
        if (key == StringToIntTable.INVALID_KEY) {
            StringBuffer enumNamesList;
            String str;
            Object[] objArr;
            try {
                QName qname = new QName(value, (PrefixResolver) handler, true);
                if (objToReturn == null) {
                    objToReturn = qname;
                }
                if (qname.getPrefix() == null) {
                    enumNamesList = getListOfEnums();
                    enumNamesList.append(" <qname-but-not-ncname>");
                    str = XSLTErrorResources.INVALID_ENUM;
                    objArr = new Object[T_AVT];
                    objArr[FATAL] = name;
                    objArr[T_CDATA] = value;
                    objArr[WARNING] = enumNamesList.toString();
                    handleError(handler, str, objArr, null);
                    return null;
                }
            } catch (IllegalArgumentException ie) {
                enumNamesList = getListOfEnums();
                enumNamesList.append(" <qname-but-not-ncname>");
                str = XSLTErrorResources.INVALID_ENUM;
                objArr = new Object[T_AVT];
                objArr[FATAL] = name;
                objArr[T_CDATA] = value;
                objArr[WARNING] = enumNamesList.toString();
                handleError(handler, str, objArr, ie);
                return null;
            } catch (RuntimeException re) {
                enumNamesList = getListOfEnums();
                enumNamesList.append(" <qname-but-not-ncname>");
                str = XSLTErrorResources.INVALID_ENUM;
                objArr = new Object[T_AVT];
                objArr[FATAL] = name;
                objArr[T_CDATA] = value;
                objArr[WARNING] = enumNamesList.toString();
                handleError(handler, str, objArr, re);
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
        String str;
        Object[] objArr;
        if (getSupportsAVT()) {
            try {
                AVT avt = new AVT(handler, uri, name, rawName, value, owner);
                if (!avt.isSimple() || XML11Char.isXML11ValidNmtoken(value)) {
                    return avt;
                }
                str = XSLTErrorResources.INVALID_NMTOKEN;
                objArr = new Object[WARNING];
                objArr[FATAL] = name;
                objArr[T_CDATA] = value;
                handleError(handler, str, objArr, null);
                return null;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        } else if (XML11Char.isXML11ValidNmtoken(value)) {
            return value;
        } else {
            str = XSLTErrorResources.INVALID_NMTOKEN;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, null);
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
        String str;
        Object[] objArr;
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
                    str = XSLTErrorResources.INVALID_NUMBER;
                    objArr = new Object[WARNING];
                    objArr[FATAL] = name;
                    objArr[T_CDATA] = value;
                    handleError(handler, str, objArr, nfe);
                    return null;
                }
            } catch (TransformerException e3) {
                te = e3;
                avt = null;
                throw new SAXException(te);
            } catch (NumberFormatException e4) {
                nfe = e4;
                avt = null;
                str = XSLTErrorResources.INVALID_NUMBER;
                objArr = new Object[WARNING];
                objArr[FATAL] = name;
                objArr[T_CDATA] = value;
                handleError(handler, str, objArr, nfe);
                return null;
            }
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException nfe2) {
            str = XSLTErrorResources.INVALID_NUMBER;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, nfe2);
            return null;
        }
    }

    Object processQNAME(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        String str;
        Object[] objArr;
        try {
            return new QName(value, (PrefixResolver) handler, true);
        } catch (IllegalArgumentException ie) {
            str = XSLTErrorResources.INVALID_QNAME;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, ie);
            return null;
        } catch (RuntimeException re) {
            str = XSLTErrorResources.INVALID_QNAME;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, re);
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
                    String str;
                    Object[] objArr;
                    if (indexOfNSSep < 0 || XML11Char.isXML11ValidNCName(value.substring(FATAL, indexOfNSSep))) {
                        String localName;
                        if (indexOfNSSep < 0) {
                            localName = value;
                        } else {
                            localName = value.substring(indexOfNSSep + T_CDATA);
                        }
                        if (localName == null || localName.length() == 0 || !XML11Char.isXML11ValidNCName(localName)) {
                            str = XSLTErrorResources.INVALID_QNAME;
                            objArr = new Object[WARNING];
                            objArr[FATAL] = name;
                            objArr[T_CDATA] = value;
                            handleError(handler, str, objArr, null);
                            return null;
                        }
                    }
                    str = XSLTErrorResources.INVALID_QNAME;
                    objArr = new Object[WARNING];
                    objArr[FATAL] = name;
                    objArr[T_CDATA] = value;
                    handleError(handler, str, objArr, null);
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
        String str;
        Object[] objArr;
        if (getSupportsAVT()) {
            AVT avt;
            try {
                avt = new AVT(handler, uri, name, rawName, value, owner);
                try {
                    if (!avt.isSimple() || XML11Char.isXML11ValidNCName(value)) {
                        return avt;
                    }
                    str = XSLTErrorResources.INVALID_NCNAME;
                    objArr = new Object[WARNING];
                    objArr[FATAL] = name;
                    objArr[T_CDATA] = value;
                    handleError(handler, str, objArr, null);
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
            str = XSLTErrorResources.INVALID_NCNAME;
            objArr = new Object[WARNING];
            objArr[FATAL] = name;
            objArr[T_CDATA] = value;
            handleError(handler, str, objArr, null);
            return null;
        }
    }

    Vector processQNAMES(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nQNames = tokenizer.countTokens();
        Vector qnames = new Vector(nQNames);
        for (int i = FATAL; i < nQNames; i += T_CDATA) {
            qnames.addElement(new QName(tokenizer.nextToken(), (PrefixResolver) handler));
        }
        return qnames;
    }

    final Vector processQNAMESRNU(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nQNames = tokenizer.countTokens();
        Vector qnames = new Vector(nQNames);
        String defaultURI = handler.getNamespaceForPrefix(SerializerConstants.EMPTYSTRING);
        for (int i = FATAL; i < nQNames; i += T_CDATA) {
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
            for (int i = FATAL; i < nPatterns; i += T_CDATA) {
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
        for (int i = FATAL; i < nStrings; i += T_CDATA) {
            strings.addElement(tokenizer.nextToken());
        }
        return strings;
    }

    StringVector processPREFIX_URLLIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        int i = FATAL;
        while (i < nStrings) {
            String prefix = tokenizer.nextToken();
            String url = handler.getNamespaceForPrefix(prefix);
            if (url != null) {
                strings.addElement(url);
                i += T_CDATA;
            } else {
                String str = XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX;
                Object[] objArr = new Object[T_CDATA];
                objArr[FATAL] = prefix;
                throw new SAXException(XSLMessages.createMessage(str, objArr));
            }
        }
        return strings;
    }

    StringVector processPREFIX_LIST(StylesheetHandler handler, String uri, String name, String rawName, String value) throws SAXException {
        StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f");
        int nStrings = tokenizer.countTokens();
        StringVector strings = new StringVector(nStrings);
        int i = FATAL;
        while (i < nStrings) {
            String prefix = tokenizer.nextToken();
            String url = handler.getNamespaceForPrefix(prefix);
            if (prefix.equals(Constants.DEFAULT_DECIMAL_FORMAT) || url != null) {
                strings.addElement(prefix);
                i += T_CDATA;
            } else {
                String str = XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX;
                Object[] objArr = new Object[T_CDATA];
                objArr[FATAL] = prefix;
                throw new SAXException(XSLMessages.createMessage(str, objArr));
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
        String str = XSLTErrorResources.INVALID_BOOLEAN;
        Object[] objArr = new Object[WARNING];
        objArr[FATAL] = name;
        objArr[T_CDATA] = value;
        handleError(handler, str, objArr, null);
        return null;
    }

    Object processValue(StylesheetHandler handler, String uri, String name, String rawName, String value, ElemTemplateElement owner) throws SAXException {
        switch (getType()) {
            case T_CDATA /*1*/:
                return processCDATA(handler, uri, name, rawName, value, owner);
            case WARNING /*2*/:
                return processURL(handler, uri, name, rawName, value, owner);
            case T_AVT /*3*/:
                return processAVT(handler, uri, name, rawName, value, owner);
            case T_PATTERN /*4*/:
                return processPATTERN(handler, uri, name, rawName, value, owner);
            case T_EXPR /*5*/:
                return processEXPR(handler, uri, name, rawName, value, owner);
            case T_CHAR /*6*/:
                return processCHAR(handler, uri, name, rawName, value, owner);
            case T_NUMBER /*7*/:
                return processNUMBER(handler, uri, name, rawName, value, owner);
            case T_YESNO /*8*/:
                return processYESNO(handler, uri, name, rawName, value);
            case T_QNAME /*9*/:
                return processQNAME(handler, uri, name, rawName, value, owner);
            case T_QNAMES /*10*/:
                return processQNAMES(handler, uri, name, rawName, value);
            case T_ENUM /*11*/:
                return processENUM(handler, uri, name, rawName, value, owner);
            case T_SIMPLEPATTERNLIST /*12*/:
                return processSIMPLEPATTERNLIST(handler, uri, name, rawName, value, owner);
            case T_NMTOKEN /*13*/:
                return processNMTOKEN(handler, uri, name, rawName, value, owner);
            case T_STRINGLIST /*14*/:
                return processSTRINGLIST(handler, uri, name, rawName, value);
            case T_PREFIX_URLLIST /*15*/:
                return processPREFIX_URLLIST(handler, uri, name, rawName, value);
            case T_ENUM_OR_PQNAME /*16*/:
                return processENUM_OR_PQNAME(handler, uri, name, rawName, value, owner);
            case T_NCNAME /*17*/:
                return processNCNAME(handler, uri, name, rawName, value, owner);
            case T_AVT_QNAME /*18*/:
                return processAVT_QNAME(handler, uri, name, rawName, value, owner);
            case T_QNAMES_RESOLVE_NULL /*19*/:
                return processQNAMESRNU(handler, uri, name, rawName, value);
            case T_PREFIXLIST /*20*/:
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
        for (int i = FATAL; i < enumValues.length; i += T_CDATA) {
            if (i > 0) {
                enumNamesList.append(' ');
            }
            enumNamesList.append(enumValues[i]);
        }
        return enumNamesList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean setAttrValue(StylesheetHandler handler, String attrUri, String attrLocalName, String attrRawName, String attrValue, ElemTemplateElement elem) throws SAXException {
        String str;
        Object[] objArr;
        if (!attrRawName.equals(SerializerConstants.XMLNS_PREFIX)) {
            if (!attrRawName.startsWith(Constants.ATTRNAME_XMLNS)) {
                String setterString = getSetterMethodName();
                if (setterString != null) {
                    try {
                        Method meth;
                        Object[] args;
                        Class[] argTypes;
                        if (setterString.equals(S_FOREIGNATTR_SETTER)) {
                            if (attrUri == null) {
                                attrUri = SerializerConstants.EMPTYSTRING;
                            }
                            Class sclass = attrUri.getClass();
                            argTypes = new Class[T_PATTERN];
                            argTypes[FATAL] = sclass;
                            argTypes[T_CDATA] = sclass;
                            argTypes[WARNING] = sclass;
                            argTypes[T_AVT] = sclass;
                            meth = elem.getClass().getMethod(setterString, argTypes);
                            args = new Object[T_PATTERN];
                            args[FATAL] = attrUri;
                            args[T_CDATA] = attrLocalName;
                            args[WARNING] = attrRawName;
                            args[T_AVT] = attrValue;
                        } else {
                            Object value = processValue(handler, attrUri, attrLocalName, attrRawName, attrValue, elem);
                            if (value == null) {
                                return false;
                            }
                            argTypes = new Class[T_CDATA];
                            argTypes[FATAL] = getPrimativeClass(value);
                            meth = elem.getClass().getMethod(setterString, argTypes);
                            args = new Object[T_CDATA];
                            args[FATAL] = value;
                        }
                        meth.invoke(elem, args);
                    } catch (NoSuchMethodException nsme) {
                        if (!setterString.equals(S_FOREIGNATTR_SETTER)) {
                            str = XSLTErrorResources.ER_FAILED_CALLING_METHOD;
                            objArr = new Object[T_CDATA];
                            objArr[FATAL] = setterString;
                            handler.error(str, objArr, nsme);
                            return false;
                        }
                    } catch (IllegalAccessException iae) {
                        str = XSLTErrorResources.ER_FAILED_CALLING_METHOD;
                        objArr = new Object[T_CDATA];
                        objArr[FATAL] = setterString;
                        handler.error(str, objArr, iae);
                        return false;
                    } catch (InvocationTargetException nsme2) {
                        str = XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE;
                        objArr = new Object[WARNING];
                        objArr[FATAL] = Keywords.FUNC_NAME_STRING;
                        objArr[T_CDATA] = getName();
                        handleError(handler, str, objArr, nsme2);
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private void handleError(StylesheetHandler handler, String msg, Object[] args, Exception exc) throws SAXException {
        switch (getErrorType()) {
            case FATAL /*0*/:
            case T_CDATA /*1*/:
                handler.error(msg, args, exc);
            case WARNING /*2*/:
                handler.warn(msg, args);
            default:
        }
    }
}
