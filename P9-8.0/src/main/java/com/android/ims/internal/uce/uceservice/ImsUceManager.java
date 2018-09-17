package com.android.ims.internal.uce.uceservice;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.ims.internal.uce.uceservice.IUceService.Stub;
import java.util.HashMap;

public class ImsUceManager {
    public static final String ACTION_UCE_SERVICE_DOWN = "com.android.ims.internal.uce.UCE_SERVICE_DOWN";
    public static final String ACTION_UCE_SERVICE_UP = "com.android.ims.internal.uce.UCE_SERVICE_UP";
    public static final String EXTRA_PHONE_ID = "android:phone_id";
    private static final String LOG_TAG = "ImsUceManager";
    private static final String UCE_SERVICE = "uce";
    public static final int UCE_SERVICE_STATUS_CLOSED = 2;
    public static final int UCE_SERVICE_STATUS_FAILURE = 0;
    public static final int UCE_SERVICE_STATUS_ON = 1;
    public static final int UCE_SERVICE_STATUS_READY = 3;
    private static HashMap<Integer, ImsUceManager> sUceManagerInstances = new HashMap();
    private Context mContext;
    private UceServiceDeathRecipient mDeathReceipient = new UceServiceDeathRecipient(this, null);
    private int mPhoneId;
    private IUceService mUceService = null;

    private class UceServiceDeathRecipient implements DeathRecipient {
        /* synthetic */ UceServiceDeathRecipient(ImsUceManager this$0, UceServiceDeathRecipient -this1) {
            this();
        }

        private UceServiceDeathRecipient() {
        }

        public void binderDied() {
            ImsUceManager.this.mUceService = null;
            if (ImsUceManager.this.mContext != null) {
                Intent intent = new Intent(ImsUceManager.ACTION_UCE_SERVICE_DOWN);
                intent.putExtra("android:phone_id", ImsUceManager.this.mPhoneId);
                ImsUceManager.this.mContext.sendBroadcast(new Intent(intent));
            }
        }
    }

    public static ImsUceManager getInstance(Context context, int phoneId) {
        synchronized (sUceManagerInstances) {
            if (sUceManagerInstances.containsKey(Integer.valueOf(phoneId))) {
                ImsUceManager imsUceManager = (ImsUceManager) sUceManagerInstances.get(Integer.valueOf(phoneId));
                return imsUceManager;
            }
            ImsUceManager uceMgr = new ImsUceManager(context, phoneId);
            sUceManagerInstances.put(Integer.valueOf(phoneId), uceMgr);
            return uceMgr;
        }
    }

    private ImsUceManager(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        createUceService(true);
    }

    public IUceService getUceServiceInstance() {
        return this.mUceService;
    }

    private String getUceServiceName(int phoneId) {
        return UCE_SERVICE;
    }

    public void createUceService(boolean checkService) {
        if (!checkService || ServiceManager.checkService(getUceServiceName(this.mPhoneId)) != null) {
            IBinder b = ServiceManager.getService(getUceServiceName(this.mPhoneId));
            if (b != null) {
                try {
                    b.linkToDeath(this.mDeathReceipient, 0);
                } catch (RemoteException e) {
                }
            }
            this.mUceService = Stub.asInterface(b);
        }
    }
}
