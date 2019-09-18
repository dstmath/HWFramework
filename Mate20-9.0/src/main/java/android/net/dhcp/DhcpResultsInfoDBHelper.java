package android.net.dhcp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DhcpResultsInfoDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DhcpResultsInfo.db";
    public static final int DATABASE_VERSION = 4;
    public static final String DHCP_RESULTS_INFO_DB_NAME = "DhcpResults";

    public DhcpResultsInfoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("CREATE TABLE if not exists [DhcpResults] (");
        sBuffer.append("[apSSID] TEXT PRIMARY KEY, ");
        sBuffer.append("[IP] TEXT, ");
        sBuffer.append("[DHCPServer] TEXT, ");
        sBuffer.append("[EX1] TEXT, ");
        sBuffer.append("[EX2] TEXT)");
        db.execSQL(sBuffer.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS DhcpResults");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS DhcpResults");
        onCreate(db);
    }
}
