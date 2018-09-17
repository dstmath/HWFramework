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
import java.io.PrintStream;
import java.util.Vector;

public final class StaticUtil {
    private static boolean IS_REGISTER_OUT_OF_MEMORY_HANDLER;
    private static byte[] emergencyMemory;
    private static final Vector outOfMemoryHandlers = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.StaticUtil.<clinit>():void");
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
            case OverlayItem.ITEM_STATE_PRESSED_MASK /*1*/:
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
        String str;
        emergencyMemory = null;
        PrintStream printStream = System.err;
        if (warning) {
            str = "LowOnMemory";
        } else {
            str = "OutOfMemory";
        }
        printStream.println(str);
        for (int i = 0; i < outOfMemoryHandlers.size(); i++) {
            ((OutOfMemoryHandler) outOfMemoryHandlers.elementAt(i)).handleOutOfMemory(warning);
        }
    }
}
