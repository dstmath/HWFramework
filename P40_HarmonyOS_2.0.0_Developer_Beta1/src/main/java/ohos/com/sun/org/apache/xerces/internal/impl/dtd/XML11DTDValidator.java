package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;

public class XML11DTDValidator extends XMLDTDValidator {
    protected static final String DTD_VALIDATOR_PROPERTY = "http://apache.org/xml/properties/internal/validator/dtd";

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) {
        XMLDTDValidator xMLDTDValidator = (XMLDTDValidator) xMLComponentManager.getProperty(DTD_VALIDATOR_PROPERTY);
        if (!(xMLDTDValidator == null || xMLDTDValidator == this)) {
            this.fGrammarBucket = xMLDTDValidator.getGrammarBucket();
        }
        super.reset(xMLComponentManager);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator
    public void init() {
        if (this.fValidation || this.fDynamicValidation) {
            super.init();
            try {
                this.fValID = this.fDatatypeValidatorFactory.getBuiltInDV("XML11ID");
                this.fValIDRef = this.fDatatypeValidatorFactory.getBuiltInDV("XML11IDREF");
                this.fValIDRefs = this.fDatatypeValidatorFactory.getBuiltInDV("XML11IDREFS");
                this.fValNMTOKEN = this.fDatatypeValidatorFactory.getBuiltInDV("XML11NMTOKEN");
                this.fValNMTOKENS = this.fDatatypeValidatorFactory.getBuiltInDV("XML11NMTOKENS");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
