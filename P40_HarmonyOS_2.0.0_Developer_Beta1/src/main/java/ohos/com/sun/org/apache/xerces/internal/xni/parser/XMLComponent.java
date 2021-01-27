package ohos.com.sun.org.apache.xerces.internal.xni.parser;

public interface XMLComponent {
    Boolean getFeatureDefault(String str);

    Object getPropertyDefault(String str);

    String[] getRecognizedFeatures();

    String[] getRecognizedProperties();

    void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException;

    void setFeature(String str, boolean z) throws XMLConfigurationException;

    void setProperty(String str, Object obj) throws XMLConfigurationException;
}
