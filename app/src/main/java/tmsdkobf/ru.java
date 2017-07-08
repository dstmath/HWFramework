package tmsdkobf;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.utils.d;
import tmsdk.common.utils.l;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.FileMedia;
import tmsdk.fg.module.spacemanager.FileScanResult;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;

/* compiled from: Unknown */
public class ru implements Callback {
    a NQ;
    boolean OL;
    HashMap<String, rm> OM;
    HashMap<String, rm> ON;
    HashMap<String, rm> OO;
    List<String> OP;
    List<rm> OQ;
    FileScanResult OR;
    int OS;
    Context mContext;
    private DeepCleanEngine oS;
    private int pX;
    int qd;
    int qe;

    /* compiled from: Unknown */
    public interface a {
        void a(long j, Object obj);

        void onCancel();

        void onProgressChanged(int i);
    }

    public ru(Context context, a aVar, int i) {
        this.OL = false;
        this.pX = 0;
        this.qd = 0;
        this.qe = 0;
        this.OM = new HashMap();
        this.ON = new HashMap();
        this.OO = new HashMap();
        this.OP = new ArrayList();
        this.OQ = new ArrayList();
        this.OR = new FileScanResult();
        this.OS = 7;
        this.mContext = context;
        this.NQ = aVar;
        this.OS = i;
    }

