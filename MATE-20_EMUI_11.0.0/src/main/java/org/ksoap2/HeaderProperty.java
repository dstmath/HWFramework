package org.ksoap2;

public class HeaderProperty {
    private String key;
    private String value;

    public HeaderProperty(String key2, String value2) {
        this.key = key2;
        this.value = value2;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }
}
