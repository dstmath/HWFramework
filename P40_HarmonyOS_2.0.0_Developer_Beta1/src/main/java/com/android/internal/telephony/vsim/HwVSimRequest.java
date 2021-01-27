package com.android.internal.telephony.vsim;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HwVSimRequest implements Cloneable {
    private RequestCore mCore;
    public int mSubId;

    private HwVSimRequest() {
    }

    public HwVSimRequest(Object argument, int subId) {
        this.mCore = new RequestCore();
        this.mCore.setArgument(argument);
        this.mSubId = subId;
    }

    public static Object sendRequest(Handler handler, int command, Object argument, int subId) {
        if (handler == null) {
            return null;
        }
        if (Looper.myLooper() != handler.getLooper()) {
            HwVSimRequest request = new HwVSimRequest(argument, subId);
            Message.obtain(handler, command, request).sendToTarget();
            request.doWait();
            return request.getResult();
        }
        throw new RuntimeException("deadlock if called from vsim thread");
    }

    @Override // java.lang.Object
    public HwVSimRequest clone() {
        try {
            return (HwVSimRequest) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private void doWait() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.doWait();
        }
    }

    public void doNotify() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.doNotify();
        }
    }

    public Object getResult() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return null;
        }
        return requestCore.getResult();
    }

    public void setResult(Object result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setResult(result);
        }
    }

    public void createGotCardType(int phoneCount) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createGotCardType(phoneCount);
        }
    }

    public void createCardTypes(int phoneCount) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createCardTypes(phoneCount);
        }
    }

    public int getMainSlot() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return 0;
        }
        return requestCore.getMainSlot();
    }

    public void setMainSlot(int mainSlot) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setMainSlot(mainSlot);
        }
    }

    public int getExpectSlot() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return 0;
        }
        return requestCore.getExpectSlot();
    }

    public void setExpectSlot(int expectSlot) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setExpectSlot(expectSlot);
        }
    }

    public boolean getIsVSimOnM0() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return false;
        }
        return requestCore.getIsVSimOnM0();
    }

    public void setIsVSimOnM0(boolean isVSimOnM0) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setIsVSimOnM0(isVSimOnM0);
        }
    }

    public void setGotCardType(int index, boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setGotCardType(index, result);
        }
    }

    public void setCardType(int index, int type) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setCardType(index, type);
        }
    }

    public void logCardTypes() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.logCardTypes();
        }
    }

    public boolean isGotAllCardTypes() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return false;
        }
        return requestCore.isGotAllCardTypes();
    }

    public void setSubs(int[] subs) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setSubs(subs);
        }
    }

    public void createPowerOnOffMark() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createPowerOnOffMark();
        }
    }

    public void createGetSimStateMark() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createGetSimStateMark();
        }
    }

    public void createCardOnOffMark() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createCardOnOffMark();
        }
    }

    public void createGetIccCardStatusMark() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.createGetIccCardStatusMark();
        }
    }

    public int getSubCount() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return 0;
        }
        return requestCore.getSubCount();
    }

    public int getSubIdByIndex(int index) {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return 0;
        }
        return requestCore.getSubIdByIndex(index);
    }

    public void setPowerOnOffMark(int index, boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setPowerOnOffMark(index, result);
        }
    }

    public void setSimStateMark(int index, boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setSimStateMark(index, result);
        }
    }

    public void setCardOnOffMark(int index, boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setCardOnOffMark(index, result);
        }
    }

    public void setGetIccCardStatusMark(int index, boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setGetIccCardStatusMark(index, result);
        }
    }

    public boolean isAllMarkClear() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return false;
        }
        return requestCore.isAllMarkClear();
    }

    public boolean isGetIccCardStatusDone() {
        RequestCore requestCore = this.mCore;
        return requestCore != null && requestCore.isGetIccCardStatusDone();
    }

    public int[] getSlots() {
        return this.mCore.getSlots();
    }

    public void setSlots(int[] slots) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setSlots(slots);
        }
    }

    public int[] getCardTypes() {
        return this.mCore.getCardTypes();
    }

    public void setGotSimSlotMark(boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setGotSimSlotMark(result);
        }
    }

    public boolean isGotSimSlot() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return false;
        }
        return requestCore.isGotSimSlot();
    }

    public Object getArgument() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return null;
        }
        return requestCore.getArgument();
    }

    public void setArgument(Object argument) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setArgument(argument);
        }
    }

    public boolean getIsNeedSwitchCommrilMode() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return false;
        }
        return requestCore.getIsNeedSwitchCommrilMode();
    }

    public void setIsNeedSwitchCommrilMode(boolean result) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setIsNeedSwitchCommrilMode(result);
        }
    }

    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
        RequestCore requestCore = this.mCore;
        if (requestCore == null) {
            return HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        }
        return requestCore.getExpectCommrilMode();
    }

    public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode mode) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setExpectCommrilMode(mode);
        }
    }

    public int getSource() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            return requestCore.getSource();
        }
        return 0;
    }

    public void setSource(int source) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setSource(source);
        }
    }

    public int getCardCount() {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            return requestCore.getCardCount();
        }
        return 0;
    }

    public void setCardCount(int count) {
        RequestCore requestCore = this.mCore;
        if (requestCore != null) {
            requestCore.setCardCount(count);
        }
    }

    /* access modifiers changed from: private */
    public static class RequestCore {
        private static final String LOG_TAG = "VSimRequest";
        private Object mArgument;
        private int mCardCount;
        private boolean[] mCardOnOffMark;
        private int[] mCardTypes;
        private HwVSimSlotSwitchController.CommrilMode mExpectCommrilMode;
        private int mExpectSlot;
        private boolean[] mGetIccCardStatusMark;
        private boolean[] mGetSimStateMark;
        private boolean[] mGotCardType;
        private boolean mGotSimSlotMark;
        private boolean mIsNeedSwitchCommrilMode;
        private boolean mIsVSimOnM0;
        private int mMainSlot;
        private boolean[] mPowerOnOffMark;
        private Object mResult;
        private int[] mSlots;
        private int mSource;
        private AtomicBoolean mStatus = new AtomicBoolean(false);
        private int[] mSubs;

        public void logd(String s) {
            HwVSimLog.VSimLogD(LOG_TAG, s);
        }

        public Object getArgument() {
            return this.mArgument;
        }

        public void setArgument(Object argument) {
            this.mArgument = argument;
        }

        public Object getResult() {
            return this.mResult;
        }

        public void setResult(Object result) {
            this.mResult = result;
        }

        public void doWait() {
            synchronized (this) {
                while (!this.mStatus.get()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void doNotify() {
            synchronized (this) {
                this.mStatus.set(true);
                notifyAll();
            }
        }

        public void createGotCardType(int phoneCount) {
            this.mGotCardType = new boolean[phoneCount];
        }

        public void createCardTypes(int phoneCount) {
            this.mCardTypes = new int[phoneCount];
        }

        public int getMainSlot() {
            return this.mMainSlot;
        }

        public void setMainSlot(int mainSlot) {
            this.mMainSlot = mainSlot;
        }

        public int getExpectSlot() {
            return this.mExpectSlot;
        }

        public void setExpectSlot(int expectSlot) {
            this.mExpectSlot = expectSlot;
        }

        public boolean getIsVSimOnM0() {
            return this.mIsVSimOnM0;
        }

        public void setIsVSimOnM0(boolean isVSimOnM0) {
            this.mIsVSimOnM0 = isVSimOnM0;
        }

        public void setGotCardType(int index, boolean result) {
            boolean[] zArr = this.mGotCardType;
            if (zArr != null && index >= 0 && index < zArr.length) {
                zArr[index] = result;
                logd("mGotCardType[" + index + "] = " + this.mGotCardType[index]);
            }
        }

        public void setCardType(int index, int type) {
            int[] iArr = this.mCardTypes;
            if (iArr != null && index >= 0 && index < iArr.length) {
                iArr[index] = type;
            }
        }

        public int[] getCardTypes() {
            return this.mCardTypes;
        }

        public void logCardTypes() {
            if (this.mCardTypes != null) {
                logd("mCardTypes = " + Arrays.toString(this.mCardTypes));
            }
        }

        public void setSubs(int[] subs) {
            this.mSubs = subs;
        }

        public boolean isGotAllCardTypes() {
            if (this.mGotCardType == null) {
                return false;
            }
            int i = 0;
            while (true) {
                boolean[] zArr = this.mGotCardType;
                if (i >= zArr.length) {
                    return true;
                }
                if (!zArr[i]) {
                    return false;
                }
                i++;
            }
        }

        public void createPowerOnOffMark() {
            int[] iArr = this.mSubs;
            if (iArr != null) {
                this.mPowerOnOffMark = new boolean[iArr.length];
            }
        }

        public void createGetSimStateMark() {
            int[] iArr = this.mSubs;
            if (iArr != null) {
                this.mGetSimStateMark = new boolean[iArr.length];
            }
        }

        public void createCardOnOffMark() {
            int[] iArr = this.mSubs;
            if (iArr != null) {
                this.mCardOnOffMark = new boolean[iArr.length];
            }
        }

        public void createGetIccCardStatusMark() {
            int[] iArr = this.mSubs;
            if (iArr != null) {
                this.mGetIccCardStatusMark = new boolean[iArr.length];
            }
        }

        public int getSubCount() {
            int[] iArr = this.mSubs;
            if (iArr == null) {
                return 0;
            }
            return iArr.length;
        }

        public int getSubIdByIndex(int index) {
            int[] iArr = this.mSubs;
            if (iArr == null) {
                return 0;
            }
            return iArr[index];
        }

        public void setPowerOnOffMark(int index, boolean result) {
            boolean[] zArr;
            int[] iArr;
            if (this.mPowerOnOffMark == null && (iArr = this.mSubs) != null) {
                this.mPowerOnOffMark = new boolean[iArr.length];
            }
            if (index >= 0 && (zArr = this.mPowerOnOffMark) != null && index < zArr.length) {
                zArr[index] = result;
                int[] iArr2 = this.mSubs;
                if (iArr2 != null && index < iArr2.length) {
                    int subId = iArr2[index];
                    logd("mPowerOnOffMark[" + index + "(" + subId + ")] = " + this.mPowerOnOffMark[index]);
                }
            }
        }

        public void setSimStateMark(int index, boolean result) {
            boolean[] zArr;
            int[] iArr;
            if (this.mGetSimStateMark == null && (iArr = this.mSubs) != null) {
                this.mGetSimStateMark = new boolean[iArr.length];
            }
            if (index >= 0 && (zArr = this.mGetSimStateMark) != null && index < zArr.length) {
                zArr[index] = result;
                int[] iArr2 = this.mSubs;
                if (iArr2 != null && index < iArr2.length) {
                    int subId = iArr2[index];
                    logd("mGetSimStateMark[" + index + "(" + subId + ")] = " + this.mGetSimStateMark[index]);
                }
            }
        }

        public void setCardOnOffMark(int index, boolean result) {
            boolean[] zArr;
            int[] iArr;
            if (this.mCardOnOffMark == null && (iArr = this.mSubs) != null) {
                this.mCardOnOffMark = new boolean[iArr.length];
            }
            if (index >= 0 && (zArr = this.mCardOnOffMark) != null && index < zArr.length) {
                zArr[index] = result;
                int[] iArr2 = this.mSubs;
                if (iArr2 != null && index < iArr2.length) {
                    int subId = iArr2[index];
                    logd("mCardOnOffMark[" + index + "(" + subId + ")] = " + this.mCardOnOffMark[index]);
                }
            }
        }

        public void setGetIccCardStatusMark(int index, boolean result) {
            int[] iArr;
            if (this.mGetIccCardStatusMark == null && (iArr = this.mSubs) != null) {
                this.mGetIccCardStatusMark = new boolean[iArr.length];
            }
            boolean[] zArr = this.mGetIccCardStatusMark;
            if (zArr != null && index >= 0 && index < zArr.length) {
                zArr[index] = result;
                int[] iArr2 = this.mSubs;
                if (iArr2 != null && index < iArr2.length) {
                    int subId = iArr2[index];
                    logd("mGetIccCardStatusMark[" + index + "(" + subId + ")] = " + this.mGetIccCardStatusMark[index]);
                }
            }
        }

        public boolean isGetIccCardStatusDone() {
            logd("isGetIccCardStatusDone:" + Arrays.toString(this.mGetIccCardStatusMark));
            if (this.mGetIccCardStatusMark == null) {
                return true;
            }
            int i = 0;
            while (true) {
                boolean[] zArr = this.mGetIccCardStatusMark;
                if (i >= zArr.length) {
                    return true;
                }
                if (zArr[i]) {
                    return false;
                }
                i++;
            }
        }

        public boolean isAllMarkClear() {
            boolean allDone = true;
            if (this.mPowerOnOffMark != null) {
                int i = 0;
                while (true) {
                    boolean[] zArr = this.mPowerOnOffMark;
                    if (i >= zArr.length) {
                        break;
                    } else if (zArr[i]) {
                        allDone = false;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (this.mGetSimStateMark != null) {
                int i2 = 0;
                while (true) {
                    boolean[] zArr2 = this.mGetSimStateMark;
                    if (i2 >= zArr2.length) {
                        break;
                    } else if (zArr2[i2]) {
                        allDone = false;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            if (this.mCardOnOffMark != null) {
                int i3 = 0;
                while (true) {
                    boolean[] zArr3 = this.mCardOnOffMark;
                    if (i3 >= zArr3.length) {
                        break;
                    } else if (zArr3[i3]) {
                        allDone = false;
                        break;
                    } else {
                        i3++;
                    }
                }
            }
            if (this.mGetIccCardStatusMark == null) {
                return allDone;
            }
            int i4 = 0;
            while (true) {
                boolean[] zArr4 = this.mGetIccCardStatusMark;
                if (i4 >= zArr4.length) {
                    return allDone;
                }
                if (zArr4[i4]) {
                    return false;
                }
                i4++;
            }
        }

        public int[] getSlots() {
            return this.mSlots;
        }

        public void setSlots(int[] slots) {
            this.mSlots = slots;
        }

        public void setGotSimSlotMark(boolean result) {
            this.mGotSimSlotMark = result;
            logd("mGotSimSlotMark = " + this.mGotSimSlotMark);
        }

        public boolean isGotSimSlot() {
            return this.mGotSimSlotMark;
        }

        public boolean getIsNeedSwitchCommrilMode() {
            return this.mIsNeedSwitchCommrilMode;
        }

        public void setIsNeedSwitchCommrilMode(boolean result) {
            this.mIsNeedSwitchCommrilMode = result;
            logd("mIsNeedSwitchCommrilMode = " + result);
        }

        public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
            return this.mExpectCommrilMode;
        }

        public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode mode) {
            this.mExpectCommrilMode = mode;
            logd("mExpectCommrilMode = " + mode);
        }

        public int getSource() {
            return this.mSource;
        }

        public void setSource(int source) {
            this.mSource = source;
        }

        public int getCardCount() {
            return this.mCardCount;
        }

        public void setCardCount(int count) {
            this.mCardCount = count;
        }
    }
}
