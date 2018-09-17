package tmsdkobf;

/* compiled from: Unknown */
public final class an extends fs {
    public String C;

    public an() {
        this.C = "";
    }

    public fs newInit() {
        return new an();
    }

    public void readFrom(fq fqVar) {
        this.C = fqVar.a(0, false);
    }

    public void writeTo(fr frVar) {
        if (this.C != null) {
            frVar.a(this.C, 0);
        }
    }
}
