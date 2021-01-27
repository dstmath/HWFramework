package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.XML11DTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;

public class XML11DTDProcessor extends XMLDTDLoader {
    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader
    public short getScannerVersion() {
        return 2;
    }

    public XML11DTDProcessor() {
    }

    public XML11DTDProcessor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    public XML11DTDProcessor(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        super(symbolTable, xMLGrammarPool);
    }

    XML11DTDProcessor(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool, XMLErrorReporter xMLErrorReporter, XMLEntityResolver xMLEntityResolver) {
        super(symbolTable, xMLGrammarPool, xMLErrorReporter, xMLEntityResolver);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor
    public boolean isValidNmtoken(String str) {
        return XML11Char.isXML11ValidNmtoken(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor
    public boolean isValidName(String str) {
        return XML11Char.isXML11ValidName(str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader
    public XMLDTDScannerImpl createDTDScanner(SymbolTable symbolTable, XMLErrorReporter xMLErrorReporter, XMLEntityManager xMLEntityManager) {
        return new XML11DTDScannerImpl(symbolTable, xMLErrorReporter, xMLEntityManager);
    }
}
