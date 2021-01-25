package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;

/* access modifiers changed from: package-private */
public interface CMSReadable {
    InputStream getInputStream() throws IOException, CMSException;
}
