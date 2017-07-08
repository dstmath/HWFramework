package tmsdkobf;

/* compiled from: Unknown */
public class rk {
    public boolean NZ;
    public long Oa;
    public boolean Ob;

    public rk() {
        this.NZ = false;
        this.Oa = 0;
        this.Ob = false;
    }

    public static rk dA(String str) {
        boolean z = false;
        if (str == null) {
            return null;
        }
        String[] split = str.split("\\|");
        if (split.length < 2) {
            return null;
        }
        try {
            int parseInt = Integer.parseInt(split[0]);
            int parseInt2 = Integer.parseInt(split[1]);
            if ((parseInt != 0 && parseInt != 1) || parseInt2 <= 0) {
                return null;
            }
            rk rkVar = new rk();
            if (parseInt == 0) {
                z = true;
            }
            rkVar.NZ = z;
            rkVar.Oa = (long) parseInt2;
            return rkVar;
        } catch (Exception e) {
            return null;
        }
    }
}
