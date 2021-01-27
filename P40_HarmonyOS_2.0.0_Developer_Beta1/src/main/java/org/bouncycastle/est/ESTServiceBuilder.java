package org.bouncycastle.est;

public class ESTServiceBuilder {
    protected ESTClientProvider clientProvider;
    protected String label;
    protected final String server;

    public ESTServiceBuilder(String str) {
        this.server = str;
    }

    public ESTService build() {
        return new ESTService(this.server, this.label, this.clientProvider);
    }

    public ESTServiceBuilder withClientProvider(ESTClientProvider eSTClientProvider) {
        this.clientProvider = eSTClientProvider;
        return this;
    }

    public ESTServiceBuilder withLabel(String str) {
        this.label = str;
        return this;
    }
}
