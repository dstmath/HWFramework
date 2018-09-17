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
import tmsdk.common.utils.f;
import tmsdk.common.utils.n;
import tmsdkobf.im;
import tmsdkobf.mj;
import tmsdkobf.pj;

public final class DefaultPhoneDeviceController extends PhoneDeviceController {
    private AudioManager mAudioManager;
    private mj tY;
    private boolean tZ;
    private ICallback ua;
    private ICallback ub;

    public interface ICallback {
        void onCallback();
    }

    private static class a {
        static DefaultPhoneDeviceController ud = new DefaultPhoneDeviceController();
    }

    private final class b implements Runnable {
        private int ue;
        private int uf;
        private int ug;

        public b(int i, int i2, int i3) {
            this.ue = i;
            this.uf = i2;
            this.ug = i3;
        }

        public void run() {
            try {
                Thread.currentThread();
                Thread.sleep((long) (this.ug * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int ringerMode = DefaultPhoneDeviceController.this.mAudioManager.getRingerMode();
            int vibrateSetting = DefaultPhoneDeviceController.this.mAudioManager.getVibrateSetting(0);
            if (!(this.ue == -1 || ringerMode == this.ue)) {
                if (DefaultPhoneDeviceController.this.ua != null) {
                    DefaultPhoneDeviceController.this.ua.onCallback();
                }
                DefaultPhoneDeviceController.this.mAudioManager.setRingerMode(this.ue);
                if (DefaultPhoneDeviceController.this.ub != null) {
                    DefaultPhoneDeviceController.this.ub.onCallback();
                }
            }
            if (!(this.uf == -1 || vibrateSetting == this.uf)) {
                DefaultPhoneDeviceController.this.mAudioManager.setVibrateSetting(0, this.uf);
            }
            DefaultPhoneDeviceController.this.tZ = false;
        }
    }

    private DefaultPhoneDeviceController() {
        this.tZ = false;
        this.tY = mj.eO();
        this.mAudioManager = (AudioManager) TMSDKContext.getApplicaionContext().getSystemService("audio");
    }

    /* synthetic */ DefaultPhoneDeviceController(AnonymousClass1 anonymousClass1) {
        this();
    }

    public static DefaultPhoneDeviceController getInstance() {
        return a.ud;
    }

    public void blockSms(Object... objArr) {
        if (objArr != null && objArr.length >= 2 && (objArr[1] instanceof BroadcastReceiver)) {
            try {
                ((BroadcastReceiver) objArr[1]).abortBroadcast();
            } catch (Throwable th) {
                f.e("abortBroadcast", th);
            }
        }
    }

    public void cancelMissCall() {
        if (ScriptHelper.providerSupportCancelMissCall()) {
            ScriptHelper.provider().cancelMissCall();
            return;
        }
        if (ScriptHelper.isRootGot() || Process.myUid() == CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
            if (n.iX() < 17) {
                ScriptHelper.runScript(-1, "service call notification 3 s16 com.android.phone");
            } else {
                ScriptHelper.runScript(-1, "service call notification 1 s16 com.android.phone i32 -1");
            }
        }
    }

    public void disableRingVibration(int i) {
        if (!this.tZ) {
            this.tZ = true;
            int ringerMode = this.mAudioManager.getRingerMode();
            int vibrateSetting = this.mAudioManager.getVibrateSetting(0);
            if (ringerMode == 0) {
                ringerMode = -1;
            } else {
                if (this.ua != null) {
                    this.ua.onCallback();
                }
                this.mAudioManager.setRingerMode(0);
                if (this.ub != null) {
                    this.ub.onCallback();
                }
            }
            if (vibrateSetting == 0) {
                vibrateSetting = -1;
            } else {
                this.mAudioManager.setVibrateSetting(0, 0);
            }
            im.bJ().newFreeThread(new b(ringerMode, vibrateSetting, i), "disableRingVibrationThread").start();
        }
    }

    public void hangup(int i) {
        disableRingVibration(3);
        ITelephony defaultTelephony = DualSimTelephonyManager.getDefaultTelephony();
        boolean z = false;
        if (defaultTelephony == null) {
            try {
                f.e("DefaultPhoneDeviceController", "Failed to get ITelephony!");
            } catch (Throwable e) {
                f.b("DefaultPhoneDeviceController", "ITelephony#endCall", e);
            }
        } else {
            z = defaultTelephony.endCall();
        }
        if (!z) {
            f.e("DefaultPhoneDeviceController", "Failed to end call by ITelephony");
            f.e("DefaultPhoneDeviceController", "Try to use the deprecated way");
            this.tY.endCall();
        }
        ((pj) ManagerCreatorC.getManager(pj.class)).a(new Runnable() {
            public void run() {
                DefaultPhoneDeviceController.this.cancelMissCall();
            }
        }, 1000);
    }

    @Deprecated
    public boolean hangup() {
        disableRingVibration(3);
        boolean endCall = this.tY.endCall();
        ((pj) ManagerCreatorC.getManager(pj.class)).a(new Runnable() {
            public void run() {
                DefaultPhoneDeviceController.this.cancelMissCall();
            }
        }, 1000);
        return endCall;
    }

    public void setSetRingModeCallback(ICallback iCallback, ICallback iCallback2) {
        this.ua = iCallback;
        this.ub = iCallback2;
    }

    public void unBlockSms(SmsEntity smsEntity, Object... objArr) {
        if (objArr != null && objArr.length >= 2) {
            switch (((Integer) objArr[0]).intValue()) {
                case 2:
                    if (TMSDKContext.getApplicaionContext().getPackageName().equals((String) objArr[1])) {
                        ((AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class)).getAresEngineFactor().getSysDao().insert(smsEntity);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
