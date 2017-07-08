package tmsdkobf;

import android.os.Environment;
import java.io.File;
import java.io.IOException;

/* compiled from: Unknown */
public class lt {
    public static int dD() {
        return ms.eW() ? dE() ? 0 : 2 : 1;
    }

    public static boolean dE() {
        String str = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File file = new File(str);
        if (!file.isDirectory() && !file.mkdirs()) {
            return false;
        }
        file = new File(str, ".probe");
        try {
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return false;
            }
            file.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
