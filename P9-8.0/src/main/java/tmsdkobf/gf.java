package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;

public class gf {
    private static gf nO = null;
    private final String nP = "ConfigInfo";
    private final String nQ = "check_imsi";
    private final String nR = "rqd";
    private final String nS = "sk";
    private final String nT = "first_run_time";
    private final String nU = "app_code_version";
    private final String nV = "app_code_old_version";
    private final String nW = "report_usage_info_time";
    private kf nX = ((kf) fj.D(9));

    private gf(Context context) {
    }

    public static gf S() {
        if (nO == null) {
            Class cls = gf.class;
            synchronized (gf.class) {
                if (nO == null) {
                    nO = new gf(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return nO;
    }

    public void I(int i) {
        T().putInt("ae", i);
    }

    public void J(int i) {
        T().putInt("st_count", i);
    }

    public void K(int i) {
        T().putInt("wakeup_flag", i);
    }

    public jx T() {
        return this.nX.getPreferenceService("ConfigInfo");
    }

    public jx U() {
        return this.nX.getPreferenceService("sk");
    }

    public int V() {
        return T().getInt("ae", -1);
    }

    public long W() {
        return T().getLong("ad", 0);
    }

    public long X() {
        return T().getLong("sl", 0);
    }

    public Boolean Y() {
        return Boolean.valueOf(T().getBoolean("a_s", true));
    }

    public Boolean Z() {
        return Boolean.valueOf(T().getBoolean("sr_s", true));
    }

    public void a(long j) {
        T().putLong("ad", j);
    }

    public void a(Boolean bool) {
        T().putBoolean("a_s", bool.booleanValue());
    }

    public Boolean aa() {
        return Boolean.valueOf(T().getBoolean("opt_s", true));
    }

    public Boolean ab() {
        return Boolean.valueOf(T().getBoolean("ps_s", true));
    }

    public Boolean ac() {
        return Boolean.valueOf(T().getBoolean("ac_swi", false));
    }

    public Boolean ad() {
        return Boolean.valueOf(T().getBoolean("virus_update", false));
    }

    public long ae() {
        return T().getLong("st_lastime", 0);
    }

    public long af() {
        return T().getLong("st_vaildtime", 0);
    }

    public Boolean ag() {
        return Boolean.valueOf(T().getBoolean("roach_exist", false));
    }

    public long ah() {
        return T().getLong("sr_day_new7", 604800000);
    }

    public Boolean ai() {
        return Boolean.valueOf(T().getBoolean("w_repo_limit", false));
    }

    public Boolean aj() {
        return Boolean.valueOf(T().getBoolean("cmd_p", false));
    }

    public int ak() {
        return T().getInt("wakeup_flag");
    }

    public void b(long j) {
        T().putLong("sl", j);
    }

    public void b(Boolean bool) {
        T().putBoolean("sr_s", bool.booleanValue());
    }

    public void c(long j) {
        T().putLong("st_lastime", j);
    }

    public void c(Boolean bool) {
        T().putBoolean("wifi_s", bool.booleanValue());
    }

    public void d(long j) {
        T().putLong("st_vaildtime", j);
    }

    public void d(Boolean bool) {
        T().putBoolean("opt_s", bool.booleanValue());
    }

    public void e(long j) {
        T().putLong("sr_day_new7", j);
    }

    public void e(Boolean bool) {
        T().putBoolean("ps_s", bool.booleanValue());
    }

    public void f(Boolean bool) {
        T().putBoolean("ac_swi", bool.booleanValue());
    }

    public void g(Boolean bool) {
        T().putBoolean("per_get_processes", bool.booleanValue());
    }

    public int getStartCount() {
        return T().getInt("st_count", 0);
    }

    public void h(Boolean bool) {
        T().putBoolean("per_ops", bool.booleanValue());
    }

    public void i(Boolean bool) {
        T().putBoolean("per_sms_operate", bool.booleanValue());
    }

    public void j(Boolean bool) {
        T().putBoolean("ht_swi", bool.booleanValue());
    }

    public void k(Boolean bool) {
        T().putBoolean("virus_update", bool.booleanValue());
    }

    public void l(Boolean bool) {
        T().putBoolean("roach_exist", bool.booleanValue());
    }

    public void m(Boolean bool) {
        T().putBoolean("w_repo_limit", bool.booleanValue());
    }

    public void n(Boolean bool) {
        T().putBoolean("cmd_p", bool.booleanValue());
    }
}
