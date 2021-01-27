package com.huawei.zxing.pdf417.encoder;

import com.huawei.android.app.admin.DevicePolicyManagerEnhancedEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.hardware.input.HwSideTouchManagerEx;
import com.huawei.android.os.HwTransCodeEx;
import com.huawei.android.server.SystemServiceEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.android.view.KeyEventEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.okhttp3.internal.http.StatusLine;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.pdf417.PDF417Common;
import huawei.android.hardware.fingerprint.FingerprintForMmiEx;

/* access modifiers changed from: package-private */
public final class PDF417ErrorCorrection {
    private static final int[][] EC_COEFFICIENTS = {new int[]{27, 917}, new int[]{522, 568, 723, 809}, new int[]{237, StatusLine.HTTP_PERM_REDIRECT, 436, 284, 646, 653, 428, 379}, new int[]{274, 562, 232, 755, 599, 524, MotionTypeApps.TYPE_POCKET_CALL_RAISE, 132, 295, 116, 442, 428, 295, 42, IccConstantsEx.SMS_RECORD_LENGTH, 65}, new int[]{361, 575, 922, 525, IccConstantsEx.SMS_RECORD_LENGTH, 586, 640, 321, 536, 742, 677, 742, 687, 284, 193, 517, 273, 494, 263, 147, 593, 800, 571, 320, 803, 133, 231, 390, 685, 330, 63, 410}, new int[]{539, 422, 6, 93, 862, 771, 453, 106, 610, 287, 107, 505, 733, 877, 381, 612, 723, 476, 462, 172, 430, 609, 858, 822, 543, 376, KeyEventEx.KEYCODE_FINGERPRINT_UP, 400, 672, 762, 283, 184, 440, 35, 519, 31, 460, 594, 225, 535, 517, 352, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 158, 651, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 488, 502, 648, 733, 717, 83, 404, 97, 280, 771, 840, 629, 4, 381, 843, 623, 264, 543}, new int[]{521, 310, 864, 547, 858, 580, 296, 379, 53, 779, 897, 444, 400, 925, 749, 415, 822, 93, 217, 208, PDF417Common.MAX_CODEWORDS_IN_BARCODE, 244, 583, 620, 246, 148, 447, 631, 292, FingerprintForMmiEx.MMI_TYPE_SET_UI_UPDATE_COMPLETE, 490, 704, 516, 258, 457, FingerprintForMmiEx.MMI_TYPE_GET_HIGHLIGHT_APLHA, 594, 723, 674, 292, 272, 96, 684, DevicePolicyManagerEnhancedEx.PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER, 686, HwTransCodeEx.IS_TOP_SPLIT_ACTIVITY_TRANSACTION, 860, 569, 193, 219, 129, 186, 236, 287, 192, 775, 278, 173, 40, 379, KeyEventEx.KEYCODE_SWING_SWIPE_UP, 463, 646, 776, 171, 491, 297, 763, 156, 732, 95, 270, 447, 90, 507, 48, 228, 821, 808, 898, 784, 663, 627, 378, 382, 262, 380, 602, 754, 336, 89, 614, 87, DevicePolicyManagerEnhancedEx.PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER, 670, 616, 157, 374, 242, 726, 600, 269, 375, 898, 845, 454, 354, 130, 814, 587, 804, 34, 211, 330, 539, 297, 827, 865, 37, 517, 834, 315, SystemServiceEx.PHASE_ACTIVITY_MANAGER_READY, 86, MotionTypeApps.TYPE_POCKET_CALL_RAISE, 4, 108, 539}, new int[]{524, 894, 75, 766, 882, 857, 74, HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING, 82, 586, 708, 250, FingerprintForMmiEx.MMI_TYPE_GET_MESSAGE_COLOR, 786, HwSideTouchManagerEx.VOLUME_MODE_STEP, 720, 858, 194, 311, 913, 275, 190, 375, 850, 438, 733, 194, 280, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 280, 828, 757, KeyEventEx.KEYCODE_SWING_SWIPE_LEFT, 814, 919, 89, 68, 569, 11, HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING, 796, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 540, 913, MotionTypeApps.TYPE_POCKET_CALL_RAISE, 700, 799, 137, 439, 418, 592, 668, 353, 859, 370, 694, 325, 240, 216, 257, 284, 549, 209, 884, 315, 70, 329, 793, 490, 274, 877, 162, 749, 812, 684, 461, 334, 376, 849, 521, StatusLine.HTTP_TEMP_REDIRECT, 291, 803, KeyEventEx.KEYCODE_SWING_SWIPE_UP, 19, 358, 399, FingerprintForMmiEx.MMI_TYPE_SET_UI_UPDATE_COMPLETE, 103, KeyEventEx.KEYCODE_FINGERPRINT_UP, 51, 8, 517, 225, 289, 470, 637, 731, 66, 255, 917, 269, 463, 830, 730, 433, 848, 585, 136, 538, FingerprintForMmiEx.MMI_TYPE_GET_HIGHLIGHT_LEVEL, 90, 2, 290, 743, HwFoldScreenManagerEx.POSTURE_OTHER, 655, FingerprintForMmiEx.MMI_TYPE_GET_HIGHLIGHT_COLOR, 329, 49, MotionTypeApps.TYPE_POCKET_AOD, 580, 355, 588, 188, 462, 10, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 628, 320, 479, 130, 739, 71, 263, 318, 374, 601, 192, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 142, 673, 687, 234, 722, 384, 177, 752, 607, 640, 455, 193, 689, 707, 805, 641, 48, 60, 732, 621, 895, 544, 261, 852, 655, 309, 697, 755, 756, 60, 231, 773, 434, 421, 726, 528, 503, 118, 49, 795, 32, 144, 500, 238, 836, 394, 280, 566, 319, 9, 647, SystemServiceEx.PHASE_ACTIVITY_MANAGER_READY, 73, 914, 342, 126, 32, 681, 331, 792, 620, 60, 609, 441, 180, 791, 893, 754, HwTransCodeEx.REMOVE_FROM_ENTRY_STACK_TRANSACTION, 383, 228, 749, 760, 213, 54, 297, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 54, 834, 299, 922, 191, 910, 532, 609, 829, 189, 20, 167, 29, 872, 449, 83, 402, 41, 656, 505, 579, 481, 173, 404, 251, 688, 95, 497, 555, 642, 543, StatusLine.HTTP_TEMP_REDIRECT, 159, 924, 558, 648, 55, 497, 10}, new int[]{352, 77, 373, HwTransCodeEx.GET_PACKAGE_NAME_FOR_PID_TRANSACTION, 35, 599, 428, 207, 409, 574, 118, 498, 285, 380, 350, 492, 197, 265, 920, 155, 914, 299, 229, 643, 294, 871, 306, 88, 87, 193, 352, 781, 846, 75, 327, 520, 435, 543, MotionTypeApps.TYPE_FLIP_MUTE_AOD, 666, 249, 346, 781, 621, 640, 268, 794, 534, 539, 781, 408, 390, 644, 102, 476, 499, 290, 632, 545, 37, 858, 916, 552, 41, 542, 289, 122, 272, 383, 800, 485, 98, 752, 472, 761, 107, 784, 860, 658, 741, 290, HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING, 681, 407, 855, 85, 99, 62, 482, 180, 20, 297, 451, 593, 913, 142, 808, 684, 287, 536, 561, 76, 653, 899, 729, 567, 744, 390, KeyEventEx.KEYCODE_FINGERPRINT_LEFT, 192, 516, 258, 240, 518, 794, 395, 768, 848, 51, 610, 384, 168, 190, 826, 328, 596, 786, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 570, 381, 415, 641, 156, 237, 151, 429, 531, 207, 676, KeyEventEx.KEYCODE_SWING_SWIPE_LEFT, 89, 168, MotionTypeApps.TYPE_PROXIMITY_SPEAKER, 402, 40, 708, 575, 162, 864, 229, 65, 861, 841, 512, 164, 477, 221, 92, 358, 785, 288, 357, 850, 836, 827, 736, 707, 94, 8, 494, 114, 521, 2, 499, 851, 543, 152, 729, 771, 95, 248, 361, 578, 323, 856, 797, 289, 51, 684, 466, 533, 820, 669, 45, FingerprintForMmiEx.MMI_TYPE_GET_HIGHLIGHT_SIZE, 452, 167, 342, 244, 173, 35, 463, 651, 51, 699, 591, 452, 578, 37, 124, 298, 332, 552, 43, 427, 119, 662, 777, 475, 850, 764, 364, 578, FingerprintForMmiEx.MMI_TYPE_GET_LOCATION_CIRCLE_COUNT, 283, KeyEventEx.KEYCODE_SWING_SWIPE_RIGHT, 472, 420, 245, 288, 594, 394, KeyEventEx.KEYCODE_FINGERPRINT_UP, 327, 589, 777, 699, 688, 43, 408, 842, 383, 721, 521, 560, 644, KeyEventEx.KEYCODE_SWING_SWIPE_PUSH, 559, 62, 145, 873, 663, KeyEventEx.KEYCODE_SWING_SWIPE_DOWN, 159, 672, 729, 624, 59, 193, 417, 158, 209, 563, 564, 343, 693, 109, 608, 563, 365, 181, 772, 677, 310, 248, 353, 708, 410, 579, 870, 617, 841, 632, 860, 289, 536, 35, 777, 618, 586, 424, 833, 77, 597, 346, 269, 757, 632, 695, 751, 331, 247, 184, 45, 787, 680, 18, 66, 407, 369, 54, 492, 228, 613, 830, 922, 437, 519, 644, FingerprintForMmiEx.MMI_TYPE_GET_MESSAGE_COLOR, 789, 420, MotionTypeApps.TYPE_PROXIMITY_BLUETOOTHSET, 441, 207, 300, 892, 827, 141, 537, 381, 662, KeyEventEx.KEYCODE_FINGERPRINT_LEFT, 56, 252, 341, 242, 797, 838, 837, 720, 224, StatusLine.HTTP_TEMP_REDIRECT, 631, 61, 87, 560, 310, 756, 665, 397, 808, 851, 309, 473, 795, 378, 31, 647, 915, 459, 806, 590, 731, 425, 216, 548, 249, 321, 881, 699, 535, 673, 782, 210, 815, FingerprintForMmiEx.MMI_TYPE_GET_MESSAGE_COLOR, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 843, 922, 281, 73, 469, 791, 660, 162, 498, StatusLine.HTTP_PERM_REDIRECT, 155, 422, FingerprintForMmiEx.MMI_TYPE_GET_HIGHLIGHT_APLHA, 817, 187, 62, 16, 425, 535, 336, 286, 437, 375, 273, 610, 296, 183, 923, 116, 667, 751, 353, 62, 366, 691, 379, 687, 842, 37, 357, 720, 742, 330, 5, 39, 923, 311, 424, 242, 749, 321, 54, 669, 316, 342, 299, 534, 105, 667, 488, 640, 672, 576, 540, 316, 486, 721, 610, 46, 656, 447, 171, 616, 464, 190, 531, 297, 321, 762, 752, 533, 175, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 14, 381, 433, 717, 45, 111, 20, 596, 284, 736, HwSideTouchManagerEx.VOLUME_MODE_STEP, 646, JlogConstantsEx.JLID_INPUTMETHOD_SHOW_PANEL_TIMEOUT, 877, 669, 141, 919, 45, 780, 407, 164, 332, 899, 165, 726, 600, 325, 498, 655, 357, 752, 768, 223, 849, 647, 63, 310, 863, 251, 366, MotionTypeApps.TYPE_PROXIMITY_SPEAKER, 282, 738, 675, 410, 389, 244, 31, 121, MotionTypeApps.TYPE_PROXIMITY_SCREEN_OFF, 263}};

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
