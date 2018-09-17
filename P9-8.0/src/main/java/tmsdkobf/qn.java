package tmsdkobf;

import tmsdk.common.TMServiceFactory;

public class qn {
    private jx NX = TMServiceFactory.getPreferenceService("DeepCleanConfigDao");

    public void J(long j) {
        this.NX.putLong("profile_last_get_data_sucess_time", j);
    }

    public void K(long j) {
        this.NX.putLong("dir_last_get_data_sucess_time", j);
    }

    public void W(boolean z) {
        this.NX.putBoolean("is_use_cloud_list_v2", z);
    }

    public void X(boolean z) {
        this.NX.putBoolean("is_report_sdcard_dir_v2", z);
    }

    public boolean jv() {
        return this.NX.getBoolean("is_use_cloud_list_v2", false);
    }

    public boolean jw() {
        return this.NX.getBoolean("is_report_sdcard_dir_v2", false);
    }

    public long jx() {
        return this.NX.getLong("profile_last_get_data_sucess_time", 0);
    }

    public long jy() {
        return this.NX.getLong("dir_last_get_data_sucess_time", 0);
    }
}
