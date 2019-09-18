package com.android.ims.internal.uce;

import com.android.ims.internal.uce.common.UceLong;
import com.android.ims.internal.uce.options.IOptionsListener;
import com.android.ims.internal.uce.options.IOptionsService;
import com.android.ims.internal.uce.presence.IPresenceListener;
import com.android.ims.internal.uce.presence.IPresenceService;
import com.android.ims.internal.uce.uceservice.IUceListener;
import com.android.ims.internal.uce.uceservice.IUceService;

public abstract class UceServiceBase {
    private UceServiceBinder mBinder;

    private final class UceServiceBinder extends IUceService.Stub {
        private UceServiceBinder() {
        }

        public boolean startService(IUceListener uceListener) {
            return UceServiceBase.this.onServiceStart(uceListener);
        }

        public boolean stopService() {
            return UceServiceBase.this.onStopService();
        }

        public boolean isServiceStarted() {
            return UceServiceBase.this.onIsServiceStarted();
        }

        public int createOptionsService(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) {
            return UceServiceBase.this.onCreateOptionsService(optionsListener, optionsServiceListenerHdl);
        }

        public void destroyOptionsService(int optionsServiceHandle) {
            UceServiceBase.this.onDestroyOptionsService(optionsServiceHandle);
        }

        public int createPresenceService(IPresenceListener presServiceListener, UceLong presServiceListenerHdl) {
            return UceServiceBase.this.onCreatePresService(presServiceListener, presServiceListenerHdl);
        }

        public void destroyPresenceService(int presServiceHdl) {
            UceServiceBase.this.onDestroyPresService(presServiceHdl);
        }

        public boolean getServiceStatus() {
            return UceServiceBase.this.onGetServiceStatus();
        }

        public IPresenceService getPresenceService() {
            return UceServiceBase.this.onGetPresenceService();
        }

        public IOptionsService getOptionsService() {
            return UceServiceBase.this.onGetOptionsService();
        }
    }

    public UceServiceBinder getBinder() {
        if (this.mBinder == null) {
            this.mBinder = new UceServiceBinder();
        }
        return this.mBinder;
    }

    /* access modifiers changed from: protected */
    public boolean onServiceStart(IUceListener uceListener) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onStopService() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onIsServiceStarted() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int onCreateOptionsService(IOptionsListener optionsListener, UceLong optionsServiceListenerHdl) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onDestroyOptionsService(int cdServiceHandle) {
    }

    /* access modifiers changed from: protected */
    public int onCreatePresService(IPresenceListener presServiceListener, UceLong presServiceListenerHdl) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void onDestroyPresService(int presServiceHdl) {
    }

    /* access modifiers changed from: protected */
    public boolean onGetServiceStatus() {
        return false;
    }

    /* access modifiers changed from: protected */
    public IPresenceService onGetPresenceService() {
        return null;
    }

    /* access modifiers changed from: protected */
    public IOptionsService onGetOptionsService() {
        return null;
    }
}
