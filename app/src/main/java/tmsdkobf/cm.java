package tmsdkobf;

/* compiled from: Unknown */
public final class cm extends fs {
    public int fr;

    public cm() {
        this.fr = 1;
    }

    public void readFrom(fq fqVar) {
        this.fr = fqVar.a(this.fr, 0, false);
    }

    public void writeTo(fr frVar) {
        if (1 != this.fr) {
            frVar.write(this.fr, 0);
        }
    }
}
