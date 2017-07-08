package com.android.okhttp.internal;

import com.android.okhttp.HttpUrl;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.Source;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class Util {
    public static final byte[] EMPTY_BYTE_ARRAY = null;
    public static final String[] EMPTY_STRING_ARRAY = null;
    public static final Charset UTF_8 = null;

    /* renamed from: com.android.okhttp.internal.Util.1 */
    static class AnonymousClass1 implements ThreadFactory {
        final /* synthetic */ boolean val$daemon;
        final /* synthetic */ String val$name;

        AnonymousClass1(String val$name, boolean val$daemon) {
            this.val$name = val$name;
            this.val$daemon = val$daemon;
        }

        public Thread newThread(Runnable runnable) {
            Thread result = new Thread(runnable, this.val$name);
            result.setDaemon(this.val$daemon);
            return result;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.Util.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.Util.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.Util.<clinit>():void");
    }

    public static boolean skipAll(com.android.okhttp.okio.Source r12, int r13, java.util.concurrent.TimeUnit r14) throws java.io.IOException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x006e in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = 9223372036854775807; // 0x7fffffffffffffff float:NaN double:NaN;
        r2 = java.lang.System.nanoTime();
        r6 = r12.timeout();
        r6 = r6.hasDeadline();
        if (r6 == 0) goto L_0x0051;
    L_0x0013:
        r6 = r12.timeout();
        r6 = r6.deadlineNanoTime();
        r4 = r6 - r2;
    L_0x001d:
        r6 = r12.timeout();
        r8 = (long) r13;
        r8 = r14.toNanos(r8);
        r8 = java.lang.Math.min(r4, r8);
        r8 = r8 + r2;
        r6.deadlineNanoTime(r8);
        r1 = new com.android.okhttp.okio.Buffer;	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        r1.<init>();	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
    L_0x0033:
        r6 = 2048; // 0x800 float:2.87E-42 double:1.0118E-320;	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        r6 = r12.read(r1, r6);	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        r8 = -1;	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        if (r6 == 0) goto L_0x0057;	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
    L_0x003f:
        r1.clear();	 Catch:{ InterruptedIOException -> 0x0043, all -> 0x0078 }
        goto L_0x0033;
    L_0x0043:
        r0 = move-exception;
        r6 = 0;
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x006e;
    L_0x0049:
        r7 = r12.timeout();
        r7.clearDeadline();
    L_0x0050:
        return r6;
    L_0x0051:
        r4 = 9223372036854775807; // 0x7fffffffffffffff float:NaN double:NaN;
        goto L_0x001d;
    L_0x0057:
        r6 = 1;
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x0064;
    L_0x005c:
        r7 = r12.timeout();
        r7.clearDeadline();
    L_0x0063:
        return r6;
    L_0x0064:
        r7 = r12.timeout();
        r8 = r2 + r4;
        r7.deadlineNanoTime(r8);
        goto L_0x0063;
    L_0x006e:
        r7 = r12.timeout();
        r8 = r2 + r4;
        r7.deadlineNanoTime(r8);
        goto L_0x0050;
    L_0x0078:
        r6 = move-exception;
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x0085;
    L_0x007d:
        r7 = r12.timeout();
        r7.clearDeadline();
    L_0x0084:
        throw r6;
    L_0x0085:
        r7 = r12.timeout();
        r8 = r2 + r4;
        r7.deadlineNanoTime(r8);
        goto L_0x0084;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.Util.skipAll(com.android.okhttp.okio.Source, int, java.util.concurrent.TimeUnit):boolean");
    }

    private Util() {
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static boolean equal(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e)) {
                    throw e;
                }
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e2) {
            }
        }
    }

    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    public static void closeAll(Closeable a, Closeable b) throws IOException {
        Object thrown = null;
        try {
            a.close();
        } catch (Throwable e) {
            Throwable thrown2 = e;
        }
        try {
            b.close();
        } catch (Throwable e2) {
            if (thrown == null) {
                thrown2 = e2;
            }
        }
        if (thrown != null) {
            if (thrown instanceof IOException) {
                throw ((IOException) thrown);
            } else if (thrown instanceof RuntimeException) {
                throw ((RuntimeException) thrown);
            } else if (thrown instanceof Error) {
                throw ((Error) thrown);
            } else {
                throw new AssertionError(thrown);
            }
        }
    }

    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }

    public static String md5Hex(String s) {
        try {
            return ByteString.of(MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"))).hex();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static String shaBase64(String s) {
        try {
            return ByteString.of(MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"))).base64();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static ByteString sha1(ByteString s) {
        try {
            return ByteString.of(MessageDigest.getInstance("SHA-1").digest(s.toByteArray()));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList(list));
    }

    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(Arrays.asList((Object[]) elements.clone()));
    }

    public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
        return Collections.unmodifiableMap(new LinkedHashMap(map));
    }

    public static ThreadFactory threadFactory(String name, boolean daemon) {
        return new AnonymousClass1(name, daemon);
    }

    public static <T> T[] intersect(Class<T> arrayType, T[] first, T[] second) {
        List<T> result = intersect(first, second);
        return result.toArray((Object[]) Array.newInstance(arrayType, result.size()));
    }

    private static <T> List<T> intersect(T[] first, T[] second) {
        List<T> result = new ArrayList();
        for (T a : first) {
            for (T b : second) {
                if (a.equals(b)) {
                    result.add(b);
                    break;
                }
            }
        }
        return result;
    }

    public static String hostHeader(HttpUrl url) {
        if (url.port() != HttpUrl.defaultPort(url.scheme())) {
            return url.rfc2732host() + ":" + url.port();
        }
        return url.rfc2732host();
    }

    public static String toHumanReadableAscii(String s) {
        int i = 0;
        int length = s.length();
        while (i < length) {
            int c = s.codePointAt(i);
            if (c <= 31 || c >= 127) {
                Buffer buffer = new Buffer();
                buffer.writeUtf8(s, 0, i);
                int j = i;
                while (j < length) {
                    c = s.codePointAt(j);
                    int i2 = (c <= 31 || c >= 127) ? 63 : c;
                    buffer.writeUtf8CodePoint(i2);
                    j += Character.charCount(c);
                }
                return buffer.readUtf8();
            }
            i += Character.charCount(c);
        }
        return s;
    }

    public static boolean isAndroidGetsocknameError(AssertionError e) {
        if (e.getCause() == null || e.getMessage() == null) {
            return false;
        }
        return e.getMessage().contains("getsockname failed");
    }
}
