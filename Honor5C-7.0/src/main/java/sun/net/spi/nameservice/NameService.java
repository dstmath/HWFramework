package sun.net.spi.nameservice;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface NameService {
    String getHostByAddr(byte[] bArr) throws UnknownHostException;

    InetAddress[] lookupAllHostAddr(String str, int i) throws UnknownHostException;
}
