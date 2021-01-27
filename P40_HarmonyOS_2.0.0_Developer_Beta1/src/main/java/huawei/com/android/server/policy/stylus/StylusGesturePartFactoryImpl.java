package huawei.com.android.server.policy.stylus;

import android.content.Context;
import com.huawei.server.DefaultHwBasicPlatformPartFactory;

public class StylusGesturePartFactoryImpl extends DefaultHwBasicPlatformPartFactory {
    public StylusGestureListener getStylusGestureListener(Context context) {
        return new StylusGestureListener(context);
    }
}
