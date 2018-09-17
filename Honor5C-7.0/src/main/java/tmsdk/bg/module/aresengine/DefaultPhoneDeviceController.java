package tmsdk.bg.module.aresengine;

import android.content.BroadcastReceiver;
import android.media.AudioManager;
import android.os.Process;
import com.android.internal.telephony.ITelephony;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.common.DualSimTelephonyManager;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.d;
import tmsdk.common.utils.j;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.jq;
import tmsdkobf.ni;
import tmsdkobf.qm;

/* compiled from: Unknown */
public final class DefaultPhoneDeviceController extends PhoneDeviceController {
    private AudioManager mAudioManager;
    private ni wU;
    private boolean wV;
    private ICallback wW;
    private ICallback wX;

    /* compiled from: Unknown */
    public interface ICallback {
        void onCallback();
    }

    /* compiled from: Unknown */
    private static class a {
        static DefaultPhoneDeviceController wZ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.bg.module.aresengine.DefaultPhoneDeviceController.a.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.bg.module.aresengine.DefaultPhoneDeviceController.a.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdk.bg.module.aresengine.DefaultPhoneDeviceController.a.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    private final class b implements Runnable {
        final /* synthetic */ DefaultPhoneDeviceController wY;
        private int xa;
        private int xb;
        private int xc;

        public b(DefaultPhoneDeviceController defaultPhoneDeviceController, int i, int i2, int i3) {
            this.wY = defaultPhoneDeviceController;
            this.xa = i;
            this.xb = i2;
            this.xc = i3;
        }

        public void run() {
            try {
                Thread.currentThread();
                Thread.sleep((long) (this.xc * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int ringerMode = this.wY.mAudioManager.getRingerMode();
            int vibrateSetting = this.wY.mAudioManager.getVibrateSetting(0);
            if (!(this.xa == -1 || ringerMode == this.xa)) {
                if (this.wY.wW != null) {
                    this.wY.wW.onCallback();
                }
                this.wY.mAudioManager.setRingerMode(this.xa);
                if (this.wY.wX != null) {
                    this.wY.wX.onCallback();
                }
            }
            if (!(this.xb == -1 || vibrateSetting == this.xb)) {
                this.wY.mAudioManager.setVibrateSetting(0, this.xb);
            }
            this.wY.wV = false;
        }
    }

    private DefaultPhoneDeviceController() {
        this.wV = false;
        this.wU = ni.fg();
        this.mAudioManager = (AudioManager) TMSDKContext.getApplicaionContext().getSystemService("audio");
    }

    /* synthetic */ DefaultPhoneDeviceController(AnonymousClass1 anonymousClass1) {
        this();
    }

    public static DefaultPhoneDeviceController getInstance() {
        return a.wZ;
    }

    public void blockSms(Object... objArr) {
        if (objArr != null && objArr.length >= 2 && (objArr[1] instanceof BroadcastReceiver)) {
            try {
                ((BroadcastReceiver) objArr[1]).abortBroadcast();
            } catch (Throwable th) {
                d.c("abortBroadcast", th);
            }
        }
    }

    public void cancelMissCall() {
        if (ScriptHelper.providerSupportCancelMissCall()) {
            ScriptHelper.provider().cancelMissCall();
            return;
        }
        if (ScriptHelper.isRootGot() || Process.myUid() == CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
            if (j.iM() < 17) {
                ScriptHelper.runScript(-1, "service call notification 3 s16 com.android.phone");
            } else {
                ScriptHelper.runScript(-1, "service call notification 1 s16 com.android.phone i32 -1");
            }
        }
    }

    public void disableRingVibration(int i) {
        int i2 = -1;
        if (!this.wV) {
            this.wV = true;
            int ringerMode = this.mAudioManager.getRingerMode();
            int vibrateSetting = this.mAudioManager.getVibrateSetting(0);
            if (ringerMode == 0) {
                ringerMode = -1;
            } else {
                if (this.wW != null) {
                    this.wW.onCallback();
                }
                this.mAudioManager.setRingerMode(0);
                if (this.wX != null) {
                    this.wX.onCallback();
                }
            }
            if (vibrateSetting != 0) {
                this.mAudioManager.setVibrateSetting(0, 0);
                i2 = vibrateSetting;
            }
            jq.ct().c(new b(this, ringerMode, i2, i), "disableRingVibrationThread").start();
        }
    }

    public void hangup(int i) {
        boolean z = false;
        disableRingVibration(3);
        ITelephony defaultTelephony = DualSimTelephonyManager.getDefaultTelephony();
        if (defaultTelephony == null) {
            try {
                d.c("DefaultPhoneDeviceController", "Failed to get ITelephony!");
            } catch (Throwable e) {
                d.a("DefaultPhoneDeviceController", "ITelephony#endCall", e);
            }
        } else {
            z = defaultTelephony.endCall();
        }
        if (!z) {
            d.c("DefaultPhoneDeviceController", "Failed to end call by ITelephony");
            d.c("DefaultPhoneDeviceController", "Try to use the deprecated way");
            this.wU.endCall();
        }
        ((qm) ManagerCreatorC.getManager(qm.class)).a(new Runnable() {
            final /* synthetic */ DefaultPhoneDeviceController wY;

            {
                this.wY = r1;
            }

            public void run() {
                this.wY.cancelMissCall();
            }
        }, 1000);
    }

    @Deprecated
    public boolean hangup() {
        disableRingVibration(3);
        boolean endCall = this.wU.endCall();
        ((qm) ManagerCreatorC.getManager(qm.class)).a(new Runnable() {
            final /* synthetic */ DefaultPhoneDeviceController wY;

            {
                this.wY = r1;
            }

            public void run() {
                this.wY.cancelMissCall();
            }
        }, 1000);
        return endCall;
    }

    public void setSetRingModeCallback(ICallback iCallback, ICallback iCallback2) {
        this.wW = iCallback;
        this.wX = iCallback2;
    }

    public void unBlockSms(SmsEntity smsEntity, Object... objArr) {
        if (objArr != null && objArr.length >= 2) {
            switch (((Integer) objArr[0]).intValue()) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    if (TMSDKContext.getApplicaionContext().getPackageName().equals((String) objArr[1])) {
                        ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().insert(smsEntity);
                    }
                default:
            }
        }
    }
}
