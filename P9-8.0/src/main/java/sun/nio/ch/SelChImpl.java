package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.Channel;

public interface SelChImpl extends Channel {
    FileDescriptor getFD();

    int getFDVal();

    void kill() throws IOException;

    void translateAndSetInterestOps(int i, SelectionKeyImpl selectionKeyImpl);

    boolean translateAndSetReadyOps(int i, SelectionKeyImpl selectionKeyImpl);

    boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl selectionKeyImpl);

    int validOps();
}
