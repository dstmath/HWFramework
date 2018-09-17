package javax.crypto.spec;

public class PSource {
    private String pSrcName;

    public static final class PSpecified extends PSource {
        public static final PSpecified DEFAULT = new PSpecified(new byte[0]);
        private byte[] p = new byte[0];

        public PSpecified(byte[] p) {
            super("PSpecified");
            this.p = (byte[]) p.clone();
        }

        public byte[] getValue() {
            return this.p.length == 0 ? this.p : (byte[]) this.p.clone();
        }
    }

    protected PSource(String pSrcName) {
        if (pSrcName == null) {
            throw new NullPointerException("pSource algorithm is null");
        }
        this.pSrcName = pSrcName;
    }

    public String getAlgorithm() {
        return this.pSrcName;
    }
}
