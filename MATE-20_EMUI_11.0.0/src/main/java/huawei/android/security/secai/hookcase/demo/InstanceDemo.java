package huawei.android.security.secai.hookcase.demo;

import android.util.Log;

public class InstanceDemo {
    private static final String TAG = InstanceDemo.class.getSimpleName();

    public int addNum(int a, int b) {
        Log.i(TAG, "Call Original Method: addNum().");
        return a + b;
    }
}
