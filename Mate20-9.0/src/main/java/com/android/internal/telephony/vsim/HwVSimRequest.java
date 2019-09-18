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

    private static class RequestCore {
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
            int i = 0;
            if (this.mGotCardType == null) {
                return false;
            }
            boolean allDone = true;
            while (true) {
                if (i >= this.mGotCardType.length) {
                    break;
                } else if (!this.mGotCardType[i]) {
                    allDone = false;
                    break;
                } else {
                    i++;
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
                    int subId = this.mSubs[index];
                    logd("mPowerOnOffMark[" + index + "(" + subId + ")] = " + this.mPowerOnOffMark[index]);
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
                    int subId = this.mSubs[index];
                    logd("mGetSimStateMark[" + index + "(" + subId + ")] = " + this.mGetSimStateMark[index]);
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
                    int subId = this.mSubs[index];
                    logd("mCardOnOffMark[" + index + "(" + subId + ")] = " + this.mCardOnOffMark[index]);
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
                    int subId = this.mSubs[index];
                    logd("mGetIccCardStatusMark[" + index + "(" + subId + ")] = " + this.mGetIccCardStatusMark[index]);
                }
            }
        }

        public boolean isAllMarkClear() {
            boolean allDone = true;
            int i = 0;
            if (this.mPowerOnOffMark != null) {
                int i2 = 0;
                while (true) {
                    if (i2 >= this.mPowerOnOffMark.length) {
                        break;
                    } else if (this.mPowerOnOffMark[i2]) {
                        allDone = false;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            if (this.mGetSimStateMark != null) {
                int i3 = 0;
                while (true) {
                    if (i3 >= this.mGetSimStateMark.length) {
                        break;
                    } else if (this.mGetSimStateMark[i3]) {
                        allDone = false;
                        break;
                    } else {
                        i3++;
                    }
                }
            }
            if (this.mCardOnOffMark != null) {
                int i4 = 0;
                while (true) {
                    if (i4 >= this.mCardOnOffMark.length) {
                        break;
                    } else if (this.mCardOnOffMark[i4]) {
                        allDone = false;
                        break;
                    } else {
                        i4++;
                    }
                }
            }
            if (this.mGetIccCardStatusMark == null) {
                return allDone;
            }
            while (true) {
                int i5 = i;
                if (i5 >= this.mGetIccCardStatusMark.length) {
                    return allDone;
                }
                if (this.mGetIccCardStatusMark[i5]) {
                    return false;
                }
                i = i5 + 1;
            }
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

        public void setIsNeedSwitchCommrilMode(boolean result) {
            this.mIsNeedSwitchCommrilMode = result;
            logd("mIsNeedSwitchCommrilMode = " + result);
        }

        public boolean getIsNeedSwitchCommrilMode() {
            return this.mIsNeedSwitchCommrilMode;
        }

        public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode mode) {
            this.mExpectCommrilMode = mode;
            logd("mExpectCommrilMode = " + mode);
        }

        public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
            return this.mExpectCommrilMode;
        }

        public void setSource(int source) {
            this.mSource = source;
        }

        public int getSource() {
            return this.mSource;
        }

        public void setCardCount(int count) {
            this.mCardCount = count;
        }

        public int getCardCount() {
            return this.mCardCount;
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
        if (Looper.myLooper() != handler.getLooper()) {
            HwVSimRequest request = new HwVSimRequest(argument, subId);
            Message.obtain(handler, command, request).sendToTarget();
            request.doWait();
            return request.getResult();
        }
        throw new RuntimeException("deadlock if called from vsim thread");
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

    public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode mode) {
        if (this.mCore != null) {
            this.mCore.setExpectCommrilMode(mode);
        }
    }

    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
        if (this.mCore == null) {
            return HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        }
        return this.mCore.getExpectCommrilMode();
    }

    public void setSource(int source) {
        if (this.mCore != null) {
            this.mCore.setSource(source);
        }
    }

    public int getSource() {
        if (this.mCore != null) {
            return this.mCore.getSource();
        }
        return 0;
    }

    public void setCardCount(int count) {
        if (this.mCore != null) {
            this.mCore.setCardCount(count);
        }
    }

    public int getCardCount() {
        if (this.mCore != null) {
            return this.mCore.getCardCount();
        }
        return 0;
    }
}
