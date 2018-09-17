package com.android.server.input;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;

public class HwCustInputManagerServiceImpl extends HwCustInputManagerService {
    public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    protected static boolean HWDBG = false;
    protected static boolean HWFLOW = false;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustInputManagerServiceImpl";
    private static final String TAG_FLOW = "HwCustInputManagerServiceImpl_FLOW";
    private static final String TAG_INIT = "HwCustInputManagerServiceImpl_INIT";
    private final String DB_GLOVE_FILE_NODE;
    private final String GLOVE_MODE_FILE_PATH;
    Context mContext;
    private UserSwitchingReceiver mReceiver;

    private class GloveModeObserver extends ContentObserver {
        public GloveModeObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            HwCustInputManagerServiceImpl.this.setGloveMode();
        }
    }

    class UserSwitchingReceiver extends BroadcastReceiver {
        UserSwitchingReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwCustInputManagerServiceImpl.TAG, "Intent is null.");
                return;
            }
            if (HwCustInputManagerServiceImpl.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                Log.d(HwCustInputManagerServiceImpl.TAG, "receive ACTION_USER_SWITCHED");
                HwCustInputManagerServiceImpl.this.setGloveMode();
            }
        }
    }

    static {
        HWDBG = HWLOGW_E;
        HWFLOW = HWLOGW_E;
    }

    public HwCustInputManagerServiceImpl(Object obj) {
        super(obj);
        this.GLOVE_MODE_FILE_PATH = "/sys/touchscreen/touch_glove";
        this.DB_GLOVE_FILE_NODE = "glove_file_node";
    }

    private void setGloveMode() {
        SecurityException e;
        Exception e2;
        Throwable th;
        if (this.mContext != null) {
            int isGloveMode = System.getIntForUser(this.mContext.getContentResolver(), "glove_file_node", 0, ActivityManager.getCurrentUser());
            if (HWDBG) {
                Log.d(TAG, "setGloveMode:" + isGloveMode);
            }
            String value = String.valueOf(isGloveMode);
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream fileOutWriteMode = new FileOutputStream(new File(SystemProperties.get("ro.config.glove_file_path", "/sys/touchscreen/touch_glove")));
                try {
                    fileOutWriteMode.write(value.getBytes("utf-8"));
                    if (fileOutWriteMode != null) {
                        try {
                            fileOutWriteMode.close();
                        } catch (SecurityException e3) {
                            e3.printStackTrace();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    fileOutputStream = fileOutWriteMode;
                } catch (SecurityException e4) {
                    e3 = e4;
                    fileOutputStream = fileOutWriteMode;
                    e3.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (SecurityException e32) {
                            e32.printStackTrace();
                        } catch (Exception e222) {
                            e222.printStackTrace();
                        }
                    }
                } catch (Exception e5) {
                    e222 = e5;
                    fileOutputStream = fileOutWriteMode;
                    try {
                        e222.printStackTrace();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (SecurityException e322) {
                                e322.printStackTrace();
                            } catch (Exception e2222) {
                                e2222.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (SecurityException e3222) {
                                e3222.printStackTrace();
                            } catch (Exception e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutWriteMode;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (SecurityException e6) {
                e3222 = e6;
                e3222.printStackTrace();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e7) {
                e22222 = e7;
                e22222.printStackTrace();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
    }

    public int registerContentObserverForSetGloveMode(Context context) {
        if (HWDBG) {
            Log.d(TAG, "registerContentObserverForSetGloveMode 1");
        }
        this.mContext = context;
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("glove_file_node"), false, new GloveModeObserver(), -1);
            IntentFilter filter = new IntentFilter(ACTION_USER_SWITCHED);
            this.mReceiver = new UserSwitchingReceiver();
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
        return 1;
    }
}
