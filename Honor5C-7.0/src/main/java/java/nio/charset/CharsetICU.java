package java.nio.charset;

import libcore.icu.NativeConverter;

final class CharsetICU extends Charset {
    private final String icuCanonicalName;

    protected CharsetICU(String canonicalName, String icuCanonName, String[] aliases) {
        super(canonicalName, aliases);
        this.icuCanonicalName = icuCanonName;
    }

    public CharsetDecoder newDecoder() {
        return CharsetDecoderICU.newInstance(this, this.icuCanonicalName);
    }

    public CharsetEncoder newEncoder() {
        return CharsetEncoderICU.newInstance(this, this.icuCanonicalName);
    }

    public boolean contains(Charset cs) {
        if (cs == null) {
            return false;
        }
        if (equals(cs)) {
            return true;
        }
        return NativeConverter.contains(name(), cs.name());
    }
}
