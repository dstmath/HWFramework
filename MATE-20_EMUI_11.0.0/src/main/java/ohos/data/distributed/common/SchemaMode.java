package ohos.data.distributed.common;

public enum SchemaMode {
    STRICT("STRICT"),
    COMPATIBLE("COMPATIBLE");
    
    private String code;

    private SchemaMode(String str) {
        this.code = str;
    }

    public String getCode() {
        return this.code;
    }
}
