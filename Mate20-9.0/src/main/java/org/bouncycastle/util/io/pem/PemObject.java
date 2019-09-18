package org.bouncycastle.util.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PemObject implements PemObjectGenerator {
    private static final List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());
    private byte[] content;
    private List headers;
    private String type;

    public PemObject(String str, List list, byte[] bArr) {
        this.type = str;
        this.headers = Collections.unmodifiableList(list);
        this.content = bArr;
    }

    public PemObject(String str, byte[] bArr) {
        this(str, EMPTY_LIST, bArr);
    }

    public PemObject generate() throws PemGenerationException {
        return this;
    }

    public byte[] getContent() {
        return this.content;
    }

    public List getHeaders() {
        return this.headers;
    }

    public String getType() {
        return this.type;
    }
}
