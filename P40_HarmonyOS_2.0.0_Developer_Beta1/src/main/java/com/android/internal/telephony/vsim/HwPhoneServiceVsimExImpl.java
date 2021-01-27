package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwInnerVSimManagerImpl;
import com.android.internal.telephony.HwPhoneServiceCommonUtils;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;
import com.huawei.internal.telephony.vsim.ServiceStateHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HwPhoneServiceVsimExImpl extends DefaultHwPhoneServiceVsimEx {
    private static final int CMD_GET_MODEM_SUPPORT_VSIM_VER_INIT = 3;
    private static final int CMD_GET_MODEM_SUPPORT_VSIM_VER_INNER = 1;
    private static final int CMD_GET_RESERVED_PLMN = 5;
    private static final int ERR_GET_VSIM_VER_FAILED = -1;
    private static final int ERR_GET_VSIM_VER_NOT_SUPPORT = -2;
    private static final int EVENT_GET_MODEM_SUPPORT_VSIM_VER_INIT_DONE = 4;
    private static final int EVENT_GET_MODEM_SUPPORT_VSIM_VER_INNER_DONE = 2;
    private static final int EVENT_GET_RESERVED_PLMN_DONE = 6;
    private static final int EVENT_LAZY_INIT_VSIM = 7;
    private static final int EXPECT_GET_VSIM_VER_SUPPORT = 10000;
    private static final int FRAMEWORK_SUPPORT_VERSION = 10000;
    private static final long GET_MODEM_SUPPORT_VERSION_INTERVAL = 2000;
    private static final boolean HW_DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    private static final int KEY_GET_FRAMEWORK_SUPPROT_VSIM_VER = 1;
    private static final int KEY_GET_MODEM_SUPPROT_VSIM_VER = 0;
    private static final int MAX_TRY_GET_MODEM_SUPPORT_VSIM_VERSION_TIMES = 3;
    private static final int MAX_WAIT_TIME_SECONDS = 5;
    private static final String TAG = "HwPhoneServiceVsimExImpl";
    private Context mContext;
    private Handler mMainHandler;
    private PhoneExt[] mPhones;
    private ServiceStateHandler[] mServiceStateHandlers;
    private int mTryGetModemSupportVsimVersionTimes = 0;
    private int mVsimModemSupportVer = ERR_GET_VSIM_VER_NOT_SUPPORT;

    static /* synthetic */ int access$508(HwPhoneServiceVsimExImpl x0) {
        int i = x0.mTryGetModemSupportVsimVersionTimes;
        x0.mTryGetModemSupportVsimVersionTimes = i + 1;
        return i;
    }

    HwPhoneServiceVsimExImpl(Context context, PhoneExt[] phones) {
        log("HwPhoneServiceVsimExImpl create");
        this.mContext = context;
        this.mPhones = phones;
        this.mMainHandler = new HwVsimExHandler();
        initGetModemSupportVsimVersion();
        this.mServiceStateHandlers = new ServiceStateHandler[this.mPhones.length];
        for (int i = 0; i < this.mPhones.length; i++) {
            this.mServiceStateHandlers[i] = new ServiceStateHandler(Looper.myLooper(), this.mPhones[i]);
            this.mPhones[i].registerForServiceStateChanged(this.mServiceStateHandlers[i], (int) HwVSimConstants.EVENT_SERVICE_STATE_CHANGE, (Object) null);
        }
    }

    private static <T> T awaitResult(CountDownLatch latch, AtomicReference<T> resultRef) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultRef.get();
    }

    private void initGetModemSupportVsimVersion() {
        if (!HuaweiTelephonyConfigs.isMTKPlatform()) {
            int slotId = getRadioOnSlotId();
            Handler handler = this.mMainHandler;
            handler.sendMessage(handler.obtainMessage(3, slotId, 0));
        }
    }

    public int getPlatformSupportVsimVer(int what) {
        int result;
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        if (what != 0) {
            if (what != 1) {
                result = -1;
            } else {
                result = 10000;
            }
        } else if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            log("getModemSupportVsimVersionInner m platform");
            if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                return 10000;
            }
            return -1;
        } else {
            if (this.mVsimModemSupportVer == -1) {
                this.mVsimModemSupportVer = getModemSupportVsimVersionInner();
            }
            result = this.mVsimModemSupportVer;
        }
        log("getPlatformSupportVsimVer done. what:" + what + " result:" + result);
        return result;
    }

    private int getModemSupportVsimVersionInner() {
        if (Process.myPid() == Binder.getCallingPid()) {
            log("getModemSupportVsimVersionInner same process obtain.");
            return -1;
        }
        int slotId = getRadioOnSlotId();
        CountDownLatch latchForGetVersion = new CountDownLatch(1);
        AtomicReference<Integer> resultForGetVersion = new AtomicReference<>();
        MainThreadRequest<Integer> request = new MainThreadRequest<>(null, latchForGetVersion, resultForGetVersion);
        Handler handler = this.mMainHandler;
        handler.sendMessage(handler.obtainMessage(1, slotId, 0, request));
        int version = ((Integer) awaitResult(latchForGetVersion, resultForGetVersion)).intValue();
        log("getModemSupportVsimVersionInner done, version is " + version);
        return version;
    }

    public String getRegPlmn(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        String result = null;
        if (slotId == 0 || slotId == 1 || slotId == 2) {
            CountDownLatch latchForGetPlmn = new CountDownLatch(1);
            AtomicReference<String> resultForGetPlmn = new AtomicReference<>();
            MainThreadRequest<String> request = new MainThreadRequest<>(null, latchForGetPlmn, resultForGetPlmn);
            Handler handler = this.mMainHandler;
            handler.sendMessage(handler.obtainMessage(5, slotId, 0, request));
            result = (String) awaitResult(latchForGetPlmn, resultForGetPlmn);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getRegPlmn done, slotId is ");
        sb.append(slotId);
        sb.append(", result is ");
        sb.append(HW_DBG ? result : "***");
        log(sb.toString());
        return result;
    }

    public boolean setVsimUserReservedSubId(int slotId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        HwVSimPhoneFactory.setVSimUserReservedSubId(this.mContext, slotId);
        log("setVsimUserReservedSubId, slotId " + slotId);
        return true;
    }

    public int getVsimUserReservedSubId() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
        int result = HwVSimPhoneFactory.getVSimUserReservedSubId(this.mContext);
        log("getVsimUserReservedSubId, return " + result);
        return result;
    }

    public void blockingGetVsimService(IGetVsimServiceCallback callback) {
        StringBuilder sb = new StringBuilder();
        sb.append("blockingGetVsimService, callback is null or not = ");
        sb.append(callback != null);
        log(sb.toString());
        Handler handler = this.mMainHandler;
        handler.sendMessage(handler.obtainMessage(7, callback));
    }

    public boolean isVsimEnabledByDatabase() {
        return HwVSimUtilsInner.isVsimEnabledByDatabase(this.mContext);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRadioOnSlotId() {
        int slotId = 0;
        while (true) {
            boolean isRadioOn = false;
            if (slotId > 2) {
                return 0;
            }
            CommandsInterfaceEx ci = getCommandsInterface(slotId);
            if (ci != null) {
                if (ci.getRadioState() == 1) {
                    isRadioOn = true;
                }
                if (isRadioOn) {
                    return slotId;
                }
            }
            slotId++;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CommandsInterfaceEx getCommandsInterface(int slotId) {
        if (slotId == 2) {
            PhoneExt vsimPhone = PhoneExt.getVsimPhone();
            if (vsimPhone != null) {
                return vsimPhone.getCi();
            }
            return null;
        } else if (HwPhoneServiceCommonUtils.isValidSlotId(slotId)) {
            return this.mPhones[slotId].getCi();
        } else {
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRequestNotSupport(Throwable ex) {
        return CommandExceptionEx.isSpecificError(ex, CommandExceptionEx.Error.REQUEST_NOT_SUPPORTED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String content) {
        RlogEx.i(TAG, content);
    }

    /* access modifiers changed from: private */
    public static final class MainThreadRequest<T> {
        Object mArgument;
        AtomicReference<T> mAtomicReference;
        CountDownLatch mCountDownLatch;
        Integer mSlotId;

        MainThreadRequest(Object argument, CountDownLatch countDownLatch, AtomicReference<T> resultRef) {
            this(argument, countDownLatch, resultRef, -1000);
        }

        MainThreadRequest(Object argument, CountDownLatch countDownLatch, AtomicReference<T> resultRef, Integer slotId) {
            this.mArgument = argument;
            this.mSlotId = slotId;
            this.mCountDownLatch = countDownLatch;
            this.mAtomicReference = resultRef;
        }
    }

    private class HwVsimExHandler extends Handler {
        HwVsimExHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        onCmdGetModemSupportVsimVersionInner(msg);
                        return;
                    case 2:
                        onGetModemSupportVsimVersionInnerDone(msg);
                        return;
                    case 3:
                        onCmdGetModemSupportVsimVersionInit(msg);
                        return;
                    case 4:
                        onGetModemSupportVsimVersionInitDone(msg);
                        return;
                    case 5:
                        onCmdGetReservedPlmn(msg);
                        return;
                    case 6:
                        onGetReservedPlmnDone(msg);
                        return;
                    case 7:
                        onCmdLazyInitVsim(msg);
                        return;
                    default:
                        return;
                }
            }
        }

        private void onCmdGetModemSupportVsimVersionInner(Message msg) {
            int slotId = msg.arg1;
            HwPhoneServiceVsimExImpl hwPhoneServiceVsimExImpl = HwPhoneServiceVsimExImpl.this;
            hwPhoneServiceVsimExImpl.log("start to get modem support vsim version inner, slotId = " + slotId);
            CommandsInterfaceEx ci = HwPhoneServiceVsimExImpl.this.getCommandsInterface(slotId);
            Message onCompleted = HwPhoneServiceVsimExImpl.this.mMainHandler.obtainMessage(2, (MainThreadRequest) msg.obj);
            if (ci != null) {
                ci.getModemSupportVSimVersion(onCompleted);
            }
        }

        private void onGetModemSupportVsimVersionInnerDone(Message msg) {
            HwPhoneServiceVsimExImpl.this.log("onGetModemSupportVsimVersionInnerDone");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            int modemVer = parseModemSupportVsimVersionResult(ar);
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            request.mAtomicReference.set((T) Integer.valueOf(modemVer));
            request.mCountDownLatch.countDown();
        }

        private int parseModemSupportVsimVersionResult(AsyncResultEx ar) {
            if (ar == null) {
                HwPhoneServiceVsimExImpl.this.log("parseModemSupportVsimVersionResult, param is null !");
                return -1;
            } else if (ar.getException() != null) {
                if (HwPhoneServiceVsimExImpl.this.isRequestNotSupport(ar.getException())) {
                    HwPhoneServiceVsimExImpl.this.log("parse modem vsim version failed for request not support");
                    return HwPhoneServiceVsimExImpl.ERR_GET_VSIM_VER_NOT_SUPPORT;
                }
                HwPhoneServiceVsimExImpl.this.log("parse modem vsim version failed, exception");
                return -1;
            } else if (ar.getResult() != null && ((int[]) ar.getResult()).length > 0) {
                return ((int[]) ar.getResult())[0];
            } else {
                HwPhoneServiceVsimExImpl.this.log("the result of modem vsim version is null");
                return -1;
            }
        }

        private void onCmdGetModemSupportVsimVersionInit(Message msg) {
            int slotId = msg.arg1;
            HwPhoneServiceVsimExImpl hwPhoneServiceVsimExImpl = HwPhoneServiceVsimExImpl.this;
            hwPhoneServiceVsimExImpl.log("start to get modem support vsim version init, slotId = " + slotId);
            CommandsInterfaceEx ci = HwPhoneServiceVsimExImpl.this.getCommandsInterface(slotId);
            Message onCompleted = HwPhoneServiceVsimExImpl.this.mMainHandler.obtainMessage(4);
            if (ci != null) {
                ci.getModemSupportVSimVersion(onCompleted);
            }
        }

        private void onGetModemSupportVsimVersionInitDone(Message msg) {
            HwPhoneServiceVsimExImpl.this.log("onGetModemSupportVsimVersionInitDone");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            HwPhoneServiceVsimExImpl.this.mVsimModemSupportVer = parseModemSupportVsimVersionResult(ar);
            HwPhoneServiceVsimExImpl hwPhoneServiceVsimExImpl = HwPhoneServiceVsimExImpl.this;
            hwPhoneServiceVsimExImpl.log("onGetModemSupportVsimVersionInitDone, result = " + HwPhoneServiceVsimExImpl.this.mVsimModemSupportVer);
            if (HwPhoneServiceVsimExImpl.this.mVsimModemSupportVer == -1 && HwPhoneServiceVsimExImpl.this.mTryGetModemSupportVsimVersionTimes < 3) {
                HwPhoneServiceVsimExImpl.this.log("retry to getModemSupportVSimVersion");
                HwPhoneServiceVsimExImpl.access$508(HwPhoneServiceVsimExImpl.this);
                HwPhoneServiceVsimExImpl.this.mMainHandler.sendMessageDelayed(HwPhoneServiceVsimExImpl.this.mMainHandler.obtainMessage(3, 0, HwPhoneServiceVsimExImpl.this.getRadioOnSlotId()), HwPhoneServiceVsimExImpl.GET_MODEM_SUPPORT_VERSION_INTERVAL);
            }
        }

        private void onCmdGetReservedPlmn(Message msg) {
            int slotId = msg.arg1;
            HwPhoneServiceVsimExImpl hwPhoneServiceVsimExImpl = HwPhoneServiceVsimExImpl.this;
            hwPhoneServiceVsimExImpl.log("start to get plmn for slotId " + slotId);
            CommandsInterfaceEx ci = HwPhoneServiceVsimExImpl.this.getCommandsInterface(slotId);
            Message onCompleted = HwPhoneServiceVsimExImpl.this.mMainHandler.obtainMessage(6, (MainThreadRequest) msg.obj);
            if (ci != null) {
                ci.getRegPlmn(onCompleted);
            }
        }

        private void onGetReservedPlmnDone(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                HwPhoneServiceVsimExImpl.this.log("onGetReservedPlmnDone, ar is null");
                return;
            }
            String result = BuildConfig.FLAVOR;
            if (ar.getException() == null && (ar.getResult() instanceof String)) {
                result = (String) ar.getResult();
            }
            HwPhoneServiceVsimExImpl hwPhoneServiceVsimExImpl = HwPhoneServiceVsimExImpl.this;
            StringBuilder sb = new StringBuilder();
            sb.append("onGetReservedPlmnDone, result = ");
            sb.append(HwPhoneServiceVsimExImpl.HW_DBG ? result : "***");
            hwPhoneServiceVsimExImpl.log(sb.toString());
            MainThreadRequest request = (MainThreadRequest) ar.getUserObj();
            request.mAtomicReference.set(result);
            request.mCountDownLatch.countDown();
        }

        private void onCmdLazyInitVsim(Message msg) {
            HwPhoneServiceVsimExImpl.this.log("onCmdLazyInitVsim, start!");
            try {
                if (msg.obj instanceof IGetVsimServiceCallback) {
                    HwInnerVSimManagerImpl.getDefault().lazyInit((IGetVsimServiceCallback) msg.obj);
                }
            } catch (RemoteException e) {
                HwPhoneServiceVsimExImpl.this.log("onCmdLazyInitVsim, occur an exception.");
            }
        }
    }
}
