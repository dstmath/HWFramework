package tmsdkobf;

/* compiled from: Unknown */
public final class bl extends fs {
    public long dF;

    public bl() {
        this.dF = 0;
    }

    public fs newInit() {
        return new bl();
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
