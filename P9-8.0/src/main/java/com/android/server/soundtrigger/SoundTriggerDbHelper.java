package com.android.server.soundtrigger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import java.util.UUID;

public class SoundTriggerDbHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_ST_SOUND_MODEL = "CREATE TABLE st_sound_model(model_uuid TEXT PRIMARY KEY,vendor_uuid TEXT,data BLOB )";
    static final boolean DBG = false;
    private static final String NAME = "st_sound_model.db";
    static final String TAG = "SoundTriggerDbHelper";
    private static final int VERSION = 1;

    public interface GenericSoundModelContract {
        public static final String KEY_DATA = "data";
        public static final String KEY_MODEL_UUID = "model_uuid";
        public static final String KEY_VENDOR_UUID = "vendor_uuid";
        public static final String TABLE = "st_sound_model";
    }

    public SoundTriggerDbHelper(Context context) {
        super(context, NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ST_SOUND_MODEL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS st_sound_model");
        onCreate(db);
    }

    public boolean updateGenericSoundModel(GenericSoundModel soundModel) {
        boolean z;
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("model_uuid", soundModel.uuid.toString());
            values.put("vendor_uuid", soundModel.vendorUuid.toString());
            values.put("data", soundModel.data);
            try {
                z = db.insertWithOnConflict(GenericSoundModelContract.TABLE, null, values, 5) != -1;
                db.close();
            } catch (Throwable th) {
                db.close();
            }
        }
        return z;
    }

    public GenericSoundModel getGenericSoundModel(UUID model_uuid) {
        synchronized (this) {
            String selectQuery = "SELECT  * FROM st_sound_model WHERE model_uuid= '" + model_uuid + "'";
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            try {
                if (c.moveToFirst()) {
                    GenericSoundModel genericSoundModel = new GenericSoundModel(model_uuid, UUID.fromString(c.getString(c.getColumnIndex("vendor_uuid"))), c.getBlob(c.getColumnIndex("data")));
                    c.close();
                    db.close();
                    return genericSoundModel;
                }
                c.close();
                db.close();
                return null;
            } catch (Throwable th) {
                c.close();
                db.close();
            }
        }
    }

    public boolean deleteGenericSoundModel(UUID model_uuid) {
        boolean z = false;
        synchronized (this) {
            GenericSoundModel soundModel = getGenericSoundModel(model_uuid);
            if (soundModel == null) {
                return false;
            }
            SQLiteDatabase db = getWritableDatabase();
            try {
                if (db.delete(GenericSoundModelContract.TABLE, "model_uuid='" + soundModel.uuid.toString() + "'", null) != 0) {
                    z = true;
                }
                db.close();
                return z;
            } catch (Throwable th) {
                db.close();
            }
        }
    }
}
