package vendor.huawei.hardware.eid.V1_0;

import java.util.ArrayList;

public final class BUFF_LEN_E {
    public static final int CERTIFICATE_MAX_LEN = 8192;
    public static final int CERT_REQ_MSG_MAX_LEN = 2048;
    public static final int DE_SKEY_MAX_LEN = 2048;
    public static final int ID_INFO_MAX_LEN = 5120;
    public static final int IMAGE_NV21_SIZE = 460800;
    public static final int INFO_MAX_LEN = 2048;
    public static final int INFO_SIGN_MAX_LEN = 4096;
    public static final int INPUT_MAX_TRANSPOT_LEN = 153600;
    public static final int INPUT_TRANSPOT_TIMES = 3;
    public static final int MAX_AID_LEN = 256;
    public static final int MAX_LOGO_SIZE = 24576;
    public static final int OUTPUT_MAX_TRANSPOT_LEN = 163840;
    public static final int OUTPUT_TRANSPOT_TIMES = 3;
    public static final int SEC_IMAGE_MAX_LEN = 491520;

    public static final String toString(int o) {
        if (o == 8192) {
            return "CERTIFICATE_MAX_LEN";
        }
        if (o == 491520) {
            return "SEC_IMAGE_MAX_LEN";
        }
        if (o == 2048) {
            return "DE_SKEY_MAX_LEN";
        }
        if (o == 2048) {
            return "CERT_REQ_MSG_MAX_LEN";
        }
        if (o == 2048) {
            return "INFO_MAX_LEN";
        }
        if (o == 4096) {
            return "INFO_SIGN_MAX_LEN";
        }
        if (o == 460800) {
            return "IMAGE_NV21_SIZE";
        }
        if (o == 5120) {
            return "ID_INFO_MAX_LEN";
        }
        if (o == 163840) {
            return "OUTPUT_MAX_TRANSPOT_LEN";
        }
        if (o == 153600) {
            return "INPUT_MAX_TRANSPOT_LEN";
        }
        if (o == 3) {
            return "OUTPUT_TRANSPOT_TIMES";
        }
        if (o == 3) {
            return "INPUT_TRANSPOT_TIMES";
        }
        if (o == 256) {
            return "MAX_AID_LEN";
        }
        if (o == 24576) {
            return "MAX_LOGO_SIZE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 8192) == 8192) {
            list.add("CERTIFICATE_MAX_LEN");
            flipped = 0 | 8192;
        }
        if ((o & 491520) == 491520) {
            list.add("SEC_IMAGE_MAX_LEN");
            flipped |= 491520;
        }
        if ((o & 2048) == 2048) {
            list.add("DE_SKEY_MAX_LEN");
            flipped |= 2048;
        }
        if ((o & 2048) == 2048) {
            list.add("CERT_REQ_MSG_MAX_LEN");
            flipped |= 2048;
        }
        if ((o & 2048) == 2048) {
            list.add("INFO_MAX_LEN");
            flipped |= 2048;
        }
        if ((o & 4096) == 4096) {
            list.add("INFO_SIGN_MAX_LEN");
            flipped |= 4096;
        }
        if ((o & 460800) == 460800) {
            list.add("IMAGE_NV21_SIZE");
            flipped |= 460800;
        }
        if ((o & 5120) == 5120) {
            list.add("ID_INFO_MAX_LEN");
            flipped |= 5120;
        }
        if ((o & 163840) == 163840) {
            list.add("OUTPUT_MAX_TRANSPOT_LEN");
            flipped |= 163840;
        }
        if ((o & 153600) == 153600) {
            list.add("INPUT_MAX_TRANSPOT_LEN");
            flipped |= 153600;
        }
        if ((o & 3) == 3) {
            list.add("OUTPUT_TRANSPOT_TIMES");
            flipped |= 3;
        }
        if ((o & 3) == 3) {
            list.add("INPUT_TRANSPOT_TIMES");
            flipped |= 3;
        }
        if ((o & 256) == 256) {
            list.add("MAX_AID_LEN");
            flipped |= 256;
        }
        if ((o & 24576) == 24576) {
            list.add("MAX_LOGO_SIZE");
            flipped |= 24576;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
