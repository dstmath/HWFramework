package com.android.okhttp;

import com.android.okhttp.Headers;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.internal.DiskLruCache;
import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.CacheRequest;
import com.android.okhttp.internal.http.CacheStrategy;
import com.android.okhttp.internal.http.HttpMethod;
import com.android.okhttp.internal.http.OkHeaders;
import com.android.okhttp.internal.http.StatusLine;
import com.android.okhttp.internal.io.FileSystem;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.ForwardingSink;
import com.android.okhttp.okio.ForwardingSource;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class Cache {
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;
    private static final int ENTRY_METADATA = 0;
    private static final int VERSION = 201105;
    /* access modifiers changed from: private */
    public final DiskLruCache cache;
    private int hitCount;
    final InternalCache internalCache;
    private int networkCount;
    private int requestCount;
    /* access modifiers changed from: private */
    public int writeAbortCount;
    /* access modifiers changed from: private */
    public int writeSuccessCount;

    private final class CacheRequestImpl implements CacheRequest {
        private Sink body;
        private Sink cacheOut;
        /* access modifiers changed from: private */
        public boolean done;
        private final DiskLruCache.Editor editor;

        public CacheRequestImpl(final DiskLruCache.Editor editor2) throws IOException {
            this.editor = editor2;
            this.cacheOut = editor2.newSink(Cache.ENTRY_BODY);
            this.body = new ForwardingSink(this.cacheOut, Cache.this) {
                public void close() throws IOException {
                    synchronized (Cache.this) {
                        if (!CacheRequestImpl.this.done) {
                            boolean unused = CacheRequestImpl.this.done = true;
                            int unused2 = Cache.this.writeSuccessCount = Cache.this.writeSuccessCount + Cache.ENTRY_BODY;
                            super.close();
                            editor2.commit();
                        }
                    }
                }
            };
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
            r2.editor.abort();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            com.android.okhttp.internal.Util.closeQuietly((java.io.Closeable) r2.cacheOut);
         */
        public void abort() {
            synchronized (Cache.this) {
                if (!this.done) {
                    this.done = true;
                    int unused = Cache.this.writeAbortCount = Cache.this.writeAbortCount + Cache.ENTRY_BODY;
                }
            }
        }

        public Sink body() {
            return this.body;
        }
    }

    private static class CacheResponseBody extends ResponseBody {
        private final BufferedSource bodySource;
        private final String contentLength;
        private final String contentType;
        /* access modifiers changed from: private */
        public final DiskLruCache.Snapshot snapshot;

        public CacheResponseBody(final DiskLruCache.Snapshot snapshot2, String contentType2, String contentLength2) {
            this.snapshot = snapshot2;
            this.contentType = contentType2;
            this.contentLength = contentLength2;
            this.bodySource = Okio.buffer((Source) new ForwardingSource(snapshot2.getSource(Cache.ENTRY_BODY)) {
                public void close() throws IOException {
                    snapshot2.close();
                    super.close();
                }
            });
        }

        public MediaType contentType() {
            if (this.contentType != null) {
                return MediaType.parse(this.contentType);
            }
            return null;
        }

        public long contentLength() {
            long j = -1;
            try {
                if (this.contentLength != null) {
                    j = Long.parseLong(this.contentLength);
                }
                return j;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        public BufferedSource source() {
            return this.bodySource;
        }
    }

    private static final class Entry {
        private final int code;
        private final Handshake handshake;
        private final String message;
        private final Protocol protocol;
        private final String requestMethod;
        private final Headers responseHeaders;
        private final String url;
        private final Headers varyHeaders;

        public Entry(Source in) throws IOException {
            try {
                BufferedSource source = Okio.buffer(in);
                this.url = source.readUtf8LineStrict();
                this.requestMethod = source.readUtf8LineStrict();
                Headers.Builder varyHeadersBuilder = new Headers.Builder();
                int varyRequestHeaderLineCount = Cache.readInt(source);
                for (int i = Cache.ENTRY_METADATA; i < varyRequestHeaderLineCount; i += Cache.ENTRY_BODY) {
                    varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                this.varyHeaders = varyHeadersBuilder.build();
                StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
                this.protocol = statusLine.protocol;
                this.code = statusLine.code;
                this.message = statusLine.message;
                Headers.Builder responseHeadersBuilder = new Headers.Builder();
                int responseHeaderLineCount = Cache.readInt(source);
                for (int i2 = Cache.ENTRY_METADATA; i2 < responseHeaderLineCount; i2 += Cache.ENTRY_BODY) {
                    responseHeadersBuilder.addLenient(source.readUtf8LineStrict());
                }
                this.responseHeaders = responseHeadersBuilder.build();
                if (isHttps()) {
                    String blank = source.readUtf8LineStrict();
                    if (blank.length() <= 0) {
                        this.handshake = Handshake.get(source.readUtf8LineStrict(), readCertificateList(source), readCertificateList(source));
                    } else {
                        throw new IOException("expected \"\" but was \"" + blank + "\"");
                    }
                } else {
                    this.handshake = null;
                }
            } finally {
                in.close();
            }
        }

        public Entry(Response response) {
            this.url = response.request().urlString();
            this.varyHeaders = OkHeaders.varyHeaders(response);
            this.requestMethod = response.request().method();
            this.protocol = response.protocol();
            this.code = response.code();
            this.message = response.message();
            this.responseHeaders = response.headers();
            this.handshake = response.handshake();
        }

        public void writeTo(DiskLruCache.Editor editor) throws IOException {
            BufferedSink sink = Okio.buffer(editor.newSink(Cache.ENTRY_METADATA));
            sink.writeUtf8(this.url);
            sink.writeByte(10);
            sink.writeUtf8(this.requestMethod);
            sink.writeByte(10);
            sink.writeDecimalLong((long) this.varyHeaders.size());
            sink.writeByte(10);
            int size = this.varyHeaders.size();
            for (int i = Cache.ENTRY_METADATA; i < size; i += Cache.ENTRY_BODY) {
                sink.writeUtf8(this.varyHeaders.name(i));
                sink.writeUtf8(": ");
                sink.writeUtf8(this.varyHeaders.value(i));
                sink.writeByte(10);
            }
            sink.writeUtf8(new StatusLine(this.protocol, this.code, this.message).toString());
            sink.writeByte(10);
            sink.writeDecimalLong((long) this.responseHeaders.size());
            sink.writeByte(10);
            int size2 = this.responseHeaders.size();
            for (int i2 = Cache.ENTRY_METADATA; i2 < size2; i2 += Cache.ENTRY_BODY) {
                sink.writeUtf8(this.responseHeaders.name(i2));
                sink.writeUtf8(": ");
                sink.writeUtf8(this.responseHeaders.value(i2));
                sink.writeByte(10);
            }
            if (isHttps() != 0) {
                sink.writeByte(10);
                sink.writeUtf8(this.handshake.cipherSuite());
                sink.writeByte(10);
                writeCertList(sink, this.handshake.peerCertificates());
                writeCertList(sink, this.handshake.localCertificates());
            }
            sink.close();
        }

        private boolean isHttps() {
            return this.url.startsWith("https://");
        }

        private List<Certificate> readCertificateList(BufferedSource source) throws IOException {
            int length = Cache.readInt(source);
            if (length == -1) {
                return Collections.emptyList();
            }
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                List<Certificate> result = new ArrayList<>(length);
                for (int i = Cache.ENTRY_METADATA; i < length; i += Cache.ENTRY_BODY) {
                    String line = source.readUtf8LineStrict();
                    Buffer bytes = new Buffer();
                    bytes.write(ByteString.decodeBase64(line));
                    result.add(certificateFactory.generateCertificate(bytes.inputStream()));
                }
                return result;
            } catch (CertificateException e) {
                throw new IOException(e.getMessage());
            }
        }

        private void writeCertList(BufferedSink sink, List<Certificate> certificates) throws IOException {
            try {
                sink.writeDecimalLong((long) certificates.size());
                sink.writeByte(10);
                int size = certificates.size();
                for (int i = Cache.ENTRY_METADATA; i < size; i += Cache.ENTRY_BODY) {
                    sink.writeUtf8(ByteString.of(certificates.get(i).getEncoded()).base64());
                    sink.writeByte(10);
                }
            } catch (CertificateEncodingException e) {
                throw new IOException(e.getMessage());
            }
        }

        public boolean matches(Request request, Response response) {
            return this.url.equals(request.urlString()) && this.requestMethod.equals(request.method()) && OkHeaders.varyMatches(response, this.varyHeaders, request);
        }

        public Response response(Request request, DiskLruCache.Snapshot snapshot) {
            String contentType = this.responseHeaders.get("Content-Type");
            String contentLength = this.responseHeaders.get("Content-Length");
            return new Response.Builder().request(new Request.Builder().url(this.url).method(this.requestMethod, null).headers(this.varyHeaders).build()).protocol(this.protocol).code(this.code).message(this.message).headers(this.responseHeaders).body(new CacheResponseBody(snapshot, contentType, contentLength)).handshake(this.handshake).build();
        }
    }

    public Cache(File directory, long maxSize) {
        this(directory, maxSize, FileSystem.SYSTEM);
    }

    Cache(File directory, long maxSize, FileSystem fileSystem) {
        this.internalCache = new InternalCache() {
            public Response get(Request request) throws IOException {
                return Cache.this.get(request);
            }

            public CacheRequest put(Response response) throws IOException {
                return Cache.this.put(response);
            }

            public void remove(Request request) throws IOException {
                Cache.this.remove(request);
            }

            public void update(Response cached, Response network) throws IOException {
                Cache.this.update(cached, network);
            }

            public void trackConditionalCacheHit() {
                Cache.this.trackConditionalCacheHit();
            }

            public void trackResponse(CacheStrategy cacheStrategy) {
                Cache.this.trackResponse(cacheStrategy);
            }
        };
        this.cache = DiskLruCache.create(fileSystem, directory, VERSION, ENTRY_COUNT, maxSize);
    }

    private static String urlToKey(Request request) {
        return Util.md5Hex(request.urlString());
    }

    /* access modifiers changed from: package-private */
    public Response get(Request request) {
        try {
            DiskLruCache.Snapshot snapshot = this.cache.get(urlToKey(request));
            if (snapshot == null) {
                return null;
            }
            try {
                Entry entry = new Entry(snapshot.getSource(ENTRY_METADATA));
                Response response = entry.response(request, snapshot);
                if (entry.matches(request, response)) {
                    return response;
                }
                Util.closeQuietly((Closeable) response.body());
                return null;
            } catch (IOException e) {
                Util.closeQuietly((Closeable) snapshot);
                return null;
            }
        } catch (IOException e2) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public CacheRequest put(Response response) throws IOException {
        String requestMethod = response.request().method();
        if (HttpMethod.invalidatesCache(response.request().method())) {
            try {
                remove(response.request());
            } catch (IOException e) {
            }
            return null;
        } else if (!requestMethod.equals("GET") || OkHeaders.hasVaryAll(response)) {
            return null;
        } else {
            Entry entry = new Entry(response);
            try {
                DiskLruCache.Editor editor = this.cache.edit(urlToKey(response.request()));
                if (editor == null) {
                    return null;
                }
                entry.writeTo(editor);
                return new CacheRequestImpl(editor);
            } catch (IOException e2) {
                abortQuietly(null);
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void remove(Request request) throws IOException {
        this.cache.remove(urlToKey(request));
    }

    /* access modifiers changed from: private */
    public void update(Response cached, Response network) {
        Entry entry = new Entry(network);
        try {
            DiskLruCache.Editor editor = ((CacheResponseBody) cached.body()).snapshot.edit();
            if (editor != null) {
                entry.writeTo(editor);
                editor.commit();
            }
        } catch (IOException e) {
            abortQuietly(null);
        }
    }

    private void abortQuietly(DiskLruCache.Editor editor) {
        if (editor != null) {
            try {
                editor.abort();
            } catch (IOException e) {
            }
        }
    }

    public void initialize() throws IOException {
        this.cache.initialize();
    }

    public void delete() throws IOException {
        this.cache.delete();
    }

    public void evictAll() throws IOException {
        this.cache.evictAll();
    }

    public Iterator<String> urls() throws IOException {
        return new Iterator<String>() {
            boolean canRemove;
            final Iterator<DiskLruCache.Snapshot> delegate = Cache.this.cache.snapshots();
            String nextUrl;

            public boolean hasNext() {
                if (this.nextUrl != null) {
                    return true;
                }
                this.canRemove = false;
                while (this.delegate.hasNext()) {
                    DiskLruCache.Snapshot snapshot = this.delegate.next();
                    try {
                        this.nextUrl = Okio.buffer(snapshot.getSource(Cache.ENTRY_METADATA)).readUtf8LineStrict();
                        return true;
                    } catch (IOException e) {
                    } finally {
                        snapshot.close();
                    }
                }
                return false;
            }

            public String next() {
                if (hasNext()) {
                    String result = this.nextUrl;
                    this.nextUrl = null;
                    this.canRemove = true;
                    return result;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                if (this.canRemove) {
                    this.delegate.remove();
                    return;
                }
                throw new IllegalStateException("remove() before next()");
            }
        };
    }

    public synchronized int getWriteAbortCount() {
        return this.writeAbortCount;
    }

    public synchronized int getWriteSuccessCount() {
        return this.writeSuccessCount;
    }

    public long getSize() throws IOException {
        return this.cache.size();
    }

    public long getMaxSize() {
        return this.cache.getMaxSize();
    }

    public void flush() throws IOException {
        this.cache.flush();
    }

    public void close() throws IOException {
        this.cache.close();
    }

    public File getDirectory() {
        return this.cache.getDirectory();
    }

    public boolean isClosed() {
        return this.cache.isClosed();
    }

    /* access modifiers changed from: private */
    public synchronized void trackResponse(CacheStrategy cacheStrategy) {
        this.requestCount += ENTRY_BODY;
        if (cacheStrategy.networkRequest != null) {
            this.networkCount += ENTRY_BODY;
        } else if (cacheStrategy.cacheResponse != null) {
            this.hitCount += ENTRY_BODY;
        }
    }

    /* access modifiers changed from: private */
    public synchronized void trackConditionalCacheHit() {
        this.hitCount += ENTRY_BODY;
    }

    public synchronized int getNetworkCount() {
        return this.networkCount;
    }

    public synchronized int getHitCount() {
        return this.hitCount;
    }

    public synchronized int getRequestCount() {
        return this.requestCount;
    }

    /* access modifiers changed from: private */
    public static int readInt(BufferedSource source) throws IOException {
        try {
            long result = source.readDecimalLong();
            String line = source.readUtf8LineStrict();
            if (result >= 0 && result <= 2147483647L && line.isEmpty()) {
                return (int) result;
            }
            throw new IOException("expected an int but was \"" + result + line + "\"");
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }
}
