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
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class hm extends hf {
    final boolean qB;
    private Pattern qC;

    public hm(boolean z) {
        this.qC = null;
        this.qB = z;
    }

    private void b(String str, List<OfflineVideo> list) {
        String bh = bh(gq.aN(str));
        if (!TextUtils.isEmpty(bh)) {
            String[] list2 = new File(str).list();
            if (list2 != null) {
                for (String str2 : list2) {
                    String bi = bi(str2);
                    if (bi != null) {
                        OfflineVideo offlineVideo = new OfflineVideo();
                        offlineVideo.mPath = str + "/" + str2;
                        offlineVideo.mTitle = !this.qB ? bh + "(\u7b2c" + bi + "\u96c6)" : bh + "(Episode " + bi + ")";
                        offlineVideo.mSize = hi.ba(offlineVideo.mPath);
                        String[] list3 = new File(offlineVideo.mPath).list();
                        if (list3 != null) {
                            for (String str3 : list3) {
                                if (str3.endsWith(".storm")) {
                                    offlineVideo.mThumnbailPath = offlineVideo.mPath + "/" + str3;
                                    break;
                                }
                            }
                        }
                        try {
                            b(offlineVideo);
                        } catch (Exception e) {
                            d.g("PiDeepClean", e.getMessage());
                        }
                        list.add(offlineVideo);
                    }
                }
            }
        }
    }

    private void b(OfflineVideo offlineVideo) {
        SQLiteDatabase openDatabase = SQLiteDatabase.openDatabase(offlineVideo.mPath.replaceFirst("\\.download/.*", ".database/bfdownload.db"), null, 0);
        if (openDatabase != null) {
            Cursor query = openDatabase.query("downloadtable", null, "local_file_path = ?", new String[]{offlineVideo.mPath}, null, null, null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        long j = query.getLong(query.getColumnIndex("total_size"));
                        long j2 = query.getLong(query.getColumnIndex("downloaded_size"));
                        if ((j <= 0 ? 1 : 0) == 0) {
                            offlineVideo.mDownProgress = (int) ((j2 * 100) / j);
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

    private String bh(String str) {
        if (this.qC == null) {
            this.qC = Pattern.compile("[0-9]+-[0-9]+-(.*)");
        }
        Matcher matcher = this.qC.matcher(str);
        return !matcher.find() ? null : matcher.group(1);
    }

    private String bi(String str) {
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

    public List<OfflineVideo> a(hg hgVar) {
        List<String> aZ = hi.aZ(hgVar.pa);
        if (aZ.size() == 0) {
            return null;
        }
        List<OfflineVideo> arrayList = new ArrayList();
        for (String b : aZ) {
            b(b, arrayList);
        }
        return arrayList;
    }
}
