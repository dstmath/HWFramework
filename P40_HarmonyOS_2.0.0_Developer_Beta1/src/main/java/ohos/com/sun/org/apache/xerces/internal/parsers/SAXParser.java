package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;

public class SAXParser extends AbstractSAXParser {
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    private static final String[] RECOGNIZED_FEATURES = {NOTIFY_BUILTIN_REFS, REPORT_WHITESPACE};
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/grammar-pool"};
    protected static final String REPORT_WHITESPACE = "http://java.sun.com/xml/schema/features/report-ignored-element-content-whitespace";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";

    public SAXParser(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
    }

    public SAXParser() {
        this(null, null);
    }

    public SAXParser(SymbolTable symbolTable) {
        this(symbolTable, null);
    }

    public SAXParser(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        super(new XIncludeAwareParserConfiguration());
        this.fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
        this.fConfiguration.setFeature(NOTIFY_BUILTIN_REFS, true);
        this.fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if (symbolTable != null) {
            this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
        }
        if (xMLGrammarPool != null) {
            this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser
    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str.equals("http://apache.org/xml/properties/security-manager")) {
            this.securityManager = XMLSecurityManager.convert(obj, this.securityManager);
            super.setProperty("http://apache.org/xml/properties/security-manager", this.securityManager);
        } else if (str.equals("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager")) {
            if (obj == null) {
                this.securityPropertyManager = new XMLSecurityPropertyManager();
            } else {
                this.securityPropertyManager = (XMLSecurityPropertyManager) obj;
            }
            super.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.securityPropertyManager);
        } else {
            if (this.securityManager == null) {
                this.securityManager = new XMLSecurityManager(true);
                super.setProperty("http://apache.org/xml/properties/security-manager", this.securityManager);
            }
            if (this.securityPropertyManager == null) {
                this.securityPropertyManager = new XMLSecurityPropertyManager();
                super.setProperty("http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager", this.securityPropertyManager);
            }
            int index = this.securityPropertyManager.getIndex(str);
            if (index > -1) {
                this.securityPropertyManager.setValue(index, XMLSecurityPropertyManager.State.APIPROPERTY, (String) obj);
            } else if (!this.securityManager.setLimit(str, XMLSecurityManager.State.APIPROPERTY, obj)) {
                super.setProperty(str, obj);
            }
        }
    }
}
