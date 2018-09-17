package com.android.org.bouncycastle.crypto.params;

public class DHKeyParameters extends AsymmetricKeyParameter {
    private DHParameters params;

    protected DHKeyParameters(boolean isPrivate, DHParameters params) {
        super(isPrivate);
        this.params = params;
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
        int code = isPrivate() ? 0 : 1;
        if (this.params != null) {
            return code ^ this.params.hashCode();
        }
        return code;
    }
}
