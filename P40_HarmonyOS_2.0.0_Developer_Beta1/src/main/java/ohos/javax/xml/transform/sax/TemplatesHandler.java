package ohos.javax.xml.transform.sax;

import ohos.javax.xml.transform.Templates;
import ohos.org.xml.sax.ContentHandler;

public interface TemplatesHandler extends ContentHandler {
    String getSystemId();

    Templates getTemplates();

    void setSystemId(String str);
}
