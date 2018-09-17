package java.nio.channels;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.util.Set;

public interface NetworkChannel extends Channel {
    NetworkChannel bind(SocketAddress socketAddress) throws IOException;

    SocketAddress getLocalAddress() throws IOException;

    <T> T getOption(SocketOption<T> socketOption) throws IOException;

    <T> NetworkChannel setOption(SocketOption<T> socketOption, T t) throws IOException;

    Set<SocketOption<?>> supportedOptions();
}
