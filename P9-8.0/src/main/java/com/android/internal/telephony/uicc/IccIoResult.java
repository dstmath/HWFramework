package com.android.internal.telephony.uicc;

import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.RadioNVItems;

public class IccIoResult {
    private static final String UNKNOWN_ERROR = "unknown";
    public byte[] payload;
    public int sw1;
    public int sw2;

    private String getErrorString() {
        switch (this.sw1) {
            case 98:
                switch (this.sw2) {
                    case 0:
                        return "No information given, state of non volatile memory unchanged";
                    case 129:
                        return "Part of returned data may be corrupted";
                    case 130:
                        return "End of file/record reached before reading Le bytes";
                    case 131:
                        return "Selected file invalidated";
                    case 132:
                        return "Selected file in termination state";
                    case 241:
                        return "More data available";
                    case 242:
                        return "More data available and proactive command pending";
                    case 243:
                        return "Response data available";
                }
                break;
            case 99:
                if ((this.sw2 >> 4) == 12) {
                    return "Command successful but after using an internalupdate retry routine but Verification failed";
                }
                switch (this.sw2) {
                    case 241:
                        return "More data expected";
                    case 242:
                        return "More data expected and proactive command pending";
                }
                break;
            case 100:
                switch (this.sw2) {
                    case 0:
                        return "No information given, state of non-volatile memory unchanged";
                }
                break;
            case 101:
                switch (this.sw2) {
                    case 0:
                        return "No information given, state of non-volatile memory changed";
                    case 129:
                        return "Memory problem";
                }
                break;
            case 103:
                switch (this.sw2) {
                    case 0:
                        return "incorrect parameter P3";
                    default:
                        return "The interpretation of this status word is command dependent";
                }
            case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                switch (this.sw2) {
                    case 0:
                        return "No information given";
                    case 129:
                        return "Logical channel not supported";
                    case 130:
                        return "Secure messaging not supported";
                }
                break;
            case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                switch (this.sw2) {
                    case 0:
                        return "No information given";
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
                    case 137:
                        return "Command not allowed - secure channel - security not satisfied";
                }
                break;
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
                    case 134:
                        return "Incorrect parameters P1 to P2";
                    case 135:
                        return "Lc inconsistent with P1 to P2";
                    case 136:
                        return "Referenced data not found";
                }
                break;
            case 107:
                return "incorrect parameter P1 or P2";
            case 109:
                return "unknown instruction code given in the command";
            case 110:
                return "wrong instruction class given in the command";
            case 111:
                switch (this.sw2) {
                    case 0:
                        return "technical problem with no diagnostic given";
                    default:
                        return "The interpretation of this status word is command dependent";
                }
            case 144:
                return null;
            case 145:
                return null;
            case 146:
                if ((this.sw2 >> 4) == 0) {
                    return "command successful but after using an internal update retry routine";
                }
                switch (this.sw2) {
                    case 64:
                        return "memory problem";
                }
                break;
            case 147:
                switch (this.sw2) {
                    case 0:
                        return "SIM Application Toolkit is busy. Command cannot be executed at present, further normal commands are allowed.";
                }
                break;
            case 148:
                switch (this.sw2) {
                    case 0:
                        return "no EF selected";
                    case 2:
                        return "out f range (invalid address)";
                    case 4:
                        return "file ID not found/pattern not found";
                    case 8:
                        return "file is inconsistent with the command";
                }
                break;
            case 152:
                switch (this.sw2) {
                    case 2:
                        return "no CHV initialized";
                    case 4:
                        return "access condition not fulfilled/unsuccessful CHV verification, at least one attempt left/unsuccessful UNBLOCK CHV verification, at least one attempt left/authentication failed";
                    case 8:
                        return "in contradiction with CHV status";
                    case 16:
                        return "in contradiction with invalidation status";
                    case 64:
                        return "unsuccessful CHV verification, no attempt left/unsuccessful UNBLOCK CHV verification, no attempt left/CHV blockedUNBLOCK CHV blocked";
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                        return "increase cannot be performed, Max value reached";
                }
                break;
            case 158:
                return null;
            case 159:
                return null;
        }
        return UNKNOWN_ERROR;
    }

    public IccIoResult(int sw1, int sw2, byte[] payload) {
        this.sw1 = sw1;
        this.sw2 = sw2;
        this.payload = payload;
    }

    public IccIoResult(int sw1, int sw2, String hexString) {
        this(sw1, sw2, IccUtils.hexStringToBytes(hexString));
    }

    public String toString() {
        return "IccIoResult sw1:0x" + Integer.toHexString(this.sw1) + " sw2:0x" + Integer.toHexString(this.sw2) + (!success() ? " Error: " + getErrorString() : "");
    }

    public boolean success() {
        return this.sw1 == 144 || this.sw1 == 145 || this.sw1 == 158 || this.sw1 == 159;
    }

    public IccException getException() {
        if (success()) {
            return null;
        }
        switch (this.sw1) {
            case 148:
                if (this.sw2 == 8) {
                    return new IccFileTypeMismatch();
                }
                return new IccFileNotFound();
            default:
                return new IccException("sw1:" + this.sw1 + " sw2:" + this.sw2);
        }
    }
}
