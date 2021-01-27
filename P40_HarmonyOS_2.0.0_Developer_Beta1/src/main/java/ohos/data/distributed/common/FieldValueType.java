package ohos.data.distributed.common;

public enum FieldValueType {
    STRING("STRING"),
    INTEGER("INTEGER"),
    LONG("LONG"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOL"),
    JSON_ARRAY("JSON_ARRAY"),
    JSON_OBJECT("JSON_OBJECT");
    
    private String code;

    private FieldValueType(String str) {
        this.code = str;
    }

    public String getCode() {
        return this.code;
    }
}
