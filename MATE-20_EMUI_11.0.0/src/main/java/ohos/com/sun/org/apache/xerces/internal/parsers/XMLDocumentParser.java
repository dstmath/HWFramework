package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;

public class XMLDocumentParser extends AbstractXMLDocumentParser {
    public XMLDocumentParser() {
        super(new XIncludeAwareParserConfiguration());
    }

    public XMLDocumentParser(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
    }

    public XMLDocumentParser(SymbolTable symbolTable) {
        super(new XIncludeAwareParserConfiguration());
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
    }

    public XMLDocumentParser(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        super(new XIncludeAwareParserConfiguration());
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/symbol-table", symbolTable);
        this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPool);
    }
}
