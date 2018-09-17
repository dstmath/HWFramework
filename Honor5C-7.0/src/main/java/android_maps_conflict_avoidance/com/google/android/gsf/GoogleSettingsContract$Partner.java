package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public final class GoogleSettingsContract$Partner extends GoogleSettingsContract$NameValueTable {
    public static final Uri CONTENT_URI = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.android.gsf.GoogleSettingsContract$Partner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.android.gsf.GoogleSettingsContract$Partner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.android.gsf.GoogleSettingsContract$Partner.<clinit>():void");
    }

    public static String getString(ContentResolver resolver, String name) {
        String str = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = resolver;
            cursor = contentResolver.query(CONTENT_URI, new String[]{"value"}, "name=?", new String[]{name}, null);
            if (cursor != null && cursor.moveToNext()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.e("GoogleSettings", "Can't get key " + name + " from " + CONTENT_URI, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static String getString(ContentResolver resolver, String name, String defaultValue) {
        String value = getString(resolver, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
