package com.android.server.voiceinteraction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.soundtrigger.SoundTrigger;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_SOUND_MODEL = "CREATE TABLE sound_model(model_uuid TEXT,vendor_uuid TEXT,keyphrase_id INTEGER,type INTEGER,data BLOB,recognition_modes INTEGER,locale TEXT,hint_text TEXT,users TEXT,PRIMARY KEY (keyphrase_id,locale,users))";
    static final boolean DBG = false;
    private static final String NAME = "sound_model.db";
    static final String TAG = "SoundModelDBHelper";
    private static final int VERSION = 6;

    public interface SoundModelContract {
        public static final String KEY_DATA = "data";
        public static final String KEY_HINT_TEXT = "hint_text";
        public static final String KEY_KEYPHRASE_ID = "keyphrase_id";
        public static final String KEY_LOCALE = "locale";
        public static final String KEY_MODEL_UUID = "model_uuid";
        public static final String KEY_RECOGNITION_MODES = "recognition_modes";
        public static final String KEY_TYPE = "type";
        public static final String KEY_USERS = "users";
        public static final String KEY_VENDOR_UUID = "vendor_uuid";
        public static final String TABLE = "sound_model";
    }

    private static class SoundModelRecord {
        public final byte[] data;
        public final String hintText;
        public final int keyphraseId;
        public final String locale;
        public final String modelUuid;
        public final int recognitionModes;
        public final int type;
        public final String users;
        public final String vendorUuid;

        public SoundModelRecord(int version, Cursor c) {
            this.modelUuid = c.getString(c.getColumnIndex("model_uuid"));
            if (version >= 5) {
                this.vendorUuid = c.getString(c.getColumnIndex("vendor_uuid"));
            } else {
                this.vendorUuid = null;
            }
            this.keyphraseId = c.getInt(c.getColumnIndex(SoundModelContract.KEY_KEYPHRASE_ID));
            this.type = c.getInt(c.getColumnIndex(SoundModelContract.KEY_TYPE));
            this.data = c.getBlob(c.getColumnIndex("data"));
            this.recognitionModes = c.getInt(c.getColumnIndex(SoundModelContract.KEY_RECOGNITION_MODES));
            this.locale = c.getString(c.getColumnIndex(SoundModelContract.KEY_LOCALE));
            this.hintText = c.getString(c.getColumnIndex(SoundModelContract.KEY_HINT_TEXT));
            this.users = c.getString(c.getColumnIndex(SoundModelContract.KEY_USERS));
        }

        private boolean V6PrimaryKeyMatches(SoundModelRecord record) {
            return this.keyphraseId == record.keyphraseId && stringComparisonHelper(this.locale, record.locale) && stringComparisonHelper(this.users, record.users);
        }

        public boolean ifViolatesV6PrimaryKeyIsFirstOfAnyDuplicates(List<SoundModelRecord> records) {
            Iterator<SoundModelRecord> it = records.iterator();
            while (true) {
                boolean z = false;
                if (it.hasNext()) {
                    SoundModelRecord record = it.next();
                    if (this != record && V6PrimaryKeyMatches(record) && !Arrays.equals(this.data, record.data)) {
                        return false;
                    }
                } else {
                    for (SoundModelRecord record2 : records) {
                        if (V6PrimaryKeyMatches(record2)) {
                            if (this == record2) {
                                z = true;
                            }
                            return z;
                        }
                    }
                    return true;
                }
            }
        }

        public long writeToDatabase(int version, SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            values.put("model_uuid", this.modelUuid);
            if (version >= 5) {
                values.put("vendor_uuid", this.vendorUuid);
            }
            values.put(SoundModelContract.KEY_KEYPHRASE_ID, Integer.valueOf(this.keyphraseId));
            values.put(SoundModelContract.KEY_TYPE, Integer.valueOf(this.type));
            values.put("data", this.data);
            values.put(SoundModelContract.KEY_RECOGNITION_MODES, Integer.valueOf(this.recognitionModes));
            values.put(SoundModelContract.KEY_LOCALE, this.locale);
            values.put(SoundModelContract.KEY_HINT_TEXT, this.hintText);
            values.put(SoundModelContract.KEY_USERS, this.users);
            return db.insertWithOnConflict(SoundModelContract.TABLE, null, values, 5);
        }

        private static boolean stringComparisonHelper(String a, String b) {
            if (a != null) {
                return a.equals(b);
            }
            return a == b;
        }
    }

    public DatabaseHelper(Context context) {
        super(context, NAME, null, 6);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SOUND_MODEL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS sound_model");
            onCreate(db);
        } else if (oldVersion == 4) {
            Slog.d(TAG, "Adding vendor UUID column");
            db.execSQL("ALTER TABLE sound_model ADD COLUMN vendor_uuid TEXT");
            oldVersion++;
        }
        if (oldVersion == 5) {
            Cursor c = db.rawQuery("SELECT * FROM sound_model", null);
            List<SoundModelRecord> old_records = new ArrayList<>();
            try {
                if (c.moveToFirst()) {
                    do {
                        old_records.add(new SoundModelRecord(5, c));
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                Slog.e(TAG, "Failed to extract V5 record", e);
            } catch (Throwable th) {
                c.close();
                throw th;
            }
            c.close();
            db.execSQL("DROP TABLE IF EXISTS sound_model");
            onCreate(db);
            for (SoundModelRecord record : old_records) {
                if (record.ifViolatesV6PrimaryKeyIsFirstOfAnyDuplicates(old_records)) {
                    try {
                        long return_value = record.writeToDatabase(6, db);
                        if (return_value == -1) {
                            Slog.e(TAG, "Database write failed " + record.modelUuid + ": " + return_value);
                        }
                    } catch (Exception e2) {
                        Slog.e(TAG, "Failed to update V6 record " + record.modelUuid, e2);
                    }
                }
            }
            int oldVersion2 = oldVersion + 1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a6, code lost:
        return false;
     */
    public boolean updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel soundModel) {
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("model_uuid", soundModel.uuid.toString());
            if (soundModel.vendorUuid != null) {
                values.put("vendor_uuid", soundModel.vendorUuid.toString());
            }
            boolean z = false;
            values.put(SoundModelContract.KEY_TYPE, 0);
            values.put("data", soundModel.data);
            if (soundModel.keyphrases != null && soundModel.keyphrases.length == 1) {
                values.put(SoundModelContract.KEY_KEYPHRASE_ID, Integer.valueOf(soundModel.keyphrases[0].id));
                values.put(SoundModelContract.KEY_RECOGNITION_MODES, Integer.valueOf(soundModel.keyphrases[0].recognitionModes));
                values.put(SoundModelContract.KEY_USERS, getCommaSeparatedString(soundModel.keyphrases[0].users));
                values.put(SoundModelContract.KEY_LOCALE, soundModel.keyphrases[0].locale);
                values.put(SoundModelContract.KEY_HINT_TEXT, soundModel.keyphrases[0].text);
                try {
                    if (db.insertWithOnConflict(SoundModelContract.TABLE, null, values, 5) != -1) {
                        z = true;
                    }
                    return z;
                } finally {
                    db.close();
                }
            }
        }
    }

    public boolean deleteKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        String bcp47Locale2 = Locale.forLanguageTag(bcp47Locale).toLanguageTag();
        synchronized (this) {
            SoundTrigger.KeyphraseSoundModel soundModel = getKeyphraseSoundModel(keyphraseId, userHandle, bcp47Locale2);
            boolean z = false;
            if (soundModel == null) {
                return false;
            }
            SQLiteDatabase db = getWritableDatabase();
            try {
                if (db.delete(SoundModelContract.TABLE, "model_uuid='" + soundModel.uuid.toString() + "'", null) != 0) {
                    z = true;
                }
                return z;
            } finally {
                db.close();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0106, code lost:
        r23 = r0;
        r1 = r4;
        r4 = r2;
        r2 = r8;
        r3 = new android.hardware.soundtrigger.SoundTrigger.Keyphrase(r4, r5, r6, r7, r22);
        r0 = new android.hardware.soundtrigger.SoundTrigger.Keyphrase[]{r3};
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x011c, code lost:
        if (r2 == null) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x011e, code lost:
        r3 = java.util.UUID.fromString(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0123, code lost:
        r4 = new android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel(java.util.UUID.fromString(r13), r3, r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        r11.close();
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0134, code lost:
        return r4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0101 A[Catch:{ all -> 0x0148, all -> 0x0159 }, LOOP:0: B:8:0x0046->B:47:0x0101, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0100 A[SYNTHETIC] */
    public SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        String bcp47Locale2;
        int[] users;
        boolean isAvailableForCurrentUser;
        String bcp47Locale3 = Locale.forLanguageTag(bcp47Locale).toLanguageTag();
        synchronized (this) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT  * FROM sound_model WHERE keyphrase_id= '");
                int i = keyphraseId;
                sb.append(i);
                sb.append("' AND ");
                sb.append(SoundModelContract.KEY_LOCALE);
                sb.append("='");
                sb.append(bcp47Locale3);
                sb.append("'");
                String selectQuery = sb.toString();
                SQLiteDatabase db = getReadableDatabase();
                Cursor c = db.rawQuery(selectQuery, null);
                try {
                    if (c.moveToFirst()) {
                        while (true) {
                            if (c.getInt(c.getColumnIndex(SoundModelContract.KEY_TYPE)) == 0) {
                                String modelUuid = c.getString(c.getColumnIndex("model_uuid"));
                                if (modelUuid == null) {
                                    try {
                                        Slog.w(TAG, "Ignoring SoundModel since it doesn't specify an ID");
                                    } catch (Throwable th) {
                                        th = th;
                                        String str = bcp47Locale3;
                                    }
                                } else {
                                    String vendorUuidString = null;
                                    int vendorUuidColumn = c.getColumnIndex("vendor_uuid");
                                    if (vendorUuidColumn != -1) {
                                        vendorUuidString = c.getString(vendorUuidColumn);
                                    }
                                    String vendorUuidString2 = vendorUuidString;
                                    byte[] data = c.getBlob(c.getColumnIndex("data"));
                                    int recognitionModes = c.getInt(c.getColumnIndex(SoundModelContract.KEY_RECOGNITION_MODES));
                                    int[] users2 = getArrayForCommaSeparatedString(c.getString(c.getColumnIndex(SoundModelContract.KEY_USERS)));
                                    String modelLocale = c.getString(c.getColumnIndex(SoundModelContract.KEY_LOCALE));
                                    String text = c.getString(c.getColumnIndex(SoundModelContract.KEY_HINT_TEXT));
                                    if (users2 == null) {
                                        bcp47Locale2 = bcp47Locale3;
                                        try {
                                            Slog.w(TAG, "Ignoring SoundModel since it doesn't specify users");
                                            int i2 = userHandle;
                                            if (!c.moveToNext()) {
                                                break;
                                            }
                                            bcp47Locale3 = bcp47Locale2;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            throw th;
                                        }
                                    } else {
                                        bcp47Locale2 = bcp47Locale3;
                                        int length = users2.length;
                                        int i3 = 0;
                                        while (true) {
                                            if (i3 >= length) {
                                                int i4 = userHandle;
                                                users = users2;
                                                isAvailableForCurrentUser = false;
                                                break;
                                            }
                                            int i5 = length;
                                            users = users2;
                                            if (userHandle == users2[i3]) {
                                                isAvailableForCurrentUser = true;
                                                break;
                                            }
                                            i3++;
                                            length = i5;
                                            users2 = users;
                                        }
                                        if (isAvailableForCurrentUser) {
                                            break;
                                        }
                                        if (!c.moveToNext()) {
                                        }
                                    }
                                }
                            }
                            bcp47Locale2 = bcp47Locale3;
                            int i22 = userHandle;
                            if (!c.moveToNext()) {
                            }
                        }
                    }
                    Slog.w(TAG, "No SoundModel available for the given keyphrase");
                    c.close();
                    db.close();
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    String str2 = bcp47Locale3;
                    c.close();
                    db.close();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                String str3 = bcp47Locale3;
                throw th;
            }
        }
    }

    private static String getCommaSeparatedString(int[] users) {
        if (users == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < users.length; i++) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(users[i]);
        }
        return sb.toString();
    }

    private static int[] getArrayForCommaSeparatedString(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        String[] usersStr = text.split(",");
        int[] users = new int[usersStr.length];
        for (int i = 0; i < usersStr.length; i++) {
            users[i] = Integer.parseInt(usersStr[i]);
        }
        return users;
    }
}
