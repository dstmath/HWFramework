package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class ko {
    public static volatile boolean tK = false;
    public static List<String> tL = new ArrayList();

    public static String aY(String str) {
        String aX = km.aX(str);
        if (aX == null || aX.length() < 7) {
            return null;
        }
        cN();
        for (String str2 : tL) {
            if (aX.startsWith(str2)) {
                if (aX.length() == str2.length() + 5) {
                    return aX.substring(str2.length());
                }
                return null;
            }
        }
        return null;
    }

    public static void cN() {
        if (!tK) {
            Class cls = ko.class;
            synchronized (ko.class) {
                if (!tK) {
                    int[] iArr = new int[]{994, 993, 992, 991, 990, 979, 978, 977, 976, 975, 974, 973, 972, 971, 970, 955, 954, 953, 952, 951, 943, 941, 939, 938, 937, 936, 935, 934, 933, 932, 931, 930, 919, 917, 916, 915, 914, 913, 912, 911, 8988, 909, 908, 906, 903, 902, 901, 898, 897, 896, 895, 894, 893, 892, 891, 888, 887, 886, 883, 881, 879, 878, 877, 876, 875, 874, 873, 872, 871, 870, 859, 858, 857, 856, 855, 854, 853, 852, 851, 839, 838, 837, 836, 835, 834, 833, 832, 831, 830, 827, 826, 825, 818, 817, 816, 8982, 813, 812, 799, 798, 797, 796, 795, 794, 793, 792, 791, 790, 779, 778, 777, 776, 775, 774, 773, 772, 771, 770, 769, 768, 766, 763, 762, 760, 759, 758, 757, 756, 755, 754, 753, 752, 751, 750, 746, 745, 744, 743, 739, 738, 737, 736, 735, 734, 731, 730, 728, 724, 722, 719, 718, 717, 716, 715, 714, 713, 712, 711, 710, 701, 692, 691, 668, 663, 662, 660, 635, 634, 633, 632, 631, 599, 598, 597, 596, 595, 594, 593, 592, 591, 580, 579, 578, 577, 576, 575, 574, 573, 572, 571, 570, 566, 565, 564, 563, 562, 561, 559, 558, 557, 556, 555, 553, 552, 551, 550, 546, 543, 539, 538, 537, 536, 535, 534, 533, 532, 531, 530, 527, 523, 519, 518, 517, 516, 515, 514, 513, 512, 511, 510, 483, 482, 479, 478, 477, 476, 475, 474, 473, 472, 471, 470, 469, 468, 467, 464, 459, 458, 457, 456, 455, 454, 453, 452, 451, 8986, 439, 438, 437, 436, 435, 434, 433, 432, 431, 429, 427, 421, 419, 418, 417, 416, 415, 24, 413, 412, 411, 410, 398, 396, 395, 394, 393, 392, 391, 379, 378, 377, 376, 375, 374, 373, 372, 371, 370, 359, 358, 357, 356, 355, 354, 353, 350, 349, 335, SmsCheckResult.ESCT_319, SmsCheckResult.ESCT_318, SmsCheckResult.ESCT_317, SmsCheckResult.ESCT_316, SmsCheckResult.ESCT_315, SmsCheckResult.ESCT_314, SmsCheckResult.ESCT_313, SmsCheckResult.ESCT_312, SmsCheckResult.ESCT_311, SmsCheckResult.ESCT_310, 29, 28, 27, 25, 352, 351, 23, 22, 21, 20, 10, 995, 996, 997, 998, 999, 5125, 9712, 235, 237, 7572, 7578, 247, 245, 293, 298, 447, 554, 414, 440, 910};
                    int[] iArr2 = iArr;
                    int length = iArr.length;
                    for (int i = 0; i < length; i++) {
                        tL.add("0" + iArr2[i]);
                    }
                    tK = true;
                }
            }
        }
    }
}
