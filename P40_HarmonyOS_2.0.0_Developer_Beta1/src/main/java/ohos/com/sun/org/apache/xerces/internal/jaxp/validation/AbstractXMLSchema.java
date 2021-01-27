package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.util.HashMap;
import ohos.javax.xml.validation.Schema;
import ohos.javax.xml.validation.Validator;
import ohos.javax.xml.validation.ValidatorHandler;

abstract class AbstractXMLSchema extends Schema implements XSGrammarPoolContainer {
    private final HashMap fFeatures = new HashMap();
    private final HashMap fProperties = new HashMap();

    public final Validator newValidator() {
        return new ValidatorImpl(this);
    }

    public final ValidatorHandler newValidatorHandler() {
        return new ValidatorHandlerImpl(this);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public final Boolean getFeature(String str) {
        return (Boolean) this.fFeatures.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public final void setFeature(String str, boolean z) {
        this.fFeatures.put(str, z ? Boolean.TRUE : Boolean.FALSE);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public final Object getProperty(String str) {
        return this.fProperties.get(str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public final void setProperty(String str, Object obj) {
        this.fProperties.put(str, obj);
    }
}
