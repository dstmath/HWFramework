package com.android.internal.telephony;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class CallerInfoAsyncQuery {
    private static final boolean DBG = false;
    private static final boolean ENABLE_UNKNOWN_NUMBER_GEO_DESCRIPTION = true;
    private static final int EVENT_ADD_LISTENER = 2;
    private static final int EVENT_EMERGENCY_NUMBER = 4;
    private static final int EVENT_END_OF_QUEUE = 3;
    private static final int EVENT_GET_GEO_DESCRIPTION = 6;
    private static final int EVENT_NEW_QUERY = 1;
    private static final int EVENT_VOICEMAIL_NUMBER = 5;
    private static final String LOG_TAG = "CallerInfoAsyncQuery";
    private static final int MIN_MATCH = 7;
    private CallerInfoAsyncQueryHandler mHandler;

    private class CallerInfoAsyncQueryHandler extends AsyncQueryHandler {
        /* access modifiers changed from: private */
        public String compNum;
        /* access modifiers changed from: private */
        public CallerInfo mCallerInfo;
        /* access modifiers changed from: private */
        public Context mContext;
        private List<Runnable> mPendingListenerCallbacks;
        /* access modifiers changed from: private */
        public Uri mQueryUri;

        protected class CallerInfoWorkerHandler extends AsyncQueryHandler.WorkerHandler {
            public CallerInfoWorkerHandler(Looper looper) {
                super(CallerInfoAsyncQueryHandler.this, looper);
            }

            public void handleMessage(Message msg) {
                AsyncQueryHandler.WorkerArgs args = (AsyncQueryHandler.WorkerArgs) msg.obj;
                CookieWrapper cw = (CookieWrapper) args.cookie;
                if (cw == null) {
                    Rlog.i(CallerInfoAsyncQuery.LOG_TAG, "Unexpected command (CookieWrapper is null): " + msg.what + " ignored by CallerInfoWorkerHandler, passing onto parent.");
                    CallerInfoAsyncQueryHandler.super.handleMessage(msg);
                    return;
                }
                Rlog.d(CallerInfoAsyncQuery.LOG_TAG, "Processing event: " + cw.event + " token (arg1): " + msg.arg1 + " command: " + msg.what + " query URI: " + CallerInfoAsyncQuery.sanitizeUriToString(args.uri));
                switch (cw.event) {
                    case 1:
                        CallerInfoAsyncQueryHandler.super.handleMessage(msg);
                        return;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        Message reply = args.handler.obtainMessage(msg.what);
                        reply.obj = args;
                        reply.arg1 = msg.arg1;
                        reply.sendToTarget();
                        return;
                    case 6:
                        handleGeoDescription(msg);
                        return;
                    default:
                        return;
                }
            }

            private void handleGeoDescription(Message msg) {
                AsyncQueryHandler.WorkerArgs args = (AsyncQueryHandler.WorkerArgs) msg.obj;
                CookieWrapper cw = (CookieWrapper) args.cookie;
                if (!(TextUtils.isEmpty(cw.number) || cw.cookie == null || CallerInfoAsyncQueryHandler.this.mContext == null)) {
                    long startTimeMillis = SystemClock.elapsedRealtime();
                    cw.geoDescription = CallerInfo.getGeoDescription(CallerInfoAsyncQueryHandler.this.mContext, cw.number);
                    long elapsedRealtime = SystemClock.elapsedRealtime() - startTimeMillis;
                }
                Message reply = args.handler.obtainMessage(msg.what);
                reply.obj = args;
                reply.arg1 = msg.arg1;
                reply.sendToTarget();
            }
        }

        private CallerInfoAsyncQueryHandler(Context context) {
            super(CallerInfoAsyncQuery.getCurrentProfileContentResolver(context));
            this.mPendingListenerCallbacks = new ArrayList();
            this.mContext = context;
        }

        /* JADX WARNING: type inference failed for: r0v0, types: [android.os.Handler, com.android.internal.telephony.CallerInfoAsyncQuery$CallerInfoAsyncQueryHandler$CallerInfoWorkerHandler] */
        /* access modifiers changed from: protected */
        public Handler createHandler(Looper looper) {
            return new CallerInfoWorkerHandler(looper);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final int i = token;
            Cursor cursor2 = cursor;
            Rlog.d(CallerInfoAsyncQuery.LOG_TAG, "##### onQueryComplete() #####   query complete for token: " + i);
            final CookieWrapper cw = (CookieWrapper) cookie;
            if (cw == null) {
                Rlog.i(CallerInfoAsyncQuery.LOG_TAG, "Cookie is null, ignoring onQueryComplete() request.");
                if (cursor2 != null) {
                    cursor.close();
                }
            } else if (cw.event == 3) {
                for (Runnable r : this.mPendingListenerCallbacks) {
                    r.run();
                }
                this.mPendingListenerCallbacks.clear();
                CallerInfoAsyncQuery.this.release();
                if (cursor2 != null) {
                    cursor.close();
                }
            } else {
                if (cw.event == 6) {
                    if (this.mCallerInfo != null) {
                        this.mCallerInfo.geoDescription = cw.geoDescription;
                    }
                    CookieWrapper endMarker = new CookieWrapper();
                    endMarker.event = 3;
                    CookieWrapper cookieWrapper = endMarker;
                    startQuery(i, endMarker, null, null, null, null, null);
                }
                if (this.mCallerInfo == null) {
                    if (this.mContext == null || this.mQueryUri == null) {
                        throw new QueryPoolException("Bad context or query uri, or CallerInfoAsyncQuery already released.");
                    }
                    if (cw.event == 4) {
                        this.mCallerInfo = new CallerInfo().markAsEmergency(this.mContext);
                    } else if (cw.event == 5) {
                        this.mCallerInfo = new CallerInfo().markAsVoiceMail(cw.subId);
                    } else {
                        this.mCallerInfo = HwFrameworkFactory.getHwInnerTelephonyManager().getCallerInfo(this.mContext, this.mQueryUri, cursor2, this.compNum);
                        CallerInfo newCallerInfo = CallerInfo.doSecondaryLookupIfNecessary(this.mContext, cw.number, this.mCallerInfo);
                        if (newCallerInfo != this.mCallerInfo) {
                            this.mCallerInfo = newCallerInfo;
                        }
                        if (!TextUtils.isEmpty(cw.number)) {
                            this.mCallerInfo.phoneNumber = PhoneNumberUtils.formatNumber(cw.number, this.mCallerInfo.normalizedNumber, CallerInfo.getCurrentCountryIso(this.mContext));
                        }
                        if (TextUtils.isEmpty(this.mCallerInfo.name)) {
                            cw.event = 6;
                            CallerInfo callerInfo = newCallerInfo;
                            startQuery(i, cw, null, null, null, null, null);
                            return;
                        }
                    }
                    CookieWrapper endMarker2 = new CookieWrapper();
                    endMarker2.event = 3;
                    startQuery(i, endMarker2, null, null, null, null, null);
                }
                if (cw.listener != null) {
                    this.mPendingListenerCallbacks.add(new Runnable() {
                        public void run() {
                            cw.listener.onQueryComplete(i, cw.cookie, CallerInfoAsyncQueryHandler.this.mCallerInfo);
                        }
                    });
                } else {
                    Rlog.w(CallerInfoAsyncQuery.LOG_TAG, "There is no listener to notify for this query.");
                }
                if (cursor2 != null) {
                    cursor.close();
                }
            }
        }
    }

    private static final class CookieWrapper {
        public Object cookie;
        public int event;
        public String geoDescription;
        public OnQueryCompleteListener listener;
        public String number;
        public int subId;

        private CookieWrapper() {
        }
    }

    public interface OnQueryCompleteListener {
        void onQueryComplete(int i, Object obj, CallerInfo callerInfo);
    }

    public static class QueryPoolException extends SQLException {
        public QueryPoolException(String error) {
            super(error);
        }
    }

    static ContentResolver getCurrentProfileContentResolver(Context context) {
        int currentUser = ActivityManager.getCurrentUser();
        if (UserManager.get(context).getUserHandle() != currentUser) {
            try {
                return context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(currentUser)).getContentResolver();
            } catch (PackageManager.NameNotFoundException e) {
                Rlog.e(LOG_TAG, "Can't find self package", e);
            }
        }
        return context.getContentResolver();
    }

    private CallerInfoAsyncQuery() {
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, Uri contactRef, OnQueryCompleteListener listener, Object cookie) {
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = 1;
        c.mHandler.startQuery(token, cw, contactRef, null, null, null, null);
        return c;
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, OnQueryCompleteListener listener, Object cookie) {
        return startQuery(token, context, number, listener, cookie, SubscriptionManager.getDefaultSubscriptionId());
    }

    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number, OnQueryCompleteListener listener, Object cookie, int subId) {
        Context context2 = context;
        String str = number;
        int i = subId;
        String queryNum = Uri.encode(PhoneNumberUtils.normalizeNumber(number), "#");
        int len = queryNum == null ? 0 : queryNum.length();
        if (len > 7) {
            queryNum = queryNum.substring(len - 7);
        }
        Uri contactRef = ContactsContract.PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI.buildUpon().appendPath(queryNum).appendQueryParameter("sip", String.valueOf(PhoneNumberUtils.isUriNumber(number))).build();
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context2, contactRef, str);
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.number = str;
        cw.subId = i;
        if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
            cw.event = 4;
        } else if (PhoneNumberUtils.isVoiceMailNumber(context2, i, str)) {
            cw.event = 5;
        } else {
            cw.event = 1;
        }
        c.mHandler.startQuery(token, cw, contactRef, null, null, null, null);
        return c;
    }

    public void addQueryListener(int token, OnQueryCompleteListener listener, Object cookie) {
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = 2;
        this.mHandler.startQuery(token, cw, null, null, null, null, null);
    }

    private void allocate(Context context, Uri contactRef) {
        if (context == null || contactRef == null) {
            throw new QueryPoolException("Bad context or query uri.");
        }
        this.mHandler = new CallerInfoAsyncQueryHandler(context);
        Uri unused = this.mHandler.mQueryUri = contactRef;
    }

    private void allocate(Context context, Uri contactRef, String num) {
        allocate(context, contactRef);
        String unused = this.mHandler.compNum = num;
    }

    /* access modifiers changed from: private */
    public void release() {
        Context unused = this.mHandler.mContext = null;
        Uri unused2 = this.mHandler.mQueryUri = null;
        CallerInfo unused3 = this.mHandler.mCallerInfo = null;
        String unused4 = this.mHandler.compNum = null;
        this.mHandler = null;
    }

    /* access modifiers changed from: private */
    public static String sanitizeUriToString(Uri uri) {
        if (uri == null) {
            return "";
        }
        String uriString = uri.toString();
        int indexOfLastSlash = uriString.lastIndexOf(47);
        if (indexOfLastSlash <= 0) {
            return uriString;
        }
        return uriString.substring(0, indexOfLastSlash) + "/xxxxxxx";
    }
}
