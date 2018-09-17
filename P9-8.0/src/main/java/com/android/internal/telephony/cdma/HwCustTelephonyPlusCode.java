package com.android.internal.telephony.cdma;

import android.util.FrameworkTagConstant;
import android.util.JlogConstants;
import android.util.LogException;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.AbstractRILConstants;
import javax.microedition.khronos.opengles.GL10;

public class HwCustTelephonyPlusCode {
    protected static final HwCustMccIddNddSid[] MccIddNddSidMap_support;
    protected static final HwCustMccSidLtmOff[] MccSidLtmOffMap_support = new HwCustMccSidLtmOff[]{new HwCustMccSidLtmOff(310, 1, -20, -10), new HwCustMccSidLtmOff(404, 1, 11, 11), new HwCustMccSidLtmOff(310, 7, -20, -10), new HwCustMccSidLtmOff(404, 7, 11, 11), new HwCustMccSidLtmOff(310, 13, -20, -10), new HwCustMccSidLtmOff(MetricsEvent.ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED, 13, 16, 16), new HwCustMccSidLtmOff(310, 1111, -20, -10), new HwCustMccSidLtmOff(450, 1111, 18, 18), new HwCustMccSidLtmOff(310, 1112, -20, -10), new HwCustMccSidLtmOff(450, 1112, 18, 18), new HwCustMccSidLtmOff(310, 1113, -20, -10), new HwCustMccSidLtmOff(450, 1113, 18, 18), new HwCustMccSidLtmOff(310, FrameworkTagConstant.HWTAG_USM, -20, -10), new HwCustMccSidLtmOff(450, FrameworkTagConstant.HWTAG_USM, 18, 18), new HwCustMccSidLtmOff(310, AbstractRILConstants.RIL_REQUEST_HW_SIMLOCK_NW_DATA_WRITE, -20, -10), new HwCustMccSidLtmOff(450, AbstractRILConstants.RIL_REQUEST_HW_SIMLOCK_NW_DATA_WRITE, 18, 18), new HwCustMccSidLtmOff(310, 2179, -20, -10), new HwCustMccSidLtmOff(450, 2179, 18, 18), new HwCustMccSidLtmOff(310, 2181, -20, -10), new HwCustMccSidLtmOff(450, 2181, 18, 18), new HwCustMccSidLtmOff(310, 2183, -20, -10), new HwCustMccSidLtmOff(450, 2183, 18, 18), new HwCustMccSidLtmOff(310, 2185, -20, -10), new HwCustMccSidLtmOff(450, 2185, 18, 18), new HwCustMccSidLtmOff(310, 2187, -20, -10), new HwCustMccSidLtmOff(450, 2187, 18, 18), new HwCustMccSidLtmOff(310, 2189, -20, -10), new HwCustMccSidLtmOff(450, 2189, 18, 18), new HwCustMccSidLtmOff(310, 2191, -20, -10), new HwCustMccSidLtmOff(450, 2191, 18, 18), new HwCustMccSidLtmOff(310, 2193, -20, -10), new HwCustMccSidLtmOff(450, 2193, 18, 18), new HwCustMccSidLtmOff(310, 2195, -20, -10), new HwCustMccSidLtmOff(450, 2195, 18, 18), new HwCustMccSidLtmOff(310, 2197, -20, -10), new HwCustMccSidLtmOff(450, 2197, 18, 18), new HwCustMccSidLtmOff(310, 2199, -20, -10), new HwCustMccSidLtmOff(450, 2199, 18, 18), new HwCustMccSidLtmOff(310, JlogConstants.JLID_PWRSCRON_PWM_GETMESSAGE, -20, -10), new HwCustMccSidLtmOff(450, JlogConstants.JLID_PWRSCRON_PWM_GETMESSAGE, 18, 18), new HwCustMccSidLtmOff(310, JlogConstants.JLID_PWRSCRON_PMS_WAKEUPINTERNAL, -20, -10), new HwCustMccSidLtmOff(450, JlogConstants.JLID_PWRSCRON_PMS_WAKEUPINTERNAL, 18, 18), new HwCustMccSidLtmOff(310, JlogConstants.JLID_PWRSCRON_DPC_BLOCKSCREENON, -20, -10), new HwCustMccSidLtmOff(450, JlogConstants.JLID_PWRSCRON_DPC_BLOCKSCREENON, 18, 18), new HwCustMccSidLtmOff(310, JlogConstants.JLID_PWRSCRON_NOTIFIER_WAKEFINISH, -20, -10), new HwCustMccSidLtmOff(450, JlogConstants.JLID_PWRSCRON_NOTIFIER_WAKEFINISH, 18, 18), new HwCustMccSidLtmOff(310, 2209, -20, -10), new HwCustMccSidLtmOff(450, 2209, 18, 18), new HwCustMccSidLtmOff(310, 2211, -20, -10), new HwCustMccSidLtmOff(450, 2211, 18, 18), new HwCustMccSidLtmOff(310, 2213, -20, -10), new HwCustMccSidLtmOff(450, 2213, 18, 18), new HwCustMccSidLtmOff(310, 2215, -20, -10), new HwCustMccSidLtmOff(450, 2215, 18, 18), new HwCustMccSidLtmOff(310, 2217, -20, -10), new HwCustMccSidLtmOff(450, 2217, 18, 18), new HwCustMccSidLtmOff(310, 2219, -20, -10), new HwCustMccSidLtmOff(450, 2219, 18, 18), new HwCustMccSidLtmOff(310, 2221, -20, -10), new HwCustMccSidLtmOff(450, 2221, 18, 18), new HwCustMccSidLtmOff(310, 2223, -20, -10), new HwCustMccSidLtmOff(450, 2223, 18, 18), new HwCustMccSidLtmOff(310, 2225, -20, -10), new HwCustMccSidLtmOff(450, 2225, 18, 18), new HwCustMccSidLtmOff(310, 2227, -20, -10), new HwCustMccSidLtmOff(450, 2227, 18, 18), new HwCustMccSidLtmOff(310, 2229, -20, -10), new HwCustMccSidLtmOff(450, 2229, 18, 18), new HwCustMccSidLtmOff(310, 2231, -20, -10), new HwCustMccSidLtmOff(450, 2231, 18, 18), new HwCustMccSidLtmOff(310, 2233, -20, -10), new HwCustMccSidLtmOff(450, 2233, 18, 18), new HwCustMccSidLtmOff(310, 2235, -20, -10), new HwCustMccSidLtmOff(450, 2235, 18, 18), new HwCustMccSidLtmOff(310, 2237, -20, -10), new HwCustMccSidLtmOff(450, 2237, 18, 18), new HwCustMccSidLtmOff(310, 2239, -20, -10), new HwCustMccSidLtmOff(450, 2239, 18, 18), new HwCustMccSidLtmOff(310, 2241, -20, -10), new HwCustMccSidLtmOff(450, 2241, 18, 18), new HwCustMccSidLtmOff(310, 2243, -20, -10), new HwCustMccSidLtmOff(450, 2243, 18, 18), new HwCustMccSidLtmOff(310, 2301, -20, -10), new HwCustMccSidLtmOff(450, 2301, 18, 18), new HwCustMccSidLtmOff(310, 2303, -20, -10), new HwCustMccSidLtmOff(450, 2303, 18, 18), new HwCustMccSidLtmOff(310, 2369, -20, -10), new HwCustMccSidLtmOff(450, 2369, 18, 18), new HwCustMccSidLtmOff(310, 2370, -20, -10), new HwCustMccSidLtmOff(450, 2370, 18, 18), new HwCustMccSidLtmOff(310, 2371, -20, -10), new HwCustMccSidLtmOff(450, 2371, 18, 18), new HwCustMccSidLtmOff(450, 2222, 18, 18), new HwCustMccSidLtmOff(404, 2222, 11, 11), new HwCustMccSidLtmOff(440, 12461, 18, 18), new HwCustMccSidLtmOff(MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, 12461, 12, 12), new HwCustMccSidLtmOff(440, 12463, 18, 18), new HwCustMccSidLtmOff(MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, 12463, 12, 12), new HwCustMccSidLtmOff(440, 12464, 18, 18), new HwCustMccSidLtmOff(MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, 12464, 12, 12)};

