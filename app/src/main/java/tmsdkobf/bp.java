package tmsdkobf;

/* compiled from: Unknown */
public final class bp extends fs {
    public long dF;

    public bp() {
        this.dF = 0;
    }

    public fs newInit() {
        return new bp();
    }

    public void readFrom(fq fqVar) {
        this.dF = fqVar.a(this.dF, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.dF != 0) {
            frVar.b(this.dF, 0);
        }
    }
}
