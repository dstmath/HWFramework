package ohos.com.sun.org.apache.xerces.internal.impl.dv;

import ohos.com.sun.org.apache.xerces.internal.util.SymbolHash;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;

public abstract class SchemaDVFactory {
    private static final String DEFAULT_FACTORY_CLASS = "ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl";

    public abstract XSSimpleType createTypeList(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList);

    public abstract XSSimpleType createTypeRestriction(String str, String str2, short s, XSSimpleType xSSimpleType, XSObjectList xSObjectList);

    public abstract XSSimpleType createTypeUnion(String str, String str2, short s, XSSimpleType[] xSSimpleTypeArr, XSObjectList xSObjectList);

    public abstract XSSimpleType getBuiltInType(String str);

    public abstract SymbolHash getBuiltInTypes();

    public static final synchronized SchemaDVFactory getInstance() throws DVFactoryException {
        SchemaDVFactory instance;
        synchronized (SchemaDVFactory.class) {
            instance = getInstance(DEFAULT_FACTORY_CLASS);
        }
        return instance;
    }

    public static final synchronized SchemaDVFactory getInstance(String str) throws DVFactoryException {
        SchemaDVFactory schemaDVFactory;
        synchronized (SchemaDVFactory.class) {
            try {
                schemaDVFactory = (SchemaDVFactory) ObjectFactory.newInstance(str, true);
            } catch (ClassCastException unused) {
                throw new DVFactoryException("Schema factory class " + str + " does not extend from SchemaDVFactory.");
            }
        }
        return schemaDVFactory;
    }

    protected SchemaDVFactory() {
    }
}
