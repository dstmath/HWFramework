package ohos.jdk.xml.internal;

public class JdkXmlFeatures {
    public static final String ORACLE_ENABLE_EXTENSION_FUNCTION = "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions";
    public static final String ORACLE_FEATURE_SERVICE_MECHANISM = "http://www.oracle.com/feature/use-service-mechanism";
    public static final String ORACLE_JAXP_PROPERTY_PREFIX = "http://www.oracle.com/xml/jaxp/properties/";
    public static final String SP_ENABLE_EXTENSION_FUNCTION = "javax.xml.enableExtensionFunctions";
    public static final String SP_ENABLE_EXTENSION_FUNCTION_SPEC = "jdk.xml.enableExtensionFunctions";
    public static final String XML_FEATURE_MANAGER = "http://www.oracle.com/xml/jaxp/properties/XmlFeatureManager";
    private final boolean[] featureValues = new boolean[XmlFeature.values().length];
    boolean secureProcessing;
    private final State[] states = new State[XmlFeature.values().length];

    public enum XmlFeature {
        ENABLE_EXTENSION_FUNCTION(JdkXmlFeatures.ORACLE_ENABLE_EXTENSION_FUNCTION, JdkXmlFeatures.SP_ENABLE_EXTENSION_FUNCTION_SPEC, JdkXmlFeatures.ORACLE_ENABLE_EXTENSION_FUNCTION, JdkXmlFeatures.SP_ENABLE_EXTENSION_FUNCTION, true, false, true, true),
        JDK_OVERRIDE_PARSER(JdkXmlUtils.OVERRIDE_PARSER, JdkXmlUtils.OVERRIDE_PARSER, JdkXmlFeatures.ORACLE_FEATURE_SERVICE_MECHANISM, JdkXmlFeatures.ORACLE_FEATURE_SERVICE_MECHANISM, false, false, true, false);
        
        private final boolean enforced;
        private final boolean hasSystem;
        private final String name;
        private final String nameOld;
        private final String nameOldSP;
        private final String nameSP;
        private final boolean valueDefault;
        private final boolean valueEnforced;

        private XmlFeature(String str, String str2, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4) {
            this.name = str;
            this.nameSP = str2;
            this.nameOld = str3;
            this.nameOldSP = str4;
            this.valueDefault = z;
            this.valueEnforced = z2;
            this.hasSystem = z3;
            this.enforced = z4;
        }

        /* access modifiers changed from: package-private */
        public boolean equalsPropertyName(String str) {
            String str2;
            return this.name.equals(str) || ((str2 = this.nameOld) != null && str2.equals(str));
        }

        public String apiProperty() {
            return this.name;
        }

        /* access modifiers changed from: package-private */
        public String systemProperty() {
            return this.nameSP;
        }

        /* access modifiers changed from: package-private */
        public String systemPropertyOld() {
            return this.nameOldSP;
        }

        public boolean defaultValue() {
            return this.valueDefault;
        }

        public boolean enforcedValue() {
            return this.valueEnforced;
        }

        /* access modifiers changed from: package-private */
        public boolean hasSystemProperty() {
            return this.hasSystem;
        }

        /* access modifiers changed from: package-private */
        public boolean enforced() {
            return this.enforced;
        }
    }

    public enum State {
        DEFAULT("default"),
        FSP("FEATURE_SECURE_PROCESSING"),
        JAXPDOTPROPERTIES("jaxp.properties"),
        SYSTEMPROPERTY("system property"),
        APIPROPERTY("property");
        
        final String literal;

        private State(String str) {
            this.literal = str;
        }

        /* access modifiers changed from: package-private */
        public String literal() {
            return this.literal;
        }
    }

    public JdkXmlFeatures(boolean z) {
        this.secureProcessing = z;
        XmlFeature[] values = XmlFeature.values();
        for (XmlFeature xmlFeature : values) {
            if (!z || !xmlFeature.enforced()) {
                this.featureValues[xmlFeature.ordinal()] = xmlFeature.defaultValue();
                this.states[xmlFeature.ordinal()] = State.DEFAULT;
            } else {
                this.featureValues[xmlFeature.ordinal()] = xmlFeature.enforcedValue();
                this.states[xmlFeature.ordinal()] = State.FSP;
            }
        }
        readSystemProperties();
    }

    public void update() {
        readSystemProperties();
    }

    public boolean setFeature(String str, State state, Object obj) {
        int index = getIndex(str);
        if (index <= -1) {
            return false;
        }
        setFeature(index, state, obj);
        return true;
    }

    public void setFeature(XmlFeature xmlFeature, State state, boolean z) {
        setFeature(xmlFeature.ordinal(), state, z);
    }

    public boolean getFeature(XmlFeature xmlFeature) {
        return this.featureValues[xmlFeature.ordinal()];
    }

    public boolean getFeature(int i) {
        return this.featureValues[i];
    }

    public void setFeature(int i, State state, Object obj) {
        boolean z;
        if (Boolean.class.isAssignableFrom(obj.getClass())) {
            z = ((Boolean) obj).booleanValue();
        } else {
            z = Boolean.parseBoolean((String) obj);
        }
        setFeature(i, state, z);
    }

    public void setFeature(int i, State state, boolean z) {
        if (state.compareTo(this.states[i]) >= 0) {
            this.featureValues[i] = z;
            this.states[i] = state;
        }
    }

    public int getIndex(String str) {
        XmlFeature[] values = XmlFeature.values();
        for (XmlFeature xmlFeature : values) {
            if (xmlFeature.equalsPropertyName(str)) {
                return xmlFeature.ordinal();
            }
        }
        return -1;
    }

    private void readSystemProperties() {
        String systemPropertyOld;
        XmlFeature[] values = XmlFeature.values();
        for (XmlFeature xmlFeature : values) {
            if (!getSystemProperty(xmlFeature, xmlFeature.systemProperty()) && (systemPropertyOld = xmlFeature.systemPropertyOld()) != null) {
                getSystemProperty(xmlFeature, systemPropertyOld);
            }
        }
    }

    private boolean getSystemProperty(XmlFeature xmlFeature, String str) {
        try {
            String systemProperty = SecuritySupport.getSystemProperty(str);
            if (systemProperty == null || systemProperty.equals("")) {
                String readJAXPProperty = SecuritySupport.readJAXPProperty(str);
                if (readJAXPProperty == null || readJAXPProperty.equals("")) {
                    return false;
                }
                setFeature(xmlFeature, State.JAXPDOTPROPERTIES, Boolean.parseBoolean(readJAXPProperty));
                return true;
            }
            setFeature(xmlFeature, State.SYSTEMPROPERTY, Boolean.parseBoolean(systemProperty));
            return true;
        } catch (NumberFormatException unused) {
            throw new NumberFormatException("Invalid setting for system property: " + xmlFeature.systemProperty());
        }
    }
}
