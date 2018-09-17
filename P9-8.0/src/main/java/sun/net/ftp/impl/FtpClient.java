package sun.net.ftp.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient.TransferType;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpDirEntry.Type;
import sun.net.ftp.FtpDirParser;
import sun.net.ftp.FtpProtocolException;
import sun.net.ftp.FtpReplyCode;
import sun.security.util.DerValue;
import sun.util.logging.PlatformLogger;

public class FtpClient extends sun.net.ftp.FtpClient {
    private static String[] MDTMformats = new String[]{"yyyyMMddHHmmss.SSS", "yyyyMMddHHmmss"};
    private static SimpleDateFormat[] dateFormats = new SimpleDateFormat[MDTMformats.length];
    private static int defaultConnectTimeout;
    private static int defaultSoTimeout;
    private static String encoding;
    private static Pattern epsvPat = null;
    private static Pattern linkp = Pattern.compile("(\\p{Print}+) \\-\\> (\\p{Print}+)$");
    private static final PlatformLogger logger = PlatformLogger.getLogger("sun.net.ftp.FtpClient");
    private static Pattern pasvPat = null;
    private static String[] patStrings = new String[]{"([\\-ld](?:[r\\-][w\\-][x\\-]){3})\\s*\\d+ (\\w+)\\s*(\\w+)\\s*(\\d+)\\s*([A-Z][a-z][a-z]\\s*\\d+)\\s*(\\d\\d:\\d\\d)\\s*(\\p{Print}*)", "([\\-ld](?:[r\\-][w\\-][x\\-]){3})\\s*\\d+ (\\w+)\\s*(\\w+)\\s*(\\d+)\\s*([A-Z][a-z][a-z]\\s*\\d+)\\s*(\\d{4})\\s*(\\p{Print}*)", "(\\d{2}/\\d{2}/\\d{4})\\s*(\\d{2}:\\d{2}[ap])\\s*((?:[0-9,]+)|(?:<DIR>))\\s*(\\p{Graph}*)", "(\\d{2}-\\d{2}-\\d{2})\\s*(\\d{2}:\\d{2}[AP]M)\\s*((?:[0-9,]+)|(?:<DIR>))\\s*(\\p{Graph}*)"};
    private static int[][] patternGroups = new int[][]{new int[]{7, 4, 5, 6, 0, 1, 2, 3}, new int[]{7, 4, 5, 0, 6, 1, 2, 3}, new int[]{4, 3, 1, 2, 0, 0, 0, 0}, new int[]{4, 3, 1, 2, 0, 0, 0, 0}};
    private static Pattern[] patterns = new Pattern[patStrings.length];
    private static Pattern transPat = null;
    private int connectTimeout = -1;
    private DateFormat df = DateFormat.getDateInstance(2, Locale.US);
    private InputStream in;
    private String lastFileName;
    private FtpReplyCode lastReplyCode = null;
    private long lastTransSize = -1;
    private boolean loggedIn = false;
    private FtpDirParser mlsxParser = new MLSxParser(this, null);
    private Socket oldSocket;
    private PrintStream out;
    private FtpDirParser parser = new DefaultParser(this, null);
    private final boolean passiveMode = true;
    private Proxy proxy;
    private int readTimeout = -1;
    private boolean replyPending = false;
    private long restartOffset = 0;
    private Socket server;
    private InetSocketAddress serverAddr;
    private Vector<String> serverResponse = new Vector(1);
    private SSLSocketFactory sslFact;
    private TransferType type = TransferType.BINARY;
    private boolean useCrypto = false;
    private String welcomeMsg;

    private class DefaultParser implements FtpDirParser {
        /* synthetic */ DefaultParser(FtpClient this$0, DefaultParser -this1) {
            this();
        }

        private DefaultParser() {
        }

