package com.huawei.internal.telephony.vsim;

import android.content.Context;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.vsim.HwVSimDualModem;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;

public class HwVSimMtkDualModem extends HwVSimDualModem {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwVSimDualModem";
    private static HwVSimMtkDualModem sModem;

    private HwVSimMtkDualModem(HwVSimBaseController vsimController, Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        super(vsimController, context, vsimCi, cis);
    }

    public static HwVSimMtkDualModem create(HwVSimBaseController vsimController, Context context, CommandsInterfaceEx[] cis) {
        HwVSimMtkDualModem hwVSimMtkDualModem;
        synchronized (LOCK) {
            if (sModem == null) {
                sModem = new HwVSimMtkDualModem(vsimController, context, null, cis);
                hwVSimMtkDualModem = sModem;
            } else {
                throw new RuntimeException("VSimController already created");
            }
        }
        return hwVSimMtkDualModem;
    }

    public static HwVSimMtkDualModem getInstance() {
        HwVSimMtkDualModem hwVSimMtkDualModem;
        synchronized (LOCK) {
            if (sModem != null) {
                hwVSimMtkDualModem = sModem;
            } else {
                throw new RuntimeException("VSimController not yet created");
            }
        }
        return hwVSimMtkDualModem;
    }

    @Override // com.android.internal.telephony.vsim.HwVSimDualModem, com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void checkEnableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor == null || request == null) {
            loge("checkEnableSimCondition, param is null !");
            return;
        }
        int[] cardTypes = request.getCardTypes();
        if (cardTypes == null) {
            loge("checkEnableSimCondition, cardTypes is null !");
        } else if (cardTypes.length == 0) {
            loge("checkEnableSimCondition, cardCount == 0 !");
        } else {
            int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
            int mainSlot = request.getMainSlot();
            logd("Enable: inserted card count = " + insertedCardCount + ", mainSlot = " + mainSlot + ", isVsimOnM0 = " + request.getIsVSimOnM0());
            int savedMainSlot = getVSimSavedMainSlot();
            if (savedMainSlot == -1) {
                setVSimSavedMainSlot(mainSlot);
            }
            int expectSlot = calcExpectSlot(request, insertedCardCount, mainSlot);
            logd("Enable: savedMainSlot = " + savedMainSlot + ", expectSlot = " + expectSlot);
            setAlternativeUserReservedSubId(expectSlot);
            processAfterCheckEnableCondition(processor, request, expectSlot);
        }
    }

    /* JADX INFO: Multiple debug info for r2v4 int: [D('reservedSlot' int), D('expectSlot' int)] */
    private int calcExpectSlot(HwVSimRequest request, int insertedCardCount, int mainSlot) {
        boolean isVsimOnM0 = request.getIsVSimOnM0();
        int secondarySlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
        if (isVsimOnM0) {
            return mainSlot;
        }
        if (insertedCardCount == 0) {
            return mainSlot;
        }
        if (insertedCardCount == 1) {
            return secondarySlot;
        }
        int reservedSlot = this.mVSimController.getUserReservedSubId();
        if (reservedSlot == -1) {
            reservedSlot = mainSlot;
            logd("Enable: reserved sub not set, this time set to " + mainSlot);
        }
        return HwVSimUtilsInner.getAnotherSlotId(reservedSlot);
    }

    private void processAfterCheckEnableCondition(HwVSimProcessor processor, HwVSimRequest request, int expectSlot) {
        request.setExpectSlot(expectSlot);
        if (setProcessType(processor, request)) {
            loge("setProcessType, occor an error, return");
        } else {
            processor.obtainMessage(100, request).sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimDualModem, com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null && processor != null) {
            int[] cardTypes = request.getCardTypes();
            if (!ArrayUtils.isEmpty(cardTypes)) {
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Disable: inserted card count = " + insertedCardCount);
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Disable: savedMainSlot = " + savedMainSlot);
                int mainSlot = request.getMainSlot();
                logd("Disable: mainSlot = " + mainSlot);
                int expectSlot = getExpectSlotForDisable(cardTypes, mainSlot, savedMainSlot);
                logd("Disable: expectSlot = " + expectSlot);
                request.setExpectSlot(expectSlot);
            }
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimDualModem, com.android.internal.telephony.vsim.HwVSimModemAdapter
    public int getAllAbilityNetworkTypeOnModem1(boolean duallteCapOpened) {
        if (HwVSimUtilsInner.isNrServiceAbilityOn(restoreSavedNetworkMode(0))) {
            return 69;
        }
        if (HwVSimUtilsInner.isDualImsSupported() && duallteCapOpened) {
            return 22;
        }
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return 3;
        }
        return 1;
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void getCardTypes(HwVSimProcessor processor, HwVSimRequest request, int slotId) {
        if (processor != null && request != null) {
            request.mSubId = slotId;
            Message onCompleted = processor.obtainMessage(56, request);
            int[] fakeCardTypes = new int[1];
            if (HwTelephonyManager.getDefault().getCardType(slotId) == -1) {
                fakeCardTypes[0] = 0;
            } else if (HwTelephonyManager.getDefault().isCTSimCard(slotId)) {
                fakeCardTypes[0] = 3;
            } else {
                fakeCardTypes[0] = 1;
            }
            setTestResult(onCompleted, request, fakeCardTypes);
        }
    }

    private void setTestResult(Message msg, HwVSimRequest request, Object testValue) {
        msg.obj = request;
        AsyncResultEx.forMessage(msg, testValue, (Throwable) null);
        msg.sendToTarget();
    }
}
