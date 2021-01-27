package ohos.com.sun.org.apache.xerces.internal.util;

public final class ShadowedSymbolTable extends SymbolTable {
    protected SymbolTable fSymbolTable;

    public ShadowedSymbolTable(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public String addSymbol(String str) {
        if (this.fSymbolTable.containsSymbol(str)) {
            return this.fSymbolTable.addSymbol(str);
        }
        return super.addSymbol(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public String addSymbol(char[] cArr, int i, int i2) {
        if (this.fSymbolTable.containsSymbol(cArr, i, i2)) {
            return this.fSymbolTable.addSymbol(cArr, i, i2);
        }
        return super.addSymbol(cArr, i, i2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public int hash(String str) {
        return this.fSymbolTable.hash(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public int hash(char[] cArr, int i, int i2) {
        return this.fSymbolTable.hash(cArr, i, i2);
    }
}
