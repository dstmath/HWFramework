package tmsdkobf;

/* compiled from: Unknown */
public final class bo extends fs {
    public String r;

    public bo() {
        this.r = "";
    }

    public fs newInit() {
        return new bo();
    }

    public void readFrom(fq fqVar) {
        this.r = fqVar.a(0, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.r, 0);
    }
}
