package com.huawei.media.scan;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class AudioFilterUtils {
    private static final String ALARMS = "alarms";
    private static final String ALARMS_PATH = "/system/media/audio/alarms/";
    private static final String AUDIO_FORMAT = ".ogg";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEL_AUDIO_LIST_FILE = "del_audio_list.xml";
    private static final String NOTIFICATIONS = "notifications";
    private static final String NOTIFICATIONS_PATH = "/system/media/audio/notifications/";
    private static final String RINGTONES = "ringtones";
    private static final String RINGTONES_PATH = "/system/media/audio/ringtones/";
    private static final String TAG = "AudioFilterUtils";
    private static final String UI = "ui";
    private static final String UI_PATH = "/system/media/audio/ui/";
    private HashSet<String> mDelRingtonesList = new HashSet<>();
    private boolean mIsAudioFilterLoad;

    AudioFilterUtils(Context context) {
        loadAudioFilterConfig(context);
    }

    private boolean loadAudioFilterConfig(Context context) {
        boolean z;
        synchronized (this) {
            z = true;
            if (!this.mIsAudioFilterLoad) {
                loadAudioFilterConfigFromCust();
                loadAudioFilterConfigFromCache(context);
                this.mIsAudioFilterLoad = true;
            }
            if (this.mDelRingtonesList.size() == 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean isNeedAudioFilter() {
        return this.mDelRingtonesList.size() != 0;
    }

    private void loadAudioFilterConfigFromCust() {
        ArrayList<File> files = HwCfgFilePolicy.getCfgFileList("xml/del_audio_list.xml", 0);
        int filesLen = files.size();
        for (int i = 0; i < filesLen; i++) {
            File file = files.get(i);
            if (file != null && file.exists()) {
                FileInputStream in = null;
                try {
                    FileInputStream in2 = new FileInputStream(file);
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(in2, null);
                    int eventType = xpp.getEventType();
                    while (eventType != 1) {
                        if (eventType != 2) {
                            eventType = xpp.next();
                        } else {
                            if (ALARMS.equals(xpp.getName())) {
                                HashSet<String> hashSet = this.mDelRingtonesList;
                                hashSet.add(ALARMS_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (NOTIFICATIONS.equals(xpp.getName())) {
                                HashSet<String> hashSet2 = this.mDelRingtonesList;
                                hashSet2.add(NOTIFICATIONS_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (RINGTONES.equals(xpp.getName())) {
                                HashSet<String> hashSet3 = this.mDelRingtonesList;
                                hashSet3.add(RINGTONES_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (UI.equals(xpp.getName())) {
                                HashSet<String> hashSet4 = this.mDelRingtonesList;
                                hashSet4.add(UI_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (DEBUG) {
                                Log.w(TAG, "No event type could be met.");
                            }
                            eventType = xpp.next();
                        }
                    }
                    try {
                        in2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException in loadAudioFilterConfigFromCust");
                    }
                } catch (XmlPullParserException e2) {
                    Log.w(TAG, "failed to load audio filter config from cust, parser exception");
                    if (0 != 0) {
                        in.close();
                    }
                } catch (IOException e3) {
                    Log.w(TAG, "failed to load audio filter config from cust, io exception");
                    if (0 != 0) {
                        in.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            in.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "IOException in loadAudioFilterConfigFromCust");
                        }
                    }
                    throw th;
                }
            }
        }
    }

    private void loadAudioFilterConfigFromCache(Context context) {
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        try {
            FileInputStream inputStream2 = context.openFileInput(DEL_AUDIO_LIST_FILE);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2, "UTF-8"));
            for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                this.mDelRingtonesList.add(line);
            }
            try {
                reader2.close();
                if (inputStream2 != null) {
                    inputStream2.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException in loadAudioFilterConfigFromCache");
            }
        } catch (FileNotFoundException e2) {
            Log.w(TAG, "failed to load audio filter config from cache, file not found exception");
            if (0 != 0) {
                reader.close();
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (IOException e3) {
            Log.w(TAG, "failed to load audio filter config from cache, io exception");
            if (0 != 0) {
                reader.close();
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Log.e(TAG, "IOException in loadAudioFilterConfigFromCache");
                    throw th;
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
            throw th;
        }
    }

    public boolean isAudioFilterFile(String path) {
        boolean contains;
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        synchronized (this) {
            contains = this.mDelRingtonesList.contains(path);
        }
        return contains;
    }
}
