package android_maps_conflict_avoidance.com.google.common.io;

import java.io.IOException;

public interface HttpConnectionFactory extends ConnectionFactory {
    GoogleHttpConnection createConnection(String str, boolean z) throws IOException, SecurityException;
}
