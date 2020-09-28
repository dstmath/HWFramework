package android.database.sqlite;

import android.database.Cursor;
import android.util.Log;
import com.huawei.indexsearch.IndexSearchParser;
import java.util.ArrayList;
import java.util.List;

public class HwSQLiteDatabase implements IHwSQLiteDatabase {
    private static final String BODY_TABLE_OF_EMAIL = "Body";
    private static final String CALENDARS_TABLE_OF_CALENDAR = "Calendars";
    private static final String EVENTS_TABLE_OF_CALENDAR = "Events";
    private static final String FAVSMS_TABLE_OF_MMSSMS = "fav_sms";
    private static final String FILES_TABLE_OF_INTERNAL_EXTERNAL = "files";
    private static final Object LOCK = new Object();
    private static final String MAILBOX_TABLE_OF_EMAIL = "Mailbox";
    private static final String MESSAGE_TABLE_OF_EMAIL = "Message";
    private static final String TAG = "HwSQLiteDatabase";
    private static IndexSearchParser mIndexSearchParser = IndexSearchParser.getInstance();
    private static HwSQLiteDatabase mInstance = null;

    public static HwSQLiteDatabase getInstance() {
        HwSQLiteDatabase hwSQLiteDatabase;
        synchronized (LOCK) {
            if (mInstance == null && mIndexSearchParser != null) {
                mInstance = new HwSQLiteDatabase();
            }
            hwSQLiteDatabase = mInstance;
        }
        return hwSQLiteDatabase;
    }

