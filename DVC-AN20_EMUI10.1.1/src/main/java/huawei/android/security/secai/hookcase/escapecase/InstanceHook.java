package huawei.android.security.secai.hookcase.escapecase;

public class InstanceHook {
    private InstanceHook() {
    }

    public static int addNumHook(Object obj, int x, int y) {
        return addNumBackup(obj, x, y) * 2;
    }

    public static int addNumBackup(Object obj, int x, int y) {
        for (int i = 0; i < 1; i++) {
        }
        return -1;
    }
}