    private static void L(List<FileMedia> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<FileMedia>() {
                public int a(FileMedia fileMedia, FileMedia fileMedia2) {
                    int i = 1;
                    int i2 = 0;
                    if (fileMedia.mSize == fileMedia2.mSize) {
                        return 0;
                    }
                    if (fileMedia.mSize >= fileMedia2.mSize) {
                        i2 = 1;
                    }
                    if (i2 != 0) {
                        i = -1;
                    }
                    return i;
                }

                public /* synthetic */ int compare(Object obj, Object obj2) {
                    return a((FileMedia) obj, (FileMedia) obj2);
                }
            });
        }
    }

    private static void M(List<FileMedia> list) {
        if (list != null) {
            L(list);
            Collection arrayList = new ArrayList();
            HashMap hashMap = new HashMap();
            for (FileMedia fileMedia : list) {
                if (l.dm(fileMedia.pkg)) {
                    arrayList.add(fileMedia);
                } else {
                    ArrayList arrayList2 = (ArrayList) hashMap.get(fileMedia.pkg);
                    if (arrayList2 == null) {
                        arrayList2 = new ArrayList();
                        hashMap.put(fileMedia.pkg, arrayList2);
                    }
                    arrayList2.add(fileMedia);
                }
            }
            Object arrayList3 = new ArrayList();
            arrayList3.addAll(hashMap.values());
            Collections.sort(arrayList3, new Comparator<ArrayList<FileMedia>>() {
                public int c(ArrayList<FileMedia> arrayList, ArrayList<FileMedia> arrayList2) {
                    if (arrayList.size() == arrayList2.size()) {
                        return 0;
                    }
                    return arrayList.size() <= arrayList2.size() ? 1 : -1;
                }

                public /* synthetic */ int compare(Object obj, Object obj2) {
                    return c((ArrayList) obj, (ArrayList) obj2);
                }
            });
            list.clear();
            Iterator it = arrayList3.iterator();
            while (it.hasNext()) {
                list.addAll((ArrayList) it.next());
            }
            list.addAll(arrayList);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(FileMedia fileMedia, Context context) {
        String[] strArr = new String[]{"title", "artist", "album"};
        String[] strArr2 = new String[]{fileMedia.mPath};
        Cursor query = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, strArr, "_data = ?", strArr2, "");
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    fileMedia.title = query.getString(query.getColumnIndex("title"));
                    fileMedia.artist = query.getString(query.getColumnIndex("artist"));
                    fileMedia.album = query.getString(query.getColumnIndex("album"));
                    if (fileMedia.artist != null) {
                        if (fileMedia.artist.contains("unknown")) {
                            fileMedia.artist = null;
                        }
                    }
                    if (fileMedia.album != null && fileMedia.album.contains("unknown")) {
                        fileMedia.album = null;
                    }
                }
                query.close();
            } catch (Exception e) {
                d.g(WeChatCacheFiles.GLOBAL_TAG, "readArtist " + e.getMessage());
            } catch (Throwable th) {
                query.close();
            }
        }
    }

    public static void a(FileScanResult fileScanResult, Context context, boolean z) {
        rf rfVar = new rf();
        rfVar.jz();
        if (fileScanResult.mBigFiles != null) {
            for (FileInfo fileInfo : fileScanResult.mBigFiles) {
                fileInfo.mSrcName = rfVar.a(re.a(fileInfo.mPath, rf.MY).toLowerCase(), null, z);
            }
        }
        if (fileScanResult.mRadioFiles != null) {
            for (FileMedia fileMedia : fileScanResult.mRadioFiles) {
                String toLowerCase = re.a(fileMedia.mPath, rf.MY).toLowerCase();
                fileMedia.pkg = rfVar.f(toLowerCase, z);
                fileMedia.mSrcName = rfVar.g(toLowerCase, z);
                a(fileMedia, context);
            }
            M(fileScanResult.mRadioFiles);
        }
        if (fileScanResult.mVideoFiles != null) {
            for (FileMedia fileMedia2 : fileScanResult.mVideoFiles) {
                toLowerCase = re.a(fileMedia2.mPath, rf.MY).toLowerCase();
                fileMedia2.pkg = rfVar.f(toLowerCase, z);
                fileMedia2.mSrcName = rfVar.g(toLowerCase, z);
            }
            M(fileScanResult.mVideoFiles);
        }
    }

    private void b(long j, Object obj) {
        if (this.NQ != null) {
            this.NQ.a(j, obj);
        }
    }

    private void bk() {
        String[] strArr = new String[this.OP.size()];
        this.OP.toArray(strArr);
        this.oS.setWhitePaths(strArr);
        String[] strArr2 = new String[this.OQ.size()];
        for (int i = 0; i < strArr2.length; i++) {
            strArr2[i] = ((rm) this.OQ.get(i)).toString();
        }
        this.oS.setComRubRule(strArr2);
        List<String> aT = re.aT();
        List aU = re.aU();
        if (aU != null) {
            this.pX = aU.size();
            if (aT != null) {
                for (String str : aT) {
                    if (jN()) {
                        this.NQ.onCancel();
                    } else {
                        this.oS.scanPath(str, "/");
                        this.qd += this.qe;
                    }
                }
            }
        }
    }

    private boolean jL() {
        rt rtVar = new rt();
        if (!rtVar.N(this.mContext)) {
            return false;
        }
        rm rmVar;
        if (rtVar.OH != null) {
            this.OP.addAll(rtVar.OH);
        }
        List<tmsdkobf.rt.a> list = rtVar.OK;
        if (list != null) {
            for (tmsdkobf.rt.a aVar : list) {
                if (l.dl(aVar.pa)) {
                    this.OP.add(aVar.pa);
                }
            }
        }
        if (!((this.OS & 1) == 0 || rtVar.OI == null)) {
            for (rm rmVar2 : rtVar.OI) {
                this.OQ.add(rmVar2);
                this.OM.put(rmVar2.oZ, rmVar2);
            }
        }
        if (!((this.OS & 2) == 0 || rtVar.OJ == null)) {
            for (rm rmVar22 : rtVar.OJ) {
                this.OQ.add(rmVar22);
                this.ON.put(rmVar22.oZ, rmVar22);
            }
        }
        if ((this.OS & 4) != 0) {
            rmVar22 = new rm();
            rmVar22.pb = "10240,-";
            rmVar22.pf = "0";
            if ((this.OS & WifiDetectManager.SECURITY_NONE) != 0) {
                rmVar22.mFileName = "/\\.(zip|rar|pdf|doc|apk|ppt|txt|log|chm|docx|pptx|iso|7z|tar|gz)";
            }
            this.OQ.add(rmVar22);
            this.OO.put(rmVar22.oZ, rmVar22);
        }
        return true;
    }

    public FileScanResult S(boolean z) {
        if (!jL() || this.OQ.size() == 0) {
            return this.OR;
        }
        this.oS = SdcardScannerFactory.getDeepCleanEngine(this, 2);
        if (this.oS != null) {
            bk();
            this.oS.release();
            this.oS = null;
        }
        a(this.OR, this.mContext, z);
        return this.OR;
    }

    public void a(String str, String str2, long j, tmsdkobf.rt.a aVar) {
        FileMedia fileMedia = new FileMedia();
        fileMedia.mPath = str2;
        fileMedia.type = 1;
        fileMedia.mSize = j;
        fileMedia.mPlayers = aVar.mPlayers;
        this.OR.mRadioFiles.add(fileMedia);
        b(j, fileMedia);
    }

    public void b(String str, String str2, long j) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.type = 3;
        fileInfo.mPath = str2;
        fileInfo.mSize = j;
        this.OR.mBigFiles.add(fileInfo);
        b(j, fileInfo);
    }

    public void b(String str, String str2, long j, tmsdkobf.rt.a aVar) {
        FileMedia fileMedia = new FileMedia();
        fileMedia.mPath = str2;
        fileMedia.type = 2;
        fileMedia.mSize = j;
        fileMedia.mPlayers = aVar.mPlayers;
        this.OR.mVideoFiles.add(fileMedia);
        b(j, fileMedia);
    }

    public String getDetailRule(String str) {
        return "";
    }

    public void jM() {
        this.OL = true;
        if (this.oS != null) {
            this.oS.cancel();
        }
    }

    public boolean jN() {
        return this.OL;
    }

    public void onFoundComRubbish(String str, String str2, long j) {
        rm rmVar = (rm) this.OM.get(str);
        if (rmVar == null) {
            rmVar = (rm) this.ON.get(str);
            if (rmVar == null) {
                if (((rm) this.OO.get(str)) != null) {
                    b(str, str2, j);
                }
                return;
            }
            b(str, str2, j, (tmsdkobf.rt.a) rmVar);
            return;
        }
        a(str, str2, j, (tmsdkobf.rt.a) rmVar);
    }

    public void onFoundEmptyDir(String str, long j) {
    }

    public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
    }

    public void onProcessChange(int i) {
        if (this.pX != 0) {
            this.qe = i;
            int i2 = (int) ((((float) (this.qe + this.qd)) * 100.0f) / ((float) this.pX));
            if (i2 == 100) {
                i2--;
            }
            if (this.NQ != null) {
                this.NQ.onProgressChanged(i2);
            }
        }
    }

    public void onVisit(QFile qFile) {
        if (qFile.type != 4) {
        }
    }
}
