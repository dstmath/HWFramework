package tmsdkobf;

/* compiled from: Unknown */
public final class cn extends fs {
    public String fs;
    public String ft;

    public cn() {
        this.fs = "";
        this.ft = "";
    }

    public void readFrom(fq fqVar) {
        this.fs = fqVar.a(0, false);
        this.ft = fqVar.a(1, false);
    }

    public void writeTo(fr frVar) {
        if (this.fs != null) {
            frVar.a(this.fs, 0);
        }
        if (this.ft != null) {
            frVar.a(this.ft, 1);
        }
    }
}
