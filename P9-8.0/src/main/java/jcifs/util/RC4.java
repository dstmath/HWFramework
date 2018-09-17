package jcifs.util;

public class RC4 {
    int i;
    int j;
    byte[] s;

    public RC4(byte[] key) {
        init(key, 0, key.length);
    }

    public void init(byte[] key, int ki, int klen) {
        this.s = new byte[256];
        this.i = 0;
        while (this.i < 256) {
            this.s[this.i] = (byte) this.i;
            this.i++;
        }
        this.j = 0;
        this.i = 0;
        while (this.i < 256) {
            this.j = ((this.j + key[(this.i % klen) + ki]) + this.s[this.i]) & 255;
            byte t = this.s[this.i];
            this.s[this.i] = this.s[this.j];
            this.s[this.j] = t;
            this.i++;
        }
        this.j = 0;
        this.i = 0;
    }

    public void update(byte[] src, int soff, int slen, byte[] dst, int doff) {
        int slim = soff + slen;
        int doff2 = doff;
        int soff2 = soff;
        while (soff2 < slim) {
            this.i = (this.i + 1) & 255;
            this.j = (this.j + this.s[this.i]) & 255;
            byte t = this.s[this.i];
            this.s[this.i] = this.s[this.j];
            this.s[this.j] = t;
            doff = doff2 + 1;
            soff = soff2 + 1;
            dst[doff2] = (byte) (src[soff2] ^ this.s[(this.s[this.i] + this.s[this.j]) & 255]);
            doff2 = doff;
            soff2 = soff;
        }
    }
}
