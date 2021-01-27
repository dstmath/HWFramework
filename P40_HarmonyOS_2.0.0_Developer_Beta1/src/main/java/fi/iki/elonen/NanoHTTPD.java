package fi.iki.elonen;

import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import com.android.server.wifi.scanner.ChannelHelper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public abstract class NanoHTTPD {
    private static final Pattern BOUNDARY_PATTERN = Pattern.compile(BOUNDARY_REGEX, 2);
    private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;]*)['|\"]?";
    private static final Pattern CHARSET_PATTERN = Pattern.compile(CHARSET_REGEX, 2);
    private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;]*)['|\"]?";
    private static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX);
    private static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-Z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
    private static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, 2);
    private static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, 2);
    private static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
    private static final Logger LOG = Logger.getLogger(NanoHTTPD.class.getName());
    public static final String MIME_HTML = "text/html";
    public static final String MIME_PLAINTEXT = "text/plain";
    protected static Map<String, String> MIME_TYPES = null;
    private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
    public static final int SOCKET_READ_TIMEOUT = 5000;
    protected AsyncRunner asyncRunner;
    private final String hostname;
    private final int myPort;
    private volatile ServerSocket myServerSocket;
    private Thread myThread;
    private ServerSocketFactory serverSocketFactory;
    private TempFileManagerFactory tempFileManagerFactory;

    public interface AsyncRunner {
        void closeAll();

        void closed(ClientHandler clientHandler);

        void exec(ClientHandler clientHandler);
    }

    public interface IHTTPSession {
        void execute() throws IOException;

        CookieHandler getCookies();

        Map<String, String> getHeaders();

        InputStream getInputStream();

        Method getMethod();

        Map<String, String> getParms();

        String getQueryParameterString();

        String getUri();

        void parseBody(Map<String, String> map) throws IOException, ResponseException;
    }

    public interface ServerSocketFactory {
        ServerSocket create() throws IOException;
    }

    public interface TempFile {
        void delete() throws Exception;

        String getName();

        OutputStream open() throws Exception;
    }

    public interface TempFileManager {
        void clear();

        TempFile createTempFile(String str) throws Exception;
    }

    public interface TempFileManagerFactory {
        TempFileManager create();
    }

    public class ClientHandler implements Runnable {
        private final Socket acceptSocket;
        private final InputStream inputStream;

        private ClientHandler(InputStream inputStream2, Socket acceptSocket2) {
            this.inputStream = inputStream2;
            this.acceptSocket = acceptSocket2;
        }

        public void close() {
            NanoHTTPD.safeClose(this.inputStream);
            NanoHTTPD.safeClose(this.acceptSocket);
        }

        @Override // java.lang.Runnable
        public void run() {
            OutputStream outputStream;
            Throwable th;
            Exception e;
            try {
                outputStream = this.acceptSocket.getOutputStream();
                try {
                    HTTPSession session = new HTTPSession(NanoHTTPD.this.tempFileManagerFactory.create(), this.inputStream, outputStream, this.acceptSocket.getInetAddress());
                    while (!this.acceptSocket.isClosed()) {
                        session.execute();
                    }
                } catch (Exception e2) {
                    e = e2;
                    try {
                        if ((!(e instanceof SocketException) || !"NanoHttpd Shutdown".equals(e.getMessage())) && !(e instanceof SocketTimeoutException)) {
                            NanoHTTPD.LOG.log(Level.FINE, "Communication with the client broken", (Throwable) e);
                        }
                        NanoHTTPD.safeClose(outputStream);
                        NanoHTTPD.safeClose(this.inputStream);
                        NanoHTTPD.safeClose(this.acceptSocket);
                        NanoHTTPD.this.asyncRunner.closed(this);
                    } catch (Throwable th2) {
                        th = th2;
                        NanoHTTPD.safeClose(outputStream);
                        NanoHTTPD.safeClose(this.inputStream);
                        NanoHTTPD.safeClose(this.acceptSocket);
                        NanoHTTPD.this.asyncRunner.closed(this);
                        throw th;
                    }
                }
            } catch (Exception e3) {
                outputStream = null;
                e = e3;
                NanoHTTPD.LOG.log(Level.FINE, "Communication with the client broken", (Throwable) e);
                NanoHTTPD.safeClose(outputStream);
                NanoHTTPD.safeClose(this.inputStream);
                NanoHTTPD.safeClose(this.acceptSocket);
                NanoHTTPD.this.asyncRunner.closed(this);
            } catch (Throwable th3) {
                outputStream = null;
                th = th3;
                NanoHTTPD.safeClose(outputStream);
                NanoHTTPD.safeClose(this.inputStream);
                NanoHTTPD.safeClose(this.acceptSocket);
                NanoHTTPD.this.asyncRunner.closed(this);
                throw th;
            }
            NanoHTTPD.safeClose(outputStream);
            NanoHTTPD.safeClose(this.inputStream);
            NanoHTTPD.safeClose(this.acceptSocket);
            NanoHTTPD.this.asyncRunner.closed(this);
        }
    }

    public static class Cookie {
        private final String e;
        private final String n;
        private final String v;

        public static String getHTTPTime(int days) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.add(5, days);
            return dateFormat.format(calendar.getTime());
        }

        public Cookie(String name, String value) {
            this(name, value, 30);
        }

        public Cookie(String name, String value, int numDays) {
            this.n = name;
            this.v = value;
            this.e = getHTTPTime(numDays);
        }

        public Cookie(String name, String value, String expires) {
            this.n = name;
            this.v = value;
            this.e = expires;
        }

        public String getHTTPHeader() {
            return String.format("%s=%s; expires=%s", this.n, this.v, this.e);
        }
    }

    public class CookieHandler implements Iterable<String> {
        private final HashMap<String, String> cookies = new HashMap<>();
        private final ArrayList<Cookie> queue = new ArrayList<>();

        public CookieHandler(Map<String, String> httpHeaders) {
            String raw = httpHeaders.get("cookie");
            if (raw != null) {
                for (String token : raw.split(NAIRealmData.NAI_REALM_STRING_SEPARATOR)) {
                    String[] data = token.trim().split("=");
                    if (data.length == 2) {
                        this.cookies.put(data[0], data[1]);
                    }
                }
            }
        }

        public void delete(String name) {
            set(name, "-delete-", -30);
        }

        @Override // java.lang.Iterable
        public Iterator<String> iterator() {
            return this.cookies.keySet().iterator();
        }

        public String read(String name) {
            return this.cookies.get(name);
        }

        public void set(Cookie cookie) {
            this.queue.add(cookie);
        }

        public void set(String name, String value, int expires) {
            this.queue.add(new Cookie(name, value, Cookie.getHTTPTime(expires)));
        }

        public void unloadQueue(Response response) {
            Iterator<Cookie> it = this.queue.iterator();
            while (it.hasNext()) {
                response.addHeader("Set-Cookie", it.next().getHTTPHeader());
            }
        }
    }

    public static class DefaultAsyncRunner implements AsyncRunner {
        private long requestCount;
        private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList());

        public List<ClientHandler> getRunning() {
            return this.running;
        }

        @Override // fi.iki.elonen.NanoHTTPD.AsyncRunner
        public void closeAll() {
            Iterator it = new ArrayList(this.running).iterator();
            while (it.hasNext()) {
                ((ClientHandler) it.next()).close();
            }
        }

        @Override // fi.iki.elonen.NanoHTTPD.AsyncRunner
        public void closed(ClientHandler clientHandler) {
            this.running.remove(clientHandler);
        }

        @Override // fi.iki.elonen.NanoHTTPD.AsyncRunner
        public void exec(ClientHandler clientHandler) {
            this.requestCount++;
            Thread t = new Thread(clientHandler);
            t.setDaemon(true);
            t.setName("NanoHttpd Request Processor (#" + this.requestCount + ")");
            this.running.add(clientHandler);
            t.start();
        }
    }

    public static class DefaultTempFile implements TempFile {
        private final File file;
        private final OutputStream fstream = new FileOutputStream(this.file);

        public DefaultTempFile(File tempdir) throws IOException {
            this.file = File.createTempFile("NanoHTTPD-", "", tempdir);
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFile
        public void delete() throws Exception {
            NanoHTTPD.safeClose(this.fstream);
            if (!this.file.delete()) {
                throw new Exception("could not delete temporary file");
            }
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFile
        public String getName() {
            return this.file.getAbsolutePath();
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFile
        public OutputStream open() throws Exception {
            return this.fstream;
        }
    }

    public static class DefaultTempFileManager implements TempFileManager {
        private final List<TempFile> tempFiles;
        private final File tmpdir = new File(System.getProperty("java.io.tmpdir"));

        public DefaultTempFileManager() {
            if (!this.tmpdir.exists()) {
                this.tmpdir.mkdirs();
            }
            this.tempFiles = new ArrayList();
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFileManager
        public void clear() {
            for (TempFile file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                    NanoHTTPD.LOG.log(Level.WARNING, "could not delete file ", (Throwable) ignored);
                }
            }
            this.tempFiles.clear();
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFileManager
        public TempFile createTempFile(String filename_hint) throws Exception {
            DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            return tempFile;
        }
    }

    private class DefaultTempFileManagerFactory implements TempFileManagerFactory {
        private DefaultTempFileManagerFactory() {
        }

        @Override // fi.iki.elonen.NanoHTTPD.TempFileManagerFactory
        public TempFileManager create() {
            return new DefaultTempFileManager();
        }
    }

    public static class DefaultServerSocketFactory implements ServerSocketFactory {
        @Override // fi.iki.elonen.NanoHTTPD.ServerSocketFactory
        public ServerSocket create() throws IOException {
            return new ServerSocket();
        }
    }

    public static class SecureServerSocketFactory implements ServerSocketFactory {
        private String[] sslProtocols;
        private SSLServerSocketFactory sslServerSocketFactory;

        public SecureServerSocketFactory(SSLServerSocketFactory sslServerSocketFactory2, String[] sslProtocols2) {
            this.sslServerSocketFactory = sslServerSocketFactory2;
            this.sslProtocols = sslProtocols2;
        }

        @Override // fi.iki.elonen.NanoHTTPD.ServerSocketFactory
        public ServerSocket create() throws IOException {
            SSLServerSocket ss = (SSLServerSocket) this.sslServerSocketFactory.createServerSocket();
            String[] strArr = this.sslProtocols;
            if (strArr != null) {
                ss.setEnabledProtocols(strArr);
            } else {
                ss.setEnabledProtocols(ss.getSupportedProtocols());
            }
            ss.setUseClientMode(false);
            ss.setWantClientAuth(false);
            ss.setNeedClientAuth(false);
            return ss;
        }
    }

    protected class HTTPSession implements IHTTPSession {
        public static final int BUFSIZE = 8192;
        public static final int MAX_HEADER_SIZE = 1024;
        private static final int MEMORY_STORE_LIMIT = 1024;
        private static final int REQUEST_BUFFER_LEN = 512;
        private CookieHandler cookies;
        private Map<String, String> headers;
        private final BufferedInputStream inputStream;
        private Method method;
        private final OutputStream outputStream;
        private Map<String, String> parms;
        private String protocolVersion;
        private String queryParameterString;
        private String remoteIp;
        private int rlen;
        private int splitbyte;
        private final TempFileManager tempFileManager;
        private String uri;

        public HTTPSession(TempFileManager tempFileManager2, InputStream inputStream2, OutputStream outputStream2) {
            this.tempFileManager = tempFileManager2;
            this.inputStream = new BufferedInputStream(inputStream2, 8192);
            this.outputStream = outputStream2;
        }

        public HTTPSession(TempFileManager tempFileManager2, InputStream inputStream2, OutputStream outputStream2, InetAddress inetAddress) {
            this.tempFileManager = tempFileManager2;
            this.inputStream = new BufferedInputStream(inputStream2, 8192);
            this.outputStream = outputStream2;
            this.remoteIp = (inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress()) ? "127.0.0.1" : inetAddress.getHostAddress().toString();
            this.headers = new HashMap();
        }

        private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, String> parms2, Map<String, String> headers2) throws ResponseException {
            String uri2;
            try {
                String inLine = in.readLine();
                if (inLine != null) {
                    StringTokenizer st = new StringTokenizer(inLine);
                    if (st.hasMoreTokens()) {
                        pre.put("method", st.nextToken());
                        if (st.hasMoreTokens()) {
                            String uri3 = st.nextToken();
                            int qmi = uri3.indexOf(63);
                            if (qmi >= 0) {
                                decodeParms(uri3.substring(qmi + 1), parms2);
                                uri2 = NanoHTTPD.decodePercent(uri3.substring(0, qmi));
                            } else {
                                uri2 = NanoHTTPD.decodePercent(uri3);
                            }
                            if (st.hasMoreTokens()) {
                                this.protocolVersion = st.nextToken();
                            } else {
                                this.protocolVersion = "HTTP/1.1";
                                NanoHTTPD.LOG.log(Level.FINE, "no protocol version specified, strange. Assuming HTTP/1.1.");
                            }
                            String line = in.readLine();
                            while (line != null && line.trim().length() > 0) {
                                int p = line.indexOf(58);
                                if (p >= 0) {
                                    headers2.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                                }
                                line = in.readLine();
                            }
                            pre.put("uri", uri2);
                            return;
                        }
                        throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                    }
                    throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }
            } catch (IOException ioe) {
                Response.Status status = Response.Status.INTERNAL_ERROR;
                throw new ResponseException(status, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
            }
        }

        /* JADX INFO: Multiple debug info for r11v9 java.lang.String: [D('attributeString' java.lang.String), D('matcher' java.util.regex.Matcher)] */
        private void decodeMultipartFormData(String boundary, String encoding, ByteBuffer fbuf, Map<String, String> parms2, Map<String, String> files) throws ResponseException {
            Exception e;
            Exception e2;
            byte[] part_header_buff;
            int[] boundary_idxs;
            HTTPSession hTTPSession = this;
            try {
                int[] boundary_idxs2 = hTTPSession.getBoundaryPositions(fbuf, boundary.getBytes());
                int i = 2;
                if (boundary_idxs2.length >= 2) {
                    int i2 = 1024;
                    byte[] part_header_buff2 = new byte[1024];
                    int i3 = 0;
                    int bi = 0;
                    while (bi < boundary_idxs2.length - 1) {
                        fbuf.position(boundary_idxs2[bi]);
                        int len = fbuf.remaining() < i2 ? fbuf.remaining() : i2;
                        fbuf.get(part_header_buff2, i3, len);
                        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(part_header_buff2, i3, len), Charset.forName(encoding)), len);
                        int headerLines = 0 + 1;
                        try {
                            if (in.readLine().contains(boundary)) {
                                String content_type = null;
                                String mpline = in.readLine();
                                int headerLines2 = headerLines + 1;
                                String part_name = null;
                                String key = null;
                                while (mpline != null && mpline.trim().length() > 0) {
                                    Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
                                    if (matcher.matches()) {
                                        String attributeString = matcher.group(i);
                                        Matcher matcher2 = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                                        while (matcher2.find()) {
                                            String key2 = matcher2.group(1);
                                            if (key2.equalsIgnoreCase("name")) {
                                                part_name = matcher2.group(2);
                                                key = key;
                                            } else if (key2.equalsIgnoreCase("filename")) {
                                                key = matcher2.group(2);
                                            } else {
                                                key = key;
                                            }
                                            attributeString = attributeString;
                                        }
                                    }
                                    Matcher matcher3 = NanoHTTPD.CONTENT_TYPE_PATTERN.matcher(mpline);
                                    if (matcher3.matches()) {
                                        content_type = matcher3.group(2).trim();
                                    }
                                    mpline = in.readLine();
                                    headerLines2++;
                                    i = 2;
                                }
                                int part_header_len = 0;
                                while (true) {
                                    int headerLines3 = headerLines2 - 1;
                                    if (headerLines2 <= 0) {
                                        break;
                                    }
                                    part_header_len = hTTPSession.scipOverNewLine(part_header_buff2, part_header_len);
                                    headerLines2 = headerLines3;
                                }
                                if (part_header_len < len - 4) {
                                    int part_data_start = boundary_idxs2[bi] + part_header_len;
                                    int part_data_end = boundary_idxs2[bi + 1] - 4;
                                    fbuf.position(part_data_start);
                                    if (content_type == null) {
                                        boundary_idxs = boundary_idxs2;
                                        byte[] data_bytes = new byte[(part_data_end - part_data_start)];
                                        fbuf.get(data_bytes);
                                        part_header_buff = part_header_buff2;
                                        try {
                                            parms2.put(part_name, new String(data_bytes, encoding));
                                        } catch (ResponseException re) {
                                            throw re;
                                        } catch (Exception e3) {
                                            e2 = e3;
                                            throw new ResponseException(Response.Status.INTERNAL_ERROR, e2.toString());
                                        }
                                    } else {
                                        boundary_idxs = boundary_idxs2;
                                        part_header_buff = part_header_buff2;
                                        String path = hTTPSession.saveTmpFile(fbuf, part_data_start, part_data_end - part_data_start, key);
                                        if (!files.containsKey(part_name)) {
                                            files.put(part_name, path);
                                        } else {
                                            int count = 2;
                                            while (true) {
                                                if (!files.containsKey(part_name + count)) {
                                                    break;
                                                }
                                                count++;
                                            }
                                            files.put(part_name + count, path);
                                        }
                                        parms2.put(part_name, key);
                                    }
                                    bi++;
                                    i2 = 1024;
                                    i = 2;
                                    i3 = 0;
                                    hTTPSession = this;
                                    boundary_idxs2 = boundary_idxs;
                                    part_header_buff2 = part_header_buff;
                                } else {
                                    throw new ResponseException(Response.Status.INTERNAL_ERROR, "Multipart header size exceeds MAX_HEADER_SIZE.");
                                }
                            } else {
                                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
                            }
                        } catch (ResponseException e4) {
                            e = e4;
                            throw e;
                        } catch (Exception e5) {
                            e2 = e5;
                            throw new ResponseException(Response.Status.INTERNAL_ERROR, e2.toString());
                        }
                    }
                    return;
                }
                throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but contains less than two boundary strings.");
            } catch (ResponseException e6) {
                e = e6;
                throw e;
            } catch (Exception e7) {
                e2 = e7;
                throw new ResponseException(Response.Status.INTERNAL_ERROR, e2.toString());
            }
        }

        private int scipOverNewLine(byte[] part_header_buff, int index) {
            while (part_header_buff[index] != 10) {
                index++;
            }
            return index + 1;
        }

        private void decodeParms(String parms2, Map<String, String> p) {
            if (parms2 == null) {
                this.queryParameterString = "";
                return;
            }
            this.queryParameterString = parms2;
            StringTokenizer st = new StringTokenizer(parms2, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf(61);
                if (sep >= 0) {
                    p.put(NanoHTTPD.decodePercent(e.substring(0, sep)).trim(), NanoHTTPD.decodePercent(e.substring(sep + 1)));
                } else {
                    p.put(NanoHTTPD.decodePercent(e).trim(), "");
                }
            }
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public void execute() throws IOException {
            Response r = null;
            try {
                byte[] buf = new byte[8192];
                boolean z = false;
                this.splitbyte = 0;
                this.rlen = 0;
                this.inputStream.mark(8192);
                try {
                    int read = this.inputStream.read(buf, 0, 8192);
                    if (read != -1) {
                        while (read > 0) {
                            this.rlen += read;
                            this.splitbyte = findHeaderEnd(buf, this.rlen);
                            if (this.splitbyte > 0) {
                                break;
                            }
                            read = this.inputStream.read(buf, this.rlen, 8192 - this.rlen);
                        }
                        if (this.splitbyte < this.rlen) {
                            this.inputStream.reset();
                            this.inputStream.skip((long) this.splitbyte);
                        }
                        this.parms = new HashMap();
                        if (this.headers == null) {
                            this.headers = new HashMap();
                        } else {
                            this.headers.clear();
                        }
                        BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf, 0, this.rlen)));
                        Map<String, String> pre = new HashMap<>();
                        decodeHeader(hin, pre, this.parms, this.headers);
                        if (this.remoteIp != null) {
                            this.headers.put("remote-addr", this.remoteIp);
                            this.headers.put("http-client-ip", this.remoteIp);
                        }
                        this.method = Method.lookup(pre.get("method"));
                        if (this.method != null) {
                            this.uri = pre.get("uri");
                            this.cookies = new CookieHandler(this.headers);
                            String connection = this.headers.get("connection");
                            boolean keepAlive = this.protocolVersion.equals("HTTP/1.1") && (connection == null || !connection.matches("(?i).*close.*"));
                            r = NanoHTTPD.this.serve(this);
                            if (r != null) {
                                String acceptEncoding = this.headers.get("accept-encoding");
                                this.cookies.unloadQueue(r);
                                r.setRequestMethod(this.method);
                                if (NanoHTTPD.this.useGzipWhenAccepted(r) && acceptEncoding != null && acceptEncoding.contains("gzip")) {
                                    z = true;
                                }
                                r.setGzipEncoding(z);
                                r.setKeepAlive(keepAlive);
                                r.send(this.outputStream);
                                if (!keepAlive || "close".equalsIgnoreCase(r.getHeader("connection"))) {
                                    throw new SocketException("NanoHttpd Shutdown");
                                }
                                NanoHTTPD.safeClose(r);
                                this.tempFileManager.clear();
                                return;
                            }
                            throw new ResponseException(Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                        }
                        throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
                    }
                    NanoHTTPD.safeClose(this.inputStream);
                    NanoHTTPD.safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                } catch (Exception e) {
                    NanoHTTPD.safeClose(this.inputStream);
                    NanoHTTPD.safeClose(this.outputStream);
                    throw new SocketException("NanoHttpd Shutdown");
                }
            } catch (SocketException e2) {
                throw e2;
            } catch (SocketTimeoutException ste) {
                throw ste;
            } catch (IOException ioe) {
                NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage()).send(this.outputStream);
                NanoHTTPD.safeClose(this.outputStream);
            } catch (ResponseException re) {
                NanoHTTPD.newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage()).send(this.outputStream);
                NanoHTTPD.safeClose(this.outputStream);
            } catch (Throwable e3) {
                NanoHTTPD.safeClose(null);
                this.tempFileManager.clear();
                throw e3;
            }
        }

        private int findHeaderEnd(byte[] buf, int rlen2) {
            for (int splitbyte2 = 0; splitbyte2 + 1 < rlen2; splitbyte2++) {
                if (buf[splitbyte2] == 13 && buf[splitbyte2 + 1] == 10 && splitbyte2 + 3 < rlen2 && buf[splitbyte2 + 2] == 13 && buf[splitbyte2 + 3] == 10) {
                    return splitbyte2 + 4;
                }
                if (buf[splitbyte2] == 10 && buf[splitbyte2 + 1] == 10) {
                    return splitbyte2 + 2;
                }
            }
            return 0;
        }

        private int[] getBoundaryPositions(ByteBuffer b, byte[] boundary) {
            int[] res = new int[0];
            if (b.remaining() < boundary.length) {
                return res;
            }
            int search_window_pos = 0;
            byte[] search_window = new byte[(boundary.length + 4096)];
            int first_fill = b.remaining() < search_window.length ? b.remaining() : search_window.length;
            b.get(search_window, 0, first_fill);
            int new_bytes = first_fill - boundary.length;
            do {
                int j = 0;
                while (j < new_bytes) {
                    int i = 0;
                    while (i < boundary.length && search_window[j + i] == boundary[i]) {
                        if (i == boundary.length - 1) {
                            int[] new_res = new int[(res.length + 1)];
                            System.arraycopy(res, 0, new_res, 0, res.length);
                            new_res[res.length] = search_window_pos + j;
                            res = new_res;
                        }
                        i++;
                    }
                    j++;
                }
                search_window_pos += new_bytes;
                System.arraycopy(search_window, search_window.length - boundary.length, search_window, 0, boundary.length);
                int new_bytes2 = search_window.length - boundary.length;
                new_bytes = b.remaining() < new_bytes2 ? b.remaining() : new_bytes2;
                b.get(search_window, boundary.length, new_bytes);
            } while (new_bytes > 0);
            return res;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public CookieHandler getCookies() {
            return this.cookies;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public final Map<String, String> getHeaders() {
            return this.headers;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public final InputStream getInputStream() {
            return this.inputStream;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public final Method getMethod() {
            return this.method;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public final Map<String, String> getParms() {
            return this.parms;
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public String getQueryParameterString() {
            return this.queryParameterString;
        }

        private RandomAccessFile getTmpBucket() {
            try {
                return new RandomAccessFile(this.tempFileManager.createTempFile(null).getName(), "rw");
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public final String getUri() {
            return this.uri;
        }

        public long getBodySize() {
            if (this.headers.containsKey("content-length")) {
                return Long.parseLong(this.headers.get("content-length"));
            }
            int i = this.splitbyte;
            int i2 = this.rlen;
            if (i < i2) {
                return (long) (i2 - i);
            }
            return 0;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // fi.iki.elonen.NanoHTTPD.IHTTPSession
        public void parseBody(Map<String, String> files) throws IOException, ResponseException {
            RandomAccessFile randomAccessFile;
            Throwable th;
            DataOutput request_data_output;
            ByteBuffer fbuf;
            StringTokenizer st;
            String contentType;
            try {
                long size = getBodySize();
                ByteArrayOutputStream baos = null;
                if (size < 1024) {
                    baos = new ByteArrayOutputStream();
                    randomAccessFile = null;
                    request_data_output = new DataOutputStream(baos);
                } else {
                    DataOutput randomAccessFile2 = getTmpBucket();
                    randomAccessFile = randomAccessFile2;
                    request_data_output = randomAccessFile2;
                }
                try {
                    byte[] buf = new byte[512];
                    long size2 = size;
                    while (this.rlen >= 0 && size2 > 0) {
                        this.rlen = this.inputStream.read(buf, 0, (int) Math.min(size2, 512L));
                        size2 -= (long) this.rlen;
                        if (this.rlen > 0) {
                            request_data_output.write(buf, 0, this.rlen);
                        }
                    }
                    if (baos != null) {
                        fbuf = ByteBuffer.wrap(baos.toByteArray(), 0, baos.size());
                    } else {
                        ByteBuffer fbuf2 = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
                        randomAccessFile.seek(0);
                        fbuf = fbuf2;
                    }
                    if (Method.POST.equals(this.method)) {
                        String contentTypeHeader = this.headers.get("content-type");
                        if (contentTypeHeader != null) {
                            StringTokenizer st2 = new StringTokenizer(contentTypeHeader, ",; ");
                            if (st2.hasMoreTokens()) {
                                contentType = st2.nextToken();
                                st = st2;
                            } else {
                                contentType = "";
                                st = st2;
                            }
                        } else {
                            contentType = "";
                            st = null;
                        }
                        if (!"multipart/form-data".equalsIgnoreCase(contentType)) {
                            byte[] postBytes = new byte[fbuf.remaining()];
                            fbuf.get(postBytes);
                            String postLine = new String(postBytes).trim();
                            if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
                                decodeParms(postLine, this.parms);
                            } else if (postLine.length() != 0) {
                                files.put("postData", postLine);
                            }
                        } else if (st.hasMoreTokens()) {
                            decodeMultipartFormData(getAttributeFromContentHeader(contentTypeHeader, NanoHTTPD.BOUNDARY_PATTERN, null), getAttributeFromContentHeader(contentTypeHeader, NanoHTTPD.CHARSET_PATTERN, "US-ASCII"), fbuf, this.parms, files);
                        } else {
                            throw new ResponseException(Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                        }
                    } else if (Method.PUT.equals(this.method)) {
                        files.put("content", saveTmpFile(fbuf, 0, fbuf.limit(), null));
                    }
                    NanoHTTPD.safeClose(randomAccessFile);
                } catch (Throwable th2) {
                    th = th2;
                    NanoHTTPD.safeClose(randomAccessFile);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = null;
                NanoHTTPD.safeClose(randomAccessFile);
                throw th;
            }
        }

        private String getAttributeFromContentHeader(String contentTypeHeader, Pattern pattern, String defaultValue) {
            Matcher matcher = pattern.matcher(contentTypeHeader);
            return matcher.find() ? matcher.group(2) : defaultValue;
        }

        private String saveTmpFile(ByteBuffer b, int offset, int len, String filename_hint) {
            if (len <= 0) {
                return "";
            }
            FileOutputStream fileOutputStream = null;
            try {
                TempFile tempFile = this.tempFileManager.createTempFile(filename_hint);
                ByteBuffer src = b.duplicate();
                fileOutputStream = new FileOutputStream(tempFile.getName());
                FileChannel dest = fileOutputStream.getChannel();
                src.position(offset).limit(offset + len);
                dest.write(src.slice());
                String path = tempFile.getName();
                NanoHTTPD.safeClose(fileOutputStream);
                return path;
            } catch (Exception e) {
                throw new Error(e);
            } catch (Throwable th) {
                NanoHTTPD.safeClose(fileOutputStream);
                throw th;
            }
        }
    }

    public enum Method {
        GET,
        PUT,
        POST,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE,
        CONNECT,
        PATCH;

        static Method lookup(String method) {
            Method[] values = values();
            for (Method m : values) {
                if (m.toString().equalsIgnoreCase(method)) {
                    return m;
                }
            }
            return null;
        }
    }

    public static class Response implements Closeable {
        private boolean chunkedTransfer;
        private long contentLength;
        private InputStream data;
        private boolean encodeAsGzip;
        private final Map<String, String> header = new HashMap();
        private boolean keepAlive;
        private String mimeType;
        private Method requestMethod;
        private IStatus status;

        public interface IStatus {
            String getDescription();

            int getRequestStatus();
        }

        public enum Status implements IStatus {
            SWITCH_PROTOCOL(101, "Switching Protocols"),
            OK(ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS, "OK"),
            CREATED(201, "Created"),
            ACCEPTED(202, "Accepted"),
            NO_CONTENT(204, "No Content"),
            PARTIAL_CONTENT(206, "Partial Content"),
            REDIRECT(301, "Moved Permanently"),
            TEMPORARY_REDIRECT(302, "Moved Temporarily"),
            NOT_MODIFIED(304, "Not Modified"),
            BAD_REQUEST(400, "Bad Request"),
            UNAUTHORIZED(401, "Unauthorized"),
            FORBIDDEN(403, "Forbidden"),
            NOT_FOUND(404, "Not Found"),
            METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
            NOT_ACCEPTABLE(406, "Not Acceptable"),
            REQUEST_TIMEOUT(408, "Request Timeout"),
            CONFLICT(409, "Conflict"),
            RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
            INTERNAL_ERROR(500, "Internal Server Error"),
            NOT_IMPLEMENTED(501, "Not Implemented"),
            UNSUPPORTED_HTTP_VERSION(505, "HTTP Version Not Supported");
            
            private final String description;
            private final int requestStatus;

            private Status(int requestStatus2, String description2) {
                this.requestStatus = requestStatus2;
                this.description = description2;
            }

            @Override // fi.iki.elonen.NanoHTTPD.Response.IStatus
            public String getDescription() {
                return "" + this.requestStatus + " " + this.description;
            }

            @Override // fi.iki.elonen.NanoHTTPD.Response.IStatus
            public int getRequestStatus() {
                return this.requestStatus;
            }
        }

        /* access modifiers changed from: private */
        public static class ChunkedOutputStream extends FilterOutputStream {
            public ChunkedOutputStream(OutputStream out) {
                super(out);
            }

            @Override // java.io.FilterOutputStream, java.io.OutputStream
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override // java.io.FilterOutputStream, java.io.OutputStream
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }

            @Override // java.io.FilterOutputStream, java.io.OutputStream
            public void write(byte[] b, int off, int len) throws IOException {
                if (len != 0) {
                    this.out.write(String.format("%x\r\n", Integer.valueOf(len)).getBytes());
                    this.out.write(b, off, len);
                    this.out.write("\r\n".getBytes());
                }
            }

            public void finish() throws IOException {
                this.out.write("0\r\n\r\n".getBytes());
            }
        }

        protected Response(IStatus status2, String mimeType2, InputStream data2, long totalBytes) {
            this.status = status2;
            this.mimeType = mimeType2;
            boolean z = false;
            if (data2 == null) {
                this.data = new ByteArrayInputStream(new byte[0]);
                this.contentLength = 0;
            } else {
                this.data = data2;
                this.contentLength = totalBytes;
            }
            this.chunkedTransfer = this.contentLength < 0 ? true : z;
            this.keepAlive = true;
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            InputStream inputStream = this.data;
            if (inputStream != null) {
                inputStream.close();
            }
        }

        public void addHeader(String name, String value) {
            this.header.put(name, value);
        }

        public InputStream getData() {
            return this.data;
        }

        public String getHeader(String name) {
            for (String headerName : this.header.keySet()) {
                if (headerName.equalsIgnoreCase(name)) {
                    return this.header.get(headerName);
                }
            }
            return null;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public Method getRequestMethod() {
            return this.requestMethod;
        }

        public IStatus getStatus() {
            return this.status;
        }

        public void setGzipEncoding(boolean encodeAsGzip2) {
            this.encodeAsGzip = encodeAsGzip2;
        }

        public void setKeepAlive(boolean useKeepAlive) {
            this.keepAlive = useKeepAlive;
        }

        private static boolean headerAlreadySent(Map<String, String> header2, String name) {
            boolean alreadySent = false;
            for (String headerName : header2.keySet()) {
                alreadySent |= headerName.equalsIgnoreCase(name);
            }
            return alreadySent;
        }

        /* access modifiers changed from: protected */
        public void send(OutputStream outputStream) {
            String mime = this.mimeType;
            SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                if (this.status != null) {
                    PrintWriter pw = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
                    pw.print("HTTP/1.1 " + this.status.getDescription() + " \r\n");
                    if (mime != null) {
                        pw.print("Content-Type: " + mime + "\r\n");
                    }
                    if (this.header == null || this.header.get("Date") == null) {
                        pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                    }
                    if (this.header != null) {
                        for (String key : this.header.keySet()) {
                            pw.print(key + ": " + this.header.get(key) + "\r\n");
                        }
                    }
                    if (!headerAlreadySent(this.header, "connection")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Connection: ");
                        sb.append(this.keepAlive ? "keep-alive" : "close");
                        sb.append("\r\n");
                        pw.print(sb.toString());
                    }
                    if (headerAlreadySent(this.header, "content-length")) {
                        this.encodeAsGzip = false;
                    }
                    if (this.encodeAsGzip) {
                        pw.print("Content-Encoding: gzip\r\n");
                        setChunkedTransfer(true);
                    }
                    long pending = this.data != null ? this.contentLength : 0;
                    if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                        pw.print("Transfer-Encoding: chunked\r\n");
                    } else if (!this.encodeAsGzip) {
                        pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, this.header, pending);
                    }
                    pw.print("\r\n");
                    pw.flush();
                    sendBodyWithCorrectTransferAndEncoding(outputStream, pending);
                    outputStream.flush();
                    NanoHTTPD.safeClose(this.data);
                    return;
                }
                throw new Error("sendResponse(): Status can't be null.");
            } catch (IOException ioe) {
                NanoHTTPD.LOG.log(Level.SEVERE, "Could not send response to the client", (Throwable) ioe);
            }
        }

        private void sendBodyWithCorrectTransferAndEncoding(OutputStream outputStream, long pending) throws IOException {
            if (this.requestMethod == Method.HEAD || !this.chunkedTransfer) {
                sendBodyWithCorrectEncoding(outputStream, pending);
                return;
            }
            ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
            sendBodyWithCorrectEncoding(chunkedOutputStream, -1);
            chunkedOutputStream.finish();
        }

        private void sendBodyWithCorrectEncoding(OutputStream outputStream, long pending) throws IOException {
            if (this.encodeAsGzip) {
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                sendBody(gzipOutputStream, -1);
                gzipOutputStream.finish();
                return;
            }
            sendBody(outputStream, pending);
        }

        private void sendBody(OutputStream outputStream, long pending) throws IOException {
            byte[] buff = new byte[((int) 16384)];
            boolean sendEverything = pending == -1;
            while (true) {
                if (pending > 0 || sendEverything) {
                    int read = this.data.read(buff, 0, (int) (sendEverything ? 16384 : Math.min(pending, 16384L)));
                    if (read > 0) {
                        outputStream.write(buff, 0, read);
                        if (!sendEverything) {
                            pending -= (long) read;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        protected static long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header2, long size) {
            for (String headerName : header2.keySet()) {
                if (headerName.equalsIgnoreCase("content-length")) {
                    try {
                        return Long.parseLong(header2.get(headerName));
                    } catch (NumberFormatException e) {
                        return size;
                    }
                }
            }
            pw.print("Content-Length: " + size + "\r\n");
            return size;
        }

        public void setChunkedTransfer(boolean chunkedTransfer2) {
            this.chunkedTransfer = chunkedTransfer2;
        }

        public void setData(InputStream data2) {
            this.data = data2;
        }

        public void setMimeType(String mimeType2) {
            this.mimeType = mimeType2;
        }

        public void setRequestMethod(Method requestMethod2) {
            this.requestMethod = requestMethod2;
        }

        public void setStatus(IStatus status2) {
            this.status = status2;
        }
    }

    public static final class ResponseException extends Exception {
        private static final long serialVersionUID = 6569838532917408380L;
        private final Response.Status status;

        public ResponseException(Response.Status status2, String message) {
            super(message);
            this.status = status2;
        }

        public ResponseException(Response.Status status2, String message, Exception e) {
            super(message, e);
            this.status = status2;
        }

        public Response.Status getStatus() {
            return this.status;
        }
    }

    public class ServerRunnable implements Runnable {
        private IOException bindException;
        private boolean hasBinded;
        private final int timeout;

        private ServerRunnable(int timeout2) {
            this.hasBinded = false;
            this.timeout = timeout2;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                NanoHTTPD.this.myServerSocket.bind(NanoHTTPD.this.hostname != null ? new InetSocketAddress(NanoHTTPD.this.hostname, NanoHTTPD.this.myPort) : new InetSocketAddress(NanoHTTPD.this.myPort));
                this.hasBinded = true;
                do {
                    try {
                        Socket finalAccept = NanoHTTPD.this.myServerSocket.accept();
                        if (this.timeout > 0) {
                            finalAccept.setSoTimeout(this.timeout);
                        }
                        NanoHTTPD.this.asyncRunner.exec(NanoHTTPD.this.createClientHandler(finalAccept, finalAccept.getInputStream()));
                    } catch (IOException e) {
                        NanoHTTPD.LOG.log(Level.FINE, "Communication with the client broken", (Throwable) e);
                    }
                } while (!NanoHTTPD.this.myServerSocket.isClosed());
            } catch (IOException e2) {
                this.bindException = e2;
            }
        }
    }

    public static Map<String, String> mimeTypes() {
        if (MIME_TYPES == null) {
            MIME_TYPES = new HashMap();
            loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/default-mimetypes.properties");
            loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/mimetypes.properties");
            if (MIME_TYPES.isEmpty()) {
                LOG.log(Level.WARNING, "no mime types found in the classpath! please provide mimetypes.properties");
            }
        }
        return MIME_TYPES;
    }

    private static void loadMimeTypes(Map<String, String> result, String resourceName) {
        try {
            Enumeration<URL> resources = NanoHTTPD.class.getClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties properties = new Properties();
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    properties.load(url.openStream());
                } catch (IOException e) {
                    Logger logger = LOG;
                    Level level = Level.SEVERE;
                    logger.log(level, "could not load mimetypes from " + url, (Throwable) e);
                } finally {
                    safeClose(stream);
                }
                result.putAll(properties);
            }
        } catch (IOException e2) {
            Logger logger2 = LOG;
            Level level2 = Level.INFO;
            logger2.log(level2, "no mime types available at " + resourceName);
        }
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManager[] keyManagers) throws IOException {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(loadedKeyStore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
            return ctx.getServerSocketFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManagerFactory loadedKeyFactory) throws IOException {
        try {
            return makeSSLSocketFactory(loadedKeyStore, loadedKeyFactory.getKeyManagers());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static SSLServerSocketFactory makeSSLSocketFactory(String keyAndTrustStoreClasspathPath, char[] passphrase) throws IOException {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(NanoHTTPD.class.getResourceAsStream(keyAndTrustStoreClasspathPath), passphrase);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            return makeSSLSocketFactory(keystore, keyManagerFactory);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf(46);
        String mime = null;
        if (dot >= 0) {
            mime = mimeTypes().get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? "application/octet-stream" : mime;
    }

    /* access modifiers changed from: private */
    public static final void safeClose(Object closeable) {
        if (closeable != null) {
            try {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else if (closeable instanceof Socket) {
                    ((Socket) closeable).close();
                } else if (closeable instanceof ServerSocket) {
                    ((ServerSocket) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Could not close", (Throwable) e);
            }
        }
    }

    public NanoHTTPD(int port) {
        this(null, port);
    }

    public NanoHTTPD(String hostname2, int port) {
        this.serverSocketFactory = new DefaultServerSocketFactory();
        this.hostname = hostname2;
        this.myPort = port;
        setTempFileManagerFactory(new DefaultTempFileManagerFactory());
        setAsyncRunner(new DefaultAsyncRunner());
    }

    public synchronized void closeAllConnections() {
        stop();
    }

    /* access modifiers changed from: protected */
    public ClientHandler createClientHandler(Socket finalAccept, InputStream inputStream) {
        return new ClientHandler(inputStream, finalAccept);
    }

    /* access modifiers changed from: protected */
    public ServerRunnable createServerRunnable(int timeout) {
        return new ServerRunnable(timeout);
    }

    protected static Map<String, List<String>> decodeParameters(Map<String, String> parms) {
        return decodeParameters(parms.get(QUERY_STRING_PARAMETER));
    }

    protected static Map<String, List<String>> decodeParameters(String queryString) {
        Map<String, List<String>> parms = new HashMap<>();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf(61);
                String propertyName = (sep >= 0 ? decodePercent(e.substring(0, sep)) : decodePercent(e)).trim();
                if (!parms.containsKey(propertyName)) {
                    parms.put(propertyName, new ArrayList<>());
                }
                String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
                if (propertyValue != null) {
                    parms.get(propertyName).add(propertyValue);
                }
            }
        }
        return parms;
    }

    protected static String decodePercent(String str) {
        try {
            return URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            LOG.log(Level.WARNING, "Encoding not supported, ignored", (Throwable) ignored);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean useGzipWhenAccepted(Response r) {
        return r.getMimeType() != null && r.getMimeType().toLowerCase().contains("text/");
    }

    public final int getListeningPort() {
        if (this.myServerSocket == null) {
            return -1;
        }
        return this.myServerSocket.getLocalPort();
    }

    public final boolean isAlive() {
        return wasStarted() && !this.myServerSocket.isClosed() && this.myThread.isAlive();
    }

    public ServerSocketFactory getServerSocketFactory() {
        return this.serverSocketFactory;
    }

    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory2) {
        this.serverSocketFactory = serverSocketFactory2;
    }

    public String getHostname() {
        return this.hostname;
    }

    public TempFileManagerFactory getTempFileManagerFactory() {
        return this.tempFileManagerFactory;
    }

    public void makeSecure(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
        this.serverSocketFactory = new SecureServerSocketFactory(sslServerSocketFactory, sslProtocols);
    }

    public static Response newChunkedResponse(Response.IStatus status, String mimeType, InputStream data) {
        return new Response(status, mimeType, data, -1);
    }

    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, InputStream data, long totalBytes) {
        return new Response(status, mimeType, data, totalBytes);
    }

    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, String txt) {
        byte[] bytes;
        if (txt == null) {
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        }
        try {
            bytes = txt.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "encoding problem, responding nothing", (Throwable) e);
            bytes = new byte[0];
        }
        return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(bytes), (long) bytes.length);
    }

    public static Response newFixedLengthResponse(String msg) {
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, msg);
    }

    public Response serve(IHTTPSession session) {
        Map<String, String> files = new HashMap<>();
        Method method = session.getMethod();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (IOException ioe) {
                Response.Status status = Response.Status.INTERNAL_ERROR;
                return newFixedLengthResponse(status, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        }
        Map<String, String> parms = session.getParms();
        parms.put(QUERY_STRING_PARAMETER, session.getQueryParameterString());
        return serve(session.getUri(), method, session.getHeaders(), parms, files);
    }

    @Deprecated
    public Response serve(String uri, Method method, Map<String, String> map, Map<String, String> map2, Map<String, String> map3) {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }

    public void setAsyncRunner(AsyncRunner asyncRunner2) {
        this.asyncRunner = asyncRunner2;
    }

    public void setTempFileManagerFactory(TempFileManagerFactory tempFileManagerFactory2) {
        this.tempFileManagerFactory = tempFileManagerFactory2;
    }

    public void start() throws IOException {
        start(5000);
    }

    public void start(int timeout) throws IOException {
        start(timeout, true);
    }

    public void start(int timeout, boolean daemon) throws IOException {
        this.myServerSocket = getServerSocketFactory().create();
        this.myServerSocket.setReuseAddress(true);
        ServerRunnable serverRunnable = createServerRunnable(timeout);
        this.myThread = new Thread(serverRunnable);
        this.myThread.setDaemon(daemon);
        this.myThread.setName("NanoHttpd Main Listener");
        this.myThread.start();
        while (!serverRunnable.hasBinded && serverRunnable.bindException == null) {
            try {
                Thread.sleep(10);
            } catch (Throwable th) {
            }
        }
        if (serverRunnable.bindException != null) {
            throw serverRunnable.bindException;
        }
    }

    public void stop() {
        try {
            safeClose(this.myServerSocket);
            this.asyncRunner.closeAll();
            if (this.myThread != null) {
                this.myThread.join();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not stop all connections", (Throwable) e);
        }
    }

    public final boolean wasStarted() {
        return (this.myServerSocket == null || this.myThread == null) ? false : true;
    }
}
