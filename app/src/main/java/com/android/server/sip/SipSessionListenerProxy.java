package com.android.server.sip;

import android.net.sip.ISipSession;
import android.net.sip.ISipSessionListener;
import android.net.sip.ISipSessionListener.Stub;
import android.net.sip.SipProfile;
import android.os.DeadObjectException;
import android.telephony.Rlog;

class SipSessionListenerProxy extends Stub {
    private static final String TAG = "SipSessionListnerProxy";
    private ISipSessionListener mListener;

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.10 */
    class AnonymousClass10 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass10(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRegistering(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRegistering()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ int val$duration;
        final /* synthetic */ ISipSession val$session;

        AnonymousClass11(ISipSession val$session, int val$duration) {
            this.val$session = val$session;
            this.val$duration = val$duration;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRegistrationDone(this.val$session, this.val$duration);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRegistrationDone()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.12 */
    class AnonymousClass12 implements Runnable {
        final /* synthetic */ int val$errorCode;
        final /* synthetic */ String val$message;
        final /* synthetic */ ISipSession val$session;

        AnonymousClass12(ISipSession val$session, int val$errorCode, String val$message) {
            this.val$session = val$session;
            this.val$errorCode = val$errorCode;
            this.val$message = val$message;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRegistrationFailed(this.val$session, this.val$errorCode, this.val$message);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRegistrationFailed()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.13 */
    class AnonymousClass13 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass13(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRegistrationTimeout(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRegistrationTimeout()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass1(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCalling(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCalling()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ SipProfile val$caller;
        final /* synthetic */ ISipSession val$session;
        final /* synthetic */ String val$sessionDescription;

        AnonymousClass2(ISipSession val$session, SipProfile val$caller, String val$sessionDescription) {
            this.val$session = val$session;
            this.val$caller = val$caller;
            this.val$sessionDescription = val$sessionDescription;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRinging(this.val$session, this.val$caller, this.val$sessionDescription);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRinging()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass3(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onRingingBack(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onRingingBack()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ ISipSession val$session;
        final /* synthetic */ String val$sessionDescription;

        AnonymousClass4(ISipSession val$session, String val$sessionDescription) {
            this.val$session = val$session;
            this.val$sessionDescription = val$sessionDescription;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCallEstablished(this.val$session, this.val$sessionDescription);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCallEstablished()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass5(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCallEnded(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCallEnded()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ ISipSession val$newSession;
        final /* synthetic */ String val$sessionDescription;

        AnonymousClass6(ISipSession val$newSession, String val$sessionDescription) {
            this.val$newSession = val$newSession;
            this.val$sessionDescription = val$sessionDescription;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCallTransferring(this.val$newSession, this.val$sessionDescription);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCallTransferring()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ ISipSession val$session;

        AnonymousClass7(ISipSession val$session) {
            this.val$session = val$session;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCallBusy(this.val$session);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCallBusy()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ int val$errorCode;
        final /* synthetic */ String val$message;
        final /* synthetic */ ISipSession val$session;

        AnonymousClass8(ISipSession val$session, int val$errorCode, String val$message) {
            this.val$session = val$session;
            this.val$errorCode = val$errorCode;
            this.val$message = val$message;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onCallChangeFailed(this.val$session, this.val$errorCode, this.val$message);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onCallChangeFailed()");
            }
        }
    }

    /* renamed from: com.android.server.sip.SipSessionListenerProxy.9 */
    class AnonymousClass9 implements Runnable {
        final /* synthetic */ int val$errorCode;
        final /* synthetic */ String val$message;
        final /* synthetic */ ISipSession val$session;

        AnonymousClass9(ISipSession val$session, int val$errorCode, String val$message) {
            this.val$session = val$session;
            this.val$errorCode = val$errorCode;
            this.val$message = val$message;
        }

        public void run() {
            try {
                SipSessionListenerProxy.this.mListener.onError(this.val$session, this.val$errorCode, this.val$message);
            } catch (Throwable t) {
                SipSessionListenerProxy.this.handle(t, "onError()");
            }
        }
    }

    SipSessionListenerProxy() {
    }

    public void setListener(ISipSessionListener listener) {
        this.mListener = listener;
    }

    public ISipSessionListener getListener() {
        return this.mListener;
    }

    private void proxy(Runnable runnable) {
        new Thread(runnable, "SipSessionCallbackThread").start();
    }

    public void onCalling(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass1(session));
        }
    }

    public void onRinging(ISipSession session, SipProfile caller, String sessionDescription) {
        if (this.mListener != null) {
            proxy(new AnonymousClass2(session, caller, sessionDescription));
        }
    }

    public void onRingingBack(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass3(session));
        }
    }

    public void onCallEstablished(ISipSession session, String sessionDescription) {
        if (this.mListener != null) {
            proxy(new AnonymousClass4(session, sessionDescription));
        }
    }

    public void onCallEnded(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass5(session));
        }
    }

    public void onCallTransferring(ISipSession newSession, String sessionDescription) {
        if (this.mListener != null) {
            proxy(new AnonymousClass6(newSession, sessionDescription));
        }
    }

    public void onCallBusy(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass7(session));
        }
    }

    public void onCallChangeFailed(ISipSession session, int errorCode, String message) {
        if (this.mListener != null) {
            proxy(new AnonymousClass8(session, errorCode, message));
        }
    }

    public void onError(ISipSession session, int errorCode, String message) {
        if (this.mListener != null) {
            proxy(new AnonymousClass9(session, errorCode, message));
        }
    }

    public void onRegistering(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass10(session));
        }
    }

    public void onRegistrationDone(ISipSession session, int duration) {
        if (this.mListener != null) {
            proxy(new AnonymousClass11(session, duration));
        }
    }

    public void onRegistrationFailed(ISipSession session, int errorCode, String message) {
        if (this.mListener != null) {
            proxy(new AnonymousClass12(session, errorCode, message));
        }
    }

    public void onRegistrationTimeout(ISipSession session) {
        if (this.mListener != null) {
            proxy(new AnonymousClass13(session));
        }
    }

    private void handle(Throwable t, String message) {
        if (t instanceof DeadObjectException) {
            this.mListener = null;
        } else if (this.mListener != null) {
            loge(message, t);
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }

    private void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }
}
