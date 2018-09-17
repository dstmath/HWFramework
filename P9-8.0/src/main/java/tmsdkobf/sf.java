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
import tmsdk.common.tcc.DeepCleanEngine;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.common.utils.q;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.FileMedia;
import tmsdk.fg.module.spacemanager.FileScanResult;

public class sf implements Callback {
    private DeepCleanEngine Pa;
    boolean QS = false;
    private int QT = 0;
    int QU = 0;
    int QV = 0;
    HashMap<String, qt> QW = new HashMap();
    HashMap<String, qt> QX = new HashMap();
    HashMap<String, qt> QY = new HashMap();
    List<String> QZ = new ArrayList();
    List<qt> Ra = new ArrayList();
    FileScanResult Rb = new FileScanResult();
    a Rc;
    int Rd = 7;
    Context mContext;

    public interface a {
        void a(long j, Object obj);

        void onProgressChanged(int i);
    }

    public sf(Context context, a aVar, int i) {
        this.mContext = context;
        this.Rc = aVar;
        this.Rd = i;
    }

    private static void R(List<FileMedia> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<FileMedia>() {
                /* renamed from: a */
                public int compare(FileMedia fileMedia, FileMedia fileMedia2) {
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
            });
        }
    }

    private static void S(List<FileMedia> list) {
        if (list != null) {
            R(list);
            Collection arrayList = new ArrayList();
            HashMap hashMap = new HashMap();
            for (FileMedia fileMedia : list) {
                if (q.cK(fileMedia.pkg)) {
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
                /* renamed from: c */
                public int compare(ArrayList<FileMedia> arrayList, ArrayList<FileMedia> arrayList2) {
                    if (arrayList.size() == arrayList2.size()) {
                        return 0;
                    }
                    return arrayList.size() <= arrayList2.size() ? 1 : -1;
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
                query.close();
            } catch (Throwable th) {
                query.close();
                throw th;
            }
        }
    }

    public static void a(FileScanResult fileScanResult, Context context, boolean z) {
        String toLowerCase;
        rl rlVar = new rl();
        rlVar.kj();
        if (fileScanResult.mBigFiles != null) {
            for (FileInfo fileInfo : fileScanResult.mBigFiles) {
                fileInfo.mSrcName = rlVar.a(rk.a(fileInfo.mPath, rl.Ps).toLowerCase(), null, z);
            }
        }
        if (fileScanResult.mRadioFiles != null) {
            for (FileMedia fileMedia : fileScanResult.mRadioFiles) {
                toLowerCase = rk.a(fileMedia.mPath, rl.Ps).toLowerCase();
                fileMedia.pkg = rlVar.k(toLowerCase, z);
                fileMedia.mSrcName = rlVar.l(toLowerCase, z);
                a(fileMedia, context);
            }
            S(fileScanResult.mRadioFiles);
        }
        if (fileScanResult.mVideoFiles != null) {
            for (FileMedia fileMedia2 : fileScanResult.mVideoFiles) {
                toLowerCase = rk.a(fileMedia2.mPath, rl.Ps).toLowerCase();
                fileMedia2.pkg = rlVar.k(toLowerCase, z);
                fileMedia2.mSrcName = rlVar.l(toLowerCase, z);
            }
            S(fileScanResult.mVideoFiles);
        }
    }

    private void b(long j, Object obj) {
        if (this.Rc != null) {
            this.Rc.a(j, obj);
        }
    }

    private boolean kx() {
        se seVar = new se();
        if (!seVar.U(this.mContext)) {
            return false;
        }
        if (seVar.QO != null) {
            this.QZ.addAll(seVar.QO);
        }
        List<tmsdkobf.se.a> list = seVar.QR;
        if (list != null) {
            for (tmsdkobf.se.a aVar : list) {
                if (q.cJ(aVar.Ok)) {
                    this.QZ.add(aVar.Ok);
                }
            }
        }
        if (!((this.Rd & 1) == 0 || seVar.QP == null)) {
            for (qt qtVar : seVar.QP) {
                this.Ra.add(qtVar);
                this.QW.put(qtVar.Oj, qtVar);
            }
        }
        if (!((this.Rd & 2) == 0 || seVar.QQ == null)) {
            for (qt qtVar2 : seVar.QQ) {
                this.Ra.add(qtVar2);
                this.QX.put(qtVar2.Oj, qtVar2);
            }
        }
        if ((this.Rd & 4) != 0) {
            qt qtVar3 = new qt();
            qtVar3.Ol = "10240,-";
            qtVar3.Op = "0";
            if ((this.Rd & 256) != 0) {
                qtVar3.mFileName = "/\\.(zip|rar|pdf|doc|apk|ppt|txt|log|chm|docx|pptx|iso|7z|tar|gz)";
            }
            this.Ra.add(qtVar3);
            this.QY.put(qtVar3.Oj, qtVar3);
        }
        return true;
    }

    private void ky() {
        this.QZ.addAll(rk.kh());
        String[] strArr = new String[this.QZ.size()];
        this.QZ.toArray(strArr);
        this.Pa.setWhitePaths(strArr);
        String[] strArr2 = new String[this.Ra.size()];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strArr2.length; i++) {
            qt.a(stringBuilder, (qt) this.Ra.get(i));
            strArr2[i] = stringBuilder.toString();
            stringBuilder.setLength(0);
        }
        this.Pa.setComRubRule(strArr2);
        List<String> jZ = rk.jZ();
        List ki = rk.ki();
        if (ki != null) {
            this.QT = ki.size();
            if (jZ != null) {
                for (String str : jZ) {
                    if (!kA()) {
                        this.Pa.scanPath(str, "/");
                        this.QU += this.QV;
                    }
                }
            }
        }
    }

    public FileScanResult Z(boolean z) {
        if (!kx() || this.Ra.size() == 0) {
            return this.Rb;
        }
        this.Pa = SdcardScannerFactory.getDeepCleanEngine(this, 2);
        if (this.Pa != null) {
            ky();
            this.Pa.release();
            this.Pa = null;
        }
        a(this.Rb, this.mContext, z);
        return this.Rb;
    }

    public void a(String str, String str2, long j) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.type = 3;
        fileInfo.mPath = str2;
        fileInfo.mSize = j;
        this.Rb.mBigFiles.add(fileInfo);
        b(j, fileInfo);
    }

    public void a(String str, String str2, long j, tmsdkobf.se.a aVar) {
        FileMedia fileMedia = new FileMedia();
        fileMedia.mPath = str2;
        fileMedia.type = 1;
        fileMedia.mSize = j;
        fileMedia.mPlayers = aVar.mPlayers;
        this.Rb.mRadioFiles.add(fileMedia);
        b(j, fileMedia);
    }

    public void b(String str, String str2, long j, tmsdkobf.se.a aVar) {
        FileMedia fileMedia = new FileMedia();
        fileMedia.mPath = str2;
        fileMedia.type = 2;
        fileMedia.mSize = j;
        fileMedia.mPlayers = aVar.mPlayers;
        this.Rb.mVideoFiles.add(fileMedia);
        b(j, fileMedia);
    }

    public String getDetailRule(String str) {
        return "";
    }

    public boolean kA() {
        return this.QS;
    }

    public void kz() {
        this.QS = true;
        if (this.Pa != null) {
            this.Pa.cancel();
        }
    }

    public void onDirectoryChange(String str, int i) {
    }

    public void onFoundComRubbish(String str, String str2, long j) {
        qt qtVar = (qt) this.QW.get(str);
        if (qtVar == null) {
            qtVar = (qt) this.QX.get(str);
            if (qtVar == null) {
                if (((qt) this.QY.get(str)) != null) {
                    a(str, str2, j);
                }
                return;
            }
            b(str, str2, j, (tmsdkobf.se.a) qtVar);
            return;
        }
        a(str, str2, j, (tmsdkobf.se.a) qtVar);
    }

    public void onFoundEmptyDir(String str, long j) {
    }

    public void onFoundKeySoftRubbish(String str, String[] strArr, long j) {
    }

    public void onFoundSoftRubbish(String str, String str2, String str3, long j) {
    }

    public void onProcessChange(int i) {
        if (this.QT != 0) {
            this.QV = i;
            int i2 = (int) ((((float) (this.QV + this.QU)) * 100.0f) / ((float) this.QT));
            if (i2 == 100) {
                i2--;
            }
            if (this.Rc != null) {
                this.Rc.onProgressChanged(i2);
            }
        }
    }

    public void onVisit(QFile qFile) {
        int i = qFile.type;
    }
}