    public void triggerAddingIndex(SQLiteDatabase db, String table, long id) {
        String realTable;
        if (db == null) {
            Log.w(TAG, "triggerBuildingIndex(): db is null, return.");
        } else if (id < 0) {
            Log.v(TAG, "triggerBuildingIndex(): invalid id, return.");
        } else {
            IndexSearchParser indexSearchParser = mIndexSearchParser;
            if (indexSearchParser != null && indexSearchParser.isValidTable(table)) {
                if (EVENTS_TABLE_OF_CALENDAR.equals(table)) {
                    Cursor cursor = null;
                    try {
                        cursor = db.rawQuery("SELECT _id FROM Events WHERE _id = " + id + " AND mutators IS NOT 'com.android.providers.contacts'", null);
                        if (cursor != null && cursor.getCount() != 0) {
                            cursor.close();
                        } else if (cursor == null) {
                            return;
                        } else {
                            return;
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                if (db.getThreadSession().hasTransaction()) {
                    if (BODY_TABLE_OF_EMAIL.equals(table)) {
                        realTable = MESSAGE_TABLE_OF_EMAIL;
                    } else {
                        realTable = table;
                    }
                    IHwSQLiteSession hwSQLiteSession = db.getThreadSession().getHwSQLiteSession();
                    if (hwSQLiteSession != null) {
                        hwSQLiteSession.insertTransMap(realTable, id, 0);
                        return;
                    }
                    return;
                }
                mIndexSearchParser.notifyIndexSearchService(id, 0);
            }
        }
    }

    public void triggerUpdatingOrDeletingIndex(SQLiteDatabase db, String table, String whereClause, String[] whereArgs, int operation) {
        String realTable;
        if (db == null) {
            Log.w(TAG, "triggerUpdatingOrDeletingIndex(): db is null, return.");
            return;
        }
        IndexSearchParser indexSearchParser = mIndexSearchParser;
        if (indexSearchParser != null && indexSearchParser.isValidTable(table)) {
            Cursor cursor = null;
            try {
                cursor = queryForIndexSearch(db, table, whereClause, whereArgs, operation);
                if (cursor != null) {
                    if (cursor.getCount() != 0) {
                        if (db.getThreadSession().hasTransaction()) {
                            while (cursor.moveToNext()) {
                                if (BODY_TABLE_OF_EMAIL.equals(table)) {
                                    realTable = MESSAGE_TABLE_OF_EMAIL;
                                } else {
                                    realTable = table;
                                }
                                IHwSQLiteSession hwSQLiteSession = db.getThreadSession().getHwSQLiteSession();
                                if (hwSQLiteSession != null) {
                                    hwSQLiteSession.insertTransMap(realTable, cursor.getLong(0), operation);
                                }
                            }
                        } else {
                            List<Long> ids = new ArrayList<>();
                            while (cursor.moveToNext()) {
                                ids.add(Long.valueOf(cursor.getLong(0)));
                            }
                            mIndexSearchParser.notifyIndexSearchService(ids, operation);
                        }
                        cursor.close();
                        return;
                    }
                }
                Log.v(TAG, "triggerBuildingIndex(): cursor is null or count is 0, return.");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "triggerUpdatingOrDeletingIndex(): RuntimeException.");
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:73:? A[RETURN, SYNTHETIC] */
    public void triggerSQLIndex(SQLiteDatabase db, String sql, Object[] bindArgs) {
        if (db == null) {
            Log.w(TAG, "triggerSQLIndex(): db is null, return.");
        } else if (mIndexSearchParser == null) {
        } else {
            if ("DELETE FROM Calendars WHERE account_name=? AND account_type=?".equals(sql)) {
                Cursor cursorFromAccount = null;
                Cursor cursorFromEvents = null;
                int i = 0;
                List<Long> eventsIds = new ArrayList<>();
                try {
                    Cursor cursorFromAccount2 = db.rawQuery("SELECT _id FROM Calendars where account_name=? AND account_type=?", (String[]) bindArgs);
                    if (cursorFromAccount2 != null) {
                        try {
                            if (cursorFromAccount2.getCount() > 0) {
                                String[] calendarIds = new String[cursorFromAccount2.getCount()];
                                int i2 = 0;
                                while (cursorFromAccount2.moveToNext()) {
                                    try {
                                        calendarIds[i2] = cursorFromAccount2.getString(0);
                                        i2++;
                                    } catch (RuntimeException e) {
                                        cursorFromAccount = cursorFromAccount2;
                                        i = i2;
                                        try {
                                            Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException.");
                                            if (cursorFromAccount != null) {
                                            }
                                            if (0 != 0) {
                                            }
                                            if (!eventsIds.isEmpty()) {
                                            }
                                        } catch (Throwable th) {
                                            th = th;
                                            cursorFromAccount2 = cursorFromAccount;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (cursorFromAccount2 != null) {
                                            cursorFromAccount2.close();
                                        }
                                        if (0 != 0) {
                                            cursorFromEvents.close();
                                        }
                                        if (!eventsIds.isEmpty()) {
                                            eventsIds.clear();
                                        }
                                        throw th;
                                    }
                                }
                                cursorFromEvents = queryForIndexSearch(db, EVENTS_TABLE_OF_CALENDAR, "calendar_id IN (?)", calendarIds, 2);
                                if (cursorFromEvents != null && cursorFromEvents.getCount() > 0) {
                                    while (cursorFromEvents.moveToNext()) {
                                        eventsIds.add(Long.valueOf(cursorFromEvents.getLong(0)));
                                    }
                                    mIndexSearchParser.notifyIndexSearchService(eventsIds, 2);
                                }
                                if (cursorFromAccount2 != null) {
                                    cursorFromAccount2.close();
                                }
                                if (cursorFromEvents != null) {
                                    cursorFromEvents.close();
                                }
                                if (eventsIds.isEmpty()) {
                                    eventsIds.clear();
                                    return;
                                }
                                return;
                            }
                        } catch (RuntimeException e2) {
                            cursorFromAccount = cursorFromAccount2;
                            Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException.");
                            if (cursorFromAccount != null) {
                            }
                            if (0 != 0) {
                            }
                            if (!eventsIds.isEmpty()) {
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursorFromAccount2 != null) {
                            }
                            if (0 != 0) {
                            }
                            if (!eventsIds.isEmpty()) {
                            }
                            throw th;
                        }
                    }
                    if (cursorFromAccount2 != null) {
                    }
                    if (cursorFromEvents != null) {
                    }
                    if (eventsIds.isEmpty()) {
                    }
                } catch (RuntimeException e3) {
                    Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException.");
                    if (cursorFromAccount != null) {
                        cursorFromAccount.close();
                    }
                    if (0 != 0) {
                        cursorFromEvents.close();
                    }
                    if (!eventsIds.isEmpty()) {
                        eventsIds.clear();
                    }
                }
            }
        }
    }

    public void triggerTransactionIndex(SQLiteDatabase db) {
        if (db == null) {
            Log.w(TAG, "triggerSQLIndex(): db is null, return.");
            return;
        }
        IHwSQLiteSession hwSQLiteSession = db.getThreadSession().getHwSQLiteSession();
        if (hwSQLiteSession != null && hwSQLiteSession.isCommitSuccess()) {
            if (!(mIndexSearchParser == null || hwSQLiteSession.getTransMap() == null || hwSQLiteSession.getTransMap().size() <= 0)) {
                ArrayList<Long> insertArgs = new ArrayList<>();
                ArrayList<Long> updateArgs = new ArrayList<>();
                ArrayList<Long> deleteArgs = new ArrayList<>();
                for (SQLInfo sqlinfo : hwSQLiteSession.getTransMap().keySet()) {
                    if (mIndexSearchParser.isValidTable(sqlinfo.getTable())) {
                        int intValue = ((Integer) hwSQLiteSession.getTransMap().get(sqlinfo)).intValue();
                        if (intValue == 0) {
                            insertArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                        } else if (intValue == 1) {
                            updateArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                        } else if (intValue == 2) {
                            deleteArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                        }
                    }
                }
                if (insertArgs.size() > 0) {
                    mIndexSearchParser.notifyIndexSearchService(insertArgs, 0);
                }
                if (updateArgs.size() > 0) {
                    mIndexSearchParser.notifyIndexSearchService(updateArgs, 1);
                }
                if (deleteArgs.size() > 0) {
                    mIndexSearchParser.notifyIndexSearchService(deleteArgs, 2);
                }
            }
            hwSQLiteSession.clearTransMap();
        }
    }

    private Cursor queryForIndexSearch(SQLiteDatabase db, String table, String whereClause, String[] whereArgs, int operation) {
        StringBuilder sql = new StringBuilder();
        if (FILES_TABLE_OF_INTERNAL_EXTERNAL.equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" AND ");
            sql.append("((mime_type='text/plain') OR (mime_type='text/html') OR (mime_type='text/htm') OR (mime_type = 'application/msword') OR (mime_type = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') OR (mime_type = 'application/vnd.ms-excel') OR (mime_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') OR (mime_type = 'application/mspowerpoint') OR (mime_type = 'application/vnd.openxmlformats-officedocument.presentationml.presentation')) ");
        } else if (BODY_TABLE_OF_EMAIL.equals(table)) {
            sql.append("SELECT messageKey FROM Body WHERE ");
            sql.append(whereClause);
        } else if (EVENTS_TABLE_OF_CALENDAR.equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" AND ");
            sql.append("mutators IS NOT 'com.android.providers.contacts'");
        } else if (CALENDARS_TABLE_OF_CALENDAR.equals(table)) {
            if (whereArgs == null || whereArgs.length != 1) {
                sql.append("SELECT _id FROM ");
                sql.append(table);
                sql.append(" WHERE ");
                sql.append(whereClause);
            } else {
                sql.append("SELECT _id FROM Events WHERE calendar_id IN (?)");
            }
        } else if (MAILBOX_TABLE_OF_EMAIL.equals(table)) {
            if (operation != 2) {
                return null;
            }
            if (whereClause == null) {
                sql.append("SELECT _id FROM Message");
            } else {
                sql.append("SELECT _id FROM Message WHERE mailboxKey in (select _id FROM Mailbox WHERE ");
                sql.append(whereClause);
                sql.append(")");
            }
        } else if (!FAVSMS_TABLE_OF_MMSSMS.equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
        } else if (whereClause == null) {
            sql.append("SELECT _id FROM words WHERE table_to_use IS 8");
        } else {
            sql.append("SELECT _id FROM words WHERE source_id in (select _id FROM fav_sms WHERE ");
            sql.append(whereClause);
            sql.append(" ) AND table_to_use IS 8");
        }
        return db.rawQuery(sql.toString(), whereArgs);
    }
}
