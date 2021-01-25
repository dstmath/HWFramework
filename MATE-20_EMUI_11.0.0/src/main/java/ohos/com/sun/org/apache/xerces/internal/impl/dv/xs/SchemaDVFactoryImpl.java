package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;

public class SchemaDVFactoryImpl extends BaseSchemaDVFactory {
    static final SymbolHash fBuiltInTypes = new SymbolHash();

    static {
        createBuiltInTypes();
    }

    static void createBuiltInTypes() {
        createBuiltInTypes(fBuiltInTypes, XSSimpleTypeDecl.fAnySimpleType);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType getBuiltInType(String str) {
        return (XSSimpleType) fBuiltInTypes.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public SymbolHash getBuiltInTypes() {
        return fBuiltInTypes.makeClone();
    }
}
