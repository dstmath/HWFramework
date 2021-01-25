package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.Build;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneSwitcher;

public class IccIoResult {
    private static final String UNKNOWN_ERROR = "unknown";
    private int fileId;
    private boolean isValid;
    @UnsupportedAppUsage
    public byte[] payload;
    @UnsupportedAppUsage
    public int sw1;
    @UnsupportedAppUsage
    public int sw2;

    private String getErrorString() {
        int i = this.sw1;
        if (i == 152) {
            int i2 = this.sw2;
            if (i2 == 2) {
                return "no CHV initialized";
            }
            if (i2 == 4) {
                return "access condition not fulfilled/unsuccessful CHV verification, at least one attempt left/unsuccessful UNBLOCK CHV verification, at least one attempt left/authentication failed";
            }
            if (i2 == 8) {
                return "in contradiction with CHV status";
            }
            if (i2 == 16) {
                return "in contradiction with invalidation status";
            }
            if (i2 == 64) {
                return "unsuccessful CHV verification, no attempt left/unsuccessful UNBLOCK CHV verification, no attempt left/CHV blockedUNBLOCK CHV blocked";
            }
            if (i2 == 80) {
                return "increase cannot be performed, Max value reached";
            }
            if (i2 == 98) {
                return "authentication error, application specific";
            }
            switch (i2) {
                case 100:
                    return "authentication error, security context not supported";
                case 101:
                    return "key freshness failure";
                case CallFailCause.RECOVERY_ON_TIMER_EXPIRY /* 102 */:
                    return "authentication error, no memory space available";
                case 103:
                    return "authentication error, no memory space available in EF_MUK";
                default:
                    return UNKNOWN_ERROR;
            }
        } else if (i == 158 || i == 159) {
            return null;
        } else {
            switch (i) {
                case 98:
                    int i3 = this.sw2;
                    if (i3 == 0) {
                        return "No information given, state of non volatile memory unchanged";
                    }
                    switch (i3) {
                        case 129:
                            return "Part of returned data may be corrupted";
                        case 130:
                            return "End of file/record reached before reading Le bytes";
                        case 131:
                            return "Selected file invalidated";
                        case 132:
                            return "Selected file in termination state";
                        default:
                            switch (i3) {
                                case CallFailCause.FDN_BLOCKED /* 241 */:
                                    return "More data available";
                                case 242:
                                    return "More data available and proactive command pending";
                                case CallFailCause.IMEI_NOT_ACCEPTED /* 243 */:
                                    return "Response data available";
                                default:
                                    return UNKNOWN_ERROR;
                            }
                    }
                case 99:
                    int i4 = this.sw2;
                    if ((i4 >> 4) == 12) {
                        return "Command successful but after using an internalupdate retry routine but Verification failed";
                    }
                    if (i4 == 241) {
                        return "More data expected";
                    }
                    if (i4 != 242) {
                        return UNKNOWN_ERROR;
                    }
                    return "More data expected and proactive command pending";
                case 100:
                    if (this.sw2 != 0) {
                        return UNKNOWN_ERROR;
                    }
                    return "No information given, state of non-volatile memory unchanged";
                case 101:
                    int i5 = this.sw2;
                    if (i5 == 0) {
                        return "No information given, state of non-volatile memory changed";
                    }
                    if (i5 != 129) {
                        return UNKNOWN_ERROR;
                    }
                    return "Memory problem";
                default:
                    switch (i) {
                        case 103:
                            if (this.sw2 != 0) {
                                return "The interpretation of this status word is command dependent";
                            }
                            return "incorrect parameter P3";
                        case 104:
                            int i6 = this.sw2;
                            if (i6 == 0) {
                                return "No information given";
                            }
                            if (i6 == 129) {
                                return "Logical channel not supported";
                            }
                            if (i6 != 130) {
                                return UNKNOWN_ERROR;
                            }
                            return "Secure messaging not supported";
                        case 105:
                            int i7 = this.sw2;
                            if (i7 == 0) {
                                return "No information given";
                            }
                            if (i7 == 137) {
                                return "Command not allowed - secure channel - security not satisfied";
                            }
                            switch (i7) {
                                case 129:
                                    return "Command incompatible with file structure";
                                case 130:
                                    return "Security status not satisfied";
                                case 131:
                                    return "Authentication/PIN method blocked";
                                case 132:
                                    return "Referenced data invalidated";
                                case 133:
                                    return "Conditions of use not satisfied";
                                case 134:
                                    return "Command not allowed (no EF selected)";
                                default:
                                    return UNKNOWN_ERROR;
                            }
                        case 106:
                            switch (this.sw2) {
                                case 128:
                                    return "Incorrect parameters in the data field";
                                case 129:
                                    return "Function not supported";
                                case 130:
                                    return "File not found";
                                case 131:
                                    return "Record not found";
                                case 132:
                                    return "Not enough memory space";
                                case 133:
                                default:
                                    return UNKNOWN_ERROR;
                                case 134:
                                    return "Incorrect parameters P1 to P2";
                                case 135:
                                    return "Lc inconsistent with P1 to P2";
                                case 136:
                                    return "Referenced data not found";
                            }
                        case 107:
                            return "incorrect parameter P1 or P2";
                        default:
                            switch (i) {
                                case PhoneSwitcher.EVENT_PRECISE_CALL_STATE_CHANGED /* 109 */:
                                    return "unknown instruction code given in the command";
                                case 110:
                                    return "wrong instruction class given in the command";
                                case 111:
                                    if (this.sw2 != 0) {
                                        return "The interpretation of this status word is command dependent";
                                    }
                                    return "technical problem with no diagnostic given";
                                default:
                                    switch (i) {
                                        case 144:
                                            return null;
                                        case 145:
                                            return null;
                                        case 146:
                                            int i8 = this.sw2;
                                            if ((i8 >> 4) == 0) {
                                                return "command successful but after using an internal update retry routine";
                                            }
                                            if (i8 != 64) {
                                                return UNKNOWN_ERROR;
                                            }
                                            return "memory problem";
                                        case 147:
                                            if (this.sw2 != 0) {
                                                return UNKNOWN_ERROR;
                                            }
                                            return "SIM Application Toolkit is busy. Command cannot be executed at present, further normal commands are allowed.";
                                        case 148:
                                            int i9 = this.sw2;
                                            if (i9 == 0) {
                                                return "no EF selected";
                                            }
                                            if (i9 == 2) {
                                                return "out f range (invalid address)";
                                            }
                                            if (i9 == 4) {
                                                return "file ID not found/pattern not found";
                                            }
                                            if (i9 != 8) {
                                                return UNKNOWN_ERROR;
                                            }
                                            return "file is inconsistent with the command";
                                        default:
                                            return UNKNOWN_ERROR;
                                    }
                            }
                    }
            }
        }
    }

