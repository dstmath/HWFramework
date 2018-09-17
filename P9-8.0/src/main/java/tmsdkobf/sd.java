package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.fg.module.spacemanager.FileMedia;
import tmsdk.fg.module.spacemanager.FileScanResult;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdkobf.sf.a;

public class sd implements Runnable {
    ri Pm;
    sf QG;
    public ISpaceScanListener QH;
    a QI;
    Context mContext = TMSDKContext.getApplicaionContext();
    Handler vW = new Handler(Looper.getMainLooper());
    boolean wO = false;

    public sd(ri riVar) {
        this.Pm = riVar;
    }

    private void a(a aVar, FileScanResult fileScanResult, boolean z) {
        List<OfflineVideo> ko = new rp(null, z).ko();
        if (ko != null && ko.size() != 0) {
            long j = 0;
            List arrayList = new ArrayList();
            rl rlVar = null;
            for (OfflineVideo offlineVideo : ko) {
                if (rlVar == null) {
                    rlVar = new rl();
                    rlVar.kj();
                }
                if (TextUtils.isEmpty(offlineVideo.mAppName)) {
                    String toLowerCase = rk.a(offlineVideo.mPath, rl.Ps).toLowerCase();
                    offlineVideo.mPackage = rlVar.k(toLowerCase, z);
                    offlineVideo.mAppName = rlVar.l(toLowerCase, z);
                }
                FileMedia fileMedia = new FileMedia();
                fileMedia.type = 2;
                fileMedia.mPath = offlineVideo.mPath;
                fileMedia.title = offlineVideo.mTitle;
                fileMedia.pkg = offlineVideo.mPackage;
                fileMedia.mSrcName = offlineVideo.mAppName;
                fileMedia.mSize = offlineVideo.mSize;
                fileMedia.mOfflineVideo = offlineVideo;
                j += fileMedia.mSize;
                arrayList.add(fileMedia);
                if (aVar != null) {
                    aVar.a(j, fileMedia);
                }
            }
            if (fileScanResult.mVideoFiles != null) {
                arrayList.addAll(fileScanResult.mVideoFiles);
            }
            fileScanResult.mVideoFiles = arrayList;
        }
    }

    private void kw() {
        if (this.QI == null) {
            this.QI = new a() {
                public void a(long j, final Object -l_4_R) {
                    if (sd.this.QH != null) {
                        sd.this.vW.post(new Runnable() {
                            public void run() {
                                sd.this.QH.onFound(-l_4_R);
                            }
                        });
                    }
                }

                public void onProgressChanged(int -l_2_I) {
                    if (sd.this.QH != null) {
                        if (-l_2_I >= 100) {
                            -l_2_I = 99;
                        }
                        final int i = -l_2_I;
                        sd.this.vW.post(new Runnable() {
                            public void run() {
                                sd.this.QH.onProgressChanged(i);
                            }
                        });
                    }
                }
            };
        }
    }

    public boolean a(ISpaceScanListener iSpaceScanListener) {
        if (this.QH != null) {
            return false;
        }
        this.QH = iSpaceScanListener;
        im.bJ().addTask(this, "scanbigfile");
        return true;
    }

    public void kv() {
        this.wO = true;
        if (this.QG != null) {
            this.QG.kz();
        }
    }

    public void run() {
        kw();
        boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
        if (this.QH != null) {
            this.vW.post(new Runnable() {
                public void run() {
                    sd.this.QH.onStart();
                }
            });
            int i = ((1 | 2) | 4) | 256;
            if (this.wO) {
                this.vW.post(new Runnable() {
                    public void run() {
                        sd.this.QH.onCancelFinished();
                        sd.this.QH = null;
                        sd.this.wO = false;
                        sd.this.Pm.bX(0);
                    }
                });
                return;
            }
            this.QG = new sf(this.mContext, this.QI, i);
            final FileScanResult Z = this.QG.Z(isENG);
            a(this.QI, Z, isENG);
            FileScanResult fileScanResult = Z;
            if (this.wO) {
                this.vW.post(new Runnable() {
                    public void run() {
                        sd.this.QH.onCancelFinished();
                        sd.this.QH.onFinish(0, Z);
                        sd.this.QH = null;
                        sd.this.wO = false;
                        sd.this.Pm.bX(0);
                    }
                });
            } else {
                this.vW.post(new Runnable() {
                    public void run() {
                        sd.this.QH.onFinish(0, Z);
                        sd.this.QH = null;
                        sd.this.Pm.bX(0);
                    }
                });
            }
            this.QG = null;
        }
    }
}
