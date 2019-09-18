package com.huawei.zxing.pdf417.encoder;

import android.telephony.HwCarrierConfigManager;
import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.os.HwTransCodeEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.internal.http.StatusLine;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.pdf417.PDF417Common;

final class PDF417ErrorCorrection {
    private static final int[][] EC_COEFFICIENTS;

    static {
        int[] iArr = new int[AppOpsManagerEx.TYPE_MICROPHONE];
        // fill-array-data instruction
        iArr[0] = 521;
        iArr[1] = 310;
        iArr[2] = 864;
        iArr[3] = 547;
        iArr[4] = 858;
        iArr[5] = 580;
        iArr[6] = 296;
        iArr[7] = 379;
        iArr[8] = 53;
        iArr[9] = 779;
        iArr[10] = 897;
        iArr[11] = 444;
        iArr[12] = 400;
        iArr[13] = 925;
        iArr[14] = 749;
        iArr[15] = 415;
        iArr[16] = 822;
        iArr[17] = 93;
        iArr[18] = 217;
        iArr[19] = 208;
        iArr[20] = 928;
        iArr[21] = 244;
        iArr[22] = 583;
        iArr[23] = 620;
        iArr[24] = 246;
        iArr[25] = 148;
        iArr[26] = 447;
        iArr[27] = 631;
        iArr[28] = 292;
        iArr[29] = 908;
        iArr[30] = 490;
        iArr[31] = 704;
        iArr[32] = 516;
        iArr[33] = 258;
        iArr[34] = 457;
        iArr[35] = 907;
        iArr[36] = 594;
        iArr[37] = 723;
        iArr[38] = 674;
        iArr[39] = 292;
        iArr[40] = 272;
        iArr[41] = 96;
        iArr[42] = 684;
        iArr[43] = 432;
        iArr[44] = 686;
        iArr[45] = 606;
        iArr[46] = 860;
        iArr[47] = 569;
        iArr[48] = 193;
        iArr[49] = 219;
        iArr[50] = 129;
        iArr[51] = 186;
        iArr[52] = 236;
        iArr[53] = 287;
        iArr[54] = 192;
        iArr[55] = 775;
        iArr[56] = 278;
        iArr[57] = 173;
        iArr[58] = 40;
        iArr[59] = 379;
        iArr[60] = 712;
        iArr[61] = 463;
        iArr[62] = 646;
        iArr[63] = 776;
        iArr[64] = 171;
        iArr[65] = 491;
        iArr[66] = 297;
        iArr[67] = 763;
        iArr[68] = 156;
        iArr[69] = 732;
        iArr[70] = 95;
        iArr[71] = 270;
        iArr[72] = 447;
        iArr[73] = 90;
        iArr[74] = 507;
        iArr[75] = 48;
        iArr[76] = 228;
        iArr[77] = 821;
        iArr[78] = 808;
        iArr[79] = 898;
        iArr[80] = 784;
        iArr[81] = 663;
        iArr[82] = 627;
        iArr[83] = 378;
        iArr[84] = 382;
        iArr[85] = 262;
        iArr[86] = 380;
        iArr[87] = 602;
        iArr[88] = 754;
        iArr[89] = 336;
        iArr[90] = 89;
        iArr[91] = 614;
        iArr[92] = 87;
        iArr[93] = 432;
        iArr[94] = 670;
        iArr[95] = 616;
        iArr[96] = 157;
        iArr[97] = 374;
        iArr[98] = 242;
        iArr[99] = 726;
        iArr[100] = 600;
        iArr[101] = 269;
        iArr[102] = 375;
        iArr[103] = 898;
        iArr[104] = 845;
        iArr[105] = 454;
        iArr[106] = 354;
        iArr[107] = 130;
        iArr[108] = 814;
        iArr[109] = 587;
        iArr[110] = 804;
        iArr[111] = 34;
        iArr[112] = 211;
        iArr[113] = 330;
        iArr[114] = 539;
        iArr[115] = 297;
        iArr[116] = 827;
        iArr[117] = 865;
        iArr[118] = 37;
        iArr[119] = 517;
        iArr[120] = 834;
        iArr[121] = 315;
        iArr[122] = 550;
        iArr[123] = 86;
        iArr[124] = 801;
        iArr[125] = 4;
        iArr[126] = 108;
        iArr[127] = 539;
        EC_COEFFICIENTS = new int[][]{new int[]{27, 917}, new int[]{522, 568, 723, 809}, new int[]{237, StatusLine.HTTP_PERM_REDIRECT, 436, 284, 646, 653, 428, 379}, new int[]{274, 562, 232, 755, 599, 524, MotionTypeApps.TYPE_POCKET_CALL_RAISE, JlogConstantsEx.JLID_EDIT_CONTACT_END, 295, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW, 442, 428, 295, 42, IccConstantsEx.SMS_RECORD_LENGTH, 65}, new int[]{361, 575, 922, 525, IccConstantsEx.SMS_RECORD_LENGTH, 586, 640, 321, 536, 742, 677, 742, 687, 284, 193, 517, 273, 494, 263, 147, 593, 800, 571, 320, 803, 133, 231, 390, 685, 330, 63, 410}, new int[]{539, 422, 6, 93, 862, 771, 453, 106, 610, 287, MetricConstant.BLUETOOTH_METRIC_ID_EX, 505, 733, 877, 381, 612, 723, 476, 462, 172, 430, 609, 858, 822, 543, 376, 511, 400, 672, 762, 283, 184, 440, 35, 519, 31, 460, 594, 225, 535, 517, 352, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 158, 651, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 488, HwTransCodeEx.ANR_FILTER_FIFO, 648, 733, 717, 83, MotionTypeApps.TYPE_SHAKE_START_PRIVACY, 97, 280, 771, 840, 629, 4, 381, 843, 623, 264, 543}, iArr, new int[]{524, 894, 75, 766, 882, 857, 74, 204, 82, 586, 708, 250, 905, 786, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 720, 858, 194, 311, 913, 275, 190, 375, 850, 438, 733, 194, 280, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 280, 828, 757, 710, 814, 919, 89, 68, 569, 11, 204, 796, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 540, 913, MotionTypeApps.TYPE_POCKET_CALL_RAISE, 700, 799, JlogConstantsEx.JLID_MMS_CONVERSATIONS_DELETE, 439, 418, 592, 668, 353, 859, 370, 694, 325, 240, 216, 257, 284, 549, 209, 884, 315, 70, 329, 793, 490, 274, 877, 162, 749, 812, 684, 461, 334, 376, 849, 521, StatusLine.HTTP_TEMP_REDIRECT, 291, 803, 712, 19, 358, 399, 908, 103, 511, 51, 8, 517, 225, 289, 470, 637, 731, 66, 255, 917, 269, 463, 830, 730, 433, 848, 585, 136, 538, 906, 90, 2, 290, 743, HwFoldScreenManagerEx.POSTURE_OTHER, 655, 903, 329, 49, MotionTypeApps.TYPE_POCKET_AOD, 580, 355, 588, 188, 462, 10, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 628, 320, 479, 130, 739, 71, 263, 318, 374, 601, HwCarrierConfigManager.HD_ICON_MASK_DIALER, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 142, 673, 687, 234, 722, 384, 177, 752, 607, 640, 455, 193, 689, 707, 805, 641, 48, 60, 732, 621, 895, 544, 261, 852, 655, 309, 697, 755, 756, 60, 231, 773, 434, 421, 726, 528, 503, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, 49, 795, 32, 144, 500, 238, 836, 394, 280, 566, 319, 9, 647, 550, 73, 914, 342, JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT, 32, 681, 331, 792, 620, 60, 609, 441, 180, 791, 893, 754, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 383, 228, 749, 760, 213, 54, 297, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 54, 834, 299, 922, 191, 910, 532, 609, 829, 189, 20, 167, 29, 872, 449, 83, MotionTypeApps.TYPE_SHAKE_CHANGE_WALLPAPER, 41, 656, 505, 579, 481, 173, MotionTypeApps.TYPE_SHAKE_START_PRIVACY, 251, 688, 95, 497, 555, 642, 543, StatusLine.HTTP_TEMP_REDIRECT, 159, 924, 558, 648, 55, 497, 10}, new int[]{352, 77, 373, HwTransCodeEx.GET_PACKAGE_NAME_FOR_PID_TRANSACTION, 35, 599, 428, 207, 409, 574, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, 498, 285, 380, 350, 492, 197, 265, 920, 155, 914, 299, 229, 643, 294, 871, 306, 88, 87, 193, 352, 781, 846, 75, 327, 520, 435, 543, MotionTypeApps.TYPE_FLIP_MUTE_AOD, 666, 249, 346, 781, 621, 640, 268, 794, 534, 539, 781, 408, 390, 644, 102, 476, 499, 290, 632, 545, 37, 858, 916, 552, 41, 542, 289, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK, 272, 383, 800, 485, 98, 752, 472, 761, MetricConstant.BLUETOOTH_METRIC_ID_EX, 784, 860, 658, 741, 290, 204, 681, 407, 855, 85, 99, 62, 482, 180, 20, 297, 451, 593, 913, 142, 808, 684, 287, 536, 561, 76, 653, 899, 729, 567, 744, 390, 513, HwCarrierConfigManager.HD_ICON_MASK_DIALER, 516, 258, 240, 518, 794, 395, 768, 848, 51, 610, 384, 168, 190, 826, 328, 596, 786, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 570, 381, 415, 641, 156, 237, 151, 429, 531, 207, 676, 710, 89, 168, MotionTypeApps.TYPE_PROXIMITY_SPEAKER, MotionTypeApps.TYPE_SHAKE_CHANGE_WALLPAPER, 40, 708, 575, 162, 864, 229, 65, 861, 841, 512, 164, 477, 221, 92, 358, 785, 288, 357, 850, 836, 827, 736, 707, 94, 8, 494, 114, 521, 2, 499, 851, 543, 152, 729, 771, 95, 248, 361, 578, 323, 856, 797, 289, 51, 684, 466, 533, 820, 669, 45, 902, 452, 167, 342, 244, 173, 35, 463, 651, 51, 699, 591, 452, 578, 37, JlogConstantsEx.JLID_NEW_CONTACT_SAVE_CLICK, 298, 332, 552, 43, 427, JlogConstantsEx.JLID_CONTACT_MULTISELECT_BIND_VIEW, 662, 777, 475, 850, 764, 364, 578, 911, 283, 711, 472, 420, 245, 288, 594, 394, 511, 327, 589, 777, 699, 688, 43, 408, 842, 383, 721, 521, 560, 644, 714, 559, 62, 145, 873, 663, 713, 159, 672, 729, 624, 59, 193, 417, 158, 209, 563, 564, 343, 693, 109, 608, 563, 365, 181, 772, 677, 310, 248, 353, 708, 410, 579, 870, 617, 841, 632, 860, 289, 536, 35, 777, 618, 586, 424, 833, 77, 597, 346, 269, 757, 632, 695, 751, 331, 247, 184, 45, 787, 680, 18, 66, 407, 369, 54, 492, 228, 613, 830, 922, 437, 519, 644, 905, 789, 420, MotionTypeApps.TYPE_PROXIMITY_BLUETOOTHSET, 441, 207, 300, 892, 827, JlogConstantsEx.JLID_MMS_MATCHED_CONTACTS_SEARCH, 537, 381, 662, 513, 56, 252, 341, 242, 797, 838, 837, 720, 224, StatusLine.HTTP_TEMP_REDIRECT, 631, 61, 87, 560, 310, 756, 665, 397, 808, 851, 309, 473, 795, 378, 31, 647, 915, 459, 806, 590, 731, 425, 216, 548, 249, 321, 881, 699, 535, 673, 782, 210, 815, 905, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 843, 922, 281, 73, 469, 791, 660, 162, 498, StatusLine.HTTP_PERM_REDIRECT, 155, 422, 907, 817, 187, 62, 16, 425, 535, 336, 286, 437, 375, 273, 610, 296, 183, 923, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW, 667, 751, 353, 62, 366, 691, 379, 687, 842, 37, 357, 720, 742, 330, 5, 39, 923, 311, 424, 242, 749, 321, 54, 669, 316, 342, 299, 534, 105, 667, 488, 640, 672, 576, 540, 316, 486, 721, 610, 46, 656, 447, 171, 616, 464, 190, 531, 297, 321, 762, 752, 533, 175, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 14, 381, 433, 717, 45, 111, 20, 596, 284, 736, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 646, 411, 877, 669, JlogConstantsEx.JLID_MMS_MATCHED_CONTACTS_SEARCH, 919, 45, 780, 407, 164, 332, 899, 165, 726, 600, 325, 498, 655, 357, 752, 768, 223, 849, 647, 63, 310, 863, 251, 366, MotionTypeApps.TYPE_PROXIMITY_SPEAKER, 282, 738, 675, 410, 389, 244, 31, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 263}};
    }

