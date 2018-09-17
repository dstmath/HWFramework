package tmsdkobf;

class jt {
    private static Object lock = new Object();
    private static jt tx;
    private jx nZ = ((kf) fj.D(9)).getPreferenceService("soft_list_sp_name");

    private jt() {
    }

    public static jt cH() {
        if (tx == null) {
            synchronized (lock) {
                if (tx == null) {
                    tx = new jt();
                }
            }
        }
        return tx;
    }

    public void ag(int i) {
        this.nZ.putInt("soft_list_profile_full_quantity_", i);
    }

    public boolean cI() {
        return this.nZ.getBoolean("soft_list_profile_full_upload_", false);
    }

    public int cz() {
        return this.nZ.getInt("soft_list_profile_full_quantity_", 0);
    }

    public void k(boolean z) {
        this.nZ.putBoolean("soft_list_profile_full_upload_", z);
    }
}
