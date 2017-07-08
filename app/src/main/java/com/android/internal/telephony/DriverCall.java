package com.android.internal.telephony;

import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduPersister;

public class DriverCall implements Comparable<DriverCall> {
    static final String LOG_TAG = "DriverCall";
    public int TOA;
    public int als;
    public int index;
    public boolean isMT;
    public boolean isMpty;
    public boolean isVoice;
    public boolean isVoicePrivacy;
    public String name;
    public int namePresentation;
    public String number;
    public int numberPresentation;
    public State state;
    public UUSInfo uusInfo;

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.DriverCall.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.DriverCall.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.DriverCall.State.<clinit>():void");
        }
    }

    static DriverCall fromCLCCLine(String line) {
        boolean z = true;
        DriverCall ret = new DriverCall();
        ATResponseParser p = new ATResponseParser(line);
        try {
            ret.index = p.nextInt();
            ret.isMT = p.nextBoolean();
            ret.state = stateFromCLCC(p.nextInt());
            if (p.nextInt() != 0) {
                z = false;
            }
            ret.isVoice = z;
            ret.isMpty = p.nextBoolean();
            ret.numberPresentation = 1;
            if (p.hasMore()) {
                ret.number = PhoneNumberUtils.extractNetworkPortionAlt(p.nextString());
                if (ret.number.length() == 0) {
                    ret.number = null;
                }
                ret.TOA = p.nextInt();
                ret.number = PhoneNumberUtils.stringFromStringAndTOA(ret.number, ret.TOA);
            }
            return ret;
        } catch (ATParseEx e) {
            Rlog.e(LOG_TAG, "Invalid CLCC line: '" + line + "'");
            return null;
        }
    }

    public DriverCall() {
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("id=").append(this.index).append(",").append(this.state).append(",").append("toa=").append(this.TOA).append(",").append(this.isMpty ? "conf" : "norm").append(",").append(this.isMT ? "mt" : "mo").append(",").append(this.als).append(",");
        if (this.isVoice) {
            str = "voc";
        } else {
            str = "nonvoc";
        }
        append = append.append(str).append(",");
        if (this.isVoicePrivacy) {
            str = "evp";
        } else {
            str = "noevp";
        }
        return append.append(str).append(",").append(",cli=").append(this.numberPresentation).append(",").append(",").append(this.namePresentation).toString();
    }

    public static State stateFromCLCC(int state) throws ATParseEx {
        switch (state) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return State.ACTIVE;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return State.HOLDING;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return State.DIALING;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return State.ALERTING;
            case CharacterSets.ISO_8859_1 /*4*/:
                return State.INCOMING;
            case CharacterSets.ISO_8859_2 /*5*/:
                return State.WAITING;
            default:
                throw new ATParseEx("illegal call state " + state);
        }
    }

    public static int presentationFromCLIP(int cli) throws ATParseEx {
        switch (cli) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                return 1;
            case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                return 2;
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                return 3;
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                return 4;
            default:
                throw new ATParseEx("illegal presentation " + cli);
        }
    }

    public /* bridge */ /* synthetic */ int compareTo(Object dc) {
        return compareTo((DriverCall) dc);
    }

    public int compareTo(DriverCall dc) {
        if (this.index < dc.index) {
            return -1;
        }
        if (this.index == dc.index) {
            return 0;
        }
        return 1;
    }
}
