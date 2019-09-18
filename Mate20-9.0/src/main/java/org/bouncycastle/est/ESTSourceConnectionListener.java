package org.bouncycastle.est;

import java.io.IOException;

public interface ESTSourceConnectionListener<T, I> {
    ESTRequest onConnection(Source<T> source, ESTRequest eSTRequest) throws IOException;
}
