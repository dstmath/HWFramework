package tmsdkobf;

/* compiled from: Unknown */
public final class az extends fs {
    public String bV;
    public String port;
    public int status;

    public az() {
        this.bV = "";
        this.port = "";
        this.status = 0;
    }

    public fs newInit() {
        return new az();
    }

    public void readFrom(fq fqVar) {
        this.bV = fqVar.a(0, true);
        this.port = fqVar.a(1, true);
        this.status = fqVar.a(this.status, 2, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.bV, 0);
        frVar.a(this.port, 1);
        if (this.status != 0) {
            frVar.write(this.status, 2);
        }
    }
}
