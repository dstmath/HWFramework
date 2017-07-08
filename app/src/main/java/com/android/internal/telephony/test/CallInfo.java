package com.android.internal.telephony.test;

import com.android.internal.telephony.ATParseEx;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.ProxyController;
import com.google.android.mms.pdu.PduPart;

/* compiled from: SimulatedGsmCallState */
class CallInfo {
    boolean mIsMT;
    boolean mIsMpty;
    String mNumber;
    State mState;
    int mTOA;

    /* compiled from: SimulatedGsmCallState */
    enum State {
        ;
        
        private final int mValue;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.test.CallInfo.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.test.CallInfo.State.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.test.CallInfo.State.<clinit>():void");
        }

        private State(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }
    }

    CallInfo(boolean isMT, State state, boolean isMpty, String number) {
        this.mIsMT = isMT;
        this.mState = state;
        this.mIsMpty = isMpty;
        this.mNumber = number;
        if (number.length() <= 0 || number.charAt(0) != '+') {
            this.mTOA = PduPart.P_DISPOSITION_ATTACHMENT;
        } else {
            this.mTOA = PduPart.P_SEC;
        }
    }

    static CallInfo createOutgoingCall(String number) {
        return new CallInfo(false, State.DIALING, false, number);
    }

    static CallInfo createIncomingCall(String number) {
        return new CallInfo(true, State.INCOMING, false, number);
    }

    String toCLCCLine(int index) {
        return "+CLCC: " + index + "," + (this.mIsMT ? ProxyController.MODEM_1 : ProxyController.MODEM_0) + "," + this.mState.value() + ",0," + (this.mIsMpty ? ProxyController.MODEM_1 : ProxyController.MODEM_0) + ",\"" + this.mNumber + "\"," + this.mTOA;
    }

    DriverCall toDriverCall(int index) {
        DriverCall ret = new DriverCall();
        ret.index = index;
        ret.isMT = this.mIsMT;
        try {
            ret.state = DriverCall.stateFromCLCC(this.mState.value());
            ret.isMpty = this.mIsMpty;
            ret.number = this.mNumber;
            ret.TOA = this.mTOA;
            ret.isVoice = true;
            ret.als = 0;
            return ret;
        } catch (ATParseEx ex) {
            throw new RuntimeException("should never happen", ex);
        }
    }

    boolean isActiveOrHeld() {
        return this.mState == State.ACTIVE || this.mState == State.HOLDING;
    }

    boolean isConnecting() {
        return this.mState == State.DIALING || this.mState == State.ALERTING;
    }

    boolean isRinging() {
        return this.mState == State.INCOMING || this.mState == State.WAITING;
    }
}
