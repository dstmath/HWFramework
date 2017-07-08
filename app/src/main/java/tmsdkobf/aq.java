package tmsdkobf;

/* compiled from: Unknown */
public final class aq extends fs {
    public String t;

    public aq() {
        this.t = "";
    }

    public fs newInit() {
        return new aq();
    }

    public void readFrom(fq fqVar) {
        this.t = fqVar.a(0, false);
    }

    public void writeTo(fr frVar) {
        if (this.t != null) {
            frVar.a(this.t, 0);
        }
    }
}
