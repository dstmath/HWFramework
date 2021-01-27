package org.bouncycastle.est;

import java.io.IOException;

public interface ESTClient {
    ESTResponse doRequest(ESTRequest eSTRequest) throws IOException;
}
