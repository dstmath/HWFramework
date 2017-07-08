package android.drm;

import android.os.Process;
import android.os.UserManager;
import java.util.HashMap;

public class DrmInfoEvent extends DrmEvent {
    public static final int TYPE_ACCOUNT_ALREADY_REGISTERED = 5;
    public static final int TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT = 1;
    public static final int TYPE_REMOVE_RIGHTS = 2;
    public static final int TYPE_RIGHTS_INSTALLED = 3;
    public static final int TYPE_RIGHTS_REMOVED = 6;
    public static final int TYPE_WAIT_FOR_RIGHTS = 4;

    public DrmInfoEvent(int uniqueId, int type, String message) {
        super(uniqueId, type, message);
        checkTypeValidity(type);
    }

    public DrmInfoEvent(int uniqueId, int type, String message, HashMap<String, Object> attributes) {
        super(uniqueId, type, message, attributes);
        checkTypeValidity(type);
    }

    private void checkTypeValidity(int type) {
        if ((type < TYPE_ALREADY_REGISTERED_BY_ANOTHER_ACCOUNT || type > TYPE_RIGHTS_REMOVED) && type != UserManager.MKDIR_FOR_USER_TRANSACTION && type != Process.BLUETOOTH_UID) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
