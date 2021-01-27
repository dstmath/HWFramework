package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.URIResolver;

public final class TemplatesImpl implements Templates, Serializable {
    private static String ABSTRACT_TRANSLET = "ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet";
    public static final String DESERIALIZE_TRANSLET = "jdk.xml.enableTemplatesImplDeserialization";
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("_name", String.class), new ObjectStreamField("_bytecodes", byte[][].class), new ObjectStreamField("_class", Class[].class), new ObjectStreamField("_transletIndex", Integer.TYPE), new ObjectStreamField("_outputProperties", Properties.class), new ObjectStreamField("_indentNumber", Integer.TYPE)};
    static final long serialVersionUID = 673094361519270707L;
    private transient String _accessExternalStylesheet = "all";
    private transient Map<String, Class<?>> _auxClasses = null;
    private byte[][] _bytecodes = null;
    private Class[] _class = null;
    private int _indentNumber;
    private String _name = null;
    private Properties _outputProperties;
    private transient boolean _overrideDefaultParser;
    private transient ThreadLocal _sdom = new ThreadLocal();
    private transient TransformerFactoryImpl _tfactory = null;
    private int _transletIndex = -1;
    private transient URIResolver _uriResolver = null;

    /* access modifiers changed from: package-private */
    public static final class TransletClassLoader extends ClassLoader {
        private final Map<String, Class> _loadedExternalExtensionFunctions;

        TransletClassLoader(ClassLoader classLoader) {
            super(classLoader);
            this._loadedExternalExtensionFunctions = null;
        }

        TransletClassLoader(ClassLoader classLoader, Map<String, Class> map) {
            super(classLoader);
            this._loadedExternalExtensionFunctions = map;
        }

        @Override // java.lang.ClassLoader
        public Class<?> loadClass(String str) throws ClassNotFoundException {
            Map<String, Class> map = this._loadedExternalExtensionFunctions;
            Class<?> cls = map != null ? map.get(str) : null;
            return cls == null ? super.loadClass(str) : cls;
        }

        /* access modifiers changed from: package-private */
        public Class defineClass(byte[] bArr) {
            return defineClass(null, bArr, 0, bArr.length);
        }
    }

    protected TemplatesImpl(byte[][] bArr, String str, Properties properties, int i, TransformerFactoryImpl transformerFactoryImpl) {
        this._bytecodes = bArr;
        init(str, properties, i, transformerFactoryImpl);
    }

    protected TemplatesImpl(Class[] clsArr, String str, Properties properties, int i, TransformerFactoryImpl transformerFactoryImpl) {
        this._class = clsArr;
        this._transletIndex = 0;
        init(str, properties, i, transformerFactoryImpl);
    }

    private void init(String str, Properties properties, int i, TransformerFactoryImpl transformerFactoryImpl) {
        this._name = str;
        this._outputProperties = properties;
        this._indentNumber = i;
        this._tfactory = transformerFactoryImpl;
        this._overrideDefaultParser = transformerFactoryImpl.overrideDefaultParser();
        this._accessExternalStylesheet = (String) transformerFactoryImpl.getAttribute("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet");
    }

    public TemplatesImpl() {
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        String systemProperty;
        if (System.getSecurityManager() == null || ((systemProperty = SecuritySupport.getSystemProperty(DESERIALIZE_TRANSLET)) != null && (systemProperty.length() == 0 || systemProperty.equalsIgnoreCase("true")))) {
            ObjectInputStream.GetField readFields = objectInputStream.readFields();
            this._name = (String) readFields.get("_name", (Object) null);
            this._bytecodes = (byte[][]) readFields.get("_bytecodes", (Object) null);
            this._class = (Class[]) readFields.get("_class", (Object) null);
            this._transletIndex = readFields.get("_transletIndex", -1);
            this._outputProperties = (Properties) readFields.get("_outputProperties", (Object) null);
            this._indentNumber = readFields.get("_indentNumber", 0);
            if (objectInputStream.readBoolean()) {
                this._uriResolver = (URIResolver) objectInputStream.readObject();
            }
            this._tfactory = new TransformerFactoryImpl();
            return;
        }
        throw new UnsupportedOperationException(new ErrorMsg(ErrorMsg.DESERIALIZE_TRANSLET_ERR).toString());
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        if (this._auxClasses == null) {
            ObjectOutputStream.PutField putFields = objectOutputStream.putFields();
            putFields.put("_name", this._name);
            putFields.put("_bytecodes", this._bytecodes);
            putFields.put("_class", this._class);
            putFields.put("_transletIndex", this._transletIndex);
            putFields.put("_outputProperties", this._outputProperties);
            putFields.put("_indentNumber", this._indentNumber);
            objectOutputStream.writeFields();
            if (this._uriResolver instanceof Serializable) {
                objectOutputStream.writeBoolean(true);
                objectOutputStream.writeObject(this._uriResolver);
                return;
            }
            objectOutputStream.writeBoolean(false);
            return;
        }
        throw new NotSerializableException("com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable");
    }

    public boolean overrideDefaultParser() {
        return this._overrideDefaultParser;
    }

    public synchronized void setURIResolver(URIResolver uRIResolver) {
        this._uriResolver = uRIResolver;
    }

    private synchronized void setTransletBytecodes(byte[][] bArr) {
        this._bytecodes = bArr;
    }

    private synchronized byte[][] getTransletBytecodes() {
        return this._bytecodes;
    }

    private synchronized Class[] getTransletClasses() {
        try {
            if (this._class == null) {
                defineTransletClasses();
            }
        } catch (TransformerConfigurationException unused) {
        }
        return this._class;
    }

    public synchronized int getTransletIndex() {
        try {
            if (this._class == null) {
                defineTransletClasses();
            }
        } catch (TransformerConfigurationException unused) {
        }
        return this._transletIndex;
    }

    /* access modifiers changed from: protected */
    public synchronized void setTransletName(String str) {
        this._name = str;
    }

    /* access modifiers changed from: protected */
    public synchronized String getTransletName() {
        return this._name;
    }

    private void defineTransletClasses() throws TransformerConfigurationException {
        if (this._bytecodes != null) {
            TransletClassLoader transletClassLoader = (TransletClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                /* class ohos.com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public Object run() {
                    return new TransletClassLoader(ObjectFactory.findClassLoader(), TemplatesImpl.this._tfactory.getExternalExtensionsMap());
                }
            });
            try {
                int length = this._bytecodes.length;
                this._class = new Class[length];
                if (length > 1) {
                    this._auxClasses = new HashMap();
                }
                for (int i = 0; i < length; i++) {
                    this._class[i] = transletClassLoader.defineClass(this._bytecodes[i]);
                    if (this._class[i].getSuperclass().getName().equals(ABSTRACT_TRANSLET)) {
                        this._transletIndex = i;
                    } else {
                        this._auxClasses.put(this._class[i].getName(), this._class[i]);
                    }
                }
                if (this._transletIndex < 0) {
                    throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.NO_MAIN_TRANSLET_ERR, this._name).toString());
                }
            } catch (ClassFormatError unused) {
                throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.TRANSLET_CLASS_ERR, this._name).toString());
            } catch (LinkageError unused2) {
                throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR, this._name).toString());
            }
        } else {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.NO_TRANSLET_CLASS_ERR).toString());
        }
    }

    private Translet getTransletInstance() throws TransformerConfigurationException {
        try {
            if (this._name == null) {
                return null;
            }
            if (this._class == null) {
                defineTransletClasses();
            }
            AbstractTranslet abstractTranslet = (AbstractTranslet) this._class[this._transletIndex].newInstance();
            abstractTranslet.postInitialization();
            abstractTranslet.setTemplates(this);
            abstractTranslet.setOverrideDefaultParser(this._overrideDefaultParser);
            abstractTranslet.setAllowedProtocols(this._accessExternalStylesheet);
            if (this._auxClasses != null) {
                abstractTranslet.setAuxiliaryClasses(this._auxClasses);
            }
            return abstractTranslet;
        } catch (InstantiationException unused) {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR, this._name).toString());
        } catch (IllegalAccessException unused2) {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.TRANSLET_OBJECT_ERR, this._name).toString());
        }
    }

    public synchronized Transformer newTransformer() throws TransformerConfigurationException {
        TransformerImpl transformerImpl;
        transformerImpl = new TransformerImpl(getTransletInstance(), this._outputProperties, this._indentNumber, this._tfactory);
        if (this._uriResolver != null) {
            transformerImpl.setURIResolver(this._uriResolver);
        }
        if (this._tfactory.getFeature(Constants.FEATURE_SECURE_PROCESSING)) {
            transformerImpl.setSecureProcessing(true);
        }
        return transformerImpl;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        return null;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public synchronized Properties getOutputProperties() {
        return newTransformer().getOutputProperties();
    }

    public DOM getStylesheetDOM() {
        return (DOM) this._sdom.get();
    }

    public void setStylesheetDOM(DOM dom) {
        this._sdom.set(dom);
    }
}
