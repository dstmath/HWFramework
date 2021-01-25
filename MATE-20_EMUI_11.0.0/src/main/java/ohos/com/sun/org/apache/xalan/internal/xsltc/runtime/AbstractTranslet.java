package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.KeyIndex;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.Templates;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;

public abstract class AbstractTranslet implements Translet {
    public static final int CURRENT_TRANSLET_VERSION = 101;
    private static final String EMPTYSTRING = "";
    public static final int FIRST_TRANSLET_VERSION = 100;
    private static final String ID_INDEX_NAME = "##id";
    public static final int VER_SPLIT_NAMES_ARRAY = 101;
    private String _accessExternalStylesheet = "all";
    private Map<String, Class<?>> _auxClasses = null;
    public Vector _cdata = null;
    private int _currentRootForKeys = 0;
    public String _doctypePublic = null;
    public String _doctypeSystem = null;
    private DOMCache _domCache = null;
    protected DOMImplementation _domImplementation = null;
    private KeyIndex _emptyKeyIndex = null;
    public String _encoding = "UTF-8";
    public Map<String, DecimalFormat> _formatSymbols = null;
    protected boolean _hasIdCall = false;
    public boolean _indent = false;
    public int _indentamount = -1;
    private int _indexSize = 0;
    public boolean _isStandalone = false;
    private Map<String, KeyIndex> _keyIndexes = null;
    public String _mediaType = null;
    public String _method = null;
    private MessageHandler _msgHandler = null;
    public boolean _omitHeader = false;
    private boolean _overrideDefaultParser;
    public String _standalone = null;
    protected Templates _templates = null;
    public String _version = "1.0";
    protected String[] namesArray;
    protected String[] namespaceArray;
    protected ArrayList paramsStack = new ArrayList();
    protected int pbase = 0;
    protected int pframe = 0;
    protected StringValueHandler stringValueHandler = new StringValueHandler();
    protected int transletVersion = 100;
    protected int[] typesArray;
    protected String[] urisArray;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public void buildKeys(DOM dom, DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler, int i) throws TransletException {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public abstract void transform(DOM dom, DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException;

    public void printInternalState() {
        System.out.println("-------------------------------------");
        PrintStream printStream = System.out;
        printStream.println("AbstractTranslet this = " + this);
        PrintStream printStream2 = System.out;
        printStream2.println("pbase = " + this.pbase);
        PrintStream printStream3 = System.out;
        printStream3.println("vframe = " + this.pframe);
        PrintStream printStream4 = System.out;
        printStream4.println("paramsStack.size() = " + this.paramsStack.size());
        PrintStream printStream5 = System.out;
        printStream5.println("namesArray.size = " + this.namesArray.length);
        PrintStream printStream6 = System.out;
        printStream6.println("namespaceArray.size = " + this.namespaceArray.length);
        System.out.println("");
        PrintStream printStream7 = System.out;
        printStream7.println("Total memory = " + Runtime.getRuntime().totalMemory());
    }

    public final DOMAdapter makeDOMAdapter(DOM dom) throws TransletException {
        setRootForKeys(dom.getDocument());
        return new DOMAdapter(dom, this.namesArray, this.urisArray, this.typesArray, this.namespaceArray);
    }

    public final void pushParamFrame() {
        this.paramsStack.add(this.pframe, new Integer(this.pbase));
        int i = this.pframe + 1;
        this.pframe = i;
        this.pbase = i;
    }

    public final void popParamFrame() {
        int i = this.pbase;
        if (i > 0) {
            ArrayList arrayList = this.paramsStack;
            int i2 = i - 1;
            this.pbase = i2;
            int intValue = ((Integer) arrayList.get(i2)).intValue();
            int i3 = this.pframe;
            while (true) {
                i3--;
                int i4 = this.pbase;
                if (i3 >= i4) {
                    this.paramsStack.remove(i3);
                } else {
                    this.pframe = i4;
                    this.pbase = intValue;
                    return;
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public final Object addParameter(String str, Object obj) {
        return addParameter(BasisLibrary.mapQNameToJavaName(str), obj, false);
    }

    public final Object addParameter(String str, Object obj, boolean z) {
        int i = this.pframe;
        while (true) {
            i--;
            if (i >= this.pbase) {
                Parameter parameter = (Parameter) this.paramsStack.get(i);
                if (parameter._name.equals(str)) {
                    if (!parameter._isDefault && z) {
                        return parameter._value;
                    }
                    parameter._value = obj;
                    parameter._isDefault = z;
                    return obj;
                }
            } else {
                ArrayList arrayList = this.paramsStack;
                int i2 = this.pframe;
                this.pframe = i2 + 1;
                arrayList.add(i2, new Parameter(str, obj, z));
                return obj;
            }
        }
    }

    public void clearParameters() {
        this.pframe = 0;
        this.pbase = 0;
        this.paramsStack.clear();
    }

    public final Object getParameter(String str) {
        String mapQNameToJavaName = BasisLibrary.mapQNameToJavaName(str);
        int i = this.pframe;
        while (true) {
            i--;
            if (i < this.pbase) {
                return null;
            }
            Parameter parameter = (Parameter) this.paramsStack.get(i);
            if (parameter._name.equals(mapQNameToJavaName)) {
                return parameter._value;
            }
        }
    }

    public final void setMessageHandler(MessageHandler messageHandler) {
        this._msgHandler = messageHandler;
    }

    public final void displayMessage(String str) {
        MessageHandler messageHandler = this._msgHandler;
        if (messageHandler == null) {
            System.err.println(str);
        } else {
            messageHandler.displayMessage(str);
        }
    }

    public void addDecimalFormat(String str, DecimalFormatSymbols decimalFormatSymbols) {
        if (this._formatSymbols == null) {
            this._formatSymbols = new HashMap();
        }
        if (str == null) {
            str = "";
        }
        DecimalFormat decimalFormat = new DecimalFormat();
        if (decimalFormatSymbols != null) {
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        }
        this._formatSymbols.put(str, decimalFormat);
    }

    public final DecimalFormat getDecimalFormat(String str) {
        if (this._formatSymbols == null) {
            return null;
        }
        if (str == null) {
            str = "";
        }
        DecimalFormat decimalFormat = this._formatSymbols.get(str);
        return decimalFormat == null ? this._formatSymbols.get("") : decimalFormat;
    }

    public final void prepassDocument(DOM dom) {
        setIndexSize(dom.getSize());
        buildIDIndex(dom);
    }

    private final void buildIDIndex(DOM dom) {
        setRootForKeys(dom.getDocument());
        if (dom instanceof DOMEnhancedForDTM) {
            DOMEnhancedForDTM dOMEnhancedForDTM = (DOMEnhancedForDTM) dom;
            if (dOMEnhancedForDTM.hasDOMSource()) {
                buildKeyIndex(ID_INDEX_NAME, dom);
                return;
            }
            Map<String, Integer> elementsWithIDs = dOMEnhancedForDTM.getElementsWithIDs();
            if (elementsWithIDs != null) {
                boolean z = false;
                for (Map.Entry<String, Integer> entry : elementsWithIDs.entrySet()) {
                    buildKeyIndex(ID_INDEX_NAME, dom.getNodeHandle(entry.getValue().intValue()), entry.getKey());
                    z = true;
                }
                if (z) {
                    setKeyIndexDom(ID_INDEX_NAME, dom);
                }
            }
        }
    }

    public final void postInitialization() {
        if (this.transletVersion < 101) {
            int length = this.namesArray.length;
            String[] strArr = new String[length];
            String[] strArr2 = new String[length];
            int[] iArr = new int[length];
            for (int i = 0; i < length; i++) {
                String str = this.namesArray[i];
                int lastIndexOf = str.lastIndexOf(58);
                int i2 = lastIndexOf + 1;
                if (lastIndexOf > -1) {
                    strArr[i] = str.substring(0, lastIndexOf);
                }
                if (str.charAt(i2) == '@') {
                    i2++;
                    iArr[i] = 2;
                } else if (str.charAt(i2) == '?') {
                    i2++;
                    iArr[i] = 13;
                } else {
                    iArr[i] = 1;
                }
                if (i2 != 0) {
                    str = str.substring(i2);
                }
                strArr2[i] = str;
            }
            this.namesArray = strArr2;
            this.urisArray = strArr;
            this.typesArray = iArr;
        }
        if (this.transletVersion > 101) {
            BasisLibrary.runTimeError(BasisLibrary.UNKNOWN_TRANSLET_VERSION_ERR, getClass().getName());
        }
    }

    public void setIndexSize(int i) {
        if (i > this._indexSize) {
            this._indexSize = i;
        }
    }

    public KeyIndex createKeyIndex() {
        return new KeyIndex(this._indexSize);
    }

    public void buildKeyIndex(String str, int i, String str2) {
        buildKeyIndexHelper(str).add(str2, i, this._currentRootForKeys);
    }

    public void buildKeyIndex(String str, DOM dom) {
        buildKeyIndexHelper(str).setDom(dom, dom.getDocument());
    }

    private KeyIndex buildKeyIndexHelper(String str) {
        if (this._keyIndexes == null) {
            this._keyIndexes = new HashMap();
        }
        KeyIndex keyIndex = this._keyIndexes.get(str);
        if (keyIndex != null) {
            return keyIndex;
        }
        Map<String, KeyIndex> map = this._keyIndexes;
        KeyIndex keyIndex2 = new KeyIndex(this._indexSize);
        map.put(str, keyIndex2);
        return keyIndex2;
    }

    public KeyIndex getKeyIndex(String str) {
        Map<String, KeyIndex> map = this._keyIndexes;
        if (map == null) {
            KeyIndex keyIndex = this._emptyKeyIndex;
            if (keyIndex != null) {
                return keyIndex;
            }
            KeyIndex keyIndex2 = new KeyIndex(1);
            this._emptyKeyIndex = keyIndex2;
            return keyIndex2;
        }
        KeyIndex keyIndex3 = map.get(str);
        if (keyIndex3 != null) {
            return keyIndex3;
        }
        KeyIndex keyIndex4 = this._emptyKeyIndex;
        if (keyIndex4 != null) {
            return keyIndex4;
        }
        KeyIndex keyIndex5 = new KeyIndex(1);
        this._emptyKeyIndex = keyIndex5;
        return keyIndex5;
    }

    private void setRootForKeys(int i) {
        this._currentRootForKeys = i;
    }

    public void setKeyIndexDom(String str, DOM dom) {
        getKeyIndex(str).setDom(dom, dom.getDocument());
    }

    public void setDOMCache(DOMCache dOMCache) {
        this._domCache = dOMCache;
    }

    public DOMCache getDOMCache() {
        return this._domCache;
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public SerializationHandler openOutputHandler(String str, boolean z) throws TransletException {
        try {
            TransletOutputHandlerFactory newInstance = TransletOutputHandlerFactory.newInstance(this._overrideDefaultParser);
            String parent = new File(str).getParent();
            if (parent != null && parent.length() > 0) {
                new File(parent).mkdirs();
            }
            newInstance.setEncoding(this._encoding);
            newInstance.setOutputMethod(this._method);
            newInstance.setOutputStream(new BufferedOutputStream(new FileOutputStream(str, z)));
            newInstance.setOutputType(0);
            SerializationHandler serializationHandler = newInstance.getSerializationHandler();
            transferOutputSettings(serializationHandler);
            serializationHandler.startDocument();
            return serializationHandler;
        } catch (Exception e) {
            throw new TransletException(e);
        }
    }

    public SerializationHandler openOutputHandler(String str) throws TransletException {
        return openOutputHandler(str, false);
    }

    public void closeOutputHandler(SerializationHandler serializationHandler) {
        try {
            serializationHandler.endDocument();
            serializationHandler.close();
        } catch (Exception unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public final void transform(DOM dom, SerializationHandler serializationHandler) throws TransletException {
        try {
            transform(dom, dom.getIterator(), serializationHandler);
        } finally {
            this._keyIndexes = null;
        }
    }

    /* JADX WARN: Type inference failed for: r1v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final void characters(String str, SerializationHandler serializationHandler) throws TransletException {
        if (str != null) {
            try {
                serializationHandler.characters(str);
            } catch (Exception e) {
                throw new TransletException(e);
            }
        }
    }

    public void addCdataElement(String str) {
        if (this._cdata == null) {
            this._cdata = new Vector();
        }
        int lastIndexOf = str.lastIndexOf(58);
        if (lastIndexOf > 0) {
            String substring = str.substring(0, lastIndexOf);
            String substring2 = str.substring(lastIndexOf + 1);
            this._cdata.addElement(substring);
            this._cdata.addElement(substring2);
            return;
        }
        this._cdata.addElement(null);
        this._cdata.addElement(str);
    }

    /* access modifiers changed from: protected */
    public void transferOutputSettings(SerializationHandler serializationHandler) {
        String str = this._method;
        if (str == null) {
            serializationHandler.setCdataSectionElements(this._cdata);
            String str2 = this._version;
            if (str2 != null) {
                serializationHandler.setVersion(str2);
            }
            String str3 = this._standalone;
            if (str3 != null) {
                serializationHandler.setStandalone(str3);
            }
            if (this._omitHeader) {
                serializationHandler.setOmitXMLDeclaration(true);
            }
            serializationHandler.setIndent(this._indent);
            serializationHandler.setDoctype(this._doctypeSystem, this._doctypePublic);
            serializationHandler.setIsStandalone(this._isStandalone);
        } else if (str.equals("xml")) {
            String str4 = this._standalone;
            if (str4 != null) {
                serializationHandler.setStandalone(str4);
            }
            if (this._omitHeader) {
                serializationHandler.setOmitXMLDeclaration(true);
            }
            serializationHandler.setCdataSectionElements(this._cdata);
            String str5 = this._version;
            if (str5 != null) {
                serializationHandler.setVersion(str5);
            }
            serializationHandler.setIndent(this._indent);
            serializationHandler.setIndentAmount(this._indentamount);
            String str6 = this._doctypeSystem;
            if (str6 != null) {
                serializationHandler.setDoctype(str6, this._doctypePublic);
            }
            serializationHandler.setIsStandalone(this._isStandalone);
        } else if (this._method.equals("html")) {
            serializationHandler.setIndent(this._indent);
            serializationHandler.setDoctype(this._doctypeSystem, this._doctypePublic);
            String str7 = this._mediaType;
            if (str7 != null) {
                serializationHandler.setMediaType(str7);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public void addAuxiliaryClass(Class cls) {
        if (this._auxClasses == null) {
            this._auxClasses = new HashMap();
        }
        this._auxClasses.put(cls.getName(), cls);
    }

    public void setAuxiliaryClasses(Map<String, Class<?>> map) {
        this._auxClasses = map;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public Class getAuxiliaryClass(String str) {
        Map<String, Class<?>> map = this._auxClasses;
        if (map == null) {
            return null;
        }
        return map.get(str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public String[] getNamesArray() {
        return this.namesArray;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public String[] getUrisArray() {
        return this.urisArray;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public int[] getTypesArray() {
        return this.typesArray;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public String[] getNamespaceArray() {
        return this.namespaceArray;
    }

    public boolean hasIdCall() {
        return this._hasIdCall;
    }

    public Templates getTemplates() {
        return this._templates;
    }

    public void setTemplates(Templates templates) {
        this._templates = templates;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public boolean overrideDefaultParser() {
        return this._overrideDefaultParser;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.Translet
    public void setOverrideDefaultParser(boolean z) {
        this._overrideDefaultParser = z;
    }

    public String getAllowedProtocols() {
        return this._accessExternalStylesheet;
    }

    public void setAllowedProtocols(String str) {
        this._accessExternalStylesheet = str;
    }

    public Document newDocument(String str, String str2) throws ParserConfigurationException {
        if (this._domImplementation == null) {
            this._domImplementation = JdkXmlUtils.getDOMFactory(this._overrideDefaultParser).newDocumentBuilder().getDOMImplementation();
        }
        return this._domImplementation.createDocument(str, str2, (DocumentType) null);
    }
}
