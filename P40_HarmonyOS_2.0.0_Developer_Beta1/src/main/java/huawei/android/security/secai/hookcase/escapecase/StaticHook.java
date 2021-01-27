package huawei.android.security.secai.hookcase.escapecase;

public class StaticHook {
    private StaticHook() {
    }

    public static int multiplyNumHook(int x, int y) {
        return multiplyNumBackup(x, y) * 2;
    }

    public static int multiplyNumBackup(int x, int y) {
        for (int i = 0; i < 1; i++) {
        }
        return -1;
    }
}
