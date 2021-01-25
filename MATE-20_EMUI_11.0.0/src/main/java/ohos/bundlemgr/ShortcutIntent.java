package ohos.bundlemgr;

public class ShortcutIntent {
    private String targetBundle;
    private String targetClass;

    public ShortcutIntent(String str, String str2) {
        this.targetBundle = str;
        this.targetClass = str2;
    }

    public String getTargetBundle() {
        return this.targetBundle;
    }

    public String getTargetClass() {
        return this.targetClass;
    }
}
