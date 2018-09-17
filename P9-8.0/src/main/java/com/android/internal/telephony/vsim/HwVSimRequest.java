package com.android.internal.telephony.vsim;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HwVSimRequest implements Cloneable {
    private RequestCore mCore;
    public int mSubId;

    private static class RequestCore {
        private static final String LOG_TAG = "VSimRequest";
        private Object mArgument;
        private boolean[] mCardOnOffMark;
        private int[] mCardTypes;
        private CommrilMode mExpectCommrilMode;
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
        private AtomicBoolean mStatus = new AtomicBoolean(false);
        private int[] mSubs;
        private boolean[] mSwitchSlotDoneMark;

        public void logd(String s) {
            HwVSimLog.VSimLogD(LOG_TAG, s);
        }

        public void setArgument(Object argument) {
            this.mArgument = argument;
        }

        public Object getArgument() {
            return this.mArgument;
        }

        public void setResult(Object result) {
            this.mResult = result;
        }

        public Object getResult() {
            return this.mResult;
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

        public void setMainSlot(int mainSlot) {
            this.mMainSlot = mainSlot;
        }

        public int getMainSlot() {
            return this.mMainSlot;
        }

        public void setExpectSlot(int expectSlot) {
            this.mExpectSlot = expectSlot;
        }

        public int getExpectSlot() {
            return this.mExpectSlot;
        }

        public void setIsVSimOnM0(boolean isVSimOnM0) {
            this.mIsVSimOnM0 = isVSimOnM0;
        }

        public boolean getIsVSimOnM0() {
            return this.mIsVSimOnM0;
        }

        public void setGotCardType(int index, boolean result) {
            if (this.mGotCardType != null && index >= 0 && index < this.mGotCardType.length) {
                this.mGotCardType[index] = result;
                logd("mGotCardType[" + index + "] = " + this.mGotCardType[index]);
            }
        }

        public void setCardType(int index, int type) {
            if (this.mCardTypes != null && index >= 0 && index < this.mCardTypes.length) {
                this.mCardTypes[index] = type;
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
            boolean allDone = true;
            for (boolean z : this.mGotCardType) {
                if (!z) {
                    allDone = false;
                    break;
                }
            }
            return allDone;
        }

        public void createPowerOnOffMark() {
            if (this.mSubs != null) {
                this.mPowerOnOffMark = new boolean[this.mSubs.length];
            }
        }

        public void createGetSimStateMark() {
            if (this.mSubs != null) {
                this.mGetSimStateMark = new boolean[this.mSubs.length];
            }
        }

        public void createCardOnOffMark() {
            if (this.mSubs != null) {
                this.mCardOnOffMark = new boolean[this.mSubs.length];
            }
        }

        public void createGetIccCardStatusMark() {
            if (this.mSubs != null) {
                this.mGetIccCardStatusMark = new boolean[this.mSubs.length];
            }
        }

        public int getSubCount() {
            if (this.mSubs == null) {
                return 0;
            }
            return this.mSubs.length;
        }

        public int getSubIdByIndex(int index) {
            if (this.mSubs == null) {
                return 0;
            }
            return this.mSubs[index];
        }

        public void setPowerOnOffMark(int index, boolean result) {
            if (this.mPowerOnOffMark == null && this.mSubs != null) {
                this.mPowerOnOffMark = new boolean[this.mSubs.length];
            }
            if (index >= 0 && index < this.mPowerOnOffMark.length) {
                this.mPowerOnOffMark[index] = result;
                if (this.mSubs != null && index < this.mSubs.length) {
                    logd("mPowerOnOffMark[" + index + "(" + this.mSubs[index] + ")] = " + this.mPowerOnOffMark[index]);
                }
            }
        }

        public void setSimStateMark(int index, boolean result) {
            if (this.mGetSimStateMark == null && this.mSubs != null) {
                this.mGetSimStateMark = new boolean[this.mSubs.length];
            }
            if (index >= 0 && index < this.mGetSimStateMark.length) {
                this.mGetSimStateMark[index] = result;
                if (this.mSubs != null && index < this.mSubs.length) {
                    logd("mGetSimStateMark[" + index + "(" + this.mSubs[index] + ")] = " + this.mGetSimStateMark[index]);
                }
            }
        }

        public void setCardOnOffMark(int index, boolean result) {
            if (this.mCardOnOffMark == null && this.mSubs != null) {
                this.mCardOnOffMark = new boolean[this.mSubs.length];
            }
            if (index >= 0 && index < this.mCardOnOffMark.length) {
                this.mCardOnOffMark[index] = result;
                if (this.mSubs != null && index < this.mSubs.length) {
                    logd("mCardOnOffMark[" + index + "(" + this.mSubs[index] + ")] = " + this.mCardOnOffMark[index]);
                }
            }
        }

        public void setGetIccCardStatusMark(int index, boolean result) {
            if (this.mGetIccCardStatusMark == null && this.mSubs != null) {
                this.mGetIccCardStatusMark = new boolean[this.mSubs.length];
            }
            if (this.mGetIccCardStatusMark != null && index >= 0 && index < this.mGetIccCardStatusMark.length) {
                this.mGetIccCardStatusMark[index] = result;
                if (this.mSubs != null && index < this.mSubs.length) {
                    logd("mGetIccCardStatusMark[" + index + "(" + this.mSubs[index] + ")] = " + this.mGetIccCardStatusMark[index]);
                }
            }
        }

        public boolean isAllMarkClear() {
            boolean allDone = true;
            if (this.mPowerOnOffMark != null) {
                for (boolean z : this.mPowerOnOffMark) {
                    if (z) {
                        allDone = false;
                        break;
                    }
                }
            }
            if (this.mGetSimStateMark != null) {
                for (boolean z2 : this.mGetSimStateMark) {
                    if (z2) {
                        allDone = false;
                        break;
                    }
                }
            }
            if (this.mCardOnOffMark != null) {
                for (boolean z22 : this.mCardOnOffMark) {
                    if (z22) {
                        allDone = false;
                        break;
                    }
                }
            }
            if (this.mGetIccCardStatusMark == null) {
                return allDone;
            }
            for (boolean z222 : this.mGetIccCardStatusMark) {
                if (z222) {
                    return false;
                }
            }
            return allDone;
        }

        public void setSlots(int[] slots) {
            this.mSlots = slots;
        }

        public int[] getSlots() {
            return this.mSlots;
        }

        public void setGotSimSlotMark(boolean result) {
            this.mGotSimSlotMark = result;
            logd("mGotSimSlotMark = " + this.mGotSimSlotMark);
        }

        public boolean isGotSimSlot() {
            return this.mGotSimSlotMark;
        }

        public void createSwitchSlotDoneMark() {
            if (this.mSubs != null) {
                this.mSwitchSlotDoneMark = new boolean[this.mSubs.length];
            }
        }

        public void setSwitchSlotDoneMark(int index, boolean result) {
            if (this.mSwitchSlotDoneMark != null && index >= 0 && index < this.mSwitchSlotDoneMark.length) {
                this.mSwitchSlotDoneMark[index] = result;
                if (this.mSubs != null && index < this.mSubs.length) {
                    logd("mSwitchSlotDoneMark[" + index + "(" + this.mSubs[index] + ")] = " + this.mSwitchSlotDoneMark[index]);
                }
            }
        }

        public boolean isDoneAllSwitchSlot() {
            if (this.mSwitchSlotDoneMark == null) {
                return false;
            }
            boolean allDone = true;
            for (boolean z : this.mSwitchSlotDoneMark) {
                if (!z) {
                    allDone = false;
                    break;
                }
            }
            return allDone;
        }

        public void setIsNeedSwitchCommrilMode(boolean result) {
            this.mIsNeedSwitchCommrilMode = result;
            logd("mIsNeedSwitchCommrilMode = " + result);
        }

        public boolean getIsNeedSwitchCommrilMode() {
            return this.mIsNeedSwitchCommrilMode;
        }

        public void setExpectCommrilMode(CommrilMode mode) {
            this.mExpectCommrilMode = mode;
            logd("mExpectCommrilMode = " + mode);
        }

        public CommrilMode getExpectCommrilMode() {
            return this.mExpectCommrilMode;
        }
    }

    private HwVSimRequest() {
    }

    public HwVSimRequest(Object argument, int subId) {
        this.mCore = new RequestCore();
        this.mCore.setArgument(argument);
        this.mSubId = subId;
    }

    public HwVSimRequest clone() {
        try {
            return (HwVSimRequest) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static Object sendRequest(Handler handler, int command, Object argument, int subId) {
        if (Looper.myLooper() == handler.getLooper()) {
            throw new RuntimeException("deadlock if called from vsim thread");
        }
        HwVSimRequest request = new HwVSimRequest(argument, subId);
        Message.obtain(handler, command, request).sendToTarget();
        request.doWait();
        return request.getResult();
    }

    private void doWait() {
        if (this.mCore != null) {
            this.mCore.doWait();
        }
    }

    public void doNotify() {
        if (this.mCore != null) {
            this.mCore.doNotify();
        }
    }

    public Object getResult() {
        if (this.mCore == null) {
            return null;
        }
        return this.mCore.getResult();
    }

    public void createGotCardType(int phoneCount) {
        if (this.mCore != null) {
            this.mCore.createGotCardType(phoneCount);
        }
    }

    public void createCardTypes(int phoneCount) {
        if (this.mCore != null) {
            this.mCore.createCardTypes(phoneCount);
        }
    }

    public void setMainSlot(int mainSlot) {
        if (this.mCore != null) {
            this.mCore.setMainSlot(mainSlot);
        }
    }

    public int getMainSlot() {
        if (this.mCore == null) {
            return 0;
        }
        return this.mCore.getMainSlot();
    }

    public void setExpectSlot(int expectSlot) {
        if (this.mCore != null) {
            this.mCore.setExpectSlot(expectSlot);
        }
    }

    public int getExpectSlot() {
        if (this.mCore == null) {
            return 0;
        }
        return this.mCore.getExpectSlot();
    }

    public void setIsVSimOnM0(boolean isVSimOnM0) {
        if (this.mCore != null) {
            this.mCore.setIsVSimOnM0(isVSimOnM0);
        }
    }

    public boolean getIsVSimOnM0() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.getIsVSimOnM0();
    }

    public void setGotCardType(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setGotCardType(index, result);
        }
    }

    public void setCardType(int index, int type) {
        if (this.mCore != null) {
            this.mCore.setCardType(index, type);
        }
    }

    public void logCardTypes() {
        if (this.mCore != null) {
            this.mCore.logCardTypes();
        }
    }

    public boolean isGotAllCardTypes() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.isGotAllCardTypes();
    }

    public void setSubs(int[] subs) {
        if (this.mCore != null) {
            this.mCore.setSubs(subs);
        }
    }

    public void createPowerOnOffMark() {
        if (this.mCore != null) {
            this.mCore.createPowerOnOffMark();
        }
    }

    public void createGetSimStateMark() {
        if (this.mCore != null) {
            this.mCore.createGetSimStateMark();
        }
    }

    public void createCardOnOffMark() {
        if (this.mCore != null) {
            this.mCore.createCardOnOffMark();
        }
    }

    public void createGetIccCardStatusMark() {
        if (this.mCore != null) {
            this.mCore.createGetIccCardStatusMark();
        }
    }

    public int getSubCount() {
        if (this.mCore == null) {
            return 0;
        }
        return this.mCore.getSubCount();
    }

    public int getSubIdByIndex(int index) {
        if (this.mCore == null) {
            return 0;
        }
        return this.mCore.getSubIdByIndex(index);
    }

    public void setPowerOnOffMark(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setPowerOnOffMark(index, result);
        }
    }

    public void setSimStateMark(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setSimStateMark(index, result);
        }
    }

    public void setCardOnOffMark(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setCardOnOffMark(index, result);
        }
    }

    public void setGetIccCardStatusMark(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setGetIccCardStatusMark(index, result);
        }
    }

    public boolean isAllMarkClear() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.isAllMarkClear();
    }

    public void setSlots(int[] slots) {
        if (this.mCore != null) {
            this.mCore.setSlots(slots);
        }
    }

    public int[] getSlots() {
        return this.mCore.getSlots();
    }

    public void setResult(Object result) {
        if (this.mCore != null) {
            this.mCore.setResult(result);
        }
    }

    public int[] getCardTypes() {
        return this.mCore.getCardTypes();
    }

    public void setGotSimSlotMark(boolean result) {
        if (this.mCore != null) {
            this.mCore.setGotSimSlotMark(result);
        }
    }

    public boolean isGotSimSlot() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.isGotSimSlot();
    }

    public void setArgument(Object argument) {
        if (this.mCore != null) {
            this.mCore.setArgument(argument);
        }
    }

    public Object getArgument() {
        if (this.mCore == null) {
            return null;
        }
        return this.mCore.getArgument();
    }

    public void createSwitchSlotDoneMark() {
        if (this.mCore != null) {
            this.mCore.createSwitchSlotDoneMark();
        }
    }

    public void setSwitchSlotDoneMark(int index, boolean result) {
        if (this.mCore != null) {
            this.mCore.setSwitchSlotDoneMark(index, result);
        }
    }

    public boolean isDoneAllSwitchSlot() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.isDoneAllSwitchSlot();
    }

    public void setIsNeedSwitchCommrilMode(boolean result) {
        if (this.mCore != null) {
            this.mCore.setIsNeedSwitchCommrilMode(result);
        }
    }

    public boolean getIsNeedSwitchCommrilMode() {
        if (this.mCore == null) {
            return false;
        }
        return this.mCore.getIsNeedSwitchCommrilMode();
    }

    public void setExpectCommrilMode(CommrilMode mode) {
        if (this.mCore != null) {
            this.mCore.setExpectCommrilMode(mode);
        }
    }

    public CommrilMode getExpectCommrilMode() {
        if (this.mCore == null) {
            return CommrilMode.NON_MODE;
        }
        return this.mCore.getExpectCommrilMode();
    }
}