        public FtpDirEntry parseLine(String line) {
            int j;
            String fdate = null;
            String fsize = null;
            String time = null;
            String filename = null;
            String permstring = null;
            String username = null;
            String groupname = null;
            boolean dir = false;
            Calendar now = Calendar.getInstance();
            int year = now.get(1);
            for (j = 0; j < FtpClient.patterns.length; j++) {
                Matcher m = FtpClient.patterns[j].matcher(line);
                if (m.find()) {
                    filename = m.group(FtpClient.patternGroups[j][0]);
                    fsize = m.group(FtpClient.patternGroups[j][1]);
                    fdate = m.group(FtpClient.patternGroups[j][2]);
                    if (FtpClient.patternGroups[j][4] > 0) {
                        fdate = fdate + ", " + m.group(FtpClient.patternGroups[j][4]);
                    } else if (FtpClient.patternGroups[j][3] > 0) {
                        fdate = fdate + ", " + String.valueOf(year);
                    }
                    if (FtpClient.patternGroups[j][3] > 0) {
                        time = m.group(FtpClient.patternGroups[j][3]);
                    }
                    if (FtpClient.patternGroups[j][5] > 0) {
                        permstring = m.group(FtpClient.patternGroups[j][5]);
                        dir = permstring.startsWith("d");
                    }
                    if (FtpClient.patternGroups[j][6] > 0) {
                        username = m.group(FtpClient.patternGroups[j][6]);
                    }
                    if (FtpClient.patternGroups[j][7] > 0) {
                        groupname = m.group(FtpClient.patternGroups[j][7]);
                    }
                    if ("<DIR>".equals(fsize)) {
                        dir = true;
                        fsize = null;
                    }
                }
            }
            if (filename == null) {
                return null;
            }
            Date d;
            try {
                d = FtpClient.this.df.parse(fdate);
            } catch (Exception e) {
                d = null;
            }
            if (!(d == null || time == null)) {
                int c = time.indexOf(":");
                now.setTime(d);
                now.set(10, Integer.parseInt(time.substring(0, c)));
                now.set(12, Integer.parseInt(time.substring(c + 1)));
                d = now.getTime();
            }
            Matcher m2 = FtpClient.linkp.matcher(filename);
            if (m2.find()) {
                filename = m2.group(1);
            }
            boolean[][] perms = (boolean[][]) Array.newInstance(Boolean.TYPE, 3, 3);
            for (int i = 0; i < 3; i++) {
                for (j = 0; j < 3; j++) {
                    perms[i][j] = permstring.charAt((i * 3) + j) != '-';
                }
            }
            FtpDirEntry file = new FtpDirEntry(filename);
            file.setUser(username).setGroup(groupname);
            file.setSize(Long.parseLong(fsize)).setLastModified(d);
            file.setPermissions(perms);
            Type type = dir ? Type.DIR : line.charAt(0) == 'l' ? Type.LINK : Type.FILE;
            file.setType(type);
            return file;
        }
    }

    private class FtpFileIterator implements Iterator<FtpDirEntry>, Closeable {
        private boolean eof = false;
        private FtpDirParser fparser = null;
        private BufferedReader in = null;
        private FtpDirEntry nextFile = null;

        public FtpFileIterator(FtpDirParser p, BufferedReader in) {
            this.in = in;
            this.fparser = p;
            readNext();
        }

        private void readNext() {
            this.nextFile = null;
            if (!this.eof) {
                String line;
                do {
                    try {
                        line = this.in.readLine();
                        if (line != null) {
                            this.nextFile = this.fparser.parseLine(line);
                            if (this.nextFile != null) {
                                return;
                            }
                        }
                    } catch (IOException e) {
                    }
                } while (line != null);
                this.in.close();
                this.eof = true;
            }
        }

        public boolean hasNext() {
            return this.nextFile != null;
        }

        public FtpDirEntry next() {
            FtpDirEntry ret = this.nextFile;
            readNext();
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void close() throws IOException {
            if (!(this.in == null || (this.eof ^ 1) == 0)) {
                this.in.close();
            }
            this.eof = true;
            this.nextFile = null;
        }
    }

    private class MLSxParser implements FtpDirParser {
        private SimpleDateFormat df;

        /* synthetic */ MLSxParser(FtpClient this$0, MLSxParser -this1) {
            this();
        }

        private MLSxParser() {
            this.df = new SimpleDateFormat("yyyyMMddhhmmss");
        }

        public FtpDirEntry parseLine(String line) {
            String name;
            String s;
            Date d;
            int i = line.lastIndexOf(";");
            if (i > 0) {
                name = line.substring(i + 1).trim();
                line = line.substring(0, i);
            } else {
                name = line.trim();
                line = "";
            }
            FtpDirEntry file = new FtpDirEntry(name);
            while (!line.isEmpty()) {
                i = line.indexOf(";");
                if (i > 0) {
                    s = line.substring(0, i);
                    line = line.substring(i + 1);
                } else {
                    s = line;
                    line = "";
                }
                i = s.indexOf("=");
                if (i > 0) {
                    file.addFact(s.substring(0, i), s.substring(i + 1));
                }
            }
            s = file.getFact("Size");
            if (s != null) {
                file.setSize(Long.parseLong(s));
            }
            s = file.getFact("Modify");
            if (s != null) {
                d = null;
                try {
                    d = this.df.parse(s);
                } catch (ParseException e) {
                }
                if (d != null) {
                    file.setLastModified(d);
                }
            }
            s = file.getFact("Create");
            if (s != null) {
                d = null;
                try {
                    d = this.df.parse(s);
                } catch (ParseException e2) {
                }
                if (d != null) {
                    file.setCreated(d);
                }
            }
            s = file.getFact("Type");
            if (s != null) {
                if (s.equalsIgnoreCase("file")) {
                    file.setType(Type.FILE);
                }
                if (s.equalsIgnoreCase("dir")) {
                    file.setType(Type.DIR);
                }
                if (s.equalsIgnoreCase("cdir")) {
                    file.setType(Type.CDIR);
                }
                if (s.equalsIgnoreCase("pdir")) {
                    file.setType(Type.PDIR);
                }
            }
            return file;
        }
    }

