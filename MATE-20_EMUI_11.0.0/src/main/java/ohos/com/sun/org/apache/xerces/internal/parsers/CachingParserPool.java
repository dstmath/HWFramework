package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.util.ShadowedSymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

public class CachingParserPool {
    public static final boolean DEFAULT_SHADOW_GRAMMAR_POOL = false;
    public static final boolean DEFAULT_SHADOW_SYMBOL_TABLE = false;
    protected boolean fShadowGrammarPool;
    protected boolean fShadowSymbolTable;
    protected XMLGrammarPool fSynchronizedGrammarPool;
    protected SymbolTable fSynchronizedSymbolTable;

    public CachingParserPool() {
        this(new SymbolTable(), new XMLGrammarPoolImpl());
    }

    public CachingParserPool(SymbolTable symbolTable, XMLGrammarPool xMLGrammarPool) {
        this.fShadowSymbolTable = false;
        this.fShadowGrammarPool = false;
        this.fSynchronizedSymbolTable = new SynchronizedSymbolTable(symbolTable);
        this.fSynchronizedGrammarPool = new SynchronizedGrammarPool(xMLGrammarPool);
    }

    public SymbolTable getSymbolTable() {
        return this.fSynchronizedSymbolTable;
    }

    public XMLGrammarPool getXMLGrammarPool() {
        return this.fSynchronizedGrammarPool;
    }

    public void setShadowSymbolTable(boolean z) {
        this.fShadowSymbolTable = z;
    }

    public DOMParser createDOMParser() {
        SymbolTable symbolTable;
        XMLGrammarPool xMLGrammarPool;
        if (this.fShadowSymbolTable) {
            symbolTable = new ShadowedSymbolTable(this.fSynchronizedSymbolTable);
        } else {
            symbolTable = this.fSynchronizedSymbolTable;
        }
        if (this.fShadowGrammarPool) {
            xMLGrammarPool = new ShadowedGrammarPool(this.fSynchronizedGrammarPool);
        } else {
            xMLGrammarPool = this.fSynchronizedGrammarPool;
        }
        return new DOMParser(symbolTable, xMLGrammarPool);
    }

    public SAXParser createSAXParser() {
        SymbolTable symbolTable;
        XMLGrammarPool xMLGrammarPool;
        if (this.fShadowSymbolTable) {
            symbolTable = new ShadowedSymbolTable(this.fSynchronizedSymbolTable);
        } else {
            symbolTable = this.fSynchronizedSymbolTable;
        }
        if (this.fShadowGrammarPool) {
            xMLGrammarPool = new ShadowedGrammarPool(this.fSynchronizedGrammarPool);
        } else {
            xMLGrammarPool = this.fSynchronizedGrammarPool;
        }
        return new SAXParser(symbolTable, xMLGrammarPool);
    }

    public static final class SynchronizedGrammarPool implements XMLGrammarPool {
        private XMLGrammarPool fGrammarPool;

        public SynchronizedGrammarPool(XMLGrammarPool xMLGrammarPool) {
            this.fGrammarPool = xMLGrammarPool;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar[] retrieveInitialGrammarSet(String str) {
            Grammar[] retrieveInitialGrammarSet;
            synchronized (this.fGrammarPool) {
                retrieveInitialGrammarSet = this.fGrammarPool.retrieveInitialGrammarSet(str);
            }
            return retrieveInitialGrammarSet;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
            Grammar retrieveGrammar;
            synchronized (this.fGrammarPool) {
                retrieveGrammar = this.fGrammarPool.retrieveGrammar(xMLGrammarDescription);
            }
            return retrieveGrammar;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void cacheGrammars(String str, Grammar[] grammarArr) {
            synchronized (this.fGrammarPool) {
                this.fGrammarPool.cacheGrammars(str, grammarArr);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void lockPool() {
            synchronized (this.fGrammarPool) {
                this.fGrammarPool.lockPool();
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void clear() {
            synchronized (this.fGrammarPool) {
                this.fGrammarPool.clear();
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void unlockPool() {
            synchronized (this.fGrammarPool) {
                this.fGrammarPool.unlockPool();
            }
        }
    }

    public static final class ShadowedGrammarPool extends XMLGrammarPoolImpl {
        private XMLGrammarPool fGrammarPool;

        public ShadowedGrammarPool(XMLGrammarPool xMLGrammarPool) {
            this.fGrammarPool = xMLGrammarPool;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl, ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar[] retrieveInitialGrammarSet(String str) {
            Grammar[] retrieveInitialGrammarSet = super.retrieveInitialGrammarSet(str);
            if (retrieveInitialGrammarSet != null) {
                return retrieveInitialGrammarSet;
            }
            return this.fGrammarPool.retrieveInitialGrammarSet(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl, ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
            Grammar retrieveGrammar = super.retrieveGrammar(xMLGrammarDescription);
            if (retrieveGrammar != null) {
                return retrieveGrammar;
            }
            return this.fGrammarPool.retrieveGrammar(xMLGrammarDescription);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl, ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public void cacheGrammars(String str, Grammar[] grammarArr) {
            super.cacheGrammars(str, grammarArr);
            this.fGrammarPool.cacheGrammars(str, grammarArr);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl
        public Grammar getGrammar(XMLGrammarDescription xMLGrammarDescription) {
            if (super.containsGrammar(xMLGrammarDescription)) {
                return super.getGrammar(xMLGrammarDescription);
            }
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl
        public boolean containsGrammar(XMLGrammarDescription xMLGrammarDescription) {
            return super.containsGrammar(xMLGrammarDescription);
        }
    }
}
