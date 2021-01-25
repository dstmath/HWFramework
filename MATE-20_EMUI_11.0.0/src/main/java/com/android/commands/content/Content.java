package com.android.commands.content;

import android.app.ActivityManager;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.text.TextUtils;
import java.io.FileDescriptor;
import java.io.PrintStream;

public class Content {
    private static final String USAGE = "usage: adb shell content [subcommand] [options]\n\nusage: adb shell content insert --uri <URI> [--user <USER_ID>] --bind <BINDING> [--bind <BINDING>...]\n  <URI> a content provider URI.\n  <BINDING> binds a typed value to a column and is formatted:\n  <COLUMN_NAME>:<TYPE>:<COLUMN_VALUE> where:\n  <TYPE> specifies data type such as:\n  b - boolean, s - string, i - integer, l - long, f - float, d - double, n - null\n  Note: Omit the value for passing an empty string, e.g column:s:\n  Example:\n  # Add \"new_setting\" secure setting with value \"new_value\".\n  adb shell content insert --uri content://settings/secure --bind name:s:new_setting --bind value:s:new_value\n\nusage: adb shell content update --uri <URI> [--user <USER_ID>] [--where <WHERE>]\n  <WHERE> is a SQL style where clause in quotes (You have to escape single quotes - see example below).\n  Example:\n  # Change \"new_setting\" secure setting to \"newer_value\".\n  adb shell content update --uri content://settings/secure --bind value:s:newer_value --where \"name='new_setting'\"\n\nusage: adb shell content delete --uri <URI> [--user <USER_ID>] --bind <BINDING> [--bind <BINDING>...] [--where <WHERE>]\n  Example:\n  # Remove \"new_setting\" secure setting.\n  adb shell content delete --uri content://settings/secure --where \"name='new_setting'\"\n\nusage: adb shell content query --uri <URI> [--user <USER_ID>] [--projection <PROJECTION>] [--where <WHERE>] [--sort <SORT_ORDER>]\n  <PROJECTION> is a list of colon separated column names and is formatted:\n  <COLUMN_NAME>[:<COLUMN_NAME>...]\n  <SORT_ORDER> is the order in which rows in the result should be sorted.\n  Example:\n  # Select \"name\" and \"value\" columns from secure settings where \"name\" is equal to \"new_setting\" and sort the result by name in ascending order.\n  adb shell content query --uri content://settings/secure --projection name:value --where \"name='new_setting'\" --sort \"name ASC\"\n\nusage: adb shell content call --uri <URI> --method <METHOD> [--arg <ARG>]\n       [--extra <BINDING> ...]\n  <METHOD> is the name of a provider-defined method\n  <ARG> is an optional string argument\n  <BINDING> is like --bind above, typed data of the form <KEY>:{b,s,i,l,f,d}:<VAL>\n\nusage: adb shell content read --uri <URI> [--user <USER_ID>]\n  Example:\n  adb shell 'content read --uri content://settings/system/ringtone_cache' > host.ogg\n\nusage: adb shell content write --uri <URI> [--user <USER_ID>]\n  Example:\n  adb shell 'content write --uri content://settings/system/ringtone_cache' < host.ogg\n\nusage: adb shell content gettype --uri <URI> [--user <USER_ID>]\n  Example:\n  adb shell content gettype --uri content://media/internal/audio/media/\n\n";

