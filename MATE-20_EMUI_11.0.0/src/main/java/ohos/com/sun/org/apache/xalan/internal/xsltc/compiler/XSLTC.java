package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.com.sun.org.apache.xalan.internal.XalanConstants;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.global.icu.text.SymbolTable;
import ohos.jdk.xml.internal.JdkXmlFeatures;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.XMLReader;

public final class XSLTC {
    public static final int BYTEARRAY_AND_FILE_OUTPUT = 4;
    public static final int BYTEARRAY_AND_JAR_OUTPUT = 5;
    public static final int BYTEARRAY_OUTPUT = 2;
    public static final int CLASSLOADER_OUTPUT = 3;
    public static final int FILE_OUTPUT = 0;
    public static final int JAR_OUTPUT = 1;
    private String _accessExternalDTD = "all";
    private String _accessExternalStylesheet = "all";
    private int _attributeSetSerial = 0;
    private Map<String, Integer> _attributes;
    private Vector _bcelClasses;
    private boolean _callsNodeset = false;
    private String _className = null;
    private Vector _classes;
    private boolean _debug = false;
    private File _destDir = null;
    private Map<String, Integer> _elements;
    private ClassLoader _extensionClassLoader;
    private final Map<String, Class> _externalExtensionFunctions;
    private boolean _hasIdCall = false;
    private int _helperClassSerial = 0;
    private boolean _isSecureProcessing = false;
    private String _jarFileName = null;
    private SourceLoader _loader = null;
    private int _modeSerial = 1;
    private boolean _multiDocument = false;
    private Vector _namesIndex;
    private Vector _namespaceIndex;
    private Map<String, Integer> _namespacePrefixes;
    private Map<String, Integer> _namespaces;
    private int _nextGType;
    private int _nextNSType;
    private int[] _numberFieldIndexes;
    private int _outputType = 0;
    private boolean _overrideDefaultParser;
    private String _packageName = null;
    private Parser _parser;
    private XMLReader _reader = null;
    private int _stepPatternSerial = 1;
    private Stylesheet _stylesheet;
    private int _stylesheetSerial = 1;
    private boolean _templateInlining = false;
    private final JdkXmlFeatures _xmlFeatures;
    private XMLSecurityManager _xmlSecurityManager;
    private Vector m_characterData;

