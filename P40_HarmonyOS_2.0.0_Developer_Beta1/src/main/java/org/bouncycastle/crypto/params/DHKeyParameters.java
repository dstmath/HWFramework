package org.bouncycastle.crypto.params;

public class DHKeyParameters extends AsymmetricKeyParameter {
    private DHParameters params;

    protected DHKeyParameters(boolean z, DHParameters dHParameters) {
        super(z);
        this.params = dHParameters;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DHKeyParameters)) {
            return false;
        }
        DHParameters dHParameters = this.params;
        DHParameters parameters = ((DHKeyParameters) obj).getParameters();
        return dHParameters == null ? parameters == null : dHParameters.equals(parameters);
    }

    public DHParameters getParameters() {
        return this.params;
    }

    public int hashCode() {
        int i = !isPrivate();
        DHParameters dHParameters = this.params;
        return dHParameters != null ? i ^ dHParameters.hashCode() : i;
    }
}
