package javax.crypto.spec;

public class PSource {
    private String pSrcName;

    public static final class PSpecified extends PSource {
        public static final PSpecified DEFAULT = new PSpecified(new byte[0]);
        private byte[] p = new byte[0];

        public PSpecified(byte[] p2) {
            super("PSpecified");
            this.p = (byte[]) p2.clone();
        }

        public byte[] getValue() {
            return this.p.length == 0 ? this.p : (byte[]) this.p.clone();
        }
    }

    protected PSource(String pSrcName2) {
        if (pSrcName2 != null) {
            this.pSrcName = pSrcName2;
            return;
        }
        throw new NullPointerException("pSource algorithm is null");
    }

    public String getAlgorithm() {
        return this.pSrcName;
    }
}
