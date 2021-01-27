package ohos.com.sun.org.apache.xerces.internal.impl.validation;

import java.util.ArrayList;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;

public class ValidationState implements ValidationContext {
    private EntityState fEntityState = null;
    private boolean fExtraChecking = true;
    private boolean fFacetChecking = true;
    private ArrayList<String> fIdList;
    private ArrayList<String> fIdRefList;
    private Locale fLocale = null;
    private NamespaceContext fNamespaceContext = null;
    private boolean fNamespaces = true;
    private boolean fNormalize = true;
    private SymbolTable fSymbolTable = null;

    public void setExtraChecking(boolean z) {
        this.fExtraChecking = z;
    }

    public void setFacetChecking(boolean z) {
        this.fFacetChecking = z;
    }

    public void setNormalizationRequired(boolean z) {
        this.fNormalize = z;
    }

    public void setUsingNamespaces(boolean z) {
        this.fNamespaces = z;
    }

    public void setEntityState(EntityState entityState) {
        this.fEntityState = entityState;
    }

    public void setNamespaceSupport(NamespaceContext namespaceContext) {
        this.fNamespaceContext = namespaceContext;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    public String checkIDRefID() {
        ArrayList<String> arrayList;
        if (this.fIdList == null && (arrayList = this.fIdRefList) != null) {
            return arrayList.get(0);
        }
        if (this.fIdRefList == null) {
            return null;
        }
        for (int i = 0; i < this.fIdRefList.size(); i++) {
            String str = this.fIdRefList.get(i);
            if (!this.fIdList.contains(str)) {
                return str;
            }
        }
        return null;
    }

    public void reset() {
        this.fExtraChecking = true;
        this.fFacetChecking = true;
        this.fNamespaces = true;
        this.fIdList = null;
        this.fIdRefList = null;
        this.fEntityState = null;
        this.fNamespaceContext = null;
        this.fSymbolTable = null;
    }

    public void resetIDTables() {
        this.fIdList = null;
        this.fIdRefList = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean needExtraChecking() {
        return this.fExtraChecking;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean needFacetChecking() {
        return this.fFacetChecking;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean needToNormalize() {
        return this.fNormalize;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean useNamespaces() {
        return this.fNamespaces;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean isEntityDeclared(String str) {
        EntityState entityState = this.fEntityState;
        if (entityState != null) {
            return entityState.isEntityDeclared(getSymbol(str));
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean isEntityUnparsed(String str) {
        EntityState entityState = this.fEntityState;
        if (entityState != null) {
            return entityState.isEntityUnparsed(getSymbol(str));
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public boolean isIdDeclared(String str) {
        ArrayList<String> arrayList = this.fIdList;
        if (arrayList == null) {
            return false;
        }
        return arrayList.contains(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public void addId(String str) {
        if (this.fIdList == null) {
            this.fIdList = new ArrayList<>();
        }
        this.fIdList.add(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public void addIdRef(String str) {
        if (this.fIdRefList == null) {
            this.fIdRefList = new ArrayList<>();
        }
        this.fIdRefList.add(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public String getSymbol(String str) {
        SymbolTable symbolTable = this.fSymbolTable;
        if (symbolTable != null) {
            return symbolTable.addSymbol(str);
        }
        return str.intern();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public String getURI(String str) {
        NamespaceContext namespaceContext = this.fNamespaceContext;
        if (namespaceContext != null) {
            return namespaceContext.getURI(str);
        }
        return null;
    }

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext
    public Locale getLocale() {
        return this.fLocale;
    }
}
