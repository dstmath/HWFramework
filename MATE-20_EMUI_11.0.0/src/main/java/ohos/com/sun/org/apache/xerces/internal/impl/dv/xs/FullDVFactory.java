package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class FullDVFactory extends BaseDVFactory {
    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
    static SymbolHash fFullTypes = new SymbolHash(89);

    static {
        createBuiltInTypes(fFullTypes);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.BaseDVFactory, ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType getBuiltInType(String str) {
        return (XSSimpleType) fFullTypes.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.BaseDVFactory, ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public SymbolHash getBuiltInTypes() {
        return fFullTypes.makeClone();
    }

    static void createBuiltInTypes(SymbolHash symbolHash) {
        BaseDVFactory.createBuiltInTypes(symbolHash);
        XSFacets xSFacets = new XSFacets();
        XSSimpleTypeDecl xSSimpleTypeDecl = XSSimpleTypeDecl.fAnySimpleType;
        symbolHash.put("float", new XSSimpleTypeDecl(xSSimpleTypeDecl, "float", 4, 1, true, true, true, true, 5));
        symbolHash.put("double", new XSSimpleTypeDecl(xSSimpleTypeDecl, "double", 5, 1, true, true, true, true, 6));
        symbolHash.put(SchemaSymbols.ATTVAL_DURATION, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DURATION, 6, 1, false, false, false, true, 7));
        symbolHash.put(SchemaSymbols.ATTVAL_HEXBINARY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_HEXBINARY, 15, 0, false, false, false, true, 16));
        symbolHash.put(SchemaSymbols.ATTVAL_QNAME, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_QNAME, 18, 0, false, false, false, true, 19));
        symbolHash.put(SchemaSymbols.ATTVAL_NOTATION, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_NOTATION, 20, 0, false, false, false, true, 20));
        xSFacets.whiteSpace = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl2 = new XSSimpleTypeDecl((XSSimpleTypeDecl) symbolHash.get("string"), SchemaSymbols.ATTVAL_NORMALIZEDSTRING, "http://www.w3.org/2001/XMLSchema", 0, false, null, 21);
        xSSimpleTypeDecl2.applyFacets1(xSFacets, 16, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NORMALIZEDSTRING, xSSimpleTypeDecl2);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl3 = new XSSimpleTypeDecl(xSSimpleTypeDecl2, SchemaSymbols.ATTVAL_TOKEN, "http://www.w3.org/2001/XMLSchema", 0, false, null, 22);
        xSSimpleTypeDecl3.applyFacets1(xSFacets, 16, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_TOKEN, xSSimpleTypeDecl3);
        xSFacets.whiteSpace = 2;
        xSFacets.pattern = "([a-zA-Z]{1,8})(-[a-zA-Z0-9]{1,8})*";
        XSSimpleTypeDecl xSSimpleTypeDecl4 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, "language", "http://www.w3.org/2001/XMLSchema", 0, false, null, 23);
        xSSimpleTypeDecl4.applyFacets1(xSFacets, 24, 0);
        symbolHash.put("language", xSSimpleTypeDecl4);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl5 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, SchemaSymbols.ATTVAL_NAME, "http://www.w3.org/2001/XMLSchema", 0, false, null, 25);
        xSSimpleTypeDecl5.applyFacets1(xSFacets, 16, 0, 2);
        symbolHash.put(SchemaSymbols.ATTVAL_NAME, xSSimpleTypeDecl5);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl6 = new XSSimpleTypeDecl(xSSimpleTypeDecl5, SchemaSymbols.ATTVAL_NCNAME, "http://www.w3.org/2001/XMLSchema", 0, false, null, 26);
        xSSimpleTypeDecl6.applyFacets1(xSFacets, 16, 0, 3);
        symbolHash.put(SchemaSymbols.ATTVAL_NCNAME, xSSimpleTypeDecl6);
        symbolHash.put(SchemaSymbols.ATTVAL_ID, new XSSimpleTypeDecl(xSSimpleTypeDecl6, SchemaSymbols.ATTVAL_ID, 21, 0, false, false, false, true, 27));
        XSSimpleTypeDecl xSSimpleTypeDecl7 = new XSSimpleTypeDecl(xSSimpleTypeDecl6, SchemaSymbols.ATTVAL_IDREF, 22, 0, false, false, false, true, 28);
        symbolHash.put(SchemaSymbols.ATTVAL_IDREF, xSSimpleTypeDecl7);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl8 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl7, true, (XSObjectList) null), SchemaSymbols.ATTVAL_IDREFS, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl8.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_IDREFS, xSSimpleTypeDecl8);
        XSSimpleTypeDecl xSSimpleTypeDecl9 = new XSSimpleTypeDecl(xSSimpleTypeDecl6, SchemaSymbols.ATTVAL_ENTITY, 23, 0, false, false, false, true, 29);
        symbolHash.put(SchemaSymbols.ATTVAL_ENTITY, xSSimpleTypeDecl9);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl10 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl9, true, (XSObjectList) null), SchemaSymbols.ATTVAL_ENTITIES, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl10.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_ENTITIES, xSSimpleTypeDecl10);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl11 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, SchemaSymbols.ATTVAL_NMTOKEN, "http://www.w3.org/2001/XMLSchema", 0, false, null, 24);
        xSSimpleTypeDecl11.applyFacets1(xSFacets, 16, 0, 1);
        symbolHash.put(SchemaSymbols.ATTVAL_NMTOKEN, xSSimpleTypeDecl11);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl12 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl11, true, (XSObjectList) null), SchemaSymbols.ATTVAL_NMTOKENS, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl12.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NMTOKENS, xSSimpleTypeDecl12);
    }
}
