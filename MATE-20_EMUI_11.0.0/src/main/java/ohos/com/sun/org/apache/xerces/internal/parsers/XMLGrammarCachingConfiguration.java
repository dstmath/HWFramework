package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class XMLGrammarCachingConfiguration extends XIncludeAwareParserConfiguration {
    public static final int BIG_PRIME = 2039;
    protected static final String SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final XMLGrammarPoolImpl fStaticGrammarPool = new XMLGrammarPoolImpl();
    protected static final SynchronizedSymbolTable fStaticSymbolTable = new SynchronizedSymbolTable((int) BIG_PRIME);
    protected XMLDTDLoader fDTDLoader;
    protected XMLSchemaLoader fSchemaLoader;

    public XMLGrammarCachingConfiguration() {
        this(fStaticSymbolTable, fStaticGrammarPool, null);
    }

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable) {
        this(symbolTable, fStaticGrammarPool, null);
    }

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public XMLGrammarCachingConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        this.fSchemaLoader = new XMLSchemaLoader(this.fSymbolTable);
        this.fSchemaLoader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarPool);
        this.fDTDLoader = new XMLDTDLoader(this.fSymbolTable, this.fGrammarPool);
    }

    public void lockGrammarPool() {
        this.fGrammarPool.lockPool();
    }

    public void clearGrammarPool() {
        this.fGrammarPool.clear();
    }

    public void unlockGrammarPool() {
        this.fGrammarPool.unlockPool();
    }

    public Grammar parseGrammar(String str, String str2) throws XNIException, IOException {
        return parseGrammar(str, new XMLInputSource(null, str2, null));
    }

    public Grammar parseGrammar(String str, XMLInputSource xMLInputSource) throws XNIException, IOException {
        if (str.equals("http://www.w3.org/2001/XMLSchema")) {
            return parseXMLSchema(xMLInputSource);
        }
        if (str.equals(XMLGrammarDescription.XML_DTD)) {
            return parseDTD(xMLInputSource);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public SchemaGrammar parseXMLSchema(XMLInputSource xMLInputSource) throws IOException {
        XMLEntityResolver entityResolver = getEntityResolver();
        if (entityResolver != null) {
            this.fSchemaLoader.setEntityResolver(entityResolver);
        }
        if (this.fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            this.fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
        }
        this.fSchemaLoader.setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        String str = Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION;
        this.fSchemaLoader.setProperty(str, getProperty(str));
        String str2 = Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_NONS_LOCATION;
        this.fSchemaLoader.setProperty(str2, getProperty(str2));
        this.fSchemaLoader.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, getProperty(JAXPConstants.JAXP_SCHEMA_SOURCE));
        this.fSchemaLoader.setFeature(SCHEMA_FULL_CHECKING, getFeature(SCHEMA_FULL_CHECKING));
        SchemaGrammar schemaGrammar = (SchemaGrammar) this.fSchemaLoader.loadGrammar(xMLInputSource);
        if (schemaGrammar != null) {
            this.fGrammarPool.cacheGrammars("http://www.w3.org/2001/XMLSchema", new Grammar[]{schemaGrammar});
        }
        return schemaGrammar;
    }

    /* access modifiers changed from: package-private */
    public DTDGrammar parseDTD(XMLInputSource xMLInputSource) throws IOException {
        XMLEntityResolver entityResolver = getEntityResolver();
        if (entityResolver != null) {
            this.fDTDLoader.setEntityResolver(entityResolver);
        }
        this.fDTDLoader.setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        DTDGrammar dTDGrammar = (DTDGrammar) this.fDTDLoader.loadGrammar(xMLInputSource);
        if (dTDGrammar != null) {
            this.fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_DTD, new Grammar[]{dTDGrammar});
        }
        return dTDGrammar;
    }
}
