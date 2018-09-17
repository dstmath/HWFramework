package sun.net.ftp;

public interface FtpDirParser {
    FtpDirEntry parseLine(String str);
}
