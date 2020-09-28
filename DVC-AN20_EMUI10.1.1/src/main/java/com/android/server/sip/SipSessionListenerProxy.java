package com.android.server.sip;

import android.net.sip.ISipSession;
import android.net.sip.ISipSessionListener;
import android.net.sip.SipProfile;
import android.os.DeadObjectException;
import android.telephony.Rlog;

class SipSessionListenerProxy extends ISipSessionListener.Stub {
    private static final String TAG = "SipSessionListnerProxy";
    private ISipSessionListener mListener;

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

    @Override // android.net.sip.ISipSessionListener
    public void onCalling(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass1 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCalling(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCalling()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRinging(final ISipSession session, final SipProfile caller, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass2 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRinging(session, caller, sessionDescription);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRinging()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRingingBack(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass3 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRingingBack(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRingingBack()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onCallEstablished(final ISipSession session, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass4 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCallEstablished(session, sessionDescription);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCallEstablished()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onCallEnded(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass5 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCallEnded(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCallEnded()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onCallTransferring(final ISipSession newSession, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass6 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCallTransferring(newSession, sessionDescription);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCallTransferring()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onCallBusy(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass7 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCallBusy(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCallBusy()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onCallChangeFailed(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass8 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onCallChangeFailed(session, errorCode, message);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onCallChangeFailed()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onError(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass9 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onError(session, errorCode, message);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onError()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRegistering(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass10 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRegistering(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRegistering()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRegistrationDone(final ISipSession session, final int duration) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass11 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRegistrationDone(session, duration);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRegistrationDone()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRegistrationFailed(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass12 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRegistrationFailed(session, errorCode, message);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRegistrationFailed()");
                    }
                }
            });
        }
    }

    @Override // android.net.sip.ISipSessionListener
    public void onRegistrationTimeout(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
                /* class com.android.server.sip.SipSessionListenerProxy.AnonymousClass13 */

                public void run() {
                    try {
                        SipSessionListenerProxy.this.mListener.onRegistrationTimeout(session);
                    } catch (Throwable t) {
                        SipSessionListenerProxy.this.handle(t, "onRegistrationTimeout()");
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
