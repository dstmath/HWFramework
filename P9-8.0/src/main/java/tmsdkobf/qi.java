package tmsdkobf;

import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.TMSDKContext;
import tmsdk.fg.module.cleanV2.IScanTaskCallBack;
import tmsdk.fg.module.cleanV2.RubbishEntity;
import tmsdk.fg.module.cleanV2.RubbishHolder;

public class qi {
    static b MJ;
    public static long startTime;
    private final int MC;
    private final int MD;
    List<String> ME;
    List<a> MF;
    private qh MG;
    final String MH;
    final String MI;
    private AtomicBoolean MK;

    public interface a {
        void a(File file, qj qjVar);
    }

    public interface b {
        void close();

        void println(String str);
    }

    public qi() {
        this.MC = Runtime.getRuntime().availableProcessors();
        this.MD = 50000;
        this.MF = null;
        this.MG = null;
        this.MH = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.MI = "fgtScanRule.txt";
        this.MK = null;
        this.MG = new qh(false);
        this.MK = new AtomicBoolean(false);
        MJ = i(this.MH + File.separator + "fgtProfile.txt", false);
    }

    private static String cS(String str) {
        PackageManager packageManager = TMSDKContext.getApplicaionContext().getPackageManager();
        try {
            return (String) packageManager.getApplicationInfo(str, 1).loadLabel(packageManager);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static b i(final String str, final boolean z) {
        try {
            return new b() {
                private PrintWriter Na;

                public void close() {
                    if (this.Na != null) {
                        this.Na.close();
                    }
                }

                public void println(String str) {
                    if (this.Na != null) {
                        this.Na.println(str);
                    }
                }
            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static long jp() {
        return System.currentTimeMillis() - startTime;
    }

    public void a(List<ql> list, RubbishHolder rubbishHolder) {
        for (ql a : list) {
            a.a(rubbishHolder);
        }
    }

    public void a(IScanTaskCallBack iScanTaskCallBack, String str, boolean z) {
        startTime = System.currentTimeMillis();
        this.MK.set(false);
        this.MG.U(z);
        this.ME = rh.jZ();
        this.MF = this.MG.cQ(str);
        if (this.MF != null && this.MF.size() >= 1) {
            String cS = cS(str);
            final boolean z2 = cS != null;
            final String str2 = cS == null ? ((a) this.MF.get(0)).mAppName : cS;
            final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
            final IScanTaskCallBack iScanTaskCallBack2 = iScanTaskCallBack;
            final String str3 = str;
            a anonymousClass1 = new a() {
                long ML = 0;
                long MM = 0;
                String MN;

                public void a(final File file, final qj qjVar) {
                    newSingleThreadExecutor.execute(new Runnable() {
                        public void run() {
                            String absolutePath = file.getAbsolutePath();
                            IScanTaskCallBack iScanTaskCallBack = iScanTaskCallBack2;
                            String absolutePath2 = file.getAbsolutePath();
                            AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                            long j = anonymousClass1.ML + 1;
                            anonymousClass1.ML = j;
                            iScanTaskCallBack.onDirectoryChange(absolutePath2, (int) j);
                            int i = !z2 ? 4 : 0;
                            List linkedList = new LinkedList();
                            linkedList.add(absolutePath);
                            if (qjVar != null) {
                                boolean z = i != 0 ? 3 != qjVar.Nt : 1 == qjVar.Nt;
                                RubbishEntity rubbishEntity;
                                if (qjVar.mDescription.equals(AnonymousClass1.this.MN)) {
                                    if ((System.currentTimeMillis() - AnonymousClass1.this.MM <= 1000 ? 1 : null) == null) {
                                        AnonymousClass1.this.MM = System.currentTimeMillis();
                                        rubbishEntity = new RubbishEntity(i, linkedList, z, qjVar.No, str2, str3, qjVar.mDescription);
                                        iScanTaskCallBack2.onRubbishFound(rubbishEntity);
                                        rubbishEntity.setExtendData(qjVar.Nu, qjVar.Ne, qjVar.Nw);
                                        return;
                                    }
                                    return;
                                }
                                AnonymousClass1.this.MN = qjVar.mDescription;
                                AnonymousClass1.this.MM = System.currentTimeMillis();
                                rubbishEntity = new RubbishEntity(i, linkedList, z, qjVar.No, str2, str3, qjVar.mDescription);
                                rubbishEntity.setExtendData(qjVar.Nu, qjVar.Ne, qjVar.Nw);
                                iScanTaskCallBack2.onRubbishFound(rubbishEntity);
                            }
                        }
                    });
                }
            };
            qj.Nm = 0;
            MJ.println("paserRootPath\t" + jp());
            ArrayList arrayList = new ArrayList();
            Log.d("fgtScan", "core size:" + this.MC);
            ExecutorService threadPoolExecutor = new ThreadPoolExecutor(this.MC << 1, this.MC << 1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(50000));
            final RubbishHolder rubbishHolder = new RubbishHolder();
            final IScanTaskCallBack iScanTaskCallBack3 = iScanTaskCallBack;
            newSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    iScanTaskCallBack3.onScanStarted();
                }
            });
            for (a aVar : this.MF) {
                String str4 = ((String) this.ME.get(0)) + aVar.MB;
                Log.d("fgtScan", "root path:" + str4);
                File file = new File(str4);
                if (file.exists() && file.isDirectory()) {
                    ql qlVar = new ql(str, str2, aVar.MB, z2);
                    qlVar.a(file, threadPoolExecutor);
                    ql a = this.MG.a(aVar.MB, qlVar);
                    if (a != null) {
                        MJ.println("paserDetailRule-" + str4 + "\t" + jp());
                        arrayList.add(a);
                        Log.d("fgtScan", "resolving:" + str4);
                        a.a("", MJ, anonymousClass1, this.MK);
                        if (this.MK.get()) {
                            threadPoolExecutor.shutdownNow();
                            try {
                                threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            qk.jq();
                            final ArrayList arrayList2 = arrayList;
                            final RubbishHolder rubbishHolder2 = rubbishHolder;
                            final IScanTaskCallBack iScanTaskCallBack4 = iScanTaskCallBack;
                            newSingleThreadExecutor.execute(new Runnable() {
                                public void run() {
                                    qi.this.a(arrayList2, rubbishHolder2);
                                    iScanTaskCallBack4.onScanCanceled(rubbishHolder2);
                                }
                            });
                            return;
                        }
                        Log.d("fgtScan", "resolve over:" + str4);
                        MJ.println("after-" + str4 + "-resolved\t" + jp());
                    } else {
                        Log.d("fgtScan", "can not parser rule!!!");
                        iScanTaskCallBack.onScanError(-18, null);
                        return;
                    }
                }
                Log.d("fgtScan", "root path:" + str4 + "  is not exist or not a directory skiped!");
            }
            threadPoolExecutor.shutdown();
            try {
                threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            final long jp = jp();
            final ArrayList arrayList3 = arrayList;
            final IScanTaskCallBack iScanTaskCallBack5 = iScanTaskCallBack;
            newSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    qi.this.a(arrayList3, rubbishHolder);
                    iScanTaskCallBack5.onScanFinished(rubbishHolder);
                }
            });
            Log.d("fgtScan", "scan all over\t" + jp);
            MJ.println("scan all over\t" + jp);
            MJ.close();
            qk.jq();
            return;
        }
        Log.d("fgtScan", "can not get root path!!!");
        iScanTaskCallBack.onScanError(-17, null);
    }

    public void cancel() {
        Log.d("fgtScan", "cancel is called");
        this.MK.set(true);
    }
}
