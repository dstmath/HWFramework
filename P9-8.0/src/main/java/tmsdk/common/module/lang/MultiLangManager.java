package tmsdk.common.module.lang;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

public class MultiLangManager extends BaseManagerC {
    private a Ax;

    public void addListener(ILangChangeListener iLangChangeListener) {
        this.Ax.addListener(iLangChangeListener);
    }

    public int getCurrentLang() {
        return this.Ax.getCurrentLang();
    }

    public boolean isCHS() {
        return getCurrentLang() == 1;
    }

    public boolean isENG() {
        return getCurrentLang() == 2;
    }

    public void onCreate(Context context) {
        this.Ax = new a();
        this.Ax.onCreate(context);
        a(this.Ax);
    }

    public void onCurrentLangNotify(int i) {
        this.Ax.aW(i);
    }

    public void removeListener(ILangChangeListener iLangChangeListener) {
        this.Ax.removeListener(iLangChangeListener);
    }
}