    private static class Parser {
        private static final String ARGUMENT_ARG = "--arg";
        private static final String ARGUMENT_BIND = "--bind";
        private static final String ARGUMENT_CALL = "call";
        private static final String ARGUMENT_DELETE = "delete";
        private static final String ARGUMENT_EXTRA = "--extra";
        private static final String ARGUMENT_GET_TYPE = "gettype";
        private static final String ARGUMENT_INSERT = "insert";
        private static final String ARGUMENT_METHOD = "--method";
        private static final String ARGUMENT_PREFIX = "--";
        private static final String ARGUMENT_PROJECTION = "--projection";
        private static final String ARGUMENT_QUERY = "query";
        private static final String ARGUMENT_READ = "read";
        private static final String ARGUMENT_SORT = "--sort";
        private static final String ARGUMENT_UPDATE = "update";
        private static final String ARGUMENT_URI = "--uri";
        private static final String ARGUMENT_USER = "--user";
        private static final String ARGUMENT_WHERE = "--where";
        private static final String ARGUMENT_WRITE = "write";
        private static final String COLON = ":";
        private static final String TYPE_BOOLEAN = "b";
        private static final String TYPE_DOUBLE = "d";
        private static final String TYPE_FLOAT = "f";
        private static final String TYPE_INTEGER = "i";
        private static final String TYPE_LONG = "l";
        private static final String TYPE_NULL = "n";
        private static final String TYPE_STRING = "s";
        private final Tokenizer mTokenizer;

        public Parser(String[] args) {
            this.mTokenizer = new Tokenizer(args);
        }

        public Command parseCommand() {
            try {
                String operation = this.mTokenizer.nextArg();
                if (ARGUMENT_INSERT.equals(operation)) {
                    return parseInsertCommand();
                }
                if (ARGUMENT_DELETE.equals(operation)) {
                    return parseDeleteCommand();
                }
                if (ARGUMENT_UPDATE.equals(operation)) {
                    return parseUpdateCommand();
                }
                if (ARGUMENT_QUERY.equals(operation)) {
                    return parseQueryCommand();
                }
                if (ARGUMENT_CALL.equals(operation)) {
                    return parseCallCommand();
                }
                if (ARGUMENT_READ.equals(operation)) {
                    return parseReadCommand();
                }
                if (ARGUMENT_WRITE.equals(operation)) {
                    return parseWriteCommand();
                }
                if (ARGUMENT_GET_TYPE.equals(operation)) {
                    return parseGetTypeCommand();
                }
                throw new IllegalArgumentException("Unsupported operation: " + operation);
            } catch (IllegalArgumentException iae) {
                System.out.println(Content.USAGE);
                PrintStream printStream = System.out;
                printStream.println("[ERROR] " + iae.getMessage());
                return null;
            }
        }

        private InsertCommand parseInsertCommand() {
            Uri uri = null;
            int userId = 0;
            ContentValues values = new ContentValues();
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else if (ARGUMENT_BIND.equals(argument)) {
                        parseBindValue(values);
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri == null) {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                } else if (values.size() != 0) {
                    return new InsertCommand(uri, userId, values);
                } else {
                    throw new IllegalArgumentException("Bindings not specified. Did you specify --bind argument(s)?");
                }
            }
        }