    private PDF417ErrorCorrection() {
    }

    static int getErrorCorrectionCodewordCount(int errorCorrectionLevel) {
        if (errorCorrectionLevel >= 0 && errorCorrectionLevel <= 8) {
            return 1 << (errorCorrectionLevel + 1);
        }
        throw new IllegalArgumentException("Error correction level must be between 0 and 8!");
    }

    static int getRecommendedMinimumErrorCorrectionLevel(int n) throws WriterException {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        } else if (n <= 40) {
            return 2;
        } else {
            if (n <= 160) {
                return 3;
            }
            if (n <= 320) {
                return 4;
            }
            if (n <= 863) {
                return 5;
            }
            throw new WriterException("No recommendation possible");
        }
    }

    static String generateErrorCorrection(CharSequence dataCodewords, int errorCorrectionLevel) {
        int k = getErrorCorrectionCodewordCount(errorCorrectionLevel);
        char[] e = new char[k];
        int sld = dataCodewords.length();
        for (int i = 0; i < sld; i++) {
            int t1 = (dataCodewords.charAt(i) + e[e.length - 1]) % PDF417Common.NUMBER_OF_CODEWORDS;
            for (int j = k - 1; j >= 1; j--) {
                e[j] = (char) ((e[j - 1] + (929 - ((EC_COEFFICIENTS[errorCorrectionLevel][j] * t1) % PDF417Common.NUMBER_OF_CODEWORDS))) % PDF417Common.NUMBER_OF_CODEWORDS);
            }
            e[0] = (char) ((929 - ((EC_COEFFICIENTS[errorCorrectionLevel][0] * t1) % PDF417Common.NUMBER_OF_CODEWORDS)) % PDF417Common.NUMBER_OF_CODEWORDS);
        }
        StringBuilder sb = new StringBuilder(k);
        for (int j2 = k - 1; j2 >= 0; j2--) {
            if (e[j2] != 0) {
                e[j2] = (char) (929 - e[j2]);
            }
            sb.append(e[j2]);
        }
        return sb.toString();
    }
}
