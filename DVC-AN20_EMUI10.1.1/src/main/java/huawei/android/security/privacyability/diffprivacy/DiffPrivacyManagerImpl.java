package huawei.android.security.privacyability.diffprivacy;

import android.text.TextUtils;
import android.util.Log;

public class DiffPrivacyManagerImpl {
    private static final int BLOOMBITS_MAX = 4096;
    private static final int BLOOMBITS_MIN = 1024;
    private static final int BLOOMBITS_ORDER = 1;
    private static final double EPSILON_MAX = 8.0d;
    private static final double EPSILON_MIN = 3.0d;
    private static final int EPSILON_ORDER = 2;
    private static final int HASHNUM_MAX = 128;
    private static final int HASHNUM_MIN = 64;
    private static final int HASHNUM_ORDER = 0;
    private static final int HEXFLAG_ORDER = 4;
    private static final int MAXWORDLENGTH_ORDER = 3;
    private static final int PARAM_NUMBER = 5;
    private static final String TAG = "DiffPrivacyManagerImpl";
    private static final int WORDLENTH_MAX = 8;
    private static final int WORDLENTH_MIN = 1;
    private static volatile DiffPrivacyManagerImpl sInstance = null;
    private int bloomBits;
    private double epsilon;
    private int hashNum;
    private boolean hexFlag;
    private int maxWordLength;
    private String word;

    private DiffPrivacyManagerImpl() {
    }

    public static DiffPrivacyManagerImpl getInstance() {
        if (sInstance == null) {
            synchronized (DiffPrivacyManagerImpl.class) {
                if (sInstance == null) {
                    sInstance = new DiffPrivacyManagerImpl();
                }
            }
        }
        return sInstance;
    }

    public String diffPrivacyBloomfilter(String data, String parameter) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(parameter)) {
            return null;
        }
        return DiffPrivacyNative.nativeDiffPrivacy(data, parameter);
    }

    public String diffPrivacyBitshistogram(int[] data, String parameter) {
        if (data == null || data.length == 0 || TextUtils.isEmpty(parameter)) {
            return null;
        }
        return DiffPrivacyNative.nativeDiffPrivacy(data, parameter);
    }

    public String diffPrivacyCountsketch(String data, String parameter) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(parameter)) {
            return null;
        }
        return DiffPrivacyNative.nativeDiffPrivacy(data, parameter);
    }

    public String diffPrivacyWordfilter(String data, String parameter) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(parameter)) {
            return null;
        }
        if (checkParameter(data, parameter)) {
            return new LocalWordFilter(this.hashNum, this.bloomBits, this.epsilon, this.maxWordLength, this.hexFlag).generateReport(this.word);
        }
        Log.e(TAG, "The parameter of wordfilter is wrong.");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002e A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0034 A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004f A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0055 A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0075 A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007b A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0095 A[Catch:{ NumberFormatException -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x009b A[Catch:{ NumberFormatException -> 0x00be }] */
    private boolean checkParameter(String data, String parameter) {
        boolean result;
        boolean result2;
        boolean result3;
        boolean result4;
        String[] params = parameter.split(",");
        if (params.length != 5) {
            Log.e(TAG, "The number of wordfilter parameter is wrong.");
            return false;
        }
        try {
            this.hashNum = Integer.parseInt(params[0]);
            if (this.hashNum >= 64) {
                if (this.hashNum <= 128) {
                    result = false;
                    if (!result) {
                        Log.e(TAG, "The hashNum of wordfilter parameter is wrong.");
                        return false;
                    }
                    this.bloomBits = Integer.parseInt(params[1]);
                    if (this.bloomBits >= 1024) {
                        if (this.bloomBits <= 4096) {
                            result2 = false;
                            if (!result2) {
                                Log.e(TAG, "The bloomBits of wordfilter parameter is wrong.");
                                return false;
                            }
                            this.epsilon = Double.parseDouble(params[2]);
                            if (this.epsilon >= EPSILON_MIN) {
                                if (this.epsilon <= EPSILON_MAX) {
                                    result3 = false;
                                    if (!result3) {
                                        Log.e(TAG, "The epsilon of wordfilter parameter is wrong.");
                                        return false;
                                    }
                                    this.maxWordLength = Integer.parseInt(params[3]);
                                    if (this.maxWordLength >= 1) {
                                        if (this.maxWordLength <= 8) {
                                            result4 = false;
                                            if (!result4) {
                                                Log.e(TAG, "The maxWordLength of wordfilter parameter is wrong.");
                                                return false;
                                            }
                                            if (data.length() > this.maxWordLength) {
                                                this.word = data.substring(0, this.maxWordLength);
                                            } else {
                                                this.word = data;
                                            }
                                            this.hexFlag = Integer.parseInt(params[4]) == 1;
                                            return true;
                                        }
                                    }
                                    result4 = true;
                                    if (!result4) {
                                    }
                                }
                            }
                            result3 = true;
                            if (!result3) {
                            }
                        }
                    }
                    result2 = true;
                    if (!result2) {
                    }
                }
            }
            result = true;
            if (!result) {
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Integer parse of parameter is wrong.");
            return false;
        }
    }
}
