package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarLoader;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class XMLGrammarPreparser {
    private static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String GRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final Map<String, String> KNOWN_LOADERS;
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", ERROR_HANDLER, "http://apache.org/xml/properties/internal/entity-resolver", "http://apache.org/xml/properties/internal/grammar-pool"};
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected XMLEntityResolver fEntityResolver;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarPool fGrammarPool;
    private Map<String, XMLGrammarLoader> fLoaders;
    protected Locale fLocale;
    protected SymbolTable fSymbolTable;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put("http://www.w3.org/2001/XMLSchema", "ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader");
        hashMap.put(XMLGrammarDescription.XML_DTD, "ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader");
        KNOWN_LOADERS = Collections.unmodifiableMap(hashMap);
    }

    public XMLGrammarPreparser() {
        this(new SymbolTable());
    }

    public XMLGrammarPreparser(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
        this.fLoaders = new HashMap();
        this.fErrorReporter = new XMLErrorReporter();
        setLocale(Locale.getDefault());
        this.fEntityResolver = new XMLEntityManager();
    }

    public boolean registerPreparser(String str, XMLGrammarLoader xMLGrammarLoader) {
        if (xMLGrammarLoader == null) {
            if (KNOWN_LOADERS.containsKey(str)) {
                try {
                    this.fLoaders.put(str, (XMLGrammarLoader) ObjectFactory.newInstance(KNOWN_LOADERS.get(str), true));
                    return true;
                } catch (Exception unused) {
                }
            }
            return false;
        }
        this.fLoaders.put(str, xMLGrammarLoader);
        return true;
    }

    public Grammar preparseGrammar(String str, XMLInputSource xMLInputSource) throws XNIException, IOException {
        if (!this.fLoaders.containsKey(str)) {
            return null;
        }
        XMLGrammarLoader xMLGrammarLoader = this.fLoaders.get(str);
        xMLGrammarLoader.setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
        xMLGrammarLoader.setProperty("http://apache.org/xml/properties/internal/entity-resolver", this.fEntityResolver);
        xMLGrammarLoader.setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        XMLGrammarPool xMLGrammarPool = this.fGrammarPool;
        if (xMLGrammarPool != null) {
            try {
                xMLGrammarLoader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool);
            } catch (Exception unused) {
            }
        }
        return xMLGrammarLoader.loadGrammar(xMLInputSource);
    }

    public void setLocale(Locale locale) {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public void setErrorHandler(XMLErrorHandler xMLErrorHandler) {
        this.fErrorReporter.setProperty(ERROR_HANDLER, xMLErrorHandler);
    }

    public XMLErrorHandler getErrorHandler() {
        return this.fErrorReporter.getErrorHandler();
    }

    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        this.fEntityResolver = xMLEntityResolver;
    }

    public XMLEntityResolver getEntityResolver() {
        return this.fEntityResolver;
    }

    public void setGrammarPool(XMLGrammarPool xMLGrammarPool) {
        this.fGrammarPool = xMLGrammarPool;
    }

    public XMLGrammarPool getGrammarPool() {
        return this.fGrammarPool;
    }

    public XMLGrammarLoader getLoader(String str) {
        return this.fLoaders.get(str);
    }

    public void setFeature(String str, boolean z) {
        for (Map.Entry<String, XMLGrammarLoader> entry : this.fLoaders.entrySet()) {
            try {
                entry.getValue().setFeature(str, z);
            } catch (Exception unused) {
            }
        }
        if (str.equals(CONTINUE_AFTER_FATAL_ERROR)) {
            this.fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, z);
        }
    }

    public void setProperty(String str, Object obj) {
        for (Map.Entry<String, XMLGrammarLoader> entry : this.fLoaders.entrySet()) {
            try {
                entry.getValue().setProperty(str, obj);
            } catch (Exception unused) {
            }
        }
    }

    public boolean getFeature(String str, String str2) {
        return this.fLoaders.get(str).getFeature(str2);
    }

    public Object getProperty(String str, String str2) {
        return this.fLoaders.get(str).getProperty(str2);
    }
}
