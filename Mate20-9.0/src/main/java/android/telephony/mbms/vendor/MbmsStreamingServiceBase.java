package android.telephony.mbms.vendor;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.mbms.IMbmsStreamingSessionCallback;
import android.telephony.mbms.IStreamingServiceCallback;
import android.telephony.mbms.MbmsStreamingSessionCallback;
import android.telephony.mbms.StreamingServiceCallback;
import android.telephony.mbms.StreamingServiceInfo;
import android.telephony.mbms.vendor.IMbmsStreamingService;
import java.util.List;

@SystemApi
public class MbmsStreamingServiceBase extends IMbmsStreamingService.Stub {
    public int initialize(MbmsStreamingSessionCallback callback, int subscriptionId) throws RemoteException {
        return 0;
    }

    public final int initialize(final IMbmsStreamingSessionCallback callback, final int subscriptionId) throws RemoteException {
        if (callback != null) {
            final int uid = Binder.getCallingUid();
            int result = initialize((MbmsStreamingSessionCallback) new MbmsStreamingSessionCallback() {
                public void onError(int errorCode, String message) {
                    if (errorCode != -1) {
                        try {
                            callback.onError(errorCode, message);
                        } catch (RemoteException e) {
                            MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                        }
                    } else {
                        throw new IllegalArgumentException("Middleware cannot send an unknown error.");
                    }
                }

                public void onStreamingServicesUpdated(List<StreamingServiceInfo> services) {
                    try {
                        callback.onStreamingServicesUpdated(services);
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }

                public void onMiddlewareReady() {
                    try {
                        callback.onMiddlewareReady();
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }
            }, subscriptionId);
            if (result == 0) {
                callback.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }, 0);
            }
            return result;
        }
        throw new NullPointerException("Callback must not be null");
    }

    public int requestUpdateStreamingServices(int subscriptionId, List<String> list) throws RemoteException {
        return 0;
    }

    public int startStreaming(int subscriptionId, String serviceId, StreamingServiceCallback callback) throws RemoteException {
        return 0;
    }

    public int startStreaming(final int subscriptionId, String serviceId, final IStreamingServiceCallback callback) throws RemoteException {
        if (callback != null) {
            final int uid = Binder.getCallingUid();
            int result = startStreaming(subscriptionId, serviceId, (StreamingServiceCallback) new StreamingServiceCallback() {
                public void onError(int errorCode, String message) {
                    if (errorCode != -1) {
                        try {
                            callback.onError(errorCode, message);
                        } catch (RemoteException e) {
                            MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                        }
                    } else {
                        throw new IllegalArgumentException("Middleware cannot send an unknown error.");
                    }
                }

                public void onStreamStateUpdated(int state, int reason) {
                    try {
                        callback.onStreamStateUpdated(state, reason);
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }

                public void onMediaDescriptionUpdated() {
                    try {
                        callback.onMediaDescriptionUpdated();
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }

                public void onBroadcastSignalStrengthUpdated(int signalStrength) {
                    try {
                        callback.onBroadcastSignalStrengthUpdated(signalStrength);
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }

                public void onStreamMethodUpdated(int methodType) {
                    try {
                        callback.onStreamMethodUpdated(methodType);
                    } catch (RemoteException e) {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }
            });
            if (result == 0) {
                callback.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        MbmsStreamingServiceBase.this.onAppCallbackDied(uid, subscriptionId);
                    }
                }, 0);
            }
            return result;
        }
        throw new NullPointerException("Callback must not be null");
    }

    public Uri getPlaybackUri(int subscriptionId, String serviceId) throws RemoteException {
        return null;
    }

    public void stopStreaming(int subscriptionId, String serviceId) throws RemoteException {
    }

    public void dispose(int subscriptionId) throws RemoteException {
    }

    public void onAppCallbackDied(int uid, int subscriptionId) {
    }
}
