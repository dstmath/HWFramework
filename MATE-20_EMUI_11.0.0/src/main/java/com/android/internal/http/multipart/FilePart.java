package com.android.internal.http.multipart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.util.EncodingUtils;

public class FilePart extends PartBase {
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";
    protected static final String FILE_NAME = "; filename=";
    private static final byte[] FILE_NAME_BYTES = EncodingUtils.getAsciiBytes(FILE_NAME);
    private static final Log LOG = LogFactory.getLog(FilePart.class);
    private PartSource source;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public FilePart(String name, PartSource partSource, String contentType, String charset) {
        super(name, contentType == null ? "application/octet-stream" : contentType, charset == null ? "ISO-8859-1" : charset, DEFAULT_TRANSFER_ENCODING);
        if (partSource != null) {
            this.source = partSource;
            return;
        }
        throw new IllegalArgumentException("Source may not be null");
    }

    public FilePart(String name, PartSource partSource) {
        this(name, partSource, (String) null, (String) null);
    }

    public FilePart(String name, File file) throws FileNotFoundException {
        this(name, new FilePartSource(file), (String) null, (String) null);
    }

    public FilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
        this(name, new FilePartSource(file), contentType, charset);
    }

    public FilePart(String name, String fileName, File file) throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), (String) null, (String) null);
    }

    public FilePart(String name, String fileName, File file, String contentType, String charset) throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), contentType, charset);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.http.multipart.Part
    public void sendDispositionHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendDispositionHeader(OutputStream out)");
        super.sendDispositionHeader(out);
        String filename = this.source.getFileName();
        if (filename != null) {
            out.write(FILE_NAME_BYTES);
            out.write(QUOTE_BYTES);
            out.write(EncodingUtils.getAsciiBytes(filename));
            out.write(QUOTE_BYTES);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.http.multipart.Part
    public void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream out)");
        if (lengthOfData() == 0) {
            LOG.debug("No data to send.");
            return;
        }
        byte[] tmp = new byte[4096];
        InputStream instream = this.source.createInputStream();
        while (true) {
            try {
                int len = instream.read(tmp);
                if (len >= 0) {
                    out.write(tmp, 0, len);
                } else {
                    return;
                }
            } finally {
                instream.close();
            }
        }
    }

    /* access modifiers changed from: protected */
    public PartSource getSource() {
        LOG.trace("enter getSource()");
        return this.source;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.http.multipart.Part
    public long lengthOfData() {
        LOG.trace("enter lengthOfData()");
        return this.source.getLength();
    }
}
