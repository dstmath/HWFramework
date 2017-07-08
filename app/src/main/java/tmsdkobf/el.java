package tmsdkobf;

/* compiled from: Unknown */
public final class el extends fs {
    static ei kC;
    public ei jH;

    public el() {
        this.jH = null;
    }

    public void readFrom(fq fqVar) {
        if (kC == null) {
            kC = new ei();
        }
        this.jH = (ei) fqVar.a(kC, 1, true);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.jH, 1);
    }
}
