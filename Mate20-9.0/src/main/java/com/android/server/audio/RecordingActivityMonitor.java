package com.android.server.audio;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hsm.HwSystemManager;
import android.media.AudioFormat;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.IRecordingConfigDispatcher;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Xml;
import com.android.server.audio.AudioEventLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class RecordingActivityMonitor implements AudioSystem.AudioRecordingCallback {
    public static final int AUDIO_TYPE = 1;
    private static final String CONFIG_FILE_WHITE_BLACK_APP = "/system/emui/base/xml/hw_Recordeffect_app_config.xml";
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String NODE_ATTR_PACKAGE = "package";
    private static final String NODE_SCENE1 = "voice_message_5px";
    private static final String NODE_SCENE10 = "default";
    private static final String NODE_SCENE2 = "video_message_5px";
    private static final String NODE_SCENE3 = "karaoke_5px";
    private static final String NODE_SCENE4 = "live_telecast_5px";
    private static final String NODE_SCENE5 = "default_5px";
    private static final String NODE_SCENE6 = "voice_message";
    private static final String NODE_SCENE7 = "video_message";
    private static final String NODE_SCENE8 = "karaoke";
    private static final String NODE_SCENE9 = "live_telecast";
    private static final String NODE_WHITEAPP = "whiteapp";
    public static final String TAG = "AudioService.RecordingActivityMonitor";
    private static final AudioEventLogger sEventLogger = new AudioEventLogger(50, "recording activity as reported through AudioSystem.AudioRecordingCallback");
    private ArrayList<RecMonitorClient> mClients = new ArrayList<>();
    private boolean mHasPublicClients = false;
    private ArrayList<String> mKaraokeWhiteList1 = null;
    private ArrayList<String> mKaraokeWhiteList10 = null;
    private ArrayList<String> mKaraokeWhiteList2 = null;
    private ArrayList<String> mKaraokeWhiteList3 = null;
    private ArrayList<String> mKaraokeWhiteList4 = null;
    private ArrayList<String> mKaraokeWhiteList5 = null;
    private ArrayList<String> mKaraokeWhiteList6 = null;
    private ArrayList<String> mKaraokeWhiteList7 = null;
    private ArrayList<String> mKaraokeWhiteList8 = null;
    private ArrayList<String> mKaraokeWhiteList9 = null;
    private final PackageManager mPackMan;
    private HashMap<Integer, AudioRecordingConfiguration> mRecordConfigs = new HashMap<>();
    private Integer whatsapp_session = 0;

    private static final class RecMonitorClient implements IBinder.DeathRecipient {
        static RecordingActivityMonitor sMonitor;
        final IRecordingConfigDispatcher mDispatcherCb;
        final boolean mIsPrivileged;

        RecMonitorClient(IRecordingConfigDispatcher rcdb, boolean isPrivileged) {
            this.mDispatcherCb = rcdb;
            this.mIsPrivileged = isPrivileged;
        }

        public void binderDied() {
            Log.w(RecordingActivityMonitor.TAG, "client died");
            sMonitor.unregisterRecordingCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(RecordingActivityMonitor.TAG, "Could not link to client death", e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    private static final class RecordingEvent extends AudioEventLogger.Event {
        private final int mClientUid;
        private final String mPackName;
        private final int mRecEvent;
        private final int mSession;
        private final int mSource;

        RecordingEvent(int event, int uid, int session, int source, String packName) {
            this.mRecEvent = event;
            this.mClientUid = uid;
            this.mSession = session;
            this.mSource = source;
            this.mPackName = packName;
        }

        public String eventToString() {
            String str;
            StringBuilder sb = new StringBuilder("rec ");
            sb.append(this.mRecEvent == 1 ? "start" : "stop ");
            sb.append(" uid:");
            sb.append(this.mClientUid);
            sb.append(" session:");
            sb.append(this.mSession);
            sb.append(" src:");
            sb.append(MediaRecorder.toLogFriendlyAudioSource(this.mSource));
            if (this.mPackName == null) {
                str = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            } else {
                str = " pack:" + this.mPackName;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    RecordingActivityMonitor(Context ctxt) {
        RecMonitorClient.sMonitor = this;
        this.mPackMan = ctxt.getPackageManager();
    }

    private void getAppInWhiteBlackList(List<String> whiteAppList1, List<String> whiteAppList2, List<String> whiteAppList3, List<String> whiteAppList4, List<String> whiteAppList5, List<String> whiteAppList6, List<String> whiteAppList7, List<String> whiteAppList8, List<String> whiteAppList9, List<String> whiteAppList10) {
        InputStream in = null;
        try {
            File configFile = new File(CONFIG_FILE_WHITE_BLACK_APP);
            if (DEBUG) {
                Log.v(TAG, "HwCfgFilePolicy get Record White List CfgFile not null, path = " + configFile.getPath());
            }
            InputStream in2 = new FileInputStream(configFile.getPath());
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(in2, null);
            if (xmlParser != null) {
                parseXmlForWhiteBlackList(xmlParser, whiteAppList1, whiteAppList2, whiteAppList3, whiteAppList4, whiteAppList5, whiteAppList6, whiteAppList7, whiteAppList8, whiteAppList9, whiteAppList10);
            }
            try {
                in2.close();
            } catch (IOException e) {
                IOException iOException = e;
                Log.e(TAG, "RecordWhiteList IO Close Fail");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "RecordWhiteList FileNotFoundException");
            if (in != null) {
                in.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "RecordWhiteList XmlPullParserException");
            if (in != null) {
                in.close();
            }
        } catch (Exception e4) {
            Log.e(TAG, "RecordWhiteList getAppInWhiteBlackList Exception ", e4);
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            InputStream in3 = in;
            Throwable th2 = th;
            if (in3 != null) {
                try {
                    in3.close();
                } catch (IOException e5) {
                    IOException iOException2 = e5;
                    Log.e(TAG, "RecordWhiteList IO Close Fail");
                }
            }
            throw th2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:123:0x0296, code lost:
        r16 = r0;
     */
    private void parseXmlForWhiteBlackList(XmlPullParser parser, List<String> whiteAppList1, List<String> whiteAppList2, List<String> whiteAppList3, List<String> whiteAppList4, List<String> whiteAppList5, List<String> whiteAppList6, List<String> whiteAppList7, List<String> whiteAppList8, List<String> whiteAppList9, List<String> whiteAppList10) {
        int scene_id;
        int scene_id2 = 0;
        while (true) {
            try {
                int next = parser.next();
                int eventType = next;
                if (next == 1) {
                    XmlPullParser xmlPullParser = parser;
                    List<String> list = whiteAppList1;
                    List<String> list2 = whiteAppList2;
                    List<String> list3 = whiteAppList3;
                    List<String> list4 = whiteAppList4;
                    List<String> list5 = whiteAppList5;
                    List<String> list6 = whiteAppList6;
                    List<String> list7 = whiteAppList7;
                    List<String> list8 = whiteAppList8;
                    List<String> list9 = whiteAppList9;
                    List<String> list10 = whiteAppList10;
                    return;
                } else if (eventType == 2) {
                    String nodeName = parser.getName();
                    if (nodeName.equals(NODE_SCENE1)) {
                        scene_id2 = 1;
                    } else if (nodeName.equals(NODE_SCENE2)) {
                        scene_id2 = 2;
                    } else if (nodeName.equals(NODE_SCENE3)) {
                        scene_id2 = 3;
                    } else if (nodeName.equals(NODE_SCENE4)) {
                        scene_id2 = 4;
                    } else if (nodeName.equals(NODE_SCENE5)) {
                        scene_id2 = 5;
                    } else if (nodeName.equals(NODE_SCENE6)) {
                        scene_id2 = 6;
                    } else if (nodeName.equals(NODE_SCENE7)) {
                        scene_id2 = 7;
                    } else if (nodeName.equals(NODE_SCENE8)) {
                        scene_id2 = 8;
                    } else if (nodeName.equals(NODE_SCENE9)) {
                        scene_id2 = 9;
                    } else if (nodeName.equals("default")) {
                        scene_id2 = 10;
                    }
                    if (nodeName.equals(NODE_WHITEAPP)) {
                        try {
                            String packageName = parser.getAttributeValue(null, "package");
                            try {
                                if (isValidCharSequence(packageName)) {
                                    switch (scene_id2) {
                                        case 1:
                                            List<String> list11 = whiteAppList2;
                                            List<String> list12 = whiteAppList3;
                                            List<String> list13 = whiteAppList4;
                                            List<String> list14 = whiteAppList5;
                                            List<String> list15 = whiteAppList6;
                                            List<String> list16 = whiteAppList7;
                                            List<String> list17 = whiteAppList8;
                                            List<String> list18 = whiteAppList9;
                                            List<String> list19 = whiteAppList10;
                                            try {
                                                whiteAppList1.add(packageName);
                                                break;
                                            } catch (XmlPullParserException e) {
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e2) {
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 2:
                                            List<String> list20 = whiteAppList3;
                                            List<String> list21 = whiteAppList4;
                                            List<String> list22 = whiteAppList5;
                                            List<String> list23 = whiteAppList6;
                                            List<String> list24 = whiteAppList7;
                                            List<String> list25 = whiteAppList8;
                                            List<String> list26 = whiteAppList9;
                                            List<String> list27 = whiteAppList10;
                                            try {
                                                whiteAppList2.add(packageName);
                                                List<String> list28 = whiteAppList1;
                                                break;
                                            } catch (XmlPullParserException e3) {
                                                List<String> list29 = whiteAppList1;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e4) {
                                                List<String> list30 = whiteAppList1;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 3:
                                            List<String> list31 = whiteAppList4;
                                            List<String> list32 = whiteAppList5;
                                            List<String> list33 = whiteAppList6;
                                            List<String> list34 = whiteAppList7;
                                            List<String> list35 = whiteAppList8;
                                            List<String> list36 = whiteAppList9;
                                            List<String> list37 = whiteAppList10;
                                            try {
                                                whiteAppList3.add(packageName);
                                                List<String> list38 = whiteAppList1;
                                                List<String> list39 = whiteAppList2;
                                                break;
                                            } catch (XmlPullParserException e5) {
                                                List<String> list40 = whiteAppList1;
                                                List<String> list41 = whiteAppList2;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e6) {
                                                List<String> list42 = whiteAppList1;
                                                List<String> list43 = whiteAppList2;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 4:
                                            List<String> list44 = whiteAppList5;
                                            List<String> list45 = whiteAppList6;
                                            List<String> list46 = whiteAppList7;
                                            List<String> list47 = whiteAppList8;
                                            List<String> list48 = whiteAppList9;
                                            List<String> list49 = whiteAppList10;
                                            try {
                                                whiteAppList4.add(packageName);
                                                List<String> list50 = whiteAppList1;
                                                List<String> list51 = whiteAppList2;
                                                List<String> list52 = whiteAppList3;
                                                break;
                                            } catch (XmlPullParserException e7) {
                                                List<String> list53 = whiteAppList1;
                                                List<String> list54 = whiteAppList2;
                                                List<String> list55 = whiteAppList3;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e8) {
                                                List<String> list56 = whiteAppList1;
                                                List<String> list57 = whiteAppList2;
                                                List<String> list58 = whiteAppList3;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 5:
                                            List<String> list59 = whiteAppList6;
                                            List<String> list60 = whiteAppList7;
                                            List<String> list61 = whiteAppList8;
                                            List<String> list62 = whiteAppList9;
                                            List<String> list63 = whiteAppList10;
                                            try {
                                                whiteAppList5.add(packageName);
                                                List<String> list64 = whiteAppList1;
                                                List<String> list65 = whiteAppList2;
                                                List<String> list66 = whiteAppList3;
                                                List<String> list67 = whiteAppList4;
                                                break;
                                            } catch (XmlPullParserException e9) {
                                                List<String> list68 = whiteAppList1;
                                                List<String> list69 = whiteAppList2;
                                                List<String> list70 = whiteAppList3;
                                                List<String> list71 = whiteAppList4;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e10) {
                                                List<String> list72 = whiteAppList1;
                                                List<String> list73 = whiteAppList2;
                                                List<String> list74 = whiteAppList3;
                                                List<String> list75 = whiteAppList4;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 6:
                                            List<String> list76 = whiteAppList7;
                                            List<String> list77 = whiteAppList8;
                                            List<String> list78 = whiteAppList9;
                                            List<String> list79 = whiteAppList10;
                                            try {
                                                whiteAppList6.add(packageName);
                                                List<String> list80 = whiteAppList1;
                                                List<String> list81 = whiteAppList2;
                                                List<String> list82 = whiteAppList3;
                                                List<String> list83 = whiteAppList4;
                                                List<String> list84 = whiteAppList5;
                                                break;
                                            } catch (XmlPullParserException e11) {
                                                List<String> list85 = whiteAppList1;
                                                List<String> list86 = whiteAppList2;
                                                List<String> list87 = whiteAppList3;
                                                List<String> list88 = whiteAppList4;
                                                List<String> list89 = whiteAppList5;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e12) {
                                                List<String> list90 = whiteAppList1;
                                                List<String> list91 = whiteAppList2;
                                                List<String> list92 = whiteAppList3;
                                                List<String> list93 = whiteAppList4;
                                                List<String> list94 = whiteAppList5;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 7:
                                            List<String> list95 = whiteAppList8;
                                            List<String> list96 = whiteAppList9;
                                            List<String> list97 = whiteAppList10;
                                            try {
                                                whiteAppList7.add(packageName);
                                                List<String> list98 = whiteAppList1;
                                                List<String> list99 = whiteAppList2;
                                                List<String> list100 = whiteAppList3;
                                                List<String> list101 = whiteAppList4;
                                                List<String> list102 = whiteAppList5;
                                                List<String> list103 = whiteAppList6;
                                                break;
                                            } catch (XmlPullParserException e13) {
                                                List<String> list104 = whiteAppList1;
                                                List<String> list105 = whiteAppList2;
                                                List<String> list106 = whiteAppList3;
                                                List<String> list107 = whiteAppList4;
                                                List<String> list108 = whiteAppList5;
                                                List<String> list109 = whiteAppList6;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e14) {
                                                List<String> list110 = whiteAppList1;
                                                List<String> list111 = whiteAppList2;
                                                List<String> list112 = whiteAppList3;
                                                List<String> list113 = whiteAppList4;
                                                List<String> list114 = whiteAppList5;
                                                List<String> list115 = whiteAppList6;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 8:
                                            List<String> list116 = whiteAppList9;
                                            List<String> list117 = whiteAppList10;
                                            try {
                                                whiteAppList8.add(packageName);
                                                List<String> list118 = whiteAppList1;
                                                List<String> list119 = whiteAppList2;
                                                List<String> list120 = whiteAppList3;
                                                List<String> list121 = whiteAppList4;
                                                List<String> list122 = whiteAppList5;
                                                List<String> list123 = whiteAppList6;
                                                List<String> list124 = whiteAppList7;
                                                break;
                                            } catch (XmlPullParserException e15) {
                                                List<String> list125 = whiteAppList1;
                                                List<String> list126 = whiteAppList2;
                                                List<String> list127 = whiteAppList3;
                                                List<String> list128 = whiteAppList4;
                                                List<String> list129 = whiteAppList5;
                                                List<String> list130 = whiteAppList6;
                                                List<String> list131 = whiteAppList7;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e16) {
                                                List<String> list132 = whiteAppList1;
                                                List<String> list133 = whiteAppList2;
                                                List<String> list134 = whiteAppList3;
                                                List<String> list135 = whiteAppList4;
                                                List<String> list136 = whiteAppList5;
                                                List<String> list137 = whiteAppList6;
                                                List<String> list138 = whiteAppList7;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 9:
                                            List<String> list139 = whiteAppList10;
                                            try {
                                                whiteAppList9.add(packageName);
                                                List<String> list140 = whiteAppList1;
                                                List<String> list141 = whiteAppList2;
                                                List<String> list142 = whiteAppList3;
                                                List<String> list143 = whiteAppList4;
                                                List<String> list144 = whiteAppList5;
                                                List<String> list145 = whiteAppList6;
                                                List<String> list146 = whiteAppList7;
                                                List<String> list147 = whiteAppList8;
                                                break;
                                            } catch (XmlPullParserException e17) {
                                                List<String> list148 = whiteAppList1;
                                                List<String> list149 = whiteAppList2;
                                                List<String> list150 = whiteAppList3;
                                                List<String> list151 = whiteAppList4;
                                                List<String> list152 = whiteAppList5;
                                                List<String> list153 = whiteAppList6;
                                                List<String> list154 = whiteAppList7;
                                                List<String> list155 = whiteAppList8;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e18) {
                                                List<String> list156 = whiteAppList1;
                                                List<String> list157 = whiteAppList2;
                                                List<String> list158 = whiteAppList3;
                                                List<String> list159 = whiteAppList4;
                                                List<String> list160 = whiteAppList5;
                                                List<String> list161 = whiteAppList6;
                                                List<String> list162 = whiteAppList7;
                                                List<String> list163 = whiteAppList8;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        case 10:
                                            try {
                                                whiteAppList10.add(packageName);
                                                List<String> list164 = whiteAppList1;
                                                List<String> list165 = whiteAppList2;
                                                List<String> list166 = whiteAppList3;
                                                List<String> list167 = whiteAppList4;
                                                List<String> list168 = whiteAppList5;
                                                List<String> list169 = whiteAppList6;
                                                List<String> list170 = whiteAppList7;
                                                List<String> list171 = whiteAppList8;
                                                List<String> list172 = whiteAppList9;
                                                break;
                                            } catch (XmlPullParserException e19) {
                                                List<String> list173 = whiteAppList1;
                                                List<String> list174 = whiteAppList2;
                                                List<String> list175 = whiteAppList3;
                                                List<String> list176 = whiteAppList4;
                                                List<String> list177 = whiteAppList5;
                                                List<String> list178 = whiteAppList6;
                                                List<String> list179 = whiteAppList7;
                                                List<String> list180 = whiteAppList8;
                                                List<String> list181 = whiteAppList9;
                                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                                return;
                                            } catch (IOException e20) {
                                                List<String> list182 = whiteAppList1;
                                                List<String> list183 = whiteAppList2;
                                                List<String> list184 = whiteAppList3;
                                                List<String> list185 = whiteAppList4;
                                                List<String> list186 = whiteAppList5;
                                                List<String> list187 = whiteAppList6;
                                                List<String> list188 = whiteAppList7;
                                                List<String> list189 = whiteAppList8;
                                                List<String> list190 = whiteAppList9;
                                                Log.e(TAG, "RecordWhiteList IOException");
                                                return;
                                            }
                                        default:
                                            List<String> list191 = whiteAppList1;
                                            List<String> list192 = whiteAppList2;
                                            List<String> list193 = whiteAppList3;
                                            List<String> list194 = whiteAppList4;
                                            List<String> list195 = whiteAppList5;
                                            List<String> list196 = whiteAppList6;
                                            List<String> list197 = whiteAppList7;
                                            List<String> list198 = whiteAppList8;
                                            List<String> list199 = whiteAppList9;
                                            List<String> list200 = whiteAppList10;
                                            scene_id = scene_id2;
                                            String str = nodeName;
                                            Log.e(TAG, "RecordWhiteList parseXmlForWhiteBlackList err");
                                            break;
                                    }
                                }
                            } catch (XmlPullParserException e21) {
                                List<String> list201 = whiteAppList1;
                                List<String> list202 = whiteAppList2;
                                List<String> list203 = whiteAppList3;
                                List<String> list204 = whiteAppList4;
                                List<String> list205 = whiteAppList5;
                                List<String> list206 = whiteAppList6;
                                List<String> list207 = whiteAppList7;
                                List<String> list208 = whiteAppList8;
                                List<String> list209 = whiteAppList9;
                                List<String> list210 = whiteAppList10;
                                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                                return;
                            } catch (IOException e22) {
                                List<String> list211 = whiteAppList1;
                                List<String> list212 = whiteAppList2;
                                List<String> list213 = whiteAppList3;
                                List<String> list214 = whiteAppList4;
                                List<String> list215 = whiteAppList5;
                                List<String> list216 = whiteAppList6;
                                List<String> list217 = whiteAppList7;
                                List<String> list218 = whiteAppList8;
                                List<String> list219 = whiteAppList9;
                                List<String> list220 = whiteAppList10;
                                Log.e(TAG, "RecordWhiteList IOException");
                                return;
                            }
                        } catch (XmlPullParserException e23) {
                            List<String> list2012 = whiteAppList1;
                            List<String> list2022 = whiteAppList2;
                            List<String> list2032 = whiteAppList3;
                            List<String> list2042 = whiteAppList4;
                            List<String> list2052 = whiteAppList5;
                            List<String> list2062 = whiteAppList6;
                            List<String> list2072 = whiteAppList7;
                            List<String> list2082 = whiteAppList8;
                            List<String> list2092 = whiteAppList9;
                            List<String> list2102 = whiteAppList10;
                            Log.e(TAG, "RecordWhiteList XmlPullParserException");
                            return;
                        } catch (IOException e24) {
                            List<String> list2112 = whiteAppList1;
                            List<String> list2122 = whiteAppList2;
                            List<String> list2132 = whiteAppList3;
                            List<String> list2142 = whiteAppList4;
                            List<String> list2152 = whiteAppList5;
                            List<String> list2162 = whiteAppList6;
                            List<String> list2172 = whiteAppList7;
                            List<String> list2182 = whiteAppList8;
                            List<String> list2192 = whiteAppList9;
                            List<String> list2202 = whiteAppList10;
                            Log.e(TAG, "RecordWhiteList IOException");
                            return;
                        }
                    } else {
                        XmlPullParser xmlPullParser2 = parser;
                    }
                    List<String> list221 = whiteAppList1;
                    List<String> list222 = whiteAppList2;
                    List<String> list223 = whiteAppList3;
                    List<String> list224 = whiteAppList4;
                    List<String> list225 = whiteAppList5;
                    List<String> list226 = whiteAppList6;
                    List<String> list227 = whiteAppList7;
                    List<String> list228 = whiteAppList8;
                    List<String> list229 = whiteAppList9;
                    List<String> list230 = whiteAppList10;
                    scene_id = scene_id2;
                    scene_id2 = scene_id;
                } else {
                    XmlPullParser xmlPullParser3 = parser;
                    List<String> list231 = whiteAppList1;
                    List<String> list232 = whiteAppList2;
                    List<String> list233 = whiteAppList3;
                    List<String> list234 = whiteAppList4;
                    List<String> list235 = whiteAppList5;
                    List<String> list236 = whiteAppList6;
                    List<String> list237 = whiteAppList7;
                    List<String> list238 = whiteAppList8;
                    List<String> list239 = whiteAppList9;
                    List<String> list240 = whiteAppList10;
                }
            } catch (XmlPullParserException e25) {
                XmlPullParser xmlPullParser4 = parser;
                List<String> list20122 = whiteAppList1;
                List<String> list20222 = whiteAppList2;
                List<String> list20322 = whiteAppList3;
                List<String> list20422 = whiteAppList4;
                List<String> list20522 = whiteAppList5;
                List<String> list20622 = whiteAppList6;
                List<String> list20722 = whiteAppList7;
                List<String> list20822 = whiteAppList8;
                List<String> list20922 = whiteAppList9;
                List<String> list21022 = whiteAppList10;
                Log.e(TAG, "RecordWhiteList XmlPullParserException");
                return;
            } catch (IOException e26) {
                XmlPullParser xmlPullParser5 = parser;
                List<String> list21122 = whiteAppList1;
                List<String> list21222 = whiteAppList2;
                List<String> list21322 = whiteAppList3;
                List<String> list21422 = whiteAppList4;
                List<String> list21522 = whiteAppList5;
                List<String> list21622 = whiteAppList6;
                List<String> list21722 = whiteAppList7;
                List<String> list21822 = whiteAppList8;
                List<String> list21922 = whiteAppList9;
                List<String> list22022 = whiteAppList10;
                Log.e(TAG, "RecordWhiteList IOException");
                return;
            }
        }
    }

    public boolean isValidCharSequence(CharSequence charSeq) {
        if (charSeq == null || charSeq.length() == 0) {
            return false;
        }
        return true;
    }

    private boolean getPackageNameInWhiteBlackList(String packageName) {
        if (this.mKaraokeWhiteList1 == null) {
            this.mKaraokeWhiteList1 = new ArrayList<>();
            this.mKaraokeWhiteList2 = new ArrayList<>();
            this.mKaraokeWhiteList3 = new ArrayList<>();
            this.mKaraokeWhiteList4 = new ArrayList<>();
            this.mKaraokeWhiteList5 = new ArrayList<>();
            this.mKaraokeWhiteList6 = new ArrayList<>();
            this.mKaraokeWhiteList7 = new ArrayList<>();
            this.mKaraokeWhiteList8 = new ArrayList<>();
            this.mKaraokeWhiteList9 = new ArrayList<>();
            this.mKaraokeWhiteList10 = new ArrayList<>();
            getAppInWhiteBlackList(this.mKaraokeWhiteList1, this.mKaraokeWhiteList2, this.mKaraokeWhiteList3, this.mKaraokeWhiteList4, this.mKaraokeWhiteList5, this.mKaraokeWhiteList6, this.mKaraokeWhiteList7, this.mKaraokeWhiteList8, this.mKaraokeWhiteList9, this.mKaraokeWhiteList10);
            Log.i(TAG, "Record white list1 =" + this.mKaraokeWhiteList1.toString());
            Log.i(TAG, "Record white list2 =" + this.mKaraokeWhiteList2.toString());
            Log.i(TAG, "Record white list3 =" + this.mKaraokeWhiteList3.toString());
            Log.i(TAG, "Record white list4 =" + this.mKaraokeWhiteList4.toString());
            Log.i(TAG, "Record white list5 =" + this.mKaraokeWhiteList5.toString());
            Log.i(TAG, "Record white list6 =" + this.mKaraokeWhiteList6.toString());
            Log.i(TAG, "Record white list7 =" + this.mKaraokeWhiteList7.toString());
            Log.i(TAG, "Record white list8 =" + this.mKaraokeWhiteList8.toString());
            Log.i(TAG, "Record white list9 =" + this.mKaraokeWhiteList9.toString());
            Log.i(TAG, "Record white list10 =" + this.mKaraokeWhiteList10.toString());
        }
        if (AudioSystem.getParameters("record_algo_version").equals("record5_0")) {
            if (this.mKaraokeWhiteList1.contains(packageName)) {
                Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=voice_message", new Object[0]));
                AudioSystem.setParameters("RECORD_SCENE=voice_message");
                return true;
            } else if (this.mKaraokeWhiteList2.contains(packageName)) {
                Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=video_message", new Object[0]));
                AudioSystem.setParameters("RECORD_SCENE=video_message");
                return true;
            } else if (this.mKaraokeWhiteList3.contains(packageName)) {
                Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=karaoke", new Object[0]));
                AudioSystem.setParameters("RECORD_SCENE=karaoke");
                return true;
            } else if (this.mKaraokeWhiteList4.contains(packageName)) {
                Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=live_telecast", new Object[0]));
                AudioSystem.setParameters("RECORD_SCENE=live_telecast");
                return true;
            } else if (this.mKaraokeWhiteList5.contains(packageName)) {
                Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=default", new Object[0]));
                AudioSystem.setParameters("RECORD_SCENE=default");
                return true;
            } else {
                Log.i(TAG, String.format("HuaweiProcess, app %s not in Record White Lise", new Object[]{packageName}));
                return false;
            }
        } else if (this.mKaraokeWhiteList6.contains(packageName)) {
            Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=voice_message", new Object[0]));
            AudioSystem.setParameters("RECORD_SCENE=voice_message");
            return true;
        } else if (this.mKaraokeWhiteList7.contains(packageName)) {
            Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=video_message", new Object[0]));
            AudioSystem.setParameters("RECORD_SCENE=video_message");
            return true;
        } else if (this.mKaraokeWhiteList8.contains(packageName)) {
            Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=karaoke", new Object[0]));
            AudioSystem.setParameters("RECORD_SCENE=karaoke");
            return true;
        } else if (this.mKaraokeWhiteList9.contains(packageName)) {
            Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=live_telecast", new Object[0]));
            AudioSystem.setParameters("RECORD_SCENE=live_telecast");
            return true;
        } else if (this.mKaraokeWhiteList10.contains(packageName)) {
            Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=default", new Object[0]));
            AudioSystem.setParameters("RECORD_SCENE=default");
            return true;
        } else {
            Log.i(TAG, String.format("HuaweiProcess, app %s not in Record White Lise", new Object[]{packageName}));
            return false;
        }
    }

    private String getPackageNameByUid(int uid) {
        String packageName;
        if (this.mPackMan == null) {
            Log.v(TAG, "mPackMan is null.");
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        String[] packages = this.mPackMan.getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            packageName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else {
            packageName = packages[0];
        }
        return packageName;
    }

    private void notifyIwareRecordingInfo(int event, int uid) {
        if (event == 0 || event == 1) {
            HwSystemManager.notifyBackgroundMgr(getPackageNameByUid(uid), Binder.getCallingPid(), uid, 1, event);
        }
    }

    public void onRecordingConfigurationChanged(int event, int uid, int session, int source, int[] recordingInfo, String packName) {
        List<AudioRecordingConfiguration> configsPublic;
        Log.v(TAG, "event: " + event + " uid: " + uid + " session: " + session + " source: " + source);
        notifyIwareRecordingInfo(event, uid);
        if (!MediaRecorder.isSystemOnlyAudioSource(source)) {
            List<AudioRecordingConfiguration> configsSystem = updateSnapshot(event, uid, session, source, recordingInfo);
            if (configsSystem != null) {
                synchronized (this.mClients) {
                    if (this.mHasPublicClients) {
                        configsPublic = anonymizeForPublicConsumption(configsSystem);
                    } else {
                        configsPublic = new ArrayList<>();
                    }
                    Iterator<RecMonitorClient> clientIterator = this.mClients.iterator();
                    while (clientIterator.hasNext()) {
                        RecMonitorClient rmc = clientIterator.next();
                        try {
                            if (rmc.mIsPrivileged) {
                                rmc.mDispatcherCb.dispatchRecordingConfigChange(configsSystem);
                            } else {
                                rmc.mDispatcherCb.dispatchRecordingConfigChange(configsPublic);
                            }
                        } catch (RemoteException e) {
                            Log.w(TAG, "Could not call dispatchRecordingConfigChange() on client", e);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        pw.println("\nRecordActivityMonitor dump time: " + DateFormat.getTimeInstance().format(new Date()));
        synchronized (this.mRecordConfigs) {
            for (AudioRecordingConfiguration conf : this.mRecordConfigs.values()) {
                conf.dump(pw);
            }
        }
        pw.println("\n");
        sEventLogger.dump(pw);
    }

    private ArrayList<AudioRecordingConfiguration> anonymizeForPublicConsumption(List<AudioRecordingConfiguration> sysConfigs) {
        ArrayList<AudioRecordingConfiguration> publicConfigs = new ArrayList<>();
        for (AudioRecordingConfiguration config : sysConfigs) {
            publicConfigs.add(AudioRecordingConfiguration.anonymizedCopy(config));
        }
        return publicConfigs;
    }

    /* access modifiers changed from: package-private */
    public void initMonitor() {
        AudioSystem.setRecordingCallback(this);
    }

    /* access modifiers changed from: package-private */
    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb, boolean isPrivileged) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                RecMonitorClient rmc = new RecMonitorClient(rcdb, isPrivileged);
                if (rmc.init()) {
                    if (!isPrivileged) {
                        this.mHasPublicClients = true;
                    }
                    this.mClients.add(rmc);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        if (rcdb != null) {
            synchronized (this.mClients) {
                Iterator<RecMonitorClient> clientIterator = this.mClients.iterator();
                boolean hasPublicClients = false;
                while (clientIterator.hasNext()) {
                    RecMonitorClient rmc = clientIterator.next();
                    if (rcdb.equals(rmc.mDispatcherCb)) {
                        rmc.release();
                        clientIterator.remove();
                    } else if (!rmc.mIsPrivileged) {
                        hasPublicClients = true;
                    }
                }
                this.mHasPublicClients = hasPublicClients;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations(boolean isPrivileged) {
        synchronized (this.mRecordConfigs) {
            if (isPrivileged) {
                try {
                    ArrayList arrayList = new ArrayList(this.mRecordConfigs.values());
                    return arrayList;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                List<AudioRecordingConfiguration> configsPublic = anonymizeForPublicConsumption(new ArrayList(this.mRecordConfigs.values()));
                return configsPublic;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0169, code lost:
        if (r9 == false) goto L_0x0177;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x016b, code lost:
        r2 = new java.util.ArrayList<>(r1.mRecordConfigs.values());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0177, code lost:
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0178, code lost:
        monitor-exit(r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0179, code lost:
        return r2;
     */
    private List<AudioRecordingConfiguration> updateSnapshot(int event, int uid, int session, int source, int[] recordingInfo) {
        HashMap<Integer, AudioRecordingConfiguration> hashMap;
        int i;
        String packageName;
        int i2;
        int i3 = uid;
        int i4 = session;
        HashMap<Integer, AudioRecordingConfiguration> hashMap2 = this.mRecordConfigs;
        synchronized (hashMap2) {
            boolean configChanged = true;
            boolean configChanged2 = false;
            switch (event) {
                case 0:
                    hashMap = hashMap2;
                    int i5 = i4;
                    if (this.mRecordConfigs.remove(new Integer(i5)) == null) {
                        configChanged = false;
                    }
                    boolean configChanged3 = configChanged;
                    if (configChanged3) {
                        AudioEventLogger audioEventLogger = sEventLogger;
                        r2 = r2;
                        i = i5;
                        try {
                            RecordingEvent recordingEvent = new RecordingEvent(event, i3, i5, source, null);
                            audioEventLogger.log(recordingEvent);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    } else {
                        i = i5;
                    }
                    if (this.whatsapp_session.equals(new Integer(i))) {
                        this.whatsapp_session = 0;
                        AudioSystem.setParameters("RECORD_SCENE=off");
                        Log.i(TAG, String.format("HuaweiProcess, set RECORD_SCENE=off", new Object[0]));
                    }
                    configChanged2 = configChanged3;
                    break;
                case 1:
                    try {
                        AudioFormat clientFormat = new AudioFormat.Builder().setEncoding(recordingInfo[0]).setChannelMask(recordingInfo[1]).setSampleRate(recordingInfo[2]).build();
                        AudioFormat deviceFormat = new AudioFormat.Builder().setEncoding(recordingInfo[3]).setChannelMask(recordingInfo[4]).setSampleRate(recordingInfo[5]).build();
                        int patchHandle = recordingInfo[6];
                        Integer sessionKey = new Integer(i4);
                        String[] packages = this.mPackMan.getPackagesForUid(i3);
                        if (packages == null || packages.length <= 0) {
                            packageName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                        } else {
                            packageName = packages[0];
                        }
                        String packageName2 = packageName;
                        Log.v(TAG, "uid = " + i3 + " packageName = " + packageName2);
                        AudioRecordingConfiguration audioRecordingConfiguration = new AudioRecordingConfiguration(i3, i4, source, clientFormat, deviceFormat, patchHandle, packageName2);
                        AudioRecordingConfiguration updatedConfig = audioRecordingConfiguration;
                        if (!this.mRecordConfigs.containsKey(sessionKey)) {
                            this.mRecordConfigs.put(sessionKey, updatedConfig);
                        } else if (updatedConfig.equals(this.mRecordConfigs.get(sessionKey))) {
                            configChanged = false;
                        } else {
                            this.mRecordConfigs.remove(sessionKey);
                            this.mRecordConfigs.put(sessionKey, updatedConfig);
                            configChanged = true;
                        }
                        boolean configChanged4 = configChanged;
                        if (configChanged4) {
                            AudioEventLogger audioEventLogger2 = sEventLogger;
                            r9 = r9;
                            String packageName3 = packageName2;
                            String[] strArr = packages;
                            hashMap = hashMap2;
                            AudioFormat audioFormat = deviceFormat;
                            i2 = i4;
                            RecordingEvent recordingEvent2 = new RecordingEvent(event, i3, i4, source, packageName3);
                            audioEventLogger2.log(recordingEvent2);
                            if (getPackageNameInWhiteBlackList(packageName3)) {
                                this.whatsapp_session = sessionKey;
                            }
                        } else {
                            hashMap = hashMap2;
                            i2 = i4;
                        }
                        configChanged2 = configChanged4;
                        int i6 = i2;
                        break;
                    } catch (Throwable th2) {
                        th = th2;
                        int i7 = i2;
                        throw th;
                    }
                    break;
                default:
                    hashMap = hashMap2;
                    int i8 = i4;
                    Log.e(TAG, String.format("Unknown event %d for session %d, source %d", new Object[]{Integer.valueOf(event), Integer.valueOf(session), Integer.valueOf(source)}));
                    break;
            }
        }
    }
}
