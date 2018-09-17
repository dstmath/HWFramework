package tmsdkobf;

public class mv {
    private static mv Bt = null;
    private md vu = new md("Optimus");

    private mv() {
    }

    public static mv fj() {
        if (Bt == null) {
            Class cls = mv.class;
            synchronized (mv.class) {
                if (Bt == null) {
                    Bt = new mv();
                }
            }
        }
        return Bt;
    }

    public static void stop() {
        Class cls = mv.class;
        synchronized (mv.class) {
            Bt = null;
        }
    }

    public long fk() {
        return this.vu.getLong("optimus_fake_station_time", 0);
    }

    public void v(long j) {
        this.vu.a("optimus_fake_sms_time", j, true);
    }

    public void w(long j) {
        this.vu.a("optimus_fake_station_time", j, true);
    }
}
