package android.os.strictmode;

import android.net.Uri;

public final class ContentUriWithoutPermissionViolation extends Violation {
    public ContentUriWithoutPermissionViolation(Uri uri, String location) {
        super(uri + " exposed beyond app through " + location + " without permission grant flags; did you forget FLAG_GRANT_READ_URI_PERMISSION?");
    }
}
