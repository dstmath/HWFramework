package com.android.internal.telephony.ims;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Telephony;
import android.text.TextUtils;

public class RcsMessageStoreUtil {
    private ContentResolver mContentResolver;

    RcsMessageStoreUtil(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r0 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        throw r2;
     */
    public int getIntValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) throws RemoteException {
        Cursor cursor = getValueFromTableRow(tableUri, valueColumn, idColumn, idValue);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int i = cursor.getInt(cursor.getColumnIndex(valueColumn));
                $closeResource(null, cursor);
                return i;
            }
        }
        throw new RemoteException("The row with (" + idColumn + " = " + idValue + ") could not be found in " + tableUri);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r0 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        throw r2;
     */
    public long getLongValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) throws RemoteException {
        Cursor cursor = getValueFromTableRow(tableUri, valueColumn, idColumn, idValue);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long j = cursor.getLong(cursor.getColumnIndex(valueColumn));
                $closeResource(null, cursor);
                return j;
            }
        }
        throw new RemoteException("The row with (" + idColumn + " = " + idValue + ") could not be found in " + tableUri);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r0 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        throw r2;
     */
    public double getDoubleValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) throws RemoteException {
        Cursor cursor = getValueFromTableRow(tableUri, valueColumn, idColumn, idValue);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                double d = cursor.getDouble(cursor.getColumnIndex(valueColumn));
                $closeResource(null, cursor);
                return d;
            }
        }
        throw new RemoteException("The row with (" + idColumn + " = " + idValue + ") could not be found in " + tableUri);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        if (r0 != null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        throw r2;
     */
    public String getStringValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) throws RemoteException {
        Cursor cursor = getValueFromTableRow(tableUri, valueColumn, idColumn, idValue);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndex(valueColumn));
                $closeResource(null, cursor);
                return string;
            }
        }
        throw new RemoteException("The row with (" + idColumn + " = " + idValue + ") could not be found in " + tableUri);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0051, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0052, code lost:
        if (r0 != null) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0054, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0057, code lost:
        throw r2;
     */
    public Uri getUriValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) throws RemoteException {
        Cursor cursor = getValueFromTableRow(tableUri, valueColumn, idColumn, idValue);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String uriAsString = cursor.getString(cursor.getColumnIndex(valueColumn));
                if (!TextUtils.isEmpty(uriAsString)) {
                    Uri parse = Uri.parse(uriAsString);
                    $closeResource(null, cursor);
                    return parse;
                }
                $closeResource(null, cursor);
                return null;
            }
        }
        throw new RemoteException("The row with (" + idColumn + " = " + idValue + ") could not be found in " + tableUri);
    }

    /* access modifiers changed from: package-private */
    public void updateValueOfProviderUri(Uri uri, String valueColumn, int value, String errorMessage) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(valueColumn, Integer.valueOf(value));
        performUpdate(uri, contentValues, errorMessage);
    }

    /* access modifiers changed from: package-private */
    public void updateValueOfProviderUri(Uri uri, String valueColumn, double value, String errorMessage) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(valueColumn, Double.valueOf(value));
        performUpdate(uri, contentValues, errorMessage);
    }

    /* access modifiers changed from: package-private */
    public void updateValueOfProviderUri(Uri uri, String valueColumn, long value, String errorMessage) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(valueColumn, Long.valueOf(value));
        performUpdate(uri, contentValues, errorMessage);
    }

    /* access modifiers changed from: package-private */
    public void updateValueOfProviderUri(Uri uri, String valueColumn, String value, String errorMessage) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(valueColumn, value);
        performUpdate(uri, contentValues, errorMessage);
    }

    /* access modifiers changed from: package-private */
    public void updateValueOfProviderUri(Uri uri, String valueColumn, Uri value, String errorMessage) throws RemoteException {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(valueColumn, value == null ? null : value.toString());
        performUpdate(uri, contentValues, errorMessage);
    }

    private void performUpdate(Uri uri, ContentValues contentValues, String errorMessage) throws RemoteException {
        if (this.mContentResolver.update(uri, contentValues, null, null) <= 0) {
            throw new RemoteException(errorMessage);
        }
    }

    private Cursor getValueFromTableRow(Uri tableUri, String valueColumn, String idColumn, int idValue) {
        ContentResolver contentResolver = this.mContentResolver;
        String[] strArr = {valueColumn};
        return contentResolver.query(tableUri, strArr, idColumn + "=?", new String[]{Integer.toString(idValue)}, null);
    }

    static Uri getMessageTableUri(boolean isIncoming) {
        return isIncoming ? Telephony.RcsColumns.RcsIncomingMessageColumns.INCOMING_MESSAGE_URI : Telephony.RcsColumns.RcsOutgoingMessageColumns.OUTGOING_MESSAGE_URI;
    }
}
