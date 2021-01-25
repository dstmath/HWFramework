package ohos.global.i18n.utils;

import com.huawei.tmr.util.TMRManagerProxy;
import java.util.Date;

public class TextRecognitionUtilsImpl extends TextRecognitionUtils {
    public static int[] getAddress(String str) {
        return TMRManagerProxy.getAddress(str);
    }

    public static Date[] convertDate(String str, long j) {
        return TMRManagerProxy.convertDate(str, j);
    }

    public static int[] getTime(String str) {
        return TMRManagerProxy.getTime(str);
    }

    public static int[] getMatchedPhoneNumber(String str, String str2) {
        return TMRManagerProxy.getMatchedPhoneNumber(str, str2);
    }
}
