package tmsdkobf;

import java.io.IOException;
import java.io.InputStream;

public abstract class on {

    public interface a {
        void a(boolean z, int i, int i2);
    }

    public static class b {
        private int Iw;
        private String Ix;
        private int mPort;

        public b(String str, int i) {
            this.Ix = str;
            this.mPort = i;
        }

        public b(String str, int i, int i2) {
            this.Iw = i2;
            this.Ix = str;
            this.mPort = i;
        }

        protected Object clone() throws CloneNotSupportedException {
            return new b(this.Ix, this.mPort, this.Iw);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            b bVar = (b) obj;
            return bVar.Ix.equals(this.Ix) && bVar.mPort == this.mPort;
        }

        public int getPort() {
            return this.mPort;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public String hd() {
            return this.Ix;
        }

        public String toString() {
            return this.mPort < 0 ? this.Ix : this.Ix + ":" + this.mPort;
        }
    }

    public static byte[] a(InputStream inputStream, int -l_4_I, int -l_7_I, a aVar) throws IOException {
        byte[] bArr = new byte[-l_7_I];
        int i = 0;
        int i2 = -l_7_I;
        while (i < -l_7_I && i2 > 0) {
            int read = inputStream.read(bArr, -l_4_I, i2);
            if (read > 0) {
                i += read;
                -l_4_I += read;
                i2 -= read;
                if (aVar != null) {
                    aVar.a(false, i, -l_7_I);
                }
            } else if (aVar != null) {
                aVar.a(true, i, -l_7_I);
            }
        }
        return i == -l_7_I ? bArr : null;
    }
}
