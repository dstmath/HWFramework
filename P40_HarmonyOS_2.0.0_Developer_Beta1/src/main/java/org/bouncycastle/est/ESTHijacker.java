package org.bouncycastle.est;

import java.io.IOException;

public interface ESTHijacker {
    ESTResponse hijack(ESTRequest eSTRequest, Source source) throws IOException;
}
