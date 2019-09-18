package java.security.spec;

public abstract class EncodedKeySpec implements KeySpec {
    private byte[] encodedKey;

    public abstract String getFormat();

    public EncodedKeySpec(byte[] encodedKey2) {
        this.encodedKey = (byte[]) encodedKey2.clone();
    }

    public byte[] getEncoded() {
        return (byte[]) this.encodedKey.clone();
    }
}
