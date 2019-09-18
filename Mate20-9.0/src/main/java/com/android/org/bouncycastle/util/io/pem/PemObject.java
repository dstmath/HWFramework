package com.android.org.bouncycastle.util.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PemObject implements PemObjectGenerator {
    private static final List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());
    private byte[] content;
    private List headers;
    private String type;

    public PemObject(String type2, byte[] content2) {
        this(type2, EMPTY_LIST, content2);
    }

    public PemObject(String type2, List headers2, byte[] content2) {
        this.type = type2;
        this.headers = Collections.unmodifiableList(headers2);
        this.content = content2;
    }

    public String getType() {
        return this.type;
    }

    public List getHeaders() {
        return this.headers;
    }

    public byte[] getContent() {
        return this.content;
    }

    public PemObject generate() throws PemGenerationException {
        return this;
    }
}
