package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;

public class ParserConfigurationSettings implements XMLComponentManager {
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    protected Map<String, Boolean> fFeatures;
    protected XMLComponentManager fParentSettings;
    protected Map<String, Object> fProperties;
    protected Set<String> fRecognizedFeatures;
    protected Set<String> fRecognizedProperties;

    public ParserConfigurationSettings() {
        this(null);
    }

    public ParserConfigurationSettings(XMLComponentManager xMLComponentManager) {
        this.fRecognizedFeatures = new HashSet();
        this.fRecognizedProperties = new HashSet();
        this.fFeatures = new HashMap();
        this.fProperties = new HashMap();
        this.fParentSettings = xMLComponentManager;
    }

    public void addRecognizedFeatures(String[] strArr) {
        int length = strArr != null ? strArr.length : 0;
        for (int i = 0; i < length; i++) {
            String str = strArr[i];
            if (!this.fRecognizedFeatures.contains(str)) {
                this.fRecognizedFeatures.add(str);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        FeatureState checkFeature = checkFeature(str);
        if (!checkFeature.isExceptional()) {
            this.fFeatures.put(str, Boolean.valueOf(z));
            return;
        }
        throw new XMLConfigurationException(checkFeature.status, str);
    }

    public void addRecognizedProperties(String[] strArr) {
        this.fRecognizedProperties.addAll(Arrays.asList(strArr));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        PropertyState checkProperty = checkProperty(str);
        if (!checkProperty.isExceptional()) {
            this.fProperties.put(str, obj);
            return;
        }
        throw new XMLConfigurationException(checkProperty.status, str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public final boolean getFeature(String str) throws XMLConfigurationException {
        FeatureState featureState = getFeatureState(str);
        if (!featureState.isExceptional()) {
            return featureState.state;
        }
        throw new XMLConfigurationException(featureState.status, str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public final boolean getFeature(String str, boolean z) {
        FeatureState featureState = getFeatureState(str);
        if (featureState.isExceptional()) {
            return z;
        }
        return featureState.state;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public FeatureState getFeatureState(String str) {
        Boolean bool = this.fFeatures.get(str);
        if (bool != null) {
            return FeatureState.is(bool.booleanValue());
        }
        FeatureState checkFeature = checkFeature(str);
        if (checkFeature.isExceptional()) {
            return checkFeature;
        }
        return FeatureState.is(false);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public final Object getProperty(String str) throws XMLConfigurationException {
        PropertyState propertyState = getPropertyState(str);
        if (!propertyState.isExceptional()) {
            return propertyState.state;
        }
        throw new XMLConfigurationException(propertyState.status, str);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public final Object getProperty(String str, Object obj) {
        PropertyState propertyState = getPropertyState(str);
        if (propertyState.isExceptional()) {
            return obj;
        }
        return propertyState.state;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager
    public PropertyState getPropertyState(String str) {
        Object obj = this.fProperties.get(str);
        if (obj == null) {
            PropertyState checkProperty = checkProperty(str);
            if (checkProperty.isExceptional()) {
                return checkProperty;
            }
        }
        return PropertyState.is(obj);
    }

    /* access modifiers changed from: protected */
    public FeatureState checkFeature(String str) throws XMLConfigurationException {
        if (this.fRecognizedFeatures.contains(str)) {
            return FeatureState.RECOGNIZED;
        }
        XMLComponentManager xMLComponentManager = this.fParentSettings;
        if (xMLComponentManager != null) {
            return xMLComponentManager.getFeatureState(str);
        }
        return FeatureState.NOT_RECOGNIZED;
    }

    /* access modifiers changed from: protected */
    public PropertyState checkProperty(String str) throws XMLConfigurationException {
        if (!this.fRecognizedProperties.contains(str)) {
            XMLComponentManager xMLComponentManager = this.fParentSettings;
            if (xMLComponentManager == null) {
                return PropertyState.NOT_RECOGNIZED;
            }
            PropertyState propertyState = xMLComponentManager.getPropertyState(str);
            if (propertyState.isExceptional()) {
                return propertyState;
            }
        }
        return PropertyState.RECOGNIZED;
    }
}
