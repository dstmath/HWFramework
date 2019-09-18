package com.huawei.android.app;

import android.app.ITransientNotification;
import android.os.IBinder;

public abstract class TransientNotificationEx {
    private ITransientNotification.Stub mTNStub = new ITransientNotification.Stub() {
        public void show(IBinder binder) {
            TransientNotificationEx.this.show(binder);
        }

        public void hide() {
            TransientNotificationEx.this.hide();
        }
    };

    public abstract void hide();

    public abstract void show(IBinder iBinder);

    public ITransientNotification.Stub getStub() {
        return this.mTNStub;
    }
}
