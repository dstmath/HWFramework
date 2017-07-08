package android.provider;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.SyncStateContract.Columns;
import android.provider.SyncStateContract.Helpers;
import android.util.Pair;

public class BrowserContract {
    public static final String AUTHORITY = "com.android.browser";
    public static final Uri AUTHORITY_URI = null;
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public static final String PARAM_LIMIT = "limit";

    public static final class Accounts {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final Uri CONTENT_URI = null;
        public static final String ROOT_ID = "root_id";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Accounts.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Accounts.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Accounts.<clinit>():void");
        }
    }

    interface BaseSyncColumns {
        public static final String SYNC1 = "sync1";
        public static final String SYNC2 = "sync2";
        public static final String SYNC3 = "sync3";
        public static final String SYNC4 = "sync4";
        public static final String SYNC5 = "sync5";
    }

    interface CommonColumns {
        public static final String DATE_CREATED = "created";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String _ID = "_id";
    }

    interface ImageColumns {
        public static final String FAVICON = "favicon";
        public static final String THUMBNAIL = "thumbnail";
        public static final String TOUCH_ICON = "touch_icon";
    }

    interface SyncColumns extends BaseSyncColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DATE_MODIFIED = "modified";
        public static final String DIRTY = "dirty";
        public static final String SOURCE_ID = "sourceid";
        public static final String VERSION = "version";
    }

    public static final class Bookmarks implements CommonColumns, ImageColumns, SyncColumns {
        public static final int BOOKMARK_TYPE_BOOKMARK = 1;
        public static final int BOOKMARK_TYPE_BOOKMARK_BAR_FOLDER = 3;
        public static final int BOOKMARK_TYPE_FOLDER = 2;
        public static final int BOOKMARK_TYPE_MOBILE_FOLDER = 5;
        public static final int BOOKMARK_TYPE_OTHER_FOLDER = 4;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/bookmark";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/bookmark";
        public static final Uri CONTENT_URI = null;
        public static final Uri CONTENT_URI_DEFAULT_FOLDER = null;
        public static final String INSERT_AFTER = "insert_after";
        public static final String INSERT_AFTER_SOURCE_ID = "insert_after_source";
        public static final String IS_DELETED = "deleted";
        public static final String IS_FOLDER = "folder";
        public static final String PARAM_ACCOUNT_NAME = "acct_name";
        public static final String PARAM_ACCOUNT_TYPE = "acct_type";
        public static final String PARENT = "parent";
        public static final String PARENT_SOURCE_ID = "parent_source";
        public static final String POSITION = "position";
        public static final String QUERY_PARAMETER_SHOW_DELETED = "show_deleted";
        public static final String TYPE = "type";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Bookmarks.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Bookmarks.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Bookmarks.<clinit>():void");
        }

        private Bookmarks() {
        }

        public static final Uri buildFolderUri(long folderId) {
            return ContentUris.withAppendedId(CONTENT_URI_DEFAULT_FOLDER, folderId);
        }
    }

    public static final class ChromeSyncColumns {
        public static final String CLIENT_UNIQUE = "sync4";
        public static final String FOLDER_NAME_BOOKMARKS = "google_chrome_bookmarks";
        public static final String FOLDER_NAME_BOOKMARKS_BAR = "bookmark_bar";
        public static final String FOLDER_NAME_OTHER_BOOKMARKS = "other_bookmarks";
        public static final String FOLDER_NAME_ROOT = "google_chrome";
        public static final String SERVER_UNIQUE = "sync3";

        private ChromeSyncColumns() {
        }
    }

    interface HistoryColumns {
        public static final String DATE_LAST_VISITED = "date";
        public static final String USER_ENTERED = "user_entered";
        public static final String VISITS = "visits";
    }

    public static final class Combined implements CommonColumns, HistoryColumns, ImageColumns {
        public static final Uri CONTENT_URI = null;
        public static final String IS_BOOKMARK = "bookmark";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Combined.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Combined.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Combined.<clinit>():void");
        }

        private Combined() {
        }
    }

    public static final class History implements CommonColumns, HistoryColumns, ImageColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/browser-history";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/browser-history";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.History.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.History.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.History.<clinit>():void");
        }

        private History() {
        }
    }

    interface ImageMappingColumns {
        public static final String IMAGE_ID = "image_id";
        public static final String URL = "url";
    }

    public static final class ImageMappings implements ImageMappingColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/image_mappings";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/image_mappings";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.ImageMappings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.ImageMappings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.ImageMappings.<clinit>():void");
        }

        private ImageMappings() {
        }
    }

    public static final class Images implements ImageColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/images";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/images";
        public static final Uri CONTENT_URI = null;
        public static final String DATA = "data";
        public static final int IMAGE_TYPE_FAVICON = 1;
        public static final int IMAGE_TYPE_PRECOMPOSED_TOUCH_ICON = 2;
        public static final int IMAGE_TYPE_TOUCH_ICON = 4;
        public static final String TYPE = "type";
        public static final String URL = "url_key";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Images.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Images.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Images.<clinit>():void");
        }

        private Images() {
        }
    }

    public static final class Searches {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/searches";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/searches";
        public static final Uri CONTENT_URI = null;
        public static final String DATE = "date";
        public static final String SEARCH = "search";
        public static final String _ID = "_id";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Searches.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Searches.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Searches.<clinit>():void");
        }

        private Searches() {
        }
    }

    public static final class Settings {
        public static final Uri CONTENT_URI = null;
        public static final String KEY = "key";
        public static final String KEY_SYNC_ENABLED = "sync_enabled";
        public static final String VALUE = "value";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.Settings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.Settings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.Settings.<clinit>():void");
        }

        private Settings() {
        }

        public static boolean isSyncEnabled(Context context) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(CONTENT_URI, new String[]{VALUE}, "key=?", new String[]{KEY_SYNC_ENABLED}, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                }
                boolean z;
                if (cursor.getInt(0) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public static void setSyncEnabled(Context context, boolean enabled) {
            ContentValues values = new ContentValues();
            values.put(KEY, KEY_SYNC_ENABLED);
            values.put(VALUE, Integer.valueOf(enabled ? 1 : 0));
            context.getContentResolver().insert(CONTENT_URI, values);
        }
    }

    public static final class SyncState implements Columns {
        public static final String CONTENT_DIRECTORY = "syncstate";
        public static final Uri CONTENT_URI = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.SyncState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.SyncState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.SyncState.<clinit>():void");
        }

        private SyncState() {
        }

        public static byte[] get(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.get(provider, CONTENT_URI, account);
        }

        public static Pair<Uri, byte[]> getWithUri(ContentProviderClient provider, Account account) throws RemoteException {
            return Helpers.getWithUri(provider, CONTENT_URI, account);
        }

        public static void set(ContentProviderClient provider, Account account, byte[] data) throws RemoteException {
            Helpers.set(provider, CONTENT_URI, account, data);
        }

        public static ContentProviderOperation newSetOperation(Account account, byte[] data) {
            return Helpers.newSetOperation(CONTENT_URI, account, data);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.BrowserContract.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.BrowserContract.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.BrowserContract.<clinit>():void");
    }

    public BrowserContract() {
    }
}
