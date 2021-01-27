package ohos.com.sun.org.apache.xerces.internal.impl.dv;

import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;

public abstract class DTDDVFactory {
    private static final String DEFAULT_FACTORY_CLASS = "ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl";
    private static final String XML11_DATATYPE_VALIDATOR_FACTORY = "ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl";

    public abstract DatatypeValidator getBuiltInDV(String str);

    public abstract Map<String, DatatypeValidator> getBuiltInTypes();

    public static final DTDDVFactory getInstance() throws DVFactoryException {
        return getInstance(DEFAULT_FACTORY_CLASS);
    }

    public static final DTDDVFactory getInstance(String str) throws DVFactoryException {
        try {
            if (DEFAULT_FACTORY_CLASS.equals(str)) {
                return new DTDDVFactoryImpl();
            }
            if (XML11_DATATYPE_VALIDATOR_FACTORY.equals(str)) {
                return new XML11DTDDVFactoryImpl();
            }
            return (DTDDVFactory) ObjectFactory.newInstance(str, true);
        } catch (ClassCastException unused) {
            throw new DVFactoryException("DTD factory class " + str + " does not extend from DTDDVFactory.");
        }
    }

    protected DTDDVFactory() {
    }
}
