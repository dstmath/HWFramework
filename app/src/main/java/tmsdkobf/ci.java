package tmsdkobf;

/* compiled from: Unknown */
public final class ci extends fs {
    public String url;

    public ci() {
        this.url = "";
    }

    public fs newInit() {
        return new ci();
    }

    public void readFrom(fq fqVar) {
        this.url = fqVar.a(0, false);
    }

    public void writeTo(fr frVar) {
        if (this.url != null) {
            frVar.a(this.url, 0);
        }
    }
}
