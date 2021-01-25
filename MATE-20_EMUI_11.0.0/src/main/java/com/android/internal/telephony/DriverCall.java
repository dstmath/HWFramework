package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;

public class DriverCall implements Comparable<DriverCall> {
    public static final int AUDIO_QUALITY_AMR = 1;
    public static final int AUDIO_QUALITY_AMR_WB = 2;
    public static final int AUDIO_QUALITY_EVRC = 6;
    public static final int AUDIO_QUALITY_EVRC_B = 7;
    public static final int AUDIO_QUALITY_EVRC_NW = 9;
    public static final int AUDIO_QUALITY_EVRC_WB = 8;
    public static final int AUDIO_QUALITY_GSM_EFR = 3;
    public static final int AUDIO_QUALITY_GSM_FR = 4;
    public static final int AUDIO_QUALITY_GSM_HR = 5;
    public static final int AUDIO_QUALITY_UNSPECIFIED = 0;
    static final String LOG_TAG = "DriverCall";
    public int TOA;
    public int als;
    public int audioQuality = 0;
    @UnsupportedAppUsage
    public int index;
    @UnsupportedAppUsage
    public boolean isMT;
    public boolean isMpty;
    @UnsupportedAppUsage
    public boolean isVoice;
    public boolean isVoicePrivacy;
    @UnsupportedAppUsage
    public String name;
    public int namePresentation;
    @UnsupportedAppUsage
    public String number;
    @UnsupportedAppUsage
    public int numberPresentation;
    public String redirectNumber;
    public int redirectNumberPresentation;
    public int redirectNumberTOA;
    @UnsupportedAppUsage
    public State state;
    public UUSInfo uusInfo;

    public enum State {
        ACTIVE,
        HOLDING,
        DIALING,
        ALERTING,
        INCOMING,
        WAITING
    }

    static DriverCall fromCLCCLine(String line) {
        DriverCall ret = new DriverCall();
        ATResponseParser p = new ATResponseParser(line);
        try {
            ret.index = p.nextInt();
            ret.isMT = p.nextBoolean();
            ret.state = stateFromCLCC(p.nextInt());
            ret.isVoice = p.nextInt() == 0;
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

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=");
        sb.append(this.index);
        sb.append(",");
        sb.append(this.state);
        sb.append(",toa=");
        sb.append(this.TOA);
        sb.append(",");
        sb.append(this.isMpty ? "conf" : "norm");
        sb.append(",");
        sb.append(this.isMT ? "mt" : "mo");
        sb.append(",");
        sb.append(this.als);
        sb.append(",");
        sb.append(this.isVoice ? "voc" : "nonvoc");
        sb.append(",");
        sb.append(this.isVoicePrivacy ? "evp" : "noevp");
        sb.append(",,cli=");
        sb.append(this.numberPresentation);
        sb.append(",,");
        sb.append(this.namePresentation);
        sb.append(",audioQuality=");
        sb.append(this.audioQuality);
        return sb.toString();
    }

    public static State stateFromCLCC(int state2) throws ATParseEx {
        if (state2 == 0) {
            return State.ACTIVE;
        }
        if (state2 == 1) {
            return State.HOLDING;
        }
        if (state2 == 2) {
            return State.DIALING;
        }
        if (state2 == 3) {
            return State.ALERTING;
        }
        if (state2 == 4) {
            return State.INCOMING;
        }
        if (state2 == 5) {
            return State.WAITING;
        }
        throw new ATParseEx("illegal call state " + state2);
    }

    public static int presentationFromCLIP(int cli) throws ATParseEx {
        if (cli == 0) {
            return 1;
        }
        if (cli == 1) {
            return 2;
        }
        if (cli == 2) {
            return 3;
        }
        if (cli == 3) {
            return 4;
        }
        throw new ATParseEx("illegal presentation " + cli);
    }

    public int compareTo(DriverCall dc) {
        int i = this.index;
        int i2 = dc.index;
        if (i < i2) {
            return -1;
        }
        if (i == i2) {
            return 0;
        }
        return 1;
    }
}
