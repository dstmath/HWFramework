package ohos.dcall;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.DisconnectCause;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public class CallAppAbilityProxy {
    private static final int CALL_CAPABILITY_OFFSET = 8;
    private static final int CALL_PROPERTY_MASK = 17;
    private static final int FLAG_EMPTY_BUNDLE = -1;
    private static final int LOG_DOMAIN_DCALL = 218111744;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218111744, TAG);
    private static final String TAG = "CallAppAbilityProxy";
    private static final int VALUE_TYPE_STRING = 8;
    private IBinder mRemote;
    private AospInCallService mService;

    public CallAppAbilityProxy(AospInCallService aospInCallService, IBinder iBinder) {
        this.mService = aospInCallService;
        this.mRemote = iBinder;
    }

    public void onCallAudioStateChanged(CallAudioState callAudioState) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallAudioStateChanged fail, no remote.", new Object[0]);
        } else if (callAudioState != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                this.mRemote.transact(1, writeCallAudioStateToParcel(obtain, callAudioState), obtain2, 0);
                HiLog.info(LOG_LABEL, "onCallAudioStateChanged success.", new Object[0]);
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onCallAudioStateChanged got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onCallAudioStateChanged fail, no audioState.", new Object[0]);
        }
    }

    public void onCallAdded(Call call) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallAdded fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                this.mRemote.transact(2, writeCallToParcel(obtain, call), obtain2, 0);
                HiLog.info(LOG_LABEL, "onCallAdded success: call is %{public}s", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onCallAdded got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onCallAdded fail, no call.", new Object[0]);
        }
    }

    public void onCallRemoved(Call call) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallRemoved fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                this.mRemote.transact(3, writeCallToParcel(obtain, call), obtain2, 0);
                HiLog.info(LOG_LABEL, "onCallRemoved success: call is %{public}s.", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onCallRemoved got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        }
    }

    public void onCanAddCallChanged(boolean z) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCanAddCallChanged fail, no remote.", new Object[0]);
            return;
        }
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeBoolean(z);
            this.mRemote.transact(4, obtain, obtain2, 0);
            HiLog.info(LOG_LABEL, "onCanAddCallChanged success: canAddCall is %{public}s.", new Object[]{Boolean.valueOf(z)});
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "onCanAddCallChanged got RemoteException.", new Object[0]);
        } catch (Throwable th) {
            obtain2.recycle();
            obtain.recycle();
            throw th;
        }
        obtain2.recycle();
        obtain.recycle();
    }

    public void onSilenceRinger() {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onSilenceRinger fail, no remote.", new Object[0]);
            return;
        }
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            this.mRemote.transact(5, obtain, obtain2, 0);
            HiLog.info(LOG_LABEL, "onSilenceRinger success.", new Object[0]);
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "onSilenceRinger got RemoteException.", new Object[0]);
        } catch (Throwable th) {
            obtain2.recycle();
            obtain.recycle();
            throw th;
        }
        obtain2.recycle();
        obtain.recycle();
    }

    public void onStateChanged(Call call, int i) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onStateChanged fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain = writeCallToParcel(obtain, call);
                obtain.writeInt(i);
                this.mRemote.transact(6, obtain, obtain2, 0);
                HiLog.info(LOG_LABEL, "onStateChanged: call = %{public}s, state = %{public}s", new Object[]{call, Integer.valueOf(i)});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onStateChanged got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onStateChanged fail, no call.", new Object[0]);
        }
    }

    public void onDetailsChanged(Call call, Call.Details details) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onDetailsChanged fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                this.mRemote.transact(7, writeCallToParcel(obtain, call), obtain2, 0);
                HiLog.info(LOG_LABEL, "onDetailsChanged success: call is %{public}s", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onDetailsChanged got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onDetailsChanged fail, no call.", new Object[0]);
        }
    }

    public void onPostDialWait(Call call, String str) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onPostDialWait fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain = writeCallToParcel(obtain, call);
                obtain.writeString(str);
                this.mRemote.transact(8, obtain, obtain2, 0);
                HiLog.info(LOG_LABEL, "onPostDialWait: call is %{public}s.", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onPostDialWait got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onPostDialWait fail, no call.", new Object[0]);
        }
    }

    public void onCallDestroyed(Call call) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onCallDestroyed fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain = writeCallToParcel(obtain, call);
                this.mRemote.transact(9, obtain, obtain2, 0);
                HiLog.info(LOG_LABEL, "onCallDestroyed: call is %{public}s.", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onCallDestroyed got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onCallDestroyed fail, no call.", new Object[0]);
        }
    }

    public void onConnectionEvent(Call call, String str, Bundle bundle) {
        if (this.mRemote == null) {
            HiLog.error(LOG_LABEL, "onConnectionEvent fail, no remote.", new Object[0]);
        } else if (call != null) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain = writeCallToParcel(obtain, call);
                obtain.writeString(str);
                writeBundleToParcel(obtain, bundle);
                this.mRemote.transact(10, obtain, obtain2, 0);
                HiLog.info(LOG_LABEL, "onConnectionEvent: call = %{public}s.", new Object[]{call});
            } catch (RemoteException unused) {
                HiLog.error(LOG_LABEL, "onConnectionEvent got RemoteException.", new Object[0]);
            } catch (Throwable th) {
                obtain2.recycle();
                obtain.recycle();
                throw th;
            }
            obtain2.recycle();
            obtain.recycle();
        } else {
            HiLog.error(LOG_LABEL, "onConnectionEvent fail, no call.", new Object[0]);
        }
    }

    private Parcel writeCallToParcel(Parcel parcel, Call call) {
        String str;
        if (!(parcel == null || call == null || call.getDetails() == null || this.mService == null)) {
            Call.Details details = call.getDetails();
            parcel.writeInt(this.mService.getIdByCall(call).intValue());
            parcel.writeInt(call.getState());
            writeDisconnectCauseToParcel(parcel, details.getDisconnectCause());
            parcel.writeInt((details.getCallCapabilities() << 8) | (details.getCallProperties() & 17));
            parcel.writeLong(details.getConnectTimeMillis());
            Uri handle = details.getHandle();
            if (handle == null) {
                str = "";
            } else {
                str = handle.getSchemeSpecificPart();
            }
            parcel.writeString(str);
            parcel.writeInt(details.getHandlePresentation());
            parcel.writeString(details.getCallerDisplayName());
            parcel.writeInt(details.getCallerDisplayNamePresentation());
            writeBundleToParcel(parcel, details.getIntentExtras());
            writeBundleToParcel(parcel, details.getExtras());
            parcel.writeLong(details.getCreationTimeMillis());
            parcel.writeInt(details.getCallDirection());
        }
        return parcel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:22:? A[RETURN, SYNTHETIC] */
    private void writeBundleToParcel(Parcel parcel, Bundle bundle) {
        if (parcel != null) {
            boolean z = true;
            if (bundle == null) {
                HiLog.info(LOG_LABEL, "writeBundleToParcel: bundle is null.", new Object[0]);
            } else {
                Object clone = bundle.clone();
                if (clone instanceof Bundle) {
                    Bundle bundle2 = (Bundle) clone;
                    try {
                        Method declaredMethod = bundle2.getClass().getSuperclass().getDeclaredMethod("getMap", new Class[0]);
                        declaredMethod.setAccessible(true);
                        Map<String, Object> convertToSpecificMap = convertToSpecificMap(declaredMethod.invoke(bundle2, new Object[0]));
                        if (convertToSpecificMap == null || convertToSpecificMap.entrySet() == null) {
                            HiLog.error(LOG_LABEL, "writeBundleToParcel: bundleMap is null or empty.", new Object[0]);
                        } else {
                            writeSpecificMapToParcel(parcel, convertToSpecificMap);
                            if (z) {
                                parcel.writeInt(-1);
                                return;
                            }
                            return;
                        }
                    } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
                        HiLog.error(LOG_LABEL, "writeBundleToParcel: fail to get ArrayMap from bundle!", new Object[0]);
                    }
                } else {
                    HiLog.error(LOG_LABEL, "writeBundleToParcel: Bundle clone fail.", new Object[0]);
                }
            }
            z = false;
            if (z) {
            }
        }
    }

    private Map<String, Object> convertToSpecificMap(Object obj) {
        HashMap hashMap = new HashMap();
        if (obj instanceof Map) {
            for (Map.Entry entry : ((Map) obj).entrySet()) {
                if (entry.getKey() instanceof String) {
                    hashMap.put((String) String.class.cast(entry.getKey()), entry.getValue());
                }
            }
        }
        return hashMap;
    }

    private void writeSpecificMapToParcel(Parcel parcel, Map<String, Object> map) {
        if (!(parcel == null || map == null)) {
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            if (entrySet == null) {
                HiLog.error(LOG_LABEL, "writeSpecificMapToParcel: entries is null.", new Object[0]);
                return;
            }
            parcel.writeInt(entrySet.size());
            for (Map.Entry<String, Object> entry : entrySet) {
                parcel.writeInt(8);
                parcel.writeString(entry.getKey());
                parcel.writeInt(8);
                if (entry.getValue() != null) {
                    parcel.writeString(entry.getValue().toString());
                } else {
                    parcel.writeString("");
                }
            }
        }
    }

    private Parcel writeCallAudioStateToParcel(Parcel parcel, CallAudioState callAudioState) {
        if (!(parcel == null || callAudioState == null)) {
            parcel.writeByte(callAudioState.isMuted() ? (byte) 1 : 0);
            parcel.writeInt(callAudioState.getRoute());
            parcel.writeInt(callAudioState.getSupportedRouteMask());
        }
        return parcel;
    }

    private Parcel writeDisconnectCauseToParcel(Parcel parcel, DisconnectCause disconnectCause) {
        if (!(parcel == null || disconnectCause == null)) {
            int code = disconnectCause.getCode();
            CharSequence label = disconnectCause.getLabel();
            String str = null;
            String charSequence = label != null ? label.toString() : null;
            CharSequence description = disconnectCause.getDescription();
            if (description != null) {
                str = description.toString();
            }
            String reason = disconnectCause.getReason();
            HiLog.info(LOG_LABEL, "writeDisconnectCauseToParcel: disconnectCode is %{public}s,disconnectLabel is %{public}s, disconnectDescription is %{public}s, disconnectReason is %{public}s", new Object[]{Integer.valueOf(code), charSequence, str, reason});
            parcel.writeInt(code);
            parcel.writeString(charSequence);
            parcel.writeString(str);
            parcel.writeString(reason);
        }
        return parcel;
    }
}
