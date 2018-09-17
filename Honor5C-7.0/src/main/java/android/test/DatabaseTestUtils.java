package android.test;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.android.collect.Sets;
import java.util.Set;

@Deprecated
public class DatabaseTestUtils {
    public static void assertSchemaEquals(SQLiteDatabase expectedDb, SQLiteDatabase db) {
        MoreAsserts.assertEquals(getSchemaSet(expectedDb), getSchemaSet(db));
    }

    private static Set<String> getSchemaSet(SQLiteDatabase db) {
        Set<String> schemaSet = Sets.newHashSet();
        Cursor entityCursor = db.rawQuery("SELECT sql FROM sqlite_master", null);
        while (entityCursor.moveToNext()) {
            try {
                schemaSet.add(entityCursor.getString(0));
            } finally {
                entityCursor.close();
            }
        }
        return schemaSet;
    }
}
