package com.android.internal.http.multipart;

public abstract class PartBase extends Part {
    private String charSet;
    private String contentType;
    private String name;
    private String transferEncoding;

    public PartBase(String name2, String contentType2, String charSet2, String transferEncoding2) {
        if (name2 != null) {
            this.name = name2;
            this.contentType = contentType2;
            this.charSet = charSet2;
            this.transferEncoding = transferEncoding2;
            return;
        }
        throw new IllegalArgumentException("Name must not be null");
    }

    @Override // com.android.internal.http.multipart.Part
    public String getName() {
        return this.name;
    }

    @Override // com.android.internal.http.multipart.Part
    public String getContentType() {
        return this.contentType;
    }

    @Override // com.android.internal.http.multipart.Part
    public String getCharSet() {
        return this.charSet;
    }

    @Override // com.android.internal.http.multipart.Part
    public String getTransferEncoding() {
        return this.transferEncoding;
    }

    public void setCharSet(String charSet2) {
        this.charSet = charSet2;
    }

    public void setContentType(String contentType2) {
        this.contentType = contentType2;
    }

    public void setName(String name2) {
        if (name2 != null) {
            this.name = name2;
            return;
        }
        throw new IllegalArgumentException("Name must not be null");
    }

    public void setTransferEncoding(String transferEncoding2) {
        this.transferEncoding = transferEncoding2;
    }
}
