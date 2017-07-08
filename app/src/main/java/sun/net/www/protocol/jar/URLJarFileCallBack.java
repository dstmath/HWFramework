package sun.net.www.protocol.jar;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

public interface URLJarFileCallBack {
    JarFile retrieve(URL url) throws IOException;
}
