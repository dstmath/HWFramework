package ohos.com.sun.org.apache.xerces.internal.utils;

import ohos.com.sun.org.apache.xerces.internal.impl.Constants;

public final class XMLSecurityPropertyManager {
    private State[] states = {State.DEFAULT, State.DEFAULT};
    private final String[] values = new String[Property.values().length];

    public enum State {
        DEFAULT,
        FSP,
        JAXPDOTPROPERTIES,
        SYSTEMPROPERTY,
        APIPROPERTY
    }

    public enum Property {
        ACCESS_EXTERNAL_DTD("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", "all"),
        ACCESS_EXTERNAL_SCHEMA("http://ohos.javax.xml.XMLConstants/property/accessExternalSchema", "all");
        
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
        Property[] values2 = Property.values();
        for (Property property : values2) {
            this.values[property.ordinal()] = property.defaultValue();
        }
        readSystemProperties();
    }

    public boolean setValue(String str, State state, Object obj) {
        int index = getIndex(str);
        if (index <= -1) {
            return false;
        }
        setValue(index, state, (String) obj);
        return true;
    }

    public void setValue(Property property, State state, String str) {
        if (state.compareTo(this.states[property.ordinal()]) >= 0) {
            this.values[property.ordinal()] = str;
            this.states[property.ordinal()] = state;
        }
    }

    public void setValue(int i, State state, String str) {
        if (state.compareTo(this.states[i]) >= 0) {
            this.values[i] = str;
            this.states[i] = state;
        }
    }

    public String getValue(String str) {
        int index = getIndex(str);
        if (index > -1) {
            return getValueByIndex(index);
        }
        return null;
    }

    public String getValue(Property property) {
        return this.values[property.ordinal()];
    }

    public String getValueByIndex(int i) {
        return this.values[i];
    }

    public int getIndex(String str) {
        Property[] values2 = Property.values();
        for (Property property : values2) {
            if (property.equalsName(str)) {
                return property.ordinal();
            }
        }
        return -1;
    }

    private void readSystemProperties() {
        getSystemProperty(Property.ACCESS_EXTERNAL_DTD, "javax.xml.accessExternalDTD");
        getSystemProperty(Property.ACCESS_EXTERNAL_SCHEMA, Constants.SP_ACCESS_EXTERNAL_SCHEMA);
    }

    private void getSystemProperty(Property property, String str) {
        try {
            String systemProperty = SecuritySupport.getSystemProperty(str);
            if (systemProperty != null) {
                this.values[property.ordinal()] = systemProperty;
                this.states[property.ordinal()] = State.SYSTEMPROPERTY;
                return;
            }
            String readJAXPProperty = SecuritySupport.readJAXPProperty(str);
            if (readJAXPProperty != null) {
                this.values[property.ordinal()] = readJAXPProperty;
                this.states[property.ordinal()] = State.JAXPDOTPROPERTIES;
            }
        } catch (NumberFormatException unused) {
        }
    }
}
