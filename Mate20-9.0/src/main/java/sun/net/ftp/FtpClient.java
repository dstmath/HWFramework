package sun.net.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class FtpClient implements Closeable {
    private static final int FTP_PORT = 21;

    public enum TransferType {
        ASCII,
        BINARY,
        EBCDIC
    }

    public abstract FtpClient abort() throws FtpProtocolException, IOException;

    public abstract FtpClient allocate(long j) throws FtpProtocolException, IOException;

    public abstract FtpClient appendFile(String str, InputStream inputStream) throws FtpProtocolException, IOException;

    public abstract FtpClient changeDirectory(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient changeToParentDirectory() throws FtpProtocolException, IOException;

    public abstract void close() throws IOException;

    public abstract FtpClient completePending() throws FtpProtocolException, IOException;

    public abstract FtpClient connect(SocketAddress socketAddress) throws FtpProtocolException, IOException;

    public abstract FtpClient connect(SocketAddress socketAddress, int i) throws FtpProtocolException, IOException;

    public abstract FtpClient deleteFile(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient enablePassiveMode(boolean z);

    public abstract FtpClient endSecureSession() throws FtpProtocolException, IOException;

    public abstract int getConnectTimeout();

    public abstract List<String> getFeatures() throws FtpProtocolException, IOException;

    public abstract FtpClient getFile(String str, OutputStream outputStream) throws FtpProtocolException, IOException;

    public abstract InputStream getFileStream(String str) throws FtpProtocolException, IOException;

    public abstract String getHelp(String str) throws FtpProtocolException, IOException;

    public abstract String getLastFileName();

    public abstract Date getLastModified(String str) throws FtpProtocolException, IOException;

    public abstract FtpReplyCode getLastReplyCode();

    public abstract String getLastResponseString();

    public abstract long getLastTransferSize();

    public abstract Proxy getProxy();

    public abstract int getReadTimeout();

    public abstract SocketAddress getServerAddress();

    public abstract long getSize(String str) throws FtpProtocolException, IOException;

    public abstract String getStatus(String str) throws FtpProtocolException, IOException;

    public abstract String getSystem() throws FtpProtocolException, IOException;

    public abstract String getWelcomeMsg();

    public abstract String getWorkingDirectory() throws FtpProtocolException, IOException;

    public abstract boolean isConnected();

    public abstract boolean isLoggedIn();

    public abstract boolean isPassiveModeEnabled();

    public abstract InputStream list(String str) throws FtpProtocolException, IOException;

    public abstract Iterator<FtpDirEntry> listFiles(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient login(String str, char[] cArr) throws FtpProtocolException, IOException;

    public abstract FtpClient login(String str, char[] cArr, String str2) throws FtpProtocolException, IOException;

    public abstract FtpClient makeDirectory(String str) throws FtpProtocolException, IOException;

    public abstract InputStream nameList(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient noop() throws FtpProtocolException, IOException;

    public abstract FtpClient putFile(String str, InputStream inputStream, boolean z) throws FtpProtocolException, IOException;

    public abstract OutputStream putFileStream(String str, boolean z) throws FtpProtocolException, IOException;

    public abstract FtpClient reInit() throws FtpProtocolException, IOException;

    public abstract FtpClient removeDirectory(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient rename(String str, String str2) throws FtpProtocolException, IOException;

    public abstract FtpClient setConnectTimeout(int i);

    public abstract FtpClient setDirParser(FtpDirParser ftpDirParser);

    public abstract FtpClient setProxy(Proxy proxy);

    public abstract FtpClient setReadTimeout(int i);

    public abstract FtpClient setRestartOffset(long j);

    public abstract FtpClient setType(TransferType transferType) throws FtpProtocolException, IOException;

    public abstract FtpClient siteCmd(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient startSecureSession() throws FtpProtocolException, IOException;

    public abstract FtpClient structureMount(String str) throws FtpProtocolException, IOException;

    public abstract FtpClient useKerberos() throws FtpProtocolException, IOException;

    public static final int defaultPort() {
        return FTP_PORT;
    }

    protected FtpClient() {
    }

    public static FtpClient create() {
        return FtpClientProvider.provider().createFtpClient();
    }

    public static FtpClient create(InetSocketAddress dest) throws FtpProtocolException, IOException {
        FtpClient client = create();
        if (dest != null) {
            client.connect(dest);
        }
        return client;
    }

    public static FtpClient create(String dest) throws FtpProtocolException, IOException {
        return create(new InetSocketAddress(dest, (int) FTP_PORT));
    }

    public OutputStream putFileStream(String name) throws FtpProtocolException, IOException {
        return putFileStream(name, false);
    }

    public FtpClient putFile(String name, InputStream local) throws FtpProtocolException, IOException {
        return putFile(name, local, false);
    }

    public FtpClient setBinaryType() throws FtpProtocolException, IOException {
        setType(TransferType.BINARY);
        return this;
    }

    public FtpClient setAsciiType() throws FtpProtocolException, IOException {
        setType(TransferType.ASCII);
        return this;
    }
}
