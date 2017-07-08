package com.android.internal.http.multipart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.http.util.EncodingUtils;

public class FilePart extends PartBase {
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";
    protected static final String FILE_NAME = "; filename=";
    private static final byte[] FILE_NAME_BYTES = null;
    private static final Log LOG = null;
    private PartSource source;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.http.multipart.FilePart.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.http.multipart.FilePart.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.http.multipart.FilePart.<clinit>():void");
    }

    public FilePart(String name, PartSource partSource, String contentType, String charset) {
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        if (charset == null) {
            charset = DEFAULT_CHARSET;
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
