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
    private static HashMap<Integer, ImsUceManager> sUceManagerInstances;
    private Context mContext;
    private UceServiceDeathRecipient mDeathReceipient;
    private int mPhoneId;
    private IUceService mUceService;

    private class UceServiceDeathRecipient implements DeathRecipient {
        private UceServiceDeathRecipient() {
        }

        public void binderDied() {
            ImsUceManager.this.mUceService = null;
            if (ImsUceManager.this.mContext != null) {
                Intent intent = new Intent(ImsUceManager.ACTION_UCE_SERVICE_DOWN);
                intent.putExtra(ImsUceManager.EXTRA_PHONE_ID, ImsUceManager.this.mPhoneId);
                ImsUceManager.this.mContext.sendBroadcast(new Intent(intent));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.uceservice.ImsUceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.uceservice.ImsUceManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.uceservice.ImsUceManager.<clinit>():void");
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
        this.mUceService = null;
        this.mDeathReceipient = new UceServiceDeathRecipient();
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
                    b.linkToDeath(this.mDeathReceipient, UCE_SERVICE_STATUS_FAILURE);
                } catch (RemoteException e) {
                }
            }
            this.mUceService = Stub.asInterface(b);
        }
    }
}
