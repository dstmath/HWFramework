package com.android.org.bouncycastle.util.io.pem;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class PemWriterEx {
    private List headers = null;
    private PemWriter mPemWriter;

    public PemWriterEx(Writer writer) {
        this.mPemWriter = new PemWriter(writer);
    }

    public void writeObjectWithList(String type, byte[] content) throws IOException {
        this.mPemWriter.writeObject(new PemObject(type, this.headers, content));
    }

    public void writeObject(String type, byte[] content) throws IOException {
        this.mPemWriter.writeObject(new PemObject(type, content));
    }

    public void addListElement(String name, String value) {
        if (this.headers == null) {
            this.headers = new ArrayList();
        }
        this.headers.add(new PemHeader(name, value));
    }

    public void close() throws IOException {
        this.mPemWriter.close();
    }
}
