package tmsdkobf;

/* compiled from: Unknown */
public abstract class pr {

    /* compiled from: Unknown */
    public interface a {
        void a(boolean z, int i, int i2);
    }

    /* compiled from: Unknown */
    public static class b {
        private int Ix;
        private String Iy;
        private int mPort;

        public b(String str, int i) {
            this.Iy = str;
            this.mPort = i;
        }

        public b(String str, int i, int i2) {
            this.Ix = i2;
            this.Iy = str;
            this.mPort = i;
        }

        protected Object clone() throws CloneNotSupportedException {
            return new b(this.Iy, this.mPort, this.Ix);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            b bVar = (b) obj;
            return bVar.Iy.equals(this.Iy) && bVar.mPort == this.mPort;
        }

        public String fZ() {
            return this.Iy;
        }

        public int getPort() {
            return this.mPort;
        }

        public int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return this.mPort < 0 ? this.Iy : this.Iy + ":" + this.mPort;
        }
    }
}
