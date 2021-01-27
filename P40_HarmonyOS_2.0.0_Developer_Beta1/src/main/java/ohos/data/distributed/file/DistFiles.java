package ohos.data.distributed.file;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DistFiles {
    public static boolean isSupported() {
        return true;
    }

    public static DistFile createFile(String str) throws IOException {
        DistFile distFile = new DistFile(str);
        if (distFile.createNewFile()) {
            return distFile;
        }
        return null;
    }

    public static DistLinkFile createLinkFile(String str, String str2) throws IOException {
        DistLinkFile distLinkFile = new DistLinkFile(str, str2);
        if (distLinkFile.createNewFile()) {
            return distLinkFile;
        }
        return null;
    }

    public static DistAgentFile createAgentFile(String str, String str2) throws IOException {
        return createAgentFile(str, str2, "0");
    }

    public static DistAgentFile createAgentFile(String str, String str2, String str3) throws IOException {
        if (invalidPath(str) || invalidPath(str2)) {
            throw new IOException("Invalid arguments");
        }
        DistAgentFile distAgentFile = new DistAgentFile(str, str2, str3);
        if (distAgentFile.createNewFile()) {
            return distAgentFile;
        }
        return null;
    }

    public static boolean delete(String str) {
        if (invalidPath(str)) {
            return false;
        }
        return delete(new DistFile(str));
    }

    public static boolean delete(DistFile distFile) {
        if (distFile == null) {
            return false;
        }
        return distFile.delete();
    }

    public static boolean copy(DistFile distFile, DistFile distFile2) throws IOException {
        if (distFile != null) {
            return distFile.copyTo(distFile2);
        }
        throw new FileNotFoundException("Invalid arguments");
    }

    public static boolean move(DistFile distFile, DistFile distFile2) throws IOException {
        if (distFile != null) {
            return distFile.copyTo(distFile2) && distFile.delete();
        }
        throw new FileNotFoundException("Invalid arguments");
    }

    private static boolean invalidPath(String str) {
        return str == null || str.isEmpty();
    }
}