    static {
        r7 = new HwCustMccIddNddSid[23];
        r7[1] = new HwCustMccIddNddSid(310, "1", 1, 2175, "011", "1");
        r7[2] = new HwCustMccIddNddSid(311, "1", GL10.GL_CW, 7679, "011", "1");
        r7[3] = new HwCustMccIddNddSid(312, "1", 0, 0, "011", "1");
        r7[4] = new HwCustMccIddNddSid(313, "1", 0, 0, "011", "1");
        r7[5] = new HwCustMccIddNddSid(314, "1", 0, 0, "011", "1");
        r7[6] = new HwCustMccIddNddSid(315, "1", 0, 0, "011", "1");
        r7[7] = new HwCustMccIddNddSid(316, "1", 0, 0, "011", "1");
        r7[8] = new HwCustMccIddNddSid(334, "52", 24576, 25075, "00", "01");
        r7[9] = new HwCustMccIddNddSid(334, "52", 25100, 25124, "00", "01");
        r7[10] = new HwCustMccIddNddSid(404, "91", 14464, 14847, "00", "0");
        r7[11] = new HwCustMccIddNddSid(425, "972", GL10.GL_MODULATE, 8479, "00", "0");
        r7[12] = new HwCustMccIddNddSid(428, "976", 15520, 15551, "002", "0");
        r7[13] = new HwCustMccIddNddSid(440, "81", 12288, 13311, "010", "0");
        r7[14] = new HwCustMccIddNddSid(450, "82", 2176, 2303, "00700", "0");
        r7[15] = new HwCustMccIddNddSid(MetricsEvent.ACTION_HIDE_APP_DISAMBIG_APP_FEATURED, "84", 13312, 13439, "00", "0");
        r7[16] = new HwCustMccIddNddSid(MetricsEvent.ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED, "852", 10640, 10655, "001", LogException.NO_VALUE);
        r7[17] = new HwCustMccIddNddSid(MetricsEvent.ACTION_APP_DISAMBIG_ALWAYS, "853", 11296, 11311, "00", "0");
        r7[18] = new HwCustMccIddNddSid(MetricsEvent.ACTION_DELETION_SELECTION_PHOTOS, "86", 13568, 14335, "00", "0");
        r7[19] = new HwCustMccIddNddSid(MetricsEvent.ACTION_DELETION_SELECTION_PHOTOS, "86", 25600, 26111, "00", "0");
        r7[20] = new HwCustMccIddNddSid(MetricsEvent.ACTION_DELETION_DOWNLOADS_COLLAPSED, "886", 13504, 13535, "005", LogException.NO_VALUE);
        r7[21] = new HwCustMccIddNddSid(MetricsEvent.ACTION_DELETION_HELPER_REMOVE_CANCEL, "880", 13472, 13503, "00", "0");
        r7[22] = new HwCustMccIddNddSid(510, "62", 10496, 10623, "01033", "0");
        MccIddNddSidMap_support = r7;
    }
}
