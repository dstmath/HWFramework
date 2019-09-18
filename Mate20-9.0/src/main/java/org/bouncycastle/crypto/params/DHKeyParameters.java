package org.bouncycastle.crypto.params;

public class DHKeyParameters extends AsymmetricKeyParameter {
    private DHParameters params;

    protected DHKeyParameters(boolean z, DHParameters dHParameters) {
        super(z);
        this.params = dHParameters;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DHKeyParameters)) {
            return false;
        }
        DHKeyParameters dHKeyParameters = (DHKeyParameters) obj;
        if (this.params != null) {
            return this.params.equals(dHKeyParameters.getParameters());
        }
        if (dHKeyParameters.getParameters() == null) {
            z = true;
        }
        return z;
    }

    public DHParameters getParameters() {
        return this.params;
    }

    public int hashCode() {
        boolean z = !isPrivate();
        return this.params != null ? z ^ this.params.hashCode() ? 1 : 0 : z ? 1 : 0;
    }
}
