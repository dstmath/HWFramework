package com.huawei.internal.telephony.cdma.sms;

import com.android.internal.telephony.cdma.sms.BearerData;

public final class BearerDataEx {
    public static final int MESSAGE_TYPE_DELIVER = 1;
    public static final int MESSAGE_TYPE_SUBMIT = 2;
    public static final int PRIORITY_NORMAL = 0;
    public static final byte SUBPARAM_MESSAGE_CENTER_TIME_STAMP = 3;
    private BearerData mBearerData = new BearerData();

    public static byte[] encode7bitAsciiHw(String msg, int septetOffset, boolean force) throws CodingExceptionEx {
        try {
            return BearerData.encode7bitAsciiHw(msg, septetOffset, force);
        } catch (BearerData.CodingException e) {
            throw new CodingExceptionEx("CodingException");
        }
    }

    public static byte[] encode(BearerDataEx bearerDataEx) {
        if (bearerDataEx == null || bearerDataEx.getBearerData() == null) {
            return null;
        }
        return BearerData.encode(bearerDataEx.getBearerData());
    }

    public static byte getSubparamMessageCenterTimeStamp() {
        return BearerData.getSubparamMessageCenterTimeStamp();
    }

    public void setMsgCenterTimeStamp(TimeStampEx timeStamp) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.msgCenterTimeStamp = timeStamp.getTimeStamp();
        }
    }

    public boolean isMsgCenterTimeStampExist() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return false;
        }
        return true;
    }

    public int getMsgCenterTimeStampYear() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.year;
    }

    public void setMsgCenterTimeStampYear(int year) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.year = year;
        }
    }

    public int getMsgCenterTimeStampMonth() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.month;
    }

    public void setMsgCenterTimeStampMonth(int month) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.month = month;
        }
    }

    public int getMsgCenterTimeStampMonthDay() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.monthDay;
    }

    public void setMsgCenterTimeStampMonthDay(int monthDay) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.monthDay = monthDay;
        }
    }

    public int getMsgCenterTimeStampHour() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.hour;
    }

    public void setMsgCenterTimeStampHour(int hour) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.hour = hour;
        }
    }

    public int getMsgCenterTimeStampMinute() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.minute;
    }

    public void setMsgCenterTimeStampMinute(int minute) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.minute = minute;
        }
    }

    public int getMsgCenterTimeStampSecond() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null || bearerData.msgCenterTimeStamp == null) {
            return 0;
        }
        return this.mBearerData.msgCenterTimeStamp.second;
    }

    public void setMsgCenterTimeStampSecond(int second) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null && bearerData.msgCenterTimeStamp != null) {
            this.mBearerData.msgCenterTimeStamp.second = second;
        }
    }

    public void setMessageId(int messageId) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.messageId = messageId;
        }
    }

    public void setUserAckReq(boolean userAckReq) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.userAckReq = userAckReq;
        }
    }

    public void setReadAckReq(boolean readAckReq) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.readAckReq = readAckReq;
        }
    }

    public void setReportReq(boolean reportReq) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.reportReq = reportReq;
        }
    }

    public void setDeliveryAckReq(boolean deliveryAckReq) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.deliveryAckReq = deliveryAckReq;
        }
    }

    public void setMessageType(int messageType) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.messageType = messageType;
        }
    }

    public void setPriorityIndicatorSet(boolean priorityIndicatorSet) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.priorityIndicatorSet = priorityIndicatorSet;
        }
    }

    public void setPriority(int priority) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.priority = priority;
        }
    }

    public void setUserData(UserDataEx userDataEx) {
        BearerData bearerData = this.mBearerData;
        if (bearerData != null) {
            bearerData.userData = userDataEx.getUserData();
        }
    }

    public BearerData getBearerData() {
        return this.mBearerData;
    }

    public void setBearerData(BearerData bearerData) {
        this.mBearerData = bearerData;
    }

    public String toString() {
        BearerData bearerData = this.mBearerData;
        if (bearerData == null) {
            return null;
        }
        return bearerData.toString();
    }

    public static class CodingExceptionEx extends Exception {
        public CodingExceptionEx(String s) {
            super(s);
        }
    }

    public static class TimeStampEx {
        BearerData.TimeStamp mTimeStamp = new BearerData.TimeStamp();

        public void set(long timestamp) {
            this.mTimeStamp.set(timestamp);
        }

        public BearerData.TimeStamp getTimeStamp() {
            return this.mTimeStamp;
        }
    }
}
