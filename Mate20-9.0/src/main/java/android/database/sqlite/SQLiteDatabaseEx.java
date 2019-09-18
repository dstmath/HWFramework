package android.database.sqlite;

public class SQLiteDatabaseEx {

    public interface DatabaseConnectionExclusiveHandler {
        boolean onConnectionExclusive();
    }
}
