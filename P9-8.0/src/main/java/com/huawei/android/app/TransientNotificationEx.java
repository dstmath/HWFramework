package com.huawei.android.app;

import android.app.ITransientNotification.Stub;
import android.os.IBinder;

public abstract class TransientNotificationEx {
    private Stub mTNStub = new Stub() {
        public void show(IBinder binder) {
            TransientNotificationEx.this.show(binder);
        }

        public void hide() {
            TransientNotificationEx.this.hide();
        }
    };

    public abstract void hide();

    public abstract void show(IBinder iBinder);

    public Stub getStub() {
        return this.mTNStub;
    }
}