    public XSLTC(JdkXmlFeatures jdkXmlFeatures) {
        this._overrideDefaultParser = jdkXmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER);
        this._parser = new Parser(this, this._overrideDefaultParser);
        this._xmlFeatures = jdkXmlFeatures;
        this._extensionClassLoader = null;
        this._externalExtensionFunctions = new HashMap();
    }

    public void setSecureProcessing(boolean z) {
        this._isSecureProcessing = z;
    }

    public boolean isSecureProcessing() {
        return this._isSecureProcessing;
    }

    public boolean getFeature(JdkXmlFeatures.XmlFeature xmlFeature) {
        return this._xmlFeatures.getFeature(xmlFeature);
    }

    public Object getProperty(String str) {
        if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet")) {
            return this._accessExternalStylesheet;
        }
        if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD")) {
            return this._accessExternalDTD;
        }
        if (str.equals("http://apache.org/xml/properties/security-manager")) {
            return this._xmlSecurityManager;
        }
        if (str.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)) {
            return this._extensionClassLoader;
        }
        return null;
    }

    public void setProperty(String str, Object obj) {
        if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet")) {
            this._accessExternalStylesheet = (String) obj;
        } else if (str.equals("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD")) {
            this._accessExternalDTD = (String) obj;
        } else if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this._xmlSecurityManager = (XMLSecurityManager) obj;
        } else if (str.equals(XalanConstants.JDK_EXTENSION_CLASSLOADER)) {
            this._extensionClassLoader = (ClassLoader) obj;
            this._externalExtensionFunctions.clear();
        }
    }

    public Parser getParser() {
        return this._parser;
    }

    public void setOutputType(int i) {
        this._outputType = i;
    }

    public Properties getOutputProperties() {
        return this._parser.getOutputProperties();
    }

    public void init() {
        reset();
        this._reader = null;
        this._classes = new Vector();
        this._bcelClasses = new Vector();
    }

    private void setExternalExtensionFunctions(String str, Class cls) {
        if (this._isSecureProcessing && cls != null && !this._externalExtensionFunctions.containsKey(str)) {
            this._externalExtensionFunctions.put(str, cls);
        }
    }

    /* access modifiers changed from: package-private */
    public Class loadExternalFunction(String str) throws ClassNotFoundException {
        Class cls;
        if (this._externalExtensionFunctions.containsKey(str)) {
            cls = this._externalExtensionFunctions.get(str);
        } else {
            ClassLoader classLoader = this._extensionClassLoader;
            if (classLoader != null) {
                Class<?> cls2 = Class.forName(str, true, classLoader);
                setExternalExtensionFunctions(str, cls2);
                cls = cls2;
            } else {
                cls = null;
            }
        }
        if (cls != null) {
            return cls;
        }
        throw new ClassNotFoundException(str);
    }

    public Map<String, Class> getExternalExtensionFunctions() {
        return Collections.unmodifiableMap(this._externalExtensionFunctions);
    }

    private void reset() {
        this._nextGType = 14;
        this._elements = new HashMap();
        this._attributes = new HashMap();
        this._namespaces = new HashMap();
        this._namespaces.put("", new Integer(this._nextNSType));
        this._namesIndex = new Vector(128);
        this._namespaceIndex = new Vector(32);
        this._namespacePrefixes = new HashMap();
        this._stylesheet = null;
        this._parser.init();
        this._modeSerial = 1;
        this._stylesheetSerial = 1;
        this._stepPatternSerial = 1;
        this._helperClassSerial = 0;
        this._attributeSetSerial = 0;
        this._multiDocument = false;
        this._hasIdCall = false;
        this._numberFieldIndexes = new int[]{-1, -1, -1};
        this._externalExtensionFunctions.clear();
    }

    public void setSourceLoader(SourceLoader sourceLoader) {
        this._loader = sourceLoader;
    }

    public void setTemplateInlining(boolean z) {
        this._templateInlining = z;
    }

    public boolean getTemplateInlining() {
        return this._templateInlining;
    }

    public void setPIParameters(String str, String str2, String str3) {
        this._parser.setPIParameters(str, str2, str3);
    }

    public boolean compile(URL url) {
        try {
            InputSource inputSource = new InputSource(url.openStream());
            inputSource.setSystemId(url.toString());
            return compile(inputSource, this._className);
        } catch (IOException e) {
            this._parser.reportError(2, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e));
            return false;
        }
    }

    public boolean compile(URL url, String str) {
        try {
            InputSource inputSource = new InputSource(url.openStream());
            inputSource.setSystemId(url.toString());
            return compile(inputSource, str);
        } catch (IOException e) {
            this._parser.reportError(2, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e));
            return false;
        }
    }

    public boolean compile(InputStream inputStream, String str) {
        InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(str);
        return compile(inputSource, str);
    }

    public boolean compile(InputSource inputSource, String str) {
        SyntaxTreeNode syntaxTreeNode;
        try {
            reset();
            String systemId = inputSource != null ? inputSource.getSystemId() : null;
            if (this._className == null) {
                if (str != null) {
                    setClassName(str);
                } else if (systemId != null && !systemId.equals("")) {
                    setClassName(Util.baseName(systemId));
                }
                if (this._className == null || this._className.length() == 0) {
                    setClassName("GregorSamsa");
                }
            }
            if (this._reader == null) {
                syntaxTreeNode = this._parser.parse(inputSource);
            } else {
                syntaxTreeNode = this._parser.parse(this._reader, inputSource);
            }
            if (!this._parser.errorsFound() && syntaxTreeNode != null) {
                this._stylesheet = this._parser.makeStylesheet(syntaxTreeNode);
                this._stylesheet.setSourceLoader(this._loader);
                this._stylesheet.setSystemId(systemId);
                this._stylesheet.setParentStylesheet(null);
                this._stylesheet.setTemplateInlining(this._templateInlining);
                this._parser.setCurrentStylesheet(this._stylesheet);
                this._parser.createAST(this._stylesheet);
            }
            if (!this._parser.errorsFound() && this._stylesheet != null) {
                this._stylesheet.setCallsNodeset(this._callsNodeset);
                this._stylesheet.setMultiDocument(this._multiDocument);
                this._stylesheet.setHasIdCall(this._hasIdCall);
                synchronized (getClass()) {
                    this._stylesheet.translate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this._parser.reportError(2, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e));
        } catch (Error e2) {
            if (this._debug) {
                e2.printStackTrace();
            }
            this._parser.reportError(2, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e2));
        } catch (Throwable th) {
            this._reader = null;
            throw th;
        }
        this._reader = null;
        return !this._parser.errorsFound();
    }

    public boolean compile(Vector vector) {
        int size = vector.size();
        if (size == 0) {
            return true;
        }
        if (size == 1) {
            Object firstElement = vector.firstElement();
            if (firstElement instanceof URL) {
                return compile((URL) firstElement);
            }
            return false;
        }
        Enumeration elements = vector.elements();
        while (elements.hasMoreElements()) {
            this._className = null;
            Object nextElement = elements.nextElement();
            if ((nextElement instanceof URL) && !compile((URL) nextElement)) {
                return false;
            }
        }
        return true;
    }

    public byte[][] getBytecodes() {
        int size = this._classes.size();
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, size, 1);
        for (int i = 0; i < size; i++) {
            bArr[i] = (byte[]) this._classes.elementAt(i);
        }
        return bArr;
    }

    public byte[][] compile(String str, InputSource inputSource, int i) {
        this._outputType = i;
        if (compile(inputSource, str)) {
            return getBytecodes();
        }
        return null;
    }

    public byte[][] compile(String str, InputSource inputSource) {
        return compile(str, inputSource, 2);
    }

    public void setXMLReader(XMLReader xMLReader) {
        this._reader = xMLReader;
    }

    public XMLReader getXMLReader() {
        return this._reader;
    }

    public ArrayList<ErrorMsg> getErrors() {
        return this._parser.getErrors();
    }

    public ArrayList<ErrorMsg> getWarnings() {
        return this._parser.getWarnings();
    }

    public void printErrors() {
        this._parser.printErrors();
    }

    public void printWarnings() {
        this._parser.printWarnings();
    }

    /* access modifiers changed from: protected */
    public void setMultiDocument(boolean z) {
        this._multiDocument = z;
    }

    public boolean isMultiDocument() {
        return this._multiDocument;
    }

    /* access modifiers changed from: protected */
    public void setCallsNodeset(boolean z) {
        if (z) {
            setMultiDocument(z);
        }
        this._callsNodeset = z;
    }

    public boolean callsNodeset() {
        return this._callsNodeset;
    }

    /* access modifiers changed from: protected */
    public void setHasIdCall(boolean z) {
        this._hasIdCall = z;
    }

    public boolean hasIdCall() {
        return this._hasIdCall;
    }

    public void setClassName(String str) {
        String javaName = Util.toJavaName(Util.noExtName(Util.baseName(str)));
        if (this._packageName == null) {
            this._className = javaName;
            return;
        }
        this._className = this._packageName + '.' + javaName;
    }

    public String getClassName() {
        return this._className;
    }

    private String classFileName(String str) {
        return str.replace('.', File.separatorChar) + ".class";
    }

    private File getOutputFile(String str) {
        File file = this._destDir;
        if (file != null) {
            return new File(file, classFileName(str));
        }
        return new File(classFileName(str));
    }

    public boolean setDestDirectory(String str) {
        File file = new File(str);
        if (SecuritySupport.getFileExists(file) || file.mkdirs()) {
            this._destDir = file;
            return true;
        }
        this._destDir = null;
        return false;
    }

    public void setPackageName(String str) {
        this._packageName = str;
        String str2 = this._className;
        if (str2 != null) {
            setClassName(str2);
        }
    }

    public void setJarFileName(String str) {
        if (str.endsWith(".jar")) {
            this._jarFileName = str;
        } else {
            this._jarFileName = str + ".jar";
        }
        this._outputType = 1;
    }

    public String getJarFileName() {
        return this._jarFileName;
    }

    public void setStylesheet(Stylesheet stylesheet) {
        if (this._stylesheet == null) {
            this._stylesheet = stylesheet;
        }
    }

    public Stylesheet getStylesheet() {
        return this._stylesheet;
    }

    public int registerAttribute(QName qName) {
        Integer num = this._attributes.get(qName.toString());
        if (num == null) {
            int i = this._nextGType;
            this._nextGType = i + 1;
            num = Integer.valueOf(i);
            this._attributes.put(qName.toString(), num);
            String namespace = qName.getNamespace();
            String str = "@" + qName.getLocalPart();
            if (namespace == null || namespace.equals("")) {
                this._namesIndex.addElement(str);
            } else {
                this._namesIndex.addElement(namespace + ":" + str);
            }
            if (qName.getLocalPart().equals("*")) {
                registerNamespace(qName.getNamespace());
            }
        }
        return num.intValue();
    }

    public int registerElement(QName qName) {
        Integer num = this._elements.get(qName.toString());
        if (num == null) {
            Map<String, Integer> map = this._elements;
            String qName2 = qName.toString();
            int i = this._nextGType;
            this._nextGType = i + 1;
            Integer valueOf = Integer.valueOf(i);
            map.put(qName2, valueOf);
            this._namesIndex.addElement(qName.toString());
            num = valueOf;
        }
        if (qName.getLocalPart().equals("*")) {
            registerNamespace(qName.getNamespace());
        }
        return num.intValue();
    }

    public int registerNamespacePrefix(QName qName) {
        Integer num = this._namespacePrefixes.get(qName.toString());
        if (num == null) {
            int i = this._nextGType;
            this._nextGType = i + 1;
            num = Integer.valueOf(i);
            this._namespacePrefixes.put(qName.toString(), num);
            String namespace = qName.getNamespace();
            if (namespace == null || namespace.equals("")) {
                Vector vector = this._namesIndex;
                vector.addElement("?" + qName.getLocalPart());
            } else {
                this._namesIndex.addElement("?");
            }
        }
        return num.intValue();
    }

    public int registerNamespace(String str) {
        Integer num = this._namespaces.get(str);
        if (num == null) {
            int i = this._nextNSType;
            this._nextNSType = i + 1;
            num = Integer.valueOf(i);
            this._namespaces.put(str, num);
            this._namespaceIndex.addElement(str);
        }
        return num.intValue();
    }

    public int nextModeSerial() {
        int i = this._modeSerial;
        this._modeSerial = i + 1;
        return i;
    }

    public int nextStylesheetSerial() {
        int i = this._stylesheetSerial;
        this._stylesheetSerial = i + 1;
        return i;
    }

    public int nextStepPatternSerial() {
        int i = this._stepPatternSerial;
        this._stepPatternSerial = i + 1;
        return i;
    }

    public int[] getNumberFieldIndexes() {
        return this._numberFieldIndexes;
    }

    public int nextHelperClassSerial() {
        int i = this._helperClassSerial;
        this._helperClassSerial = i + 1;
        return i;
    }

    public int nextAttributeSetSerial() {
        int i = this._attributeSetSerial;
        this._attributeSetSerial = i + 1;
        return i;
    }

    public Vector getNamesIndex() {
        return this._namesIndex;
    }

    public Vector getNamespaceIndex() {
        return this._namespaceIndex;
    }

    public String getHelperClassName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClassName());
        sb.append(SymbolTable.SYMBOL_REF);
        int i = this._helperClassSerial;
        this._helperClassSerial = i + 1;
        sb.append(i);
        return sb.toString();
    }

    public void dumpClass(JavaClass javaClass) {
        String parent;
        int i = this._outputType;
        if ((i == 0 || i == 4) && (parent = getOutputFile(javaClass.getClassName()).getParent()) != null) {
            File file = new File(parent);
            if (!SecuritySupport.getFileExists(file)) {
                file.mkdirs();
            }
        }
        try {
            int i2 = this._outputType;
            if (i2 == 0) {
                javaClass.dump(new BufferedOutputStream(new FileOutputStream(getOutputFile(javaClass.getClassName()))));
            } else if (i2 == 1) {
                this._bcelClasses.addElement(javaClass);
            } else if (i2 == 2 || i2 == 3 || i2 == 4 || i2 == 5) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
                javaClass.dump(byteArrayOutputStream);
                this._classes.addElement(byteArrayOutputStream.toByteArray());
                if (this._outputType == 4) {
                    javaClass.dump(new BufferedOutputStream(new FileOutputStream(getOutputFile(javaClass.getClassName()))));
                } else if (this._outputType == 5) {
                    this._bcelClasses.addElement(javaClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String entryName(File file) throws IOException {
        return file.getName().replace(File.separatorChar, '/');
    }

    public void outputToJar() throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.2");
        Map<String, Attributes> entries = manifest.getEntries();
        Enumeration elements = this._bcelClasses.elements();
        String date = new Date().toString();
        Attributes.Name name = new Attributes.Name("Date");
        while (elements.hasMoreElements()) {
            String replace = ((JavaClass) elements.nextElement()).getClassName().replace('.', '/');
            Attributes attributes = new Attributes();
            attributes.put(name, date);
            entries.put(replace + ".class", attributes);
        }
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(new File(this._destDir, this._jarFileName)), manifest);
        Enumeration elements2 = this._bcelClasses.elements();
        while (elements2.hasMoreElements()) {
            JavaClass javaClass = (JavaClass) elements2.nextElement();
            String replace2 = javaClass.getClassName().replace('.', '/');
            jarOutputStream.putNextEntry(new JarEntry(replace2 + ".class"));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
            javaClass.dump(byteArrayOutputStream);
            byteArrayOutputStream.writeTo(jarOutputStream);
        }
        jarOutputStream.close();
    }

    public void setDebug(boolean z) {
        this._debug = z;
    }

    public boolean debug() {
        return this._debug;
    }

    public String getCharacterData(int i) {
        return ((StringBuffer) this.m_characterData.elementAt(i)).toString();
    }

    public int getCharacterDataCount() {
        Vector vector = this.m_characterData;
        if (vector != null) {
            return vector.size();
        }
        return 0;
    }

    public int addCharacterData(String str) {
        StringBuffer stringBuffer;
        Vector vector = this.m_characterData;
        if (vector == null) {
            this.m_characterData = new Vector();
            stringBuffer = new StringBuffer();
            this.m_characterData.addElement(stringBuffer);
        } else {
            stringBuffer = (StringBuffer) vector.elementAt(vector.size() - 1);
        }
        if (str.length() + stringBuffer.length() > 21845) {
            stringBuffer = new StringBuffer();
            this.m_characterData.addElement(stringBuffer);
        }
        int length = stringBuffer.length();
        stringBuffer.append(str);
        return length;
    }
}
