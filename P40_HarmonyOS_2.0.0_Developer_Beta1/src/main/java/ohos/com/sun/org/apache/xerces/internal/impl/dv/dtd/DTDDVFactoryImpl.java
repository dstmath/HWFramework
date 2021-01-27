package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

public class DTDDVFactoryImpl extends DTDDVFactory {
    static final Map<String, DatatypeValidator> fBuiltInTypes;

    static {
        HashMap hashMap = new HashMap();
        hashMap.put("string", new StringDatatypeValidator());
        hashMap.put(SchemaSymbols.ATTVAL_ID, new IDDatatypeValidator());
        IDREFDatatypeValidator iDREFDatatypeValidator = new IDREFDatatypeValidator();
        hashMap.put(SchemaSymbols.ATTVAL_IDREF, iDREFDatatypeValidator);
        hashMap.put(SchemaSymbols.ATTVAL_IDREFS, new ListDatatypeValidator(iDREFDatatypeValidator));
        ENTITYDatatypeValidator eNTITYDatatypeValidator = new ENTITYDatatypeValidator();
        hashMap.put(SchemaSymbols.ATTVAL_ENTITY, new ENTITYDatatypeValidator());
        hashMap.put(SchemaSymbols.ATTVAL_ENTITIES, new ListDatatypeValidator(eNTITYDatatypeValidator));
        hashMap.put(SchemaSymbols.ATTVAL_NOTATION, new NOTATIONDatatypeValidator());
        NMTOKENDatatypeValidator nMTOKENDatatypeValidator = new NMTOKENDatatypeValidator();
        hashMap.put(SchemaSymbols.ATTVAL_NMTOKEN, nMTOKENDatatypeValidator);
        hashMap.put(SchemaSymbols.ATTVAL_NMTOKENS, new ListDatatypeValidator(nMTOKENDatatypeValidator));
        fBuiltInTypes = Collections.unmodifiableMap(hashMap);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory
    public DatatypeValidator getBuiltInDV(String str) {
        return fBuiltInTypes.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory
    public Map<String, DatatypeValidator> getBuiltInTypes() {
        return new HashMap(fBuiltInTypes);
    }
}
