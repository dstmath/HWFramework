package com.android.internal.telephony.nano;

import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.RadioNVItems;
import com.android.internal.telephony.cat.BerTlv;
import com.android.internal.telephony.protobuf.nano.CodedInputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.ExtendableMessageNano;
import com.android.internal.telephony.protobuf.nano.InternalNano;
import com.android.internal.telephony.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.telephony.protobuf.nano.WireFormatNano;
import com.google.android.mms.pdu.PduHeaders;
import java.io.IOException;

public interface TelephonyProto {

    public interface ImsServiceErrno {
        public static final int IMS_E_SMS_SEND_STATUS_ERROR = 2;
        public static final int IMS_E_SMS_SEND_STATUS_ERROR_FALLBACK = 4;
        public static final int IMS_E_SMS_SEND_STATUS_ERROR_RETRY = 3;
        public static final int IMS_E_SUCCESS = 1;
        public static final int IMS_E_UNKNOWN = 0;
    }

    public interface PdpType {
        public static final int PDP_TYPE_IP = 1;
        public static final int PDP_TYPE_IPV4V6 = 3;
        public static final int PDP_TYPE_IPV6 = 2;
        public static final int PDP_TYPE_NON_IP = 5;
        public static final int PDP_TYPE_PPP = 4;
        public static final int PDP_TYPE_UNSTRUCTURED = 6;
        public static final int PDP_UNKNOWN = 0;
    }

    public interface RadioAccessTechnology {
        public static final int RAT_1XRTT = 6;
        public static final int RAT_EDGE = 2;
        public static final int RAT_EHRPD = 13;
        public static final int RAT_EVDO_0 = 7;
        public static final int RAT_EVDO_A = 8;
        public static final int RAT_EVDO_B = 12;
        public static final int RAT_GPRS = 1;
        public static final int RAT_GSM = 16;
        public static final int RAT_HSDPA = 9;
        public static final int RAT_HSPA = 11;
        public static final int RAT_HSPAP = 15;
        public static final int RAT_HSUPA = 10;
        public static final int RAT_IS95A = 4;
        public static final int RAT_IS95B = 5;
        public static final int RAT_IWLAN = 18;
        public static final int RAT_LTE = 14;
        public static final int RAT_LTE_CA = 19;
        public static final int RAT_TD_SCDMA = 17;
        public static final int RAT_UMTS = 3;
        public static final int RAT_UNKNOWN = 0;
        public static final int UNKNOWN = -1;
    }

    public interface RilErrno {
        public static final int RIL_E_ABORTED = 66;
        public static final int RIL_E_CANCELLED = 8;
        public static final int RIL_E_DEVICE_IN_USE = 65;
        public static final int RIL_E_DIAL_MODIFIED_TO_DIAL = 21;
        public static final int RIL_E_DIAL_MODIFIED_TO_SS = 20;
        public static final int RIL_E_DIAL_MODIFIED_TO_USSD = 19;
        public static final int RIL_E_EMPTY_RECORD = 56;
        public static final int RIL_E_ENCODING_ERR = 58;
        public static final int RIL_E_FDN_CHECK_FAILURE = 15;
        public static final int RIL_E_GENERIC_FAILURE = 3;
        public static final int RIL_E_ILLEGAL_SIM_OR_ME = 16;
        public static final int RIL_E_INTERNAL_ERR = 39;
        public static final int RIL_E_INVALID_ARGUMENTS = 45;
        public static final int RIL_E_INVALID_CALL_ID = 48;
        public static final int RIL_E_INVALID_MODEM_STATE = 47;
        public static final int RIL_E_INVALID_RESPONSE = 67;
        public static final int RIL_E_INVALID_SIM_STATE = 46;
        public static final int RIL_E_INVALID_SMSC_ADDRESS = 59;
        public static final int RIL_E_INVALID_SMS_FORMAT = 57;
        public static final int RIL_E_INVALID_STATE = 42;
        public static final int RIL_E_LCE_NOT_SUPPORTED = 36;
        public static final int RIL_E_LCE_NOT_SUPPORTED_NEW = 37;
        public static final int RIL_E_MISSING_RESOURCE = 17;
        public static final int RIL_E_MODEM_ERR = 41;
        public static final int RIL_E_MODE_NOT_SUPPORTED = 14;
        public static final int RIL_E_NETWORK_ERR = 50;
        public static final int RIL_E_NETWORK_NOT_READY = 61;
        public static final int RIL_E_NETWORK_REJECT = 54;
        public static final int RIL_E_NOT_PROVISIONED = 62;
        public static final int RIL_E_NO_MEMORY = 38;
        public static final int RIL_E_NO_NETWORK_FOUND = 64;
        public static final int RIL_E_NO_RESOURCES = 43;
        public static final int RIL_E_NO_SMS_TO_ACK = 49;
        public static final int RIL_E_NO_SUBSCRIPTION = 63;
        public static final int RIL_E_NO_SUCH_ELEMENT = 18;
        public static final int RIL_E_NO_SUCH_ENTRY = 60;
        public static final int RIL_E_OPERATION_NOT_ALLOWED = 55;
        public static final int RIL_E_OP_NOT_ALLOWED_BEFORE_REG_TO_NW = 10;
        public static final int RIL_E_OP_NOT_ALLOWED_DURING_VOICE_CALL = 9;
        public static final int RIL_E_PASSWORD_INCORRECT = 4;
        public static final int RIL_E_RADIO_NOT_AVAILABLE = 2;
        public static final int RIL_E_REQUEST_NOT_SUPPORTED = 7;
        public static final int RIL_E_REQUEST_RATE_LIMITED = 51;
        public static final int RIL_E_SIM_ABSENT = 12;
        public static final int RIL_E_SIM_BUSY = 52;
        public static final int RIL_E_SIM_ERR = 44;
        public static final int RIL_E_SIM_FULL = 53;
        public static final int RIL_E_SIM_PIN2 = 5;
        public static final int RIL_E_SIM_PUK2 = 6;
        public static final int RIL_E_SMS_SEND_FAIL_RETRY = 11;
        public static final int RIL_E_SS_MODIFIED_TO_DIAL = 25;
        public static final int RIL_E_SS_MODIFIED_TO_SS = 28;
        public static final int RIL_E_SS_MODIFIED_TO_USSD = 26;
        public static final int RIL_E_SUBSCRIPTION_NOT_AVAILABLE = 13;
        public static final int RIL_E_SUBSCRIPTION_NOT_SUPPORTED = 27;
        public static final int RIL_E_SUCCESS = 1;
        public static final int RIL_E_SYSTEM_ERR = 40;
        public static final int RIL_E_UNKNOWN = 0;
        public static final int RIL_E_USSD_MODIFIED_TO_DIAL = 22;
        public static final int RIL_E_USSD_MODIFIED_TO_SS = 23;
        public static final int RIL_E_USSD_MODIFIED_TO_USSD = 24;
    }

    public interface SimState {
        public static final int SIM_STATE_ABSENT = 1;
        public static final int SIM_STATE_LOADED = 2;
        public static final int SIM_STATE_UNKNOWN = 0;
    }

    public interface TimeInterval {
        public static final int TI_100_MILLIS = 4;
        public static final int TI_10_MILLIS = 1;
        public static final int TI_10_MINUTES = 14;
        public static final int TI_10_SEC = 10;
        public static final int TI_1_HOUR = 16;
        public static final int TI_1_MINUTE = 12;
        public static final int TI_1_SEC = 7;
        public static final int TI_200_MILLIS = 5;
        public static final int TI_20_MILLIS = 2;
        public static final int TI_2_HOURS = 17;
        public static final int TI_2_SEC = 8;
        public static final int TI_30_MINUTES = 15;
        public static final int TI_30_SEC = 11;
        public static final int TI_3_MINUTES = 13;
        public static final int TI_4_HOURS = 18;
        public static final int TI_500_MILLIS = 6;
        public static final int TI_50_MILLIS = 3;
        public static final int TI_5_SEC = 9;
        public static final int TI_MANY_HOURS = 19;
        public static final int TI_UNKNOWN = 0;
    }

    public static final class TelephonyLog extends ExtendableMessageNano<TelephonyLog> {
        private static volatile TelephonyLog[] _emptyArray;
        public TelephonyCallSession[] callSessions;
        public Time endTime;
        public TelephonyEvent[] events;
        public boolean eventsDropped;
        public String hardwareRevision;
        public TelephonyHistogram[] histograms;
        public ActiveSubscriptionInfo[] lastActiveSubscriptionInfo;
        public ModemPowerStats modemPowerStats;
        public SmsSession[] smsSessions;
        public Time startTime;

