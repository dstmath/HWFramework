package android.media.scan;

import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.LongArray;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmpInterface {
    private static final String NAME_DESCRIPTION = "Description";
    private static final String NAME_DOCUMENT_ID = "DocumentID";
    private static final String NAME_FORMAT = "format";
    private static final String NAME_INSTANCE_ID = "InstanceID";
    private static final String NAME_ORIGINAL_DOCUMENT_ID = "OriginalDocumentID";
    private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
    private static final String NS_EXIF = "http://ns.adobe.com/exif/1.0/";
    private static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String NS_XMP = "http://ns.adobe.com/xap/1.0/";
    private static final String NS_XMPMM = "http://ns.adobe.com/xap/1.0/mm/";
    private String mDocumentId;
    private String mFormat;
    private final ByteCountingInputStream mIn;
    private String mInstanceId;
    private String mOriginalDocumentId;
    private final Set<String> mRedactedExifTags;
    private final LongArray mRedactedRanges;
    private final long mXmpOffset;

    private XmpInterface(InputStream in) throws IOException {
        this(in, Collections.emptySet(), EmptyArray.LONG);
    }

    private XmpInterface(InputStream in, Set<String> redactedExifTags, long[] xmpOffsets) throws IOException {
        this.mIn = new ByteCountingInputStream(in);
        this.mRedactedExifTags = redactedExifTags;
        long offset = 0;
        this.mXmpOffset = xmpOffsets.length == 0 ? 0 : xmpOffsets[0];
        this.mRedactedRanges = new LongArray();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(this.mIn, StandardCharsets.UTF_8.name());
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type != 2) {
                    offset = this.mIn.getOffset(parser);
                } else {
                    String ns = parser.getNamespace();
                    String name = parser.getName();
                    if (NS_RDF.equals(ns) && NAME_DESCRIPTION.equals(name)) {
                        this.mFormat = maybeOverride(this.mFormat, parser.getAttributeValue(NS_DC, "format"));
                        this.mDocumentId = maybeOverride(this.mDocumentId, parser.getAttributeValue(NS_XMPMM, NAME_DOCUMENT_ID));
                        this.mInstanceId = maybeOverride(this.mInstanceId, parser.getAttributeValue(NS_XMPMM, NAME_INSTANCE_ID));
                        this.mOriginalDocumentId = maybeOverride(this.mOriginalDocumentId, parser.getAttributeValue(NS_XMPMM, NAME_ORIGINAL_DOCUMENT_ID));
                    } else if (NS_DC.equals(ns) && "format".equals(name)) {
                        this.mFormat = maybeOverride(this.mFormat, parser.nextText());
                    } else if (NS_XMPMM.equals(ns) && NAME_DOCUMENT_ID.equals(name)) {
                        this.mDocumentId = maybeOverride(this.mDocumentId, parser.nextText());
                    } else if (NS_XMPMM.equals(ns) && NAME_INSTANCE_ID.equals(name)) {
                        this.mInstanceId = maybeOverride(this.mInstanceId, parser.nextText());
                    } else if (NS_XMPMM.equals(ns) && NAME_ORIGINAL_DOCUMENT_ID.equals(name)) {
                        this.mOriginalDocumentId = maybeOverride(this.mOriginalDocumentId, parser.nextText());
                    } else if (NS_EXIF.equals(ns) && this.mRedactedExifTags.contains(name)) {
                        while (true) {
                            if (parser.next() == 3 && parser.getName().equals(name)) {
                                break;
                            }
                        }
                        offset = this.mIn.getOffset(parser);
                        this.mRedactedRanges.add(this.mXmpOffset + offset);
                        this.mRedactedRanges.add(this.mXmpOffset + offset);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    public static XmpInterface fromContainer(InputStream is) throws IOException {
        return fromContainer(new ExifInterface(is));
    }

    public static XmpInterface fromContainer(InputStream is, Set<String> redactedExifTags) throws IOException {
        return fromContainer(new ExifInterface(is), redactedExifTags);
    }

    public static XmpInterface fromContainer(ExifInterface exif) throws IOException {
        return fromContainer(exif, Collections.emptySet());
    }

    public static XmpInterface fromContainer(ExifInterface exif, Set<String> redactedExifTags) throws IOException {
        byte[] buf;
        long[] xmpOffsets;
        if (exif.hasAttribute(ExifInterface.TAG_XMP)) {
            buf = exif.getAttributeBytes(ExifInterface.TAG_XMP);
            xmpOffsets = exif.getAttributeRange(ExifInterface.TAG_XMP);
        } else {
            buf = EmptyArray.BYTE;
            xmpOffsets = EmptyArray.LONG;
        }
        return new XmpInterface(new ByteArrayInputStream(buf), redactedExifTags, xmpOffsets);
    }

    public static XmpInterface fromContainer(IsoInterface iso) throws IOException {
        return fromContainer(iso, Collections.emptySet());
    }

    public static XmpInterface fromContainer(IsoInterface iso, Set<String> redactedExifTags) throws IOException {
        byte[] buf = null;
        long[] xmpOffsets = EmptyArray.LONG;
        if (0 == 0) {
            UUID uuid = UUID.fromString("be7acfcb-97a9-42e8-9c71-999491e3afac");
            buf = iso.getBoxBytes(uuid);
            xmpOffsets = iso.getBoxRanges(uuid);
        }
        if (buf == null) {
            buf = iso.getBoxBytes(IsoInterface.BOX_XMP);
            xmpOffsets = iso.getBoxRanges(IsoInterface.BOX_XMP);
        }
        if (buf == null) {
            buf = EmptyArray.BYTE;
            xmpOffsets = EmptyArray.LONG;
        }
        return new XmpInterface(new ByteArrayInputStream(buf), redactedExifTags, xmpOffsets);
    }

    public static XmpInterface fromSidecar(File file) throws IOException {
        return new XmpInterface(new FileInputStream(file));
    }

    private static String maybeOverride(String existing, String current) {
        if (!TextUtils.isEmpty(existing)) {
            return existing;
        }
        if (!TextUtils.isEmpty(current)) {
            return current;
        }
        return null;
    }

    public String getFormat() {
        return this.mFormat;
    }

    public String getDocumentId() {
        return this.mDocumentId;
    }

    public String getInstanceId() {
        return this.mInstanceId;
    }

    public String getOriginalDocumentId() {
        return this.mOriginalDocumentId;
    }

    public LongArray getRedactionRanges() {
        return this.mRedactedRanges;
    }

    @VisibleForTesting
    public static class ByteCountingInputStream extends InputStream {
        private int mLine = 1;
        private int mOffset = 0;
        private final LongArray mOffsets = new LongArray();
        private final InputStream mWrapped;

        public ByteCountingInputStream(InputStream wrapped) {
            this.mWrapped = wrapped;
        }

        public long getOffset(XmlPullParser parser) {
            int line = parser.getLineNumber() - 1;
            return ((long) (parser.getColumnNumber() - 1)) + (line == 0 ? 0 : this.mOffsets.get(line - 1));
        }

        @Override // java.io.InputStream
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override // java.io.InputStream
        public int read(byte[] b, int off, int len) throws IOException {
            int read = this.mWrapped.read(b, off, len);
            if (read == -1) {
                return -1;
            }
            for (int i = 0; i < read; i++) {
                if (b[off + i] == 10) {
                    this.mOffsets.add(this.mLine - 1, (long) (this.mOffset + i + 1));
                    this.mLine++;
                }
            }
            this.mOffset += read;
            return read;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            int r = this.mWrapped.read();
            if (r == -1) {
                return -1;
            }
            this.mOffset++;
            if (r == 10) {
                this.mOffsets.add(this.mLine - 1, (long) this.mOffset);
                this.mLine++;
            }
            return r;
        }

        @Override // java.io.InputStream
        public long skip(long n) throws IOException {
            return super.skip(n);
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return this.mWrapped.available();
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public void close() throws IOException {
            this.mWrapped.close();
        }

        public String toString() {
            return Arrays.toString(this.mOffsets.toArray());
        }
    }
}
