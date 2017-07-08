package tmsdkobf;

/* compiled from: Unknown */
public final class oh extends fs {
    public boolean Ec;
    public int Ej;
    public float Ek;
    public int El;

    public oh() {
        this.Ej = 0;
        this.Ec = true;
        this.Ek = 0.0f;
        this.El = 0;
    }

    public fs newInit() {
        return new oh();
    }

    public void readFrom(fq fqVar) {
        this.Ej = fqVar.a(this.Ej, 0, true);
        this.Ec = fqVar.a(this.Ec, 1, true);
        this.Ek = fqVar.a(this.Ek, 2, true);
        this.El = fqVar.a(this.El, 3, true);
    }

    public String toString() {
        return "SCCloudResult [eCloudFakeType=" + this.Ej + ", bLastSmsIsFake=" + this.Ec + ", fCloudScore=" + this.Ek + ", usSmsType=" + this.El + "]";
    }

    public void writeTo(fr frVar) {
        frVar.write(this.Ej, 0);
        frVar.a(this.Ec, 1);
        frVar.a(this.Ek, 2);
        frVar.write(this.El, 3);
    }
}
