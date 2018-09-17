package com.huawei.android.view;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class RemoteableView extends View {
    public RemoteableView(Context context) {
        this(context, null);
    }

    public RemoteableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RemoteableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @RemotableViewMethod
    public void callRemoteableMethod(Bundle bundle) {
        onCallRemoteable(bundle);
    }

    protected void onCallRemoteable(Bundle bundle) {
    }
}