    static {
        int i;
        encoding = "ISO8859_1";
        final int[] vals = new int[]{0, 0};
        final String[] encs = new String[]{null};
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0).lambda$-java_util_stream_IntPipeline_14709();
                vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0).lambda$-java_util_stream_IntPipeline_14709();
                encs[0] = System.getProperty("file.encoding", "ISO8859_1");
                return null;
            }
        });
        if (vals[0] == 0) {
            defaultSoTimeout = -1;
        } else {
            defaultSoTimeout = vals[0];
        }
        if (vals[1] == 0) {
            defaultConnectTimeout = -1;
        } else {
            defaultConnectTimeout = vals[1];
        }
        encoding = encs[0];
        try {
            if (!isASCIISuperset(encoding)) {
                encoding = "ISO8859_1";
            }
        } catch (Exception e) {
            encoding = "ISO8859_1";
        }
        for (i = 0; i < patStrings.length; i++) {
            patterns[i] = Pattern.compile(patStrings[i]);
        }
        for (i = 0; i < MDTMformats.length; i++) {
            dateFormats[i] = new SimpleDateFormat(MDTMformats[i]);
            dateFormats[i].setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    private static boolean isASCIISuperset(String encoding) throws Exception {
        return Arrays.equals("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,".getBytes(encoding), new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, ObjectStreamConstants.TC_REFERENCE, ObjectStreamConstants.TC_CLASSDESC, ObjectStreamConstants.TC_OBJECT, ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_ARRAY, ObjectStreamConstants.TC_CLASS, ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_ENDBLOCKDATA, ObjectStreamConstants.TC_RESET, ObjectStreamConstants.TC_BLOCKDATALONG, (byte) 45, (byte) 95, (byte) 46, (byte) 33, (byte) 126, (byte) 42, (byte) 39, (byte) 40, (byte) 41, (byte) 59, (byte) 47, (byte) 63, (byte) 58, DerValue.TAG_APPLICATION, (byte) 38, (byte) 61, (byte) 43, (byte) 36, (byte) 44});
    }

    private void getTransferSize() {
        this.lastTransSize = -1;
        String response = getLastResponseString();
        if (transPat == null) {
            transPat = Pattern.compile("150 Opening .*\\((\\d+) bytes\\).");
        }
        Matcher m = transPat.matcher(response);
        if (m.find()) {
            this.lastTransSize = Long.parseLong(m.group(1));
        }
    }

    private void getTransferName() {
        this.lastFileName = null;
        String response = getLastResponseString();
        int i = response.indexOf("unique file name:");
        int e = response.lastIndexOf(41);
        if (i >= 0) {
            this.lastFileName = response.substring(i + 17, e);
        }
    }

    private int readServerResponse() throws IOException {
        int code;
        StringBuffer replyBuf = new StringBuffer(32);
        int continuingCode = -1;
        this.serverResponse.setSize(0);
        while (true) {
            int c = this.in.read();
            if (c != -1) {
                if (c == 13) {
                    c = this.in.read();
                    if (c != 10) {
                        replyBuf.append(13);
                    }
                }
                replyBuf.append((char) c);
                if (c != 10) {
                    continue;
                }
            }
            String response = replyBuf.toString();
            replyBuf.setLength(0);
            if (logger.isLoggable(300)) {
                logger.finest("Server [" + this.serverAddr + "] --> " + response);
            }
            if (response.length() == 0) {
                code = -1;
            } else {
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                } catch (StringIndexOutOfBoundsException e2) {
                }
            }
            this.serverResponse.addElement(response);
            if (continuingCode == -1) {
                if (response.length() < 4 || response.charAt(3) != '-') {
                    break;
                }
                continuingCode = code;
            } else if (code == continuingCode && (response.length() < 4 || response.charAt(3) != '-')) {
            }
        }
        return code;
    }

    private void sendServer(String cmd) {
        this.out.print(cmd);
        if (logger.isLoggable(300)) {
            logger.finest("Server [" + this.serverAddr + "] <-- " + cmd);
        }
    }

    private String getResponseString() {
        return (String) this.serverResponse.elementAt(0);
    }

    private Vector<String> getResponseStrings() {
        return this.serverResponse;
    }

    private boolean readReply() throws IOException {
        this.lastReplyCode = FtpReplyCode.find(readServerResponse());
        if (this.lastReplyCode.isPositivePreliminary()) {
            this.replyPending = true;
            return true;
        } else if (!this.lastReplyCode.isPositiveCompletion() && !this.lastReplyCode.isPositiveIntermediate()) {
            return false;
        } else {
            if (this.lastReplyCode == FtpReplyCode.CLOSING_DATA_CONNECTION) {
                getTransferName();
            }
            return true;
        }
    }

    private boolean issueCommand(String cmd) throws IOException, FtpProtocolException {
        if (isConnected()) {
            if (this.replyPending) {
                try {
                    completePending();
                } catch (FtpProtocolException e) {
                }
            }
            if (cmd.indexOf(10) != -1) {
                FtpProtocolException ex = new FtpProtocolException("Illegal FTP command");
                ex.initCause(new IllegalArgumentException("Illegal carriage return"));
                throw ex;
            }
            sendServer(cmd + "\r\n");
            return readReply();
        }
        throw new IllegalStateException("Not connected");
    }

    private void issueCommandCheck(String cmd) throws FtpProtocolException, IOException {
        if (!issueCommand(cmd)) {
            throw new FtpProtocolException(cmd + ":" + getResponseString(), getLastReplyCode());
        }
    }

    private Socket openPassiveDataConnection(String cmd) throws FtpProtocolException, IOException {
        InetSocketAddress dest;
        Socket s;
        String serverAnswer;
        Matcher m;
        if (issueCommand("EPSV ALL")) {
            issueCommandCheck("EPSV");
            serverAnswer = getResponseString();
            if (epsvPat == null) {
                epsvPat = Pattern.compile("^229 .* \\(\\|\\|\\|(\\d+)\\|\\)");
            }
            m = epsvPat.matcher(serverAnswer);
            if (m.find()) {
                int port = Integer.parseInt(m.group(1));
                InetAddress add = this.server.getInetAddress();
                if (add != null) {
                    dest = new InetSocketAddress(add, port);
                } else {
                    dest = InetSocketAddress.createUnresolved(this.serverAddr.getHostName(), port);
                }
            } else {
                throw new FtpProtocolException("EPSV failed : " + serverAnswer);
            }
        }
        issueCommandCheck("PASV");
        serverAnswer = getResponseString();
        if (pasvPat == null) {
            pasvPat = Pattern.compile("227 .* \\(?(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)?");
        }
        m = pasvPat.matcher(serverAnswer);
        if (m.find()) {
            dest = new InetSocketAddress(m.group(1).replace(',', '.'), Integer.parseInt(m.group(3)) + (Integer.parseInt(m.group(2)) << 8));
        } else {
            throw new FtpProtocolException("PASV failed : " + serverAnswer);
        }
        if (this.proxy == null) {
            s = new Socket();
        } else if (this.proxy.type() == Proxy.Type.SOCKS) {
            s = (Socket) AccessController.doPrivileged(new PrivilegedAction<Socket>() {
                public Socket run() {
                    return new Socket(FtpClient.this.proxy);
                }
            });
        } else {
            s = new Socket(Proxy.NO_PROXY);
        }
        s.bind(new InetSocketAddress((InetAddress) AccessController.doPrivileged(new PrivilegedAction<InetAddress>() {
            public InetAddress run() {
                return FtpClient.this.server.getLocalAddress();
            }
        }), 0));
        if (this.connectTimeout >= 0) {
            s.connect(dest, this.connectTimeout);
        } else if (defaultConnectTimeout > 0) {
            s.connect(dest, defaultConnectTimeout);
        } else {
            s.connect(dest);
        }
        if (this.readTimeout >= 0) {
            s.setSoTimeout(this.readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        if (this.useCrypto) {
            try {
                s = this.sslFact.createSocket(s, dest.getHostName(), dest.getPort(), true);
            } catch (Object e) {
                throw new FtpProtocolException("Can't open secure data channel: " + e);
            }
        }
        if (issueCommand(cmd)) {
            return s;
        }
        s.close();
        if (getLastReplyCode() == FtpReplyCode.FILE_UNAVAILABLE) {
            throw new FileNotFoundException(cmd);
        }
        throw new FtpProtocolException(cmd + ":" + getResponseString(), getLastReplyCode());
    }

    private Socket openDataConnection(String cmd) throws FtpProtocolException, IOException {
        ServerSocket portSocket;
        try {
            return openPassiveDataConnection(cmd);
        } catch (FtpProtocolException e) {
            String errmsg = e.getMessage();
            if (!errmsg.startsWith("PASV") && (errmsg.startsWith("EPSV") ^ 1) != 0) {
                throw e;
            } else if (this.proxy == null || this.proxy.type() != Proxy.Type.SOCKS) {
                portSocket = new ServerSocket(0, 1, this.server.getLocalAddress());
                InetAddress myAddress = portSocket.getInetAddress();
                if (myAddress.isAnyLocalAddress()) {
                    myAddress = this.server.getLocalAddress();
                }
                if (!(issueCommand("EPRT |" + (myAddress instanceof Inet6Address ? "2" : "1") + "|" + myAddress.getHostAddress() + "|" + portSocket.getLocalPort() + "|") && (issueCommand(cmd) ^ 1) == 0)) {
                    String portCmd = "PORT ";
                    for (byte b : myAddress.getAddress()) {
                        portCmd = portCmd + (b & 255) + ",";
                    }
                    issueCommandCheck(portCmd + ((portSocket.getLocalPort() >>> 8) & 255) + "," + (portSocket.getLocalPort() & 255));
                    issueCommandCheck(cmd);
                }
                if (this.connectTimeout >= 0) {
                    portSocket.setSoTimeout(this.connectTimeout);
                } else if (defaultConnectTimeout > 0) {
                    portSocket.setSoTimeout(defaultConnectTimeout);
                }
                Socket clientSocket = portSocket.accept();
                if (this.readTimeout >= 0) {
                    clientSocket.setSoTimeout(this.readTimeout);
                } else if (defaultSoTimeout > 0) {
                    clientSocket.setSoTimeout(defaultSoTimeout);
                }
                portSocket.close();
                if (this.useCrypto) {
                    try {
                        clientSocket = this.sslFact.createSocket(clientSocket, this.serverAddr.getHostName(), this.serverAddr.getPort(), true);
                    } catch (Exception ex) {
                        throw new IOException(ex.getLocalizedMessage());
                    }
                }
                return clientSocket;
            } else {
                throw new FtpProtocolException("Passive mode failed");
            }
        } catch (Throwable th) {
            portSocket.close();
        }
    }

    private InputStream createInputStream(InputStream in) {
        if (this.type == TransferType.ASCII) {
            return new TelnetInputStream(in, false);
        }
        return in;
    }

    private OutputStream createOutputStream(OutputStream out) {
        if (this.type == TransferType.ASCII) {
            return new TelnetOutputStream(out, false);
        }
        return out;
    }

    protected FtpClient() {
    }

    public static sun.net.ftp.FtpClient create() {
        return new FtpClient();
    }

    public sun.net.ftp.FtpClient enablePassiveMode(boolean passive) {
        return this;
    }

    public boolean isPassiveModeEnabled() {
        return true;
    }

    public sun.net.ftp.FtpClient setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public sun.net.ftp.FtpClient setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public sun.net.ftp.FtpClient setProxy(Proxy p) {
        this.proxy = p;
        return this;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    private void tryConnect(InetSocketAddress dest, int timeout) throws IOException {
        if (isConnected()) {
            disconnect();
        }
        this.server = doConnect(dest, timeout);
        try {
            this.out = new PrintStream(new BufferedOutputStream(this.server.getOutputStream()), true, encoding);
            this.in = new BufferedInputStream(this.server.getInputStream());
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found");
        }
    }

    private Socket doConnect(InetSocketAddress dest, int timeout) throws IOException {
        Socket s;
        if (this.proxy == null) {
            s = new Socket();
        } else if (this.proxy.type() == Proxy.Type.SOCKS) {
            s = (Socket) AccessController.doPrivileged(new PrivilegedAction<Socket>() {
                public Socket run() {
                    return new Socket(FtpClient.this.proxy);
                }
            });
        } else {
            s = new Socket(Proxy.NO_PROXY);
        }
        if (timeout >= 0) {
            s.connect(dest, timeout);
        } else if (this.connectTimeout >= 0) {
            s.connect(dest, this.connectTimeout);
        } else if (defaultConnectTimeout > 0) {
            s.connect(dest, defaultConnectTimeout);
        } else {
            s.connect(dest);
        }
        if (this.readTimeout >= 0) {
            s.setSoTimeout(this.readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        return s;
    }

    private void disconnect() throws IOException {
        if (isConnected()) {
            this.server.close();
        }
        this.server = null;
        this.in = null;
        this.out = null;
        this.lastTransSize = -1;
        this.lastFileName = null;
        this.restartOffset = 0;
        this.welcomeMsg = null;
        this.lastReplyCode = null;
        this.serverResponse.setSize(0);
    }

    public boolean isConnected() {
        return this.server != null;
    }

    public SocketAddress getServerAddress() {
        return this.server == null ? null : this.server.getRemoteSocketAddress();
    }

    public sun.net.ftp.FtpClient connect(SocketAddress dest) throws FtpProtocolException, IOException {
        return connect(dest, -1);
    }

    public sun.net.ftp.FtpClient connect(SocketAddress dest, int timeout) throws FtpProtocolException, IOException {
        if (dest instanceof InetSocketAddress) {
            this.serverAddr = (InetSocketAddress) dest;
            tryConnect(this.serverAddr, timeout);
            if (readReply()) {
                this.welcomeMsg = getResponseString().substring(4);
                return this;
            }
            throw new FtpProtocolException("Welcome message: " + getResponseString(), this.lastReplyCode);
        }
        throw new IllegalArgumentException("Wrong address type");
    }

    private void tryLogin(String user, char[] password) throws FtpProtocolException, IOException {
        issueCommandCheck("USER " + user);
        if (this.lastReplyCode == FtpReplyCode.NEED_PASSWORD && password != null && password.length > 0) {
            issueCommandCheck("PASS " + String.valueOf(password));
        }
    }

    public sun.net.ftp.FtpClient login(String user, char[] password) throws FtpProtocolException, IOException {
        if (!isConnected()) {
            throw new FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
        } else if (user == null || user.length() == 0) {
            throw new IllegalArgumentException("User name can't be null or empty");
        } else {
            tryLogin(user, password);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < this.serverResponse.size(); i++) {
                String l = (String) this.serverResponse.elementAt(i);
                if (l != null) {
                    if (l.length() >= 4 && l.startsWith("230")) {
                        l = l.substring(4);
                    }
                    sb.append(l);
                }
            }
            this.welcomeMsg = sb.toString();
            this.loggedIn = true;
            return this;
        }
    }

    public sun.net.ftp.FtpClient login(String user, char[] password, String account) throws FtpProtocolException, IOException {
        if (!isConnected()) {
            throw new FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
        } else if (user == null || user.length() == 0) {
            throw new IllegalArgumentException("User name can't be null or empty");
        } else {
            tryLogin(user, password);
            if (this.lastReplyCode == FtpReplyCode.NEED_ACCOUNT) {
                issueCommandCheck("ACCT " + account);
            }
            StringBuffer sb = new StringBuffer();
            if (this.serverResponse != null) {
                for (String l : this.serverResponse) {
                    String l2;
                    if (l2 != null) {
                        if (l2.length() >= 4 && l2.startsWith("230")) {
                            l2 = l2.substring(4);
                        }
                        sb.append(l2);
                    }
                }
            }
            this.welcomeMsg = sb.toString();
            this.loggedIn = true;
            return this;
        }
    }

    public void close() throws IOException {
        if (isConnected()) {
            try {
                issueCommand("QUIT");
            } catch (FtpProtocolException e) {
            }
            this.loggedIn = false;
        }
        disconnect();
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    public sun.net.ftp.FtpClient changeDirectory(String remoteDirectory) throws FtpProtocolException, IOException {
        if (remoteDirectory == null || "".equals(remoteDirectory)) {
            throw new IllegalArgumentException("directory can't be null or empty");
        }
        issueCommandCheck("CWD " + remoteDirectory);
        return this;
    }

    public sun.net.ftp.FtpClient changeToParentDirectory() throws FtpProtocolException, IOException {
        issueCommandCheck("CDUP");
        return this;
    }

    public String getWorkingDirectory() throws FtpProtocolException, IOException {
        issueCommandCheck("PWD");
        String answ = getResponseString();
        if (answ.startsWith("257")) {
            return answ.substring(5, answ.lastIndexOf(34));
        }
        return null;
    }

    public sun.net.ftp.FtpClient setRestartOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset can't be negative");
        }
        this.restartOffset = offset;
        return this;
    }

    public sun.net.ftp.FtpClient getFile(String name, OutputStream local) throws FtpProtocolException, IOException {
        Socket s;
        InputStream remote;
        byte[] buf;
        int l;
        if (this.restartOffset > 0) {
            try {
                s = openDataConnection("REST " + this.restartOffset);
                issueCommandCheck("RETR " + name);
                getTransferSize();
                remote = createInputStream(s.getInputStream());
                buf = new byte[15000];
                while (true) {
                    l = remote.read(buf);
                    if (l < 0) {
                        break;
                    } else if (l > 0) {
                        local.write(buf, 0, l);
                    }
                }
                remote.close();
            } finally {
                this.restartOffset = 0;
            }
        } else {
            s = openDataConnection("RETR " + name);
            getTransferSize();
            remote = createInputStream(s.getInputStream());
            buf = new byte[15000];
            while (true) {
                l = remote.read(buf);
                if (l < 0) {
                    break;
                } else if (l > 0) {
                    local.write(buf, 0, l);
                }
            }
            remote.close();
        }
        return completePending();
    }

    public InputStream getFileStream(String name) throws FtpProtocolException, IOException {
        Socket s;
        if (this.restartOffset > 0) {
            try {
                s = openDataConnection("REST " + this.restartOffset);
                if (s == null) {
                    return null;
                }
                issueCommandCheck("RETR " + name);
                getTransferSize();
                return createInputStream(s.getInputStream());
            } finally {
                this.restartOffset = 0;
            }
        } else {
            s = openDataConnection("RETR " + name);
            if (s == null) {
                return null;
            }
            getTransferSize();
            return createInputStream(s.getInputStream());
        }
    }

    public OutputStream putFileStream(String name, boolean unique) throws FtpProtocolException, IOException {
        Socket s = openDataConnection((unique ? "STOU " : "STOR ") + name);
        if (s == null) {
            return null;
        }
        return new TelnetOutputStream(s.getOutputStream(), this.type == TransferType.BINARY);
    }

    public sun.net.ftp.FtpClient putFile(String name, InputStream local, boolean unique) throws FtpProtocolException, IOException {
        String cmd = unique ? "STOU " : "STOR ";
        if (this.type == TransferType.BINARY) {
            OutputStream remote = createOutputStream(openDataConnection(cmd + name).getOutputStream());
            byte[] buf = new byte[15000];
            while (true) {
                int l = local.read(buf);
                if (l < 0) {
                    break;
                } else if (l > 0) {
                    remote.write(buf, 0, l);
                }
            }
            remote.close();
        }
        return completePending();
    }

    public sun.net.ftp.FtpClient appendFile(String name, InputStream local) throws FtpProtocolException, IOException {
        OutputStream remote = createOutputStream(openDataConnection("APPE " + name).getOutputStream());
        byte[] buf = new byte[15000];
        while (true) {
            int l = local.read(buf);
            if (l < 0) {
                remote.close();
                return completePending();
            } else if (l > 0) {
                remote.write(buf, 0, l);
            }
        }
    }

    public sun.net.ftp.FtpClient rename(String from, String to) throws FtpProtocolException, IOException {
        issueCommandCheck("RNFR " + from);
        issueCommandCheck("RNTO " + to);
        return this;
    }

    public sun.net.ftp.FtpClient deleteFile(String name) throws FtpProtocolException, IOException {
        issueCommandCheck("DELE " + name);
        return this;
    }

    public sun.net.ftp.FtpClient makeDirectory(String name) throws FtpProtocolException, IOException {
        issueCommandCheck("MKD " + name);
        return this;
    }

    public sun.net.ftp.FtpClient removeDirectory(String name) throws FtpProtocolException, IOException {
        issueCommandCheck("RMD " + name);
        return this;
    }

    public sun.net.ftp.FtpClient noop() throws FtpProtocolException, IOException {
        issueCommandCheck("NOOP");
        return this;
    }

    public String getStatus(String name) throws FtpProtocolException, IOException {
        String str;
        if (name == null) {
            str = "STAT";
        } else {
            str = "STAT " + name;
        }
        issueCommandCheck(str);
        Vector<String> resp = getResponseStrings();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < resp.size() - 1; i++) {
            sb.append((String) resp.get(i));
        }
        return sb.toString();
    }

    public List<String> getFeatures() throws FtpProtocolException, IOException {
        ArrayList<String> features = new ArrayList();
        issueCommandCheck("FEAT");
        Vector<String> resp = getResponseStrings();
        for (int i = 1; i < resp.size() - 1; i++) {
            String s = (String) resp.get(i);
            features.add(s.substring(1, s.length() - 1));
        }
        return features;
    }

    public sun.net.ftp.FtpClient abort() throws FtpProtocolException, IOException {
        issueCommandCheck("ABOR");
        return this;
    }

    public sun.net.ftp.FtpClient completePending() throws FtpProtocolException, IOException {
        while (this.replyPending) {
            this.replyPending = false;
            if (!readReply()) {
                throw new FtpProtocolException(getLastResponseString(), this.lastReplyCode);
            }
        }
        return this;
    }

    public sun.net.ftp.FtpClient reInit() throws FtpProtocolException, IOException {
        issueCommandCheck("REIN");
        this.loggedIn = false;
        if (this.useCrypto && (this.server instanceof SSLSocket)) {
            ((SSLSocket) this.server).getSession().invalidate();
            this.server = this.oldSocket;
            this.oldSocket = null;
            try {
                this.out = new PrintStream(new BufferedOutputStream(this.server.getOutputStream()), true, encoding);
                this.in = new BufferedInputStream(this.server.getInputStream());
            } catch (UnsupportedEncodingException e) {
                throw new InternalError(encoding + "encoding not found");
            }
        }
        this.useCrypto = false;
        return this;
    }

    public sun.net.ftp.FtpClient setType(TransferType type) throws FtpProtocolException, IOException {
        String cmd = "NOOP";
        this.type = type;
        if (type == TransferType.ASCII) {
            cmd = "TYPE A";
        }
        if (type == TransferType.BINARY) {
            cmd = "TYPE I";
        }
        if (type == TransferType.EBCDIC) {
            cmd = "TYPE E";
        }
        issueCommandCheck(cmd);
        return this;
    }

    public InputStream list(String path) throws FtpProtocolException, IOException {
        Socket s = openDataConnection(path == null ? "LIST" : "LIST " + path);
        if (s != null) {
            return createInputStream(s.getInputStream());
        }
        return null;
    }

    public InputStream nameList(String path) throws FtpProtocolException, IOException {
        Socket s = openDataConnection("NLST " + path);
        if (s != null) {
            return createInputStream(s.getInputStream());
        }
        return null;
    }

    public long getSize(String path) throws FtpProtocolException, IOException {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("path can't be null or empty");
        }
        issueCommandCheck("SIZE " + path);
        if (this.lastReplyCode != FtpReplyCode.FILE_STATUS) {
            return -1;
        }
        String s = getResponseString();
        return Long.parseLong(s.substring(4, s.length() - 1));
    }

    public Date getLastModified(String path) throws FtpProtocolException, IOException {
        issueCommandCheck("MDTM " + path);
        if (this.lastReplyCode == FtpReplyCode.FILE_STATUS) {
            String s = getResponseString().substring(4);
            Date d = null;
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    d = dateFormat.parse(s);
                } catch (ParseException e) {
                }
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }

    public sun.net.ftp.FtpClient setDirParser(FtpDirParser p) {
        this.parser = p;
        return this;
    }

    public Iterator<FtpDirEntry> listFiles(String path) throws FtpProtocolException, IOException {
        String str;
        Socket s = null;
        if (path == null) {
            try {
                str = "MLSD";
            } catch (FtpProtocolException e) {
            }
        } else {
            str = "MLSD " + path;
        }
        s = openDataConnection(str);
        if (s != null) {
            return new FtpFileIterator(this.mlsxParser, new BufferedReader(new InputStreamReader(s.getInputStream())));
        }
        s = openDataConnection(path == null ? "LIST" : "LIST " + path);
        if (s == null) {
            return null;
        }
        return new FtpFileIterator(this.parser, new BufferedReader(new InputStreamReader(s.getInputStream())));
    }

    private boolean sendSecurityData(byte[] buf) throws IOException, FtpProtocolException {
        return issueCommand("ADAT " + new BASE64Encoder().encode(buf));
    }

    private byte[] getSecurityData() {
        String s = getLastResponseString();
        if (s.substring(4, 9).equalsIgnoreCase("ADAT=")) {
            try {
                return new BASE64Decoder().decodeBuffer(s.substring(9, s.length() - 1));
            } catch (IOException e) {
            }
        }
        return null;
    }

    public sun.net.ftp.FtpClient useKerberos() throws FtpProtocolException, IOException {
        return this;
    }

    public String getWelcomeMsg() {
        return this.welcomeMsg;
    }

    public FtpReplyCode getLastReplyCode() {
        return this.lastReplyCode;
    }

    public String getLastResponseString() {
        StringBuffer sb = new StringBuffer();
        if (this.serverResponse != null) {
            for (String l : this.serverResponse) {
                if (l != null) {
                    sb.append(l);
                }
            }
        }
        return sb.toString();
    }

    public long getLastTransferSize() {
        return this.lastTransSize;
    }

    public String getLastFileName() {
        return this.lastFileName;
    }

    public sun.net.ftp.FtpClient startSecureSession() throws FtpProtocolException, IOException {
        if (isConnected()) {
            if (this.sslFact == null) {
                try {
                    this.sslFact = (SSLSocketFactory) SSLSocketFactory.getDefault();
                } catch (Exception e) {
                    throw new IOException(e.getLocalizedMessage());
                }
            }
            issueCommandCheck("AUTH TLS");
            try {
                Socket s = this.sslFact.createSocket(this.server, this.serverAddr.getHostName(), this.serverAddr.getPort(), true);
                this.oldSocket = this.server;
                this.server = s;
                try {
                    this.out = new PrintStream(new BufferedOutputStream(this.server.getOutputStream()), true, encoding);
                    this.in = new BufferedInputStream(this.server.getInputStream());
                    issueCommandCheck("PBSZ 0");
                    issueCommandCheck("PROT P");
                    this.useCrypto = true;
                    return this;
                } catch (UnsupportedEncodingException e2) {
                    throw new InternalError(encoding + "encoding not found");
                }
            } catch (SSLException ssle) {
                try {
                    disconnect();
                } catch (Exception e3) {
                }
                throw ssle;
            }
        }
        throw new FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
    }

    public sun.net.ftp.FtpClient endSecureSession() throws FtpProtocolException, IOException {
        if (!this.useCrypto) {
            return this;
        }
        issueCommandCheck("CCC");
        issueCommandCheck("PROT C");
        this.useCrypto = false;
        this.server = this.oldSocket;
        this.oldSocket = null;
        try {
            this.out = new PrintStream(new BufferedOutputStream(this.server.getOutputStream()), true, encoding);
            this.in = new BufferedInputStream(this.server.getInputStream());
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found");
        }
    }

    public sun.net.ftp.FtpClient allocate(long size) throws FtpProtocolException, IOException {
        issueCommandCheck("ALLO " + size);
        return this;
    }

    public sun.net.ftp.FtpClient structureMount(String struct) throws FtpProtocolException, IOException {
        issueCommandCheck("SMNT " + struct);
        return this;
    }

    public String getSystem() throws FtpProtocolException, IOException {
        issueCommandCheck("SYST");
        return getResponseString().substring(4);
    }

    public String getHelp(String cmd) throws FtpProtocolException, IOException {
        issueCommandCheck("HELP " + cmd);
        Vector<String> resp = getResponseStrings();
        if (resp.size() == 1) {
            return ((String) resp.get(0)).substring(4);
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < resp.size() - 1; i++) {
            sb.append(((String) resp.get(i)).substring(3));
        }
        return sb.toString();
    }

    public sun.net.ftp.FtpClient siteCmd(String cmd) throws FtpProtocolException, IOException {
        issueCommandCheck("SITE " + cmd);
        return this;
    }
}
