package tmsdkobf;

/* compiled from: Unknown */
public final class s extends fs {
    public int result;

    public s() {
        this.result = 0;
    }

    public fs newInit() {
        return new s();
    }

    public void readFrom(fq fqVar) {
        this.result = fqVar.a(this.result, 0, false);
    }

    public void writeTo(fr frVar) {
        if (this.result != 0) {
            frVar.write(this.result, 0);
        }
    }
}
