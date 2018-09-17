package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

public final class StaticUtil {
    private static boolean IS_REGISTER_OUT_OF_MEMORY_HANDLER = true;
    private static byte[] emergencyMemory;
    private static final Vector outOfMemoryHandlers = new Vector();

    static {
        allocateEmergencyMemory();
    }

    private StaticUtil() {
    }

    private static void allocateEmergencyMemory() {
        if (emergencyMemory != null) {
            System.gc();
            try {
                emergencyMemory = new byte[4096];
            } catch (OutOfMemoryError e) {
            }
        }
    }

    private static PersistentStore getPersistentStore() {
        return Config.getInstance().getPersistentStore();
    }

    public static void savePreferenceAsString(String preference, String value) {
        savePreferenceAsObject(preference, value);
    }

    private static void savePreferenceAsObject(String preference, Object object) {
        PersistentStore store = Config.getInstance().getPersistentStore();
        if (object != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutput dataOut = new DataOutputStream(baos);
            try {
                if (object instanceof Boolean) {
                    dataOut.writeBoolean(((Boolean) object).booleanValue());
                } else if (object instanceof String) {
                    dataOut.writeUTF((String) object);
                } else if (object instanceof Integer) {
                    dataOut.writeInt(((Integer) object).intValue());
                } else if (object instanceof Long) {
                    dataOut.writeLong(((Long) object).longValue());
                } else {
                    throw new IllegalArgumentException("Bad type: " + object.getClass() + " for " + preference);
                }
                store.setPreference(preference, baos.toByteArray());
            } catch (IOException e) {
                Log.logQuietThrowable("Writing: " + preference, e);
            }
            return;
        }
        store.setPreference(preference, null);
    }

    public static String readPreferenceAsString(String preference) {
        return (String) readPreferenceAsObject(preference, 3);
    }

    private static Object readPreferenceAsObject(String preference, int type) {
        DataInput input = readPreferenceAsDataInput(preference);
        if (input == null) {
            return null;
        }
        switch (type) {
            case LayoutParams.MODE_MAP /*0*/:
                return new Boolean(input.readBoolean());
            case 1:
                return new Integer(input.readInt());
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                return new Long(input.readLong());
            case LayoutParams.LEFT /*3*/:
                return input.readUTF();
            default:
                try {
                    throw new RuntimeException("Bad class: " + type + " for " + preference);
                } catch (IOException e) {
                    return null;
                }
        }
    }

    public static DataInput readPreferenceAsDataInput(String preference) {
        byte[] data = getPersistentStore().readPreference(preference);
        if (data != null) {
            return new DataInputStream(new ByteArrayInputStream(data));
        }
        return null;
    }

    public static void registerOutOfMemoryHandler(OutOfMemoryHandler handler) {
        if (IS_REGISTER_OUT_OF_MEMORY_HANDLER) {
            outOfMemoryHandlers.addElement(handler);
        }
    }

    public static void removeOutOfMemoryHandler(OutOfMemoryHandler handler) {
        outOfMemoryHandlers.removeElement(handler);
    }

    public static void handleOutOfMemory() {
        handleOutOfMemory(false);
    }

    private static void handleOutOfMemory(boolean warning) {
        emergencyMemory = null;
        System.err.println(!warning ? "OutOfMemory" : "LowOnMemory");
        for (int i = 0; i < outOfMemoryHandlers.size(); i++) {
            ((OutOfMemoryHandler) outOfMemoryHandlers.elementAt(i)).handleOutOfMemory(warning);
        }
    }
}