        private DeleteCommand parseDeleteCommand() {
            Uri uri = null;
            int userId = 0;
            String where = null;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else if (ARGUMENT_WHERE.equals(argument)) {
                        where = argumentValueRequired(argument);
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri != null) {
                    return new DeleteCommand(uri, userId, where);
                } else {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                }
            }
        }

        private UpdateCommand parseUpdateCommand() {
            Uri uri = null;
            int userId = 0;
            String where = null;
            ContentValues values = new ContentValues();
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else if (ARGUMENT_WHERE.equals(argument)) {
                        where = argumentValueRequired(argument);
                    } else if (ARGUMENT_BIND.equals(argument)) {
                        parseBindValue(values);
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri == null) {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                } else if (values.size() != 0) {
                    return new UpdateCommand(uri, userId, values, where);
                } else {
                    throw new IllegalArgumentException("Bindings not specified. Did you specify --bind argument(s)?");
                }
            }
        }

        public CallCommand parseCallCommand() {
            String method = null;
            int userId = 0;
            String arg = null;
            Uri uri = null;
            ContentValues values = new ContentValues();
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else if (ARGUMENT_METHOD.equals(argument)) {
                        method = argumentValueRequired(argument);
                    } else if (ARGUMENT_ARG.equals(argument)) {
                        arg = argumentValueRequired(argument);
                    } else if (ARGUMENT_EXTRA.equals(argument)) {
                        parseBindValue(values);
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri == null) {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                } else if (method != null) {
                    return new CallCommand(uri, userId, method, arg, values);
                } else {
                    throw new IllegalArgumentException("Content provider method not specified.");
                }
            }
        }

        private GetTypeCommand parseGetTypeCommand() {
            Uri uri = null;
            int userId = 0;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri != null) {
                    return new GetTypeCommand(uri, userId);
                } else {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                }
            }
        }

        private ReadCommand parseReadCommand() {
            Uri uri = null;
            int userId = 0;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri != null) {
                    return new ReadCommand(uri, userId);
                } else {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                }
            }
        }

        private WriteCommand parseWriteCommand() {
            Uri uri = null;
            int userId = 0;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri != null) {
                    return new WriteCommand(uri, userId);
                } else {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                }
            }
        }

        public QueryCommand parseQueryCommand() {
            Uri uri = null;
            int userId = 0;
            String[] projection = null;
            String sort = null;
            String where = null;
            while (true) {
                String argument = this.mTokenizer.nextArg();
                if (argument != null) {
                    if (ARGUMENT_URI.equals(argument)) {
                        uri = Uri.parse(argumentValueRequired(argument));
                    } else if (ARGUMENT_USER.equals(argument)) {
                        userId = Integer.parseInt(argumentValueRequired(argument));
                    } else if (ARGUMENT_WHERE.equals(argument)) {
                        where = argumentValueRequired(argument);
                    } else if (ARGUMENT_SORT.equals(argument)) {
                        sort = argumentValueRequired(argument);
                    } else if (ARGUMENT_PROJECTION.equals(argument)) {
                        projection = argumentValueRequired(argument).split("[\\s]*:[\\s]*");
                    } else {
                        throw new IllegalArgumentException("Unsupported argument: " + argument);
                    }
                } else if (uri != null) {
                    return new QueryCommand(uri, userId, projection, where, sort);
                } else {
                    throw new IllegalArgumentException("Content provider URI not specified. Did you specify --uri argument?");
                }
            }
        }

        private void parseBindValue(ContentValues values) {
            String argument = this.mTokenizer.nextArg();
            if (!TextUtils.isEmpty(argument)) {
                int firstColonIndex = argument.indexOf(COLON);
                if (firstColonIndex >= 0) {
                    int secondColonIndex = argument.indexOf(COLON, firstColonIndex + 1);
                    if (secondColonIndex >= 0) {
                        String column = argument.substring(0, firstColonIndex);
                        String type = argument.substring(firstColonIndex + 1, secondColonIndex);
                        String value = argument.substring(secondColonIndex + 1);
                        if (TYPE_STRING.equals(type)) {
                            values.put(column, value);
                        } else if (TYPE_BOOLEAN.equalsIgnoreCase(type)) {
                            values.put(column, Boolean.valueOf(Boolean.parseBoolean(value)));
                        } else if (TYPE_INTEGER.equalsIgnoreCase(type) || TYPE_LONG.equalsIgnoreCase(type)) {
                            values.put(column, Long.valueOf(Long.parseLong(value)));
                        } else if (TYPE_FLOAT.equalsIgnoreCase(type) || TYPE_DOUBLE.equalsIgnoreCase(type)) {
                            values.put(column, Double.valueOf(Double.parseDouble(value)));
                        } else if (TYPE_NULL.equalsIgnoreCase(type)) {
                            values.putNull(column);
                        } else {
                            throw new IllegalArgumentException("Unsupported type: " + type);
                        }
                    } else {
                        throw new IllegalArgumentException("Binding not well formed: " + argument);
                    }
                } else {
                    throw new IllegalArgumentException("Binding not well formed: " + argument);
                }
            } else {
                throw new IllegalArgumentException("Binding not well formed: " + argument);
            }
        }

        private String argumentValueRequired(String argument) {
            String value = this.mTokenizer.nextArg();
            if (!TextUtils.isEmpty(value) && !value.startsWith(ARGUMENT_PREFIX)) {
                return value;
            }
            throw new IllegalArgumentException("No value for argument: " + argument);
        }
    }

    /* access modifiers changed from: private */
    public static class Tokenizer {
        private final String[] mArgs;
        private int mNextArg;

        public Tokenizer(String[] args) {
            this.mArgs = args;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String nextArg() {
            int i = this.mNextArg;
            String[] strArr = this.mArgs;
            if (i >= strArr.length) {
                return null;
            }
            this.mNextArg = i + 1;
            return strArr[i];
        }
    }

    private static abstract class Command {
        final Uri mUri;
        final int mUserId;

        /* access modifiers changed from: protected */
        public abstract void onExecute(IContentProvider iContentProvider) throws Exception;

        public Command(Uri uri, int userId) {
            this.mUri = uri;
            this.mUserId = userId;
        }

        public final void execute() {
            String providerName = this.mUri.getAuthority();
            try {
                IActivityManager activityManager = ActivityManager.getService();
                IContentProvider provider = null;
                IBinder token = new Binder();
                try {
                    ContentProviderHolder holder = activityManager.getContentProviderExternal(providerName, this.mUserId, token, "*cmd*");
                    if (holder != null) {
                        provider = holder.provider;
                        onExecute(provider);
                        if (provider == null) {
                            return;
                        }
                        return;
                    }
                    throw new IllegalStateException("Could not find provider: " + providerName);
                } finally {
                    if (provider != null) {
                        activityManager.removeContentProviderExternalAsUser(providerName, token, this.mUserId);
                    }
                }
            } catch (Exception e) {
                PrintStream printStream = System.err;
                printStream.println("Error while accessing provider:" + providerName);
                e.printStackTrace();
            }
        }

        public static String resolveCallingPackage() {
            int myUid = Process.myUid();
            if (myUid == 0) {
                return "root";
            }
            if (myUid != 2000) {
                return null;
            }
            return "com.android.shell";
        }
    }

    /* access modifiers changed from: private */
    public static class InsertCommand extends Command {
        final ContentValues mContentValues;

        public InsertCommand(Uri uri, int userId, ContentValues contentValues) {
            super(uri, userId);
            this.mContentValues = contentValues;
        }

        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            provider.insert(resolveCallingPackage(), this.mUri, this.mContentValues);
        }
    }

    /* access modifiers changed from: private */
    public static class DeleteCommand extends Command {
        final String mWhere;

        public DeleteCommand(Uri uri, int userId, String where) {
            super(uri, userId);
            this.mWhere = where;
        }

        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            provider.delete(resolveCallingPackage(), this.mUri, this.mWhere, (String[]) null);
        }
    }

    /* access modifiers changed from: private */
    public static class CallCommand extends Command {
        final String mArg;
        Bundle mExtras = null;
        final String mMethod;

        public CallCommand(Uri uri, int userId, String method, String arg, ContentValues values) {
            super(uri, userId);
            this.mMethod = method;
            this.mArg = arg;
            if (values != null) {
                this.mExtras = new Bundle();
                for (String key : values.keySet()) {
                    Object val = values.get(key);
                    if (val instanceof String) {
                        this.mExtras.putString(key, (String) val);
                    } else if (val instanceof Float) {
                        this.mExtras.putFloat(key, ((Float) val).floatValue());
                    } else if (val instanceof Double) {
                        this.mExtras.putDouble(key, ((Double) val).doubleValue());
                    } else if (val instanceof Boolean) {
                        this.mExtras.putBoolean(key, ((Boolean) val).booleanValue());
                    } else if (val instanceof Integer) {
                        this.mExtras.putInt(key, ((Integer) val).intValue());
                    } else if (val instanceof Long) {
                        this.mExtras.putLong(key, ((Long) val).longValue());
                    }
                }
            }
        }

        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            Bundle result = provider.call((String) null, this.mUri.getAuthority(), this.mMethod, this.mArg, this.mExtras);
            if (result != null) {
                result.size();
            }
            PrintStream printStream = System.out;
            printStream.println("Result: " + result);
        }
    }

    /* access modifiers changed from: private */
    public static class GetTypeCommand extends Command {
        public GetTypeCommand(Uri uri, int userId) {
            super(uri, userId);
        }

        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            String type = provider.getType(this.mUri);
            PrintStream printStream = System.out;
            printStream.println("Result: " + type);
        }
    }

    /* access modifiers changed from: private */
    public static class ReadCommand extends Command {
        public ReadCommand(Uri uri, int userId) {
            super(uri, userId);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
            r1.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
            if (r0 != null) goto L_0x001e;
         */
        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            ParcelFileDescriptor fd = provider.openFile((String) null, this.mUri, "r", (ICancellationSignal) null, (IBinder) null);
            FileUtils.copy(fd.getFileDescriptor(), FileDescriptor.out);
            fd.close();
        }
    }

    /* access modifiers changed from: private */
    public static class WriteCommand extends Command {
        public WriteCommand(Uri uri, int userId) {
            super(uri, userId);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
            r1.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
            if (r0 != null) goto L_0x001e;
         */
        @Override // com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            ParcelFileDescriptor fd = provider.openFile((String) null, this.mUri, "w", (ICancellationSignal) null, (IBinder) null);
            FileUtils.copy(FileDescriptor.in, fd.getFileDescriptor());
            fd.close();
        }
    }

    /* access modifiers changed from: private */
    public static class QueryCommand extends DeleteCommand {
        final String[] mProjection;
        final String mSortOrder;

        public QueryCommand(Uri uri, int userId, String[] projection, String where, String sortOrder) {
            super(uri, userId, where);
            this.mProjection = projection;
            this.mSortOrder = sortOrder;
        }

        @Override // com.android.commands.content.Content.DeleteCommand, com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            Cursor cursor = provider.query(resolveCallingPackage(), this.mUri, this.mProjection, ContentResolver.createSqlQueryBundle(this.mWhere, null, this.mSortOrder), (ICancellationSignal) null);
            if (cursor == null) {
                System.out.println("No result found.");
                return;
            }
            try {
                if (cursor.moveToFirst()) {
                    int rowIndex = 0;
                    StringBuilder builder = new StringBuilder();
                    do {
                        builder.setLength(0);
                        builder.append("Row: ");
                        builder.append(rowIndex);
                        builder.append(" ");
                        rowIndex++;
                        int columnCount = cursor.getColumnCount();
                        for (int i = 0; i < columnCount; i++) {
                            if (i > 0) {
                                builder.append(", ");
                            }
                            String columnName = cursor.getColumnName(i);
                            String columnValue = null;
                            int columnIndex = cursor.getColumnIndex(columnName);
                            int type = cursor.getType(columnIndex);
                            if (type == 0) {
                                columnValue = "NULL";
                            } else if (type == 1) {
                                columnValue = String.valueOf(cursor.getLong(columnIndex));
                            } else if (type == 2) {
                                columnValue = String.valueOf(cursor.getFloat(columnIndex));
                            } else if (type == 3) {
                                columnValue = cursor.getString(columnIndex);
                            } else if (type == 4) {
                                columnValue = "BLOB";
                            }
                            builder.append(columnName);
                            builder.append("=");
                            builder.append(columnValue);
                        }
                        System.out.println(builder);
                    } while (cursor.moveToNext());
                } else {
                    System.out.println("No result found.");
                }
            } finally {
                cursor.close();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class UpdateCommand extends InsertCommand {
        final String mWhere;

        public UpdateCommand(Uri uri, int userId, ContentValues contentValues, String where) {
            super(uri, userId, contentValues);
            this.mWhere = where;
        }

        @Override // com.android.commands.content.Content.InsertCommand, com.android.commands.content.Content.Command
        public void onExecute(IContentProvider provider) throws Exception {
            provider.update(resolveCallingPackage(), this.mUri, this.mContentValues, this.mWhere, (String[]) null);
        }
    }

    public static void main(String[] args) {
        Command command = new Parser(args).parseCommand();
        if (command != null) {
            command.execute();
        }
    }
}
