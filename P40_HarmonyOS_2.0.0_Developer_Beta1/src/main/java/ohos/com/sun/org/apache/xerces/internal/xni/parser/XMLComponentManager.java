package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.util.FeatureState;
import ohos.com.sun.org.apache.xerces.internal.util.PropertyState;

public interface XMLComponentManager {
    boolean getFeature(String str) throws XMLConfigurationException;

    boolean getFeature(String str, boolean z);

    FeatureState getFeatureState(String str);

    Object getProperty(String str) throws XMLConfigurationException;

    Object getProperty(String str, Object obj);

    PropertyState getPropertyState(String str);
}
