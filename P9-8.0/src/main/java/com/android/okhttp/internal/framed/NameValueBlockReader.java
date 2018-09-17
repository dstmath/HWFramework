package com.android.okhttp.internal.framed;

import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.ForwardingSource;
import com.android.okhttp.okio.InflaterSource;
import com.android.okhttp.okio.Okio;
import com.squareup.okhttp.internal.framed.Header;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class NameValueBlockReader {
    private int compressedLimit;
    private final InflaterSource inflaterSource;
    private final BufferedSource source = Okio.buffer(this.inflaterSource);

    public NameValueBlockReader(BufferedSource source) {
        this.inflaterSource = new InflaterSource(new ForwardingSource(source) {
            public long read(Buffer sink, long byteCount) throws IOException {
                if (NameValueBlockReader.this.compressedLimit == 0) {
                    return -1;
                }
                long read = super.read(sink, Math.min(byteCount, (long) NameValueBlockReader.this.compressedLimit));
                if (read == -1) {
                    return -1;
                }
                NameValueBlockReader nameValueBlockReader = NameValueBlockReader.this;
                nameValueBlockReader.compressedLimit = (int) (((long) nameValueBlockReader.compressedLimit) - read);
                return read;
            }
        }, new Inflater() {
            public int inflate(byte[] buffer, int offset, int count) throws DataFormatException {
                int result = super.inflate(buffer, offset, count);
                if (result != 0 || !needsDictionary()) {
                    return result;
                }
                setDictionary(Spdy3.DICTIONARY);
                return super.inflate(buffer, offset, count);
            }
        });
    }

    public List<Header> readNameValueBlock(int length) throws IOException {
        this.compressedLimit += length;
        int numberOfPairs = this.source.readInt();
        if (numberOfPairs < 0) {
            throw new IOException("numberOfPairs < 0: " + numberOfPairs);
        } else if (numberOfPairs > 1024) {
            throw new IOException("numberOfPairs > 1024: " + numberOfPairs);
        } else {
            List<Header> entries = new ArrayList(numberOfPairs);
            for (int i = 0; i < numberOfPairs; i++) {
                ByteString name = readByteString().toAsciiLowercase();
                ByteString values = readByteString();
                if (name.size() == 0) {
                    throw new IOException("name.size == 0");
                }
                entries.add(new Header(name, values));
            }
            doneReading();
            return entries;
        }
    }

    private ByteString readByteString() throws IOException {
        return this.source.readByteString((long) this.source.readInt());
    }

    private void doneReading() throws IOException {
        if (this.compressedLimit > 0) {
            this.inflaterSource.refill();
            if (this.compressedLimit != 0) {
                throw new IOException("compressedLimit > 0: " + this.compressedLimit);
            }
        }
    }

    public void close() throws IOException {
        this.source.close();
    }
}
