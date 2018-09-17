package huawei.android.widget;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.attestation.HwAttestationStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwAlphaIndexResourceManager {
    public static final int LANDSCAPE_ALPHA_COUNT_MAX = 18;
    private static final String LanguageIndexerFile = "LanguageIndexerFile.conf";
    public static final int PORTRAIT_ALPHA_COUNT_MAX = 28;
    public static final String ROOT_ALPHA_INDEX = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z";
    private static final String TAG = "HwAlphaIndexResourceManager";
    private static Map<String, String> languageIndexerMap;
    private List<Alpha> labelList;

    public static class Alpha {
        public static final int COLLAPSIBLE = 1;
        public static final int DELETABLE = 2;
        public static final int NORMAL = 0;
        public String content;
        public int desc;

        public Alpha(String content, int desc) {
            this.content = content;
            this.desc = desc;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.HwAlphaIndexResourceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.HwAlphaIndexResourceManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.HwAlphaIndexResourceManager.<clinit>():void");
    }

    private HwAlphaIndexResourceManager(Locale locale) {
        this.labelList = new ArrayList();
        String validLocale = locale.getLanguage();
        if ("zh".equals(validLocale)) {
            String region = locale.getCountry();
            if ("TW".equals(region)) {
                validLocale = Collator.getInstance(locale).compare("\u5df4", "\u535c") == -1 ? "zh_TW" : "zh_Hant";
            } else if ("HK".equals(region) || "MO".equals(region)) {
                validLocale = "zh_Hant";
            }
        }
        if ("fa".equals(validLocale)) {
            if ("AF".equals(locale.getCountry())) {
                validLocale = "fa_AF";
            }
        }
        try {
            languageIndexerMap = parseLanguageIndexerFile(Resources.getSystem().getAssets().open(LanguageIndexerFile));
        } catch (IOException e) {
            Log.e(TAG, "Can not find the LanguageIndexerFile.conf file");
        }
        if ("zh_Hant".equals(validLocale)) {
            this.labelList = getLabelList("TRADITIONAL_CHINESE_ALPHA_INDEX");
        } else if ("zh_TW".equals(validLocale)) {
            this.labelList = getLabelList("TAIWAN_ALPHA_INDEX");
        } else if ("fa_AF".equals(validLocale)) {
            this.labelList = getLabelList("PASHOTO_ALPHA_INDEX");
        } else if ("tl".equals(validLocale)) {
            this.labelList = getLabelList("FILIPINO_ALPHA_INDEX");
        } else {
            this.labelList = getLabelList(locale.getDisplayLanguage(Locale.US).split(" ")[0].toUpperCase(Locale.US) + "_ALPHA_INDEX");
        }
        if (this.labelList.isEmpty()) {
            this.labelList = getLabelList("ROOT_ALPHA_INDEX");
        }
    }

    private List<Alpha> getLabelList(String alphaIndex) {
        alphaIndex = (String) languageIndexerMap.get(alphaIndex);
        if (alphaIndex == null) {
            return new ArrayList();
        }
        String[] alphaArray = alphaIndex.split(" ");
        List<Alpha> ret = new ArrayList();
        for (String item : alphaArray) {
            String item2;
            if (!TextUtils.isEmpty(item2.trim())) {
                item2 = item2.trim();
                Alpha alpha = new Alpha();
                int pos = item2.indexOf("(");
                if (-1 != pos) {
                    alpha.content = item2.substring(0, pos);
                    alpha.desc = Character.digit(item2.charAt(pos + 1), 10);
                } else {
                    alpha.content = item2;
                    alpha.desc = 0;
                }
                ret.add(alpha);
            }
        }
        return ret;
    }

    private Map<String, String> parseLanguageIndexerFile(InputStream file) {
        Throwable th;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        Map<String, String> languageMapIndexer = new HashMap();
        languageMapIndexer.put("ROOT_ALPHA_INDEX", ROOT_ALPHA_INDEX);
        try {
            InputStreamReader reader = new InputStreamReader(file, "utf-8");
            try {
                BufferedReader buffReader = new BufferedReader(reader, HwAttestationStatus.CERT_MAX_LENGTH);
                int lineNum = 0;
                while (true) {
                    try {
                        String line = buffReader.readLine();
                        if (line == null) {
                            break;
                        }
                        lineNum++;
                        if (!line.startsWith(AlphaIndexerListView.DIGIT_LABEL)) {
                            String[] entries = line.trim().split(":");
                            if (entries.length != 2) {
                                Log.e(TAG, "Invalid line: " + lineNum);
                            } else {
                                languageMapIndexer.put(entries[0], entries[1]);
                            }
                        }
                    } catch (IOException e) {
                        bufferedReader = buffReader;
                        inputStreamReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = buffReader;
                        inputStreamReader = reader;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Exception when trying to close InputStreamReader.");
                    }
                }
                if (buffReader != null) {
                    try {
                        buffReader.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Exception when trying to close BufferedReader.");
                    }
                }
            } catch (IOException e4) {
                inputStreamReader = reader;
                try {
                    Log.e(TAG, "Exception when parsing LanguageIndexerFile.");
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e5) {
                            Log.e(TAG, "Exception when trying to close InputStreamReader.");
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "Exception when trying to close BufferedReader.");
                        }
                    }
                    return languageMapIndexer;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e7) {
                            Log.e(TAG, "Exception when trying to close InputStreamReader.");
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e8) {
                            Log.e(TAG, "Exception when trying to close BufferedReader.");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                inputStreamReader = reader;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            Log.e(TAG, "Exception when parsing LanguageIndexerFile.");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return languageMapIndexer;
        }
        return languageMapIndexer;
    }

    public static HwAlphaIndexResourceManager getInstance() {
        return getInstance(Locale.getDefault());
    }

    public static HwAlphaIndexResourceManager getInstance(Locale locale) {
        return new HwAlphaIndexResourceManager(locale);
    }

    public String[] getAlphaIndex() {
        return null;
    }

    public List<String> getPortraitDisplayAlphaIndex() {
        return getDisplayFromComplete(getPortraitCompleteAlphaIndex());
    }

    public List<String> getPortraitCompleteAlphaIndex() {
        return getAlphaIndexWithContract();
    }

    public List<String> getLandscapeDisplayAlphaIndex() {
        return getDisplayFromComplete(getLandscapeCompleteAlphaIndex());
    }

    public List<String> getLandscapeCompleteAlphaIndex() {
        return populateBulletAlphaIndex(LANDSCAPE_ALPHA_COUNT_MAX, getAlphaListContent(this.labelList));
    }

    private static List<String> getDisplayFromComplete(List<String> completeList) {
        List<String> ret = new ArrayList();
        for (String item : completeList) {
            if (item.split(" ").length > 1) {
                ret.add(AlphaIndexerListView.BULLET_CHAR);
            } else {
                ret.add(item);
            }
        }
        return ret;
    }

    private List<String> getAlphaIndexWithContract() {
        List<String> ret = new ArrayList();
        StringBuilder bulletContent = new StringBuilder();
        for (Alpha alpha : this.labelList) {
            if (alpha.desc == 0) {
                if (bulletContent.length() > 0) {
                    bulletContent.setLength(bulletContent.length() - 1);
                    ret.add(bulletContent.toString());
                    bulletContent.setLength(0);
                }
                ret.add(alpha.content);
            } else if (alpha.desc == 1) {
                bulletContent.append(alpha.content);
                bulletContent.append(' ');
            }
        }
        return ret;
    }

    public String[] getAlphaIndexWithoutDeletable() {
        return null;
    }

    public String[] getAlphaIndexFewest() {
        return null;
    }

    private List<String> getAlphaListContent(List<Alpha> list) {
        List<String> ret = new ArrayList();
        for (Alpha alpha : list) {
            ret.add(alpha.content);
        }
        return ret;
    }

    public static List<String> getRootPortraitDisplayAlphaIndex() {
        return new ArrayList(Arrays.asList(ROOT_ALPHA_INDEX.split(" ")));
    }

    public static List<String> getRootLandscapeDisplayAlphaIndex() {
        return getDisplayFromComplete(getRootLandscapeCompleteAlphaIndex());
    }

    public static List<String> getRootLandscapeCompleteAlphaIndex() {
        return populateBulletAlphaIndex(LANDSCAPE_ALPHA_COUNT_MAX, new ArrayList(Arrays.asList(ROOT_ALPHA_INDEX.split(" "))));
    }

    public static List<String> populateBulletAlphaIndex(int M, List<String> alphaList) {
        if (alphaList == null || alphaList.size() < M) {
            return alphaList;
        }
        int N = alphaList.size();
        int min = (M - 1) / 2;
        int minBulletCount = 1;
        while ((minBulletCount + 1) * min < N - 1) {
            minBulletCount++;
        }
        int bulletCount = minBulletCount - 1;
        if ((bulletCount + 1) * min == N - 1) {
            bulletCount++;
        }
        int n2 = (N - 1) - ((bulletCount + 1) * min);
        int n1 = min - n2;
        List<String> ret = new ArrayList();
        ret.add((String) alphaList.get(0));
        int pos = 1;
        while (n2 > 0) {
            n2--;
            StringBuilder bulletContent = new StringBuilder();
            int i = 0;
            while (i < bulletCount + 1) {
                bulletContent.append((String) alphaList.get(pos));
                bulletContent.append(' ');
                i++;
                pos++;
            }
            bulletContent.setLength(bulletContent.length() - 1);
            ret.add(bulletContent.toString());
            ret.add((String) alphaList.get(pos));
            pos++;
        }
        while (n1 > 0) {
            n1--;
            bulletContent = new StringBuilder();
            i = 0;
            while (i < bulletCount) {
                bulletContent.append((String) alphaList.get(pos));
                bulletContent.append(' ');
                i++;
                pos++;
            }
            bulletContent.setLength(bulletContent.length() - 1);
            ret.add(bulletContent.toString());
            ret.add((String) alphaList.get(pos));
            pos++;
        }
        for (i = pos; i < N; i++) {
            ret.add((String) alphaList.get(i));
        }
        return ret;
    }
}
