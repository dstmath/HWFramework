package com.android.org.bouncycastle.crypto.params;

public class DHKeyParameters extends AsymmetricKeyParameter {
    private DHParameters params;

    protected DHKeyParameters(boolean isPrivate, DHParameters params2) {
        super(isPrivate);
        this.params = params2;
    }

    public DHParameters getParameters() {
        return this.params;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DHKeyParameters)) {
            return false;
        }
        DHKeyParameters dhKey = (DHKeyParameters) obj;
        if (this.params != null) {
            return this.params.equals(dhKey.getParameters());
        }
        if (dhKey.getParameters() == null) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int code = isPrivate() ^ 1;
        return this.params != null ? (int) (code ^ this.params.hashCode()) : (int) code;
    }
}
