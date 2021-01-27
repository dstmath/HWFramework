package ohos.com.sun.org.apache.xalan.internal.utils;

import java.io.PrintStream;
import java.util.concurrent.CopyOnWriteArrayList;
import ohos.bluetooth.A2dpCodecInfo;
import ohos.org.xml.sax.SAXException;

public final class XMLSecurityManager {
    private static final CopyOnWriteArrayList<String> printedWarnings = new CopyOnWriteArrayList<>();
    private final int indexEntityCountInfo;
    private boolean[] isSet;
    private String printEntityCountInfo;
    private State[] states;
    private final int[] values;

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

    public enum Limit {
        ENTITY_EXPANSION_LIMIT("EntityExpansionLimit", "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", "jdk.xml.entityExpansionLimit", 0, 64000),
        MAX_OCCUR_NODE_LIMIT("MaxOccurLimit", "http://www.oracle.com/xml/jaxp/properties/maxOccurLimit", "jdk.xml.maxOccurLimit", 0, 5000),
        ELEMENT_ATTRIBUTE_LIMIT("ElementAttributeLimit", "http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit", "jdk.xml.elementAttributeLimit", 0, 10000),
        TOTAL_ENTITY_SIZE_LIMIT("TotalEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit", "jdk.xml.totalEntitySizeLimit", 0, 50000000),
        GENERAL_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/maxGeneralEntitySizeLimit", "jdk.xml.maxGeneralEntitySizeLimit", 0, 0),
        PARAMETER_ENTITY_SIZE_LIMIT("MaxEntitySizeLimit", "http://www.oracle.com/xml/jaxp/properties/maxParameterEntitySizeLimit", "jdk.xml.maxParameterEntitySizeLimit", 0, A2dpCodecInfo.CODEC_PRIORITY_HIGHEST),
        MAX_ELEMENT_DEPTH_LIMIT("MaxElementDepthLimit", "http://www.oracle.com/xml/jaxp/properties/maxElementDepth", "jdk.xml.maxElementDepth", 0, 0),
        MAX_NAME_LIMIT("MaxXMLNameLimit", "http://www.oracle.com/xml/jaxp/properties/maxXMLNameLimit", "jdk.xml.maxXMLNameLimit", 1000, 1000),
        ENTITY_REPLACEMENT_LIMIT("EntityReplacementLimit", "http://www.oracle.com/xml/jaxp/properties/entityReplacementLimit", "jdk.xml.entityReplacementLimit", 0, 3000000);
        
        final String apiProperty;
        final int defaultValue;
        final String key;
        final int secureValue;
        final String systemProperty;

        private Limit(String str, String str2, String str3, int i, int i2) {
            this.key = str;
            this.apiProperty = str2;
            this.systemProperty = str3;
            this.defaultValue = i;
            this.secureValue = i2;
        }

        public boolean equalsAPIPropertyName(String str) {
            if (str == null) {
                return false;
            }
            return this.apiProperty.equals(str);
        }

        public boolean equalsSystemPropertyName(String str) {
            if (str == null) {
                return false;
            }
            return this.systemProperty.equals(str);
        }

        public String key() {
            return this.key;
        }

        public String apiProperty() {
            return this.apiProperty;
        }

        /* access modifiers changed from: package-private */
        public String systemProperty() {
            return this.systemProperty;
        }

        public int defaultValue() {
            return this.defaultValue;
        }

        /* access modifiers changed from: package-private */
        public int secureValue() {
            return this.secureValue;
        }
    }

    public enum NameMap {
        ENTITY_EXPANSION_LIMIT("jdk.xml.entityExpansionLimit", "entityExpansionLimit"),
        MAX_OCCUR_NODE_LIMIT("jdk.xml.maxOccurLimit", "maxOccurLimit"),
        ELEMENT_ATTRIBUTE_LIMIT("jdk.xml.elementAttributeLimit", "elementAttributeLimit");
        
        final String newName;
        final String oldName;

        private NameMap(String str, String str2) {
            this.newName = str;
            this.oldName = str2;
        }

        /* access modifiers changed from: package-private */
        public String getOldName(String str) {
            if (str.equals(this.newName)) {
                return this.oldName;
            }
            return null;
        }
    }

    public XMLSecurityManager() {
        this(false);
    }

