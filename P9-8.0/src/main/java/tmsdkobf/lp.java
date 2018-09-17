package tmsdkobf;

public class lp {
    private md vu = new md("CheckPoint");
    private StringBuffer yS = new StringBuffer();

    public void commit() {
        if (this.yS.length() > 0) {
            String string = this.vu.getString("data", null);
            if (string == null) {
                string = "";
            }
            this.vu.a("data", string + this.yS.toString(), true);
            this.yS = new StringBuffer();
        }
    }

    public void t(int i, int i2) {
        this.yS.append(i + ":" + i2 + ";");
    }
}
