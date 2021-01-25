package ohos.com.sun.org.apache.xerces.internal.util;

public final class SynchronizedSymbolTable extends SymbolTable {
    protected SymbolTable fSymbolTable;

    public SynchronizedSymbolTable(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    public SynchronizedSymbolTable() {
        this.fSymbolTable = new SymbolTable();
    }

    public SynchronizedSymbolTable(int i) {
        this.fSymbolTable = new SymbolTable(i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public String addSymbol(String str) {
        String addSymbol;
        synchronized (this.fSymbolTable) {
            addSymbol = this.fSymbolTable.addSymbol(str);
        }
        return addSymbol;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public String addSymbol(char[] cArr, int i, int i2) {
        String addSymbol;
        synchronized (this.fSymbolTable) {
            addSymbol = this.fSymbolTable.addSymbol(cArr, i, i2);
        }
        return addSymbol;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public boolean containsSymbol(String str) {
        boolean containsSymbol;
        synchronized (this.fSymbolTable) {
            containsSymbol = this.fSymbolTable.containsSymbol(str);
        }
        return containsSymbol;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.util.SymbolTable
    public boolean containsSymbol(char[] cArr, int i, int i2) {
        boolean containsSymbol;
        synchronized (this.fSymbolTable) {
            containsSymbol = this.fSymbolTable.containsSymbol(cArr, i, i2);
        }
        return containsSymbol;
    }
}
