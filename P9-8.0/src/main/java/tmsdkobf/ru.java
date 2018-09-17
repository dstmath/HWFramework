package tmsdkobf;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.OfflineVideo;
import tmsdk.common.utils.f;

public class ru extends rn {
    final boolean PY;
    private Pattern PZ = null;

    public ru(boolean z) {
        this.PY = z;
    }

    private void b(String str, List<OfflineVideo> list) {
        String dx = dx(rh.di(str));
        if (!TextUtils.isEmpty(dx)) {
            String[] list2 = new File(str).list();
            if (list2 != null) {
                String[] strArr = list2;
                for (String str2 : list2) {
                    String dy = dy(str2);
                    if (dy != null) {
                        OfflineVideo offlineVideo = new OfflineVideo();
                        offlineVideo.mPath = str + "/" + str2;
                        offlineVideo.mTitle = !this.PY ? dx + "(第" + dy + "集)" : dx + "(Episode " + dy + ")";
                        offlineVideo.mSize = rq.dq(offlineVideo.mPath);
                        String[] list3 = new File(offlineVideo.mPath).list();
                        if (list3 != null) {
                            String[] strArr2 = list3;
                            for (String str3 : list3) {
                                if (str3.endsWith(".storm")) {
                                    offlineVideo.mThumnbailPath = offlineVideo.mPath + "/" + str3;
                                    break;
                                }
                            }
                        }
                        try {
                            f(offlineVideo);
                        } catch (Exception e) {
                            f.h("PiDeepClean", e.getMessage());
                        }
                        list.add(offlineVideo);
                    }
                }
            }
        }
    }

    private String dx(String str) {
        if (this.PZ == null) {
            this.PZ = Pattern.compile("[0-9]+-[0-9]+-(.*)");
        }
        Matcher matcher = this.PZ.matcher(str);
        return !matcher.find() ? null : matcher.group(1);
    }

    private String dy(String str) {
        String str2 = null;
        String[] split = str.split("-");
        if (split == null || split.length != 2) {
            return null;
        }
        if (split[0].length() != 0) {
            str2 = split[0];
        }
        return str2;
    }

    private void f(OfflineVideo offlineVideo) {
        SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase(offlineVideo.mPath.replaceFirst("\\.download/.*", ".database/bfdownload.db"), null, 0);
        if (openDatabase != null) {
            Cursor query = openDatabase.query("downloadtable", null, "local_file_path = ?", new String[]{offlineVideo.mPath}, null, null, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        long j = query.getLong(query.getColumnIndex("total_size"));
                        long j2 = query.getLong(query.getColumnIndex("downloaded_size"));
                        if ((j <= 0 ? 1 : null) == null) {
                            offlineVideo.mDownProgress = (int) ((100 * j2) / j);
                        } else {
                            offlineVideo.mDownProgress = -1;
                        }
                    }
                    if (query != null) {
                        query.close();
                    }
                } catch (Exception e) {
                    if (query != null) {
                        query.close();
                    }
                } catch (Throwable th) {
                    if (query != null) {
                        query.close();
                    }
                }
            }
            openDatabase.close();
        }
    }

    public List<OfflineVideo> a(ro roVar) {
        List<String> dp = rq.dp(roVar.Ok);
        if (dp.size() == 0) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        for (String b : dp) {
            b(b, arrayList);
        }
        return arrayList;
    }
}
