package tmsdkobf;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore.Files;
import java.io.File;
import java.io.IOException;

@TargetApi(11)
public class rf {
    private final ContentResolver Pf;
    private final Uri Pg = Files.getContentUri("external");
    private final File file;

    public rf(ContentResolver contentResolver, File file) {
        this.file = file;
        this.Pf = contentResolver;
    }

    public boolean delete() throws IOException {
        boolean z = false;
        if (!this.file.exists() || this.file.isDirectory()) {
            return true;
        }
        if (this.Pf == null) {
            return false;
        }
        try {
            String[] strArr = new String[]{this.file.getAbsolutePath()};
            this.Pf.delete(this.Pg, "_data=?", strArr);
            if (this.file.exists()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("_data", this.file.getAbsolutePath());
                Uri insert = this.Pf.insert(this.Pg, contentValues);
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("media_type", Integer.valueOf(4));
                this.Pf.update(insert, contentValues2, null, null);
                this.Pf.delete(insert, null, null);
            }
        } catch (Throwable th) {
        }
        if (!this.file.exists()) {
            z = true;
        }
        return z;
    }
}
