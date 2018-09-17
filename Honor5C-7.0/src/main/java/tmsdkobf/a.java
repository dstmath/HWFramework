package tmsdkobf;

/* compiled from: Unknown */
public final class a extends fs {
    public int hash;

    public a() {
        this.hash = 0;
    }

    public fs newInit() {
        return new a();
    }

    public void readFrom(fq fqVar) {
        this.hash = fqVar.a(this.hash, 0, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.hash, 0);
    }
}
