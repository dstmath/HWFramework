package vendor.huawei.hardware.eid.V1_1;

import java.util.ArrayList;

public final class ERR_CODE_E {
    public static final int ADDRESS_INFO_ERR = 5;
    public static final int CAMERA_REGISTER_FAIL = 34;
    public static final int CUT_IMAGE_MISMATCH = 35;
    public static final int DLSYM_ERROR = 28;
    public static final int FACEID_CHANGED = 14;
    public static final int FACEID_DEL_FAIL = 18;
    public static final int FACEID_NOCHANGE = 15;
    public static final int FACEID_NOTUSED = 16;
    public static final int FACEID_SET_FAIL = 17;
    public static final int GET_APPLET_INFO_FAIL = 25;
    public static final int GET_IMAGE_FROM_SECIMAGE_FAIL = 30;
    public static final int GET_PUBLICKEY_FAIL = 22;
    public static final int GET_SEC_IMAGE_FAIL = 23;
    public static final int LIVE_DECT_FAIL = 29;
    public static final int LOAD_LIVELIB_FAIL = 21;
    public static final int MAP_STATUS_ERR = 6;
    public static final int MAP_UNKNOW_ERR = 7;
    public static final int MEMCPY_ERROR = 36;
    public static final int MEM_ALLOC_FAIL = 33;
    public static final int NEED_FACE_AUTH = 31;
    public static final int NO_REPLY_MSG_ERR = 8;
    public static final int NO_TEE_MEMORY = 19;
    public static final int OK = 0;
    public static final int PARA_ERR = 1;
    public static final int REPLY_MSG_ERR = 2;
    public static final int REPLY_MSG_PARA_ERR = 9;
    public static final int SEND_MSG_ERR = 3;
    public static final int SET_BACK_CAMERA_FAIL = 37;
    public static final int SET_SEC_MODE_FAIL = 32;
    public static final int TA_FREE_FAIL = 20;
    public static final int TUI_PIN_LEN_ERR = 13;
    public static final int TUI_PIN_OVER_TIMES = 12;
    public static final int TUI_PIN_SYS_ERR = 10;
    public static final int TUI_PIN_USER_CANCEL = 11;
    public static final int UNKNOWN_CMD = 4;
    public static final int USER_NO_AGREE_TUI_INFO = 27;
    public static final int USE_TEE_SM_FAIL = 24;
    public static final int USE_TUI_FAIL = 26;

    public static final String toString(int o) {
        if (o == 0) {
            return "OK";
        }
        if (o == 1) {
            return "PARA_ERR";
        }
        if (o == 2) {
            return "REPLY_MSG_ERR";
        }
        if (o == 3) {
            return "SEND_MSG_ERR";
        }
        if (o == 4) {
            return "UNKNOWN_CMD";
        }
        if (o == 5) {
            return "ADDRESS_INFO_ERR";
        }
        if (o == 6) {
            return "MAP_STATUS_ERR";
        }
        if (o == 7) {
            return "MAP_UNKNOW_ERR";
        }
        if (o == 8) {
            return "NO_REPLY_MSG_ERR";
        }
        if (o == 9) {
            return "REPLY_MSG_PARA_ERR";
        }
        if (o == 10) {
            return "TUI_PIN_SYS_ERR";
        }
        if (o == 11) {
            return "TUI_PIN_USER_CANCEL";
        }
        if (o == 12) {
            return "TUI_PIN_OVER_TIMES";
        }
        if (o == 13) {
            return "TUI_PIN_LEN_ERR";
        }
        if (o == 14) {
            return "FACEID_CHANGED";
        }
        if (o == 15) {
            return "FACEID_NOCHANGE";
        }
        if (o == 16) {
            return "FACEID_NOTUSED";
        }
        if (o == 17) {
            return "FACEID_SET_FAIL";
        }
        if (o == 18) {
            return "FACEID_DEL_FAIL";
        }
        if (o == 19) {
            return "NO_TEE_MEMORY";
        }
        if (o == 20) {
            return "TA_FREE_FAIL";
        }
        if (o == 21) {
            return "LOAD_LIVELIB_FAIL";
        }
        if (o == 22) {
            return "GET_PUBLICKEY_FAIL";
        }
        if (o == 23) {
            return "GET_SEC_IMAGE_FAIL";
        }
        if (o == 24) {
            return "USE_TEE_SM_FAIL";
        }
        if (o == 25) {
            return "GET_APPLET_INFO_FAIL";
        }
        if (o == 26) {
            return "USE_TUI_FAIL";
        }
        if (o == 27) {
            return "USER_NO_AGREE_TUI_INFO";
        }
        if (o == 28) {
            return "DLSYM_ERROR";
        }
        if (o == 29) {
            return "LIVE_DECT_FAIL";
        }
        if (o == 30) {
            return "GET_IMAGE_FROM_SECIMAGE_FAIL";
        }
        if (o == 31) {
            return "NEED_FACE_AUTH";
        }
        if (o == 32) {
            return "SET_SEC_MODE_FAIL";
        }
        if (o == 33) {
            return "MEM_ALLOC_FAIL";
        }
        if (o == 34) {
            return "CAMERA_REGISTER_FAIL";
        }
        if (o == 35) {
            return "CUT_IMAGE_MISMATCH";
        }
        if (o == 36) {
            return "MEMCPY_ERROR";
        }
        if (o == 37) {
            return "SET_BACK_CAMERA_FAIL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OK");
        if ((o & 1) == 1) {
            list.add("PARA_ERR");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("REPLY_MSG_ERR");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SEND_MSG_ERR");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("UNKNOWN_CMD");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ADDRESS_INFO_ERR");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("MAP_STATUS_ERR");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("MAP_UNKNOW_ERR");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("NO_REPLY_MSG_ERR");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("REPLY_MSG_PARA_ERR");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("TUI_PIN_SYS_ERR");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("TUI_PIN_USER_CANCEL");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("TUI_PIN_OVER_TIMES");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("TUI_PIN_LEN_ERR");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("FACEID_CHANGED");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("FACEID_NOCHANGE");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("FACEID_NOTUSED");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("FACEID_SET_FAIL");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("FACEID_DEL_FAIL");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("NO_TEE_MEMORY");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("TA_FREE_FAIL");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("LOAD_LIVELIB_FAIL");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("GET_PUBLICKEY_FAIL");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("GET_SEC_IMAGE_FAIL");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("USE_TEE_SM_FAIL");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("GET_APPLET_INFO_FAIL");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("USE_TUI_FAIL");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("USER_NO_AGREE_TUI_INFO");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("DLSYM_ERROR");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("LIVE_DECT_FAIL");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("GET_IMAGE_FROM_SECIMAGE_FAIL");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("NEED_FACE_AUTH");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("SET_SEC_MODE_FAIL");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("MEM_ALLOC_FAIL");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("CAMERA_REGISTER_FAIL");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("CUT_IMAGE_MISMATCH");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("MEMCPY_ERROR");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("SET_BACK_CAMERA_FAIL");
            flipped |= 37;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
