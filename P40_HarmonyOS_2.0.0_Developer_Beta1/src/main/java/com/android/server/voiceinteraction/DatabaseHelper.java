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

    public DatabaseHelper(Context context) {
        super(context, NAME, (SQLiteDatabase.CursorFactory) null, 6);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SOUND_MODEL);
    }

    /* JADX INFO: finally extract failed */
    @Override // android.database.sqlite.SQLiteOpenHelper
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
                        try {
                            old_records.add(new SoundModelRecord(5, c));
                        } catch (Exception e) {
                            Slog.e(TAG, "Failed to extract V5 record", e);
                        }
                    } while (c.moveToNext());
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
            } catch (Throwable th) {
                c.close();
                throw th;
            }
        }
    }

    public boolean updateKeyphraseSoundModel(SoundTrigger.KeyphraseSoundModel soundModel) {
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("model_uuid", soundModel.uuid.toString());
            if (soundModel.vendorUuid != null) {
                values.put("vendor_uuid", soundModel.vendorUuid.toString());
            }
            boolean z = false;
            values.put(SoundModelContract.KEY_TYPE, (Integer) 0);
            values.put("data", soundModel.data);
            if (soundModel.keyphrases == null || soundModel.keyphrases.length != 1) {
                return false;
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0110, code lost:
        r2 = new android.hardware.soundtrigger.SoundTrigger.Keyphrase[]{new android.hardware.soundtrigger.SoundTrigger.Keyphrase(r22, r5, r6, r7, r3)};
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0126, code lost:
        if (r15 == null) goto L_0x012d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0128, code lost:
        r3 = java.util.UUID.fromString(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x012d, code lost:
        r4 = new android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel(java.util.UUID.fromString(r3), r3, r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r3.close();
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x013e, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0169, code lost:
        r0 = th;
     */
    public SoundTrigger.KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        Throwable th;
        String bcp47Locale2;
        String vendorUuidString;
        boolean isAvailableForCurrentUser;
        String bcp47Locale3 = Locale.forLanguageTag(bcp47Locale).toLanguageTag();
        synchronized (this) {
            try {
                SQLiteDatabase db = getReadableDatabase();
                Cursor c = db.rawQuery("SELECT  * FROM sound_model WHERE keyphrase_id= '" + keyphraseId + "' AND " + SoundModelContract.KEY_LOCALE + "='" + bcp47Locale3 + "'", null);
                try {
                    if (c.moveToFirst()) {
                        while (true) {
                            if (c.getInt(c.getColumnIndex(SoundModelContract.KEY_TYPE)) == 0) {
                                String modelUuid = c.getString(c.getColumnIndex("model_uuid"));
                                if (modelUuid != null) {
                                    int vendorUuidColumn = c.getColumnIndex("vendor_uuid");
                                    if (vendorUuidColumn != -1) {
                                        vendorUuidString = c.getString(vendorUuidColumn);
                                    } else {
                                        vendorUuidString = null;
                                    }
                                    byte[] data = c.getBlob(c.getColumnIndex("data"));
                                    int recognitionModes = c.getInt(c.getColumnIndex(SoundModelContract.KEY_RECOGNITION_MODES));
                                    int[] users = getArrayForCommaSeparatedString(c.getString(c.getColumnIndex(SoundModelContract.KEY_USERS)));
                                    String modelLocale = c.getString(c.getColumnIndex(SoundModelContract.KEY_LOCALE));
                                    String text = c.getString(c.getColumnIndex(SoundModelContract.KEY_HINT_TEXT));
                                    if (users != null) {
                                        int length = users.length;
                                        int i = 0;
                                        while (true) {
                                            if (i >= length) {
                                                bcp47Locale2 = bcp47Locale3;
                                                isAvailableForCurrentUser = false;
                                                break;
                                            }
                                            bcp47Locale2 = bcp47Locale3;
                                            if (userHandle == users[i]) {
                                                isAvailableForCurrentUser = true;
                                                break;
                                            }
                                            i++;
                                            bcp47Locale3 = bcp47Locale2;
                                        }
                                        if (isAvailableForCurrentUser) {
                                            break;
                                        }
                                    } else {
                                        Slog.w(TAG, "Ignoring SoundModel since it doesn't specify users");
                                        bcp47Locale2 = bcp47Locale3;
                                    }
                                } else {
                                    try {
                                        Slog.w(TAG, "Ignoring SoundModel since it doesn't specify an ID");
                                        bcp47Locale2 = bcp47Locale3;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        c.close();
                                        db.close();
                                        throw th;
                                    }
                                }
                            } else {
                                bcp47Locale2 = bcp47Locale3;
                            }
                            try {
                                if (!c.moveToNext()) {
                                    break;
                                }
                                bcp47Locale3 = bcp47Locale2;
                            } catch (Throwable th3) {
                                th = th3;
                                c.close();
                                db.close();
                                throw th;
                            }
                        }
                    }
                    Slog.w(TAG, "No SoundModel available for the given keyphrase");
                    c.close();
                    db.close();
                    return null;
                } catch (Throwable th4) {
                    th = th4;
                    c.close();
                    db.close();
                    throw th;
                }
            } catch (Throwable th5) {
                Throwable th6 = th5;
                throw th6;
            }
        }
    }

    private static String getCommaSeparatedString(int[] users) {
        if (users == null) {
            return "";
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
            for (SoundModelRecord record : records) {
                if (this != record && V6PrimaryKeyMatches(record) && !Arrays.equals(this.data, record.data)) {
                    return false;
                }
            }
            for (SoundModelRecord record2 : records) {
                if (V6PrimaryKeyMatches(record2)) {
                    if (this == record2) {
                        return true;
                    }
                    return false;
                }
            }
            return true;
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
}
