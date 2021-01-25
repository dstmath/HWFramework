package org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;

public interface CMSProcessable {
    Object getContent();

    void write(OutputStream outputStream) throws IOException, CMSException;
}
