package ohos.com.sun.org.apache.xerces.internal.utils;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;

public final class XMLLimitAnalyzer {
    private final Map[] caches = new Map[XMLSecurityManager.Limit.values().length];
    private String entityEnd;
    private String entityStart;
    private final String[] names = new String[XMLSecurityManager.Limit.values().length];
    private final int[] totalValue = new int[XMLSecurityManager.Limit.values().length];
    private final int[] values = new int[XMLSecurityManager.Limit.values().length];

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

    public void addValue(XMLSecurityManager.Limit limit, String str, int i) {
        addValue(limit.ordinal(), str, i);
    }

    public void addValue(int i, String str, int i2) {
        Map map;
        int i3;
        if (i == XMLSecurityManager.Limit.ENTITY_EXPANSION_LIMIT.ordinal() || i == XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT.ordinal() || i == XMLSecurityManager.Limit.ELEMENT_ATTRIBUTE_LIMIT.ordinal() || i == XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal() || i == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT.ordinal()) {
            int[] iArr = this.totalValue;
            iArr[i] = iArr[i] + i2;
        } else if (i == XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT.ordinal() || i == XMLSecurityManager.Limit.MAX_NAME_LIMIT.ordinal()) {
            this.values[i] = i2;
            this.totalValue[i] = i2;
        } else {
            Map[] mapArr = this.caches;
            if (mapArr[i] == null) {
                map = new HashMap(10);
                this.caches[i] = map;
            } else {
                map = mapArr[i];
            }
            if (map.containsKey(str)) {
                i3 = ((Integer) map.get(str)).intValue() + i2;
                map.put(str, Integer.valueOf(i3));
            } else {
                map.put(str, Integer.valueOf(i2));
                i3 = i2;
            }
            int[] iArr2 = this.values;
            if (i3 > iArr2[i]) {
                iArr2[i] = i3;
                this.names[i] = str;
            }
            if (i == XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT.ordinal() || i == XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT.ordinal()) {
                int[] iArr3 = this.totalValue;
                int ordinal = XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal();
                iArr3[ordinal] = iArr3[ordinal] + i2;
            }
        }
    }

    public int getValue(XMLSecurityManager.Limit limit) {
        return getValue(limit.ordinal());
    }

    public int getValue(int i) {
        if (i == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT.ordinal()) {
            return this.totalValue[i];
        }
        return this.values[i];
    }

    public int getTotalValue(XMLSecurityManager.Limit limit) {
        return this.totalValue[limit.ordinal()];
    }

    public int getTotalValue(int i) {
        return this.totalValue[i];
    }

    public int getValueByIndex(int i) {
        return this.values[i];
    }

    public void startEntity(String str) {
        this.entityStart = str;
    }

    public boolean isTracking(String str) {
        String str2 = this.entityStart;
        if (str2 == null) {
            return false;
        }
        return str2.equals(str);
    }

    public void endEntity(XMLSecurityManager.Limit limit, String str) {
        this.entityStart = "";
        Map map = this.caches[limit.ordinal()];
        if (map != null) {
            map.remove(str);
        }
    }

    public void reset(XMLSecurityManager.Limit limit) {
        if (limit.ordinal() == XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT.ordinal()) {
            this.totalValue[limit.ordinal()] = 0;
        } else if (limit.ordinal() == XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT.ordinal()) {
            this.names[limit.ordinal()] = null;
            this.values[limit.ordinal()] = 0;
            this.caches[limit.ordinal()] = null;
            this.totalValue[limit.ordinal()] = 0;
        }
    }

    public void debugPrint(XMLSecurityManager xMLSecurityManager) {
        System.out.println(new Formatter().format("%30s %15s %15s %15s %30s", "Property", "Limit", "Total size", "Size", "Entity Name"));
        XMLSecurityManager.Limit[] values2 = XMLSecurityManager.Limit.values();
        for (XMLSecurityManager.Limit limit : values2) {
            System.out.println(new Formatter().format("%30s %15d %15d %15d %30s", limit.name(), Integer.valueOf(xMLSecurityManager.getLimit(limit)), Integer.valueOf(this.totalValue[limit.ordinal()]), Integer.valueOf(this.values[limit.ordinal()]), this.names[limit.ordinal()]));
        }
    }
}
