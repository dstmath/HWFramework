package android.test;

import android.content.Context;

@Deprecated
public interface TestCase extends Runnable {
    void setUp(Context context);

    void tearDown();
}
