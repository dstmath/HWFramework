package com.android.server.swing;

import android.util.Log;
import com.android.server.swing.HwAwarenessServiceConnector;
import com.android.server.swing.notification.HwSwingEventAvailabler;
import com.android.server.swing.notification.IHwSwingEventDispatcher;
import com.huawei.hiai.awareness.service.AwarenessManager;

public abstract class HwSwingEventProcessor implements HwAwarenessServiceConnector.Listener, HwSwingEventAvailabler.IAvailableListener {
    protected static final int STATUS_INIT = -1;
    protected static final int STATUS_REGISTERED = 1;
    protected static final int STATUS_REGISTERING = 0;
    protected static final int STATUS_UNREGISTERED = 3;
    protected static final int STATUS_UNREGISTERING = 2;
    protected final String TAG = getClass().getSimpleName();
    protected HwSwingEventAvailabler mAvailabler;
    protected AwarenessManager mAwarenessManager;
    protected IHwSwingEventDispatcher mEventDispatcher;
    private int mRegistedStatus = -1;

    /* access modifiers changed from: protected */
    public abstract void register();

    /* access modifiers changed from: protected */
    public abstract void unRegister();

    public HwSwingEventProcessor(IHwSwingEventDispatcher eventDispatcher, HwSwingEventAvailabler availabler) {
        this.mEventDispatcher = eventDispatcher;
        this.mAvailabler = availabler;
    }

    @Override // com.android.server.swing.HwAwarenessServiceConnector.Listener
    public void setAwarenessManager(AwarenessManager awarenessManager) {
        this.mAwarenessManager = awarenessManager;
    }

    @Override // com.android.server.swing.HwAwarenessServiceConnector.Listener
    public void onServiceConnectedStateChanged(boolean isConnected) {
        if (!isEnabled()) {
            Log.w(this.TAG, "onServiceConnectedStateChanged: processor is not enabled");
        } else if (isConnected) {
            doInit();
            if (isAvailable()) {
                register();
            }
        } else {
            doRelease();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isEnabled() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isAvailable() {
        HwSwingEventAvailabler hwSwingEventAvailabler = this.mAvailabler;
        if (hwSwingEventAvailabler != null) {
            return hwSwingEventAvailabler.isAvailable();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setRegisterStatus(int registerStatus) {
        this.mRegistedStatus = registerStatus;
    }

    /* access modifiers changed from: protected */
    public int getRegisterStatus() {
        return this.mRegistedStatus;
    }

    /* access modifiers changed from: protected */
    public boolean isRegistered() {
        int i = this.mRegistedStatus;
        return i == 0 || i == 1;
    }

    /* access modifiers changed from: protected */
    public void doInit() {
        setRegisterStatus(-1);
        HwSwingEventAvailabler hwSwingEventAvailabler = this.mAvailabler;
        if (hwSwingEventAvailabler != null) {
            hwSwingEventAvailabler.addListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public void doRelease() {
        setRegisterStatus(-1);
        HwSwingEventAvailabler hwSwingEventAvailabler = this.mAvailabler;
        if (hwSwingEventAvailabler != null) {
            hwSwingEventAvailabler.removeListener(this);
        }
    }

    @Override // com.android.server.swing.notification.HwSwingEventAvailabler.IAvailableListener
    public void onAvailableChanged(boolean isAvailable) {
        String str = this.TAG;
        Log.i(str, "onAvailableChanged: isAvailable=" + isAvailable + ";registerStatus=" + this.mRegistedStatus);
        if (isAvailable) {
            register();
        } else {
            unRegister();
        }
    }
}
