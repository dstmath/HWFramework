package huawei.android.security.secai.hookcase.demo;

import android.util.Log;

public class StaticDemo {
    private static final String TAG = StaticDemo.class.getSimpleName();

    private StaticDemo() {
    }

    public static int multiplyNum(int x1, int y1) {
        Log.i(TAG, "Call Original Method: multiplyNum().");
        return x1 * y1;
    }
}
