package defpackage;

import java.io.InputStream;

/* renamed from: ab */
class ab extends InputStream {
    private InputStream aC;
    final /* synthetic */ aa aD;
    private byte[] buff;
    private int currIndex;

    public ab(aa aaVar, InputStream inputStream) {
        this.aD = aaVar;
        this.buff = null;
        this.currIndex = 0;
        this.aC = inputStream;
    }

    public int read() {
        synchronized (this.aD) {
            if (this.aD.isInitialized) {
                byte[] bArr;
                int i;
                if (this.buff != null && this.buff.length > 0) {
                    if (this.currIndex < this.buff.length) {
                        bArr = this.buff;
                        i = this.currIndex;
                        this.currIndex = i + 1;
                        return bArr[i] & 255;
                    }
                    aw.d("PushLog2828", "bufferByte has read end , need read bytes from socket");
                }
                this.buff = null;
                this.currIndex = 0;
                if (this.aC != null) {
                    i = this.aC.read();
                    if (-1 == i) {
                        aw.w("PushLog2828", "read -1 from inputstream");
                        return -1;
                    } else if (48 == i) {
                        this.buff = bj.c(aa.e(this.aC), aa.aA);
                        if (this.buff == null) {
                            aw.w("PushLog2828", "ase decrypt serverkey error");
                            return -1;
                        }
                        bArr = this.buff;
                        i = this.currIndex;
                        this.currIndex = i + 1;
                        return bArr[i] & 255;
                    } else {
                        aw.w("PushLog2828", "read secure message error, return -1");
                        return -1;
                    }
                }
                aw.w("PushLog2828", "secureInputStream is null, return -1");
                return -1;
            }
            aw.e("PushLog2828", "secure socket is not initialized, can not read any data");
            return -1;
        }
    }
}
