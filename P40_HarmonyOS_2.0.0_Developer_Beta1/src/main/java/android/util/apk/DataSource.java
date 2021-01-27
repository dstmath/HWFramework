package android.util.apk;

import java.io.IOException;
import java.security.DigestException;

/* access modifiers changed from: package-private */
public interface DataSource {
    void feedIntoDataDigester(DataDigester dataDigester, long j, int i) throws IOException, DigestException;

    long size();
}
