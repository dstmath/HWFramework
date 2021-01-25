package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import java.io.IOException;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLParserConfiguration extends XMLComponentManager {
    void addRecognizedFeatures(String[] strArr);

    void addRecognizedProperties(String[] strArr);

    XMLDTDContentModelHandler getDTDContentModelHandler();

    XMLDTDHandler getDTDHandler();

    XMLDocumentHandler getDocumentHandler();

    XMLEntityResolver getEntityResolver();

    XMLErrorHandler getErrorHandler();

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    boolean getFeature(String str) throws XMLConfigurationException;

    Locale getLocale();

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    Object getProperty(String str) throws XMLConfigurationException;

    void parse(XMLInputSource xMLInputSource) throws XNIException, IOException;

    void setDTDContentModelHandler(XMLDTDContentModelHandler xMLDTDContentModelHandler);

    void setDTDHandler(XMLDTDHandler xMLDTDHandler);

    void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler);

    void setEntityResolver(XMLEntityResolver xMLEntityResolver);

    void setErrorHandler(XMLErrorHandler xMLErrorHandler);

    void setFeature(String str, boolean z) throws XMLConfigurationException;

    void setLocale(Locale locale) throws XNIException;

    void setProperty(String str, Object obj) throws XMLConfigurationException;
}
