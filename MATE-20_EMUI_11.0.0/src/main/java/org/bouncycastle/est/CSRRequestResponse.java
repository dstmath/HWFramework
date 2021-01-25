package org.bouncycastle.est;

public class CSRRequestResponse {
    private final CSRAttributesResponse attributesResponse;
    private final Source source;

    public CSRRequestResponse(CSRAttributesResponse cSRAttributesResponse, Source source2) {
        this.attributesResponse = cSRAttributesResponse;
        this.source = source2;
    }

    public CSRAttributesResponse getAttributesResponse() {
        CSRAttributesResponse cSRAttributesResponse = this.attributesResponse;
        if (cSRAttributesResponse != null) {
            return cSRAttributesResponse;
        }
        throw new IllegalStateException("Response has no CSRAttributesResponse.");
    }

    public Object getSession() {
        return this.source.getSession();
    }

    public Source getSource() {
        return this.source;
    }

    public boolean hasAttributesResponse() {
        return this.attributesResponse != null;
    }
}
