package ohos.com.sun.org.apache.xml.internal.serializer;

import java.util.Vector;

interface XSLOutputAttributes {
    String getDoctypePublic();

    String getDoctypeSystem();

    String getEncoding();

    boolean getIndent();

    int getIndentAmount();

    String getMediaType();

    boolean getOmitXMLDeclaration();

    String getStandalone();

    String getVersion();

    void setCdataSectionElements(Vector vector);

    void setDoctype(String str, String str2);

    void setDoctypePublic(String str);

    void setDoctypeSystem(String str);

    void setEncoding(String str);

    void setIndent(boolean z);

    void setMediaType(String str);

    void setOmitXMLDeclaration(boolean z);

    void setStandalone(String str);

    void setVersion(String str);
}
