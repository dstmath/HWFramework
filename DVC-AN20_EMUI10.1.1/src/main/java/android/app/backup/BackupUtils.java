package android.app.backup;

import android.app.backup.FullBackup;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class BackupUtils {
    private BackupUtils() {
    }

    public static boolean isFileSpecifiedInPathList(File file, Collection<FullBackup.BackupScheme.PathWithRequiredFlags> canonicalPathList) throws IOException {
        for (FullBackup.BackupScheme.PathWithRequiredFlags canonical : canonicalPathList) {
            String canonicalPath = canonical.getPath();
            File fileFromList = new File(canonicalPath);
            if (fileFromList.isDirectory()) {
                if (file.isDirectory()) {
                    if (file.equals(fileFromList)) {
                        return true;
                    }
                } else if (file.toPath().startsWith(canonicalPath)) {
                    return true;
                }
            } else if (file.equals(fileFromList)) {
                return true;
            }
        }
        return false;
    }
}
