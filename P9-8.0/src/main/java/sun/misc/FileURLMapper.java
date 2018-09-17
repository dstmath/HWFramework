package sun.misc;

import java.io.File;
import java.net.URL;
import sun.net.www.ParseUtil;

public class FileURLMapper {
    String path;
    URL url;

    public FileURLMapper(URL url) {
        this.url = url;
    }

    public String getPath() {
        if (this.path != null) {
            return this.path;
        }
        String host = this.url.getHost();
        if (host == null || "".equals(host) || "localhost".equalsIgnoreCase(host)) {
            this.path = this.url.getFile();
            this.path = ParseUtil.decode(this.path);
        }
        return this.path;
    }

    public boolean exists() {
        String s = getPath();
        if (s == null) {
            return false;
        }
        return new File(s).exists();
    }
}
