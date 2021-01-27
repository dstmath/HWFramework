package huawei.android.provider;

import android.provider.IHwMediaStore;
import android.util.Log;
import huawei.android.provider.HanziToPinyin;
import java.util.ArrayList;

public class HwMediaStoreImpl implements IHwMediaStore {
    private static final String TAG = null;
    private static IHwMediaStore sInstance = new HwMediaStoreImpl();

    public String getPinyinForSort(String name) {
        HanziToPinyin hanZi = HanziToPinyin.getInstance();
        if (!hanZi.hasChineseTransliterator()) {
            Log.w(TAG, "Has no chinese transliterator.");
            return name;
        }
        ArrayList<HanziToPinyin.Token> tokens = hanZi.get(name);
        StringBuilder pinYin = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            pinYin.append(tokens.get(i).target);
            if (tokens.get(i).type == 2) {
                pinYin.append('.');
            }
        }
        return pinYin.toString();
    }

    public static IHwMediaStore getDefault() {
        return sInstance;
    }
}
