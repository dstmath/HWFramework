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

    public FilePart(String name, PartSource partSource, String contentType, String charset) {
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        super(name, contentType, charset, DEFAULT_TRANSFER_ENCODING);
        if (partSource == null) {
            throw new IllegalArgumentException("Source may not be null");
        }
        this.source = partSource;
    }

    public FilePart(String name, PartSource partSource) {
        this(name, partSource, null, null);
    }

    public FilePart(String name, File file) throws FileNotFoundException {
        this(name, new FilePartSource(file), null, null);
    }

    public FilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
        this(name, new FilePartSource(file), contentType, charset);
    }

    public FilePart(String name, String fileName, File file) throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), null, null);
    }

    public FilePart(String name, String fileName, File file, String contentType, String charset) throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), contentType, charset);
    }

    protected void sendDispositionHeader(OutputStream out) throws IOException {
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

    protected void sendData(OutputStream out) throws IOException {
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
                if (len < 0) {
                    break;
                }
                out.write(tmp, 0, len);
            } finally {
                instream.close();
            }
        }
    }

    protected PartSource getSource() {
        LOG.trace("enter getSource()");
        return this.source;
    }

    protected long lengthOfData() {
        LOG.trace("enter lengthOfData()");
        return this.source.getLength();
    }
}
