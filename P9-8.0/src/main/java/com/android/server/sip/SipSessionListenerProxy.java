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

    public void onCalling(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRinging(final ISipSession session, final SipProfile caller, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRingingBack(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onCallEstablished(final ISipSession session, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onCallEnded(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onCallTransferring(final ISipSession newSession, final String sessionDescription) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onCallBusy(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onCallChangeFailed(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onError(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRegistering(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRegistrationDone(final ISipSession session, final int duration) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRegistrationFailed(final ISipSession session, final int errorCode, final String message) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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

    public void onRegistrationTimeout(final ISipSession session) {
        if (this.mListener != null) {
            proxy(new Runnable() {
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
