package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;

public abstract class XMLGrammarParser extends XMLParser {
    protected DTDDVFactory fDatatypeValidatorFactory;

    protected XMLGrammarParser(SymbolTable symbolTable) {
        super(new XIncludeAwareParserConfiguration());
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
    }
}
