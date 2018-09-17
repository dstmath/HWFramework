package tmsdk.common.utils;

import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.TccCryptor;
import tmsdkobf.dz;
import tmsdkobf.ea;
import tmsdkobf.fn;
import tmsdkobf.ls;

public class d extends c {
    public ArrayList<dz> LD = null;
    Context mContext;

    public d(Context context, String str) {
        super(context, str);
        this.mContext = context;
    }

    private ea v(ArrayList<dz> arrayList) {
        ea eaVar = new ea();
        eaVar.iC = arrayList;
        return eaVar;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0050 A:{SYNTHETIC, Splitter: B:21:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005f A:{SYNTHETIC, Splitter: B:29:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x006c A:{SYNTHETIC, Splitter: B:36:0x006c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(String str, String str2, ls lsVar, ArrayList<dz> arrayList) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File(str));
            if (lsVar != null) {
                fileOutputStream2.write(lsVar.eD());
            }
            try {
                ea v = v(arrayList);
                fn fnVar = new fn();
                fnVar.B("UTF-8");
                fnVar.put(str2, v);
                fileOutputStream2.write(TccCryptor.encrypt(fnVar.l(), null));
                fileOutputStream2.flush();
                fileOutputStream2.close();
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return true;
            } catch (FileNotFoundException e4) {
                e = e4;
                fileOutputStream = fileOutputStream2;
                try {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e2 = e5;
                fileOutputStream = fileOutputStream2;
                e2.printStackTrace();
                if (fileOutputStream != null) {
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fileOutputStream2;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e6) {
                        e6.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            return false;
        } catch (IOException e8) {
            e22 = e8;
            e22.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            return false;
        }
    }

    public boolean h(String str, boolean z) {
        ea eaVar = null;
        try {
            eaVar = (ea) a(TMSDKContext.getApplicaionContext(), str, "UTF-8", z).a(this.LA, new ea());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (eaVar == null || eaVar.iC == null) {
            return false;
        }
        this.LD = eaVar.iC;
        return true;
    }
}
