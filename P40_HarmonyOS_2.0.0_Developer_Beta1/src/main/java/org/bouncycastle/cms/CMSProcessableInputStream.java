package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.util.io.Streams;

class CMSProcessableInputStream implements CMSProcessable, CMSReadable {
    private InputStream input;
    private boolean used = false;

    public CMSProcessableInputStream(InputStream inputStream) {
        this.input = inputStream;
    }

    private synchronized void checkSingleUsage() {
        if (!this.used) {
            this.used = true;
        } else {
            throw new IllegalStateException("CMSProcessableInputStream can only be used once");
        }
    }

    @Override // org.bouncycastle.cms.CMSProcessable
    public Object getContent() {
        return getInputStream();
    }

    @Override // org.bouncycastle.cms.CMSReadable
    public InputStream getInputStream() {
        checkSingleUsage();
        return this.input;
    }

    @Override // org.bouncycastle.cms.CMSProcessable
    public void write(OutputStream outputStream) throws IOException, CMSException {
        checkSingleUsage();
        Streams.pipeAll(this.input, outputStream);
        this.input.close();
    }
}
