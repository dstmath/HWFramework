package android.hardware.display;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.NightDisplayListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import java.time.LocalTime;

public class NightDisplayListener {
    private Callback mCallback;
    private final ContentObserver mContentObserver;
    private final Context mContext;
    private final Handler mHandler;
    private final ColorDisplayManager mManager;
    private final int mUserId;

    public NightDisplayListener(Context context) {
        this(context, ActivityManager.getCurrentUser(), new Handler(Looper.getMainLooper()));
    }

    public NightDisplayListener(Context context, Handler handler) {
        this(context, ActivityManager.getCurrentUser(), handler);
    }

    public NightDisplayListener(Context context, int userId, Handler handler) {
        this.mContext = context.getApplicationContext();
        this.mManager = (ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class);
        this.mUserId = userId;
        this.mHandler = handler;
        this.mContentObserver = new ContentObserver(this.mHandler) {
            /* class android.hardware.display.NightDisplayListener.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                String setting = uri == null ? null : uri.getLastPathSegment();
                if (setting != null && NightDisplayListener.this.mCallback != null) {
                    char c = 65535;
                    switch (setting.hashCode()) {
                        case -2038150513:
                            if (setting.equals(Settings.Secure.NIGHT_DISPLAY_AUTO_MODE)) {
                                c = 1;
                                break;
                            }
                            break;
                        case -1761668069:
                            if (setting.equals(Settings.Secure.NIGHT_DISPLAY_CUSTOM_END_TIME)) {
                                c = 3;
                                break;
                            }
                            break;
                        case -969458956:
                            if (setting.equals(Settings.Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 800115245:
                            if (setting.equals(Settings.Secure.NIGHT_DISPLAY_ACTIVATED)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 1578271348:
                            if (setting.equals(Settings.Secure.NIGHT_DISPLAY_CUSTOM_START_TIME)) {
                                c = 2;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        NightDisplayListener.this.mCallback.onActivated(NightDisplayListener.this.mManager.isNightDisplayActivated());
                    } else if (c == 1) {
                        NightDisplayListener.this.mCallback.onAutoModeChanged(NightDisplayListener.this.mManager.getNightDisplayAutoMode());
                    } else if (c == 2) {
                        NightDisplayListener.this.mCallback.onCustomStartTimeChanged(NightDisplayListener.this.mManager.getNightDisplayCustomStartTime());
                    } else if (c == 3) {
                        NightDisplayListener.this.mCallback.onCustomEndTimeChanged(NightDisplayListener.this.mManager.getNightDisplayCustomEndTime());
                    } else if (c == 4) {
                        NightDisplayListener.this.mCallback.onColorTemperatureChanged(NightDisplayListener.this.mManager.getNightDisplayColorTemperature());
                    }
                }
            }
        };
    }

    public void setCallback(Callback callback) {
        if (Looper.myLooper() != this.mHandler.getLooper()) {
            this.mHandler.post(new Runnable(callback) {
                /* class android.hardware.display.$$Lambda$NightDisplayListener$sOK1HmSbMnFLzc4SdDD1WpVWJiI */
                private final /* synthetic */ NightDisplayListener.Callback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    NightDisplayListener.this.lambda$setCallback$0$NightDisplayListener(this.f$1);
                }
            });
        }
        lambda$setCallback$0$NightDisplayListener(callback);
    }

    /* access modifiers changed from: private */
    /* renamed from: setCallbackInternal */
    public void lambda$setCallback$0$NightDisplayListener(Callback newCallback) {
        Callback oldCallback = this.mCallback;
        if (oldCallback != newCallback) {
            this.mCallback = newCallback;
            if (this.mCallback == null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            } else if (oldCallback == null) {
                ContentResolver cr = this.mContext.getContentResolver();
                cr.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_ACTIVATED), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_AUTO_MODE), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_CUSTOM_START_TIME), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_CUSTOM_END_TIME), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE), false, this.mContentObserver, this.mUserId);
            }
        }
    }

    public interface Callback {
        default void onActivated(boolean activated) {
        }

        default void onAutoModeChanged(int autoMode) {
        }

        default void onCustomStartTimeChanged(LocalTime startTime) {
        }

        default void onCustomEndTimeChanged(LocalTime endTime) {
        }

        default void onColorTemperatureChanged(int colorTemperature) {
        }
    }
}
