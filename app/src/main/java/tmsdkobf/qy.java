package tmsdkobf;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class qy {
    public static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.qy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.qy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.qy.<clinit>():void");
    }

    private static void c(OfflineVideo offlineVideo) {
        d.g(TAG, offlineVideo.mPath);
        try {
            if ("qiyi".equals(offlineVideo.mAdapter)) {
                d(offlineVideo);
            } else if ("qqlive".equals(offlineVideo.mAdapter)) {
                e(offlineVideo);
                if (offlineVideo.mTitle == null || "".equals(offlineVideo.mTitle)) {
                    offlineVideo.mTitle = cY(offlineVideo.mPath);
                }
            } else if ("sohu".equals(offlineVideo.mAdapter)) {
                f(offlineVideo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            d.g(TAG, e.getMessage());
        }
    }

    private static String cY(String str) {
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf <= 0 && lastIndexOf < str.length() - 1) ? null : str.substring(lastIndexOf + 1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void d(OfflineVideo offlineVideo) {
        if (offlineVideo.mPath != null) {
            String[] split = cY(offlineVideo.mPath).split("_");
            if (split.length == 2) {
                String str = split[0];
                str = split[1];
                SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.qiyi.video/databases/qyvideo.db", null, 0);
                if (openDatabase != null) {
                    Cursor query = openDatabase.query("rc_tbl", null, "tvId = ?", new String[]{str}, null, null, null);
                    if (query != null) {
                        try {
                            if (query.moveToFirst()) {
                                int columnIndex = query.getColumnIndex("videoPlayTime");
                                int columnIndex2 = query.getColumnIndex("videoDuration");
                                query.moveToFirst();
                                columnIndex = query.getInt(columnIndex);
                                columnIndex2 = query.getInt(columnIndex2);
                                if (columnIndex != 0) {
                                    offlineVideo.mPlayProgress = columnIndex2 > 0 ? (columnIndex * 100) / columnIndex2 : -1;
                                } else {
                                    offlineVideo.mPlayProgress = 100;
                                }
                            } else {
                                offlineVideo.mPlayProgress = 0;
                            }
                            query.close();
                        } catch (Exception e) {
                        } catch (Throwable th) {
                            query.close();
                        }
                    }
                    openDatabase.close();
                }
            }
        }
    }

    public static void d(String[] strArr) {
        if (strArr.length >= 1) {
            d.g(TAG, "uid " + Process.myUid());
            String str = strArr[0];
            List<OfflineVideo> readOfflineVideos = OfflineVideo.readOfflineVideos(str);
            if (readOfflineVideos != null && readOfflineVideos.size() != 0) {
                for (OfflineVideo c : readOfflineVideos) {
                    c(c);
                }
                OfflineVideo.dumpToFile(readOfflineVideos, str);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void e(OfflineVideo offlineVideo) {
        if (offlineVideo.mPath != null) {
            String cY = cY(offlineVideo.mPath);
            SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.tencent.qqlive/databases/download_db", null, 0);
            if (openDatabase != null) {
                Cursor query = openDatabase.query("download_db", null, "recordid = ?", new String[]{cY}, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            String string = query.getString(query.getColumnIndex("covername"));
                            String string2 = query.getString(query.getColumnIndex("episodename"));
                            long j = query.getLong(query.getColumnIndex("videosize"));
                            StringBuilder stringBuilder = new StringBuilder();
                            if (string == null) {
                                string = "";
                            }
                            StringBuilder append = stringBuilder.append(string);
                            if (string2 == null) {
                                string2 = "";
                            }
                            offlineVideo.mTitle = append.append(string2).toString();
                            if ((j <= 0 ? 1 : 0) == 0) {
                                offlineVideo.mDownProgress = (int) ((offlineVideo.mSize * 100) / j);
                            } else {
                                offlineVideo.mDownProgress = -1;
                            }
                        }
                        query.close();
                    } catch (Exception e) {
                        d.g(TAG, "fillQQLiveVideoInfo " + e.getMessage());
                    } catch (Throwable th) {
                        query.close();
                    }
                }
                openDatabase.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void f(OfflineVideo offlineVideo) {
        int i;
        int i2;
        String str;
        String str2;
        int i3;
        Exception exception;
        Cursor query;
        if (cY(offlineVideo.mPath) != null) {
            String str3;
            SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/sohutv.db", null, 0);
            if (openDatabase == null) {
                i = 0;
                str3 = null;
            } else {
                Cursor query2 = openDatabase.query("t_videodownload", null, "save_filename = ?", new String[]{str3}, null, null, null);
                if (query2 == null) {
                    i2 = 0;
                    str = null;
                } else {
                    try {
                        if (query2.moveToFirst()) {
                            str = query2.getString(query2.getColumnIndex("play_id"));
                            try {
                                i2 = query2.getInt(query2.getColumnIndex("time_length"));
                                try {
                                    offlineVideo.mTitle = query2.getString(query2.getColumnIndex("vd_titile"));
                                    offlineVideo.mDownProgress = query2.getInt(query2.getColumnIndex("download_percent"));
                                    d.g(TAG, "" + str + " " + i2 + " " + offlineVideo.mTitle + " " + offlineVideo.mDownProgress);
                                } catch (Exception e) {
                                    Exception exception2 = e;
                                    str2 = str;
                                    i3 = i2;
                                    exception = exception2;
                                    try {
                                        d.g(TAG, "fillSohuVideoInfo " + exception.getMessage());
                                        i2 = i3;
                                        str = str2;
                                        openDatabase.close();
                                        i = i2;
                                        str3 = str;
                                        if (str3 != null) {
                                            openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/other.db", null, 0);
                                            if (openDatabase != null) {
                                                query = openDatabase.query("sohu_video_history", null, "playId = ?", new String[]{str3}, null, null, null);
                                                if (query != null) {
                                                    try {
                                                        if (query.moveToFirst()) {
                                                            i2 = query.getInt(query.getColumnIndex("playedTime"));
                                                            offlineVideo.mPlayProgress = (i2 * 100) / i;
                                                            d.g(TAG, "play time " + i2);
                                                        } else {
                                                            offlineVideo.mPlayProgress = 0;
                                                        }
                                                        query.close();
                                                    } catch (Exception exception3) {
                                                        d.g(TAG, "fillSohuVideoInfo " + exception3.getMessage());
                                                    } catch (Throwable th) {
                                                        openDatabase = th;
                                                    }
                                                }
                                                openDatabase.close();
                                            }
                                        }
                                    } finally {
                                        query2.close();
                                    }
                                }
                            } catch (Exception e2) {
                                exception3 = e2;
                                str2 = str;
                                i3 = 0;
                                d.g(TAG, "fillSohuVideoInfo " + exception3.getMessage());
                                i2 = i3;
                                str = str2;
                                openDatabase.close();
                                i = i2;
                                str3 = str;
                                if (str3 != null) {
                                    openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/other.db", null, 0);
                                    if (openDatabase != null) {
                                        query = openDatabase.query("sohu_video_history", null, "playId = ?", new String[]{str3}, null, null, null);
                                        if (query != null) {
                                            if (query.moveToFirst()) {
                                                i2 = query.getInt(query.getColumnIndex("playedTime"));
                                                offlineVideo.mPlayProgress = (i2 * 100) / i;
                                                d.g(TAG, "play time " + i2);
                                            } else {
                                                offlineVideo.mPlayProgress = 0;
                                            }
                                            query.close();
                                        }
                                        openDatabase.close();
                                    }
                                }
                            }
                        }
                        i2 = 0;
                        str = null;
                        query2.close();
                    } catch (Exception e3) {
                        exception3 = e3;
                        i3 = 0;
                        Object obj = null;
                        d.g(TAG, "fillSohuVideoInfo " + exception3.getMessage());
                        i2 = i3;
                        str = str2;
                        openDatabase.close();
                        i = i2;
                        str3 = str;
                        if (str3 != null) {
                            openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/other.db", null, 0);
                            if (openDatabase != null) {
                                query = openDatabase.query("sohu_video_history", null, "playId = ?", new String[]{str3}, null, null, null);
                                if (query != null) {
                                    if (query.moveToFirst()) {
                                        offlineVideo.mPlayProgress = 0;
                                    } else {
                                        i2 = query.getInt(query.getColumnIndex("playedTime"));
                                        offlineVideo.mPlayProgress = (i2 * 100) / i;
                                        d.g(TAG, "play time " + i2);
                                    }
                                    query.close();
                                }
                                openDatabase.close();
                            }
                        }
                    }
                }
                openDatabase.close();
                i = i2;
                str3 = str;
            }
            if (str3 != null && i > 0) {
                openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/other.db", null, 0);
                if (openDatabase != null) {
                    query = openDatabase.query("sohu_video_history", null, "playId = ?", new String[]{str3}, null, null, null);
                    if (query != null) {
                        if (query.moveToFirst()) {
                            offlineVideo.mPlayProgress = 0;
                        } else {
                            i2 = query.getInt(query.getColumnIndex("playedTime"));
                            offlineVideo.mPlayProgress = (i2 * 100) / i;
                            d.g(TAG, "play time " + i2);
                        }
                        query.close();
                    }
                    openDatabase.close();
                }
            }
        }
    }
}
