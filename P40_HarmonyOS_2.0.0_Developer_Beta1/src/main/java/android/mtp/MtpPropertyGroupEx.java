package android.mtp;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class MtpPropertyGroupEx {
    private MtpPropertyGroupEx() {
    }

    public static String formatDateTime(long seconds) {
        return MtpPropertyGroup.format_date_time(seconds);
    }
}
