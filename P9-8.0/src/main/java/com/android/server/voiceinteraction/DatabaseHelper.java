package com.android.server.voiceinteraction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.soundtrigger.SoundTrigger.Keyphrase;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.text.TextUtils;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
            if (this.keyphraseId == record.keyphraseId && stringComparisonHelper(this.locale, record.locale)) {
                return stringComparisonHelper(this.users, record.users);
            }
            return false;
        }

        public boolean ifViolatesV6PrimaryKeyIsFirstOfAnyDuplicates(List<SoundModelRecord> records) {
            boolean z = true;
            for (SoundModelRecord record : records) {
                if (this != record && V6PrimaryKeyMatches(record) && (Arrays.equals(this.data, record.data) ^ 1) != 0) {
                    return false;
                }
            }
            for (SoundModelRecord record2 : records) {
                if (V6PrimaryKeyMatches(record2)) {
                    if (this != record2) {
                        z = false;
                    }
                    return z;
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
            List<SoundModelRecord> old_records = new ArrayList();
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
            oldVersion++;
        }
    }

    /* JADX WARNING: Missing block: B:28:0x00b0, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateKeyphraseSoundModel(KeyphraseSoundModel soundModel) {
        boolean z = true;
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("model_uuid", soundModel.uuid.toString());
            if (soundModel.vendorUuid != null) {
                values.put("vendor_uuid", soundModel.vendorUuid.toString());
            }
            values.put(SoundModelContract.KEY_TYPE, Integer.valueOf(0));
            values.put("data", soundModel.data);
            if (soundModel.keyphrases == null || soundModel.keyphrases.length != 1) {
            } else {
                values.put(SoundModelContract.KEY_KEYPHRASE_ID, Integer.valueOf(soundModel.keyphrases[0].id));
                values.put(SoundModelContract.KEY_RECOGNITION_MODES, Integer.valueOf(soundModel.keyphrases[0].recognitionModes));
                values.put(SoundModelContract.KEY_USERS, getCommaSeparatedString(soundModel.keyphrases[0].users));
                values.put(SoundModelContract.KEY_LOCALE, soundModel.keyphrases[0].locale);
                values.put(SoundModelContract.KEY_HINT_TEXT, soundModel.keyphrases[0].text);
                try {
                    if (db.insertWithOnConflict(SoundModelContract.TABLE, null, values, 5) == -1) {
                        z = false;
                    }
                    db.close();
                    return z;
                } catch (Throwable th) {
                    db.close();
                }
            }
        }
    }

    public boolean deleteKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        boolean z = false;
        bcp47Locale = Locale.forLanguageTag(bcp47Locale).toLanguageTag();
        synchronized (this) {
            KeyphraseSoundModel soundModel = getKeyphraseSoundModel(keyphraseId, userHandle, bcp47Locale);
            if (soundModel == null) {
                return false;
            }
            SQLiteDatabase db = getWritableDatabase();
            try {
                if (db.delete(SoundModelContract.TABLE, "model_uuid='" + soundModel.uuid.toString() + "'", null) != 0) {
                    z = true;
                }
                db.close();
                return z;
            } catch (Throwable th) {
                db.close();
            }
        }
    }

    public KeyphraseSoundModel getKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        synchronized (this) {
            String selectQuery = "SELECT  * FROM sound_model WHERE keyphrase_id= '" + keyphraseId + "' AND " + SoundModelContract.KEY_LOCALE + "='" + Locale.forLanguageTag(bcp47Locale).toLanguageTag() + "'";
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            try {
                if (c.moveToFirst()) {
                    do {
                        if (c.getInt(c.getColumnIndex(SoundModelContract.KEY_TYPE)) == 0) {
                            String modelUuid = c.getString(c.getColumnIndex("model_uuid"));
                            if (modelUuid == null) {
                                Slog.w(TAG, "Ignoring SoundModel since it doesn't specify an ID");
                            } else {
                                String vendorUuidString = null;
                                int vendorUuidColumn = c.getColumnIndex("vendor_uuid");
                                if (vendorUuidColumn != -1) {
                                    vendorUuidString = c.getString(vendorUuidColumn);
                                }
                                byte[] data = c.getBlob(c.getColumnIndex("data"));
                                int recognitionModes = c.getInt(c.getColumnIndex(SoundModelContract.KEY_RECOGNITION_MODES));
                                int[] users = getArrayForCommaSeparatedString(c.getString(c.getColumnIndex(SoundModelContract.KEY_USERS)));
                                String modelLocale = c.getString(c.getColumnIndex(SoundModelContract.KEY_LOCALE));
                                String text = c.getString(c.getColumnIndex(SoundModelContract.KEY_HINT_TEXT));
                                if (users == null) {
                                    Slog.w(TAG, "Ignoring SoundModel since it doesn't specify users");
                                } else {
                                    boolean isAvailableForCurrentUser = false;
                                    for (int i : users) {
                                        if (userHandle == i) {
                                            isAvailableForCurrentUser = true;
                                            break;
                                        }
                                    }
                                    if (isAvailableForCurrentUser) {
                                        Keyphrase[] keyphrases = new Keyphrase[]{new Keyphrase(keyphraseId, recognitionModes, modelLocale, text, users)};
                                        UUID vendorUuid = null;
                                        if (vendorUuidString != null) {
                                            vendorUuid = UUID.fromString(vendorUuidString);
                                        }
                                        KeyphraseSoundModel model = new KeyphraseSoundModel(UUID.fromString(modelUuid), vendorUuid, data, keyphrases);
                                        c.close();
                                        db.close();
                                        return model;
                                    }
                                }
                            }
                        }
                    } while (c.moveToNext());
                }
                Slog.w(TAG, "No SoundModel available for the given keyphrase");
                c.close();
                db.close();
                return null;
            } catch (Throwable th) {
                c.close();
                db.close();
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
}
