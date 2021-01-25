package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;

public class ExtendedSchemaDVFactoryImpl extends BaseSchemaDVFactory {
    static SymbolHash fBuiltInTypes = new SymbolHash();

    static {
        createBuiltInTypes();
    }

    static void createBuiltInTypes() {
        createBuiltInTypes(fBuiltInTypes, XSSimpleTypeDecl.fAnyAtomicType);
        fBuiltInTypes.put("anyAtomicType", XSSimpleTypeDecl.fAnyAtomicType);
        XSSimpleTypeDecl xSSimpleTypeDecl = (XSSimpleTypeDecl) fBuiltInTypes.get(SchemaSymbols.ATTVAL_DURATION);
        fBuiltInTypes.put("yearMonthDuration", new XSSimpleTypeDecl(xSSimpleTypeDecl, "yearMonthDuration", 27, 1, false, false, false, true, 46));
        fBuiltInTypes.put("dayTimeDuration", new XSSimpleTypeDecl(xSSimpleTypeDecl, "dayTimeDuration", 28, 1, false, false, false, true, 47));
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
