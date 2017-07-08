package tmsdkobf;

/* compiled from: Unknown */
public final class bh extends fs {
    public int cs;
    public int ct;

    public bh() {
        this.cs = 0;
        this.ct = 0;
    }

    public fs newInit() {
        return new bh();
    }

    public void readFrom(fq fqVar) {
        this.cs = fqVar.a(this.cs, 0, true);
        this.ct = fqVar.a(this.ct, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.cs, 0);
        frVar.write(this.ct, 1);
    }
}
