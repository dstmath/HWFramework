package ohos.com.sun.org.apache.xalan.internal.utils;

import ohos.com.sun.org.apache.xalan.internal.XalanConstants;

public final class XMLSecurityPropertyManager extends FeaturePropertyBase {

    public enum Property {
        ACCESS_EXTERNAL_DTD("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", "all"),
        ACCESS_EXTERNAL_STYLESHEET("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet", "all");
        
        final String defaultValue;
        final String name;

        private Property(String str, String str2) {
            this.name = str;
            this.defaultValue = str2;
        }

        public boolean equalsName(String str) {
            if (str == null) {
                return false;
            }
            return this.name.equals(str);
        }

        /* access modifiers changed from: package-private */
        public String defaultValue() {
            return this.defaultValue;
        }
    }

    public XMLSecurityPropertyManager() {
        this.values = new String[Property.values().length];
        Property[] values = Property.values();
        for (Property property : values) {
            this.values[property.ordinal()] = property.defaultValue();
        }
        readSystemProperties();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.utils.FeaturePropertyBase
    public int getIndex(String str) {
        Property[] values = Property.values();
        for (Property property : values) {
            if (property.equalsName(str)) {
                return property.ordinal();
            }
        }
        return -1;
    }

    private void readSystemProperties() {
        getSystemProperty(Property.ACCESS_EXTERNAL_DTD, "javax.xml.accessExternalDTD");
        getSystemProperty(Property.ACCESS_EXTERNAL_STYLESHEET, XalanConstants.SP_ACCESS_EXTERNAL_STYLESHEET);
    }
}