        public static TelephonyLog[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonyLog[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonyLog() {
            clear();
        }

        public TelephonyLog clear() {
            this.events = TelephonyEvent.emptyArray();
            this.callSessions = TelephonyCallSession.emptyArray();
            this.smsSessions = SmsSession.emptyArray();
            this.histograms = TelephonyHistogram.emptyArray();
            this.eventsDropped = false;
            this.startTime = null;
            this.endTime = null;
            this.modemPowerStats = null;
            this.hardwareRevision = PhoneConfigurationManager.SSSS;
            this.lastActiveSubscriptionInfo = ActiveSubscriptionInfo.emptyArray();
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            TelephonyEvent[] telephonyEventArr = this.events;
            if (telephonyEventArr != null && telephonyEventArr.length > 0) {
                int i = 0;
                while (true) {
                    TelephonyEvent[] telephonyEventArr2 = this.events;
                    if (i >= telephonyEventArr2.length) {
                        break;
                    }
                    TelephonyEvent element = telephonyEventArr2[i];
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                    i++;
                }
            }
            TelephonyCallSession[] telephonyCallSessionArr = this.callSessions;
            if (telephonyCallSessionArr != null && telephonyCallSessionArr.length > 0) {
                int i2 = 0;
                while (true) {
                    TelephonyCallSession[] telephonyCallSessionArr2 = this.callSessions;
                    if (i2 >= telephonyCallSessionArr2.length) {
                        break;
                    }
                    TelephonyCallSession element2 = telephonyCallSessionArr2[i2];
                    if (element2 != null) {
                        output.writeMessage(2, element2);
                    }
                    i2++;
                }
            }
            SmsSession[] smsSessionArr = this.smsSessions;
            if (smsSessionArr != null && smsSessionArr.length > 0) {
                int i3 = 0;
                while (true) {
                    SmsSession[] smsSessionArr2 = this.smsSessions;
                    if (i3 >= smsSessionArr2.length) {
                        break;
                    }
                    SmsSession element3 = smsSessionArr2[i3];
                    if (element3 != null) {
                        output.writeMessage(3, element3);
                    }
                    i3++;
                }
            }
            TelephonyHistogram[] telephonyHistogramArr = this.histograms;
            if (telephonyHistogramArr != null && telephonyHistogramArr.length > 0) {
                int i4 = 0;
                while (true) {
                    TelephonyHistogram[] telephonyHistogramArr2 = this.histograms;
                    if (i4 >= telephonyHistogramArr2.length) {
                        break;
                    }
                    TelephonyHistogram element4 = telephonyHistogramArr2[i4];
                    if (element4 != null) {
                        output.writeMessage(4, element4);
                    }
                    i4++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                output.writeBool(5, z);
            }
            Time time = this.startTime;
            if (time != null) {
                output.writeMessage(6, time);
            }
            Time time2 = this.endTime;
            if (time2 != null) {
                output.writeMessage(7, time2);
            }
            ModemPowerStats modemPowerStats2 = this.modemPowerStats;
            if (modemPowerStats2 != null) {
                output.writeMessage(8, modemPowerStats2);
            }
            if (!this.hardwareRevision.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(9, this.hardwareRevision);
            }
            ActiveSubscriptionInfo[] activeSubscriptionInfoArr = this.lastActiveSubscriptionInfo;
            if (activeSubscriptionInfoArr != null && activeSubscriptionInfoArr.length > 0) {
                int i5 = 0;
                while (true) {
                    ActiveSubscriptionInfo[] activeSubscriptionInfoArr2 = this.lastActiveSubscriptionInfo;
                    if (i5 >= activeSubscriptionInfoArr2.length) {
                        break;
                    }
                    ActiveSubscriptionInfo element5 = activeSubscriptionInfoArr2[i5];
                    if (element5 != null) {
                        output.writeMessage(10, element5);
                    }
                    i5++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            TelephonyEvent[] telephonyEventArr = this.events;
            if (telephonyEventArr != null && telephonyEventArr.length > 0) {
                int i = 0;
                while (true) {
                    TelephonyEvent[] telephonyEventArr2 = this.events;
                    if (i >= telephonyEventArr2.length) {
                        break;
                    }
                    TelephonyEvent element = telephonyEventArr2[i];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                    i++;
                }
            }
            TelephonyCallSession[] telephonyCallSessionArr = this.callSessions;
            if (telephonyCallSessionArr != null && telephonyCallSessionArr.length > 0) {
                int i2 = 0;
                while (true) {
                    TelephonyCallSession[] telephonyCallSessionArr2 = this.callSessions;
                    if (i2 >= telephonyCallSessionArr2.length) {
                        break;
                    }
                    TelephonyCallSession element2 = telephonyCallSessionArr2[i2];
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                    }
                    i2++;
                }
            }
            SmsSession[] smsSessionArr = this.smsSessions;
            if (smsSessionArr != null && smsSessionArr.length > 0) {
                int i3 = 0;
                while (true) {
                    SmsSession[] smsSessionArr2 = this.smsSessions;
                    if (i3 >= smsSessionArr2.length) {
                        break;
                    }
                    SmsSession element3 = smsSessionArr2[i3];
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element3);
                    }
                    i3++;
                }
            }
            TelephonyHistogram[] telephonyHistogramArr = this.histograms;
            if (telephonyHistogramArr != null && telephonyHistogramArr.length > 0) {
                int i4 = 0;
                while (true) {
                    TelephonyHistogram[] telephonyHistogramArr2 = this.histograms;
                    if (i4 >= telephonyHistogramArr2.length) {
                        break;
                    }
                    TelephonyHistogram element4 = telephonyHistogramArr2[i4];
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element4);
                    }
                    i4++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, z);
            }
            Time time = this.startTime;
            if (time != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, time);
            }
            Time time2 = this.endTime;
            if (time2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, time2);
            }
            ModemPowerStats modemPowerStats2 = this.modemPowerStats;
            if (modemPowerStats2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(8, modemPowerStats2);
            }
            if (!this.hardwareRevision.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(9, this.hardwareRevision);
            }
            ActiveSubscriptionInfo[] activeSubscriptionInfoArr = this.lastActiveSubscriptionInfo;
            if (activeSubscriptionInfoArr != null && activeSubscriptionInfoArr.length > 0) {
                int i5 = 0;
                while (true) {
                    ActiveSubscriptionInfo[] activeSubscriptionInfoArr2 = this.lastActiveSubscriptionInfo;
                    if (i5 >= activeSubscriptionInfoArr2.length) {
                        break;
                    }
                    ActiveSubscriptionInfo element5 = activeSubscriptionInfoArr2[i5];
                    if (element5 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(10, element5);
                    }
                    i5++;
                }
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonyLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        TelephonyEvent[] telephonyEventArr = this.events;
                        int i = telephonyEventArr == null ? 0 : telephonyEventArr.length;
                        TelephonyEvent[] newArray = new TelephonyEvent[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.events, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new TelephonyEvent();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new TelephonyEvent();
                        input.readMessage(newArray[i]);
                        this.events = newArray;
                        break;
                    case 18:
                        int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        TelephonyCallSession[] telephonyCallSessionArr = this.callSessions;
                        int i2 = telephonyCallSessionArr == null ? 0 : telephonyCallSessionArr.length;
                        TelephonyCallSession[] newArray2 = new TelephonyCallSession[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.callSessions, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length - 1) {
                            newArray2[i2] = new TelephonyCallSession();
                            input.readMessage(newArray2[i2]);
                            input.readTag();
                            i2++;
                        }
                        newArray2[i2] = new TelephonyCallSession();
                        input.readMessage(newArray2[i2]);
                        this.callSessions = newArray2;
                        break;
                    case 26:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        SmsSession[] smsSessionArr = this.smsSessions;
                        int i3 = smsSessionArr == null ? 0 : smsSessionArr.length;
                        SmsSession[] newArray3 = new SmsSession[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.smsSessions, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = new SmsSession();
                            input.readMessage(newArray3[i3]);
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = new SmsSession();
                        input.readMessage(newArray3[i3]);
                        this.smsSessions = newArray3;
                        break;
                    case 34:
                        int arrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        TelephonyHistogram[] telephonyHistogramArr = this.histograms;
                        int i4 = telephonyHistogramArr == null ? 0 : telephonyHistogramArr.length;
                        TelephonyHistogram[] newArray4 = new TelephonyHistogram[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.histograms, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length - 1) {
                            newArray4[i4] = new TelephonyHistogram();
                            input.readMessage(newArray4[i4]);
                            input.readTag();
                            i4++;
                        }
                        newArray4[i4] = new TelephonyHistogram();
                        input.readMessage(newArray4[i4]);
                        this.histograms = newArray4;
                        break;
                    case 40:
                        this.eventsDropped = input.readBool();
                        break;
                    case 50:
                        if (this.startTime == null) {
                            this.startTime = new Time();
                        }
                        input.readMessage(this.startTime);
                        break;
                    case 58:
                        if (this.endTime == null) {
                            this.endTime = new Time();
                        }
                        input.readMessage(this.endTime);
                        break;
                    case 66:
                        if (this.modemPowerStats == null) {
                            this.modemPowerStats = new ModemPowerStats();
                        }
                        input.readMessage(this.modemPowerStats);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                        this.hardwareRevision = input.readString();
                        break;
                    case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /* 82 */:
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 82);
                        ActiveSubscriptionInfo[] activeSubscriptionInfoArr = this.lastActiveSubscriptionInfo;
                        int i5 = activeSubscriptionInfoArr == null ? 0 : activeSubscriptionInfoArr.length;
                        ActiveSubscriptionInfo[] newArray5 = new ActiveSubscriptionInfo[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.lastActiveSubscriptionInfo, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = new ActiveSubscriptionInfo();
                            input.readMessage(newArray5[i5]);
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = new ActiveSubscriptionInfo();
                        input.readMessage(newArray5[i5]);
                        this.lastActiveSubscriptionInfo = newArray5;
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static TelephonyLog parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonyLog) MessageNano.mergeFrom(new TelephonyLog(), data);
        }

        public static TelephonyLog parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonyLog().mergeFrom(input);
        }
    }

    public static final class Time extends ExtendableMessageNano<Time> {
        private static volatile Time[] _emptyArray;
        public long elapsedTimestampMillis;
        public long systemTimestampMillis;

        public static Time[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Time[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Time() {
            clear();
        }

        public Time clear() {
            this.systemTimestampMillis = 0;
            this.elapsedTimestampMillis = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.systemTimestampMillis;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            long j2 = this.elapsedTimestampMillis;
            if (j2 != 0) {
                output.writeInt64(2, j2);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            long j = this.systemTimestampMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            long j2 = this.elapsedTimestampMillis;
            if (j2 != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(2, j2);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public Time mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.systemTimestampMillis = input.readInt64();
                } else if (tag == 16) {
                    this.elapsedTimestampMillis = input.readInt64();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static Time parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (Time) MessageNano.mergeFrom(new Time(), data);
        }

        public static Time parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new Time().mergeFrom(input);
        }
    }

    public static final class TelephonyHistogram extends ExtendableMessageNano<TelephonyHistogram> {
        private static volatile TelephonyHistogram[] _emptyArray;
        public int avgTimeMillis;
        public int bucketCount;
        public int[] bucketCounters;
        public int[] bucketEndPoints;
        public int category;
        public int count;
        public int id;
        public int maxTimeMillis;
        public int minTimeMillis;

        public static TelephonyHistogram[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonyHistogram[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonyHistogram() {
            clear();
        }

        public TelephonyHistogram clear() {
            this.category = 0;
            this.id = 0;
            this.minTimeMillis = 0;
            this.maxTimeMillis = 0;
            this.avgTimeMillis = 0;
            this.count = 0;
            this.bucketCount = 0;
            this.bucketEndPoints = WireFormatNano.EMPTY_INT_ARRAY;
            this.bucketCounters = WireFormatNano.EMPTY_INT_ARRAY;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.category;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.id;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.minTimeMillis;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            int i4 = this.maxTimeMillis;
            if (i4 != 0) {
                output.writeInt32(4, i4);
            }
            int i5 = this.avgTimeMillis;
            if (i5 != 0) {
                output.writeInt32(5, i5);
            }
            int i6 = this.count;
            if (i6 != 0) {
                output.writeInt32(6, i6);
            }
            int i7 = this.bucketCount;
            if (i7 != 0) {
                output.writeInt32(7, i7);
            }
            int[] iArr = this.bucketEndPoints;
            if (iArr != null && iArr.length > 0) {
                int i8 = 0;
                while (true) {
                    int[] iArr2 = this.bucketEndPoints;
                    if (i8 >= iArr2.length) {
                        break;
                    }
                    output.writeInt32(8, iArr2[i8]);
                    i8++;
                }
            }
            int[] iArr3 = this.bucketCounters;
            if (iArr3 != null && iArr3.length > 0) {
                int i9 = 0;
                while (true) {
                    int[] iArr4 = this.bucketCounters;
                    if (i9 >= iArr4.length) {
                        break;
                    }
                    output.writeInt32(9, iArr4[i9]);
                    i9++;
                }
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int[] iArr;
            int size = super.computeSerializedSize();
            int i = this.category;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.id;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.minTimeMillis;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            int i4 = this.maxTimeMillis;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
            }
            int i5 = this.avgTimeMillis;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
            }
            int i6 = this.count;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i6);
            }
            int i7 = this.bucketCount;
            if (i7 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, i7);
            }
            int[] iArr2 = this.bucketEndPoints;
            if (iArr2 != null && iArr2.length > 0) {
                int dataSize = 0;
                int i8 = 0;
                while (true) {
                    iArr = this.bucketEndPoints;
                    if (i8 >= iArr.length) {
                        break;
                    }
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(iArr[i8]);
                    i8++;
                }
                size = size + dataSize + (iArr.length * 1);
            }
            int[] iArr3 = this.bucketCounters;
            if (iArr3 == null || iArr3.length <= 0) {
                return size;
            }
            int dataSize2 = 0;
            int i9 = 0;
            while (true) {
                int[] iArr4 = this.bucketCounters;
                if (i9 >= iArr4.length) {
                    return size + dataSize2 + (iArr4.length * 1);
                }
                dataSize2 += CodedOutputByteBufferNano.computeInt32SizeNoTag(iArr4[i9]);
                i9++;
            }
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonyHistogram mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.category = input.readInt32();
                        break;
                    case 16:
                        this.id = input.readInt32();
                        break;
                    case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                        this.minTimeMillis = input.readInt32();
                        break;
                    case 32:
                        this.maxTimeMillis = input.readInt32();
                        break;
                    case 40:
                        this.avgTimeMillis = input.readInt32();
                        break;
                    case 48:
                        this.count = input.readInt32();
                        break;
                    case 56:
                        this.bucketCount = input.readInt32();
                        break;
                    case 64:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 64);
                        int[] iArr = this.bucketEndPoints;
                        int i = iArr == null ? 0 : iArr.length;
                        int[] newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.bucketEndPoints, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.bucketEndPoints = newArray;
                        break;
                    case 66:
                        int limit = input.pushLimit(input.readRawVarint32());
                        int arrayLength2 = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength2++;
                        }
                        input.rewindToPosition(startPos);
                        int[] iArr2 = this.bucketEndPoints;
                        int i2 = iArr2 == null ? 0 : iArr2.length;
                        int[] newArray2 = new int[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.bucketEndPoints, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length) {
                            newArray2[i2] = input.readInt32();
                            i2++;
                        }
                        this.bucketEndPoints = newArray2;
                        input.popLimit(limit);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 72);
                        int[] iArr3 = this.bucketCounters;
                        int i3 = iArr3 == null ? 0 : iArr3.length;
                        int[] newArray3 = new int[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.bucketCounters, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = input.readInt32();
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = input.readInt32();
                        this.bucketCounters = newArray3;
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                        int limit2 = input.pushLimit(input.readRawVarint32());
                        int arrayLength4 = 0;
                        int startPos2 = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength4++;
                        }
                        input.rewindToPosition(startPos2);
                        int[] iArr4 = this.bucketCounters;
                        int i4 = iArr4 == null ? 0 : iArr4.length;
                        int[] newArray4 = new int[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.bucketCounters, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length) {
                            newArray4[i4] = input.readInt32();
                            i4++;
                        }
                        this.bucketCounters = newArray4;
                        input.popLimit(limit2);
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static TelephonyHistogram parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonyHistogram) MessageNano.mergeFrom(new TelephonyHistogram(), data);
        }

        public static TelephonyHistogram parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonyHistogram().mergeFrom(input);
        }
    }

    public static final class TelephonySettings extends ExtendableMessageNano<TelephonySettings> {
        private static volatile TelephonySettings[] _emptyArray;
        public boolean isAirplaneMode;
        public boolean isCellularDataEnabled;
        public boolean isDataRoamingEnabled;
        public boolean isEnhanced4GLteModeEnabled;
        public boolean isVtOverLteEnabled;
        public boolean isVtOverWifiEnabled;
        public boolean isWifiCallingEnabled;
        public boolean isWifiEnabled;
        public int preferredNetworkMode;
        public int wifiCallingMode;

        public interface RilNetworkMode {
            public static final int NETWORK_MODE_CDMA = 5;
            public static final int NETWORK_MODE_CDMA_NO_EVDO = 6;
            public static final int NETWORK_MODE_EVDO_NO_CDMA = 7;
            public static final int NETWORK_MODE_GLOBAL = 8;
            public static final int NETWORK_MODE_GSM_ONLY = 2;
            public static final int NETWORK_MODE_GSM_UMTS = 4;
            public static final int NETWORK_MODE_LTE_CDMA_EVDO = 9;
            public static final int NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA = 11;
            public static final int NETWORK_MODE_LTE_GSM_WCDMA = 10;
            public static final int NETWORK_MODE_LTE_ONLY = 12;
            public static final int NETWORK_MODE_LTE_TDSCDMA = 16;
            public static final int NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 23;
            public static final int NETWORK_MODE_LTE_TDSCDMA_GSM = 18;
            public static final int NETWORK_MODE_LTE_TDSCDMA_GSM_WCDMA = 21;
            public static final int NETWORK_MODE_LTE_TDSCDMA_WCDMA = 20;
            public static final int NETWORK_MODE_LTE_WCDMA = 13;
            public static final int NETWORK_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 22;
            public static final int NETWORK_MODE_TDSCDMA_GSM = 17;
            public static final int NETWORK_MODE_TDSCDMA_GSM_WCDMA = 19;
            public static final int NETWORK_MODE_TDSCDMA_ONLY = 14;
            public static final int NETWORK_MODE_TDSCDMA_WCDMA = 15;
            public static final int NETWORK_MODE_UNKNOWN = 0;
            public static final int NETWORK_MODE_WCDMA_ONLY = 3;
            public static final int NETWORK_MODE_WCDMA_PREF = 1;
        }

        public interface WiFiCallingMode {
            public static final int WFC_MODE_CELLULAR_PREFERRED = 2;
            public static final int WFC_MODE_UNKNOWN = 0;
            public static final int WFC_MODE_WIFI_ONLY = 1;
            public static final int WFC_MODE_WIFI_PREFERRED = 3;
        }

        public static TelephonySettings[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonySettings[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonySettings() {
            clear();
        }

        public TelephonySettings clear() {
            this.isAirplaneMode = false;
            this.isCellularDataEnabled = false;
            this.isDataRoamingEnabled = false;
            this.preferredNetworkMode = 0;
            this.isEnhanced4GLteModeEnabled = false;
            this.isWifiEnabled = false;
            this.isWifiCallingEnabled = false;
            this.wifiCallingMode = 0;
            this.isVtOverLteEnabled = false;
            this.isVtOverWifiEnabled = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            boolean z = this.isAirplaneMode;
            if (z) {
                output.writeBool(1, z);
            }
            boolean z2 = this.isCellularDataEnabled;
            if (z2) {
                output.writeBool(2, z2);
            }
            boolean z3 = this.isDataRoamingEnabled;
            if (z3) {
                output.writeBool(3, z3);
            }
            int i = this.preferredNetworkMode;
            if (i != 0) {
                output.writeInt32(4, i);
            }
            boolean z4 = this.isEnhanced4GLteModeEnabled;
            if (z4) {
                output.writeBool(5, z4);
            }
            boolean z5 = this.isWifiEnabled;
            if (z5) {
                output.writeBool(6, z5);
            }
            boolean z6 = this.isWifiCallingEnabled;
            if (z6) {
                output.writeBool(7, z6);
            }
            int i2 = this.wifiCallingMode;
            if (i2 != 0) {
                output.writeInt32(8, i2);
            }
            boolean z7 = this.isVtOverLteEnabled;
            if (z7) {
                output.writeBool(9, z7);
            }
            boolean z8 = this.isVtOverWifiEnabled;
            if (z8) {
                output.writeBool(10, z8);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            boolean z = this.isAirplaneMode;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, z);
            }
            boolean z2 = this.isCellularDataEnabled;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, z2);
            }
            boolean z3 = this.isDataRoamingEnabled;
            if (z3) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, z3);
            }
            int i = this.preferredNetworkMode;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i);
            }
            boolean z4 = this.isEnhanced4GLteModeEnabled;
            if (z4) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, z4);
            }
            boolean z5 = this.isWifiEnabled;
            if (z5) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, z5);
            }
            boolean z6 = this.isWifiCallingEnabled;
            if (z6) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, z6);
            }
            int i2 = this.wifiCallingMode;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, i2);
            }
            boolean z7 = this.isVtOverLteEnabled;
            if (z7) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, z7);
            }
            boolean z8 = this.isVtOverWifiEnabled;
            if (z8) {
                return size + CodedOutputByteBufferNano.computeBoolSize(10, z8);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonySettings mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.isAirplaneMode = input.readBool();
                        break;
                    case 16:
                        this.isCellularDataEnabled = input.readBool();
                        break;
                    case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                        this.isDataRoamingEnabled = input.readBool();
                        break;
                    case 32:
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                                this.preferredNetworkMode = value;
                                continue;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    case 40:
                        this.isEnhanced4GLteModeEnabled = input.readBool();
                        break;
                    case 48:
                        this.isWifiEnabled = input.readBool();
                        break;
                    case 56:
                        this.isWifiCallingEnabled = input.readBool();
                        break;
                    case 64:
                        int initialPos2 = input.getPosition();
                        int value2 = input.readInt32();
                        if (value2 != 0 && value2 != 1 && value2 != 2 && value2 != 3) {
                            input.rewindToPosition(initialPos2);
                            storeUnknownField(input, tag);
                            break;
                        } else {
                            this.wifiCallingMode = value2;
                            break;
                        }
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                        this.isVtOverLteEnabled = input.readBool();
                        break;
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                        this.isVtOverWifiEnabled = input.readBool();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static TelephonySettings parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonySettings) MessageNano.mergeFrom(new TelephonySettings(), data);
        }

        public static TelephonySettings parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonySettings().mergeFrom(input);
        }
    }

    public static final class TelephonyServiceState extends ExtendableMessageNano<TelephonyServiceState> {
        private static volatile TelephonyServiceState[] _emptyArray;
        public int channelNumber;
        public TelephonyOperator dataOperator;
        public int dataRat;
        public int dataRoamingType;
        public TelephonyOperator voiceOperator;
        public int voiceRat;
        public int voiceRoamingType;

        public interface RoamingType {
            public static final int ROAMING_TYPE_DOMESTIC = 2;
            public static final int ROAMING_TYPE_INTERNATIONAL = 3;
            public static final int ROAMING_TYPE_NOT_ROAMING = 0;
            public static final int ROAMING_TYPE_UNKNOWN = 1;
            public static final int UNKNOWN = -1;
        }

        public static final class TelephonyOperator extends ExtendableMessageNano<TelephonyOperator> {
            private static volatile TelephonyOperator[] _emptyArray;
            public String alphaLong;
            public String alphaShort;
            public String numeric;

            public static TelephonyOperator[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new TelephonyOperator[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public TelephonyOperator() {
                clear();
            }

            public TelephonyOperator clear() {
                this.alphaLong = PhoneConfigurationManager.SSSS;
                this.alphaShort = PhoneConfigurationManager.SSSS;
                this.numeric = PhoneConfigurationManager.SSSS;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!this.alphaLong.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(1, this.alphaLong);
                }
                if (!this.alphaShort.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(2, this.alphaShort);
                }
                if (!this.numeric.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(3, this.numeric);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!this.alphaLong.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.alphaLong);
                }
                if (!this.alphaShort.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(2, this.alphaShort);
                }
                if (!this.numeric.equals(PhoneConfigurationManager.SSSS)) {
                    return size + CodedOutputByteBufferNano.computeStringSize(3, this.numeric);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public TelephonyOperator mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 10) {
                        this.alphaLong = input.readString();
                    } else if (tag == 18) {
                        this.alphaShort = input.readString();
                    } else if (tag == 26) {
                        this.numeric = input.readString();
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static TelephonyOperator parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (TelephonyOperator) MessageNano.mergeFrom(new TelephonyOperator(), data);
            }

            public static TelephonyOperator parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new TelephonyOperator().mergeFrom(input);
            }
        }

        public static TelephonyServiceState[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonyServiceState[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonyServiceState() {
            clear();
        }

        public TelephonyServiceState clear() {
            this.voiceOperator = null;
            this.dataOperator = null;
            this.voiceRoamingType = -1;
            this.dataRoamingType = -1;
            this.voiceRat = -1;
            this.dataRat = -1;
            this.channelNumber = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            TelephonyOperator telephonyOperator = this.voiceOperator;
            if (telephonyOperator != null) {
                output.writeMessage(1, telephonyOperator);
            }
            TelephonyOperator telephonyOperator2 = this.dataOperator;
            if (telephonyOperator2 != null) {
                output.writeMessage(2, telephonyOperator2);
            }
            int i = this.voiceRoamingType;
            if (i != -1) {
                output.writeInt32(3, i);
            }
            int i2 = this.dataRoamingType;
            if (i2 != -1) {
                output.writeInt32(4, i2);
            }
            int i3 = this.voiceRat;
            if (i3 != -1) {
                output.writeInt32(5, i3);
            }
            int i4 = this.dataRat;
            if (i4 != -1) {
                output.writeInt32(6, i4);
            }
            int i5 = this.channelNumber;
            if (i5 != 0) {
                output.writeInt32(7, i5);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            TelephonyOperator telephonyOperator = this.voiceOperator;
            if (telephonyOperator != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, telephonyOperator);
            }
            TelephonyOperator telephonyOperator2 = this.dataOperator;
            if (telephonyOperator2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, telephonyOperator2);
            }
            int i = this.voiceRoamingType;
            if (i != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i);
            }
            int i2 = this.dataRoamingType;
            if (i2 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i2);
            }
            int i3 = this.voiceRat;
            if (i3 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, i3);
            }
            int i4 = this.dataRat;
            if (i4 != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, i4);
            }
            int i5 = this.channelNumber;
            if (i5 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(7, i5);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonyServiceState mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    if (this.voiceOperator == null) {
                        this.voiceOperator = new TelephonyOperator();
                    }
                    input.readMessage(this.voiceOperator);
                } else if (tag == 18) {
                    if (this.dataOperator == null) {
                        this.dataOperator = new TelephonyOperator();
                    }
                    input.readMessage(this.dataOperator);
                } else if (tag == 24) {
                    int initialPos = input.getPosition();
                    int value = input.readInt32();
                    if (value == -1 || value == 0 || value == 1 || value == 2 || value == 3) {
                        this.voiceRoamingType = value;
                    } else {
                        input.rewindToPosition(initialPos);
                        storeUnknownField(input, tag);
                    }
                } else if (tag == 32) {
                    int initialPos2 = input.getPosition();
                    int value2 = input.readInt32();
                    if (value2 == -1 || value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3) {
                        this.dataRoamingType = value2;
                    } else {
                        input.rewindToPosition(initialPos2);
                        storeUnknownField(input, tag);
                    }
                } else if (tag == 40) {
                    int initialPos3 = input.getPosition();
                    int value3 = input.readInt32();
                    switch (value3) {
                        case -1:
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                            this.voiceRat = value3;
                            continue;
                        default:
                            input.rewindToPosition(initialPos3);
                            storeUnknownField(input, tag);
                            continue;
                    }
                } else if (tag == 48) {
                    int initialPos4 = input.getPosition();
                    int value4 = input.readInt32();
                    switch (value4) {
                        case -1:
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                            this.dataRat = value4;
                            continue;
                        default:
                            input.rewindToPosition(initialPos4);
                            storeUnknownField(input, tag);
                            continue;
                    }
                } else if (tag == 56) {
                    this.channelNumber = input.readInt32();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static TelephonyServiceState parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonyServiceState) MessageNano.mergeFrom(new TelephonyServiceState(), data);
        }

        public static TelephonyServiceState parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonyServiceState().mergeFrom(input);
        }
    }

    public static final class ImsReasonInfo extends ExtendableMessageNano<ImsReasonInfo> {
        private static volatile ImsReasonInfo[] _emptyArray;
        public int extraCode;
        public String extraMessage;
        public int reasonCode;

        public static ImsReasonInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ImsReasonInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ImsReasonInfo() {
            clear();
        }

        public ImsReasonInfo clear() {
            this.reasonCode = 0;
            this.extraCode = 0;
            this.extraMessage = PhoneConfigurationManager.SSSS;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.reasonCode;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.extraCode;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            if (!this.extraMessage.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(3, this.extraMessage);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.reasonCode;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.extraCode;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            if (!this.extraMessage.equals(PhoneConfigurationManager.SSSS)) {
                return size + CodedOutputByteBufferNano.computeStringSize(3, this.extraMessage);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public ImsReasonInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.reasonCode = input.readInt32();
                } else if (tag == 16) {
                    this.extraCode = input.readInt32();
                } else if (tag == 26) {
                    this.extraMessage = input.readString();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ImsReasonInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ImsReasonInfo) MessageNano.mergeFrom(new ImsReasonInfo(), data);
        }

        public static ImsReasonInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ImsReasonInfo().mergeFrom(input);
        }
    }

    public static final class ImsConnectionState extends ExtendableMessageNano<ImsConnectionState> {
        private static volatile ImsConnectionState[] _emptyArray;
        public ImsReasonInfo reasonInfo;
        public int state;

        public interface State {
            public static final int CONNECTED = 1;
            public static final int DISCONNECTED = 3;
            public static final int PROGRESSING = 2;
            public static final int RESUMED = 4;
            public static final int STATE_UNKNOWN = 0;
            public static final int SUSPENDED = 5;
        }

        public static ImsConnectionState[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ImsConnectionState[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ImsConnectionState() {
            clear();
        }

        public ImsConnectionState clear() {
            this.state = 0;
            this.reasonInfo = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.state;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            ImsReasonInfo imsReasonInfo = this.reasonInfo;
            if (imsReasonInfo != null) {
                output.writeMessage(2, imsReasonInfo);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.state;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            ImsReasonInfo imsReasonInfo = this.reasonInfo;
            if (imsReasonInfo != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(2, imsReasonInfo);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public ImsConnectionState mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    int initialPos = input.getPosition();
                    int value = input.readInt32();
                    if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4 || value == 5) {
                        this.state = value;
                    } else {
                        input.rewindToPosition(initialPos);
                        storeUnknownField(input, tag);
                    }
                } else if (tag == 18) {
                    if (this.reasonInfo == null) {
                        this.reasonInfo = new ImsReasonInfo();
                    }
                    input.readMessage(this.reasonInfo);
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ImsConnectionState parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ImsConnectionState) MessageNano.mergeFrom(new ImsConnectionState(), data);
        }

        public static ImsConnectionState parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ImsConnectionState().mergeFrom(input);
        }
    }

    public static final class ImsCapabilities extends ExtendableMessageNano<ImsCapabilities> {
        private static volatile ImsCapabilities[] _emptyArray;
        public boolean utOverLte;
        public boolean utOverWifi;
        public boolean videoOverLte;
        public boolean videoOverWifi;
        public boolean voiceOverLte;
        public boolean voiceOverWifi;

        public static ImsCapabilities[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ImsCapabilities[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ImsCapabilities() {
            clear();
        }

        public ImsCapabilities clear() {
            this.voiceOverLte = false;
            this.voiceOverWifi = false;
            this.videoOverLte = false;
            this.videoOverWifi = false;
            this.utOverLte = false;
            this.utOverWifi = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            boolean z = this.voiceOverLte;
            if (z) {
                output.writeBool(1, z);
            }
            boolean z2 = this.voiceOverWifi;
            if (z2) {
                output.writeBool(2, z2);
            }
            boolean z3 = this.videoOverLte;
            if (z3) {
                output.writeBool(3, z3);
            }
            boolean z4 = this.videoOverWifi;
            if (z4) {
                output.writeBool(4, z4);
            }
            boolean z5 = this.utOverLte;
            if (z5) {
                output.writeBool(5, z5);
            }
            boolean z6 = this.utOverWifi;
            if (z6) {
                output.writeBool(6, z6);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            boolean z = this.voiceOverLte;
            if (z) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, z);
            }
            boolean z2 = this.voiceOverWifi;
            if (z2) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, z2);
            }
            boolean z3 = this.videoOverLte;
            if (z3) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, z3);
            }
            boolean z4 = this.videoOverWifi;
            if (z4) {
                size += CodedOutputByteBufferNano.computeBoolSize(4, z4);
            }
            boolean z5 = this.utOverLte;
            if (z5) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, z5);
            }
            boolean z6 = this.utOverWifi;
            if (z6) {
                return size + CodedOutputByteBufferNano.computeBoolSize(6, z6);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public ImsCapabilities mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.voiceOverLte = input.readBool();
                } else if (tag == 16) {
                    this.voiceOverWifi = input.readBool();
                } else if (tag == 24) {
                    this.videoOverLte = input.readBool();
                } else if (tag == 32) {
                    this.videoOverWifi = input.readBool();
                } else if (tag == 40) {
                    this.utOverLte = input.readBool();
                } else if (tag == 48) {
                    this.utOverWifi = input.readBool();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ImsCapabilities parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ImsCapabilities) MessageNano.mergeFrom(new ImsCapabilities(), data);
        }

        public static ImsCapabilities parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ImsCapabilities().mergeFrom(input);
        }
    }

    public static final class RilDataCall extends ExtendableMessageNano<RilDataCall> {
        private static volatile RilDataCall[] _emptyArray;
        public int apnTypeBitmask;
        public int cid;
        public String iframe;
        public int state;
        public int type;

        public interface State {
            public static final int CONNECTED = 1;
            public static final int DISCONNECTED = 2;
            public static final int UNKNOWN = 0;
        }

        public static RilDataCall[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RilDataCall[0];
                    }
                }
            }
            return _emptyArray;
        }

        public RilDataCall() {
            clear();
        }

        public RilDataCall clear() {
            this.cid = 0;
            this.type = 0;
            this.iframe = PhoneConfigurationManager.SSSS;
            this.state = 0;
            this.apnTypeBitmask = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.cid;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.type;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            if (!this.iframe.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(3, this.iframe);
            }
            int i3 = this.state;
            if (i3 != 0) {
                output.writeInt32(4, i3);
            }
            int i4 = this.apnTypeBitmask;
            if (i4 != 0) {
                output.writeInt32(5, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.cid;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.type;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            if (!this.iframe.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.iframe);
            }
            int i3 = this.state;
            if (i3 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i3);
            }
            int i4 = this.apnTypeBitmask;
            if (i4 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(5, i4);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public RilDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.cid = input.readInt32();
                } else if (tag == 16) {
                    int initialPos = input.getPosition();
                    int value = input.readInt32();
                    switch (value) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            this.type = value;
                            continue;
                        default:
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                            continue;
                    }
                } else if (tag == 26) {
                    this.iframe = input.readString();
                } else if (tag == 32) {
                    int initialPos2 = input.getPosition();
                    int value2 = input.readInt32();
                    if (value2 == 0 || value2 == 1 || value2 == 2) {
                        this.state = value2;
                    } else {
                        input.rewindToPosition(initialPos2);
                        storeUnknownField(input, tag);
                    }
                } else if (tag == 40) {
                    this.apnTypeBitmask = input.readInt32();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static RilDataCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (RilDataCall) MessageNano.mergeFrom(new RilDataCall(), data);
        }

        public static RilDataCall parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new RilDataCall().mergeFrom(input);
        }
    }

    public static final class EmergencyNumberInfo extends ExtendableMessageNano<EmergencyNumberInfo> {
        private static volatile EmergencyNumberInfo[] _emptyArray;
        public String address;
        public String countryIso;
        public String mnc;
        public int numberSourcesBitmask;
        public int routing;
        public int serviceCategoriesBitmask;
        public String[] urns;

        public static EmergencyNumberInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new EmergencyNumberInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public EmergencyNumberInfo() {
            clear();
        }

        public EmergencyNumberInfo clear() {
            this.address = PhoneConfigurationManager.SSSS;
            this.countryIso = PhoneConfigurationManager.SSSS;
            this.mnc = PhoneConfigurationManager.SSSS;
            this.serviceCategoriesBitmask = 0;
            this.urns = WireFormatNano.EMPTY_STRING_ARRAY;
            this.numberSourcesBitmask = 0;
            this.routing = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (!this.address.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(1, this.address);
            }
            if (!this.countryIso.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(2, this.countryIso);
            }
            if (!this.mnc.equals(PhoneConfigurationManager.SSSS)) {
                output.writeString(3, this.mnc);
            }
            int i = this.serviceCategoriesBitmask;
            if (i != 0) {
                output.writeInt32(4, i);
            }
            String[] strArr = this.urns;
            if (strArr != null && strArr.length > 0) {
                int i2 = 0;
                while (true) {
                    String[] strArr2 = this.urns;
                    if (i2 >= strArr2.length) {
                        break;
                    }
                    String element = strArr2[i2];
                    if (element != null) {
                        output.writeString(5, element);
                    }
                    i2++;
                }
            }
            int i3 = this.numberSourcesBitmask;
            if (i3 != 0) {
                output.writeInt32(6, i3);
            }
            int i4 = this.routing;
            if (i4 != 0) {
                output.writeInt32(7, i4);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (!this.address.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.address);
            }
            if (!this.countryIso.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.countryIso);
            }
            if (!this.mnc.equals(PhoneConfigurationManager.SSSS)) {
                size += CodedOutputByteBufferNano.computeStringSize(3, this.mnc);
            }
            int i = this.serviceCategoriesBitmask;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, i);
            }
            String[] strArr = this.urns;
            if (strArr != null && strArr.length > 0) {
                int dataCount = 0;
                int dataSize = 0;
                int i2 = 0;
                while (true) {
                    String[] strArr2 = this.urns;
                    if (i2 >= strArr2.length) {
                        break;
                    }
                    String element = strArr2[i2];
                    if (element != null) {
                        dataCount++;
                        dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                    }
                    i2++;
                }
                size = size + dataSize + (dataCount * 1);
            }
            int dataCount2 = this.numberSourcesBitmask;
            if (dataCount2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, dataCount2);
            }
            int i3 = this.routing;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(7, i3);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public EmergencyNumberInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 10) {
                    this.address = input.readString();
                } else if (tag == 18) {
                    this.countryIso = input.readString();
                } else if (tag == 26) {
                    this.mnc = input.readString();
                } else if (tag == 32) {
                    this.serviceCategoriesBitmask = input.readInt32();
                } else if (tag == 42) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                    String[] strArr = this.urns;
                    int i = strArr == null ? 0 : strArr.length;
                    String[] newArray = new String[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.urns, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = input.readString();
                        input.readTag();
                        i++;
                    }
                    newArray[i] = input.readString();
                    this.urns = newArray;
                } else if (tag == 48) {
                    this.numberSourcesBitmask = input.readInt32();
                } else if (tag == 56) {
                    this.routing = input.readInt32();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static EmergencyNumberInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (EmergencyNumberInfo) MessageNano.mergeFrom(new EmergencyNumberInfo(), data);
        }

        public static EmergencyNumberInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new EmergencyNumberInfo().mergeFrom(input);
        }
    }

    public static final class TelephonyEvent extends ExtendableMessageNano<TelephonyEvent> {
        private static volatile TelephonyEvent[] _emptyArray;
        public ActiveSubscriptionInfo activeSubscriptionInfo;
        public CarrierIdMatching carrierIdMatching;
        public CarrierKeyChange carrierKeyChange;
        public RilDataCall[] dataCalls;
        public int dataStallAction;
        public DataSwitch dataSwitch;
        public RilDeactivateDataCall deactivateDataCall;
        public int enabledModemBitmap;
        public int error;
        public ImsCapabilities imsCapabilities;
        public ImsConnectionState imsConnectionState;
        public ModemRestart modemRestart;
        public int networkValidationState;
        public long nitzTimestampMillis;
        public OnDemandDataSwitch onDemandDataSwitch;
        public int phoneId;
        public TelephonyServiceState serviceState;
        public TelephonySettings settings;
        public RilSetupDataCall setupDataCall;
        public RilSetupDataCallResponse setupDataCallResponse;
        public int[] simState;
        public long timestampMillis;
        public int type;
        public EmergencyNumberInfo updatedEmergencyNumber;

        public interface ApnType {
            public static final int APN_TYPE_CBS = 8;
            public static final int APN_TYPE_DEFAULT = 1;
            public static final int APN_TYPE_DUN = 4;
            public static final int APN_TYPE_EMERGENCY = 10;
            public static final int APN_TYPE_FOTA = 6;
            public static final int APN_TYPE_HIPRI = 5;
            public static final int APN_TYPE_IA = 9;
            public static final int APN_TYPE_IMS = 7;
            public static final int APN_TYPE_MMS = 2;
            public static final int APN_TYPE_SUPL = 3;
            public static final int APN_TYPE_UNKNOWN = 0;
        }

        public interface EventState {
            public static final int EVENT_STATE_END = 2;
            public static final int EVENT_STATE_START = 1;
            public static final int EVENT_STATE_UNKNOWN = 0;
        }

        public interface NetworkValidationState {
            public static final int NETWORK_VALIDATION_STATE_AVAILABLE = 1;
            public static final int NETWORK_VALIDATION_STATE_FAILED = 2;
            public static final int NETWORK_VALIDATION_STATE_PASSED = 3;
            public static final int NETWORK_VALIDATION_STATE_UNKNOWN = 0;
        }

        public interface Type {
            public static final int ACTIVE_SUBSCRIPTION_INFO_CHANGED = 19;
            public static final int CARRIER_ID_MATCHING = 13;
            public static final int CARRIER_KEY_CHANGED = 14;
            public static final int DATA_CALL_DEACTIVATE = 8;
            public static final int DATA_CALL_DEACTIVATE_RESPONSE = 9;
            public static final int DATA_CALL_LIST_CHANGED = 7;
            public static final int DATA_CALL_SETUP = 5;
            public static final int DATA_CALL_SETUP_RESPONSE = 6;
            public static final int DATA_STALL_ACTION = 10;
            public static final int DATA_SWITCH = 15;
            public static final int EMERGENCY_NUMBER_REPORT = 21;
            public static final int ENABLED_MODEM_CHANGED = 20;
            public static final int IMS_CAPABILITIES_CHANGED = 4;
            public static final int IMS_CONNECTION_STATE_CHANGED = 3;
            public static final int MODEM_RESTART = 11;
            public static final int NETWORK_VALIDATE = 16;
            public static final int NITZ_TIME = 12;
            public static final int ON_DEMAND_DATA_SWITCH = 17;
            public static final int RIL_SERVICE_STATE_CHANGED = 2;
            public static final int SETTINGS_CHANGED = 1;
            public static final int SIM_STATE_CHANGED = 18;
            public static final int UNKNOWN = 0;
        }

        public static final class DataSwitch extends ExtendableMessageNano<DataSwitch> {
            private static volatile DataSwitch[] _emptyArray;
            public int reason;
            public int state;

            public interface Reason {
                public static final int DATA_SWITCH_REASON_CBRS = 3;
                public static final int DATA_SWITCH_REASON_IN_CALL = 2;
                public static final int DATA_SWITCH_REASON_MANUAL = 1;
                public static final int DATA_SWITCH_REASON_UNKNOWN = 0;
            }

            public static DataSwitch[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new DataSwitch[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public DataSwitch() {
                clear();
            }

            public DataSwitch clear() {
                this.reason = 0;
                this.state = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.reason;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.state;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.reason;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.state;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public DataSwitch mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3) {
                            this.reason = value;
                        } else {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                        }
                    } else if (tag == 16) {
                        int initialPos2 = input.getPosition();
                        int value2 = input.readInt32();
                        if (value2 == 0 || value2 == 1 || value2 == 2) {
                            this.state = value2;
                        } else {
                            input.rewindToPosition(initialPos2);
                            storeUnknownField(input, tag);
                        }
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static DataSwitch parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (DataSwitch) MessageNano.mergeFrom(new DataSwitch(), data);
            }

            public static DataSwitch parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new DataSwitch().mergeFrom(input);
            }
        }

        public static final class OnDemandDataSwitch extends ExtendableMessageNano<OnDemandDataSwitch> {
            private static volatile OnDemandDataSwitch[] _emptyArray;
            public int apn;
            public int state;

            public static OnDemandDataSwitch[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new OnDemandDataSwitch[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public OnDemandDataSwitch() {
                clear();
            }

            public OnDemandDataSwitch clear() {
                this.apn = 0;
                this.state = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.apn;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.state;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.apn;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.state;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public OnDemandDataSwitch mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                                this.apn = value;
                                continue;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    } else if (tag == 16) {
                        int initialPos2 = input.getPosition();
                        int value2 = input.readInt32();
                        if (value2 == 0 || value2 == 1 || value2 == 2) {
                            this.state = value2;
                        } else {
                            input.rewindToPosition(initialPos2);
                            storeUnknownField(input, tag);
                        }
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static OnDemandDataSwitch parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (OnDemandDataSwitch) MessageNano.mergeFrom(new OnDemandDataSwitch(), data);
            }

            public static OnDemandDataSwitch parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new OnDemandDataSwitch().mergeFrom(input);
            }
        }

        public static final class RilSetupDataCall extends ExtendableMessageNano<RilSetupDataCall> {
            private static volatile RilSetupDataCall[] _emptyArray;
            public String apn;
            public int dataProfile;
            public int rat;
            public int type;

            public interface RilDataProfile {
                public static final int RIL_DATA_PROFILE_CBS = 5;
                public static final int RIL_DATA_PROFILE_DEFAULT = 1;
                public static final int RIL_DATA_PROFILE_FOTA = 4;
                public static final int RIL_DATA_PROFILE_IMS = 3;
                public static final int RIL_DATA_PROFILE_INVALID = 7;
                public static final int RIL_DATA_PROFILE_OEM_BASE = 6;
                public static final int RIL_DATA_PROFILE_TETHERED = 2;
                public static final int RIL_DATA_UNKNOWN = 0;
            }

            public static RilSetupDataCall[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RilSetupDataCall[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RilSetupDataCall() {
                clear();
            }

            public RilSetupDataCall clear() {
                this.rat = -1;
                this.dataProfile = 0;
                this.apn = PhoneConfigurationManager.SSSS;
                this.type = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.rat;
                if (i != -1) {
                    output.writeInt32(1, i);
                }
                int i2 = this.dataProfile;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                if (!this.apn.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(3, this.apn);
                }
                int i3 = this.type;
                if (i3 != 0) {
                    output.writeInt32(4, i3);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.rat;
                if (i != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.dataProfile;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                if (!this.apn.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(3, this.apn);
                }
                int i3 = this.type;
                if (i3 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(4, i3);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public RilSetupDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        switch (value) {
                            case -1:
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                                this.rat = value;
                                continue;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    } else if (tag == 16) {
                        int initialPos2 = input.getPosition();
                        int value2 = input.readInt32();
                        switch (value2) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                                this.dataProfile = value2;
                                continue;
                            default:
                                input.rewindToPosition(initialPos2);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    } else if (tag == 26) {
                        this.apn = input.readString();
                    } else if (tag == 32) {
                        int initialPos3 = input.getPosition();
                        int value3 = input.readInt32();
                        switch (value3) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                                this.type = value3;
                                continue;
                            default:
                                input.rewindToPosition(initialPos3);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RilSetupDataCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RilSetupDataCall) MessageNano.mergeFrom(new RilSetupDataCall(), data);
            }

            public static RilSetupDataCall parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RilSetupDataCall().mergeFrom(input);
            }
        }

        public static final class RilSetupDataCallResponse extends ExtendableMessageNano<RilSetupDataCallResponse> {
            private static volatile RilSetupDataCallResponse[] _emptyArray;
            public RilDataCall call;
            public int status;
            public int suggestedRetryTimeMillis;

            public interface RilDataCallFailCause {
                public static final int PDP_FAIL_ACCESS_ATTEMPT_ALREADY_IN_PROGRESS = 2219;
                public static final int PDP_FAIL_ACCESS_BLOCK = 2087;
                public static final int PDP_FAIL_ACCESS_BLOCK_ALL = 2088;
                public static final int PDP_FAIL_ACCESS_CLASS_DSAC_REJECTION = 2108;
                public static final int PDP_FAIL_ACCESS_CONTROL_LIST_CHECK_FAILURE = 2128;
                public static final int PDP_FAIL_ACTIVATION_REJECTED_BCM_VIOLATION = 48;
                public static final int PDP_FAIL_ACTIVATION_REJECT_GGSN = 30;
                public static final int PDP_FAIL_ACTIVATION_REJECT_UNSPECIFIED = 31;
                public static final int PDP_FAIL_APN_DISABLED = 2045;
                public static final int PDP_FAIL_APN_DISALLOWED_ON_ROAMING = 2059;
                public static final int PDP_FAIL_APN_MISMATCH = 2054;
                public static final int PDP_FAIL_APN_PARAMETERS_CHANGED = 2060;
                public static final int PDP_FAIL_APN_PENDING_HANDOVER = 2041;
                public static final int PDP_FAIL_APN_TYPE_CONFLICT = 112;
                public static final int PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL = 122;
                public static final int PDP_FAIL_BEARER_HANDLING_NOT_SUPPORTED = 60;
                public static final int PDP_FAIL_CALL_DISALLOWED_IN_ROAMING = 2068;
                public static final int PDP_FAIL_CALL_PREEMPT_BY_EMERGENCY_APN = 127;
                public static final int PDP_FAIL_CANNOT_ENCODE_OTA_MESSAGE = 2159;
                public static final int PDP_FAIL_CDMA_ALERT_STOP = 2077;
                public static final int PDP_FAIL_CDMA_INCOMING_CALL = 2076;
                public static final int PDP_FAIL_CDMA_INTERCEPT = 2073;
                public static final int PDP_FAIL_CDMA_LOCK = 2072;
                public static final int PDP_FAIL_CDMA_RELEASE_DUE_TO_SO_REJECTION = 2075;
                public static final int PDP_FAIL_CDMA_REORDER = 2074;
                public static final int PDP_FAIL_CDMA_RETRY_ORDER = 2086;
                public static final int PDP_FAIL_CHANNEL_ACQUISITION_FAILURE = 2078;
                public static final int PDP_FAIL_CLOSE_IN_PROGRESS = 2030;
                public static final int PDP_FAIL_COLLISION_WITH_NETWORK_INITIATED_REQUEST = 56;
                public static final int PDP_FAIL_COMPANION_IFACE_IN_USE = 118;
                public static final int PDP_FAIL_CONCURRENT_SERVICES_INCOMPATIBLE = 2083;
                public static final int PDP_FAIL_CONCURRENT_SERVICES_NOT_ALLOWED = 2091;
                public static final int PDP_FAIL_CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION = 2080;
                public static final int PDP_FAIL_CONDITIONAL_IE_ERROR = 100;
                public static final int PDP_FAIL_CONGESTION = 2106;
                public static final int PDP_FAIL_CONNECTION_RELEASED = 2113;
                public static final int PDP_FAIL_CS_DOMAIN_NOT_AVAILABLE = 2181;
                public static final int PDP_FAIL_CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED = 2188;
                public static final int PDP_FAIL_DATA_PLAN_EXPIRED = 2198;
                public static final int PDP_FAIL_DATA_REGISTRATION_FAIL = -2;
                public static final int PDP_FAIL_DATA_ROAMING_SETTINGS_DISABLED = 2064;
                public static final int PDP_FAIL_DATA_SETTINGS_DISABLED = 2063;
                public static final int PDP_FAIL_DBM_OR_SMS_IN_PROGRESS = 2211;
                public static final int PDP_FAIL_DDS_SWITCHED = 2065;
                public static final int PDP_FAIL_DDS_SWITCH_IN_PROGRESS = 2067;
                public static final int PDP_FAIL_DRB_RELEASED_BY_RRC = 2112;
                public static final int PDP_FAIL_DS_EXPLICIT_DEACTIVATION = 2125;
                public static final int PDP_FAIL_DUAL_SWITCH = 2227;
                public static final int PDP_FAIL_DUN_CALL_DISALLOWED = 2056;
                public static final int PDP_FAIL_DUPLICATE_BEARER_ID = 2118;
                public static final int PDP_FAIL_EHRPD_TO_HRPD_FALLBACK = 2049;
                public static final int PDP_FAIL_EMBMS_NOT_ENABLED = 2193;
                public static final int PDP_FAIL_EMBMS_REGULAR_DEACTIVATION = 2195;
                public static final int PDP_FAIL_EMERGENCY_IFACE_ONLY = 116;
                public static final int PDP_FAIL_EMERGENCY_MODE = 2221;
                public static final int PDP_FAIL_EMM_ACCESS_BARRED = 115;
                public static final int PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY = 121;
                public static final int PDP_FAIL_EMM_ATTACH_FAILED = 2115;
                public static final int PDP_FAIL_EMM_ATTACH_STARTED = 2116;
                public static final int PDP_FAIL_EMM_DETACHED = 2114;
                public static final int PDP_FAIL_EMM_T3417_EXPIRED = 2130;
                public static final int PDP_FAIL_EMM_T3417_EXT_EXPIRED = 2131;
                public static final int PDP_FAIL_EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED = 2178;
                public static final int PDP_FAIL_EPS_SERVICES_NOT_ALLOWED_IN_PLMN = 2179;
                public static final int PDP_FAIL_ERROR_UNSPECIFIED = 65535;
                public static final int PDP_FAIL_ESM_BAD_OTA_MESSAGE = 2122;
                public static final int PDP_FAIL_ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK = 2120;
                public static final int PDP_FAIL_ESM_COLLISION_SCENARIOS = 2119;
                public static final int PDP_FAIL_ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT = 2124;
                public static final int PDP_FAIL_ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL = 2123;
                public static final int PDP_FAIL_ESM_FAILURE = 2182;
                public static final int PDP_FAIL_ESM_INFO_NOT_RECEIVED = 53;
                public static final int PDP_FAIL_ESM_LOCAL_CAUSE_NONE = 2126;
                public static final int PDP_FAIL_ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER = 2121;
                public static final int PDP_FAIL_ESM_PROCEDURE_TIME_OUT = 2155;
                public static final int PDP_FAIL_ESM_UNKNOWN_EPS_BEARER_CONTEXT = 2111;
                public static final int PDP_FAIL_EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE = 2201;
                public static final int PDP_FAIL_EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY = 2200;
                public static final int PDP_FAIL_EVDO_HDR_CHANGED = 2202;
                public static final int PDP_FAIL_EVDO_HDR_CONNECTION_SETUP_TIMEOUT = 2206;
                public static final int PDP_FAIL_EVDO_HDR_EXITED = 2203;
                public static final int PDP_FAIL_EVDO_HDR_NO_SESSION = 2204;
                public static final int PDP_FAIL_EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL = 2205;
                public static final int PDP_FAIL_FADE = 2217;
                public static final int PDP_FAIL_FAILED_TO_ACQUIRE_COLOCATED_HDR = 2207;
                public static final int PDP_FAIL_FEATURE_NOT_SUPP = 40;
                public static final int PDP_FAIL_FILTER_SEMANTIC_ERROR = 44;
                public static final int PDP_FAIL_FILTER_SYTAX_ERROR = 45;
                public static final int PDP_FAIL_FORBIDDEN_APN_NAME = 2066;
                public static final int PDP_FAIL_GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED = 2097;
                public static final int PDP_FAIL_GPRS_SERVICES_NOT_ALLOWED = 2098;
                public static final int PDP_FAIL_GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN = 2103;
                public static final int PDP_FAIL_HANDOFF_PREFERENCE_CHANGED = 2251;
                public static final int PDP_FAIL_HDR_ACCESS_FAILURE = 2213;
                public static final int PDP_FAIL_HDR_FADE = 2212;
                public static final int PDP_FAIL_HDR_NO_LOCK_GRANTED = 2210;
                public static final int PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH = 120;
                public static final int PDP_FAIL_IFACE_MISMATCH = 117;
                public static final int PDP_FAIL_ILLEGAL_ME = 2096;
                public static final int PDP_FAIL_ILLEGAL_MS = 2095;
                public static final int PDP_FAIL_IMEI_NOT_ACCEPTED = 2177;
                public static final int PDP_FAIL_IMPLICITLY_DETACHED = 2100;
                public static final int PDP_FAIL_IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER = 2176;
                public static final int PDP_FAIL_INCOMING_CALL_REJECTED = 2092;
                public static final int PDP_FAIL_INSUFFICIENT_RESOURCES = 26;
                public static final int PDP_FAIL_INTERFACE_IN_USE = 2058;
                public static final int PDP_FAIL_INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN = 114;
                public static final int PDP_FAIL_INTERNAL_EPC_NONEPC_TRANSITION = 2057;
                public static final int PDP_FAIL_INVALID_CONNECTION_ID = 2156;
                public static final int PDP_FAIL_INVALID_DNS_ADDR = 123;
                public static final int PDP_FAIL_INVALID_EMM_STATE = 2190;
                public static final int PDP_FAIL_INVALID_MANDATORY_INFO = 96;
                public static final int PDP_FAIL_INVALID_MODE = 2223;
                public static final int PDP_FAIL_INVALID_PCSCF_ADDR = 113;
                public static final int PDP_FAIL_INVALID_PCSCF_OR_DNS_ADDRESS = 124;
                public static final int PDP_FAIL_INVALID_PRIMARY_NSAPI = 2158;
                public static final int PDP_FAIL_INVALID_SIM_STATE = 2224;
                public static final int PDP_FAIL_INVALID_TRANSACTION_ID = 81;
                public static final int PDP_FAIL_IPV6_ADDRESS_TRANSFER_FAILED = 2047;
                public static final int PDP_FAIL_IPV6_PREFIX_UNAVAILABLE = 2250;
                public static final int PDP_FAIL_IP_ADDRESS_MISMATCH = 119;
                public static final int PDP_FAIL_IP_VERSION_MISMATCH = 2055;
                public static final int PDP_FAIL_IRAT_HANDOVER_FAILED = 2194;
                public static final int PDP_FAIL_IS707B_MAX_ACCESS_PROBES = 2089;
                public static final int PDP_FAIL_LIMITED_TO_IPV4 = 2234;
                public static final int PDP_FAIL_LIMITED_TO_IPV6 = 2235;
                public static final int PDP_FAIL_LLC_SNDCP = 25;
                public static final int PDP_FAIL_LOCAL_END = 2215;
                public static final int PDP_FAIL_LOCATION_AREA_NOT_ALLOWED = 2102;
                public static final int PDP_FAIL_LOWER_LAYER_REGISTRATION_FAILURE = 2197;
                public static final int PDP_FAIL_LOW_POWER_MODE_OR_POWERING_DOWN = 2044;
                public static final int PDP_FAIL_LTE_NAS_SERVICE_REQUEST_FAILED = 2117;
                public static final int PDP_FAIL_LTE_THROTTLING_NOT_REQUIRED = 2127;
                public static final int PDP_FAIL_MAC_FAILURE = 2183;
                public static final int PDP_FAIL_MAXIMIUM_NSAPIS_EXCEEDED = 2157;
                public static final int PDP_FAIL_MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED = 2166;
                public static final int PDP_FAIL_MAX_ACCESS_PROBE = 2079;
                public static final int PDP_FAIL_MAX_ACTIVE_PDP_CONTEXT_REACHED = 65;
                public static final int PDP_FAIL_MAX_IPV4_CONNECTIONS = 2052;
                public static final int PDP_FAIL_MAX_IPV6_CONNECTIONS = 2053;
                public static final int PDP_FAIL_MAX_PPP_INACTIVITY_TIMER_EXPIRED = 2046;
                public static final int PDP_FAIL_MESSAGE_INCORRECT_SEMANTIC = 95;
                public static final int PDP_FAIL_MESSAGE_TYPE_UNSUPPORTED = 97;
                public static final int PDP_FAIL_MIP_CONFIG_FAILURE = 2050;
                public static final int PDP_FAIL_MIP_FA_ADMIN_PROHIBITED = 2001;
                public static final int PDP_FAIL_MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED = 2012;
                public static final int PDP_FAIL_MIP_FA_ENCAPSULATION_UNAVAILABLE = 2008;
                public static final int PDP_FAIL_MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE = 2004;
                public static final int PDP_FAIL_MIP_FA_INSUFFICIENT_RESOURCES = 2002;
                public static final int PDP_FAIL_MIP_FA_MALFORMED_REPLY = 2007;
                public static final int PDP_FAIL_MIP_FA_MALFORMED_REQUEST = 2006;
                public static final int PDP_FAIL_MIP_FA_MISSING_CHALLENGE = 2017;
                public static final int PDP_FAIL_MIP_FA_MISSING_HOME_ADDRESS = 2015;
                public static final int PDP_FAIL_MIP_FA_MISSING_HOME_AGENT = 2014;
                public static final int PDP_FAIL_MIP_FA_MISSING_NAI = 2013;
                public static final int PDP_FAIL_MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE = 2003;
                public static final int PDP_FAIL_MIP_FA_REASON_UNSPECIFIED = 2000;
                public static final int PDP_FAIL_MIP_FA_REQUESTED_LIFETIME_TOO_LONG = 2005;
                public static final int PDP_FAIL_MIP_FA_REVERSE_TUNNEL_IS_MANDATORY = 2011;
                public static final int PDP_FAIL_MIP_FA_REVERSE_TUNNEL_UNAVAILABLE = 2010;
                public static final int PDP_FAIL_MIP_FA_STALE_CHALLENGE = 2018;
                public static final int PDP_FAIL_MIP_FA_UNKNOWN_CHALLENGE = 2016;
                public static final int PDP_FAIL_MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE = 2009;
                public static final int PDP_FAIL_MIP_HA_ADMIN_PROHIBITED = 2020;
                public static final int PDP_FAIL_MIP_HA_ENCAPSULATION_UNAVAILABLE = 2029;
                public static final int PDP_FAIL_MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE = 2023;
                public static final int PDP_FAIL_MIP_HA_INSUFFICIENT_RESOURCES = 2021;
                public static final int PDP_FAIL_MIP_HA_MALFORMED_REQUEST = 2025;
                public static final int PDP_FAIL_MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE = 2022;
                public static final int PDP_FAIL_MIP_HA_REASON_UNSPECIFIED = 2019;
                public static final int PDP_FAIL_MIP_HA_REGISTRATION_ID_MISMATCH = 2024;
                public static final int PDP_FAIL_MIP_HA_REVERSE_TUNNEL_IS_MANDATORY = 2028;
                public static final int PDP_FAIL_MIP_HA_REVERSE_TUNNEL_UNAVAILABLE = 2027;
                public static final int PDP_FAIL_MIP_HA_UNKNOWN_HOME_AGENT_ADDRESS = 2026;
                public static final int PDP_FAIL_MISSING_UKNOWN_APN = 27;
                public static final int PDP_FAIL_MODEM_APP_PREEMPTED = 2032;
                public static final int PDP_FAIL_MODEM_RESTART = 2037;
                public static final int PDP_FAIL_MSC_TEMPORARILY_NOT_REACHABLE = 2180;
                public static final int PDP_FAIL_MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE = 101;
                public static final int PDP_FAIL_MSG_TYPE_NONCOMPATIBLE_STATE = 98;
                public static final int PDP_FAIL_MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK = 2099;
                public static final int PDP_FAIL_MULTIPLE_PDP_CALL_NOT_ALLOWED = 2192;
                public static final int PDP_FAIL_MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED = 55;
                public static final int PDP_FAIL_NAS_LAYER_FAILURE = 2191;
                public static final int PDP_FAIL_NAS_REQUEST_REJECTED_BY_NETWORK = 2167;
                public static final int PDP_FAIL_NAS_SIGNALLING = 14;
                public static final int PDP_FAIL_NETWORK_FAILURE = 38;
                public static final int PDP_FAIL_NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH = 2154;
                public static final int PDP_FAIL_NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH = 2153;
                public static final int PDP_FAIL_NETWORK_INITIATED_TERMINATION = 2031;
                public static final int PDP_FAIL_NONE = 1;
                public static final int PDP_FAIL_NON_IP_NOT_SUPPORTED = 2069;
                public static final int PDP_FAIL_NORMAL_RELEASE = 2218;
                public static final int PDP_FAIL_NO_CDMA_SERVICE = 2084;
                public static final int PDP_FAIL_NO_COLLOCATED_HDR = 2225;
                public static final int PDP_FAIL_NO_EPS_BEARER_CONTEXT_ACTIVATED = 2189;
                public static final int PDP_FAIL_NO_GPRS_CONTEXT = 2094;
                public static final int PDP_FAIL_NO_HYBRID_HDR_SERVICE = 2209;
                public static final int PDP_FAIL_NO_PDP_CONTEXT_ACTIVATED = 2107;
                public static final int PDP_FAIL_NO_RESPONSE_FROM_BASE_STATION = 2081;
                public static final int PDP_FAIL_NO_SERVICE = 2216;
                public static final int PDP_FAIL_NO_SERVICE_ON_GATEWAY = 2093;
                public static final int PDP_FAIL_NSAPI_IN_USE = 35;
                public static final int PDP_FAIL_NULL_APN_DISALLOWED = 2061;
                public static final int PDP_FAIL_ONLY_IPV4V6_ALLOWED = 57;
                public static final int PDP_FAIL_ONLY_IPV4_ALLOWED = 50;
                public static final int PDP_FAIL_ONLY_IPV6_ALLOWED = 51;
                public static final int PDP_FAIL_ONLY_NON_IP_ALLOWED = 58;
                public static final int PDP_FAIL_ONLY_SINGLE_BEARER_ALLOWED = 52;
                public static final int PDP_FAIL_OPERATOR_BARRED = 8;
                public static final int PDP_FAIL_OTASP_COMMIT_IN_PROGRESS = 2208;
                public static final int PDP_FAIL_PDN_CONN_DOES_NOT_EXIST = 54;
                public static final int PDP_FAIL_PDN_INACTIVITY_TIMER_EXPIRED = 2051;
                public static final int PDP_FAIL_PDN_IPV4_CALL_DISALLOWED = 2033;
                public static final int PDP_FAIL_PDN_IPV4_CALL_THROTTLED = 2034;
                public static final int PDP_FAIL_PDN_IPV6_CALL_DISALLOWED = 2035;
                public static final int PDP_FAIL_PDN_IPV6_CALL_THROTTLED = 2036;
                public static final int PDP_FAIL_PDN_NON_IP_CALL_DISALLOWED = 2071;
                public static final int PDP_FAIL_PDN_NON_IP_CALL_THROTTLED = 2070;
                public static final int PDP_FAIL_PDP_ACTIVATE_MAX_RETRY_FAILED = 2109;
                public static final int PDP_FAIL_PDP_DUPLICATE = 2104;
                public static final int PDP_FAIL_PDP_ESTABLISH_TIMEOUT_EXPIRED = 2161;
                public static final int PDP_FAIL_PDP_INACTIVE_TIMEOUT_EXPIRED = 2163;
                public static final int PDP_FAIL_PDP_LOWERLAYER_ERROR = 2164;
                public static final int PDP_FAIL_PDP_MODIFY_COLLISION = 2165;
                public static final int PDP_FAIL_PDP_MODIFY_TIMEOUT_EXPIRED = 2162;
                public static final int PDP_FAIL_PDP_PPP_NOT_SUPPORTED = 2038;
                public static final int PDP_FAIL_PDP_WITHOUT_ACTIVE_TFT = 46;
                public static final int PDP_FAIL_PHONE_IN_USE = 2222;
                public static final int PDP_FAIL_PHYSICAL_LINK_CLOSE_IN_PROGRESS = 2040;
                public static final int PDP_FAIL_PLMN_NOT_ALLOWED = 2101;
                public static final int PDP_FAIL_PPP_AUTH_FAILURE = 2229;
                public static final int PDP_FAIL_PPP_CHAP_FAILURE = 2232;
                public static final int PDP_FAIL_PPP_CLOSE_IN_PROGRESS = 2233;
                public static final int PDP_FAIL_PPP_OPTION_MISMATCH = 2230;
                public static final int PDP_FAIL_PPP_PAP_FAILURE = 2231;
                public static final int PDP_FAIL_PPP_TIMEOUT = 2228;
                public static final int PDP_FAIL_PREF_RADIO_TECH_CHANGED = -4;
                public static final int PDP_FAIL_PROFILE_BEARER_INCOMPATIBLE = 2042;
                public static final int PDP_FAIL_PROTOCOL_ERRORS = 111;
                public static final int PDP_FAIL_QOS_NOT_ACCEPTED = 37;
                public static final int PDP_FAIL_RADIO_ACCESS_BEARER_FAILURE = 2110;
                public static final int PDP_FAIL_RADIO_ACCESS_BEARER_SETUP_FAILURE = 2160;
                public static final int PDP_FAIL_RADIO_POWER_OFF = -5;
                public static final int PDP_FAIL_REDIRECTION_OR_HANDOFF_IN_PROGRESS = 2220;
                public static final int PDP_FAIL_REGULAR_DEACTIVATION = 36;
                public static final int PDP_FAIL_REJECTED_BY_BASE_STATION = 2082;
                public static final int PDP_FAIL_RRC_CONNECTION_ABORTED_AFTER_HANDOVER = 2173;
                public static final int PDP_FAIL_RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE = 2174;
                public static final int PDP_FAIL_RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE = 2171;
                public static final int PDP_FAIL_RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE = 2175;
                public static final int PDP_FAIL_RRC_CONNECTION_ABORT_REQUEST = 2151;
                public static final int PDP_FAIL_RRC_CONNECTION_ACCESS_BARRED = 2139;
                public static final int PDP_FAIL_RRC_CONNECTION_ACCESS_STRATUM_FAILURE = 2137;
                public static final int PDP_FAIL_RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS = 2138;
                public static final int PDP_FAIL_RRC_CONNECTION_CELL_NOT_CAMPED = 2144;
                public static final int PDP_FAIL_RRC_CONNECTION_CELL_RESELECTION = 2140;
                public static final int PDP_FAIL_RRC_CONNECTION_CONFIG_FAILURE = 2141;
                public static final int PDP_FAIL_RRC_CONNECTION_INVALID_REQUEST = 2168;
                public static final int PDP_FAIL_RRC_CONNECTION_LINK_FAILURE = 2143;
                public static final int PDP_FAIL_RRC_CONNECTION_NORMAL_RELEASE = 2147;
                public static final int PDP_FAIL_RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER = 2150;
                public static final int PDP_FAIL_RRC_CONNECTION_RADIO_LINK_FAILURE = 2148;
                public static final int PDP_FAIL_RRC_CONNECTION_REESTABLISHMENT_FAILURE = 2149;
                public static final int PDP_FAIL_RRC_CONNECTION_REJECT_BY_NETWORK = 2146;
                public static final int PDP_FAIL_RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE = 2172;
                public static final int PDP_FAIL_RRC_CONNECTION_RF_UNAVAILABLE = 2170;
                public static final int PDP_FAIL_RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR = 2152;
                public static final int PDP_FAIL_RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE = 2145;
                public static final int PDP_FAIL_RRC_CONNECTION_TIMER_EXPIRED = 2142;
                public static final int PDP_FAIL_RRC_CONNECTION_TRACKING_AREA_ID_CHANGED = 2169;
                public static final int PDP_FAIL_RRC_UPLINK_CONNECTION_RELEASE = 2134;
                public static final int PDP_FAIL_RRC_UPLINK_DATA_TRANSMISSION_FAILURE = 2132;
                public static final int PDP_FAIL_RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER = 2133;
                public static final int PDP_FAIL_RRC_UPLINK_ERROR_REQUEST_FROM_NAS = 2136;
                public static final int PDP_FAIL_RRC_UPLINK_RADIO_LINK_FAILURE = 2135;
                public static final int PDP_FAIL_RUIM_NOT_PRESENT = 2085;
                public static final int PDP_FAIL_SECURITY_MODE_REJECTED = 2186;
                public static final int PDP_FAIL_SERVICE_NOT_ALLOWED_ON_PLMN = 2129;
                public static final int PDP_FAIL_SERVICE_OPTION_NOT_SUBSCRIBED = 33;
                public static final int PDP_FAIL_SERVICE_OPTION_NOT_SUPPORTED = 32;
                public static final int PDP_FAIL_SERVICE_OPTION_OUT_OF_ORDER = 34;
                public static final int PDP_FAIL_SIGNAL_LOST = -3;
                public static final int PDP_FAIL_SIM_CARD_CHANGED = 2043;
                public static final int PDP_FAIL_SYNCHRONIZATION_FAILURE = 2184;
                public static final int PDP_FAIL_TEST_LOOPBACK_REGULAR_DEACTIVATION = 2196;
                public static final int PDP_FAIL_TETHERED_CALL_ACTIVE = -6;
                public static final int PDP_FAIL_TFT_SEMANTIC_ERROR = 41;
                public static final int PDP_FAIL_TFT_SYTAX_ERROR = 42;
                public static final int PDP_FAIL_THERMAL_EMERGENCY = 2090;
                public static final int PDP_FAIL_THERMAL_MITIGATION = 2062;
                public static final int PDP_FAIL_TRAT_SWAP_FAILED = 2048;
                public static final int PDP_FAIL_UE_INITIATED_DETACH_OR_DISCONNECT = 128;
                public static final int PDP_FAIL_UE_IS_ENTERING_POWERSAVE_MODE = 2226;
                public static final int PDP_FAIL_UE_RAT_CHANGE = 2105;
                public static final int PDP_FAIL_UE_SECURITY_CAPABILITIES_MISMATCH = 2185;
                public static final int PDP_FAIL_UMTS_HANDOVER_TO_IWLAN = 2199;
                public static final int PDP_FAIL_UMTS_REACTIVATION_REQ = 39;
                public static final int PDP_FAIL_UNACCEPTABLE_NON_EPS_AUTHENTICATION = 2187;
                public static final int PDP_FAIL_UNKNOWN = 0;
                public static final int PDP_FAIL_UNKNOWN_INFO_ELEMENT = 99;
                public static final int PDP_FAIL_UNKNOWN_PDP_ADDRESS_TYPE = 28;
                public static final int PDP_FAIL_UNKNOWN_PDP_CONTEXT = 43;
                public static final int PDP_FAIL_UNPREFERRED_RAT = 2039;
                public static final int PDP_FAIL_UNSUPPORTED_1X_PREV = 2214;
                public static final int PDP_FAIL_UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
                public static final int PDP_FAIL_UNSUPPORTED_QCI_VALUE = 59;
                public static final int PDP_FAIL_USER_AUTHENTICATION = 29;
                public static final int PDP_FAIL_VOICE_REGISTRATION_FAIL = -1;
                public static final int PDP_FAIL_VSNCP_ADMINISTRATIVELY_PROHIBITED = 2245;
                public static final int PDP_FAIL_VSNCP_APN_UNATHORIZED = 2238;
                public static final int PDP_FAIL_VSNCP_GEN_ERROR = 2237;
                public static final int PDP_FAIL_VSNCP_INSUFFICIENT_PARAMETERS = 2243;
                public static final int PDP_FAIL_VSNCP_NO_PDN_GATEWAY_ADDRESS = 2240;
                public static final int PDP_FAIL_VSNCP_PDN_EXISTS_FOR_THIS_APN = 2248;
                public static final int PDP_FAIL_VSNCP_PDN_GATEWAY_REJECT = 2242;
                public static final int PDP_FAIL_VSNCP_PDN_GATEWAY_UNREACHABLE = 2241;
                public static final int PDP_FAIL_VSNCP_PDN_ID_IN_USE = 2246;
                public static final int PDP_FAIL_VSNCP_PDN_LIMIT_EXCEEDED = 2239;
                public static final int PDP_FAIL_VSNCP_RECONNECT_NOT_ALLOWED = 2249;
                public static final int PDP_FAIL_VSNCP_RESOURCE_UNAVAILABLE = 2244;
                public static final int PDP_FAIL_VSNCP_SUBSCRIBER_LIMITATION = 2247;
                public static final int PDP_FAIL_VSNCP_TIMEOUT = 2236;
            }

            public static RilSetupDataCallResponse[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RilSetupDataCallResponse[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RilSetupDataCallResponse() {
                clear();
            }

            public RilSetupDataCallResponse clear() {
                this.status = 0;
                this.suggestedRetryTimeMillis = 0;
                this.call = null;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.status;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.suggestedRetryTimeMillis;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                RilDataCall rilDataCall = this.call;
                if (rilDataCall != null) {
                    output.writeMessage(3, rilDataCall);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.status;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.suggestedRetryTimeMillis;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                RilDataCall rilDataCall = this.call;
                if (rilDataCall != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(3, rilDataCall);
                }
                return size;
            }

            /* JADX WARNING: Removed duplicated region for block: B:41:0x0075 A[FALL_THROUGH] */
            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public RilSetupDataCallResponse mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        if (!(value == 8 || value == 14 || value == 48 || value == 81 || value == 65535 || value == 65 || value == 66 || value == 127 || value == 128)) {
                            switch (value) {
                                default:
                                    switch (value) {
                                        default:
                                            switch (value) {
                                                default:
                                                    switch (value) {
                                                        default:
                                                            switch (value) {
                                                                default:
                                                                    switch (value) {
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_REASON_UNSPECIFIED /* 2000 */:
                                                                        case 2001:
                                                                        case 2002:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE /* 2003 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE /* 2004 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_REQUESTED_LIFETIME_TOO_LONG /* 2005 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MALFORMED_REQUEST /* 2006 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MALFORMED_REPLY /* 2007 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_ENCAPSULATION_UNAVAILABLE /* 2008 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE /* 2009 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_REVERSE_TUNNEL_UNAVAILABLE /* 2010 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_REVERSE_TUNNEL_IS_MANDATORY /* 2011 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED /* 2012 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MISSING_NAI /* 2013 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MISSING_HOME_AGENT /* 2014 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MISSING_HOME_ADDRESS /* 2015 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_UNKNOWN_CHALLENGE /* 2016 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_MISSING_CHALLENGE /* 2017 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_FA_STALE_CHALLENGE /* 2018 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_REASON_UNSPECIFIED /* 2019 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_ADMIN_PROHIBITED /* 2020 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_INSUFFICIENT_RESOURCES /* 2021 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE /* 2022 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE /* 2023 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_REGISTRATION_ID_MISMATCH /* 2024 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_MALFORMED_REQUEST /* 2025 */:
                                                                        case 2026:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_REVERSE_TUNNEL_UNAVAILABLE /* 2027 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_REVERSE_TUNNEL_IS_MANDATORY /* 2028 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_HA_ENCAPSULATION_UNAVAILABLE /* 2029 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CLOSE_IN_PROGRESS /* 2030 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NETWORK_INITIATED_TERMINATION /* 2031 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MODEM_APP_PREEMPTED /* 2032 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_IPV4_CALL_DISALLOWED /* 2033 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_IPV4_CALL_THROTTLED /* 2034 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_IPV6_CALL_DISALLOWED /* 2035 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_IPV6_CALL_THROTTLED /* 2036 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MODEM_RESTART /* 2037 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_PPP_NOT_SUPPORTED /* 2038 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UNPREFERRED_RAT /* 2039 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PHYSICAL_LINK_CLOSE_IN_PROGRESS /* 2040 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_APN_PENDING_HANDOVER /* 2041 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PROFILE_BEARER_INCOMPATIBLE /* 2042 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_SIM_CARD_CHANGED /* 2043 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LOW_POWER_MODE_OR_POWERING_DOWN /* 2044 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_APN_DISABLED /* 2045 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAX_PPP_INACTIVITY_TIMER_EXPIRED /* 2046 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IPV6_ADDRESS_TRANSFER_FAILED /* 2047 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_TRAT_SWAP_FAILED /* 2048 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EHRPD_TO_HRPD_FALLBACK /* 2049 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MIP_CONFIG_FAILURE /* 2050 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_INACTIVITY_TIMER_EXPIRED /* 2051 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAX_IPV4_CONNECTIONS /* 2052 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAX_IPV6_CONNECTIONS /* 2053 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_APN_MISMATCH /* 2054 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IP_VERSION_MISMATCH /* 2055 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DUN_CALL_DISALLOWED /* 2056 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INTERNAL_EPC_NONEPC_TRANSITION /* 2057 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INTERFACE_IN_USE /* 2058 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_APN_DISALLOWED_ON_ROAMING /* 2059 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_APN_PARAMETERS_CHANGED /* 2060 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NULL_APN_DISALLOWED /* 2061 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_THERMAL_MITIGATION /* 2062 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DATA_SETTINGS_DISABLED /* 2063 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DATA_ROAMING_SETTINGS_DISABLED /* 2064 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DDS_SWITCHED /* 2065 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_FORBIDDEN_APN_NAME /* 2066 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DDS_SWITCH_IN_PROGRESS /* 2067 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CALL_DISALLOWED_IN_ROAMING /* 2068 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NON_IP_NOT_SUPPORTED /* 2069 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_NON_IP_CALL_THROTTLED /* 2070 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDN_NON_IP_CALL_DISALLOWED /* 2071 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_LOCK /* 2072 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_INTERCEPT /* 2073 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_REORDER /* 2074 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_RELEASE_DUE_TO_SO_REJECTION /* 2075 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_INCOMING_CALL /* 2076 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_ALERT_STOP /* 2077 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CHANNEL_ACQUISITION_FAILURE /* 2078 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAX_ACCESS_PROBE /* 2079 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION /* 2080 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_RESPONSE_FROM_BASE_STATION /* 2081 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_REJECTED_BY_BASE_STATION /* 2082 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CONCURRENT_SERVICES_INCOMPATIBLE /* 2083 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_CDMA_SERVICE /* 2084 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RUIM_NOT_PRESENT /* 2085 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CDMA_RETRY_ORDER /* 2086 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ACCESS_BLOCK /* 2087 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ACCESS_BLOCK_ALL /* 2088 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IS707B_MAX_ACCESS_PROBES /* 2089 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_THERMAL_EMERGENCY /* 2090 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CONCURRENT_SERVICES_NOT_ALLOWED /* 2091 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INCOMING_CALL_REJECTED /* 2092 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_SERVICE_ON_GATEWAY /* 2093 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_GPRS_CONTEXT /* 2094 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ILLEGAL_MS /* 2095 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ILLEGAL_ME /* 2096 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED /* 2097 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_GPRS_SERVICES_NOT_ALLOWED /* 2098 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK /* 2099 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IMPLICITLY_DETACHED /* 2100 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PLMN_NOT_ALLOWED /* 2101 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LOCATION_AREA_NOT_ALLOWED /* 2102 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN /* 2103 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_DUPLICATE /* 2104 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UE_RAT_CHANGE /* 2105 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CONGESTION /* 2106 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_PDP_CONTEXT_ACTIVATED /* 2107 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ACCESS_CLASS_DSAC_REJECTION /* 2108 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_ACTIVATE_MAX_RETRY_FAILED /* 2109 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RADIO_ACCESS_BEARER_FAILURE /* 2110 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_UNKNOWN_EPS_BEARER_CONTEXT /* 2111 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DRB_RELEASED_BY_RRC /* 2112 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CONNECTION_RELEASED /* 2113 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMM_DETACHED /* 2114 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMM_ATTACH_FAILED /* 2115 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMM_ATTACH_STARTED /* 2116 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LTE_NAS_SERVICE_REQUEST_FAILED /* 2117 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DUPLICATE_BEARER_ID /* 2118 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_COLLISION_SCENARIOS /* 2119 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK /* 2120 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER /* 2121 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_BAD_OTA_MESSAGE /* 2122 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL /* 2123 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT /* 2124 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DS_EXPLICIT_DEACTIVATION /* 2125 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_LOCAL_CAUSE_NONE /* 2126 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LTE_THROTTLING_NOT_REQUIRED /* 2127 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ACCESS_CONTROL_LIST_CHECK_FAILURE /* 2128 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_SERVICE_NOT_ALLOWED_ON_PLMN /* 2129 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMM_T3417_EXPIRED /* 2130 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMM_T3417_EXT_EXPIRED /* 2131 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_UPLINK_DATA_TRANSMISSION_FAILURE /* 2132 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER /* 2133 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_UPLINK_CONNECTION_RELEASE /* 2134 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_UPLINK_RADIO_LINK_FAILURE /* 2135 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_UPLINK_ERROR_REQUEST_FROM_NAS /* 2136 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ACCESS_STRATUM_FAILURE /* 2137 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS /* 2138 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ACCESS_BARRED /* 2139 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_CELL_RESELECTION /* 2140 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_CONFIG_FAILURE /* 2141 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_TIMER_EXPIRED /* 2142 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_LINK_FAILURE /* 2143 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_CELL_NOT_CAMPED /* 2144 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE /* 2145 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_REJECT_BY_NETWORK /* 2146 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_NORMAL_RELEASE /* 2147 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_RADIO_LINK_FAILURE /* 2148 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_REESTABLISHMENT_FAILURE /* 2149 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER /* 2150 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ABORT_REQUEST /* 2151 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR /* 2152 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH /* 2153 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH /* 2154 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_PROCEDURE_TIME_OUT /* 2155 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INVALID_CONNECTION_ID /* 2156 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAXIMIUM_NSAPIS_EXCEEDED /* 2157 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INVALID_PRIMARY_NSAPI /* 2158 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CANNOT_ENCODE_OTA_MESSAGE /* 2159 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RADIO_ACCESS_BEARER_SETUP_FAILURE /* 2160 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_ESTABLISH_TIMEOUT_EXPIRED /* 2161 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_MODIFY_TIMEOUT_EXPIRED /* 2162 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_INACTIVE_TIMEOUT_EXPIRED /* 2163 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_LOWERLAYER_ERROR /* 2164 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PDP_MODIFY_COLLISION /* 2165 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED /* 2166 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NAS_REQUEST_REJECTED_BY_NETWORK /* 2167 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_INVALID_REQUEST /* 2168 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_TRACKING_AREA_ID_CHANGED /* 2169 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_RF_UNAVAILABLE /* 2170 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE /* 2171 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE /* 2172 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ABORTED_AFTER_HANDOVER /* 2173 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE /* 2174 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE /* 2175 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER /* 2176 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IMEI_NOT_ACCEPTED /* 2177 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED /* 2178 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EPS_SERVICES_NOT_ALLOWED_IN_PLMN /* 2179 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MSC_TEMPORARILY_NOT_REACHABLE /* 2180 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CS_DOMAIN_NOT_AVAILABLE /* 2181 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ESM_FAILURE /* 2182 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MAC_FAILURE /* 2183 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_SYNCHRONIZATION_FAILURE /* 2184 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UE_SECURITY_CAPABILITIES_MISMATCH /* 2185 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_SECURITY_MODE_REJECTED /* 2186 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UNACCEPTABLE_NON_EPS_AUTHENTICATION /* 2187 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED /* 2188 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_EPS_BEARER_CONTEXT_ACTIVATED /* 2189 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INVALID_EMM_STATE /* 2190 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NAS_LAYER_FAILURE /* 2191 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_MULTIPLE_PDP_CALL_NOT_ALLOWED /* 2192 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMBMS_NOT_ENABLED /* 2193 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IRAT_HANDOVER_FAILED /* 2194 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMBMS_REGULAR_DEACTIVATION /* 2195 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_TEST_LOOPBACK_REGULAR_DEACTIVATION /* 2196 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LOWER_LAYER_REGISTRATION_FAILURE /* 2197 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DATA_PLAN_EXPIRED /* 2198 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UMTS_HANDOVER_TO_IWLAN /* 2199 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY /* 2200 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE /* 2201 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_HDR_CHANGED /* 2202 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_HDR_EXITED /* 2203 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_HDR_NO_SESSION /* 2204 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL /* 2205 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EVDO_HDR_CONNECTION_SETUP_TIMEOUT /* 2206 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_FAILED_TO_ACQUIRE_COLOCATED_HDR /* 2207 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_OTASP_COMMIT_IN_PROGRESS /* 2208 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_HYBRID_HDR_SERVICE /* 2209 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_HDR_NO_LOCK_GRANTED /* 2210 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DBM_OR_SMS_IN_PROGRESS /* 2211 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_HDR_FADE /* 2212 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_HDR_ACCESS_FAILURE /* 2213 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UNSUPPORTED_1X_PREV /* 2214 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LOCAL_END /* 2215 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_SERVICE /* 2216 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_FADE /* 2217 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NORMAL_RELEASE /* 2218 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_ACCESS_ATTEMPT_ALREADY_IN_PROGRESS /* 2219 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_REDIRECTION_OR_HANDOFF_IN_PROGRESS /* 2220 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_EMERGENCY_MODE /* 2221 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PHONE_IN_USE /* 2222 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INVALID_MODE /* 2223 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_INVALID_SIM_STATE /* 2224 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_NO_COLLOCATED_HDR /* 2225 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_UE_IS_ENTERING_POWERSAVE_MODE /* 2226 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_DUAL_SWITCH /* 2227 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_TIMEOUT /* 2228 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_AUTH_FAILURE /* 2229 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_OPTION_MISMATCH /* 2230 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_PAP_FAILURE /* 2231 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_CHAP_FAILURE /* 2232 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_PPP_CLOSE_IN_PROGRESS /* 2233 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LIMITED_TO_IPV4 /* 2234 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_LIMITED_TO_IPV6 /* 2235 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_TIMEOUT /* 2236 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_GEN_ERROR /* 2237 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_APN_UNATHORIZED /* 2238 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_PDN_LIMIT_EXCEEDED /* 2239 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_NO_PDN_GATEWAY_ADDRESS /* 2240 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_PDN_GATEWAY_UNREACHABLE /* 2241 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_PDN_GATEWAY_REJECT /* 2242 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_INSUFFICIENT_PARAMETERS /* 2243 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_RESOURCE_UNAVAILABLE /* 2244 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_ADMINISTRATIVELY_PROHIBITED /* 2245 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_PDN_ID_IN_USE /* 2246 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_SUBSCRIBER_LIMITATION /* 2247 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_PDN_EXISTS_FOR_THIS_APN /* 2248 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_VSNCP_RECONNECT_NOT_ALLOWED /* 2249 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_IPV6_PREFIX_UNAVAILABLE /* 2250 */:
                                                                        case RilDataCallFailCause.PDP_FAIL_HANDOFF_PREFERENCE_CHANGED /* 2251 */:
                                                                            break;
                                                                        default:
                                                                            input.rewindToPosition(initialPos);
                                                                            storeUnknownField(input, tag);
                                                                            break;
                                                                    }
                                                                case 111:
                                                                case 112:
                                                                case 113:
                                                                case 114:
                                                                case 115:
                                                                case 116:
                                                                case 117:
                                                                case 118:
                                                                case RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH /* 119 */:
                                                                case 120:
                                                                case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /* 121 */:
                                                                case RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /* 122 */:
                                                                case RilDataCallFailCause.PDP_FAIL_INVALID_DNS_ADDR /* 123 */:
                                                                case RilDataCallFailCause.PDP_FAIL_INVALID_PCSCF_OR_DNS_ADDRESS /* 124 */:
                                                                    this.status = value;
                                                                    break;
                                                            }
                                                        case 95:
                                                        case 96:
                                                        case 97:
                                                        case 98:
                                                        case 99:
                                                        case 100:
                                                        case 101:
                                                            break;
                                                    }
                                                case 50:
                                                case 51:
                                                case 52:
                                                case 53:
                                                case 54:
                                                case 55:
                                                case 56:
                                                case 57:
                                                case 58:
                                                case 59:
                                                case 60:
                                                    break;
                                            }
                                        case 25:
                                        case 26:
                                        case 27:
                                        case 28:
                                        case 29:
                                        case 30:
                                        case 31:
                                        case 32:
                                        case 33:
                                        case 34:
                                        case 35:
                                        case 36:
                                        case 37:
                                        case 38:
                                        case 39:
                                        case 40:
                                        case 41:
                                        case 42:
                                        case 43:
                                        case 44:
                                        case 45:
                                        case 46:
                                            break;
                                    }
                                case RilDataCallFailCause.PDP_FAIL_TETHERED_CALL_ACTIVE /* -6 */:
                                case RilDataCallFailCause.PDP_FAIL_RADIO_POWER_OFF /* -5 */:
                                case RilDataCallFailCause.PDP_FAIL_PREF_RADIO_TECH_CHANGED /* -4 */:
                                case RilDataCallFailCause.PDP_FAIL_SIGNAL_LOST /* -3 */:
                                case -2:
                                case -1:
                                case 0:
                                case 1:
                                    break;
                            }
                        }
                        this.status = value;
                    } else if (tag == 16) {
                        this.suggestedRetryTimeMillis = input.readInt32();
                    } else if (tag == 26) {
                        if (this.call == null) {
                            this.call = new RilDataCall();
                        }
                        input.readMessage(this.call);
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RilSetupDataCallResponse parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RilSetupDataCallResponse) MessageNano.mergeFrom(new RilSetupDataCallResponse(), data);
            }

            public static RilSetupDataCallResponse parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RilSetupDataCallResponse().mergeFrom(input);
            }
        }

        public static final class CarrierKeyChange extends ExtendableMessageNano<CarrierKeyChange> {
            private static volatile CarrierKeyChange[] _emptyArray;
            public boolean isDownloadSuccessful;
            public int keyType;

            public interface KeyType {
                public static final int EPDG = 2;
                public static final int UNKNOWN = 0;
                public static final int WLAN = 1;
            }

            public static CarrierKeyChange[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new CarrierKeyChange[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public CarrierKeyChange() {
                clear();
            }

            public CarrierKeyChange clear() {
                this.keyType = 0;
                this.isDownloadSuccessful = false;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.keyType;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                boolean z = this.isDownloadSuccessful;
                if (z) {
                    output.writeBool(2, z);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.keyType;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                boolean z = this.isDownloadSuccessful;
                if (z) {
                    return size + CodedOutputByteBufferNano.computeBoolSize(2, z);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public CarrierKeyChange mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2) {
                            this.keyType = value;
                        } else {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                        }
                    } else if (tag == 16) {
                        this.isDownloadSuccessful = input.readBool();
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static CarrierKeyChange parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (CarrierKeyChange) MessageNano.mergeFrom(new CarrierKeyChange(), data);
            }

            public static CarrierKeyChange parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new CarrierKeyChange().mergeFrom(input);
            }
        }

        public static final class RilDeactivateDataCall extends ExtendableMessageNano<RilDeactivateDataCall> {
            private static volatile RilDeactivateDataCall[] _emptyArray;
            public int cid;
            public int reason;

            public interface DeactivateReason {
                public static final int DEACTIVATE_REASON_HANDOVER = 4;
                public static final int DEACTIVATE_REASON_NONE = 1;
                public static final int DEACTIVATE_REASON_PDP_RESET = 3;
                public static final int DEACTIVATE_REASON_RADIO_OFF = 2;
                public static final int DEACTIVATE_REASON_UNKNOWN = 0;
            }

            public static RilDeactivateDataCall[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new RilDeactivateDataCall[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public RilDeactivateDataCall() {
                clear();
            }

            public RilDeactivateDataCall clear() {
                this.cid = 0;
                this.reason = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.cid;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.reason;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.cid;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.reason;
                if (i2 != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public RilDeactivateDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.cid = input.readInt32();
                    } else if (tag == 16) {
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4) {
                            this.reason = value;
                        } else {
                            input.rewindToPosition(initialPos);
                            storeUnknownField(input, tag);
                        }
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static RilDeactivateDataCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (RilDeactivateDataCall) MessageNano.mergeFrom(new RilDeactivateDataCall(), data);
            }

            public static RilDeactivateDataCall parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new RilDeactivateDataCall().mergeFrom(input);
            }
        }

        public static final class ModemRestart extends ExtendableMessageNano<ModemRestart> {
            private static volatile ModemRestart[] _emptyArray;
            public String basebandVersion;
            public String reason;

            public static ModemRestart[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new ModemRestart[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public ModemRestart() {
                clear();
            }

            public ModemRestart clear() {
                this.basebandVersion = PhoneConfigurationManager.SSSS;
                this.reason = PhoneConfigurationManager.SSSS;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!this.basebandVersion.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(1, this.basebandVersion);
                }
                if (!this.reason.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(2, this.reason);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!this.basebandVersion.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.basebandVersion);
                }
                if (!this.reason.equals(PhoneConfigurationManager.SSSS)) {
                    return size + CodedOutputByteBufferNano.computeStringSize(2, this.reason);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public ModemRestart mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 10) {
                        this.basebandVersion = input.readString();
                    } else if (tag == 18) {
                        this.reason = input.readString();
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static ModemRestart parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (ModemRestart) MessageNano.mergeFrom(new ModemRestart(), data);
            }

            public static ModemRestart parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new ModemRestart().mergeFrom(input);
            }
        }

        public static final class CarrierIdMatching extends ExtendableMessageNano<CarrierIdMatching> {
            private static volatile CarrierIdMatching[] _emptyArray;
            public int cidTableVersion;
            public CarrierIdMatchingResult result;

            public static CarrierIdMatching[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new CarrierIdMatching[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public CarrierIdMatching() {
                clear();
            }

            public CarrierIdMatching clear() {
                this.cidTableVersion = 0;
                this.result = null;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.cidTableVersion;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                CarrierIdMatchingResult carrierIdMatchingResult = this.result;
                if (carrierIdMatchingResult != null) {
                    output.writeMessage(2, carrierIdMatchingResult);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.cidTableVersion;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                CarrierIdMatchingResult carrierIdMatchingResult = this.result;
                if (carrierIdMatchingResult != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(2, carrierIdMatchingResult);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public CarrierIdMatching mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    if (tag == 0) {
                        return this;
                    }
                    if (tag == 8) {
                        this.cidTableVersion = input.readInt32();
                    } else if (tag == 18) {
                        if (this.result == null) {
                            this.result = new CarrierIdMatchingResult();
                        }
                        input.readMessage(this.result);
                    } else if (!storeUnknownField(input, tag)) {
                        return this;
                    }
                }
            }

            public static CarrierIdMatching parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (CarrierIdMatching) MessageNano.mergeFrom(new CarrierIdMatching(), data);
            }

            public static CarrierIdMatching parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new CarrierIdMatching().mergeFrom(input);
            }
        }

        public static final class CarrierIdMatchingResult extends ExtendableMessageNano<CarrierIdMatchingResult> {
            private static volatile CarrierIdMatchingResult[] _emptyArray;
            public int carrierId;
            public String gid1;
            public String gid2;
            public String iccidPrefix;
            public String imsiPrefix;
            public String mccmnc;
            public String pnn;
            public String preferApn;
            public String[] privilegeAccessRule;
            public String spn;
            public String unknownGid1;
            public String unknownMccmnc;

            public static CarrierIdMatchingResult[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new CarrierIdMatchingResult[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public CarrierIdMatchingResult() {
                clear();
            }

            public CarrierIdMatchingResult clear() {
                this.carrierId = 0;
                this.unknownGid1 = PhoneConfigurationManager.SSSS;
                this.unknownMccmnc = PhoneConfigurationManager.SSSS;
                this.mccmnc = PhoneConfigurationManager.SSSS;
                this.gid1 = PhoneConfigurationManager.SSSS;
                this.gid2 = PhoneConfigurationManager.SSSS;
                this.spn = PhoneConfigurationManager.SSSS;
                this.pnn = PhoneConfigurationManager.SSSS;
                this.iccidPrefix = PhoneConfigurationManager.SSSS;
                this.imsiPrefix = PhoneConfigurationManager.SSSS;
                this.privilegeAccessRule = WireFormatNano.EMPTY_STRING_ARRAY;
                this.preferApn = PhoneConfigurationManager.SSSS;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.carrierId;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                if (!this.unknownGid1.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(2, this.unknownGid1);
                }
                if (!this.unknownMccmnc.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(3, this.unknownMccmnc);
                }
                if (!this.mccmnc.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(4, this.mccmnc);
                }
                if (!this.gid1.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(5, this.gid1);
                }
                if (!this.gid2.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(6, this.gid2);
                }
                if (!this.spn.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(7, this.spn);
                }
                if (!this.pnn.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(8, this.pnn);
                }
                if (!this.iccidPrefix.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(9, this.iccidPrefix);
                }
                if (!this.imsiPrefix.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(10, this.imsiPrefix);
                }
                String[] strArr = this.privilegeAccessRule;
                if (strArr != null && strArr.length > 0) {
                    int i2 = 0;
                    while (true) {
                        String[] strArr2 = this.privilegeAccessRule;
                        if (i2 >= strArr2.length) {
                            break;
                        }
                        String element = strArr2[i2];
                        if (element != null) {
                            output.writeString(11, element);
                        }
                        i2++;
                    }
                }
                if (!this.preferApn.equals(PhoneConfigurationManager.SSSS)) {
                    output.writeString(12, this.preferApn);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.carrierId;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                if (!this.unknownGid1.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(2, this.unknownGid1);
                }
                if (!this.unknownMccmnc.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(3, this.unknownMccmnc);
                }
                if (!this.mccmnc.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(4, this.mccmnc);
                }
                if (!this.gid1.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(5, this.gid1);
                }
                if (!this.gid2.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(6, this.gid2);
                }
                if (!this.spn.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(7, this.spn);
                }
                if (!this.pnn.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(8, this.pnn);
                }
                if (!this.iccidPrefix.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(9, this.iccidPrefix);
                }
                if (!this.imsiPrefix.equals(PhoneConfigurationManager.SSSS)) {
                    size += CodedOutputByteBufferNano.computeStringSize(10, this.imsiPrefix);
                }
                String[] strArr = this.privilegeAccessRule;
                if (strArr != null && strArr.length > 0) {
                    int dataCount = 0;
                    int dataSize = 0;
                    int i2 = 0;
                    while (true) {
                        String[] strArr2 = this.privilegeAccessRule;
                        if (i2 >= strArr2.length) {
                            break;
                        }
                        String element = strArr2[i2];
                        if (element != null) {
                            dataCount++;
                            dataSize += CodedOutputByteBufferNano.computeStringSizeNoTag(element);
                        }
                        i2++;
                    }
                    size = size + dataSize + (dataCount * 1);
                }
                if (!this.preferApn.equals(PhoneConfigurationManager.SSSS)) {
                    return size + CodedOutputByteBufferNano.computeStringSize(12, this.preferApn);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public CarrierIdMatchingResult mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            this.carrierId = input.readInt32();
                            break;
                        case 18:
                            this.unknownGid1 = input.readString();
                            break;
                        case 26:
                            this.unknownMccmnc = input.readString();
                            break;
                        case 34:
                            this.mccmnc = input.readString();
                            break;
                        case 42:
                            this.gid1 = input.readString();
                            break;
                        case 50:
                            this.gid2 = input.readString();
                            break;
                        case 58:
                            this.spn = input.readString();
                            break;
                        case 66:
                            this.pnn = input.readString();
                            break;
                        case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                            this.iccidPrefix = input.readString();
                            break;
                        case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /* 82 */:
                            this.imsiPrefix = input.readString();
                            break;
                        case 90:
                            int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 90);
                            String[] strArr = this.privilegeAccessRule;
                            int i = strArr == null ? 0 : strArr.length;
                            String[] newArray = new String[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.privilegeAccessRule, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = input.readString();
                                input.readTag();
                                i++;
                            }
                            newArray[i] = input.readString();
                            this.privilegeAccessRule = newArray;
                            break;
                        case 98:
                            this.preferApn = input.readString();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            } else {
                                return this;
                            }
                    }
                }
            }

            public static CarrierIdMatchingResult parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (CarrierIdMatchingResult) MessageNano.mergeFrom(new CarrierIdMatchingResult(), data);
            }

            public static CarrierIdMatchingResult parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new CarrierIdMatchingResult().mergeFrom(input);
            }
        }

        public static TelephonyEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonyEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonyEvent() {
            clear();
        }

        public TelephonyEvent clear() {
            this.timestampMillis = 0;
            this.phoneId = 0;
            this.type = 0;
            this.settings = null;
            this.serviceState = null;
            this.imsConnectionState = null;
            this.imsCapabilities = null;
            this.dataCalls = RilDataCall.emptyArray();
            this.error = 0;
            this.setupDataCall = null;
            this.setupDataCallResponse = null;
            this.deactivateDataCall = null;
            this.dataStallAction = 0;
            this.modemRestart = null;
            this.nitzTimestampMillis = 0;
            this.carrierIdMatching = null;
            this.carrierKeyChange = null;
            this.dataSwitch = null;
            this.networkValidationState = 0;
            this.onDemandDataSwitch = null;
            this.simState = WireFormatNano.EMPTY_INT_ARRAY;
            this.activeSubscriptionInfo = null;
            this.enabledModemBitmap = 0;
            this.updatedEmergencyNumber = null;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.timestampMillis;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            int i = this.phoneId;
            if (i != 0) {
                output.writeInt32(2, i);
            }
            int i2 = this.type;
            if (i2 != 0) {
                output.writeInt32(3, i2);
            }
            TelephonySettings telephonySettings = this.settings;
            if (telephonySettings != null) {
                output.writeMessage(4, telephonySettings);
            }
            TelephonyServiceState telephonyServiceState = this.serviceState;
            if (telephonyServiceState != null) {
                output.writeMessage(5, telephonyServiceState);
            }
            ImsConnectionState imsConnectionState2 = this.imsConnectionState;
            if (imsConnectionState2 != null) {
                output.writeMessage(6, imsConnectionState2);
            }
            ImsCapabilities imsCapabilities2 = this.imsCapabilities;
            if (imsCapabilities2 != null) {
                output.writeMessage(7, imsCapabilities2);
            }
            RilDataCall[] rilDataCallArr = this.dataCalls;
            if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                int i3 = 0;
                while (true) {
                    RilDataCall[] rilDataCallArr2 = this.dataCalls;
                    if (i3 >= rilDataCallArr2.length) {
                        break;
                    }
                    RilDataCall element = rilDataCallArr2[i3];
                    if (element != null) {
                        output.writeMessage(8, element);
                    }
                    i3++;
                }
            }
            int i4 = this.error;
            if (i4 != 0) {
                output.writeInt32(9, i4);
            }
            RilSetupDataCall rilSetupDataCall = this.setupDataCall;
            if (rilSetupDataCall != null) {
                output.writeMessage(10, rilSetupDataCall);
            }
            RilSetupDataCallResponse rilSetupDataCallResponse = this.setupDataCallResponse;
            if (rilSetupDataCallResponse != null) {
                output.writeMessage(11, rilSetupDataCallResponse);
            }
            RilDeactivateDataCall rilDeactivateDataCall = this.deactivateDataCall;
            if (rilDeactivateDataCall != null) {
                output.writeMessage(12, rilDeactivateDataCall);
            }
            int i5 = this.dataStallAction;
            if (i5 != 0) {
                output.writeInt32(13, i5);
            }
            ModemRestart modemRestart2 = this.modemRestart;
            if (modemRestart2 != null) {
                output.writeMessage(14, modemRestart2);
            }
            long j2 = this.nitzTimestampMillis;
            if (j2 != 0) {
                output.writeInt64(15, j2);
            }
            CarrierIdMatching carrierIdMatching2 = this.carrierIdMatching;
            if (carrierIdMatching2 != null) {
                output.writeMessage(16, carrierIdMatching2);
            }
            CarrierKeyChange carrierKeyChange2 = this.carrierKeyChange;
            if (carrierKeyChange2 != null) {
                output.writeMessage(17, carrierKeyChange2);
            }
            DataSwitch dataSwitch2 = this.dataSwitch;
            if (dataSwitch2 != null) {
                output.writeMessage(19, dataSwitch2);
            }
            int i6 = this.networkValidationState;
            if (i6 != 0) {
                output.writeInt32(20, i6);
            }
            OnDemandDataSwitch onDemandDataSwitch2 = this.onDemandDataSwitch;
            if (onDemandDataSwitch2 != null) {
                output.writeMessage(21, onDemandDataSwitch2);
            }
            int[] iArr = this.simState;
            if (iArr != null && iArr.length > 0) {
                int i7 = 0;
                while (true) {
                    int[] iArr2 = this.simState;
                    if (i7 >= iArr2.length) {
                        break;
                    }
                    output.writeInt32(22, iArr2[i7]);
                    i7++;
                }
            }
            ActiveSubscriptionInfo activeSubscriptionInfo2 = this.activeSubscriptionInfo;
            if (activeSubscriptionInfo2 != null) {
                output.writeMessage(23, activeSubscriptionInfo2);
            }
            int i8 = this.enabledModemBitmap;
            if (i8 != 0) {
                output.writeInt32(24, i8);
            }
            EmergencyNumberInfo emergencyNumberInfo = this.updatedEmergencyNumber;
            if (emergencyNumberInfo != null) {
                output.writeMessage(25, emergencyNumberInfo);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int[] iArr;
            int size = super.computeSerializedSize();
            long j = this.timestampMillis;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            int i = this.phoneId;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i);
            }
            int i2 = this.type;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, i2);
            }
            TelephonySettings telephonySettings = this.settings;
            if (telephonySettings != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(4, telephonySettings);
            }
            TelephonyServiceState telephonyServiceState = this.serviceState;
            if (telephonyServiceState != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(5, telephonyServiceState);
            }
            ImsConnectionState imsConnectionState2 = this.imsConnectionState;
            if (imsConnectionState2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, imsConnectionState2);
            }
            ImsCapabilities imsCapabilities2 = this.imsCapabilities;
            if (imsCapabilities2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, imsCapabilities2);
            }
            RilDataCall[] rilDataCallArr = this.dataCalls;
            if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                int i3 = 0;
                while (true) {
                    RilDataCall[] rilDataCallArr2 = this.dataCalls;
                    if (i3 >= rilDataCallArr2.length) {
                        break;
                    }
                    RilDataCall element = rilDataCallArr2[i3];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, element);
                    }
                    i3++;
                }
            }
            int i4 = this.error;
            if (i4 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, i4);
            }
            RilSetupDataCall rilSetupDataCall = this.setupDataCall;
            if (rilSetupDataCall != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(10, rilSetupDataCall);
            }
            RilSetupDataCallResponse rilSetupDataCallResponse = this.setupDataCallResponse;
            if (rilSetupDataCallResponse != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(11, rilSetupDataCallResponse);
            }
            RilDeactivateDataCall rilDeactivateDataCall = this.deactivateDataCall;
            if (rilDeactivateDataCall != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(12, rilDeactivateDataCall);
            }
            int i5 = this.dataStallAction;
            if (i5 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, i5);
            }
            ModemRestart modemRestart2 = this.modemRestart;
            if (modemRestart2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(14, modemRestart2);
            }
            long j2 = this.nitzTimestampMillis;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(15, j2);
            }
            CarrierIdMatching carrierIdMatching2 = this.carrierIdMatching;
            if (carrierIdMatching2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(16, carrierIdMatching2);
            }
            CarrierKeyChange carrierKeyChange2 = this.carrierKeyChange;
            if (carrierKeyChange2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(17, carrierKeyChange2);
            }
            DataSwitch dataSwitch2 = this.dataSwitch;
            if (dataSwitch2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(19, dataSwitch2);
            }
            int i6 = this.networkValidationState;
            if (i6 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(20, i6);
            }
            OnDemandDataSwitch onDemandDataSwitch2 = this.onDemandDataSwitch;
            if (onDemandDataSwitch2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(21, onDemandDataSwitch2);
            }
            int[] iArr2 = this.simState;
            if (iArr2 != null && iArr2.length > 0) {
                int dataSize = 0;
                int i7 = 0;
                while (true) {
                    iArr = this.simState;
                    if (i7 >= iArr.length) {
                        break;
                    }
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(iArr[i7]);
                    i7++;
                }
                size = size + dataSize + (iArr.length * 2);
            }
            ActiveSubscriptionInfo activeSubscriptionInfo2 = this.activeSubscriptionInfo;
            if (activeSubscriptionInfo2 != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(23, activeSubscriptionInfo2);
            }
            int i8 = this.enabledModemBitmap;
            if (i8 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(24, i8);
            }
            EmergencyNumberInfo emergencyNumberInfo = this.updatedEmergencyNumber;
            if (emergencyNumberInfo != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(25, emergencyNumberInfo);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonyEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timestampMillis = input.readInt64();
                        break;
                    case 16:
                        this.phoneId = input.readInt32();
                        break;
                    case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                                this.type = value;
                                continue;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                continue;
                        }
                    case 34:
                        if (this.settings == null) {
                            this.settings = new TelephonySettings();
                        }
                        input.readMessage(this.settings);
                        break;
                    case 42:
                        if (this.serviceState == null) {
                            this.serviceState = new TelephonyServiceState();
                        }
                        input.readMessage(this.serviceState);
                        break;
                    case 50:
                        if (this.imsConnectionState == null) {
                            this.imsConnectionState = new ImsConnectionState();
                        }
                        input.readMessage(this.imsConnectionState);
                        break;
                    case 58:
                        if (this.imsCapabilities == null) {
                            this.imsCapabilities = new ImsCapabilities();
                        }
                        input.readMessage(this.imsCapabilities);
                        break;
                    case 66:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        RilDataCall[] rilDataCallArr = this.dataCalls;
                        int i = rilDataCallArr == null ? 0 : rilDataCallArr.length;
                        RilDataCall[] newArray = new RilDataCall[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.dataCalls, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = new RilDataCall();
                            input.readMessage(newArray[i]);
                            input.readTag();
                            i++;
                        }
                        newArray[i] = new RilDataCall();
                        input.readMessage(newArray[i]);
                        this.dataCalls = newArray;
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                        int initialPos2 = input.getPosition();
                        int value2 = input.readInt32();
                        switch (value2) {
                            default:
                                switch (value2) {
                                    case 36:
                                    case 37:
                                    case 38:
                                    case 39:
                                    case 40:
                                    case 41:
                                    case 42:
                                    case 43:
                                    case 44:
                                    case 45:
                                    case 46:
                                    case 47:
                                    case 48:
                                    case 49:
                                    case 50:
                                    case 51:
                                    case 52:
                                    case 53:
                                    case 54:
                                    case 55:
                                    case 56:
                                    case 57:
                                    case 58:
                                    case 59:
                                    case 60:
                                    case RilErrno.RIL_E_NETWORK_NOT_READY /* 61 */:
                                    case 62:
                                    case 63:
                                    case 64:
                                    case 65:
                                    case 66:
                                    case RilErrno.RIL_E_INVALID_RESPONSE /* 67 */:
                                        break;
                                    default:
                                        input.rewindToPosition(initialPos2);
                                        storeUnknownField(input, tag);
                                        break;
                                }
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                                this.error = value2;
                                break;
                        }
                    case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /* 82 */:
                        if (this.setupDataCall == null) {
                            this.setupDataCall = new RilSetupDataCall();
                        }
                        input.readMessage(this.setupDataCall);
                        break;
                    case 90:
                        if (this.setupDataCallResponse == null) {
                            this.setupDataCallResponse = new RilSetupDataCallResponse();
                        }
                        input.readMessage(this.setupDataCallResponse);
                        break;
                    case 98:
                        if (this.deactivateDataCall == null) {
                            this.deactivateDataCall = new RilDeactivateDataCall();
                        }
                        input.readMessage(this.deactivateDataCall);
                        break;
                    case 104:
                        this.dataStallAction = input.readInt32();
                        break;
                    case 114:
                        if (this.modemRestart == null) {
                            this.modemRestart = new ModemRestart();
                        }
                        input.readMessage(this.modemRestart);
                        break;
                    case 120:
                        this.nitzTimestampMillis = input.readInt64();
                        break;
                    case 130:
                        if (this.carrierIdMatching == null) {
                            this.carrierIdMatching = new CarrierIdMatching();
                        }
                        input.readMessage(this.carrierIdMatching);
                        break;
                    case 138:
                        if (this.carrierKeyChange == null) {
                            this.carrierKeyChange = new CarrierKeyChange();
                        }
                        input.readMessage(this.carrierKeyChange);
                        break;
                    case 154:
                        if (this.dataSwitch == null) {
                            this.dataSwitch = new DataSwitch();
                        }
                        input.readMessage(this.dataSwitch);
                        break;
                    case 160:
                        int initialPos3 = input.getPosition();
                        int value3 = input.readInt32();
                        if (value3 != 0 && value3 != 1 && value3 != 2 && value3 != 3) {
                            input.rewindToPosition(initialPos3);
                            storeUnknownField(input, tag);
                            break;
                        } else {
                            this.networkValidationState = value3;
                            break;
                        }
                    case PduHeaders.MBOX_TOTALS /* 170 */:
                        if (this.onDemandDataSwitch == null) {
                            this.onDemandDataSwitch = new OnDemandDataSwitch();
                        }
                        input.readMessage(this.onDemandDataSwitch);
                        break;
                    case 176:
                        int length = WireFormatNano.getRepeatedFieldArrayLength(input, 176);
                        int[] validValues = new int[length];
                        int validCount = 0;
                        for (int i2 = 0; i2 < length; i2++) {
                            if (i2 != 0) {
                                input.readTag();
                            }
                            int initialPos4 = input.getPosition();
                            int value4 = input.readInt32();
                            if (value4 == 0 || value4 == 1 || value4 == 2) {
                                validValues[validCount] = value4;
                                validCount++;
                            } else {
                                input.rewindToPosition(initialPos4);
                                storeUnknownField(input, tag);
                            }
                        }
                        if (validCount == 0) {
                            break;
                        } else {
                            int[] iArr = this.simState;
                            int i3 = iArr == null ? 0 : iArr.length;
                            if (i3 != 0 || validCount != validValues.length) {
                                int[] newArray2 = new int[(i3 + validCount)];
                                if (i3 != 0) {
                                    System.arraycopy(this.simState, 0, newArray2, 0, i3);
                                }
                                System.arraycopy(validValues, 0, newArray2, i3, validCount);
                                this.simState = newArray2;
                                break;
                            } else {
                                this.simState = validValues;
                                break;
                            }
                        }
                    case PduHeaders.ELEMENT_DESCRIPTOR /* 178 */:
                        int limit = input.pushLimit(input.readRawVarint32());
                        int arrayLength2 = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            int readInt32 = input.readInt32();
                            if (readInt32 == 0 || readInt32 == 1 || readInt32 == 2) {
                                arrayLength2++;
                            }
                        }
                        if (arrayLength2 != 0) {
                            input.rewindToPosition(startPos);
                            int[] iArr2 = this.simState;
                            int i4 = iArr2 == null ? 0 : iArr2.length;
                            int[] newArray3 = new int[(i4 + arrayLength2)];
                            if (i4 != 0) {
                                System.arraycopy(this.simState, 0, newArray3, 0, i4);
                            }
                            while (input.getBytesUntilLimit() > 0) {
                                int initialPos5 = input.getPosition();
                                int value5 = input.readInt32();
                                if (value5 == 0 || value5 == 1 || value5 == 2) {
                                    newArray3[i4] = value5;
                                    i4++;
                                } else {
                                    input.rewindToPosition(initialPos5);
                                    storeUnknownField(input, 176);
                                }
                            }
                            this.simState = newArray3;
                        }
                        input.popLimit(limit);
                        break;
                    case PduHeaders.CONTENT_CLASS /* 186 */:
                        if (this.activeSubscriptionInfo == null) {
                            this.activeSubscriptionInfo = new ActiveSubscriptionInfo();
                        }
                        input.readMessage(this.activeSubscriptionInfo);
                        break;
                    case 192:
                        this.enabledModemBitmap = input.readInt32();
                        break;
                    case 202:
                        if (this.updatedEmergencyNumber == null) {
                            this.updatedEmergencyNumber = new EmergencyNumberInfo();
                        }
                        input.readMessage(this.updatedEmergencyNumber);
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static TelephonyEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonyEvent) MessageNano.mergeFrom(new TelephonyEvent(), data);
        }

        public static TelephonyEvent parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonyEvent().mergeFrom(input);
        }
    }

    public static final class ActiveSubscriptionInfo extends ExtendableMessageNano<ActiveSubscriptionInfo> {
        private static volatile ActiveSubscriptionInfo[] _emptyArray;
        public int carrierId;
        public int isOpportunistic;
        public int slotIndex;

        public static ActiveSubscriptionInfo[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ActiveSubscriptionInfo[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ActiveSubscriptionInfo() {
            clear();
        }

        public ActiveSubscriptionInfo clear() {
            this.slotIndex = 0;
            this.carrierId = 0;
            this.isOpportunistic = 0;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.slotIndex;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.carrierId;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            int i3 = this.isOpportunistic;
            if (i3 != 0) {
                output.writeInt32(3, i3);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.slotIndex;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.carrierId;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            int i3 = this.isOpportunistic;
            if (i3 != 0) {
                return size + CodedOutputByteBufferNano.computeInt32Size(3, i3);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public ActiveSubscriptionInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.slotIndex = input.readInt32();
                } else if (tag == 16) {
                    this.carrierId = input.readInt32();
                } else if (tag == 24) {
                    this.isOpportunistic = input.readInt32();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static ActiveSubscriptionInfo parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ActiveSubscriptionInfo) MessageNano.mergeFrom(new ActiveSubscriptionInfo(), data);
        }

        public static ActiveSubscriptionInfo parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ActiveSubscriptionInfo().mergeFrom(input);
        }
    }

    public static final class TelephonyCallSession extends ExtendableMessageNano<TelephonyCallSession> {
        private static volatile TelephonyCallSession[] _emptyArray;
        public Event[] events;
        public boolean eventsDropped;
        public int phoneId;
        public int startTimeMinutes;

        public static final class Event extends ExtendableMessageNano<Event> {
            private static volatile Event[] _emptyArray;
            public int audioCodec;
            public int callIndex;
            public CallQuality callQuality;
            public CallQualitySummary callQualitySummaryDl;
            public CallQualitySummary callQualitySummaryUl;
            public int callState;
            public RilCall[] calls;
            public RilDataCall[] dataCalls;
            public int delay;
            public int error;
            public ImsCapabilities imsCapabilities;
            public int imsCommand;
            public ImsConnectionState imsConnectionState;
            public EmergencyNumberInfo imsEmergencyNumberInfo;
            public boolean isImsEmergencyCall;
            public int mergedCallIndex;
            public long nitzTimestampMillis;
            public int phoneState;
            public ImsReasonInfo reasonInfo;
            public int rilRequest;
            public int rilRequestId;
            public TelephonyServiceState serviceState;
            public TelephonySettings settings;
            public int srcAccessTech;
            public int srvccState;
            public int targetAccessTech;
            public int type;

            public interface AudioCodec {
                public static final int AUDIO_CODEC_AMR = 1;
                public static final int AUDIO_CODEC_AMR_WB = 2;
                public static final int AUDIO_CODEC_EVRC = 4;
                public static final int AUDIO_CODEC_EVRC_B = 5;
                public static final int AUDIO_CODEC_EVRC_NW = 7;
                public static final int AUDIO_CODEC_EVRC_WB = 6;
                public static final int AUDIO_CODEC_EVS_FB = 20;
                public static final int AUDIO_CODEC_EVS_NB = 17;
                public static final int AUDIO_CODEC_EVS_SWB = 19;
                public static final int AUDIO_CODEC_EVS_WB = 18;
                public static final int AUDIO_CODEC_G711A = 13;
                public static final int AUDIO_CODEC_G711AB = 15;
                public static final int AUDIO_CODEC_G711U = 11;
                public static final int AUDIO_CODEC_G722 = 14;
                public static final int AUDIO_CODEC_G723 = 12;
                public static final int AUDIO_CODEC_G729 = 16;
                public static final int AUDIO_CODEC_GSM_EFR = 8;
                public static final int AUDIO_CODEC_GSM_FR = 9;
                public static final int AUDIO_CODEC_GSM_HR = 10;
                public static final int AUDIO_CODEC_QCELP13K = 3;
                public static final int AUDIO_CODEC_UNKNOWN = 0;
            }

            public interface CallState {
                public static final int CALL_ACTIVE = 2;
                public static final int CALL_ALERTING = 5;
                public static final int CALL_DIALING = 4;
                public static final int CALL_DISCONNECTED = 8;
                public static final int CALL_DISCONNECTING = 9;
                public static final int CALL_HOLDING = 3;
                public static final int CALL_IDLE = 1;
                public static final int CALL_INCOMING = 6;
                public static final int CALL_UNKNOWN = 0;
                public static final int CALL_WAITING = 7;
            }

            public interface ImsCommand {
                public static final int IMS_CMD_ACCEPT = 2;
                public static final int IMS_CMD_CONFERENCE_EXTEND = 9;
                public static final int IMS_CMD_HOLD = 5;
                public static final int IMS_CMD_INVITE_PARTICIPANT = 10;
                public static final int IMS_CMD_MERGE = 7;
                public static final int IMS_CMD_REJECT = 3;
                public static final int IMS_CMD_REMOVE_PARTICIPANT = 11;
                public static final int IMS_CMD_RESUME = 6;
                public static final int IMS_CMD_START = 1;
                public static final int IMS_CMD_TERMINATE = 4;
                public static final int IMS_CMD_UNKNOWN = 0;
                public static final int IMS_CMD_UPDATE = 8;
            }

            public interface PhoneState {
                public static final int STATE_IDLE = 1;
                public static final int STATE_OFFHOOK = 3;
                public static final int STATE_RINGING = 2;
                public static final int STATE_UNKNOWN = 0;
            }

            public interface RilRequest {
                public static final int RIL_REQUEST_ANSWER = 2;
                public static final int RIL_REQUEST_CDMA_FLASH = 6;
                public static final int RIL_REQUEST_CONFERENCE = 7;
                public static final int RIL_REQUEST_DIAL = 1;
                public static final int RIL_REQUEST_HANGUP = 3;
                public static final int RIL_REQUEST_SET_CALL_WAITING = 4;
                public static final int RIL_REQUEST_SWITCH_HOLDING_AND_ACTIVE = 5;
                public static final int RIL_REQUEST_UNKNOWN = 0;
            }

            public interface RilSrvccState {
                public static final int HANDOVER_CANCELED = 4;
                public static final int HANDOVER_COMPLETED = 2;
                public static final int HANDOVER_FAILED = 3;
                public static final int HANDOVER_STARTED = 1;
                public static final int HANDOVER_UNKNOWN = 0;
            }

            public interface Type {
                public static final int AUDIO_CODEC = 22;
                public static final int CALL_QUALITY_CHANGED = 23;
                public static final int DATA_CALL_LIST_CHANGED = 5;
                public static final int EVENT_UNKNOWN = 0;
                public static final int IMS_CALL_HANDOVER = 18;
                public static final int IMS_CALL_HANDOVER_FAILED = 19;
                public static final int IMS_CALL_RECEIVE = 15;
                public static final int IMS_CALL_STATE_CHANGED = 16;
                public static final int IMS_CALL_TERMINATED = 17;
                public static final int IMS_CAPABILITIES_CHANGED = 4;
                public static final int IMS_COMMAND = 11;
                public static final int IMS_COMMAND_COMPLETE = 14;
                public static final int IMS_COMMAND_FAILED = 13;
                public static final int IMS_COMMAND_RECEIVED = 12;
                public static final int IMS_CONNECTION_STATE_CHANGED = 3;
                public static final int NITZ_TIME = 21;
                public static final int PHONE_STATE_CHANGED = 20;
                public static final int RIL_CALL_LIST_CHANGED = 10;
                public static final int RIL_CALL_RING = 8;
                public static final int RIL_CALL_SRVCC = 9;
                public static final int RIL_REQUEST = 6;
                public static final int RIL_RESPONSE = 7;
                public static final int RIL_SERVICE_STATE_CHANGED = 2;
                public static final int SETTINGS_CHANGED = 1;
            }

            public static final class RilCall extends ExtendableMessageNano<RilCall> {
                private static volatile RilCall[] _emptyArray;
                public int callEndReason;
                public EmergencyNumberInfo emergencyNumberInfo;
                public int index;
                public boolean isEmergencyCall;
                public boolean isMultiparty;
                public int preciseDisconnectCause;
                public int state;
                public int type;

                public interface Type {
                    public static final int MO = 1;
                    public static final int MT = 2;
                    public static final int UNKNOWN = 0;
                }

                public static RilCall[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new RilCall[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public RilCall() {
                    clear();
                }

                public RilCall clear() {
                    this.index = 0;
                    this.state = 0;
                    this.type = 0;
                    this.callEndReason = 0;
                    this.isMultiparty = false;
                    this.preciseDisconnectCause = 0;
                    this.isEmergencyCall = false;
                    this.emergencyNumberInfo = null;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.index;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    int i2 = this.state;
                    if (i2 != 0) {
                        output.writeInt32(2, i2);
                    }
                    int i3 = this.type;
                    if (i3 != 0) {
                        output.writeInt32(3, i3);
                    }
                    int i4 = this.callEndReason;
                    if (i4 != 0) {
                        output.writeInt32(4, i4);
                    }
                    boolean z = this.isMultiparty;
                    if (z) {
                        output.writeBool(5, z);
                    }
                    int i5 = this.preciseDisconnectCause;
                    if (i5 != 0) {
                        output.writeInt32(6, i5);
                    }
                    boolean z2 = this.isEmergencyCall;
                    if (z2) {
                        output.writeBool(7, z2);
                    }
                    EmergencyNumberInfo emergencyNumberInfo2 = this.emergencyNumberInfo;
                    if (emergencyNumberInfo2 != null) {
                        output.writeMessage(8, emergencyNumberInfo2);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.index;
                    if (i != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    int i2 = this.state;
                    if (i2 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                    }
                    int i3 = this.type;
                    if (i3 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
                    }
                    int i4 = this.callEndReason;
                    if (i4 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
                    }
                    boolean z = this.isMultiparty;
                    if (z) {
                        size += CodedOutputByteBufferNano.computeBoolSize(5, z);
                    }
                    int i5 = this.preciseDisconnectCause;
                    if (i5 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(6, i5);
                    }
                    boolean z2 = this.isEmergencyCall;
                    if (z2) {
                        size += CodedOutputByteBufferNano.computeBoolSize(7, z2);
                    }
                    EmergencyNumberInfo emergencyNumberInfo2 = this.emergencyNumberInfo;
                    if (emergencyNumberInfo2 != null) {
                        return size + CodedOutputByteBufferNano.computeMessageSize(8, emergencyNumberInfo2);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public RilCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            this.index = input.readInt32();
                        } else if (tag == 16) {
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                    this.state = value;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        } else if (tag == 24) {
                            int initialPos2 = input.getPosition();
                            int value2 = input.readInt32();
                            if (value2 == 0 || value2 == 1 || value2 == 2) {
                                this.type = value2;
                            } else {
                                input.rewindToPosition(initialPos2);
                                storeUnknownField(input, tag);
                            }
                        } else if (tag == 32) {
                            this.callEndReason = input.readInt32();
                        } else if (tag == 40) {
                            this.isMultiparty = input.readBool();
                        } else if (tag == 48) {
                            this.preciseDisconnectCause = input.readInt32();
                        } else if (tag == 56) {
                            this.isEmergencyCall = input.readBool();
                        } else if (tag == 66) {
                            if (this.emergencyNumberInfo == null) {
                                this.emergencyNumberInfo = new EmergencyNumberInfo();
                            }
                            input.readMessage(this.emergencyNumberInfo);
                        } else if (!storeUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }

                public static RilCall parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (RilCall) MessageNano.mergeFrom(new RilCall(), data);
                }

                public static RilCall parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new RilCall().mergeFrom(input);
                }
            }

            public static final class SignalStrength extends ExtendableMessageNano<SignalStrength> {
                private static volatile SignalStrength[] _emptyArray;
                public int lteSnr;

                public static SignalStrength[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new SignalStrength[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public SignalStrength() {
                    clear();
                }

                public SignalStrength clear() {
                    this.lteSnr = 0;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.lteSnr;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.lteSnr;
                    if (i != 0) {
                        return size + CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public SignalStrength mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            this.lteSnr = input.readInt32();
                        } else if (!storeUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }

                public static SignalStrength parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (SignalStrength) MessageNano.mergeFrom(new SignalStrength(), data);
                }

                public static SignalStrength parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new SignalStrength().mergeFrom(input);
                }
            }

            public static final class CallQuality extends ExtendableMessageNano<CallQuality> {
                private static volatile CallQuality[] _emptyArray;
                public int averageRelativeJitterMillis;
                public int averageRoundTripTime;
                public int codecType;
                public int downlinkLevel;
                public int durationInSeconds;
                public int maxRelativeJitterMillis;
                public int rtpPacketsNotReceived;
                public int rtpPacketsReceived;
                public int rtpPacketsTransmitted;
                public int rtpPacketsTransmittedLost;
                public int uplinkLevel;

                public interface CallQualityLevel {
                    public static final int BAD = 5;
                    public static final int EXCELLENT = 1;
                    public static final int FAIR = 3;
                    public static final int GOOD = 2;
                    public static final int NOT_AVAILABLE = 6;
                    public static final int POOR = 4;
                    public static final int UNDEFINED = 0;
                }

                public static CallQuality[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new CallQuality[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public CallQuality() {
                    clear();
                }

                public CallQuality clear() {
                    this.downlinkLevel = 0;
                    this.uplinkLevel = 0;
                    this.durationInSeconds = 0;
                    this.rtpPacketsTransmitted = 0;
                    this.rtpPacketsReceived = 0;
                    this.rtpPacketsTransmittedLost = 0;
                    this.rtpPacketsNotReceived = 0;
                    this.averageRelativeJitterMillis = 0;
                    this.maxRelativeJitterMillis = 0;
                    this.averageRoundTripTime = 0;
                    this.codecType = 0;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.downlinkLevel;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    int i2 = this.uplinkLevel;
                    if (i2 != 0) {
                        output.writeInt32(2, i2);
                    }
                    int i3 = this.durationInSeconds;
                    if (i3 != 0) {
                        output.writeInt32(3, i3);
                    }
                    int i4 = this.rtpPacketsTransmitted;
                    if (i4 != 0) {
                        output.writeInt32(4, i4);
                    }
                    int i5 = this.rtpPacketsReceived;
                    if (i5 != 0) {
                        output.writeInt32(5, i5);
                    }
                    int i6 = this.rtpPacketsTransmittedLost;
                    if (i6 != 0) {
                        output.writeInt32(6, i6);
                    }
                    int i7 = this.rtpPacketsNotReceived;
                    if (i7 != 0) {
                        output.writeInt32(7, i7);
                    }
                    int i8 = this.averageRelativeJitterMillis;
                    if (i8 != 0) {
                        output.writeInt32(8, i8);
                    }
                    int i9 = this.maxRelativeJitterMillis;
                    if (i9 != 0) {
                        output.writeInt32(9, i9);
                    }
                    int i10 = this.averageRoundTripTime;
                    if (i10 != 0) {
                        output.writeInt32(10, i10);
                    }
                    int i11 = this.codecType;
                    if (i11 != 0) {
                        output.writeInt32(11, i11);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.downlinkLevel;
                    if (i != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    int i2 = this.uplinkLevel;
                    if (i2 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                    }
                    int i3 = this.durationInSeconds;
                    if (i3 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
                    }
                    int i4 = this.rtpPacketsTransmitted;
                    if (i4 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
                    }
                    int i5 = this.rtpPacketsReceived;
                    if (i5 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
                    }
                    int i6 = this.rtpPacketsTransmittedLost;
                    if (i6 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(6, i6);
                    }
                    int i7 = this.rtpPacketsNotReceived;
                    if (i7 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(7, i7);
                    }
                    int i8 = this.averageRelativeJitterMillis;
                    if (i8 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(8, i8);
                    }
                    int i9 = this.maxRelativeJitterMillis;
                    if (i9 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(9, i9);
                    }
                    int i10 = this.averageRoundTripTime;
                    if (i10 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(10, i10);
                    }
                    int i11 = this.codecType;
                    if (i11 != 0) {
                        return size + CodedOutputByteBufferNano.computeInt32Size(11, i11);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public CallQuality mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0:
                                return this;
                            case 8:
                                int initialPos = input.getPosition();
                                int value = input.readInt32();
                                switch (value) {
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                        this.downlinkLevel = value;
                                        continue;
                                    default:
                                        input.rewindToPosition(initialPos);
                                        storeUnknownField(input, tag);
                                        continue;
                                }
                            case 16:
                                int initialPos2 = input.getPosition();
                                int value2 = input.readInt32();
                                switch (value2) {
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                        this.uplinkLevel = value2;
                                        continue;
                                    default:
                                        input.rewindToPosition(initialPos2);
                                        storeUnknownField(input, tag);
                                        continue;
                                }
                            case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                                this.durationInSeconds = input.readInt32();
                                break;
                            case 32:
                                this.rtpPacketsTransmitted = input.readInt32();
                                break;
                            case 40:
                                this.rtpPacketsReceived = input.readInt32();
                                break;
                            case 48:
                                this.rtpPacketsTransmittedLost = input.readInt32();
                                break;
                            case 56:
                                this.rtpPacketsNotReceived = input.readInt32();
                                break;
                            case 64:
                                this.averageRelativeJitterMillis = input.readInt32();
                                break;
                            case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                                this.maxRelativeJitterMillis = input.readInt32();
                                break;
                            case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                                this.averageRoundTripTime = input.readInt32();
                                break;
                            case CallFailCause.INCOMPATIBLE_DESTINATION /* 88 */:
                                int initialPos3 = input.getPosition();
                                int value3 = input.readInt32();
                                switch (value3) {
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    case 10:
                                    case 11:
                                    case 12:
                                    case 13:
                                    case 14:
                                    case 15:
                                    case 16:
                                    case 17:
                                    case 18:
                                    case 19:
                                    case 20:
                                        this.codecType = value3;
                                        continue;
                                    default:
                                        input.rewindToPosition(initialPos3);
                                        storeUnknownField(input, tag);
                                        continue;
                                }
                            default:
                                if (storeUnknownField(input, tag)) {
                                    break;
                                } else {
                                    return this;
                                }
                        }
                    }
                }

                public static CallQuality parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (CallQuality) MessageNano.mergeFrom(new CallQuality(), data);
                }

                public static CallQuality parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new CallQuality().mergeFrom(input);
                }
            }

            public static final class CallQualitySummary extends ExtendableMessageNano<CallQualitySummary> {
                private static volatile CallQualitySummary[] _emptyArray;
                public SignalStrength bestSsWithBadQuality;
                public SignalStrength bestSsWithGoodQuality;
                public CallQuality snapshotOfBestSsWithBadQuality;
                public CallQuality snapshotOfBestSsWithGoodQuality;
                public CallQuality snapshotOfEnd;
                public CallQuality snapshotOfWorstSsWithBadQuality;
                public CallQuality snapshotOfWorstSsWithGoodQuality;
                public int totalBadQualityDurationInSeconds;
                public int totalDurationWithQualityInformationInSeconds;
                public int totalGoodQualityDurationInSeconds;
                public SignalStrength worstSsWithBadQuality;
                public SignalStrength worstSsWithGoodQuality;

                public static CallQualitySummary[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new CallQualitySummary[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public CallQualitySummary() {
                    clear();
                }

                public CallQualitySummary clear() {
                    this.totalGoodQualityDurationInSeconds = 0;
                    this.totalBadQualityDurationInSeconds = 0;
                    this.totalDurationWithQualityInformationInSeconds = 0;
                    this.snapshotOfWorstSsWithGoodQuality = null;
                    this.snapshotOfBestSsWithGoodQuality = null;
                    this.snapshotOfWorstSsWithBadQuality = null;
                    this.snapshotOfBestSsWithBadQuality = null;
                    this.worstSsWithGoodQuality = null;
                    this.bestSsWithGoodQuality = null;
                    this.worstSsWithBadQuality = null;
                    this.bestSsWithBadQuality = null;
                    this.snapshotOfEnd = null;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.totalGoodQualityDurationInSeconds;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    int i2 = this.totalBadQualityDurationInSeconds;
                    if (i2 != 0) {
                        output.writeInt32(2, i2);
                    }
                    int i3 = this.totalDurationWithQualityInformationInSeconds;
                    if (i3 != 0) {
                        output.writeInt32(3, i3);
                    }
                    CallQuality callQuality = this.snapshotOfWorstSsWithGoodQuality;
                    if (callQuality != null) {
                        output.writeMessage(4, callQuality);
                    }
                    CallQuality callQuality2 = this.snapshotOfBestSsWithGoodQuality;
                    if (callQuality2 != null) {
                        output.writeMessage(5, callQuality2);
                    }
                    CallQuality callQuality3 = this.snapshotOfWorstSsWithBadQuality;
                    if (callQuality3 != null) {
                        output.writeMessage(6, callQuality3);
                    }
                    CallQuality callQuality4 = this.snapshotOfBestSsWithBadQuality;
                    if (callQuality4 != null) {
                        output.writeMessage(7, callQuality4);
                    }
                    SignalStrength signalStrength = this.worstSsWithGoodQuality;
                    if (signalStrength != null) {
                        output.writeMessage(8, signalStrength);
                    }
                    SignalStrength signalStrength2 = this.bestSsWithGoodQuality;
                    if (signalStrength2 != null) {
                        output.writeMessage(9, signalStrength2);
                    }
                    SignalStrength signalStrength3 = this.worstSsWithBadQuality;
                    if (signalStrength3 != null) {
                        output.writeMessage(10, signalStrength3);
                    }
                    SignalStrength signalStrength4 = this.bestSsWithBadQuality;
                    if (signalStrength4 != null) {
                        output.writeMessage(11, signalStrength4);
                    }
                    CallQuality callQuality5 = this.snapshotOfEnd;
                    if (callQuality5 != null) {
                        output.writeMessage(12, callQuality5);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.totalGoodQualityDurationInSeconds;
                    if (i != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    int i2 = this.totalBadQualityDurationInSeconds;
                    if (i2 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                    }
                    int i3 = this.totalDurationWithQualityInformationInSeconds;
                    if (i3 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
                    }
                    CallQuality callQuality = this.snapshotOfWorstSsWithGoodQuality;
                    if (callQuality != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, callQuality);
                    }
                    CallQuality callQuality2 = this.snapshotOfBestSsWithGoodQuality;
                    if (callQuality2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(5, callQuality2);
                    }
                    CallQuality callQuality3 = this.snapshotOfWorstSsWithBadQuality;
                    if (callQuality3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(6, callQuality3);
                    }
                    CallQuality callQuality4 = this.snapshotOfBestSsWithBadQuality;
                    if (callQuality4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(7, callQuality4);
                    }
                    SignalStrength signalStrength = this.worstSsWithGoodQuality;
                    if (signalStrength != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, signalStrength);
                    }
                    SignalStrength signalStrength2 = this.bestSsWithGoodQuality;
                    if (signalStrength2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(9, signalStrength2);
                    }
                    SignalStrength signalStrength3 = this.worstSsWithBadQuality;
                    if (signalStrength3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(10, signalStrength3);
                    }
                    SignalStrength signalStrength4 = this.bestSsWithBadQuality;
                    if (signalStrength4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(11, signalStrength4);
                    }
                    CallQuality callQuality5 = this.snapshotOfEnd;
                    if (callQuality5 != null) {
                        return size + CodedOutputByteBufferNano.computeMessageSize(12, callQuality5);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public CallQualitySummary mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0:
                                return this;
                            case 8:
                                this.totalGoodQualityDurationInSeconds = input.readInt32();
                                break;
                            case 16:
                                this.totalBadQualityDurationInSeconds = input.readInt32();
                                break;
                            case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                                this.totalDurationWithQualityInformationInSeconds = input.readInt32();
                                break;
                            case 34:
                                if (this.snapshotOfWorstSsWithGoodQuality == null) {
                                    this.snapshotOfWorstSsWithGoodQuality = new CallQuality();
                                }
                                input.readMessage(this.snapshotOfWorstSsWithGoodQuality);
                                break;
                            case 42:
                                if (this.snapshotOfBestSsWithGoodQuality == null) {
                                    this.snapshotOfBestSsWithGoodQuality = new CallQuality();
                                }
                                input.readMessage(this.snapshotOfBestSsWithGoodQuality);
                                break;
                            case 50:
                                if (this.snapshotOfWorstSsWithBadQuality == null) {
                                    this.snapshotOfWorstSsWithBadQuality = new CallQuality();
                                }
                                input.readMessage(this.snapshotOfWorstSsWithBadQuality);
                                break;
                            case 58:
                                if (this.snapshotOfBestSsWithBadQuality == null) {
                                    this.snapshotOfBestSsWithBadQuality = new CallQuality();
                                }
                                input.readMessage(this.snapshotOfBestSsWithBadQuality);
                                break;
                            case 66:
                                if (this.worstSsWithGoodQuality == null) {
                                    this.worstSsWithGoodQuality = new SignalStrength();
                                }
                                input.readMessage(this.worstSsWithGoodQuality);
                                break;
                            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                                if (this.bestSsWithGoodQuality == null) {
                                    this.bestSsWithGoodQuality = new SignalStrength();
                                }
                                input.readMessage(this.bestSsWithGoodQuality);
                                break;
                            case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /* 82 */:
                                if (this.worstSsWithBadQuality == null) {
                                    this.worstSsWithBadQuality = new SignalStrength();
                                }
                                input.readMessage(this.worstSsWithBadQuality);
                                break;
                            case 90:
                                if (this.bestSsWithBadQuality == null) {
                                    this.bestSsWithBadQuality = new SignalStrength();
                                }
                                input.readMessage(this.bestSsWithBadQuality);
                                break;
                            case 98:
                                if (this.snapshotOfEnd == null) {
                                    this.snapshotOfEnd = new CallQuality();
                                }
                                input.readMessage(this.snapshotOfEnd);
                                break;
                            default:
                                if (storeUnknownField(input, tag)) {
                                    break;
                                } else {
                                    return this;
                                }
                        }
                    }
                }

                public static CallQualitySummary parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (CallQualitySummary) MessageNano.mergeFrom(new CallQualitySummary(), data);
                }

                public static CallQualitySummary parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new CallQualitySummary().mergeFrom(input);
                }
            }

            public static Event[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Event[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Event() {
                clear();
            }

            public Event clear() {
                this.type = 0;
                this.delay = 0;
                this.settings = null;
                this.serviceState = null;
                this.imsConnectionState = null;
                this.imsCapabilities = null;
                this.dataCalls = RilDataCall.emptyArray();
                this.phoneState = 0;
                this.callState = 0;
                this.callIndex = 0;
                this.mergedCallIndex = 0;
                this.calls = RilCall.emptyArray();
                this.error = 0;
                this.rilRequest = 0;
                this.rilRequestId = 0;
                this.srvccState = 0;
                this.imsCommand = 0;
                this.reasonInfo = null;
                this.srcAccessTech = -1;
                this.targetAccessTech = -1;
                this.nitzTimestampMillis = 0;
                this.audioCodec = 0;
                this.callQuality = null;
                this.callQualitySummaryDl = null;
                this.callQualitySummaryUl = null;
                this.isImsEmergencyCall = false;
                this.imsEmergencyNumberInfo = null;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.type;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.delay;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                TelephonySettings telephonySettings = this.settings;
                if (telephonySettings != null) {
                    output.writeMessage(3, telephonySettings);
                }
                TelephonyServiceState telephonyServiceState = this.serviceState;
                if (telephonyServiceState != null) {
                    output.writeMessage(4, telephonyServiceState);
                }
                ImsConnectionState imsConnectionState2 = this.imsConnectionState;
                if (imsConnectionState2 != null) {
                    output.writeMessage(5, imsConnectionState2);
                }
                ImsCapabilities imsCapabilities2 = this.imsCapabilities;
                if (imsCapabilities2 != null) {
                    output.writeMessage(6, imsCapabilities2);
                }
                RilDataCall[] rilDataCallArr = this.dataCalls;
                if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                    int i3 = 0;
                    while (true) {
                        RilDataCall[] rilDataCallArr2 = this.dataCalls;
                        if (i3 >= rilDataCallArr2.length) {
                            break;
                        }
                        RilDataCall element = rilDataCallArr2[i3];
                        if (element != null) {
                            output.writeMessage(7, element);
                        }
                        i3++;
                    }
                }
                int i4 = this.phoneState;
                if (i4 != 0) {
                    output.writeInt32(8, i4);
                }
                int i5 = this.callState;
                if (i5 != 0) {
                    output.writeInt32(9, i5);
                }
                int i6 = this.callIndex;
                if (i6 != 0) {
                    output.writeInt32(10, i6);
                }
                int i7 = this.mergedCallIndex;
                if (i7 != 0) {
                    output.writeInt32(11, i7);
                }
                RilCall[] rilCallArr = this.calls;
                if (rilCallArr != null && rilCallArr.length > 0) {
                    int i8 = 0;
                    while (true) {
                        RilCall[] rilCallArr2 = this.calls;
                        if (i8 >= rilCallArr2.length) {
                            break;
                        }
                        RilCall element2 = rilCallArr2[i8];
                        if (element2 != null) {
                            output.writeMessage(12, element2);
                        }
                        i8++;
                    }
                }
                int i9 = this.error;
                if (i9 != 0) {
                    output.writeInt32(13, i9);
                }
                int i10 = this.rilRequest;
                if (i10 != 0) {
                    output.writeInt32(14, i10);
                }
                int i11 = this.rilRequestId;
                if (i11 != 0) {
                    output.writeInt32(15, i11);
                }
                int i12 = this.srvccState;
                if (i12 != 0) {
                    output.writeInt32(16, i12);
                }
                int i13 = this.imsCommand;
                if (i13 != 0) {
                    output.writeInt32(17, i13);
                }
                ImsReasonInfo imsReasonInfo = this.reasonInfo;
                if (imsReasonInfo != null) {
                    output.writeMessage(18, imsReasonInfo);
                }
                int i14 = this.srcAccessTech;
                if (i14 != -1) {
                    output.writeInt32(19, i14);
                }
                int i15 = this.targetAccessTech;
                if (i15 != -1) {
                    output.writeInt32(20, i15);
                }
                long j = this.nitzTimestampMillis;
                if (j != 0) {
                    output.writeInt64(21, j);
                }
                int i16 = this.audioCodec;
                if (i16 != 0) {
                    output.writeInt32(22, i16);
                }
                CallQuality callQuality2 = this.callQuality;
                if (callQuality2 != null) {
                    output.writeMessage(23, callQuality2);
                }
                CallQualitySummary callQualitySummary = this.callQualitySummaryDl;
                if (callQualitySummary != null) {
                    output.writeMessage(24, callQualitySummary);
                }
                CallQualitySummary callQualitySummary2 = this.callQualitySummaryUl;
                if (callQualitySummary2 != null) {
                    output.writeMessage(25, callQualitySummary2);
                }
                boolean z = this.isImsEmergencyCall;
                if (z) {
                    output.writeBool(26, z);
                }
                EmergencyNumberInfo emergencyNumberInfo = this.imsEmergencyNumberInfo;
                if (emergencyNumberInfo != null) {
                    output.writeMessage(27, emergencyNumberInfo);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.type;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.delay;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                TelephonySettings telephonySettings = this.settings;
                if (telephonySettings != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, telephonySettings);
                }
                TelephonyServiceState telephonyServiceState = this.serviceState;
                if (telephonyServiceState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, telephonyServiceState);
                }
                ImsConnectionState imsConnectionState2 = this.imsConnectionState;
                if (imsConnectionState2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, imsConnectionState2);
                }
                ImsCapabilities imsCapabilities2 = this.imsCapabilities;
                if (imsCapabilities2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, imsCapabilities2);
                }
                RilDataCall[] rilDataCallArr = this.dataCalls;
                if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                    int i3 = 0;
                    while (true) {
                        RilDataCall[] rilDataCallArr2 = this.dataCalls;
                        if (i3 >= rilDataCallArr2.length) {
                            break;
                        }
                        RilDataCall element = rilDataCallArr2[i3];
                        if (element != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(7, element);
                        }
                        i3++;
                    }
                }
                int i4 = this.phoneState;
                if (i4 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(8, i4);
                }
                int i5 = this.callState;
                if (i5 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(9, i5);
                }
                int i6 = this.callIndex;
                if (i6 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(10, i6);
                }
                int i7 = this.mergedCallIndex;
                if (i7 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(11, i7);
                }
                RilCall[] rilCallArr = this.calls;
                if (rilCallArr != null && rilCallArr.length > 0) {
                    int i8 = 0;
                    while (true) {
                        RilCall[] rilCallArr2 = this.calls;
                        if (i8 >= rilCallArr2.length) {
                            break;
                        }
                        RilCall element2 = rilCallArr2[i8];
                        if (element2 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(12, element2);
                        }
                        i8++;
                    }
                }
                int i9 = this.error;
                if (i9 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(13, i9);
                }
                int i10 = this.rilRequest;
                if (i10 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(14, i10);
                }
                int i11 = this.rilRequestId;
                if (i11 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(15, i11);
                }
                int i12 = this.srvccState;
                if (i12 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(16, i12);
                }
                int i13 = this.imsCommand;
                if (i13 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(17, i13);
                }
                ImsReasonInfo imsReasonInfo = this.reasonInfo;
                if (imsReasonInfo != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(18, imsReasonInfo);
                }
                int i14 = this.srcAccessTech;
                if (i14 != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(19, i14);
                }
                int i15 = this.targetAccessTech;
                if (i15 != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(20, i15);
                }
                long j = this.nitzTimestampMillis;
                if (j != 0) {
                    size += CodedOutputByteBufferNano.computeInt64Size(21, j);
                }
                int i16 = this.audioCodec;
                if (i16 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(22, i16);
                }
                CallQuality callQuality2 = this.callQuality;
                if (callQuality2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(23, callQuality2);
                }
                CallQualitySummary callQualitySummary = this.callQualitySummaryDl;
                if (callQualitySummary != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(24, callQualitySummary);
                }
                CallQualitySummary callQualitySummary2 = this.callQualitySummaryUl;
                if (callQualitySummary2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(25, callQualitySummary2);
                }
                boolean z = this.isImsEmergencyCall;
                if (z) {
                    size += CodedOutputByteBufferNano.computeBoolSize(26, z);
                }
                EmergencyNumberInfo emergencyNumberInfo = this.imsEmergencyNumberInfo;
                if (emergencyNumberInfo != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(27, emergencyNumberInfo);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public Event mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                case 20:
                                case 21:
                                case 22:
                                case 23:
                                    this.type = value;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 16:
                            int initialPos2 = input.getPosition();
                            int value2 = input.readInt32();
                            switch (value2) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                    this.delay = value2;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos2);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 26:
                            if (this.settings == null) {
                                this.settings = new TelephonySettings();
                            }
                            input.readMessage(this.settings);
                            break;
                        case 34:
                            if (this.serviceState == null) {
                                this.serviceState = new TelephonyServiceState();
                            }
                            input.readMessage(this.serviceState);
                            break;
                        case 42:
                            if (this.imsConnectionState == null) {
                                this.imsConnectionState = new ImsConnectionState();
                            }
                            input.readMessage(this.imsConnectionState);
                            break;
                        case 50:
                            if (this.imsCapabilities == null) {
                                this.imsCapabilities = new ImsCapabilities();
                            }
                            input.readMessage(this.imsCapabilities);
                            break;
                        case 58:
                            int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                            RilDataCall[] rilDataCallArr = this.dataCalls;
                            int i = rilDataCallArr == null ? 0 : rilDataCallArr.length;
                            RilDataCall[] newArray = new RilDataCall[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.dataCalls, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = new RilDataCall();
                                input.readMessage(newArray[i]);
                                input.readTag();
                                i++;
                            }
                            newArray[i] = new RilDataCall();
                            input.readMessage(newArray[i]);
                            this.dataCalls = newArray;
                            break;
                        case 64:
                            int initialPos3 = input.getPosition();
                            int value3 = input.readInt32();
                            if (value3 != 0 && value3 != 1 && value3 != 2 && value3 != 3) {
                                input.rewindToPosition(initialPos3);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.phoneState = value3;
                                break;
                            }
                        case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                            int initialPos4 = input.getPosition();
                            int value4 = input.readInt32();
                            switch (value4) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                    this.callState = value4;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos4);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                            this.callIndex = input.readInt32();
                            break;
                        case CallFailCause.INCOMPATIBLE_DESTINATION /* 88 */:
                            this.mergedCallIndex = input.readInt32();
                            break;
                        case 98:
                            int arrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(input, 98);
                            RilCall[] rilCallArr = this.calls;
                            int i2 = rilCallArr == null ? 0 : rilCallArr.length;
                            RilCall[] newArray2 = new RilCall[(i2 + arrayLength2)];
                            if (i2 != 0) {
                                System.arraycopy(this.calls, 0, newArray2, 0, i2);
                            }
                            while (i2 < newArray2.length - 1) {
                                newArray2[i2] = new RilCall();
                                input.readMessage(newArray2[i2]);
                                input.readTag();
                                i2++;
                            }
                            newArray2[i2] = new RilCall();
                            input.readMessage(newArray2[i2]);
                            this.calls = newArray2;
                            break;
                        case 104:
                            int initialPos5 = input.getPosition();
                            int value5 = input.readInt32();
                            switch (value5) {
                                default:
                                    switch (value5) {
                                        case 36:
                                        case 37:
                                        case 38:
                                        case 39:
                                        case 40:
                                        case 41:
                                        case 42:
                                        case 43:
                                        case 44:
                                        case 45:
                                        case 46:
                                        case 47:
                                        case 48:
                                        case 49:
                                        case 50:
                                        case 51:
                                        case 52:
                                        case 53:
                                        case 54:
                                        case 55:
                                        case 56:
                                        case 57:
                                        case 58:
                                        case 59:
                                        case 60:
                                        case RilErrno.RIL_E_NETWORK_NOT_READY /* 61 */:
                                        case 62:
                                        case 63:
                                        case 64:
                                        case 65:
                                        case 66:
                                        case RilErrno.RIL_E_INVALID_RESPONSE /* 67 */:
                                            break;
                                        default:
                                            input.rewindToPosition(initialPos5);
                                            storeUnknownField(input, tag);
                                            break;
                                    }
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                case 20:
                                case 21:
                                case 22:
                                case 23:
                                case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                    this.error = value5;
                                    break;
                            }
                        case 112:
                            int initialPos6 = input.getPosition();
                            int value6 = input.readInt32();
                            switch (value6) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                    this.rilRequest = value6;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos6);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 120:
                            this.rilRequestId = input.readInt32();
                            break;
                        case 128:
                            int initialPos7 = input.getPosition();
                            int value7 = input.readInt32();
                            if (value7 != 0 && value7 != 1 && value7 != 2 && value7 != 3 && value7 != 4) {
                                input.rewindToPosition(initialPos7);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.srvccState = value7;
                                break;
                            }
                        case 136:
                            int initialPos8 = input.getPosition();
                            int value8 = input.readInt32();
                            switch (value8) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                    this.imsCommand = value8;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos8);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 146:
                            if (this.reasonInfo == null) {
                                this.reasonInfo = new ImsReasonInfo();
                            }
                            input.readMessage(this.reasonInfo);
                            break;
                        case 152:
                            int initialPos9 = input.getPosition();
                            int value9 = input.readInt32();
                            switch (value9) {
                                case -1:
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                    this.srcAccessTech = value9;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos9);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 160:
                            int initialPos10 = input.getPosition();
                            int value10 = input.readInt32();
                            switch (value10) {
                                case -1:
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                    this.targetAccessTech = value10;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos10);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case PduHeaders.ATTRIBUTES /* 168 */:
                            this.nitzTimestampMillis = input.readInt64();
                            break;
                        case 176:
                            int initialPos11 = input.getPosition();
                            int value11 = input.readInt32();
                            switch (value11) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                case 20:
                                    this.audioCodec = value11;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos11);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case PduHeaders.CONTENT_CLASS /* 186 */:
                            if (this.callQuality == null) {
                                this.callQuality = new CallQuality();
                            }
                            input.readMessage(this.callQuality);
                            break;
                        case 194:
                            if (this.callQualitySummaryDl == null) {
                                this.callQualitySummaryDl = new CallQualitySummary();
                            }
                            input.readMessage(this.callQualitySummaryDl);
                            break;
                        case 202:
                            if (this.callQualitySummaryUl == null) {
                                this.callQualitySummaryUl = new CallQualitySummary();
                            }
                            input.readMessage(this.callQualitySummaryUl);
                            break;
                        case BerTlv.BER_PROACTIVE_COMMAND_TAG /* 208 */:
                            this.isImsEmergencyCall = input.readBool();
                            break;
                        case 218:
                            if (this.imsEmergencyNumberInfo == null) {
                                this.imsEmergencyNumberInfo = new EmergencyNumberInfo();
                            }
                            input.readMessage(this.imsEmergencyNumberInfo);
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            } else {
                                return this;
                            }
                    }
                }
            }

            public static Event parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (Event) MessageNano.mergeFrom(new Event(), data);
            }

            public static Event parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new Event().mergeFrom(input);
            }
        }

        public static TelephonyCallSession[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new TelephonyCallSession[0];
                    }
                }
            }
            return _emptyArray;
        }

        public TelephonyCallSession() {
            clear();
        }

        public TelephonyCallSession clear() {
            this.startTimeMinutes = 0;
            this.phoneId = 0;
            this.events = Event.emptyArray();
            this.eventsDropped = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.startTimeMinutes;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.phoneId;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            Event[] eventArr = this.events;
            if (eventArr != null && eventArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Event[] eventArr2 = this.events;
                    if (i3 >= eventArr2.length) {
                        break;
                    }
                    Event element = eventArr2[i3];
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                    i3++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                output.writeBool(4, z);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.startTimeMinutes;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.phoneId;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            Event[] eventArr = this.events;
            if (eventArr != null && eventArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Event[] eventArr2 = this.events;
                    if (i3 >= eventArr2.length) {
                        break;
                    }
                    Event element = eventArr2[i3];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                    i3++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                return size + CodedOutputByteBufferNano.computeBoolSize(4, z);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public TelephonyCallSession mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.startTimeMinutes = input.readInt32();
                } else if (tag == 16) {
                    this.phoneId = input.readInt32();
                } else if (tag == 26) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    Event[] eventArr = this.events;
                    int i = eventArr == null ? 0 : eventArr.length;
                    Event[] newArray = new Event[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.events, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new Event();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new Event();
                    input.readMessage(newArray[i]);
                    this.events = newArray;
                } else if (tag == 32) {
                    this.eventsDropped = input.readBool();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static TelephonyCallSession parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (TelephonyCallSession) MessageNano.mergeFrom(new TelephonyCallSession(), data);
        }

        public static TelephonyCallSession parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new TelephonyCallSession().mergeFrom(input);
        }
    }

    public static final class SmsSession extends ExtendableMessageNano<SmsSession> {
        private static volatile SmsSession[] _emptyArray;
        public Event[] events;
        public boolean eventsDropped;
        public int phoneId;
        public int startTimeMinutes;

        public static final class Event extends ExtendableMessageNano<Event> {
            private static volatile Event[] _emptyArray;
            public boolean blocked;
            public CBMessage cellBroadcastMessage;
            public RilDataCall[] dataCalls;
            public int delay;
            public int error;
            public int errorCode;
            public int format;
            public ImsCapabilities imsCapabilities;
            public ImsConnectionState imsConnectionState;
            public int imsError;
            public IncompleteSms incompleteSms;
            public int rilRequestId;
            public TelephonyServiceState serviceState;
            public TelephonySettings settings;
            public int smsType;
            public int tech;
            public int type;

            public interface CBMessageType {
                public static final int CMAS = 2;
                public static final int ETWS = 1;
                public static final int OTHER = 3;
                public static final int TYPE_UNKNOWN = 0;
            }

            public interface CBPriority {
                public static final int EMERGENCY = 4;
                public static final int INTERACTIVE = 2;
                public static final int NORMAL = 1;
                public static final int PRIORITY_UNKNOWN = 0;
                public static final int URGENT = 3;
            }

            public interface Format {
                public static final int SMS_FORMAT_3GPP = 1;
                public static final int SMS_FORMAT_3GPP2 = 2;
                public static final int SMS_FORMAT_UNKNOWN = 0;
            }

            public interface SmsType {
                public static final int SMS_TYPE_NORMAL = 0;
                public static final int SMS_TYPE_SMS_PP = 1;
                public static final int SMS_TYPE_VOICEMAIL_INDICATION = 2;
                public static final int SMS_TYPE_WAP_PUSH = 4;
                public static final int SMS_TYPE_ZERO = 3;
            }

            public interface Tech {
                public static final int SMS_CDMA = 2;
                public static final int SMS_GSM = 1;
                public static final int SMS_IMS = 3;
                public static final int SMS_UNKNOWN = 0;
            }

            public interface Type {
                public static final int CB_SMS_RECEIVED = 9;
                public static final int DATA_CALL_LIST_CHANGED = 5;
                public static final int EVENT_UNKNOWN = 0;
                public static final int IMS_CAPABILITIES_CHANGED = 4;
                public static final int IMS_CONNECTION_STATE_CHANGED = 3;
                public static final int INCOMPLETE_SMS_RECEIVED = 10;
                public static final int RIL_SERVICE_STATE_CHANGED = 2;
                public static final int SETTINGS_CHANGED = 1;
                public static final int SMS_RECEIVED = 8;
                public static final int SMS_SEND = 6;
                public static final int SMS_SEND_RESULT = 7;
            }

            public static final class CBMessage extends ExtendableMessageNano<CBMessage> {
                private static volatile CBMessage[] _emptyArray;
                public long deliveredTimestampMillis;
                public int msgFormat;
                public int msgPriority;
                public int msgType;
                public int serialNumber;
                public int serviceCategory;

                public static CBMessage[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new CBMessage[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public CBMessage() {
                    clear();
                }

                public CBMessage clear() {
                    this.msgFormat = 0;
                    this.msgPriority = 0;
                    this.msgType = 0;
                    this.serviceCategory = 0;
                    this.serialNumber = 0;
                    this.deliveredTimestampMillis = 0;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.msgFormat;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    int i2 = this.msgPriority;
                    if (i2 != 0) {
                        output.writeInt32(2, i2);
                    }
                    int i3 = this.msgType;
                    if (i3 != 0) {
                        output.writeInt32(3, i3);
                    }
                    int i4 = this.serviceCategory;
                    if (i4 != 0) {
                        output.writeInt32(4, i4);
                    }
                    int i5 = this.serialNumber;
                    if (i5 != 0) {
                        output.writeInt32(5, i5);
                    }
                    long j = this.deliveredTimestampMillis;
                    if (j != 0) {
                        output.writeInt64(6, j);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.msgFormat;
                    if (i != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    int i2 = this.msgPriority;
                    if (i2 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                    }
                    int i3 = this.msgType;
                    if (i3 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(3, i3);
                    }
                    int i4 = this.serviceCategory;
                    if (i4 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(4, i4);
                    }
                    int i5 = this.serialNumber;
                    if (i5 != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(5, i5);
                    }
                    long j = this.deliveredTimestampMillis;
                    if (j != 0) {
                        return size + CodedOutputByteBufferNano.computeInt64Size(6, j);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public CBMessage mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            if (value == 0 || value == 1 || value == 2) {
                                this.msgFormat = value;
                            } else {
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                            }
                        } else if (tag == 16) {
                            int initialPos2 = input.getPosition();
                            int value2 = input.readInt32();
                            if (value2 == 0 || value2 == 1 || value2 == 2 || value2 == 3 || value2 == 4) {
                                this.msgPriority = value2;
                            } else {
                                input.rewindToPosition(initialPos2);
                                storeUnknownField(input, tag);
                            }
                        } else if (tag == 24) {
                            int initialPos3 = input.getPosition();
                            int value3 = input.readInt32();
                            if (value3 == 0 || value3 == 1 || value3 == 2 || value3 == 3) {
                                this.msgType = value3;
                            } else {
                                input.rewindToPosition(initialPos3);
                                storeUnknownField(input, tag);
                            }
                        } else if (tag == 32) {
                            this.serviceCategory = input.readInt32();
                        } else if (tag == 40) {
                            this.serialNumber = input.readInt32();
                        } else if (tag == 48) {
                            this.deliveredTimestampMillis = input.readInt64();
                        } else if (!storeUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }

                public static CBMessage parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (CBMessage) MessageNano.mergeFrom(new CBMessage(), data);
                }

                public static CBMessage parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new CBMessage().mergeFrom(input);
                }
            }

            public static final class IncompleteSms extends ExtendableMessageNano<IncompleteSms> {
                private static volatile IncompleteSms[] _emptyArray;
                public int receivedParts;
                public int totalParts;

                public static IncompleteSms[] emptyArray() {
                    if (_emptyArray == null) {
                        synchronized (InternalNano.LAZY_INIT_LOCK) {
                            if (_emptyArray == null) {
                                _emptyArray = new IncompleteSms[0];
                            }
                        }
                    }
                    return _emptyArray;
                }

                public IncompleteSms() {
                    clear();
                }

                public IncompleteSms clear() {
                    this.receivedParts = 0;
                    this.totalParts = 0;
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    int i = this.receivedParts;
                    if (i != 0) {
                        output.writeInt32(1, i);
                    }
                    int i2 = this.totalParts;
                    if (i2 != 0) {
                        output.writeInt32(2, i2);
                    }
                    super.writeTo(output);
                }

                /* access modifiers changed from: protected */
                @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
                public int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    int i = this.receivedParts;
                    if (i != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                    }
                    int i2 = this.totalParts;
                    if (i2 != 0) {
                        return size + CodedOutputByteBufferNano.computeInt32Size(2, i2);
                    }
                    return size;
                }

                @Override // com.android.internal.telephony.protobuf.nano.MessageNano
                public IncompleteSms mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        if (tag == 0) {
                            return this;
                        }
                        if (tag == 8) {
                            this.receivedParts = input.readInt32();
                        } else if (tag == 16) {
                            this.totalParts = input.readInt32();
                        } else if (!storeUnknownField(input, tag)) {
                            return this;
                        }
                    }
                }

                public static IncompleteSms parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                    return (IncompleteSms) MessageNano.mergeFrom(new IncompleteSms(), data);
                }

                public static IncompleteSms parseFrom(CodedInputByteBufferNano input) throws IOException {
                    return new IncompleteSms().mergeFrom(input);
                }
            }

            public static Event[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Event[0];
                        }
                    }
                }
                return _emptyArray;
            }

            public Event() {
                clear();
            }

            public Event clear() {
                this.type = 0;
                this.delay = 0;
                this.settings = null;
                this.serviceState = null;
                this.imsConnectionState = null;
                this.imsCapabilities = null;
                this.dataCalls = RilDataCall.emptyArray();
                this.format = 0;
                this.tech = 0;
                this.errorCode = 0;
                this.error = 0;
                this.rilRequestId = 0;
                this.cellBroadcastMessage = null;
                this.imsError = 0;
                this.incompleteSms = null;
                this.smsType = 0;
                this.blocked = false;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                int i = this.type;
                if (i != 0) {
                    output.writeInt32(1, i);
                }
                int i2 = this.delay;
                if (i2 != 0) {
                    output.writeInt32(2, i2);
                }
                TelephonySettings telephonySettings = this.settings;
                if (telephonySettings != null) {
                    output.writeMessage(3, telephonySettings);
                }
                TelephonyServiceState telephonyServiceState = this.serviceState;
                if (telephonyServiceState != null) {
                    output.writeMessage(4, telephonyServiceState);
                }
                ImsConnectionState imsConnectionState2 = this.imsConnectionState;
                if (imsConnectionState2 != null) {
                    output.writeMessage(5, imsConnectionState2);
                }
                ImsCapabilities imsCapabilities2 = this.imsCapabilities;
                if (imsCapabilities2 != null) {
                    output.writeMessage(6, imsCapabilities2);
                }
                RilDataCall[] rilDataCallArr = this.dataCalls;
                if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                    int i3 = 0;
                    while (true) {
                        RilDataCall[] rilDataCallArr2 = this.dataCalls;
                        if (i3 >= rilDataCallArr2.length) {
                            break;
                        }
                        RilDataCall element = rilDataCallArr2[i3];
                        if (element != null) {
                            output.writeMessage(7, element);
                        }
                        i3++;
                    }
                }
                int i4 = this.format;
                if (i4 != 0) {
                    output.writeInt32(8, i4);
                }
                int i5 = this.tech;
                if (i5 != 0) {
                    output.writeInt32(9, i5);
                }
                int i6 = this.errorCode;
                if (i6 != 0) {
                    output.writeInt32(10, i6);
                }
                int i7 = this.error;
                if (i7 != 0) {
                    output.writeInt32(11, i7);
                }
                int i8 = this.rilRequestId;
                if (i8 != 0) {
                    output.writeInt32(12, i8);
                }
                CBMessage cBMessage = this.cellBroadcastMessage;
                if (cBMessage != null) {
                    output.writeMessage(13, cBMessage);
                }
                int i9 = this.imsError;
                if (i9 != 0) {
                    output.writeInt32(14, i9);
                }
                IncompleteSms incompleteSms2 = this.incompleteSms;
                if (incompleteSms2 != null) {
                    output.writeMessage(15, incompleteSms2);
                }
                int i10 = this.smsType;
                if (i10 != 0) {
                    output.writeInt32(16, i10);
                }
                boolean z = this.blocked;
                if (z) {
                    output.writeBool(17, z);
                }
                super.writeTo(output);
            }

            /* access modifiers changed from: protected */
            @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
            public int computeSerializedSize() {
                int size = super.computeSerializedSize();
                int i = this.type;
                if (i != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, i);
                }
                int i2 = this.delay;
                if (i2 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
                }
                TelephonySettings telephonySettings = this.settings;
                if (telephonySettings != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, telephonySettings);
                }
                TelephonyServiceState telephonyServiceState = this.serviceState;
                if (telephonyServiceState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, telephonyServiceState);
                }
                ImsConnectionState imsConnectionState2 = this.imsConnectionState;
                if (imsConnectionState2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, imsConnectionState2);
                }
                ImsCapabilities imsCapabilities2 = this.imsCapabilities;
                if (imsCapabilities2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, imsCapabilities2);
                }
                RilDataCall[] rilDataCallArr = this.dataCalls;
                if (rilDataCallArr != null && rilDataCallArr.length > 0) {
                    int i3 = 0;
                    while (true) {
                        RilDataCall[] rilDataCallArr2 = this.dataCalls;
                        if (i3 >= rilDataCallArr2.length) {
                            break;
                        }
                        RilDataCall element = rilDataCallArr2[i3];
                        if (element != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(7, element);
                        }
                        i3++;
                    }
                }
                int i4 = this.format;
                if (i4 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(8, i4);
                }
                int i5 = this.tech;
                if (i5 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(9, i5);
                }
                int i6 = this.errorCode;
                if (i6 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(10, i6);
                }
                int i7 = this.error;
                if (i7 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(11, i7);
                }
                int i8 = this.rilRequestId;
                if (i8 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(12, i8);
                }
                CBMessage cBMessage = this.cellBroadcastMessage;
                if (cBMessage != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(13, cBMessage);
                }
                int i9 = this.imsError;
                if (i9 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(14, i9);
                }
                IncompleteSms incompleteSms2 = this.incompleteSms;
                if (incompleteSms2 != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(15, incompleteSms2);
                }
                int i10 = this.smsType;
                if (i10 != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(16, i10);
                }
                boolean z = this.blocked;
                if (z) {
                    return size + CodedOutputByteBufferNano.computeBoolSize(17, z);
                }
                return size;
            }

            @Override // com.android.internal.telephony.protobuf.nano.MessageNano
            public Event mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                    this.type = value;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 16:
                            int initialPos2 = input.getPosition();
                            int value2 = input.readInt32();
                            switch (value2) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                    this.delay = value2;
                                    continue;
                                default:
                                    input.rewindToPosition(initialPos2);
                                    storeUnknownField(input, tag);
                                    continue;
                            }
                        case 26:
                            if (this.settings == null) {
                                this.settings = new TelephonySettings();
                            }
                            input.readMessage(this.settings);
                            break;
                        case 34:
                            if (this.serviceState == null) {
                                this.serviceState = new TelephonyServiceState();
                            }
                            input.readMessage(this.serviceState);
                            break;
                        case 42:
                            if (this.imsConnectionState == null) {
                                this.imsConnectionState = new ImsConnectionState();
                            }
                            input.readMessage(this.imsConnectionState);
                            break;
                        case 50:
                            if (this.imsCapabilities == null) {
                                this.imsCapabilities = new ImsCapabilities();
                            }
                            input.readMessage(this.imsCapabilities);
                            break;
                        case 58:
                            int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                            RilDataCall[] rilDataCallArr = this.dataCalls;
                            int i = rilDataCallArr == null ? 0 : rilDataCallArr.length;
                            RilDataCall[] newArray = new RilDataCall[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.dataCalls, 0, newArray, 0, i);
                            }
                            while (i < newArray.length - 1) {
                                newArray[i] = new RilDataCall();
                                input.readMessage(newArray[i]);
                                input.readTag();
                                i++;
                            }
                            newArray[i] = new RilDataCall();
                            input.readMessage(newArray[i]);
                            this.dataCalls = newArray;
                            break;
                        case 64:
                            int initialPos3 = input.getPosition();
                            int value3 = input.readInt32();
                            if (value3 != 0 && value3 != 1 && value3 != 2) {
                                input.rewindToPosition(initialPos3);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.format = value3;
                                break;
                            }
                        case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                            int initialPos4 = input.getPosition();
                            int value4 = input.readInt32();
                            if (value4 != 0 && value4 != 1 && value4 != 2 && value4 != 3) {
                                input.rewindToPosition(initialPos4);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.tech = value4;
                                break;
                            }
                        case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                            this.errorCode = input.readInt32();
                            break;
                        case CallFailCause.INCOMPATIBLE_DESTINATION /* 88 */:
                            int initialPos5 = input.getPosition();
                            int value5 = input.readInt32();
                            switch (value5) {
                                default:
                                    switch (value5) {
                                        case 36:
                                        case 37:
                                        case 38:
                                        case 39:
                                        case 40:
                                        case 41:
                                        case 42:
                                        case 43:
                                        case 44:
                                        case 45:
                                        case 46:
                                        case 47:
                                        case 48:
                                        case 49:
                                        case 50:
                                        case 51:
                                        case 52:
                                        case 53:
                                        case 54:
                                        case 55:
                                        case 56:
                                        case 57:
                                        case 58:
                                        case 59:
                                        case 60:
                                        case RilErrno.RIL_E_NETWORK_NOT_READY /* 61 */:
                                        case 62:
                                        case 63:
                                        case 64:
                                        case 65:
                                        case 66:
                                        case RilErrno.RIL_E_INVALID_RESPONSE /* 67 */:
                                            break;
                                        default:
                                            input.rewindToPosition(initialPos5);
                                            storeUnknownField(input, tag);
                                            break;
                                    }
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                case 10:
                                case 11:
                                case 12:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 17:
                                case 18:
                                case 19:
                                case 20:
                                case 21:
                                case 22:
                                case 23:
                                case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                    this.error = value5;
                                    break;
                            }
                        case 96:
                            this.rilRequestId = input.readInt32();
                            break;
                        case 106:
                            if (this.cellBroadcastMessage == null) {
                                this.cellBroadcastMessage = new CBMessage();
                            }
                            input.readMessage(this.cellBroadcastMessage);
                            break;
                        case 112:
                            int initialPos6 = input.getPosition();
                            int value6 = input.readInt32();
                            if (value6 != 0 && value6 != 1 && value6 != 2 && value6 != 3 && value6 != 4) {
                                input.rewindToPosition(initialPos6);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.imsError = value6;
                                break;
                            }
                        case TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /* 122 */:
                            if (this.incompleteSms == null) {
                                this.incompleteSms = new IncompleteSms();
                            }
                            input.readMessage(this.incompleteSms);
                            break;
                        case 128:
                            int initialPos7 = input.getPosition();
                            int value7 = input.readInt32();
                            if (value7 != 0 && value7 != 1 && value7 != 2 && value7 != 3 && value7 != 4) {
                                input.rewindToPosition(initialPos7);
                                storeUnknownField(input, tag);
                                break;
                            } else {
                                this.smsType = value7;
                                break;
                            }
                        case 136:
                            this.blocked = input.readBool();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            } else {
                                return this;
                            }
                    }
                }
            }

            public static Event parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
                return (Event) MessageNano.mergeFrom(new Event(), data);
            }

            public static Event parseFrom(CodedInputByteBufferNano input) throws IOException {
                return new Event().mergeFrom(input);
            }
        }

        public static SmsSession[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new SmsSession[0];
                    }
                }
            }
            return _emptyArray;
        }

        public SmsSession() {
            clear();
        }

        public SmsSession clear() {
            this.startTimeMinutes = 0;
            this.phoneId = 0;
            this.events = Event.emptyArray();
            this.eventsDropped = false;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int i = this.startTimeMinutes;
            if (i != 0) {
                output.writeInt32(1, i);
            }
            int i2 = this.phoneId;
            if (i2 != 0) {
                output.writeInt32(2, i2);
            }
            Event[] eventArr = this.events;
            if (eventArr != null && eventArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Event[] eventArr2 = this.events;
                    if (i3 >= eventArr2.length) {
                        break;
                    }
                    Event element = eventArr2[i3];
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                    i3++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                output.writeBool(4, z);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            int size = super.computeSerializedSize();
            int i = this.startTimeMinutes;
            if (i != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, i);
            }
            int i2 = this.phoneId;
            if (i2 != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, i2);
            }
            Event[] eventArr = this.events;
            if (eventArr != null && eventArr.length > 0) {
                int i3 = 0;
                while (true) {
                    Event[] eventArr2 = this.events;
                    if (i3 >= eventArr2.length) {
                        break;
                    }
                    Event element = eventArr2[i3];
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                    i3++;
                }
            }
            boolean z = this.eventsDropped;
            if (z) {
                return size + CodedOutputByteBufferNano.computeBoolSize(4, z);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public SmsSession mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                if (tag == 0) {
                    return this;
                }
                if (tag == 8) {
                    this.startTimeMinutes = input.readInt32();
                } else if (tag == 16) {
                    this.phoneId = input.readInt32();
                } else if (tag == 26) {
                    int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                    Event[] eventArr = this.events;
                    int i = eventArr == null ? 0 : eventArr.length;
                    Event[] newArray = new Event[(i + arrayLength)];
                    if (i != 0) {
                        System.arraycopy(this.events, 0, newArray, 0, i);
                    }
                    while (i < newArray.length - 1) {
                        newArray[i] = new Event();
                        input.readMessage(newArray[i]);
                        input.readTag();
                        i++;
                    }
                    newArray[i] = new Event();
                    input.readMessage(newArray[i]);
                    this.events = newArray;
                } else if (tag == 32) {
                    this.eventsDropped = input.readBool();
                } else if (!storeUnknownField(input, tag)) {
                    return this;
                }
            }
        }

        public static SmsSession parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (SmsSession) MessageNano.mergeFrom(new SmsSession(), data);
        }

        public static SmsSession parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new SmsSession().mergeFrom(input);
        }
    }

    public static final class ModemPowerStats extends ExtendableMessageNano<ModemPowerStats> {
        private static volatile ModemPowerStats[] _emptyArray;
        public long cellularKernelActiveTimeMs;
        public double energyConsumedMah;
        public long idleTimeMs;
        public long loggingDurationMs;
        public double monitoredRailEnergyConsumedMah;
        public long numBytesRx;
        public long numBytesTx;
        public long numPacketsRx;
        public long numPacketsTx;
        public long rxTimeMs;
        public long sleepTimeMs;
        public long[] timeInRatMs;
        public long[] timeInRxSignalStrengthLevelMs;
        public long timeInVeryPoorRxSignalLevelMs;
        public long[] txTimeMs;

        public static ModemPowerStats[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ModemPowerStats[0];
                    }
                }
            }
            return _emptyArray;
        }

        public ModemPowerStats() {
            clear();
        }

        public ModemPowerStats clear() {
            this.loggingDurationMs = 0;
            this.energyConsumedMah = 0.0d;
            this.numPacketsTx = 0;
            this.cellularKernelActiveTimeMs = 0;
            this.timeInVeryPoorRxSignalLevelMs = 0;
            this.sleepTimeMs = 0;
            this.idleTimeMs = 0;
            this.rxTimeMs = 0;
            this.txTimeMs = WireFormatNano.EMPTY_LONG_ARRAY;
            this.numBytesTx = 0;
            this.numPacketsRx = 0;
            this.numBytesRx = 0;
            this.timeInRatMs = WireFormatNano.EMPTY_LONG_ARRAY;
            this.timeInRxSignalStrengthLevelMs = WireFormatNano.EMPTY_LONG_ARRAY;
            this.monitoredRailEnergyConsumedMah = 0.0d;
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            long j = this.loggingDurationMs;
            if (j != 0) {
                output.writeInt64(1, j);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(2, this.energyConsumedMah);
            }
            long j2 = this.numPacketsTx;
            if (j2 != 0) {
                output.writeInt64(3, j2);
            }
            long j3 = this.cellularKernelActiveTimeMs;
            if (j3 != 0) {
                output.writeInt64(4, j3);
            }
            long j4 = this.timeInVeryPoorRxSignalLevelMs;
            if (j4 != 0) {
                output.writeInt64(5, j4);
            }
            long j5 = this.sleepTimeMs;
            if (j5 != 0) {
                output.writeInt64(6, j5);
            }
            long j6 = this.idleTimeMs;
            if (j6 != 0) {
                output.writeInt64(7, j6);
            }
            long j7 = this.rxTimeMs;
            if (j7 != 0) {
                output.writeInt64(8, j7);
            }
            long[] jArr = this.txTimeMs;
            if (jArr != null && jArr.length > 0) {
                int i = 0;
                while (true) {
                    long[] jArr2 = this.txTimeMs;
                    if (i >= jArr2.length) {
                        break;
                    }
                    output.writeInt64(9, jArr2[i]);
                    i++;
                }
            }
            long j8 = this.numBytesTx;
            if (j8 != 0) {
                output.writeInt64(10, j8);
            }
            long j9 = this.numPacketsRx;
            if (j9 != 0) {
                output.writeInt64(11, j9);
            }
            long j10 = this.numBytesRx;
            if (j10 != 0) {
                output.writeInt64(12, j10);
            }
            long[] jArr3 = this.timeInRatMs;
            if (jArr3 != null && jArr3.length > 0) {
                int i2 = 0;
                while (true) {
                    long[] jArr4 = this.timeInRatMs;
                    if (i2 >= jArr4.length) {
                        break;
                    }
                    output.writeInt64(13, jArr4[i2]);
                    i2++;
                }
            }
            long[] jArr5 = this.timeInRxSignalStrengthLevelMs;
            if (jArr5 != null && jArr5.length > 0) {
                int i3 = 0;
                while (true) {
                    long[] jArr6 = this.timeInRxSignalStrengthLevelMs;
                    if (i3 >= jArr6.length) {
                        break;
                    }
                    output.writeInt64(14, jArr6[i3]);
                    i3++;
                }
            }
            if (Double.doubleToLongBits(this.monitoredRailEnergyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                output.writeDouble(15, this.monitoredRailEnergyConsumedMah);
            }
            super.writeTo(output);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.telephony.protobuf.nano.ExtendableMessageNano, com.android.internal.telephony.protobuf.nano.MessageNano
        public int computeSerializedSize() {
            long[] jArr;
            long[] jArr2;
            long[] jArr3;
            int size = super.computeSerializedSize();
            long j = this.loggingDurationMs;
            if (j != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, j);
            }
            if (Double.doubleToLongBits(this.energyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                size += CodedOutputByteBufferNano.computeDoubleSize(2, this.energyConsumedMah);
            }
            long j2 = this.numPacketsTx;
            if (j2 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(3, j2);
            }
            long j3 = this.cellularKernelActiveTimeMs;
            if (j3 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(4, j3);
            }
            long j4 = this.timeInVeryPoorRxSignalLevelMs;
            if (j4 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(5, j4);
            }
            long j5 = this.sleepTimeMs;
            if (j5 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(6, j5);
            }
            long j6 = this.idleTimeMs;
            if (j6 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(7, j6);
            }
            long j7 = this.rxTimeMs;
            if (j7 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(8, j7);
            }
            long[] jArr4 = this.txTimeMs;
            if (jArr4 != null && jArr4.length > 0) {
                int dataSize = 0;
                int i = 0;
                while (true) {
                    jArr3 = this.txTimeMs;
                    if (i >= jArr3.length) {
                        break;
                    }
                    dataSize += CodedOutputByteBufferNano.computeInt64SizeNoTag(jArr3[i]);
                    i++;
                }
                size = size + dataSize + (jArr3.length * 1);
            }
            long j8 = this.numBytesTx;
            if (j8 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(10, j8);
            }
            long j9 = this.numPacketsRx;
            if (j9 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(11, j9);
            }
            long j10 = this.numBytesRx;
            if (j10 != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(12, j10);
            }
            long[] jArr5 = this.timeInRatMs;
            if (jArr5 != null && jArr5.length > 0) {
                int dataSize2 = 0;
                int i2 = 0;
                while (true) {
                    jArr2 = this.timeInRatMs;
                    if (i2 >= jArr2.length) {
                        break;
                    }
                    dataSize2 += CodedOutputByteBufferNano.computeInt64SizeNoTag(jArr2[i2]);
                    i2++;
                }
                size = size + dataSize2 + (jArr2.length * 1);
            }
            long[] jArr6 = this.timeInRxSignalStrengthLevelMs;
            if (jArr6 != null && jArr6.length > 0) {
                int dataSize3 = 0;
                int i3 = 0;
                while (true) {
                    jArr = this.timeInRxSignalStrengthLevelMs;
                    if (i3 >= jArr.length) {
                        break;
                    }
                    dataSize3 += CodedOutputByteBufferNano.computeInt64SizeNoTag(jArr[i3]);
                    i3++;
                }
                size = size + dataSize3 + (jArr.length * 1);
            }
            if (Double.doubleToLongBits(this.monitoredRailEnergyConsumedMah) != Double.doubleToLongBits(0.0d)) {
                return size + CodedOutputByteBufferNano.computeDoubleSize(15, this.monitoredRailEnergyConsumedMah);
            }
            return size;
        }

        @Override // com.android.internal.telephony.protobuf.nano.MessageNano
        public ModemPowerStats mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.loggingDurationMs = input.readInt64();
                        break;
                    case 17:
                        this.energyConsumedMah = input.readDouble();
                        break;
                    case RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                        this.numPacketsTx = input.readInt64();
                        break;
                    case 32:
                        this.cellularKernelActiveTimeMs = input.readInt64();
                        break;
                    case 40:
                        this.timeInVeryPoorRxSignalLevelMs = input.readInt64();
                        break;
                    case 48:
                        this.sleepTimeMs = input.readInt64();
                        break;
                    case 56:
                        this.idleTimeMs = input.readInt64();
                        break;
                    case 64:
                        this.rxTimeMs = input.readInt64();
                        break;
                    case RadioNVItems.RIL_NV_LTE_BAND_ENABLE_26 /* 72 */:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 72);
                        long[] jArr = this.txTimeMs;
                        int i = jArr == null ? 0 : jArr.length;
                        long[] newArray = new long[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.txTimeMs, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt64();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt64();
                        this.txTimeMs = newArray;
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /* 74 */:
                        int limit = input.pushLimit(input.readRawVarint32());
                        int arrayLength2 = 0;
                        int startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt64();
                            arrayLength2++;
                        }
                        input.rewindToPosition(startPos);
                        long[] jArr2 = this.txTimeMs;
                        int i2 = jArr2 == null ? 0 : jArr2.length;
                        long[] newArray2 = new long[(i2 + arrayLength2)];
                        if (i2 != 0) {
                            System.arraycopy(this.txTimeMs, 0, newArray2, 0, i2);
                        }
                        while (i2 < newArray2.length) {
                            newArray2[i2] = input.readInt64();
                            i2++;
                        }
                        this.txTimeMs = newArray2;
                        input.popLimit(limit);
                        break;
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /* 80 */:
                        this.numBytesTx = input.readInt64();
                        break;
                    case CallFailCause.INCOMPATIBLE_DESTINATION /* 88 */:
                        this.numPacketsRx = input.readInt64();
                        break;
                    case 96:
                        this.numBytesRx = input.readInt64();
                        break;
                    case 104:
                        int arrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(input, 104);
                        long[] jArr3 = this.timeInRatMs;
                        int i3 = jArr3 == null ? 0 : jArr3.length;
                        long[] newArray3 = new long[(i3 + arrayLength3)];
                        if (i3 != 0) {
                            System.arraycopy(this.timeInRatMs, 0, newArray3, 0, i3);
                        }
                        while (i3 < newArray3.length - 1) {
                            newArray3[i3] = input.readInt64();
                            input.readTag();
                            i3++;
                        }
                        newArray3[i3] = input.readInt64();
                        this.timeInRatMs = newArray3;
                        break;
                    case 106:
                        int limit2 = input.pushLimit(input.readRawVarint32());
                        int arrayLength4 = 0;
                        int startPos2 = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt64();
                            arrayLength4++;
                        }
                        input.rewindToPosition(startPos2);
                        long[] jArr4 = this.timeInRatMs;
                        int i4 = jArr4 == null ? 0 : jArr4.length;
                        long[] newArray4 = new long[(i4 + arrayLength4)];
                        if (i4 != 0) {
                            System.arraycopy(this.timeInRatMs, 0, newArray4, 0, i4);
                        }
                        while (i4 < newArray4.length) {
                            newArray4[i4] = input.readInt64();
                            i4++;
                        }
                        this.timeInRatMs = newArray4;
                        input.popLimit(limit2);
                        break;
                    case 112:
                        int arrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(input, 112);
                        long[] jArr5 = this.timeInRxSignalStrengthLevelMs;
                        int i5 = jArr5 == null ? 0 : jArr5.length;
                        long[] newArray5 = new long[(i5 + arrayLength5)];
                        if (i5 != 0) {
                            System.arraycopy(this.timeInRxSignalStrengthLevelMs, 0, newArray5, 0, i5);
                        }
                        while (i5 < newArray5.length - 1) {
                            newArray5[i5] = input.readInt64();
                            input.readTag();
                            i5++;
                        }
                        newArray5[i5] = input.readInt64();
                        this.timeInRxSignalStrengthLevelMs = newArray5;
                        break;
                    case 114:
                        int limit3 = input.pushLimit(input.readRawVarint32());
                        int arrayLength6 = 0;
                        int startPos3 = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt64();
                            arrayLength6++;
                        }
                        input.rewindToPosition(startPos3);
                        long[] jArr6 = this.timeInRxSignalStrengthLevelMs;
                        int i6 = jArr6 == null ? 0 : jArr6.length;
                        long[] newArray6 = new long[(i6 + arrayLength6)];
                        if (i6 != 0) {
                            System.arraycopy(this.timeInRxSignalStrengthLevelMs, 0, newArray6, 0, i6);
                        }
                        while (i6 < newArray6.length) {
                            newArray6[i6] = input.readInt64();
                            i6++;
                        }
                        this.timeInRxSignalStrengthLevelMs = newArray6;
                        input.popLimit(limit3);
                        break;
                    case TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /* 121 */:
                        this.monitoredRailEnergyConsumedMah = input.readDouble();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        } else {
                            return this;
                        }
                }
            }
        }

        public static ModemPowerStats parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (ModemPowerStats) MessageNano.mergeFrom(new ModemPowerStats(), data);
        }

        public static ModemPowerStats parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new ModemPowerStats().mergeFrom(input);
        }
    }
}
