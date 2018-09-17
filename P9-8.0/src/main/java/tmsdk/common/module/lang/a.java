package tmsdk.common.module.lang;

import android.content.Context;
import java.util.HashSet;
import java.util.Iterator;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.f;
import tmsdkobf.kt;
import tmsdkobf.md;

class a extends BaseManagerC {
    private int Ay;
    private HashSet<ILangChangeListener> Az = new HashSet();
    private Context mContext;
    md wD;

    a() {
    }

    void aW(int i) {
        f.d("MultiLangManager", "setCurrentLang:[" + this.Ay + "][" + i + "]");
        this.wD.a("LANG", i, false);
        Iterator it = this.Az.iterator();
        while (it.hasNext()) {
            ((ILangChangeListener) it.next()).onCurrentLang(i, i);
        }
        if (i == 1) {
            kt.saveActionData(1320014);
        } else if (i == 2) {
            kt.saveActionData(1320015);
        }
    }

    void addListener(ILangChangeListener iLangChangeListener) {
        f.d("MultiLangManager", "addListener:[" + iLangChangeListener + "]");
        this.Az.add(iLangChangeListener);
    }

    int getCurrentLang() {
        return this.wD.getInt("LANG", 1);
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.wD = new md("multi_lang_setting");
        this.mContext = context;
    }

    void removeListener(ILangChangeListener iLangChangeListener) {
        f.d("MultiLangManager", "removeListener:[" + iLangChangeListener + "]");
        this.Az.remove(iLangChangeListener);
    }
}
