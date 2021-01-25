package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.UserHandle;
import android.provider.Telephony;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.util.Date;

public class CarrierInfoManager {
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String LOG_TAG = "CarrierInfoManager";
    private static final int RESET_CARRIER_KEY_RATE_LIMIT = 43200000;
    private long mLastAccessResetCarrierKey = 0;

    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0108, code lost:
        if (r1 != null) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x010a, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0115, code lost:
        if (r1 == null) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0118, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x011b  */
    public static ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType, Context context) {
        Throwable th;
        Cursor findCursor;
        String simOperator = ((TelephonyManager) context.getSystemService("phone")).getSimOperator();
        if (!TextUtils.isEmpty(simOperator)) {
            String mcc = simOperator.substring(0, 3);
            String mnc = simOperator.substring(3);
            Log.i(LOG_TAG, "using values for mnc, mcc: " + mnc + "," + mcc);
            Cursor findCursor2 = null;
            try {
                Cursor findCursor3 = context.getContentResolver().query(Telephony.CarrierColumns.CONTENT_URI, new String[]{"public_key", "expiration_time", "key_identifier"}, "mcc=? and mnc=? and key_type=?", new String[]{mcc, mnc, String.valueOf(keyType)}, null);
                if (findCursor3 != null) {
                    try {
                        if (!findCursor3.moveToFirst()) {
                            findCursor = findCursor3;
                        } else {
                            if (findCursor3.getCount() > 1) {
                                try {
                                    Log.e(LOG_TAG, "More than 1 row found for the keyType: " + keyType);
                                } catch (IllegalArgumentException e) {
                                    findCursor2 = findCursor3;
                                } catch (Exception e2) {
                                    findCursor2 = findCursor3;
                                    try {
                                        Log.e(LOG_TAG, "Query failed.");
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    findCursor2 = findCursor3;
                                    if (findCursor2 != null) {
                                    }
                                    throw th;
                                }
                            }
                            try {
                                ImsiEncryptionInfo imsiEncryptionInfo = new ImsiEncryptionInfo(mcc, mnc, keyType, findCursor3.getString(2), findCursor3.getBlob(0), new Date(findCursor3.getLong(1)));
                                findCursor3.close();
                                return imsiEncryptionInfo;
                            } catch (IllegalArgumentException e3) {
                                findCursor2 = findCursor3;
                                Log.e(LOG_TAG, "Bad arguments.");
                            } catch (Exception e4) {
                                findCursor2 = findCursor3;
                                Log.e(LOG_TAG, "Query failed.");
                            } catch (Throwable th4) {
                                th = th4;
                                findCursor2 = findCursor3;
                                if (findCursor2 != null) {
                                    findCursor2.close();
                                }
                                throw th;
                            }
                        }
                    } catch (IllegalArgumentException e5) {
                        findCursor2 = findCursor3;
                        Log.e(LOG_TAG, "Bad arguments.");
                    } catch (Exception e6) {
                        findCursor2 = findCursor3;
                        Log.e(LOG_TAG, "Query failed.");
                    } catch (Throwable th5) {
                        th = th5;
                        findCursor2 = findCursor3;
                        if (findCursor2 != null) {
                        }
                        throw th;
                    }
                } else {
                    findCursor = findCursor3;
                }
                Log.d(LOG_TAG, "No rows found for keyType: " + keyType);
                if (findCursor != null) {
                    findCursor.close();
                }
                return null;
            } catch (IllegalArgumentException e7) {
                Log.e(LOG_TAG, "Bad arguments.");
            } catch (Exception e8) {
                Log.e(LOG_TAG, "Query failed.");
            }
        } else {
            Log.e(LOG_TAG, "Invalid networkOperator: " + simOperator);
            return null;
        }
    }

    public static void updateOrInsertCarrierKey(ImsiEncryptionInfo imsiEncryptionInfo, Context context, int phoneId) {
        byte[] keyBytes = imsiEncryptionInfo.getPublicKey().getEncoded();
        ContentResolver mContentResolver = context.getContentResolver();
        TelephonyMetrics tm = TelephonyMetrics.getInstance();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mcc", imsiEncryptionInfo.getMcc());
        contentValues.put("mnc", imsiEncryptionInfo.getMnc());
        contentValues.put("key_type", Integer.valueOf(imsiEncryptionInfo.getKeyType()));
        contentValues.put("key_identifier", imsiEncryptionInfo.getKeyIdentifier());
        contentValues.put("public_key", keyBytes);
        contentValues.put("expiration_time", Long.valueOf(imsiEncryptionInfo.getExpirationTime().getTime()));
        boolean downloadSuccessfull = true;
        try {
            Log.i(LOG_TAG, "Inserting imsiEncryptionInfo into db");
            mContentResolver.insert(Telephony.CarrierColumns.CONTENT_URI, contentValues);
        } catch (SQLiteConstraintException e) {
            Log.i(LOG_TAG, "Insert failed, updating imsiEncryptionInfo into db");
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("public_key", keyBytes);
            updatedValues.put("expiration_time", Long.valueOf(imsiEncryptionInfo.getExpirationTime().getTime()));
            updatedValues.put("key_identifier", imsiEncryptionInfo.getKeyIdentifier());
            try {
                if (mContentResolver.update(Telephony.CarrierColumns.CONTENT_URI, updatedValues, "mcc=? and mnc=? and key_type=?", new String[]{imsiEncryptionInfo.getMcc(), imsiEncryptionInfo.getMnc(), String.valueOf(imsiEncryptionInfo.getKeyType())}) == 0) {
                    Log.d(LOG_TAG, "Error updating values:" + imsiEncryptionInfo);
                    downloadSuccessfull = false;
                }
            } catch (Exception e2) {
                Log.d(LOG_TAG, "Error updating values:" + imsiEncryptionInfo);
                downloadSuccessfull = false;
            }
        } catch (Exception e3) {
            Log.d(LOG_TAG, "Error inserting/updating values:" + imsiEncryptionInfo);
            downloadSuccessfull = false;
        } catch (Throwable th) {
            tm.writeCarrierKeyEvent(phoneId, imsiEncryptionInfo.getKeyType(), true);
            throw th;
        }
        tm.writeCarrierKeyEvent(phoneId, imsiEncryptionInfo.getKeyType(), downloadSuccessfull);
    }

    public static void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo, Context context, int phoneId) {
        Log.i(LOG_TAG, "inserting carrier key: " + imsiEncryptionInfo);
        updateOrInsertCarrierKey(imsiEncryptionInfo, context, phoneId);
    }

    public void resetCarrierKeysForImsiEncryption(Context context, int mPhoneId) {
        Log.i(LOG_TAG, "resetting carrier key");
        long now = System.currentTimeMillis();
        if (now - this.mLastAccessResetCarrierKey < 43200000) {
            Log.i(LOG_TAG, "resetCarrierKeysForImsiEncryption: Access rate exceeded");
            return;
        }
        this.mLastAccessResetCarrierKey = now;
        deleteCarrierInfoForImsiEncryption(context);
        Intent resetIntent = new Intent("com.android.internal.telephony.ACTION_CARRIER_CERTIFICATE_DOWNLOAD");
        resetIntent.putExtra("phone", mPhoneId);
        context.sendBroadcastAsUser(resetIntent, UserHandle.ALL);
    }

    public static void deleteCarrierInfoForImsiEncryption(Context context) {
        Log.i(LOG_TAG, "deleting carrier key from db");
        String simOperator = ((TelephonyManager) context.getSystemService("phone")).getSimOperator();
        if (!TextUtils.isEmpty(simOperator)) {
            try {
                context.getContentResolver().delete(Telephony.CarrierColumns.CONTENT_URI, "mcc=? and mnc=?", new String[]{simOperator.substring(0, 3), simOperator.substring(3)});
            } catch (Exception e) {
                Log.e(LOG_TAG, "Delete failed");
            }
        } else {
            Log.e(LOG_TAG, "Invalid networkOperator: " + simOperator);
        }
    }

    public static void deleteAllCarrierKeysForImsiEncryption(Context context) {
        Log.i(LOG_TAG, "deleting ALL carrier keys from db");
        try {
            context.getContentResolver().delete(Telephony.CarrierColumns.CONTENT_URI, null, null);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Delete failed");
        }
    }
}
