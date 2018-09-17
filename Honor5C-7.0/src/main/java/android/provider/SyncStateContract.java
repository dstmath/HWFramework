package android.provider;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.service.voice.VoiceInteractionSession;
import android.util.Pair;

public class SyncStateContract {

    public interface Columns extends BaseColumns {
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String DATA = "data";
    }

    public static class Constants implements Columns {
        public static final String CONTENT_DIRECTORY = "syncstate";
    }

    public static final class Helpers {
        private static final String[] DATA_PROJECTION = null;
        private static final String SELECT_BY_ACCOUNT = "account_name=? AND account_type=?";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.SyncStateContract.Helpers.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.SyncStateContract.Helpers.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.provider.SyncStateContract.Helpers.<clinit>():void");
        }

        public static byte[] get(ContentProviderClient provider, Uri uri, Account account) throws RemoteException {
            Cursor c = provider.query(uri, DATA_PROJECTION, SELECT_BY_ACCOUNT, new String[]{account.name, account.type}, null);
            if (c == null) {
                throw new RemoteException();
            }
            try {
                if (c.moveToNext()) {
                    byte[] blob = c.getBlob(c.getColumnIndexOrThrow(VoiceInteractionSession.KEY_DATA));
                    return blob;
                }
                c.close();
                return null;
            } finally {
                c.close();
            }
        }

        public static void set(ContentProviderClient provider, Uri uri, Account account, byte[] data) throws RemoteException {
            ContentValues values = new ContentValues();
            values.put(VoiceInteractionSession.KEY_DATA, data);
            values.put(SettingsColumns.ACCOUNT_NAME, account.name);
            values.put(SettingsColumns.ACCOUNT_TYPE, account.type);
            provider.insert(uri, values);
        }

        public static Uri insert(ContentProviderClient provider, Uri uri, Account account, byte[] data) throws RemoteException {
            ContentValues values = new ContentValues();
            values.put(VoiceInteractionSession.KEY_DATA, data);
            values.put(SettingsColumns.ACCOUNT_NAME, account.name);
            values.put(SettingsColumns.ACCOUNT_TYPE, account.type);
            return provider.insert(uri, values);
        }

        public static void update(ContentProviderClient provider, Uri uri, byte[] data) throws RemoteException {
            ContentValues values = new ContentValues();
            values.put(VoiceInteractionSession.KEY_DATA, data);
            provider.update(uri, values, null, null);
        }

        public static Pair<Uri, byte[]> getWithUri(ContentProviderClient provider, Uri uri, Account account) throws RemoteException {
            Cursor c = provider.query(uri, DATA_PROJECTION, SELECT_BY_ACCOUNT, new String[]{account.name, account.type}, null);
            if (c == null) {
                throw new RemoteException();
            }
            try {
                if (c.moveToNext()) {
                    long rowId = c.getLong(1);
                    Pair<Uri, byte[]> create = Pair.create(ContentUris.withAppendedId(uri, rowId), c.getBlob(c.getColumnIndexOrThrow(VoiceInteractionSession.KEY_DATA)));
                    return create;
                }
                c.close();
                return null;
            } finally {
                c.close();
            }
        }

        public static ContentProviderOperation newSetOperation(Uri uri, Account account, byte[] data) {
            ContentValues values = new ContentValues();
            values.put(VoiceInteractionSession.KEY_DATA, data);
            return ContentProviderOperation.newInsert(uri).withValue(SettingsColumns.ACCOUNT_NAME, account.name).withValue(SettingsColumns.ACCOUNT_TYPE, account.type).withValues(values).build();
        }

        public static ContentProviderOperation newUpdateOperation(Uri uri, byte[] data) {
            ContentValues values = new ContentValues();
            values.put(VoiceInteractionSession.KEY_DATA, data);
            return ContentProviderOperation.newUpdate(uri).withValues(values).build();
        }
    }

    public SyncStateContract() {
    }
}
