package ohos.com.sun.org.apache.xerces.internal.util;

public final class SecurityManager {
    private static final int DEFAULT_ELEMENT_ATTRIBUTE_LIMIT = 10000;
    private static final int DEFAULT_ENTITY_EXPANSION_LIMIT = 64000;
    private static final int DEFAULT_MAX_OCCUR_NODE_LIMIT = 5000;
    private int entityExpansionLimit = DEFAULT_ENTITY_EXPANSION_LIMIT;
    private int fElementAttributeLimit = 10000;
    private int maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT;

    public SecurityManager() {
        readSystemProperties();
    }

    public void setEntityExpansionLimit(int i) {
        this.entityExpansionLimit = i;
    }

    public int getEntityExpansionLimit() {
        return this.entityExpansionLimit;
    }

    public void setMaxOccurNodeLimit(int i) {
        this.maxOccurLimit = i;
    }

    public int getMaxOccurNodeLimit() {
        return this.maxOccurLimit;
    }

    public int getElementAttrLimit() {
        return this.fElementAttributeLimit;
    }

    public void setElementAttrLimit(int i) {
        this.fElementAttributeLimit = i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x004b A[Catch:{ Exception -> 0x0060 }] */
    private void readSystemProperties() {
        String property;
        String property2;
        try {
            String property3 = System.getProperty("entityExpansionLimit");
            if (property3 == null || property3.equals("")) {
                this.entityExpansionLimit = DEFAULT_ENTITY_EXPANSION_LIMIT;
                try {
                    property2 = System.getProperty("maxOccurLimit");
                    if (property2 != null || property2.equals("")) {
                        this.maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT;
                        try {
                            property = System.getProperty("elementAttributeLimit");
                            if (property != null || property.equals("")) {
                                this.fElementAttributeLimit = 10000;
                            }
                            this.fElementAttributeLimit = Integer.parseInt(property);
                            if (this.fElementAttributeLimit < 0) {
                                this.fElementAttributeLimit = 10000;
                            }
                        } catch (Exception unused) {
                        }
                    } else {
                        this.maxOccurLimit = Integer.parseInt(property2);
                        if (this.maxOccurLimit < 0) {
                            this.maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT;
                        }
                        property = System.getProperty("elementAttributeLimit");
                        if (property != null) {
                        }
                        this.fElementAttributeLimit = 10000;
                    }
                } catch (Exception unused2) {
                }
            } else {
                this.entityExpansionLimit = Integer.parseInt(property3);
                if (this.entityExpansionLimit < 0) {
                    this.entityExpansionLimit = DEFAULT_ENTITY_EXPANSION_LIMIT;
                }
                property2 = System.getProperty("maxOccurLimit");
                if (property2 != null) {
                }
                this.maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT;
                property = System.getProperty("elementAttributeLimit");
                if (property != null) {
                }
                this.fElementAttributeLimit = 10000;
            }
        } catch (Exception unused3) {
        }
    }
}
