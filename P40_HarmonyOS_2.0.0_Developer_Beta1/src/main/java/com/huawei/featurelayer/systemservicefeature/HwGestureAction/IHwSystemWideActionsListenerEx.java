package com.huawei.featurelayer.systemservicefeature.HwGestureAction;

import android.content.Context;
import android.view.MotionEvent;
import com.huawei.featurelayer.featureframework.IFeature;

public interface IHwSystemWideActionsListenerEx extends IFeature {
    public static final String CLASS = "IHwSystemWideActionsListenerEx";
    public static final String PACKAGE = "com.huawei.featurelayer.systemservicefeature.HwGestureAction";

    void create(Context context);

    void createPointerLocationView();

    void destroyPointerLocationView();

    void onPointerEvent(MotionEvent motionEvent);

    void setOrientation(int i);

    void updateConfiguration();
}
