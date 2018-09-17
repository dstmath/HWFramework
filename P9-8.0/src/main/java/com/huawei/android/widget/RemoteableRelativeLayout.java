package com.huawei.android.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.RelativeLayout;

public class RemoteableRelativeLayout extends RelativeLayout {
    public RemoteableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteableRelativeLayout(Context context) {
        super(context);
    }

    public RemoteableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RemoteableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @RemotableViewMethod
    public void callRemoteableMethod(Bundle bundle) {
        onCallRemoteable(bundle);
    }

    protected void onCallRemoteable(Bundle bundle) {
    }
}
