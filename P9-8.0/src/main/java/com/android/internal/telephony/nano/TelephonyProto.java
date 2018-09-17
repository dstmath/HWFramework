package com.android.internal.telephony.nano;

import android.hardware.radio.V1_0.RadioError;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.RadioNVItems;
import com.android.internal.telephony.protobuf.nano.CodedInputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.CodedOutputByteBufferNano;
import com.android.internal.telephony.protobuf.nano.ExtendableMessageNano;
import com.android.internal.telephony.protobuf.nano.InternalNano;
import com.android.internal.telephony.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.internal.telephony.protobuf.nano.MessageNano;
import com.android.internal.telephony.protobuf.nano.WireFormatNano;
import java.io.IOException;

public interface TelephonyProto {

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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.voiceOverLte) {
                output.writeBool(1, this.voiceOverLte);
            }
            if (this.voiceOverWifi) {
                output.writeBool(2, this.voiceOverWifi);
            }
            if (this.videoOverLte) {
                output.writeBool(3, this.videoOverLte);
            }
            if (this.videoOverWifi) {
                output.writeBool(4, this.videoOverWifi);
            }
            if (this.utOverLte) {
                output.writeBool(5, this.utOverLte);
            }
            if (this.utOverWifi) {
                output.writeBool(6, this.utOverWifi);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.voiceOverLte) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, this.voiceOverLte);
            }
            if (this.voiceOverWifi) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, this.voiceOverWifi);
            }
            if (this.videoOverLte) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.videoOverLte);
            }
            if (this.videoOverWifi) {
                size += CodedOutputByteBufferNano.computeBoolSize(4, this.videoOverWifi);
            }
            if (this.utOverLte) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.utOverLte);
            }
            if (this.utOverWifi) {
                return size + CodedOutputByteBufferNano.computeBoolSize(6, this.utOverWifi);
            }
            return size;
        }

        public ImsCapabilities mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.voiceOverLte = input.readBool();
                        break;
                    case 16:
                        this.voiceOverWifi = input.readBool();
                        break;
                    case 24:
                        this.videoOverLte = input.readBool();
                        break;
                    case 32:
                        this.videoOverWifi = input.readBool();
                        break;
                    case 40:
                        this.utOverLte = input.readBool();
                        break;
                    case RadioError.NO_SMS_TO_ACK /*48*/:
                        this.utOverWifi = input.readBool();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.state != 0) {
                output.writeInt32(1, this.state);
            }
            if (this.reasonInfo != null) {
                output.writeMessage(2, this.reasonInfo);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.state != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.state);
            }
            if (this.reasonInfo != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(2, this.reasonInfo);
            }
            return size;
        }

        public ImsConnectionState mergeFrom(CodedInputByteBufferNano input) throws IOException {
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
                                this.state = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 18:
                        if (this.reasonInfo == null) {
                            this.reasonInfo = new ImsReasonInfo();
                        }
                        input.readMessage(this.reasonInfo);
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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
            this.extraMessage = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.reasonCode != 0) {
                output.writeInt32(1, this.reasonCode);
            }
            if (this.extraCode != 0) {
                output.writeInt32(2, this.extraCode);
            }
            if (!this.extraMessage.equals("")) {
                output.writeString(3, this.extraMessage);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.reasonCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.reasonCode);
            }
            if (this.extraCode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.extraCode);
            }
            if (this.extraMessage.equals("")) {
                return size;
            }
            return size + CodedOutputByteBufferNano.computeStringSize(3, this.extraMessage);
        }

        public ImsReasonInfo mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.reasonCode = input.readInt32();
                        break;
                    case 16:
                        this.extraCode = input.readInt32();
                        break;
                    case 26:
                        this.extraMessage = input.readString();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

    public interface PdpType {
        public static final int PDP_TYPE_IP = 1;
        public static final int PDP_TYPE_IPV4V6 = 3;
        public static final int PDP_TYPE_IPV6 = 2;
        public static final int PDP_TYPE_PPP = 4;
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

    public static final class RilDataCall extends ExtendableMessageNano<RilDataCall> {
        private static volatile RilDataCall[] _emptyArray;
        public int cid;
        public String iframe;
        public int type;

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
            this.iframe = "";
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.cid != 0) {
                output.writeInt32(1, this.cid);
            }
            if (this.type != 0) {
                output.writeInt32(2, this.type);
            }
            if (!this.iframe.equals("")) {
                output.writeString(3, this.iframe);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.cid != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.cid);
            }
            if (this.type != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.type);
            }
            if (this.iframe.equals("")) {
                return size;
            }
            return size + CodedOutputByteBufferNano.computeStringSize(3, this.iframe);
        }

        public RilDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.cid = input.readInt32();
                        break;
                    case 16:
                        int initialPos = input.getPosition();
                        int value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                this.type = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 26:
                        this.iframe = input.readString();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

    public interface RilErrno {
        public static final int RIL_E_CANCELLED = 8;
        public static final int RIL_E_DIAL_MODIFIED_TO_DIAL = 21;
        public static final int RIL_E_DIAL_MODIFIED_TO_SS = 20;
        public static final int RIL_E_DIAL_MODIFIED_TO_USSD = 19;
        public static final int RIL_E_FDN_CHECK_FAILURE = 15;
        public static final int RIL_E_GENERIC_FAILURE = 3;
        public static final int RIL_E_ILLEGAL_SIM_OR_ME = 16;
        public static final int RIL_E_LCE_NOT_SUPPORTED = 36;
        public static final int RIL_E_LCE_NOT_SUPPORTED_NEW = 37;
        public static final int RIL_E_MISSING_RESOURCE = 17;
        public static final int RIL_E_MODE_NOT_SUPPORTED = 14;
        public static final int RIL_E_NO_SUCH_ELEMENT = 18;
        public static final int RIL_E_OP_NOT_ALLOWED_BEFORE_REG_TO_NW = 10;
        public static final int RIL_E_OP_NOT_ALLOWED_DURING_VOICE_CALL = 9;
        public static final int RIL_E_PASSWORD_INCORRECT = 4;
        public static final int RIL_E_RADIO_NOT_AVAILABLE = 2;
        public static final int RIL_E_REQUEST_NOT_SUPPORTED = 7;
        public static final int RIL_E_SIM_ABSENT = 12;
        public static final int RIL_E_SIM_PIN2 = 5;
        public static final int RIL_E_SIM_PUK2 = 6;
        public static final int RIL_E_SMS_SEND_FAIL_RETRY = 11;
        public static final int RIL_E_SS_MODIFIED_TO_DIAL = 25;
        public static final int RIL_E_SS_MODIFIED_TO_SS = 28;
        public static final int RIL_E_SS_MODIFIED_TO_USSD = 26;
        public static final int RIL_E_SUBSCRIPTION_NOT_AVAILABLE = 13;
        public static final int RIL_E_SUBSCRIPTION_NOT_SUPPORTED = 27;
        public static final int RIL_E_SUCCESS = 1;
        public static final int RIL_E_UNKNOWN = 0;
        public static final int RIL_E_USSD_MODIFIED_TO_DIAL = 22;
        public static final int RIL_E_USSD_MODIFIED_TO_SS = 23;
        public static final int RIL_E_USSD_MODIFIED_TO_USSD = 24;
    }

    public static final class SmsSession extends ExtendableMessageNano<SmsSession> {
        private static volatile SmsSession[] _emptyArray;
        public Event[] events;
        public boolean eventsDropped;
        public int phoneId;
        public int startTimeMinutes;

        public static final class Event extends ExtendableMessageNano<Event> {
            private static volatile Event[] _emptyArray;
            public RilDataCall[] dataCalls;
            public int delay;
            public int error;
            public int errorCode;
            public int format;
            public ImsCapabilities imsCapabilities;
            public ImsConnectionState imsConnectionState;
            public int rilRequestId;
            public TelephonyServiceState serviceState;
            public TelephonySettings settings;
            public int tech;
            public int type;

            public interface Format {
                public static final int SMS_FORMAT_3GPP = 1;
                public static final int SMS_FORMAT_3GPP2 = 2;
                public static final int SMS_FORMAT_UNKNOWN = 0;
            }

            public interface Tech {
                public static final int SMS_CDMA = 2;
                public static final int SMS_GSM = 1;
                public static final int SMS_IMS = 3;
                public static final int SMS_UNKNOWN = 0;
            }

            public interface Type {
                public static final int DATA_CALL_LIST_CHANGED = 5;
                public static final int EVENT_UNKNOWN = 0;
                public static final int IMS_CAPABILITIES_CHANGED = 4;
                public static final int IMS_CONNECTION_STATE_CHANGED = 3;
                public static final int RIL_SERVICE_STATE_CHANGED = 2;
                public static final int SETTINGS_CHANGED = 1;
                public static final int SMS_RECEIVED = 8;
                public static final int SMS_SEND = 6;
                public static final int SMS_SEND_RESULT = 7;
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
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.type != 0) {
                    output.writeInt32(1, this.type);
                }
                if (this.delay != 0) {
                    output.writeInt32(2, this.delay);
                }
                if (this.settings != null) {
                    output.writeMessage(3, this.settings);
                }
                if (this.serviceState != null) {
                    output.writeMessage(4, this.serviceState);
                }
                if (this.imsConnectionState != null) {
                    output.writeMessage(5, this.imsConnectionState);
                }
                if (this.imsCapabilities != null) {
                    output.writeMessage(6, this.imsCapabilities);
                }
                if (this.dataCalls != null && this.dataCalls.length > 0) {
                    for (RilDataCall element : this.dataCalls) {
                        if (element != null) {
                            output.writeMessage(7, element);
                        }
                    }
                }
                if (this.format != 0) {
                    output.writeInt32(8, this.format);
                }
                if (this.tech != 0) {
                    output.writeInt32(9, this.tech);
                }
                if (this.errorCode != 0) {
                    output.writeInt32(10, this.errorCode);
                }
                if (this.error != 0) {
                    output.writeInt32(11, this.error);
                }
                if (this.rilRequestId != 0) {
                    output.writeInt32(12, this.rilRequestId);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.type != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.type);
                }
                if (this.delay != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, this.delay);
                }
                if (this.settings != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, this.settings);
                }
                if (this.serviceState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, this.serviceState);
                }
                if (this.imsConnectionState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, this.imsConnectionState);
                }
                if (this.imsCapabilities != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, this.imsCapabilities);
                }
                if (this.dataCalls != null && this.dataCalls.length > 0) {
                    for (RilDataCall element : this.dataCalls) {
                        if (element != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(7, element);
                        }
                    }
                }
                if (this.format != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(8, this.format);
                }
                if (this.tech != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(9, this.tech);
                }
                if (this.errorCode != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(10, this.errorCode);
                }
                if (this.error != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(11, this.error);
                }
                if (this.rilRequestId != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(12, this.rilRequestId);
                }
                return size;
            }

            public Event mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int initialPos;
                    int value;
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.type = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 16:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.delay = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
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
                            int i = this.dataCalls == null ? 0 : this.dataCalls.length;
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
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                    this.format = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 72:
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                    this.tech = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                            this.errorCode = input.readInt32();
                            break;
                        case 88:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                case 36:
                                case 37:
                                    this.error = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 96:
                            this.rilRequestId = input.readInt32();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
                            return this;
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.startTimeMinutes != 0) {
                output.writeInt32(1, this.startTimeMinutes);
            }
            if (this.phoneId != 0) {
                output.writeInt32(2, this.phoneId);
            }
            if (this.events != null && this.events.length > 0) {
                for (Event element : this.events) {
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                }
            }
            if (this.eventsDropped) {
                output.writeBool(4, this.eventsDropped);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.startTimeMinutes != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.startTimeMinutes);
            }
            if (this.phoneId != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.phoneId);
            }
            if (this.events != null && this.events.length > 0) {
                for (Event element : this.events) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                }
            }
            if (this.eventsDropped) {
                return size + CodedOutputByteBufferNano.computeBoolSize(4, this.eventsDropped);
            }
            return size;
        }

        public SmsSession mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.startTimeMinutes = input.readInt32();
                        break;
                    case 16:
                        this.phoneId = input.readInt32();
                        break;
                    case 26:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        int i = this.events == null ? 0 : this.events.length;
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
                        break;
                    case 32:
                        this.eventsDropped = input.readBool();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

    public static final class TelephonyCallSession extends ExtendableMessageNano<TelephonyCallSession> {
        private static volatile TelephonyCallSession[] _emptyArray;
        public Event[] events;
        public boolean eventsDropped;
        public int phoneId;
        public int startTimeMinutes;

        public static final class Event extends ExtendableMessageNano<Event> {
            private static volatile Event[] _emptyArray;
            public int callIndex;
            public int callState;
            public RilCall[] calls;
            public RilDataCall[] dataCalls;
            public int delay;
            public int error;
            public ImsCapabilities imsCapabilities;
            public int imsCommand;
            public ImsConnectionState imsConnectionState;
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

            public static final class RilCall extends ExtendableMessageNano<RilCall> {
                private static volatile RilCall[] _emptyArray;
                public int callEndReason;
                public int index;
                public boolean isMultiparty;
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
                    this.unknownFieldData = null;
                    this.cachedSize = -1;
                    return this;
                }

                public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                    if (this.index != 0) {
                        output.writeInt32(1, this.index);
                    }
                    if (this.state != 0) {
                        output.writeInt32(2, this.state);
                    }
                    if (this.type != 0) {
                        output.writeInt32(3, this.type);
                    }
                    if (this.callEndReason != 0) {
                        output.writeInt32(4, this.callEndReason);
                    }
                    if (this.isMultiparty) {
                        output.writeBool(5, this.isMultiparty);
                    }
                    super.writeTo(output);
                }

                protected int computeSerializedSize() {
                    int size = super.computeSerializedSize();
                    if (this.index != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(1, this.index);
                    }
                    if (this.state != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(2, this.state);
                    }
                    if (this.type != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(3, this.type);
                    }
                    if (this.callEndReason != 0) {
                        size += CodedOutputByteBufferNano.computeInt32Size(4, this.callEndReason);
                    }
                    if (this.isMultiparty) {
                        return size + CodedOutputByteBufferNano.computeBoolSize(5, this.isMultiparty);
                    }
                    return size;
                }

                public RilCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                    while (true) {
                        int tag = input.readTag();
                        int initialPos;
                        int value;
                        switch (tag) {
                            case 0:
                                return this;
                            case 8:
                                this.index = input.readInt32();
                                break;
                            case 16:
                                initialPos = input.getPosition();
                                value = input.readInt32();
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
                                        break;
                                    default:
                                        input.rewindToPosition(initialPos);
                                        storeUnknownField(input, tag);
                                        break;
                                }
                            case 24:
                                initialPos = input.getPosition();
                                value = input.readInt32();
                                switch (value) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        this.type = value;
                                        break;
                                    default:
                                        input.rewindToPosition(initialPos);
                                        storeUnknownField(input, tag);
                                        break;
                                }
                            case 32:
                                this.callEndReason = input.readInt32();
                                break;
                            case 40:
                                this.isMultiparty = input.readBool();
                                break;
                            default:
                                if (storeUnknownField(input, tag)) {
                                    break;
                                }
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
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.type != 0) {
                    output.writeInt32(1, this.type);
                }
                if (this.delay != 0) {
                    output.writeInt32(2, this.delay);
                }
                if (this.settings != null) {
                    output.writeMessage(3, this.settings);
                }
                if (this.serviceState != null) {
                    output.writeMessage(4, this.serviceState);
                }
                if (this.imsConnectionState != null) {
                    output.writeMessage(5, this.imsConnectionState);
                }
                if (this.imsCapabilities != null) {
                    output.writeMessage(6, this.imsCapabilities);
                }
                if (this.dataCalls != null && this.dataCalls.length > 0) {
                    for (RilDataCall element : this.dataCalls) {
                        if (element != null) {
                            output.writeMessage(7, element);
                        }
                    }
                }
                if (this.phoneState != 0) {
                    output.writeInt32(8, this.phoneState);
                }
                if (this.callState != 0) {
                    output.writeInt32(9, this.callState);
                }
                if (this.callIndex != 0) {
                    output.writeInt32(10, this.callIndex);
                }
                if (this.mergedCallIndex != 0) {
                    output.writeInt32(11, this.mergedCallIndex);
                }
                if (this.calls != null && this.calls.length > 0) {
                    for (RilCall element2 : this.calls) {
                        if (element2 != null) {
                            output.writeMessage(12, element2);
                        }
                    }
                }
                if (this.error != 0) {
                    output.writeInt32(13, this.error);
                }
                if (this.rilRequest != 0) {
                    output.writeInt32(14, this.rilRequest);
                }
                if (this.rilRequestId != 0) {
                    output.writeInt32(15, this.rilRequestId);
                }
                if (this.srvccState != 0) {
                    output.writeInt32(16, this.srvccState);
                }
                if (this.imsCommand != 0) {
                    output.writeInt32(17, this.imsCommand);
                }
                if (this.reasonInfo != null) {
                    output.writeMessage(18, this.reasonInfo);
                }
                if (this.srcAccessTech != -1) {
                    output.writeInt32(19, this.srcAccessTech);
                }
                if (this.targetAccessTech != -1) {
                    output.writeInt32(20, this.targetAccessTech);
                }
                if (this.nitzTimestampMillis != 0) {
                    output.writeInt64(21, this.nitzTimestampMillis);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.type != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.type);
                }
                if (this.delay != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, this.delay);
                }
                if (this.settings != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(3, this.settings);
                }
                if (this.serviceState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(4, this.serviceState);
                }
                if (this.imsConnectionState != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(5, this.imsConnectionState);
                }
                if (this.imsCapabilities != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(6, this.imsCapabilities);
                }
                if (this.dataCalls != null && this.dataCalls.length > 0) {
                    for (RilDataCall element : this.dataCalls) {
                        if (element != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(7, element);
                        }
                    }
                }
                if (this.phoneState != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(8, this.phoneState);
                }
                if (this.callState != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(9, this.callState);
                }
                if (this.callIndex != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(10, this.callIndex);
                }
                if (this.mergedCallIndex != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(11, this.mergedCallIndex);
                }
                if (this.calls != null && this.calls.length > 0) {
                    for (RilCall element2 : this.calls) {
                        if (element2 != null) {
                            size += CodedOutputByteBufferNano.computeMessageSize(12, element2);
                        }
                    }
                }
                if (this.error != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(13, this.error);
                }
                if (this.rilRequest != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(14, this.rilRequest);
                }
                if (this.rilRequestId != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(15, this.rilRequestId);
                }
                if (this.srvccState != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(16, this.srvccState);
                }
                if (this.imsCommand != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(17, this.imsCommand);
                }
                if (this.reasonInfo != null) {
                    size += CodedOutputByteBufferNano.computeMessageSize(18, this.reasonInfo);
                }
                if (this.srcAccessTech != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(19, this.srcAccessTech);
                }
                if (this.targetAccessTech != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(20, this.targetAccessTech);
                }
                if (this.nitzTimestampMillis != 0) {
                    return size + CodedOutputByteBufferNano.computeInt64Size(21, this.nitzTimestampMillis);
                }
                return size;
            }

            public Event mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int initialPos;
                    int value;
                    int arrayLength;
                    int i;
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 16:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.delay = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
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
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 58);
                            i = this.dataCalls == null ? 0 : this.dataCalls.length;
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
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                    this.phoneState = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 72:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.callState = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                            this.callIndex = input.readInt32();
                            break;
                        case 88:
                            this.mergedCallIndex = input.readInt32();
                            break;
                        case 98:
                            arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 98);
                            i = this.calls == null ? 0 : this.calls.length;
                            RilCall[] newArray2 = new RilCall[(i + arrayLength)];
                            if (i != 0) {
                                System.arraycopy(this.calls, 0, newArray2, 0, i);
                            }
                            while (i < newArray2.length - 1) {
                                newArray2[i] = new RilCall();
                                input.readMessage(newArray2[i]);
                                input.readTag();
                                i++;
                            }
                            newArray2[i] = new RilCall();
                            input.readMessage(newArray2[i]);
                            this.calls = newArray2;
                            break;
                        case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                case 36:
                                case 37:
                                    this.error = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 112:
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                    this.rilRequest = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 120:
                            this.rilRequestId = input.readInt32();
                            break;
                        case 128:
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    this.srvccState = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 136:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.imsCommand = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 146:
                            if (this.reasonInfo == null) {
                                this.reasonInfo = new ImsReasonInfo();
                            }
                            input.readMessage(this.reasonInfo);
                            break;
                        case 152:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.srcAccessTech = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 160:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    this.targetAccessTech = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 168:
                            this.nitzTimestampMillis = input.readInt64();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
                            return this;
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.startTimeMinutes != 0) {
                output.writeInt32(1, this.startTimeMinutes);
            }
            if (this.phoneId != 0) {
                output.writeInt32(2, this.phoneId);
            }
            if (this.events != null && this.events.length > 0) {
                for (Event element : this.events) {
                    if (element != null) {
                        output.writeMessage(3, element);
                    }
                }
            }
            if (this.eventsDropped) {
                output.writeBool(4, this.eventsDropped);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.startTimeMinutes != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.startTimeMinutes);
            }
            if (this.phoneId != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.phoneId);
            }
            if (this.events != null && this.events.length > 0) {
                for (Event element : this.events) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element);
                    }
                }
            }
            if (this.eventsDropped) {
                return size + CodedOutputByteBufferNano.computeBoolSize(4, this.eventsDropped);
            }
            return size;
        }

        public TelephonyCallSession mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.startTimeMinutes = input.readInt32();
                        break;
                    case 16:
                        this.phoneId = input.readInt32();
                        break;
                    case 26:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        int i = this.events == null ? 0 : this.events.length;
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
                        break;
                    case 32:
                        this.eventsDropped = input.readBool();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

    public static final class TelephonyEvent extends ExtendableMessageNano<TelephonyEvent> {
        private static volatile TelephonyEvent[] _emptyArray;
        public RilDataCall[] dataCalls;
        public int dataStallAction;
        public RilDeactivateDataCall deactivateDataCall;
        public int error;
        public ImsCapabilities imsCapabilities;
        public ImsConnectionState imsConnectionState;
        public ModemRestart modemRestart;
        public long nitzTimestampMillis;
        public int phoneId;
        public TelephonyServiceState serviceState;
        public TelephonySettings settings;
        public RilSetupDataCall setupDataCall;
        public RilSetupDataCallResponse setupDataCallResponse;
        public long timestampMillis;
        public int type;

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
                this.basebandVersion = "";
                this.reason = "";
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!this.basebandVersion.equals("")) {
                    output.writeString(1, this.basebandVersion);
                }
                if (!this.reason.equals("")) {
                    output.writeString(2, this.reason);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!this.basebandVersion.equals("")) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.basebandVersion);
                }
                if (this.reason.equals("")) {
                    return size;
                }
                return size + CodedOutputByteBufferNano.computeStringSize(2, this.reason);
            }

            public ModemRestart mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 10:
                            this.basebandVersion = input.readString();
                            break;
                        case 18:
                            this.reason = input.readString();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
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

        public static final class RilDeactivateDataCall extends ExtendableMessageNano<RilDeactivateDataCall> {
            private static volatile RilDeactivateDataCall[] _emptyArray;
            public int cid;
            public int reason;

            public interface DeactivateReason {
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

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.cid != 0) {
                    output.writeInt32(1, this.cid);
                }
                if (this.reason != 0) {
                    output.writeInt32(2, this.reason);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.cid != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.cid);
                }
                if (this.reason != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(2, this.reason);
                }
                return size;
            }

            public RilDeactivateDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            this.cid = input.readInt32();
                            break;
                        case 16:
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                    this.reason = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
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
                this.apn = "";
                this.type = 0;
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.rat != -1) {
                    output.writeInt32(1, this.rat);
                }
                if (this.dataProfile != 0) {
                    output.writeInt32(2, this.dataProfile);
                }
                if (!this.apn.equals("")) {
                    output.writeString(3, this.apn);
                }
                if (this.type != 0) {
                    output.writeInt32(4, this.type);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.rat != -1) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.rat);
                }
                if (this.dataProfile != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, this.dataProfile);
                }
                if (!this.apn.equals("")) {
                    size += CodedOutputByteBufferNano.computeStringSize(3, this.apn);
                }
                if (this.type != 0) {
                    return size + CodedOutputByteBufferNano.computeInt32Size(4, this.type);
                }
                return size;
            }

            public RilSetupDataCall mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    int initialPos;
                    int value;
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            initialPos = input.getPosition();
                            value = input.readInt32();
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
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 16:
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                case 7:
                                    this.dataProfile = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 26:
                            this.apn = input.readString();
                            break;
                        case 32:
                            initialPos = input.getPosition();
                            value = input.readInt32();
                            switch (value) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    this.type = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
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
                public static final int PDP_FAIL_ACTIVATION_REJECT_GGSN = 30;
                public static final int PDP_FAIL_ACTIVATION_REJECT_UNSPECIFIED = 31;
                public static final int PDP_FAIL_APN_TYPE_CONFLICT = 112;
                public static final int PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL = 122;
                public static final int PDP_FAIL_COMPANION_IFACE_IN_USE = 118;
                public static final int PDP_FAIL_CONDITIONAL_IE_ERROR = 100;
                public static final int PDP_FAIL_DATA_REGISTRATION_FAIL = -2;
                public static final int PDP_FAIL_EMERGENCY_IFACE_ONLY = 116;
                public static final int PDP_FAIL_EMM_ACCESS_BARRED = 115;
                public static final int PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY = 121;
                public static final int PDP_FAIL_ERROR_UNSPECIFIED = 65535;
                public static final int PDP_FAIL_ESM_INFO_NOT_RECEIVED = 53;
                public static final int PDP_FAIL_FEATURE_NOT_SUPP = 40;
                public static final int PDP_FAIL_FILTER_SEMANTIC_ERROR = 44;
                public static final int PDP_FAIL_FILTER_SYTAX_ERROR = 45;
                public static final int PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH = 120;
                public static final int PDP_FAIL_IFACE_MISMATCH = 117;
                public static final int PDP_FAIL_INSUFFICIENT_RESOURCES = 26;
                public static final int PDP_FAIL_INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN = 114;
                public static final int PDP_FAIL_INVALID_MANDATORY_INFO = 96;
                public static final int PDP_FAIL_INVALID_PCSCF_ADDR = 113;
                public static final int PDP_FAIL_INVALID_TRANSACTION_ID = 81;
                public static final int PDP_FAIL_IP_ADDRESS_MISMATCH = 119;
                public static final int PDP_FAIL_LLC_SNDCP = 25;
                public static final int PDP_FAIL_MAX_ACTIVE_PDP_CONTEXT_REACHED = 65;
                public static final int PDP_FAIL_MESSAGE_INCORRECT_SEMANTIC = 95;
                public static final int PDP_FAIL_MESSAGE_TYPE_UNSUPPORTED = 97;
                public static final int PDP_FAIL_MISSING_UKNOWN_APN = 27;
                public static final int PDP_FAIL_MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE = 101;
                public static final int PDP_FAIL_MSG_TYPE_NONCOMPATIBLE_STATE = 98;
                public static final int PDP_FAIL_MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED = 55;
                public static final int PDP_FAIL_NAS_SIGNALLING = 14;
                public static final int PDP_FAIL_NETWORK_FAILURE = 38;
                public static final int PDP_FAIL_NONE = 1;
                public static final int PDP_FAIL_NSAPI_IN_USE = 35;
                public static final int PDP_FAIL_ONLY_IPV4_ALLOWED = 50;
                public static final int PDP_FAIL_ONLY_IPV6_ALLOWED = 51;
                public static final int PDP_FAIL_ONLY_SINGLE_BEARER_ALLOWED = 52;
                public static final int PDP_FAIL_OPERATOR_BARRED = 8;
                public static final int PDP_FAIL_PDN_CONN_DOES_NOT_EXIST = 54;
                public static final int PDP_FAIL_PDP_WITHOUT_ACTIVE_TFT = 46;
                public static final int PDP_FAIL_PREF_RADIO_TECH_CHANGED = -4;
                public static final int PDP_FAIL_PROTOCOL_ERRORS = 111;
                public static final int PDP_FAIL_QOS_NOT_ACCEPTED = 37;
                public static final int PDP_FAIL_RADIO_POWER_OFF = -5;
                public static final int PDP_FAIL_REGULAR_DEACTIVATION = 36;
                public static final int PDP_FAIL_SERVICE_OPTION_NOT_SUBSCRIBED = 33;
                public static final int PDP_FAIL_SERVICE_OPTION_NOT_SUPPORTED = 32;
                public static final int PDP_FAIL_SERVICE_OPTION_OUT_OF_ORDER = 34;
                public static final int PDP_FAIL_SIGNAL_LOST = -3;
                public static final int PDP_FAIL_TETHERED_CALL_ACTIVE = -6;
                public static final int PDP_FAIL_TFT_SEMANTIC_ERROR = 41;
                public static final int PDP_FAIL_TFT_SYTAX_ERROR = 42;
                public static final int PDP_FAIL_UMTS_REACTIVATION_REQ = 39;
                public static final int PDP_FAIL_UNKNOWN = 0;
                public static final int PDP_FAIL_UNKNOWN_INFO_ELEMENT = 99;
                public static final int PDP_FAIL_UNKNOWN_PDP_ADDRESS_TYPE = 28;
                public static final int PDP_FAIL_UNKNOWN_PDP_CONTEXT = 43;
                public static final int PDP_FAIL_UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
                public static final int PDP_FAIL_USER_AUTHENTICATION = 29;
                public static final int PDP_FAIL_VOICE_REGISTRATION_FAIL = -1;
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

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (this.status != 0) {
                    output.writeInt32(1, this.status);
                }
                if (this.suggestedRetryTimeMillis != 0) {
                    output.writeInt32(2, this.suggestedRetryTimeMillis);
                }
                if (this.call != null) {
                    output.writeMessage(3, this.call);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (this.status != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(1, this.status);
                }
                if (this.suggestedRetryTimeMillis != 0) {
                    size += CodedOutputByteBufferNano.computeInt32Size(2, this.suggestedRetryTimeMillis);
                }
                if (this.call != null) {
                    return size + CodedOutputByteBufferNano.computeMessageSize(3, this.call);
                }
                return size;
            }

            public RilSetupDataCallResponse mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 8:
                            int initialPos = input.getPosition();
                            int value = input.readInt32();
                            switch (value) {
                                case -6:
                                case -5:
                                case -4:
                                case -3:
                                case -2:
                                case -1:
                                case 0:
                                case 1:
                                case 8:
                                case 14:
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
                                case 50:
                                case 51:
                                case 52:
                                case 53:
                                case 54:
                                case 55:
                                case 65:
                                case 66:
                                case 81:
                                case 95:
                                case 96:
                                case 97:
                                case 98:
                                case 99:
                                case 100:
                                case 101:
                                case 111:
                                case 112:
                                case 113:
                                case 114:
                                case 115:
                                case 116:
                                case 117:
                                case 118:
                                case 119:
                                case 120:
                                case 121:
                                case 122:
                                case 65535:
                                    this.status = value;
                                    break;
                                default:
                                    input.rewindToPosition(initialPos);
                                    storeUnknownField(input, tag);
                                    break;
                            }
                        case 16:
                            this.suggestedRetryTimeMillis = input.readInt32();
                            break;
                        case 26:
                            if (this.call == null) {
                                this.call = new RilDataCall();
                            }
                            input.readMessage(this.call);
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
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

        public interface Type {
            public static final int DATA_CALL_DEACTIVATE = 8;
            public static final int DATA_CALL_DEACTIVATE_RESPONSE = 9;
            public static final int DATA_CALL_LIST_CHANGED = 7;
            public static final int DATA_CALL_SETUP = 5;
            public static final int DATA_CALL_SETUP_RESPONSE = 6;
            public static final int DATA_STALL_ACTION = 10;
            public static final int IMS_CAPABILITIES_CHANGED = 4;
            public static final int IMS_CONNECTION_STATE_CHANGED = 3;
            public static final int MODEM_RESTART = 11;
            public static final int NITZ_TIME = 12;
            public static final int RIL_SERVICE_STATE_CHANGED = 2;
            public static final int SETTINGS_CHANGED = 1;
            public static final int UNKNOWN = 0;
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
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.timestampMillis != 0) {
                output.writeInt64(1, this.timestampMillis);
            }
            if (this.phoneId != 0) {
                output.writeInt32(2, this.phoneId);
            }
            if (this.type != 0) {
                output.writeInt32(3, this.type);
            }
            if (this.settings != null) {
                output.writeMessage(4, this.settings);
            }
            if (this.serviceState != null) {
                output.writeMessage(5, this.serviceState);
            }
            if (this.imsConnectionState != null) {
                output.writeMessage(6, this.imsConnectionState);
            }
            if (this.imsCapabilities != null) {
                output.writeMessage(7, this.imsCapabilities);
            }
            if (this.dataCalls != null && this.dataCalls.length > 0) {
                for (RilDataCall element : this.dataCalls) {
                    if (element != null) {
                        output.writeMessage(8, element);
                    }
                }
            }
            if (this.error != 0) {
                output.writeInt32(9, this.error);
            }
            if (this.setupDataCall != null) {
                output.writeMessage(10, this.setupDataCall);
            }
            if (this.setupDataCallResponse != null) {
                output.writeMessage(11, this.setupDataCallResponse);
            }
            if (this.deactivateDataCall != null) {
                output.writeMessage(12, this.deactivateDataCall);
            }
            if (this.dataStallAction != 0) {
                output.writeInt32(13, this.dataStallAction);
            }
            if (this.modemRestart != null) {
                output.writeMessage(14, this.modemRestart);
            }
            if (this.nitzTimestampMillis != 0) {
                output.writeInt64(15, this.nitzTimestampMillis);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.timestampMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.timestampMillis);
            }
            if (this.phoneId != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.phoneId);
            }
            if (this.type != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.type);
            }
            if (this.settings != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(4, this.settings);
            }
            if (this.serviceState != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(5, this.serviceState);
            }
            if (this.imsConnectionState != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, this.imsConnectionState);
            }
            if (this.imsCapabilities != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(7, this.imsCapabilities);
            }
            if (this.dataCalls != null && this.dataCalls.length > 0) {
                for (RilDataCall element : this.dataCalls) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(8, element);
                    }
                }
            }
            if (this.error != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(9, this.error);
            }
            if (this.setupDataCall != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(10, this.setupDataCall);
            }
            if (this.setupDataCallResponse != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(11, this.setupDataCallResponse);
            }
            if (this.deactivateDataCall != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(12, this.deactivateDataCall);
            }
            if (this.dataStallAction != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(13, this.dataStallAction);
            }
            if (this.modemRestart != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(14, this.modemRestart);
            }
            if (this.nitzTimestampMillis != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(15, this.nitzTimestampMillis);
            }
            return size;
        }

        public TelephonyEvent mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int initialPos;
                int value;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.timestampMillis = input.readInt64();
                        break;
                    case 16:
                        this.phoneId = input.readInt32();
                        break;
                    case 24:
                        initialPos = input.getPosition();
                        value = input.readInt32();
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
                                this.type = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
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
                        int i = this.dataCalls == null ? 0 : this.dataCalls.length;
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
                    case 72:
                        initialPos = input.getPosition();
                        value = input.readInt32();
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
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 36:
                            case 37:
                                this.error = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
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
                    case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
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
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.category != 0) {
                output.writeInt32(1, this.category);
            }
            if (this.id != 0) {
                output.writeInt32(2, this.id);
            }
            if (this.minTimeMillis != 0) {
                output.writeInt32(3, this.minTimeMillis);
            }
            if (this.maxTimeMillis != 0) {
                output.writeInt32(4, this.maxTimeMillis);
            }
            if (this.avgTimeMillis != 0) {
                output.writeInt32(5, this.avgTimeMillis);
            }
            if (this.count != 0) {
                output.writeInt32(6, this.count);
            }
            if (this.bucketCount != 0) {
                output.writeInt32(7, this.bucketCount);
            }
            if (this.bucketEndPoints != null && this.bucketEndPoints.length > 0) {
                for (int writeInt32 : this.bucketEndPoints) {
                    output.writeInt32(8, writeInt32);
                }
            }
            if (this.bucketCounters != null && this.bucketCounters.length > 0) {
                for (int writeInt322 : this.bucketCounters) {
                    output.writeInt32(9, writeInt322);
                }
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int dataSize;
            int size = super.computeSerializedSize();
            if (this.category != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(1, this.category);
            }
            if (this.id != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(2, this.id);
            }
            if (this.minTimeMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.minTimeMillis);
            }
            if (this.maxTimeMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.maxTimeMillis);
            }
            if (this.avgTimeMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.avgTimeMillis);
            }
            if (this.count != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(6, this.count);
            }
            if (this.bucketCount != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(7, this.bucketCount);
            }
            if (this.bucketEndPoints != null && this.bucketEndPoints.length > 0) {
                dataSize = 0;
                for (int element : this.bucketEndPoints) {
                    dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element);
                }
                size = (size + dataSize) + (this.bucketEndPoints.length * 1);
            }
            if (this.bucketCounters == null || this.bucketCounters.length <= 0) {
                return size;
            }
            dataSize = 0;
            for (int element2 : this.bucketCounters) {
                dataSize += CodedOutputByteBufferNano.computeInt32SizeNoTag(element2);
            }
            return (size + dataSize) + (this.bucketCounters.length * 1);
        }

        public TelephonyHistogram mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                int[] newArray;
                int limit;
                int startPos;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.category = input.readInt32();
                        break;
                    case 16:
                        this.id = input.readInt32();
                        break;
                    case 24:
                        this.minTimeMillis = input.readInt32();
                        break;
                    case 32:
                        this.maxTimeMillis = input.readInt32();
                        break;
                    case 40:
                        this.avgTimeMillis = input.readInt32();
                        break;
                    case RadioError.NO_SMS_TO_ACK /*48*/:
                        this.count = input.readInt32();
                        break;
                    case 56:
                        this.bucketCount = input.readInt32();
                        break;
                    case 64:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 64);
                        i = this.bucketEndPoints == null ? 0 : this.bucketEndPoints.length;
                        newArray = new int[(i + arrayLength)];
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
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.bucketEndPoints == null ? 0 : this.bucketEndPoints.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.bucketEndPoints, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.bucketEndPoints = newArray;
                        input.popLimit(limit);
                        break;
                    case 72:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 72);
                        i = this.bucketCounters == null ? 0 : this.bucketCounters.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.bucketCounters, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readInt32();
                            input.readTag();
                            i++;
                        }
                        newArray[i] = input.readInt32();
                        this.bucketCounters = newArray;
                        break;
                    case 74:
                        limit = input.pushLimit(input.readRawVarint32());
                        arrayLength = 0;
                        startPos = input.getPosition();
                        while (input.getBytesUntilLimit() > 0) {
                            input.readInt32();
                            arrayLength++;
                        }
                        input.rewindToPosition(startPos);
                        i = this.bucketCounters == null ? 0 : this.bucketCounters.length;
                        newArray = new int[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.bucketCounters, 0, newArray, 0, i);
                        }
                        while (i < newArray.length) {
                            newArray[i] = input.readInt32();
                            i++;
                        }
                        this.bucketCounters = newArray;
                        input.popLimit(limit);
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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

    public static final class TelephonyLog extends ExtendableMessageNano<TelephonyLog> {
        private static volatile TelephonyLog[] _emptyArray;
        public TelephonyCallSession[] callSessions;
        public Time endTime;
        public TelephonyEvent[] events;
        public boolean eventsDropped;
        public TelephonyHistogram[] histograms;
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
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.events != null && this.events.length > 0) {
                for (TelephonyEvent element : this.events) {
                    if (element != null) {
                        output.writeMessage(1, element);
                    }
                }
            }
            if (this.callSessions != null && this.callSessions.length > 0) {
                for (TelephonyCallSession element2 : this.callSessions) {
                    if (element2 != null) {
                        output.writeMessage(2, element2);
                    }
                }
            }
            if (this.smsSessions != null && this.smsSessions.length > 0) {
                for (SmsSession element3 : this.smsSessions) {
                    if (element3 != null) {
                        output.writeMessage(3, element3);
                    }
                }
            }
            if (this.histograms != null && this.histograms.length > 0) {
                for (TelephonyHistogram element4 : this.histograms) {
                    if (element4 != null) {
                        output.writeMessage(4, element4);
                    }
                }
            }
            if (this.eventsDropped) {
                output.writeBool(5, this.eventsDropped);
            }
            if (this.startTime != null) {
                output.writeMessage(6, this.startTime);
            }
            if (this.endTime != null) {
                output.writeMessage(7, this.endTime);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.events != null && this.events.length > 0) {
                for (TelephonyEvent element : this.events) {
                    if (element != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(1, element);
                    }
                }
            }
            if (this.callSessions != null && this.callSessions.length > 0) {
                for (TelephonyCallSession element2 : this.callSessions) {
                    if (element2 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(2, element2);
                    }
                }
            }
            if (this.smsSessions != null && this.smsSessions.length > 0) {
                for (SmsSession element3 : this.smsSessions) {
                    if (element3 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(3, element3);
                    }
                }
            }
            if (this.histograms != null && this.histograms.length > 0) {
                for (TelephonyHistogram element4 : this.histograms) {
                    if (element4 != null) {
                        size += CodedOutputByteBufferNano.computeMessageSize(4, element4);
                    }
                }
            }
            if (this.eventsDropped) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.eventsDropped);
            }
            if (this.startTime != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(6, this.startTime);
            }
            if (this.endTime != null) {
                return size + CodedOutputByteBufferNano.computeMessageSize(7, this.endTime);
            }
            return size;
        }

        public TelephonyLog mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int arrayLength;
                int i;
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 10);
                        i = this.events == null ? 0 : this.events.length;
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
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 18);
                        i = this.callSessions == null ? 0 : this.callSessions.length;
                        TelephonyCallSession[] newArray2 = new TelephonyCallSession[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.callSessions, 0, newArray2, 0, i);
                        }
                        while (i < newArray2.length - 1) {
                            newArray2[i] = new TelephonyCallSession();
                            input.readMessage(newArray2[i]);
                            input.readTag();
                            i++;
                        }
                        newArray2[i] = new TelephonyCallSession();
                        input.readMessage(newArray2[i]);
                        this.callSessions = newArray2;
                        break;
                    case 26:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 26);
                        i = this.smsSessions == null ? 0 : this.smsSessions.length;
                        SmsSession[] newArray3 = new SmsSession[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.smsSessions, 0, newArray3, 0, i);
                        }
                        while (i < newArray3.length - 1) {
                            newArray3[i] = new SmsSession();
                            input.readMessage(newArray3[i]);
                            input.readTag();
                            i++;
                        }
                        newArray3[i] = new SmsSession();
                        input.readMessage(newArray3[i]);
                        this.smsSessions = newArray3;
                        break;
                    case 34:
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        i = this.histograms == null ? 0 : this.histograms.length;
                        TelephonyHistogram[] newArray4 = new TelephonyHistogram[(i + arrayLength)];
                        if (i != 0) {
                            System.arraycopy(this.histograms, 0, newArray4, 0, i);
                        }
                        while (i < newArray4.length - 1) {
                            newArray4[i] = new TelephonyHistogram();
                            input.readMessage(newArray4[i]);
                            input.readTag();
                            i++;
                        }
                        newArray4[i] = new TelephonyHistogram();
                        input.readMessage(newArray4[i]);
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
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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

    public static final class TelephonyServiceState extends ExtendableMessageNano<TelephonyServiceState> {
        private static volatile TelephonyServiceState[] _emptyArray;
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
                this.alphaLong = "";
                this.alphaShort = "";
                this.numeric = "";
                this.unknownFieldData = null;
                this.cachedSize = -1;
                return this;
            }

            public void writeTo(CodedOutputByteBufferNano output) throws IOException {
                if (!this.alphaLong.equals("")) {
                    output.writeString(1, this.alphaLong);
                }
                if (!this.alphaShort.equals("")) {
                    output.writeString(2, this.alphaShort);
                }
                if (!this.numeric.equals("")) {
                    output.writeString(3, this.numeric);
                }
                super.writeTo(output);
            }

            protected int computeSerializedSize() {
                int size = super.computeSerializedSize();
                if (!this.alphaLong.equals("")) {
                    size += CodedOutputByteBufferNano.computeStringSize(1, this.alphaLong);
                }
                if (!this.alphaShort.equals("")) {
                    size += CodedOutputByteBufferNano.computeStringSize(2, this.alphaShort);
                }
                if (this.numeric.equals("")) {
                    return size;
                }
                return size + CodedOutputByteBufferNano.computeStringSize(3, this.numeric);
            }

            public TelephonyOperator mergeFrom(CodedInputByteBufferNano input) throws IOException {
                while (true) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            return this;
                        case 10:
                            this.alphaLong = input.readString();
                            break;
                        case 18:
                            this.alphaShort = input.readString();
                            break;
                        case 26:
                            this.numeric = input.readString();
                            break;
                        default:
                            if (storeUnknownField(input, tag)) {
                                break;
                            }
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
            this.unknownFieldData = null;
            this.cachedSize = -1;
            return this;
        }

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.voiceOperator != null) {
                output.writeMessage(1, this.voiceOperator);
            }
            if (this.dataOperator != null) {
                output.writeMessage(2, this.dataOperator);
            }
            if (this.voiceRoamingType != -1) {
                output.writeInt32(3, this.voiceRoamingType);
            }
            if (this.dataRoamingType != -1) {
                output.writeInt32(4, this.dataRoamingType);
            }
            if (this.voiceRat != -1) {
                output.writeInt32(5, this.voiceRat);
            }
            if (this.dataRat != -1) {
                output.writeInt32(6, this.dataRat);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.voiceOperator != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(1, this.voiceOperator);
            }
            if (this.dataOperator != null) {
                size += CodedOutputByteBufferNano.computeMessageSize(2, this.dataOperator);
            }
            if (this.voiceRoamingType != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(3, this.voiceRoamingType);
            }
            if (this.dataRoamingType != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.dataRoamingType);
            }
            if (this.voiceRat != -1) {
                size += CodedOutputByteBufferNano.computeInt32Size(5, this.voiceRat);
            }
            if (this.dataRat != -1) {
                return size + CodedOutputByteBufferNano.computeInt32Size(6, this.dataRat);
            }
            return size;
        }

        public TelephonyServiceState mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int initialPos;
                int value;
                switch (tag) {
                    case 0:
                        return this;
                    case 10:
                        if (this.voiceOperator == null) {
                            this.voiceOperator = new TelephonyOperator();
                        }
                        input.readMessage(this.voiceOperator);
                        break;
                    case 18:
                        if (this.dataOperator == null) {
                            this.dataOperator = new TelephonyOperator();
                        }
                        input.readMessage(this.dataOperator);
                        break;
                    case 24:
                        initialPos = input.getPosition();
                        value = input.readInt32();
                        switch (value) {
                            case -1:
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.voiceRoamingType = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 32:
                        initialPos = input.getPosition();
                        value = input.readInt32();
                        switch (value) {
                            case -1:
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.dataRoamingType = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 40:
                        initialPos = input.getPosition();
                        value = input.readInt32();
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
                                this.voiceRat = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case RadioError.NO_SMS_TO_ACK /*48*/:
                        initialPos = input.getPosition();
                        value = input.readInt32();
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
                                this.dataRat = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.isAirplaneMode) {
                output.writeBool(1, this.isAirplaneMode);
            }
            if (this.isCellularDataEnabled) {
                output.writeBool(2, this.isCellularDataEnabled);
            }
            if (this.isDataRoamingEnabled) {
                output.writeBool(3, this.isDataRoamingEnabled);
            }
            if (this.preferredNetworkMode != 0) {
                output.writeInt32(4, this.preferredNetworkMode);
            }
            if (this.isEnhanced4GLteModeEnabled) {
                output.writeBool(5, this.isEnhanced4GLteModeEnabled);
            }
            if (this.isWifiEnabled) {
                output.writeBool(6, this.isWifiEnabled);
            }
            if (this.isWifiCallingEnabled) {
                output.writeBool(7, this.isWifiCallingEnabled);
            }
            if (this.wifiCallingMode != 0) {
                output.writeInt32(8, this.wifiCallingMode);
            }
            if (this.isVtOverLteEnabled) {
                output.writeBool(9, this.isVtOverLteEnabled);
            }
            if (this.isVtOverWifiEnabled) {
                output.writeBool(10, this.isVtOverWifiEnabled);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.isAirplaneMode) {
                size += CodedOutputByteBufferNano.computeBoolSize(1, this.isAirplaneMode);
            }
            if (this.isCellularDataEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(2, this.isCellularDataEnabled);
            }
            if (this.isDataRoamingEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(3, this.isDataRoamingEnabled);
            }
            if (this.preferredNetworkMode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(4, this.preferredNetworkMode);
            }
            if (this.isEnhanced4GLteModeEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(5, this.isEnhanced4GLteModeEnabled);
            }
            if (this.isWifiEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(6, this.isWifiEnabled);
            }
            if (this.isWifiCallingEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(7, this.isWifiCallingEnabled);
            }
            if (this.wifiCallingMode != 0) {
                size += CodedOutputByteBufferNano.computeInt32Size(8, this.wifiCallingMode);
            }
            if (this.isVtOverLteEnabled) {
                size += CodedOutputByteBufferNano.computeBoolSize(9, this.isVtOverLteEnabled);
            }
            if (this.isVtOverWifiEnabled) {
                return size + CodedOutputByteBufferNano.computeBoolSize(10, this.isVtOverWifiEnabled);
            }
            return size;
        }

        public TelephonySettings mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                int initialPos;
                int value;
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.isAirplaneMode = input.readBool();
                        break;
                    case 16:
                        this.isCellularDataEnabled = input.readBool();
                        break;
                    case 24:
                        this.isDataRoamingEnabled = input.readBool();
                        break;
                    case 32:
                        initialPos = input.getPosition();
                        value = input.readInt32();
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
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 40:
                        this.isEnhanced4GLteModeEnabled = input.readBool();
                        break;
                    case RadioError.NO_SMS_TO_ACK /*48*/:
                        this.isWifiEnabled = input.readBool();
                        break;
                    case 56:
                        this.isWifiCallingEnabled = input.readBool();
                        break;
                    case 64:
                        initialPos = input.getPosition();
                        value = input.readInt32();
                        switch (value) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                                this.wifiCallingMode = value;
                                break;
                            default:
                                input.rewindToPosition(initialPos);
                                storeUnknownField(input, tag);
                                break;
                        }
                    case 72:
                        this.isVtOverLteEnabled = input.readBool();
                        break;
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                        this.isVtOverWifiEnabled = input.readBool();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
                        return this;
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

        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            if (this.systemTimestampMillis != 0) {
                output.writeInt64(1, this.systemTimestampMillis);
            }
            if (this.elapsedTimestampMillis != 0) {
                output.writeInt64(2, this.elapsedTimestampMillis);
            }
            super.writeTo(output);
        }

        protected int computeSerializedSize() {
            int size = super.computeSerializedSize();
            if (this.systemTimestampMillis != 0) {
                size += CodedOutputByteBufferNano.computeInt64Size(1, this.systemTimestampMillis);
            }
            if (this.elapsedTimestampMillis != 0) {
                return size + CodedOutputByteBufferNano.computeInt64Size(2, this.elapsedTimestampMillis);
            }
            return size;
        }

        public Time mergeFrom(CodedInputByteBufferNano input) throws IOException {
            while (true) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        return this;
                    case 8:
                        this.systemTimestampMillis = input.readInt64();
                        break;
                    case 16:
                        this.elapsedTimestampMillis = input.readInt64();
                        break;
                    default:
                        if (storeUnknownField(input, tag)) {
                            break;
                        }
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
}
