package defpackage;

/* renamed from: g */
public class g {
    public String p;
    public Object q;
    public Class r;

    public g(String str, Class cls, Object obj) {
        this.p = str;
        this.r = cls;
        this.q = obj;
    }

    public g(String str, Class cls, String str2) {
        this.p = str;
        this.r = cls;
        a(str2);
    }

    public void a(String str) {
        if (String.class == this.r) {
            this.q = str;
        } else if (Integer.class == this.r) {
            this.q = Integer.valueOf(Integer.parseInt(str));
        } else if (Long.class == this.r) {
            this.q = Long.valueOf(Long.parseLong(str));
        } else if (Boolean.class == this.r) {
            this.q = Boolean.valueOf(Boolean.parseBoolean(str));
        } else {
            this.q = null;
        }
    }

    public String toString() {
        return new StringBuffer().append(this.p).append(":").append(this.q).append(":").append(this.r.getSimpleName()).toString();
    }
}
