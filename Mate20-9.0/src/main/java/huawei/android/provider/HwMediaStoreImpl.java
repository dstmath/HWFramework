package huawei.android.provider;

import android.provider.IHwMediaStore;
import android.util.Log;
import huawei.android.provider.HanziToPinyin;
import java.util.ArrayList;

public class HwMediaStoreImpl implements IHwMediaStore {
    private static final String TAG = null;
    private static IHwMediaStore mInstance = new HwMediaStoreImpl();

    public String getPinyinForSort(String name) {
        HanziToPinyin hanzi = HanziToPinyin.getInstance();
        if (!hanzi.hasChineseTransliterator()) {
            Log.w(TAG, "Has no chinese transliterator.");
            return name;
        }
        ArrayList<HanziToPinyin.Token> tokens = hanzi.get(name);
        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            pinyin.append(tokens.get(i).target);
            if (tokens.get(i).type == 2) {
                pinyin.append('.');
            }
        }
        return pinyin.toString();
    }

    public static IHwMediaStore getDefault() {
        return mInstance;
    }
}
