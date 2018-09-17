package tmsdk.common.module.lang;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public class MultiLangManager extends BaseManagerC {
    private a CK;

    public void addListener(ILangChangeListener iLangChangeListener) {
        this.CK.addListener(iLangChangeListener);
    }

    public int getCurrentLang() {
        return this.CK.getCurrentLang();
    }

    public boolean isCHS() {
        return getCurrentLang() == 1;
    }

    public boolean isENG() {
        return getCurrentLang() == 2;
    }

    public void onCreate(Context context) {
        this.CK = new a();
        this.CK.onCreate(context);
        a(this.CK);
    }

    public void onCurrentLangNotify(int i) {
        this.CK.bN(i);
    }

    public void removeListener(ILangChangeListener iLangChangeListener) {
        this.CK.removeListener(iLangChangeListener);
    }
}