    public XMLSecurityManager(boolean z) {
        this.indexEntityCountInfo = 10000;
        this.printEntityCountInfo = "";
        this.values = new int[Limit.values().length];
        this.states = new State[Limit.values().length];
        this.isSet = new boolean[Limit.values().length];
        Limit[] values2 = Limit.values();
        for (Limit limit : values2) {
            if (z) {
                this.values[limit.ordinal()] = limit.secureValue();
                this.states[limit.ordinal()] = State.FSP;
            } else {
                this.values[limit.ordinal()] = limit.defaultValue();
                this.states[limit.ordinal()] = State.DEFAULT;
            }
        }
        readSystemProperties();
    }

    public void setSecureProcessing(boolean z) {
        Limit[] values2 = Limit.values();
        for (Limit limit : values2) {
            if (z) {
                setLimit(limit.ordinal(), State.FSP, limit.secureValue());
            } else {
                setLimit(limit.ordinal(), State.FSP, limit.defaultValue());
            }
        }
    }

    public boolean setLimit(String str, State state, Object obj) {
        int index = getIndex(str);
        if (index <= -1) {
            return false;
        }
        setLimit(index, state, obj);
        return true;
    }

    public void setLimit(Limit limit, State state, int i) {
        setLimit(limit.ordinal(), state, i);
    }

    public void setLimit(int i, State state, Object obj) {
        if (i == 10000) {
            this.printEntityCountInfo = (String) obj;
            return;
        }
        int i2 = 0;
        try {
            int parseInt = Integer.parseInt((String) obj);
            if (parseInt >= 0) {
                i2 = parseInt;
            }
        } catch (NumberFormatException unused) {
        }
        setLimit(i, state, i2);
    }

    public void setLimit(int i, State state, int i2) {
        if (i == 10000) {
            this.printEntityCountInfo = "yes";
        } else if (state.compareTo(this.states[i]) >= 0) {
            this.values[i] = i2;
            this.states[i] = state;
            this.isSet[i] = true;
        }
    }

    public String getLimitAsString(String str) {
        int index = getIndex(str);
        if (index > -1) {
            return getLimitValueByIndex(index);
        }
        return null;
    }

    public String getLimitValueAsString(Limit limit) {
        return Integer.toString(this.values[limit.ordinal()]);
    }

    public int getLimit(Limit limit) {
        return this.values[limit.ordinal()];
    }

    public int getLimitByIndex(int i) {
        return this.values[i];
    }

    public String getLimitValueByIndex(int i) {
        if (i == 10000) {
            return this.printEntityCountInfo;
        }
        return Integer.toString(this.values[i]);
    }

    public State getState(Limit limit) {
        return this.states[limit.ordinal()];
    }

    public String getStateLiteral(Limit limit) {
        return this.states[limit.ordinal()].literal();
    }

    public int getIndex(String str) {
        Limit[] values2 = Limit.values();
        for (Limit limit : values2) {
            if (limit.equalsAPIPropertyName(str)) {
                return limit.ordinal();
            }
        }
        return str.equals("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo") ? 10000 : -1;
    }

    public boolean isSet(int i) {
        return this.isSet[i];
    }

    public boolean printEntityCountInfo() {
        return this.printEntityCountInfo.equals("yes");
    }

    private void readSystemProperties() {
        Limit[] values2 = Limit.values();
        for (Limit limit : values2) {
            if (!getSystemProperty(limit, limit.systemProperty())) {
                for (NameMap nameMap : NameMap.values()) {
                    String oldName = nameMap.getOldName(limit.systemProperty());
                    if (oldName != null) {
                        getSystemProperty(limit, oldName);
                    }
                }
            }
        }
    }

    public static void printWarning(String str, String str2, SAXException sAXException) {
        if (printedWarnings.addIfAbsent(str + ":" + str2)) {
            PrintStream printStream = System.err;
            printStream.println("Warning: " + str + ": " + sAXException.getMessage());
        }
    }

    private boolean getSystemProperty(Limit limit, String str) {
        try {
            String systemProperty = SecuritySupport.getSystemProperty(str);
            if (systemProperty == null || systemProperty.equals("")) {
                String readJAXPProperty = SecuritySupport.readJAXPProperty(str);
                if (readJAXPProperty == null || readJAXPProperty.equals("")) {
                    return false;
                }
                this.values[limit.ordinal()] = Integer.parseInt(readJAXPProperty);
                this.states[limit.ordinal()] = State.JAXPDOTPROPERTIES;
                return true;
            }
            this.values[limit.ordinal()] = Integer.parseInt(systemProperty);
            this.states[limit.ordinal()] = State.SYSTEMPROPERTY;
            return true;
        } catch (NumberFormatException unused) {
            throw new NumberFormatException("Invalid setting for system property: " + limit.systemProperty());
        }
    }
}
