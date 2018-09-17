package android.content.res;

import java.io.File;
import java.io.IOException;

public class ObbScanner {
    private static native void getObbInfo_native(String str, ObbInfo obbInfo) throws IOException;

    private ObbScanner() {
    }

    public static ObbInfo getObbInfo(String filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("file path cannot be null");
        }
        File obbFile = new File(filePath);
        if (obbFile.exists()) {
            String canonicalFilePath = obbFile.getCanonicalPath();
            ObbInfo obbInfo = new ObbInfo();
            obbInfo.filename = canonicalFilePath;
            getObbInfo_native(canonicalFilePath, obbInfo);
            return obbInfo;
        }
        throw new IllegalArgumentException("OBB file does not exist: " + filePath);
    }
}
