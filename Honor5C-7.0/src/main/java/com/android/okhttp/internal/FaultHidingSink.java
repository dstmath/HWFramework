package com.android.okhttp.internal;

import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.ForwardingSink;
import com.android.okhttp.okio.Sink;
import java.io.IOException;

class FaultHidingSink extends ForwardingSink {
    private boolean hasErrors;

    public FaultHidingSink(Sink delegate) {
        super(delegate);
    }

    public void write(Buffer source, long byteCount) throws IOException {
        if (this.hasErrors) {
            source.skip(byteCount);
            return;
        }
        try {
            super.write(source, byteCount);
        } catch (IOException e) {
            this.hasErrors = true;
            onException(e);
        }
    }

    public void flush() throws IOException {
        if (!this.hasErrors) {
            try {
                super.flush();
            } catch (IOException e) {
                this.hasErrors = true;
                onException(e);
            }
        }
    }

    public void close() throws IOException {
        if (!this.hasErrors) {
            try {
                super.close();
            } catch (IOException e) {
                this.hasErrors = true;
                onException(e);
            }
        }
    }

    protected void onException(IOException e) {
    }
}
