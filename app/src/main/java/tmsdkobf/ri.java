package tmsdkobf;

import android.text.TextUtils;
import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.tcc.QFile;
import tmsdk.common.tcc.QSdcardScanner;
import tmsdk.common.tcc.SdcardScannerFactory;
import tmsdk.fg.module.spacemanager.WeChatFileModel;

/* compiled from: Unknown */
public class ri extends rh<rj> {
    private int NG;
    private long NH;
    public boolean NI;
    private QSdcardScanner NJ;
    private Calendar mCalendar;

    /* compiled from: Unknown */
    private class a implements tmsdkobf.qv.a {
        List<rj> NE;
        int NK;
        long NL;
        int NM;
        long NN;
        rj NO;
        String NP;
        tmsdkobf.ru.a NQ;
        final /* synthetic */ ri NR;

        private a(ri riVar) {
            this.NR = riVar;
        }

        private int H(List<rj> list) {
            int i = 0;
            Iterator it = list.iterator();
            while (true) {
                int i2 = i;
                if (!it.hasNext()) {
                    return i2;
                }
                rj rjVar = (rj) it.next();
                i = rjVar.pN == null ? i2 : rjVar.pN.size() + i2;
            }
        }

        public boolean jF() {
            if (this.NE == null || this.NE.size() == 0 || this.NR.NJ == null) {
                return false;
            }
            int H = H(this.NE);
            boolean z = false;
            for (rj rjVar : this.NE) {
                rjVar.mFileModes.clear();
                if (this.NK <= 80000) {
                    boolean z2;
                    this.NO = rjVar;
                    List list = rjVar.pN;
                    if (list != null && list.size() > 0) {
                        z2 = z;
                        int i = 0;
                        while (i < list.size()) {
                            boolean z3 = z2 + 1;
                            if (!(this.NQ == null || H == 0)) {
                                int i2 = (z3 * 100) / H;
                                if (100 == i2) {
                                    i2 = 99;
                                }
                                this.NQ.onProgressChanged(i2);
                            }
                            String str = (String) list.get(i);
                            if (this.NR.NI) {
                                if (this.NQ != null) {
                                    this.NQ.onCancel();
                                }
                                this.NR.NI = false;
                                return true;
                            }
                            if (!TextUtils.isEmpty(str)) {
                                if (this.NR.NG > 80000) {
                                    z2 = z3;
                                    break;
                                }
                                this.NP = str;
                                if (this.NR.NJ != null) {
                                    this.NR.NJ.startScan(str);
                                }
                            }
                            i++;
                            z2 = z3;
                        }
                    } else {
                        z2 = z;
                    }
                    z = z2;
                }
            }
            if (this.NR.NJ != null) {
                this.NR.NJ.release();
                this.NR.NJ = null;
            }
            for (rj rjVar2 : this.NE) {
                if (!(rjVar2 == null || rjVar2.mFileModes == null || rjVar2.mCleanType != 2)) {
                    Collections.sort(rjVar2.mFileModes);
                }
            }
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onFound(int i, QFile qFile) {
            if (!this.NR.jD() && this.NK <= 80000 && this.NO != null && this.NP != null) {
                WeChatFileModel a = this.NR.a(this.NO, this.NP, qFile.filePath, qFile.size);
                if (a != null) {
                    this.NL += (long) a.mFileSize;
                    this.NN = ((long) a.mFileSize) + this.NN;
                    this.NK++;
                    this.NM++;
                    if (this.NM > 0) {
                        if (this.NQ != null) {
                            this.NQ.a(this.NN, this.NO.jG());
                        }
                        this.NM = 0;
                        this.NN = 0;
                    }
                }
            }
        }
    }

    public ri(List<rj> list, int i) {
        super(list, i);
        this.NG = 0;
        this.NH = 0;
        this.NI = false;
        this.mCalendar = Calendar.getInstance();
    }

    private boolean a(rj rjVar, String str) {
        if (rjVar.NX == null) {
            return true;
        }
        return rjVar.NX.match(re.aN(str));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WeChatFileModel a(rj rjVar, String str, String str2, long j) {
        int i = 0;
        if (rjVar == null || str == null || j == 0 || !a(rjVar, str2)) {
            return null;
        }
        WeChatFileModel weChatFileModel = new WeChatFileModel();
        weChatFileModel.mPrefixPath = str;
        weChatFileModel.mSubFilePath = str2.substring(str.length());
        if (j > 0) {
            i = 1;
        }
        weChatFileModel.mFileSize = i == 0 ? 4 : (int) j;
        if (rjVar.mCleanType == 2) {
            weChatFileModel.mModifyTime = new File(str2).lastModified();
            this.mCalendar.setTimeInMillis(weChatFileModel.mModifyTime);
            weChatFileModel.mYear = (short) ((short) this.mCalendar.get(1));
            weChatFileModel.mMonth = (byte) ((byte) (this.mCalendar.get(2) + 1));
            weChatFileModel.mDay = (byte) ((byte) this.mCalendar.get(5));
        }
        rjVar.mFileModes.add(weChatFileModel);
        rjVar.mTotalSize += (long) weChatFileModel.mFileSize;
        return weChatFileModel;
    }

    public boolean a(tmsdkobf.ru.a aVar) {
        a aVar2 = new a();
        aVar2.NE = this.NE;
        aVar2.NQ = aVar;
        this.NJ = SdcardScannerFactory.getQSdcardScanner(12, aVar2, null);
        if (this.NJ == null || !aVar2.jF()) {
            return false;
        }
        this.NG = aVar2.NK;
        this.NH = aVar2.NL;
        return true;
    }

    void bl() {
        if (this.NJ != null) {
            this.NJ.cancleScan();
        }
    }
}
