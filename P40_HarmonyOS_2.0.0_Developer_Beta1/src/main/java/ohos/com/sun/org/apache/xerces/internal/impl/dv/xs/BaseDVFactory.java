package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public class BaseDVFactory extends SchemaDVFactory {
    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
    static SymbolHash fBaseTypes = new SymbolHash(53);

    static {
        createBuiltInTypes(fBaseTypes);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType getBuiltInType(String str) {
        return (XSSimpleType) fBaseTypes.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public SymbolHash getBuiltInTypes() {
        return fBaseTypes.makeClone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeRestriction(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList) {
        return new XSSimpleTypeDecl((XSSimpleTypeDecl) xSSimpleType, str, str2, s, false, xSObjectList);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeList(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList) {
        return new XSSimpleTypeDecl(str, str2, s, (XSSimpleTypeDecl) xSSimpleType, false, xSObjectList);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeUnion(String str, String str2, short s, XSSimpleType[] xSSimpleTypeArr, XSObjectList xSObjectList) {
        int length = xSSimpleTypeArr.length;
        XSSimpleTypeDecl[] xSSimpleTypeDeclArr = new XSSimpleTypeDecl[length];
        System.arraycopy(xSSimpleTypeArr, 0, xSSimpleTypeDeclArr, 0, length);
        return new XSSimpleTypeDecl(str, str2, s, xSSimpleTypeDeclArr, xSObjectList);
    }

    static void createBuiltInTypes(SymbolHash symbolHash) {
        XSFacets xSFacets = new XSFacets();
        XSSimpleTypeDecl xSSimpleTypeDecl = XSSimpleTypeDecl.fAnySimpleType;
        symbolHash.put(SchemaSymbols.ATTVAL_ANYSIMPLETYPE, xSSimpleTypeDecl);
        symbolHash.put("string", new XSSimpleTypeDecl(xSSimpleTypeDecl, "string", 1, 0, false, false, false, true, 2));
        symbolHash.put("boolean", new XSSimpleTypeDecl(xSSimpleTypeDecl, "boolean", 2, 0, false, true, false, true, 3));
        XSSimpleTypeDecl xSSimpleTypeDecl2 = new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DECIMAL, 3, 2, false, false, true, true, 4);
        symbolHash.put(SchemaSymbols.ATTVAL_DECIMAL, xSSimpleTypeDecl2);
        symbolHash.put(SchemaSymbols.ATTVAL_ANYURI, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_ANYURI, 17, 0, false, false, false, true, 18));
        symbolHash.put(SchemaSymbols.ATTVAL_BASE64BINARY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_BASE64BINARY, 16, 0, false, false, false, true, 17));
        symbolHash.put(SchemaSymbols.ATTVAL_DATETIME, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DATETIME, 7, 1, false, false, false, true, 8));
        symbolHash.put("time", new XSSimpleTypeDecl(xSSimpleTypeDecl, "time", 8, 1, false, false, false, true, 9));
        symbolHash.put(SchemaSymbols.ATTVAL_DATE, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DATE, 9, 1, false, false, false, true, 10));
        symbolHash.put(SchemaSymbols.ATTVAL_YEARMONTH, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_YEARMONTH, 10, 1, false, false, false, true, 11));
        symbolHash.put(SchemaSymbols.ATTVAL_YEAR, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_YEAR, 11, 1, false, false, false, true, 12));
        symbolHash.put(SchemaSymbols.ATTVAL_MONTHDAY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_MONTHDAY, 12, 1, false, false, false, true, 13));
        symbolHash.put(SchemaSymbols.ATTVAL_DAY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DAY, 13, 1, false, false, false, true, 14));
        symbolHash.put(SchemaSymbols.ATTVAL_MONTH, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_MONTH, 14, 1, false, false, false, true, 15));
        XSSimpleTypeDecl xSSimpleTypeDecl3 = new XSSimpleTypeDecl(xSSimpleTypeDecl2, "integer", 24, 2, false, false, true, true, 30);
        symbolHash.put("integer", xSSimpleTypeDecl3);
        xSFacets.maxInclusive = "0";
        XSSimpleTypeDecl xSSimpleTypeDecl4 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 31);
        xSSimpleTypeDecl4.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, xSSimpleTypeDecl4);
        xSFacets.maxInclusive = "-1";
        XSSimpleTypeDecl xSSimpleTypeDecl5 = new XSSimpleTypeDecl(xSSimpleTypeDecl4, SchemaSymbols.ATTVAL_NEGATIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 32);
        xSSimpleTypeDecl5.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NEGATIVEINTEGER, xSSimpleTypeDecl5);
        xSFacets.maxInclusive = "9223372036854775807";
        xSFacets.minInclusive = "-9223372036854775808";
        XSSimpleTypeDecl xSSimpleTypeDecl6 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, "long", "http://www.w3.org/2001/XMLSchema", 0, false, null, 33);
        xSSimpleTypeDecl6.applyFacets1(xSFacets, 288, 0);
        symbolHash.put("long", xSSimpleTypeDecl6);
        xSFacets.maxInclusive = "2147483647";
        xSFacets.minInclusive = "-2147483648";
        XSSimpleTypeDecl xSSimpleTypeDecl7 = new XSSimpleTypeDecl(xSSimpleTypeDecl6, "int", "http://www.w3.org/2001/XMLSchema", 0, false, null, 34);
        xSSimpleTypeDecl7.applyFacets1(xSFacets, 288, 0);
        symbolHash.put("int", xSSimpleTypeDecl7);
        xSFacets.maxInclusive = "32767";
        xSFacets.minInclusive = "-32768";
        XSSimpleTypeDecl xSSimpleTypeDecl8 = new XSSimpleTypeDecl(xSSimpleTypeDecl7, SchemaSymbols.ATTVAL_SHORT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 35);
        xSSimpleTypeDecl8.applyFacets1(xSFacets, 288, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_SHORT, xSSimpleTypeDecl8);
        xSFacets.maxInclusive = "127";
        xSFacets.minInclusive = "-128";
        XSSimpleTypeDecl xSSimpleTypeDecl9 = new XSSimpleTypeDecl(xSSimpleTypeDecl8, SchemaSymbols.ATTVAL_BYTE, "http://www.w3.org/2001/XMLSchema", 0, false, null, 36);
        xSSimpleTypeDecl9.applyFacets1(xSFacets, 288, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_BYTE, xSSimpleTypeDecl9);
        xSFacets.minInclusive = "0";
        XSSimpleTypeDecl xSSimpleTypeDecl10 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 37);
        xSSimpleTypeDecl10.applyFacets1(xSFacets, 256, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, xSSimpleTypeDecl10);
        xSFacets.maxInclusive = "18446744073709551615";
        XSSimpleTypeDecl xSSimpleTypeDecl11 = new XSSimpleTypeDecl(xSSimpleTypeDecl10, SchemaSymbols.ATTVAL_UNSIGNEDLONG, "http://www.w3.org/2001/XMLSchema", 0, false, null, 38);
        xSSimpleTypeDecl11.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDLONG, xSSimpleTypeDecl11);
        xSFacets.maxInclusive = "4294967295";
        XSSimpleTypeDecl xSSimpleTypeDecl12 = new XSSimpleTypeDecl(xSSimpleTypeDecl11, SchemaSymbols.ATTVAL_UNSIGNEDINT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 39);
        xSSimpleTypeDecl12.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDINT, xSSimpleTypeDecl12);
        xSFacets.maxInclusive = "65535";
        XSSimpleTypeDecl xSSimpleTypeDecl13 = new XSSimpleTypeDecl(xSSimpleTypeDecl12, SchemaSymbols.ATTVAL_UNSIGNEDSHORT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 40);
        xSSimpleTypeDecl13.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDSHORT, xSSimpleTypeDecl13);
        xSFacets.maxInclusive = "255";
        XSSimpleTypeDecl xSSimpleTypeDecl14 = new XSSimpleTypeDecl(xSSimpleTypeDecl13, SchemaSymbols.ATTVAL_UNSIGNEDBYTE, "http://www.w3.org/2001/XMLSchema", 0, false, null, 41);
        xSSimpleTypeDecl14.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDBYTE, xSSimpleTypeDecl14);
        xSFacets.minInclusive = "1";
        XSSimpleTypeDecl xSSimpleTypeDecl15 = new XSSimpleTypeDecl(xSSimpleTypeDecl10, SchemaSymbols.ATTVAL_POSITIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 42);
        xSSimpleTypeDecl15.applyFacets1(xSFacets, 256, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_POSITIVEINTEGER, xSSimpleTypeDecl15);
    }
}
