package sun.nio.ch;

import sun.misc.Cleaner;

public interface DirectBuffer {
    long address();

    Object attachment();

    Cleaner cleaner();
}
