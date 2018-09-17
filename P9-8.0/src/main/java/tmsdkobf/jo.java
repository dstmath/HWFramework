package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

public class jo {
    private static Object lock = new Object();
    private static jo tp;
    private jx nZ = ((kf) fj.D(9)).getPreferenceService("kv_profile_sp_name");
    private Boolean tq = null;

    private jo() {
    }

    public static jo cy() {
        if (tp == null) {
            synchronized (lock) {
                if (tp == null) {
                    tp = new jo();
                }
            }
        }
        return tp;
    }

    public void aj(int i) {
        this.nZ.putInt("kv_profile_full_quantity_", cz() + i);
    }

    public boolean cA() {
        if (this.tq == null) {
            this.tq = Boolean.valueOf(this.nZ.getBoolean("kv_profile_all_report", true));
        }
        return this.tq.booleanValue();
    }

    public int cz() {
        return this.nZ.getInt("kv_profile_full_quantity_", CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY);
    }

    public void j(boolean z) {
        this.tq = Boolean.valueOf(z);
        this.nZ.putBoolean("kv_profile_all_report", z);
    }
}
