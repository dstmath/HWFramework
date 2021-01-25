package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;

public class XML11DTDDVFactoryImpl extends DTDDVFactoryImpl {
    static Map<String, DatatypeValidator> XML11BUILTINTYPES;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put("XML11ID", new XML11IDDatatypeValidator());
        XML11IDREFDatatypeValidator xML11IDREFDatatypeValidator = new XML11IDREFDatatypeValidator();
        hashMap.put("XML11IDREF", xML11IDREFDatatypeValidator);
        hashMap.put("XML11IDREFS", new ListDatatypeValidator(xML11IDREFDatatypeValidator));
        XML11NMTOKENDatatypeValidator xML11NMTOKENDatatypeValidator = new XML11NMTOKENDatatypeValidator();
        hashMap.put("XML11NMTOKEN", xML11NMTOKENDatatypeValidator);
        hashMap.put("XML11NMTOKENS", new ListDatatypeValidator(xML11NMTOKENDatatypeValidator));
        XML11BUILTINTYPES = Collections.unmodifiableMap(hashMap);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl, ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory
    public DatatypeValidator getBuiltInDV(String str) {
        if (XML11BUILTINTYPES.get(str) != null) {
            return XML11BUILTINTYPES.get(str);
        }
        return (DatatypeValidator) fBuiltInTypes.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.DTDDVFactoryImpl, ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory
    public Map<String, DatatypeValidator> getBuiltInTypes() {
        HashMap hashMap = new HashMap(fBuiltInTypes);
        hashMap.putAll(XML11BUILTINTYPES);
        return hashMap;
    }
}
