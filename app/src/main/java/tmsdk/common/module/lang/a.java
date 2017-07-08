package tmsdk.common.module.lang;

import android.content.Context;
import java.util.HashSet;
import java.util.Iterator;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.d;
import tmsdkobf.ma;
import tmsdkobf.nc;

/* compiled from: Unknown */
class a extends BaseManagerC {
    private int CL;
    private HashSet<ILangChangeListener> CM;
    private Context mContext;
    nc nf;

    a() {
        this.CM = new HashSet();
    }

    void addListener(ILangChangeListener iLangChangeListener) {
        d.e("MultiLangManager", "addListener:[" + iLangChangeListener + "]");
        this.CM.add(iLangChangeListener);
    }

    void bN(int i) {
        d.e("MultiLangManager", "setCurrentLang:[" + this.CL + "][" + i + "]");
        this.nf.a("LANG", i, false);
        Iterator it = this.CM.iterator();
        while (it.hasNext()) {
            ((ILangChangeListener) it.next()).onCurrentLang(i, i);
        }
        if (i == 1) {
            ma.bx(1320014);
        } else if (i == 2) {
            ma.bx(1320015);
        }
    }

    int getCurrentLang() {
        return this.nf.getInt("LANG", 1);
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.nf = new nc("multi_lang_setting");
        this.mContext = context;
    }

    void removeListener(ILangChangeListener iLangChangeListener) {
        d.e("MultiLangManager", "removeListener:[" + iLangChangeListener + "]");
        this.CM.remove(iLangChangeListener);
    }
}
