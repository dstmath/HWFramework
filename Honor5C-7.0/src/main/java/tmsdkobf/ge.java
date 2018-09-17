package tmsdkobf;

/* compiled from: Unknown */
public class ge {
    private lf oi;

    public ge() {
        this.oi = ((ln) fe.ad(9)).getPreferenceService("DeepCleanConfigDao");
    }

    public boolean aG() {
        return this.oi.getBoolean("is_use_cloud_list_v2", false);
    }

    public boolean aH() {
        return this.oi.getBoolean("is_report_sdcard_dir_v2", false);
    }

    public void p(boolean z) {
        this.oi.d("is_use_cloud_list_v2", z);
    }

    public void q(boolean z) {
        this.oi.d("is_report_sdcard_dir_v2", z);
    }
}
