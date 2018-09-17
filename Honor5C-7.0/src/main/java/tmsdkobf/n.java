package tmsdkobf;

/* compiled from: Unknown */
public final class n extends fs {
    public int H;
    public int O;
    public long U;
    public long V;
    public int result;

    public n() {
        this.U = 0;
        this.V = 0;
        this.O = 0;
        this.H = 0;
        this.result = 0;
    }

    public fs newInit() {
        return new n();
    }

    public void readFrom(fq fqVar) {
        this.U = fqVar.a(this.U, 0, false);
        this.V = fqVar.a(this.V, 1, false);
        this.O = fqVar.a(this.O, 2, false);
        this.H = fqVar.a(this.H, 3, false);
        this.result = fqVar.a(this.result, 4, false);
    }

    public void writeTo(fr frVar) {
        if (this.U != 0) {
            frVar.b(this.U, 0);
        }
        if (this.V != 0) {
            frVar.b(this.V, 1);
        }
        if (this.O != 0) {
            frVar.write(this.O, 2);
        }
        frVar.write(this.H, 3);
        if (this.result != 0) {
            frVar.write(this.result, 4);
        }
    }
}
