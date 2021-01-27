package ohos.data;

public enum DatabaseFileType {
    NORMAL("db"),
    BACKUP("backup"),
    CORRUPT("corrupt");
    
    private String value;

    private DatabaseFileType(String str) {
        this.value = str;
    }

    public String getValue() {
        return this.value;
    }
}
