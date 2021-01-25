package android.net.lowpan;

import android.net.lowpan.ILowpanEnergyScanCallback;
import android.net.lowpan.ILowpanNetScanCallback;
import android.net.lowpan.LowpanScanner;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LowpanScanner {
    private static final String TAG = LowpanScanner.class.getSimpleName();
    private ILowpanInterface mBinder;
    private Callback mCallback = null;
    private ArrayList<Integer> mChannelMask = null;
    private Handler mHandler = null;
    private int mTxPower = Integer.MAX_VALUE;

    public static abstract class Callback {
        public void onNetScanBeacon(LowpanBeaconInfo beacon) {
        }

        public void onEnergyScanResult(LowpanEnergyScanResult result) {
        }

        public void onScanFinished() {
        }
    }

    LowpanScanner(ILowpanInterface binder) {
        this.mBinder = binder;
    }

    public synchronized void setCallback(Callback cb, Handler handler) {
        this.mCallback = cb;
        this.mHandler = handler;
    }

    public void setCallback(Callback cb) {
        setCallback(cb, null);
    }

    public void setChannelMask(Collection<Integer> mask) {
        if (mask == null) {
            this.mChannelMask = null;
            return;
        }
        ArrayList<Integer> arrayList = this.mChannelMask;
        if (arrayList == null) {
            this.mChannelMask = new ArrayList<>();
        } else {
            arrayList.clear();
        }
        this.mChannelMask.addAll(mask);
    }

    public Collection<Integer> getChannelMask() {
        return (Collection) this.mChannelMask.clone();
    }

    public void addChannel(int channel) {
        if (this.mChannelMask == null) {
            this.mChannelMask = new ArrayList<>();
        }
        this.mChannelMask.add(Integer.valueOf(channel));
    }

    public void setTxPower(int txPower) {
        this.mTxPower = txPower;
    }

    public int getTxPower() {
        return this.mTxPower;
    }

    private Map<String, Object> createScanOptionMap() {
        Map<String, Object> map = new HashMap<>();
        if (this.mChannelMask != null) {
            LowpanProperties.KEY_CHANNEL_MASK.putInMap(map, this.mChannelMask.stream().mapToInt($$Lambda$LowpanScanner$b0nnjTe02JXonssLsm5Kp4EaFqs.INSTANCE).toArray());
        }
        if (this.mTxPower != Integer.MAX_VALUE) {
            LowpanProperties.KEY_MAX_TX_POWER.putInMap(map, Integer.valueOf(this.mTxPower));
        }
        return map;
    }

    public void startNetScan() throws LowpanException {
        try {
            this.mBinder.startNetScan(createScanOptionMap(), new ILowpanNetScanCallback.Stub() {
                /* class android.net.lowpan.LowpanScanner.AnonymousClass1 */

                @Override // android.net.lowpan.ILowpanNetScanCallback
                public void onNetScanBeacon(LowpanBeaconInfo beaconInfo) {
                    Callback callback;
                    Handler handler;
                    synchronized (LowpanScanner.this) {
                        callback = LowpanScanner.this.mCallback;
                        handler = LowpanScanner.this.mHandler;
                    }
                    if (callback != null) {
                        Runnable runnable = new Runnable(beaconInfo) {
                            /* class android.net.lowpan.$$Lambda$LowpanScanner$1$47buDsybUOrvvSl0JOZR_FC9ISg */
                            private final /* synthetic */ LowpanBeaconInfo f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                LowpanScanner.Callback.this.onNetScanBeacon(this.f$1);
                            }
                        };
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }

                @Override // android.net.lowpan.ILowpanNetScanCallback
                public void onNetScanFinished() {
                    Callback callback;
                    Handler handler;
                    synchronized (LowpanScanner.this) {
                        callback = LowpanScanner.this.mCallback;
                        handler = LowpanScanner.this.mHandler;
                    }
                    if (callback != null) {
                        Runnable runnable = new Runnable() {
                            /* class android.net.lowpan.$$Lambda$LowpanScanner$1$lUw1npYnRpaO9LS5odGyASQYaic */

                            @Override // java.lang.Runnable
                            public final void run() {
                                LowpanScanner.Callback.this.onScanFinished();
                            }
                        };
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }
            });
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void stopNetScan() {
        try {
            this.mBinder.stopNetScan();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }

    public void startEnergyScan() throws LowpanException {
        try {
            this.mBinder.startEnergyScan(createScanOptionMap(), new ILowpanEnergyScanCallback.Stub() {
                /* class android.net.lowpan.LowpanScanner.AnonymousClass2 */

                @Override // android.net.lowpan.ILowpanEnergyScanCallback
                public void onEnergyScanResult(int channel, int rssi) {
                    Callback callback = LowpanScanner.this.mCallback;
                    Handler handler = LowpanScanner.this.mHandler;
                    if (callback != null) {
                        Runnable runnable = new Runnable(channel, rssi) {
                            /* class android.net.lowpan.$$Lambda$LowpanScanner$2$GBDCgjndr24KQueHMX2qGNtfLPg */
                            private final /* synthetic */ int f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                LowpanScanner.AnonymousClass2.lambda$onEnergyScanResult$0(LowpanScanner.Callback.this, this.f$1, this.f$2);
                            }
                        };
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }

                static /* synthetic */ void lambda$onEnergyScanResult$0(Callback callback, int channel, int rssi) {
                    if (callback != null) {
                        LowpanEnergyScanResult result = new LowpanEnergyScanResult();
                        result.setChannel(channel);
                        result.setMaxRssi(rssi);
                        callback.onEnergyScanResult(result);
                    }
                }

                @Override // android.net.lowpan.ILowpanEnergyScanCallback
                public void onEnergyScanFinished() {
                    Callback callback = LowpanScanner.this.mCallback;
                    Handler handler = LowpanScanner.this.mHandler;
                    if (callback != null) {
                        Runnable runnable = new Runnable() {
                            /* class android.net.lowpan.$$Lambda$LowpanScanner$2$n8MSb22N9MEsazioSumvyQhW3Z4 */

                            @Override // java.lang.Runnable
                            public final void run() {
                                LowpanScanner.Callback.this.onScanFinished();
                            }
                        };
                        if (handler != null) {
                            handler.post(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }
            });
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        } catch (ServiceSpecificException x2) {
            throw LowpanException.rethrowFromServiceSpecificException(x2);
        }
    }

    public void stopEnergyScan() {
        try {
            this.mBinder.stopEnergyScan();
        } catch (RemoteException x) {
            throw x.rethrowAsRuntimeException();
        }
    }
}
