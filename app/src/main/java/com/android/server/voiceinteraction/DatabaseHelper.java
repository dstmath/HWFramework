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
import java.util.Locale;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_SOUND_MODEL = "CREATE TABLE sound_model(model_uuid TEXT PRIMARY KEY,keyphrase_id INTEGER,type INTEGER,data BLOB,recognition_modes INTEGER,locale TEXT,hint_text TEXT,users TEXT)";
    static final boolean DBG = false;
    private static final String NAME = "sound_model.db";
    static final String TAG = "SoundModelDBHelper";
    private static final int VERSION = 4;

    public interface SoundModelContract {
        public static final String KEY_DATA = "data";
        public static final String KEY_HINT_TEXT = "hint_text";
        public static final String KEY_KEYPHRASE_ID = "keyphrase_id";
        public static final String KEY_LOCALE = "locale";
        public static final String KEY_MODEL_UUID = "model_uuid";
        public static final String KEY_RECOGNITION_MODES = "recognition_modes";
        public static final String KEY_TYPE = "type";
        public static final String KEY_USERS = "users";
        public static final String TABLE = "sound_model";
    }

    public DatabaseHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SOUND_MODEL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS sound_model");
        onCreate(db);
    }

    public boolean updateKeyphraseSoundModel(KeyphraseSoundModel soundModel) {
        boolean z = true;
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SoundModelContract.KEY_MODEL_UUID, soundModel.uuid.toString());
            values.put(SoundModelContract.KEY_TYPE, Integer.valueOf(0));
            values.put(SoundModelContract.KEY_DATA, soundModel.data);
            if (soundModel.keyphrases == null || soundModel.keyphrases.length != 1) {
                return DBG;
            }
            values.put(SoundModelContract.KEY_KEYPHRASE_ID, Integer.valueOf(soundModel.keyphrases[0].id));
            values.put(SoundModelContract.KEY_RECOGNITION_MODES, Integer.valueOf(soundModel.keyphrases[0].recognitionModes));
            values.put(SoundModelContract.KEY_USERS, getCommaSeparatedString(soundModel.keyphrases[0].users));
            values.put(SoundModelContract.KEY_LOCALE, soundModel.keyphrases[0].locale);
            values.put(SoundModelContract.KEY_HINT_TEXT, soundModel.keyphrases[0].text);
            try {
                if (db.insertWithOnConflict(SoundModelContract.TABLE, null, values, 5) == -1) {
                    z = DBG;
                }
                db.close();
                return z;
            } catch (Throwable th) {
                db.close();
            }
        }
    }

    public boolean deleteKeyphraseSoundModel(int keyphraseId, int userHandle, String bcp47Locale) {
        boolean z = DBG;
        bcp47Locale = Locale.forLanguageTag(bcp47Locale).toLanguageTag();
        synchronized (this) {
            KeyphraseSoundModel soundModel = getKeyphraseSoundModel(keyphraseId, userHandle, bcp47Locale);
            if (soundModel == null) {
                return DBG;
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
            if (c.moveToFirst()) {
                do {
                    if (c.getInt(c.getColumnIndex(SoundModelContract.KEY_TYPE)) == 0) {
                        try {
                            String modelUuid = c.getString(c.getColumnIndex(SoundModelContract.KEY_MODEL_UUID));
                            if (modelUuid == null) {
                                Slog.w(TAG, "Ignoring SoundModel since it doesn't specify an ID");
                            } else {
                                byte[] data = c.getBlob(c.getColumnIndex(SoundModelContract.KEY_DATA));
                                int recognitionModes = c.getInt(c.getColumnIndex(SoundModelContract.KEY_RECOGNITION_MODES));
                                int[] users = getArrayForCommaSeparatedString(c.getString(c.getColumnIndex(SoundModelContract.KEY_USERS)));
                                String modelLocale = c.getString(c.getColumnIndex(SoundModelContract.KEY_LOCALE));
                                String text = c.getString(c.getColumnIndex(SoundModelContract.KEY_HINT_TEXT));
                                if (users == null) {
                                    Slog.w(TAG, "Ignoring SoundModel since it doesn't specify users");
                                } else {
                                    boolean isAvailableForCurrentUser = DBG;
                                    for (int i : users) {
                                        if (userHandle == i) {
                                            isAvailableForCurrentUser = true;
                                            break;
                                        }
                                    }
                                    if (isAvailableForCurrentUser) {
                                        KeyphraseSoundModel model = new KeyphraseSoundModel(UUID.fromString(modelUuid), null, data, new Keyphrase[]{new Keyphrase(keyphraseId, recognitionModes, modelLocale, text, users)});
                                        c.close();
                                        db.close();
                                        return model;
                                    }
                                }
                            }
                        } catch (Throwable th) {
                            c.close();
                            db.close();
                        }
                    }
                } while (c.moveToNext());
                Slog.w(TAG, "No SoundModel available for the given keyphrase");
                c.close();
                db.close();
                return null;
            }
            Slog.w(TAG, "No SoundModel available for the given keyphrase");
            c.close();
            db.close();
            return null;
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
