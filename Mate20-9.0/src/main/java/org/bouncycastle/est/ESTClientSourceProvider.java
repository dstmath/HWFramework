package org.bouncycastle.est;

import java.io.IOException;

public interface ESTClientSourceProvider {
    Source makeSource(String str, int i) throws IOException;
}
