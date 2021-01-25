package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;

public class SecurityConfiguration extends XIncludeAwareParserConfiguration {
    protected static final String SECURITY_MANAGER_PROPERTY = "http://apache.org/xml/properties/security-manager";

    public SecurityConfiguration() {
        this(null, null, null);
    }

    public SecurityConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public SecurityConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this(symbolTable, xMLGrammarPool, null);
    }

    public SecurityConfiguration(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLComponentManager xMLComponentManager) {
        super(symbolTable, xMLGrammarPool, xMLComponentManager);
        setProperty("http://apache.org/xml/properties/security-manager", new XMLSecurityManager(true));
    }
}
