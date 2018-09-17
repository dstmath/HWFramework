package android.database.sqlite;

import android.database.sqlite.SQLiteDatabase.CustomFunction;

public final class SQLiteCustomFunction {
    public final CustomFunction callback;
    public final String name;
    public final int numArgs;

    public SQLiteCustomFunction(String name, int numArgs, CustomFunction callback) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null.");
        }
        this.name = name;
        this.numArgs = numArgs;
        this.callback = callback;
    }

    private void dispatchCallback(String[] args) {
        this.callback.callback(args);
    }
}
