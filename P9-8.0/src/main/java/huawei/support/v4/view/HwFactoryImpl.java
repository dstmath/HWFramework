package huawei.support.v4.view;

import android.content.Context;
import android.support.v4.interfaces.HwControlFactory.Factory;
import android.support.v4.interfaces.HwControlFactory.HwViewPager;

public class HwFactoryImpl implements Factory {
    public HwViewPager newHwViewPager(Context context) {
        return new HwViewPagerImpl(context);
    }
}
