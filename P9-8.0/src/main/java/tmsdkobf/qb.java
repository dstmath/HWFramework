package tmsdkobf;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.utils.f;

public class qb {
    public static final String TAG = qb.class.getSimpleName();

    private static void a(OfflineVideo offlineVideo) {
        f.h(TAG, offlineVideo.mPath);
        try {
            if ("qiyi".equals(offlineVideo.mAdapter)) {
                b(offlineVideo);
                return;
            }
            if ("qqlive".equals(offlineVideo.mAdapter)) {
                c(offlineVideo);
                if (offlineVideo.mTitle != null) {
                    if (!"".equals(offlineVideo.mTitle)) {
                        return;
                    }
                }
                offlineVideo.mTitle = ct(offlineVideo.mPath);
                return;
            }
            if ("sohu".equals(offlineVideo.mAdapter)) {
                d(offlineVideo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            f.h(TAG, e.getMessage());
        }
    }

    public static void a(String[] strArr) {
        if (strArr.length >= 1) {
            f.h(TAG, "uid " + Process.myUid());
            String str = strArr[0];
            List<OfflineVideo> readOfflineVideos = OfflineVideo.readOfflineVideos(str);
            if (readOfflineVideos != null && readOfflineVideos.size() != 0) {
                for (OfflineVideo a : readOfflineVideos) {
                    a(a);
                }
                OfflineVideo.dumpToFile(readOfflineVideos, str);
            }
        }
    }

    private static void b(OfflineVideo offlineVideo) {
        if (offlineVideo.mPath != null) {
            String[] split = ct(offlineVideo.mPath).split("_");
            if (split.length == 2) {
                String str = split[0];
                String str2 = split[1];
                SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.qiyi.video/databases/qyvideo.db", null, 0);
                if (openDatabase != null) {
                    Cursor query = openDatabase.query("rc_tbl", null, "tvId = ?", new String[]{str2}, null, null, null);
                    if (query != null) {
                        try {
                            if (query.moveToFirst()) {
                                int columnIndex = query.getColumnIndex("videoPlayTime");
                                int columnIndex2 = query.getColumnIndex("videoDuration");
                                query.moveToFirst();
                                int i = query.getInt(columnIndex);
                                int i2 = query.getInt(columnIndex2);
                                if (i != 0) {
                                    offlineVideo.mPlayProgress = i2 > 0 ? (i * 100) / i2 : -1;
                                } else {
                                    offlineVideo.mPlayProgress = 100;
                                }
                            } else {
                                offlineVideo.mPlayProgress = 0;
                            }
                            query.close();
                        } catch (Exception e) {
                            query.close();
                        } catch (Throwable th) {
                            query.close();
                            throw th;
                        }
                    }
                    openDatabase.close();
                }
            }
        }
    }

    private static void c(OfflineVideo offlineVideo) {
        if (offlineVideo.mPath != null) {
            String ct = ct(offlineVideo.mPath);
            SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.tencent.qqlive/databases/download_db", null, 0);
            if (openDatabase != null) {
                Cursor query = openDatabase.query("download_db", null, "recordid = ?", new String[]{ct}, null, null, null);
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
                            stringBuilder = stringBuilder.append(string);
                            if (string2 == null) {
                                string2 = "";
                            }
                            offlineVideo.mTitle = stringBuilder.append(string2).toString();
                            if ((j <= 0 ? 1 : null) == null) {
                                offlineVideo.mDownProgress = (int) ((offlineVideo.mSize * 100) / j);
                            } else {
                                offlineVideo.mDownProgress = -1;
                            }
                        }
                        query.close();
                    } catch (Exception e) {
                        f.h(TAG, "fillQQLiveVideoInfo " + e.getMessage());
                        query.close();
                    } catch (Throwable th) {
                        query.close();
                        throw th;
                    }
                }
                openDatabase.close();
            }
        }
    }

    private static String ct(String str) {
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf <= 0 && lastIndexOf < str.length() - 1) ? null : str.substring(lastIndexOf + 1);
    }

    private static void d(OfflineVideo offlineVideo) {
        if (ct(offlineVideo.mPath) != null) {
            Cursor query;
            String str = null;
            int i = 0;
            SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/sohutv.db", null, 0);
            if (openDatabase != null) {
                query = openDatabase.query("t_videodownload", null, "save_filename = ?", new String[]{r9}, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            str = query.getString(query.getColumnIndex("play_id"));
                            i = query.getInt(query.getColumnIndex("time_length"));
                            offlineVideo.mTitle = query.getString(query.getColumnIndex("vd_titile"));
                            offlineVideo.mDownProgress = query.getInt(query.getColumnIndex("download_percent"));
                            f.h(TAG, "" + str + " " + i + " " + offlineVideo.mTitle + " " + offlineVideo.mDownProgress);
                        }
                        query.close();
                    } catch (Exception e) {
                        f.h(TAG, "fillSohuVideoInfo " + e.getMessage());
                        query.close();
                    } catch (Throwable th) {
                        query.close();
                        throw th;
                    }
                }
                openDatabase.close();
            }
            if (str != null && i > 0) {
                openDatabase = SQLiteDatabase.openDatabase("/data/data/com.sohu.sohuvideo/files/databases/other.db", null, 0);
                if (openDatabase != null) {
                    query = openDatabase.query("sohu_video_history", null, "playId = ?", new String[]{str}, null, null, null);
                    if (query != null) {
                        try {
                            if (query.moveToFirst()) {
                                int i2 = query.getInt(query.getColumnIndex("playedTime"));
                                offlineVideo.mPlayProgress = (i2 * 100) / i;
                                f.h(TAG, "play time " + i2);
                            } else {
                                offlineVideo.mPlayProgress = 0;
                            }
                            query.close();
                        } catch (Exception e2) {
                            f.h(TAG, "fillSohuVideoInfo " + e2.getMessage());
                            query.close();
                        } catch (Throwable th2) {
                            query.close();
                            throw th2;
                        }
                    }
                    openDatabase.close();
                }
            }
        }
    }
}