    @UnsupportedAppUsage
    public IccIoResult(int sw12, int sw22, byte[] payload2) {
        this.sw1 = sw12;
        this.sw2 = sw22;
        this.payload = payload2;
    }

    @UnsupportedAppUsage
    public IccIoResult(int sw12, int sw22, String hexString) {
        this(sw12, sw22, IccUtils.hexStringToBytes(hexString));
    }

    public IccIoResult(boolean isValid2, int fileId2, int sw12, int sw22, byte[] payload2) {
        this(sw12, sw22, payload2);
        this.isValid = isValid2;
        this.fileId = fileId2;
    }

    public IccIoResult(boolean isValid2, int fileId2, int sw12, int sw22, String hexString) {
        this(isValid2, fileId2, sw12, sw22, IccUtils.hexStringToBytes(hexString));
    }

    public boolean isValidIccioResult() {
        return this.isValid;
    }

    public int getFileId() {
        return this.fileId;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("IccIoResult sw1:0x");
        sb.append(Integer.toHexString(this.sw1));
        sb.append(" sw2:0x");
        sb.append(Integer.toHexString(this.sw2));
        sb.append(" Payload: ");
        sb.append((!Build.IS_DEBUGGABLE || !Build.IS_ENG) ? "*******" : IccUtils.bytesToHexString(this.payload));
        if (!success()) {
            str = " Error: " + getErrorString();
        } else {
            str = PhoneConfigurationManager.SSSS;
        }
        sb.append(str);
        return sb.toString();
    }

    @UnsupportedAppUsage
    public boolean success() {
        int i = this.sw1;
        return i == 144 || i == 145 || i == 158 || i == 159;
    }

    public IccException getException() {
        if (success()) {
            return null;
        }
        if (this.sw1 != 148) {
            return new IccException("sw1:" + this.sw1 + " sw2:" + this.sw2);
        } else if (this.sw2 == 8) {
            return new IccFileTypeMismatch();
        } else {
            return new IccFileNotFound();
        }
    }
}
