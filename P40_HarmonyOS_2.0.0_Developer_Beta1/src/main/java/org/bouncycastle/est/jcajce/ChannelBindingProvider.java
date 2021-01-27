package org.bouncycastle.est.jcajce;

import java.net.Socket;

public interface ChannelBindingProvider {
    boolean canAccessChannelBinding(Socket socket);

    byte[] getChannelBinding(Socket socket, String str);
}
