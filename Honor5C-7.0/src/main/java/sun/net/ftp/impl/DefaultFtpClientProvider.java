package sun.net.ftp.impl;

import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpClientProvider;

public class DefaultFtpClientProvider extends FtpClientProvider {
    public FtpClient createFtpClient() {
        return FtpClient.create();
    }
}
