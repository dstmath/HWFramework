package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface XMLDTDValidatorFilter extends XMLDocumentFilter {
    boolean hasGrammar();

    boolean validate();
}
