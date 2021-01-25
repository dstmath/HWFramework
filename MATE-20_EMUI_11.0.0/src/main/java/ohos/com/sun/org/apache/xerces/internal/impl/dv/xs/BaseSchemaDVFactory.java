package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSDeclarationPool;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public abstract class BaseSchemaDVFactory extends SchemaDVFactory {
    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
    protected XSDeclarationPool fDeclPool = null;

    protected static void createBuiltInTypes(SymbolHash symbolHash, XSSimpleTypeDecl xSSimpleTypeDecl) {
        XSFacets xSFacets = new XSFacets();
        symbolHash.put(SchemaSymbols.ATTVAL_ANYSIMPLETYPE, XSSimpleTypeDecl.fAnySimpleType);
        XSSimpleTypeDecl xSSimpleTypeDecl2 = new XSSimpleTypeDecl(xSSimpleTypeDecl, "string", 1, 0, false, false, false, true, 2);
        symbolHash.put("string", xSSimpleTypeDecl2);
        symbolHash.put("boolean", new XSSimpleTypeDecl(xSSimpleTypeDecl, "boolean", 2, 0, false, true, false, true, 3));
        XSSimpleTypeDecl xSSimpleTypeDecl3 = new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DECIMAL, 3, 2, false, false, true, true, 4);
        symbolHash.put(SchemaSymbols.ATTVAL_DECIMAL, xSSimpleTypeDecl3);
        symbolHash.put(SchemaSymbols.ATTVAL_ANYURI, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_ANYURI, 17, 0, false, false, false, true, 18));
        symbolHash.put(SchemaSymbols.ATTVAL_BASE64BINARY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_BASE64BINARY, 16, 0, false, false, false, true, 17));
        symbolHash.put(SchemaSymbols.ATTVAL_DURATION, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DURATION, 6, 1, false, false, false, true, 7));
        symbolHash.put(SchemaSymbols.ATTVAL_DATETIME, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DATETIME, 7, 1, false, false, false, true, 8));
        symbolHash.put("time", new XSSimpleTypeDecl(xSSimpleTypeDecl, "time", 8, 1, false, false, false, true, 9));
        symbolHash.put(SchemaSymbols.ATTVAL_DATE, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DATE, 9, 1, false, false, false, true, 10));
        symbolHash.put(SchemaSymbols.ATTVAL_YEARMONTH, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_YEARMONTH, 10, 1, false, false, false, true, 11));
        symbolHash.put(SchemaSymbols.ATTVAL_YEAR, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_YEAR, 11, 1, false, false, false, true, 12));
        symbolHash.put(SchemaSymbols.ATTVAL_MONTHDAY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_MONTHDAY, 12, 1, false, false, false, true, 13));
        symbolHash.put(SchemaSymbols.ATTVAL_DAY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_DAY, 13, 1, false, false, false, true, 14));
        symbolHash.put(SchemaSymbols.ATTVAL_MONTH, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_MONTH, 14, 1, false, false, false, true, 15));
        XSSimpleTypeDecl xSSimpleTypeDecl4 = new XSSimpleTypeDecl(xSSimpleTypeDecl3, "integer", 24, 2, false, false, true, true, 30);
        symbolHash.put("integer", xSSimpleTypeDecl4);
        xSFacets.maxInclusive = "0";
        XSSimpleTypeDecl xSSimpleTypeDecl5 = new XSSimpleTypeDecl(xSSimpleTypeDecl4, SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 31);
        xSSimpleTypeDecl5.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, xSSimpleTypeDecl5);
        xSFacets.maxInclusive = "-1";
        XSSimpleTypeDecl xSSimpleTypeDecl6 = new XSSimpleTypeDecl(xSSimpleTypeDecl5, SchemaSymbols.ATTVAL_NEGATIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 32);
        xSSimpleTypeDecl6.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NEGATIVEINTEGER, xSSimpleTypeDecl6);
        xSFacets.maxInclusive = "9223372036854775807";
        xSFacets.minInclusive = "-9223372036854775808";
        XSSimpleTypeDecl xSSimpleTypeDecl7 = new XSSimpleTypeDecl(xSSimpleTypeDecl4, "long", "http://www.w3.org/2001/XMLSchema", 0, false, null, 33);
        xSSimpleTypeDecl7.applyFacets1(xSFacets, 288, 0);
        symbolHash.put("long", xSSimpleTypeDecl7);
        xSFacets.maxInclusive = "2147483647";
        xSFacets.minInclusive = "-2147483648";
        XSSimpleTypeDecl xSSimpleTypeDecl8 = new XSSimpleTypeDecl(xSSimpleTypeDecl7, "int", "http://www.w3.org/2001/XMLSchema", 0, false, null, 34);
        xSSimpleTypeDecl8.applyFacets1(xSFacets, 288, 0);
        symbolHash.put("int", xSSimpleTypeDecl8);
        xSFacets.maxInclusive = "32767";
        xSFacets.minInclusive = "-32768";
        XSSimpleTypeDecl xSSimpleTypeDecl9 = new XSSimpleTypeDecl(xSSimpleTypeDecl8, SchemaSymbols.ATTVAL_SHORT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 35);
        xSSimpleTypeDecl9.applyFacets1(xSFacets, 288, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_SHORT, xSSimpleTypeDecl9);
        xSFacets.maxInclusive = "127";
        xSFacets.minInclusive = "-128";
        XSSimpleTypeDecl xSSimpleTypeDecl10 = new XSSimpleTypeDecl(xSSimpleTypeDecl9, SchemaSymbols.ATTVAL_BYTE, "http://www.w3.org/2001/XMLSchema", 0, false, null, 36);
        xSSimpleTypeDecl10.applyFacets1(xSFacets, 288, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_BYTE, xSSimpleTypeDecl10);
        xSFacets.minInclusive = "0";
        XSSimpleTypeDecl xSSimpleTypeDecl11 = new XSSimpleTypeDecl(xSSimpleTypeDecl4, SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 37);
        xSSimpleTypeDecl11.applyFacets1(xSFacets, 256, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, xSSimpleTypeDecl11);
        xSFacets.maxInclusive = "18446744073709551615";
        XSSimpleTypeDecl xSSimpleTypeDecl12 = new XSSimpleTypeDecl(xSSimpleTypeDecl11, SchemaSymbols.ATTVAL_UNSIGNEDLONG, "http://www.w3.org/2001/XMLSchema", 0, false, null, 38);
        xSSimpleTypeDecl12.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDLONG, xSSimpleTypeDecl12);
        xSFacets.maxInclusive = "4294967295";
        XSSimpleTypeDecl xSSimpleTypeDecl13 = new XSSimpleTypeDecl(xSSimpleTypeDecl12, SchemaSymbols.ATTVAL_UNSIGNEDINT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 39);
        xSSimpleTypeDecl13.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDINT, xSSimpleTypeDecl13);
        xSFacets.maxInclusive = "65535";
        XSSimpleTypeDecl xSSimpleTypeDecl14 = new XSSimpleTypeDecl(xSSimpleTypeDecl13, SchemaSymbols.ATTVAL_UNSIGNEDSHORT, "http://www.w3.org/2001/XMLSchema", 0, false, null, 40);
        xSSimpleTypeDecl14.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDSHORT, xSSimpleTypeDecl14);
        xSFacets.maxInclusive = "255";
        XSSimpleTypeDecl xSSimpleTypeDecl15 = new XSSimpleTypeDecl(xSSimpleTypeDecl14, SchemaSymbols.ATTVAL_UNSIGNEDBYTE, "http://www.w3.org/2001/XMLSchema", 0, false, null, 41);
        xSSimpleTypeDecl15.applyFacets1(xSFacets, 32, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_UNSIGNEDBYTE, xSSimpleTypeDecl15);
        xSFacets.minInclusive = "1";
        XSSimpleTypeDecl xSSimpleTypeDecl16 = new XSSimpleTypeDecl(xSSimpleTypeDecl11, SchemaSymbols.ATTVAL_POSITIVEINTEGER, "http://www.w3.org/2001/XMLSchema", 0, false, null, 42);
        xSSimpleTypeDecl16.applyFacets1(xSFacets, 256, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_POSITIVEINTEGER, xSSimpleTypeDecl16);
        symbolHash.put("float", new XSSimpleTypeDecl(xSSimpleTypeDecl, "float", 4, 1, true, true, true, true, 5));
        symbolHash.put("double", new XSSimpleTypeDecl(xSSimpleTypeDecl, "double", 5, 1, true, true, true, true, 6));
        symbolHash.put(SchemaSymbols.ATTVAL_HEXBINARY, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_HEXBINARY, 15, 0, false, false, false, true, 16));
        symbolHash.put(SchemaSymbols.ATTVAL_NOTATION, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_NOTATION, 20, 0, false, false, false, true, 20));
        xSFacets.whiteSpace = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl17 = new XSSimpleTypeDecl(xSSimpleTypeDecl2, SchemaSymbols.ATTVAL_NORMALIZEDSTRING, "http://www.w3.org/2001/XMLSchema", 0, false, null, 21);
        xSSimpleTypeDecl17.applyFacets1(xSFacets, 16, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NORMALIZEDSTRING, xSSimpleTypeDecl17);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl18 = new XSSimpleTypeDecl(xSSimpleTypeDecl17, SchemaSymbols.ATTVAL_TOKEN, "http://www.w3.org/2001/XMLSchema", 0, false, null, 22);
        xSSimpleTypeDecl18.applyFacets1(xSFacets, 16, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_TOKEN, xSSimpleTypeDecl18);
        xSFacets.whiteSpace = 2;
        xSFacets.pattern = "([a-zA-Z]{1,8})(-[a-zA-Z0-9]{1,8})*";
        XSSimpleTypeDecl xSSimpleTypeDecl19 = new XSSimpleTypeDecl(xSSimpleTypeDecl18, "language", "http://www.w3.org/2001/XMLSchema", 0, false, null, 23);
        xSSimpleTypeDecl19.applyFacets1(xSFacets, 24, 0);
        symbolHash.put("language", xSSimpleTypeDecl19);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl20 = new XSSimpleTypeDecl(xSSimpleTypeDecl18, SchemaSymbols.ATTVAL_NAME, "http://www.w3.org/2001/XMLSchema", 0, false, null, 25);
        xSSimpleTypeDecl20.applyFacets1(xSFacets, 16, 0, 2);
        symbolHash.put(SchemaSymbols.ATTVAL_NAME, xSSimpleTypeDecl20);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl21 = new XSSimpleTypeDecl(xSSimpleTypeDecl20, SchemaSymbols.ATTVAL_NCNAME, "http://www.w3.org/2001/XMLSchema", 0, false, null, 26);
        xSSimpleTypeDecl21.applyFacets1(xSFacets, 16, 0, 3);
        symbolHash.put(SchemaSymbols.ATTVAL_NCNAME, xSSimpleTypeDecl21);
        symbolHash.put(SchemaSymbols.ATTVAL_QNAME, new XSSimpleTypeDecl(xSSimpleTypeDecl, SchemaSymbols.ATTVAL_QNAME, 18, 0, false, false, false, true, 19));
        symbolHash.put(SchemaSymbols.ATTVAL_ID, new XSSimpleTypeDecl(xSSimpleTypeDecl21, SchemaSymbols.ATTVAL_ID, 21, 0, false, false, false, true, 27));
        XSSimpleTypeDecl xSSimpleTypeDecl22 = new XSSimpleTypeDecl(xSSimpleTypeDecl21, SchemaSymbols.ATTVAL_IDREF, 22, 0, false, false, false, true, 28);
        symbolHash.put(SchemaSymbols.ATTVAL_IDREF, xSSimpleTypeDecl22);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl23 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl22, true, (XSObjectList) null), SchemaSymbols.ATTVAL_IDREFS, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl23.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_IDREFS, xSSimpleTypeDecl23);
        XSSimpleTypeDecl xSSimpleTypeDecl24 = new XSSimpleTypeDecl(xSSimpleTypeDecl21, SchemaSymbols.ATTVAL_ENTITY, 23, 0, false, false, false, true, 29);
        symbolHash.put(SchemaSymbols.ATTVAL_ENTITY, xSSimpleTypeDecl24);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl25 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl24, true, (XSObjectList) null), SchemaSymbols.ATTVAL_ENTITIES, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl25.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_ENTITIES, xSSimpleTypeDecl25);
        xSFacets.whiteSpace = 2;
        XSSimpleTypeDecl xSSimpleTypeDecl26 = new XSSimpleTypeDecl(xSSimpleTypeDecl18, SchemaSymbols.ATTVAL_NMTOKEN, "http://www.w3.org/2001/XMLSchema", 0, false, null, 24);
        xSSimpleTypeDecl26.applyFacets1(xSFacets, 16, 0, 1);
        symbolHash.put(SchemaSymbols.ATTVAL_NMTOKEN, xSSimpleTypeDecl26);
        xSFacets.minLength = 1;
        XSSimpleTypeDecl xSSimpleTypeDecl27 = new XSSimpleTypeDecl(new XSSimpleTypeDecl((String) null, "http://www.w3.org/2001/XMLSchema", (short) 0, xSSimpleTypeDecl26, true, (XSObjectList) null), SchemaSymbols.ATTVAL_NMTOKENS, "http://www.w3.org/2001/XMLSchema", (short) 0, false, (XSObjectList) null);
        xSSimpleTypeDecl27.applyFacets1(xSFacets, 2, 0);
        symbolHash.put(SchemaSymbols.ATTVAL_NMTOKENS, xSSimpleTypeDecl27);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeRestriction(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList) {
        XSDeclarationPool xSDeclarationPool = this.fDeclPool;
        if (xSDeclarationPool != null) {
            return xSDeclarationPool.getSimpleTypeDecl().setRestrictionValues((XSSimpleTypeDecl) xSSimpleType, str, str2, s, xSObjectList);
        }
        return new XSSimpleTypeDecl((XSSimpleTypeDecl) xSSimpleType, str, str2, s, false, xSObjectList);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeList(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList) {
        XSDeclarationPool xSDeclarationPool = this.fDeclPool;
        if (xSDeclarationPool != null) {
            return xSDeclarationPool.getSimpleTypeDecl().setListValues(str, str2, s, (XSSimpleTypeDecl) xSSimpleType, xSObjectList);
        }
        return new XSSimpleTypeDecl(str, str2, s, (XSSimpleTypeDecl) xSSimpleType, false, xSObjectList);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory
    public XSSimpleType createTypeUnion(String str, String str2, short s, XSSimpleType[] xSSimpleTypeArr, XSObjectList xSObjectList) {
        int length = xSSimpleTypeArr.length;
        XSSimpleTypeDecl[] xSSimpleTypeDeclArr = new XSSimpleTypeDecl[length];
        System.arraycopy(xSSimpleTypeArr, 0, xSSimpleTypeDeclArr, 0, length);
        XSDeclarationPool xSDeclarationPool = this.fDeclPool;
        if (xSDeclarationPool != null) {
            return xSDeclarationPool.getSimpleTypeDecl().setUnionValues(str, str2, s, xSSimpleTypeDeclArr, xSObjectList);
        }
        return new XSSimpleTypeDecl(str, str2, s, xSSimpleTypeDeclArr, xSObjectList);
    }

    public void setDeclPool(XSDeclarationPool xSDeclarationPool) {
        this.fDeclPool = xSDeclarationPool;
    }

    public XSSimpleTypeDecl newXSSimpleTypeDecl() {
        return new XSSimpleTypeDecl();
    }
}
