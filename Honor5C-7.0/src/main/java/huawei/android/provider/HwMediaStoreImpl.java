package huawei.android.provider;

import android.provider.IHwMediaStore;
import android.util.Log;
import huawei.android.provider.HanziToPinyin.Token;
import java.util.ArrayList;

public class HwMediaStoreImpl implements IHwMediaStore {
    private static final String TAG = null;
    private static IHwMediaStore mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.provider.HwMediaStoreImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.provider.HwMediaStoreImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.provider.HwMediaStoreImpl.<clinit>():void");
    }

    public String getPinyinForSort(String name) {
        HanziToPinyin hanzi = HanziToPinyin.getInstance();
        if (hanzi.hasChineseTransliterator()) {
            ArrayList<Token> tokens = hanzi.get(name);
            StringBuilder pinyin = new StringBuilder();
            for (int i = 0; i < tokens.size(); i++) {
                pinyin.append(((Token) tokens.get(i)).target);
                if (((Token) tokens.get(i)).type == 2) {
                    pinyin.append('.');
                }
            }
            return pinyin.toString();
        }
        Log.w(TAG, "Has no chinese transliterator.");
        return name;
    }

    public static IHwMediaStore getDefault() {
        return mInstance;
    }
}
