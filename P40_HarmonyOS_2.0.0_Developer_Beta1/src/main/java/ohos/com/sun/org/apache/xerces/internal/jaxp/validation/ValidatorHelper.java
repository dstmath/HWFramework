package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.org.xml.sax.SAXException;

interface ValidatorHelper {
    void validate(Source source, Result result) throws SAXException, IOException;
}
