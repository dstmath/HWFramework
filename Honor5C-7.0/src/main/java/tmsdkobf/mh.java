package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.tencent.tmsecurelite.base.ITmsConnection;
import com.tencent.tmsecurelite.base.ITmsProvider.Stub;
import com.tencent.tmsecurelite.commom.ServiceManager;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class mh {
    private static mh AM;
    private final int AH;
    private String AI;
    private boolean AJ;
    private ITmsConnection AK;
    private boolean AL;
    private Stub AN;
    private a AO;
    private ServiceConnection mConnection;
    Handler yO;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.mh.3 */
    class AnonymousClass3 extends Handler {
        final /* synthetic */ mh AP;

        AnonymousClass3(mh mhVar, Looper looper) {
            this.AP = mhVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            Object obj = null;
            if (message.what == WifiDetectManager.SECURITY_NONE) {
                String eM = mg.eM();
                if (l.dm(this.AP.AI) || !this.AP.AI.equals(eM)) {
                    this.AP.AI = eM;
                    obj = 1;
                }
                if (obj != null) {
                    try {
                        if (!this.AP.AJ) {
                            Bundle bundle = new Bundle();
                            Bundle bundle2 = new Bundle();
                            bundle.putString("t.pkg", eM);
                            this.AP.AK.sendTmsRequest(786434, bundle, bundle2);
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
                if (!this.AP.AJ) {
                    this.AP.yO.sendEmptyMessageDelayed(WifiDetectManager.SECURITY_NONE, 500);
                }
            }
        }
    }

    /* compiled from: Unknown */
    public static class a extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean booleanValue = fw.w().J().booleanValue();
            mg.a(booleanValue, "com.tencent.qqpimsecure", "OP_WRITE_SMS");
            int a = mg.a(booleanValue, "com.tencent.qqpimsecure", "OP_SYSTEM_ALERT_WINDOW");
            if (a == 1) {
                ma.bx(1320018);
            } else if (a == 2) {
                ma.bx(1320019);
            }
            mh.eN().eO();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.mh.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.mh.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.mh.<clinit>():void");
    }

    private mh() {
        this.AH = WifiDetectManager.SECURITY_NONE;
        this.AI = null;
        this.AJ = false;
        this.AL = false;
        this.mConnection = new ServiceConnection() {
            final /* synthetic */ mh AP;

            {
                this.AP = r1;
            }

            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                this.AP.AL = false;
                this.AP.AK = (ITmsConnection) ServiceManager.getTmsConnection(iBinder);
                try {
                    String packageName = TMSDKContext.getApplicaionContext().getPackageName();
                    boolean checkVersion = this.AP.AK.checkVersion(3);
                    boolean checkPermission = this.AP.AK.checkPermission(packageName, 12);
                    if (!checkVersion || !checkPermission) {
                        d.e("TMSLiteService--TMS", "checkVersion or checkPermission failed!!");
                        this.AP.eP();
                    } else if (this.AP.AK.setProvider(this.AP.AN) != 0) {
                    }
                } catch (Throwable th) {
                    this.AP.eP();
                }
            }

            public void onServiceDisconnected(ComponentName componentName) {
                this.AP.AL = false;
                this.AP.eR();
                this.AP.AK = null;
            }
        };
        this.AN = new Stub() {
            final /* synthetic */ mh AP;

            {
                this.AP = r1;
            }

            public int getVersion() throws RemoteException {
                return 1;
            }

            public int ipcCall(int i, Bundle bundle, Bundle bundle2) throws RemoteException {
                switch (i) {
                    case 790529:
                        d.e("TMSLiteService--TMS", "EXIT");
                        this.AP.eR();
                        this.AP.eP();
                        break;
                    case 790530:
                        d.e("TMSLiteService--TMS", "START_TOP_PKG_MONITOR");
                        if (fw.w().I().booleanValue()) {
                            if (!this.AP.eQ()) {
                                bundle2.putInt("ret", 1);
                                break;
                            }
                            bundle2.putInt("ret", 0);
                            break;
                        }
                        bundle2.putInt("ret", 2);
                        break;
                    case 790531:
                        d.e("TMSLiteService--TMS", "STOP_TOP_PKG_MONITOR");
                        this.AP.eR();
                        break;
                }
                return 0;
            }
        };
        this.yO = new AnonymousClass3(this, Looper.getMainLooper());
        register();
    }

    public static mh eN() {
        if (AM == null) {
            AM = new mh();
        }
        return AM;
    }

    private synchronized boolean eO() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        if (!this.AL) {
            if (this.AK == null) {
                if (fw.w().I().booleanValue()) {
                    boolean bindService = applicaionContext.bindService(ServiceManager.getTmsIntent(12), this.mConnection, 1);
                    if (bindService) {
                        this.AL = true;
                    }
                    return bindService;
                }
                d.e("TMSLiteService--TMS", "getTopActPerSwitch off");
                return false;
            }
        }
        return true;
    }

    private synchronized void eP() {
        this.AL = false;
        if (this.AK != null) {
            TMSDKContext.getApplicaionContext().unbindService(this.mConnection);
            this.AK = null;
        }
    }

    private boolean eQ() {
        this.AJ = false;
        if (mg.eM() != null) {
            ma.bx(1320016);
            this.yO.sendEmptyMessageDelayed(WifiDetectManager.SECURITY_NONE, 500);
            return true;
        }
        d.f("TMSLiteService--TMS", "No Permission");
        ma.bx(1320017);
        return false;
    }

    private void eR() {
        this.AJ = true;
        this.yO.removeMessages(WifiDetectManager.SECURITY_NONE);
    }

    private void register() {
        if (this.AO == null) {
            this.AO = new a();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("tencent.bxx.ayy.czz");
            intentFilter.setPriority(UrlCheckType.UNKNOWN);
            try {
                TMSDKContext.getApplicaionContext().registerReceiver(this.AO, intentFilter);
            } catch (Throwable th) {
            }
        }
    }
}
