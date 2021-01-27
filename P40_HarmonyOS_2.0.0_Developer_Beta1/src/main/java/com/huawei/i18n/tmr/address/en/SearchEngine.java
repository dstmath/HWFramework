package com.huawei.i18n.tmr.address.en;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.address.jni.DicSearch;
import com.huawei.sidetouch.TpCommandConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {
    private static final String TAG = "SearchEngine";
    private static final int TYPE_BUILDING = 1;
    private static final int TYPE_BUILDING2 = 2;
    private static final int TYPE_CITY = 0;
    private static volatile SearchEngine instance;
    private ArrayList<Integer> matchIndex2 = new ArrayList<>();
    private RegularExpression regularExpression = new RegularExpression();

    private SearchEngine() {
    }

    public static SearchEngine getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (SearchEngine.class) {
            if (instance == null) {
                instance = new SearchEngine();
            }
        }
        return instance;
    }

    private ArrayList<Match> searchMatch(String source) {
        return new Search(source).search();
    }

    public int[] search(String input) {
        List<Match> matchList = searchMatch(input);
        if (matchList == null) {
            return new int[1];
        }
        int[] result = new int[((matchList.size() * 2) + 1)];
        result[0] = matchList.size();
        for (int index = 0; index < matchList.size(); index++) {
            result[(index * 2) + 1] = matchList.get(index).getStartPos().intValue();
            int end = matchList.get(index).getEndPos().intValue();
            if (end > 1) {
                end--;
            }
            result[(index * 2) + 2] = end;
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Match> createAddressResultData(int[] addressArray, String source) {
        ArrayList<Match> matchedList = new ArrayList<>();
        if (addressArray.length == 0) {
            return matchedList;
        }
        int count = addressArray[0];
        for (int i = 1; i < (count * 2) + 1; i += 2) {
            Match match = new Match();
            match.setMatchedAddr(source.substring(addressArray[i], addressArray[i + 1]));
            match.setStartPos(Integer.valueOf(addressArray[i]));
            match.setEndPos(Integer.valueOf(addressArray[i + 1]));
            matchedList.add(match);
        }
        return sortAndMergePosList(matchedList, source);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] searSpot(String string, int head) {
        return new SearchSpot(string, head).search();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int max(int i, int j) {
        if (i > j) {
            return i;
        }
        return j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] searBuilding(String string, int head) {
        boolean flag;
        if (stanWri(string)) {
            flag = false;
        } else {
            flag = true;
        }
        return searBuildingSuf(string, StorageManagerExt.INVALID_KEY_DESC, 0, flag, head);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] searBuildingSuf(String str, String subLeft, int leftState, boolean flag, int head) {
        return new SearchBuildingSuf(str, subLeft, leftState, flag, head).search();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] searBuildingDic(String string, int head) {
        return new SearchBuildingDic(string, head).search();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean noBlank(String strParam) {
        int len = strParam.length();
        String str = strParam.toLowerCase(Locale.getDefault());
        boolean flag = true;
        int index = 0;
        while (flag && index < len) {
            if ((str.charAt(index) <= 'z' && str.charAt(index) >= 'a') || (str.charAt(index) <= '9' && str.charAt(index) >= '0')) {
                flag = false;
            }
            index++;
        }
        return !flag;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] divStr(String str) {
        String[] strs = new String[TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX];
        int length = str.length();
        strs[0] = StorageManagerExt.INVALID_KEY_DESC;
        int pr = getPr(str, strs, length, 0);
        if (strs[pr].length() > 0) {
            pr++;
        }
        if (pr >= 150) {
            return strs;
        }
        String[] re = new String[pr];
        System.arraycopy(strs, 0, re, 0, pr);
        return re;
    }

    private int getPr(String str, String[] strs, int length, int pr) {
        for (int index = 0; index < length; index++) {
            char letter = str.charAt(index);
            if ((letter <= 'z' && letter >= 'a') || ((letter <= 'Z' && letter >= 'A') || (letter <= '9' && letter >= '0'))) {
                strs[pr] = strs[pr] + letter;
            } else if (strs[pr].length() > 0) {
                strs[pr] = strs[pr] + letter;
                pr++;
                strs[pr] = StorageManagerExt.INVALID_KEY_DESC;
            } else if (pr > 0) {
                StringBuilder sb = new StringBuilder();
                int i = pr - 1;
                sb.append(strs[i]);
                sb.append(letter);
                strs[i] = sb.toString();
            }
        }
        return pr;
    }

    private boolean stanWri(String str) {
        String[] strs = divStr(str);
        int length = strs.length;
        boolean flag = true;
        int index = 0;
        while (flag && index < length) {
            flag = isFlag(strs[index], flag);
            if (length > 3) {
                if (index == 0) {
                    index = (length / 2) - 1;
                } else if (index == (length / 2) - 1) {
                    index = length - 2;
                }
            }
            index++;
        }
        return flag;
    }

    private boolean isFlag(String str, boolean flag) {
        int length2 = str.length();
        int index2 = 1;
        while (flag && index2 < length2) {
            char letter = str.charAt(index2);
            if (letter <= 'Z' && letter >= 'A') {
                flag = false;
            }
            index2++;
        }
        return flag;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String searCity(String string, int mode) {
        int length = string.length();
        Matcher matcherCity = this.regularExpression.getPatCity().matcher(string);
        if (mode == 1) {
            return mode1(length, string, matcherCity);
        }
        if (matcherCity.find() && noBlank(matcherCity.group(1).substring(0, matcherCity.group(2).length()))) {
            return matcherCity.group(1);
        }
        int position = DicSearch.dicsearch(0, string.toLowerCase(Locale.getDefault()));
        if (position <= 0) {
            return null;
        }
        Matcher matcherCity2 = this.regularExpression.getPatCity2().matcher(string.substring(position, length));
        if (!matcherCity2.matches()) {
            return string.substring(0, position);
        }
        return string.substring(0, position) + matcherCity2.group(1);
    }

    private String mode1(int length, String str, Matcher matcherCity) {
        if (matcherCity.find() && noBlank(matcherCity.group(1).substring(0, matcherCity.group(2).length()))) {
            return str;
        }
        int index = 0;
        while (index < length) {
            str = str.substring(index, length);
            length -= index;
            if (DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault())) != 0) {
                return str;
            }
            index = dealPosition0(0, length, str) + 1;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int searchBracket(String str) {
        Matcher matcherBracket = Pattern.compile("(\\s*.?\\s*)\\)").matcher(str);
        if (matcherBracket.lookingAt()) {
            return matcherBracket.group().length();
        }
        return 0;
    }

    private ArrayList<Match> sortAndMergePosList(ArrayList<Match> posList, String sourceTxt) {
        if (posList.isEmpty()) {
            return posList;
        }
        Collections.sort(posList, new Comparator<Match>() {
            /* class com.huawei.i18n.tmr.address.en.SearchEngine.AnonymousClass1 */

            public int compare(Match match1, Match match2) {
                if (match1.getStartPos().compareTo(match2.getStartPos()) == 0) {
                    return match1.getEndPos().compareTo(match2.getEndPos());
                }
                return match1.getStartPos().compareTo(match2.getStartPos());
            }
        });
        for (int i = posList.size() - 1; i > 0; i--) {
            if (posList.get(i - 1).getStartPos().intValue() <= posList.get(i).getStartPos().intValue() && posList.get(i).getStartPos().intValue() <= posList.get(i - 1).getEndPos().intValue()) {
                if (posList.get(i - 1).getEndPos().intValue() < posList.get(i).getEndPos().intValue()) {
                    posList.get(i - 1).setEndPos(posList.get(i).getEndPos());
                    posList.get(i - 1).setMatchedAddr(sourceTxt.substring(posList.get(i - 1).getStartPos().intValue(), posList.get(i - 1).getEndPos().intValue()));
                    posList.remove(i);
                } else if (posList.get(i - 1).getEndPos().intValue() >= posList.get(i).getEndPos().intValue()) {
                    posList.remove(i);
                }
            }
        }
        return posList;
    }

    /* access modifiers changed from: private */
    public class Search {
        private String city;
        private int end = 0;
        private boolean isBox = true;
        private Iterator<Integer> it;
        private ArrayList<Integer> nn = new ArrayList<>();
        private String out;
        private int outLen;
        private final String source;
        private int start = 0;

        Search(String source2) {
            this.source = source2;
            this.nn.add(0);
            SearchEngine.this.matchIndex2.clear();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private ArrayList<Match> search() {
            match28();
            match52OrSub(SearchEngine.this.regularExpression.getPat52().matcher(this.source));
            match52OrSub(SearchEngine.this.regularExpression.getPat52Sub().matcher(this.source));
            match1346();
            return getMatches();
        }

        private ArrayList<Match> getMatches() {
            int num = this.nn.size();
            int[] addressArr2 = new int[num];
            for (int i = 0; i < num; i++) {
                addressArr2[i] = this.nn.get(i).intValue();
            }
            if (num <= 4) {
                addressArr2[0] = (num - 1) / 2;
                return SearchEngine.this.createAddressResultData(addressArr2, this.source);
            }
            int[] addressArr = new int[num];
            int t = 0;
            for (int i2 = 1; i2 < (num - 1) / 2; i2++) {
                for (int j = i2 + 1; j < (num + 1) / 2; j++) {
                    if (addressArr2[(i2 * 2) - 1] > addressArr2[(j * 2) - 1]) {
                        int i3 = (i2 * 2) - 1;
                        addressArr2[i3] = addressArr2[i3] + addressArr2[(j * 2) - 1];
                        addressArr2[(j * 2) - 1] = addressArr2[(i2 * 2) - 1] - addressArr2[(j * 2) - 1];
                        addressArr2[(i2 * 2) - 1] = addressArr2[(i2 * 2) - 1] - addressArr2[(j * 2) - 1];
                        int i4 = i2 * 2;
                        addressArr2[i4] = addressArr2[i4] + addressArr2[j * 2];
                        addressArr2[j * 2] = addressArr2[i2 * 2] - addressArr2[j * 2];
                        addressArr2[i2 * 2] = addressArr2[i2 * 2] - addressArr2[j * 2];
                    }
                }
            }
            int i5 = 1;
            while (i5 < (num + 1) / 2) {
                t++;
                addressArr[(t * 2) - 1] = addressArr2[(i5 * 2) - 1];
                addressArr[t * 2] = addressArr2[i5 * 2];
                int j2 = i5 + 1;
                while (true) {
                    if (j2 >= (num + 1) / 2) {
                        break;
                    }
                    if (addressArr2[i5 * 2] < addressArr2[(j2 * 2) - 1]) {
                        i5 = j2 - 1;
                        break;
                    }
                    addressArr2[i5 * 2] = SearchEngine.this.max(addressArr2[i5 * 2], addressArr2[j2 * 2]);
                    addressArr[t * 2] = addressArr2[i5 * 2];
                    if (j2 == ((num + 1) / 2) - 1) {
                        i5 = j2;
                    }
                    j2++;
                }
                i5++;
            }
            addressArr2[0] = t;
            addressArr[0] = t;
            return SearchEngine.this.createAddressResultData(addressArr, this.source);
        }

        private void match1346() {
            Matcher matcher1346 = SearchEngine.this.regularExpression.getPat1346().matcher(this.source);
            while (matcher1346.find()) {
                if (SearchEngine.this.regularExpression.getPatBig().matcher(matcher1346.group()).find()) {
                    int head = matcher1346.start();
                    matchSearchBuilding(matcher1346, head);
                    matchSearchSpot(matcher1346, head);
                }
            }
        }

        private void matchSearchSpot(Matcher matcher1346, int head) {
            SearchEngine.this.matchIndex2.clear();
            String[] buildings = SearchEngine.this.searSpot(matcher1346.group(), head);
            if (buildings != null) {
                int lengthBui = buildings.length;
                this.it = SearchEngine.this.matchIndex2.iterator();
                int pr = 0;
                while (pr < lengthBui && buildings[pr] != null) {
                    if (!matchCleanAndNum(buildings[pr]) && this.it.hasNext()) {
                        this.start += this.it.next().intValue();
                        this.end = this.start + this.out.length();
                        matchSubstring();
                    }
                    pr++;
                }
            }
        }

        private void matchSubstring() {
            try {
                String temp = this.source.substring(this.start, this.end);
                if (SearchEngine.this.regularExpression.getPatDir().matcher(temp).lookingAt()) {
                    this.out = temp;
                } else {
                    Matcher matcherClean = SearchEngine.this.regularExpression.getPatClean().matcher(temp);
                    if (matcherClean.matches()) {
                        this.start += matcherClean.group(1).length();
                        this.out = this.out.substring(matcherClean.group(1).length());
                    }
                }
                if (!SearchEngine.this.regularExpression.getPatBuilding().matcher(this.out).matches()) {
                    this.nn.add(Integer.valueOf(this.start));
                    this.nn.add(Integer.valueOf(this.end));
                }
            } catch (Exception e) {
                Log.e(SearchEngine.TAG, StorageManagerExt.INVALID_KEY_DESC);
            }
        }

        private void matchSearchBuilding(Matcher matcher1346, int head) {
            SearchEngine.this.matchIndex2.clear();
            String[] buildings = SearchEngine.this.searBuilding(matcher1346.group(), head);
            if (buildings != null) {
                int lengthBui = buildings.length;
                this.it = SearchEngine.this.matchIndex2.iterator();
                int pr = 0;
                while (pr < lengthBui && buildings[pr] != null) {
                    if (!matchCleanAndNum(buildings[pr]) && this.it.hasNext()) {
                        listIterator();
                    }
                    pr++;
                }
            }
        }

        private boolean matchCleanAndNum(String building) {
            Matcher matcherResultClean = SearchEngine.this.regularExpression.getPatResultClean().matcher(building);
            if (!matcherResultClean.matches()) {
                return true;
            }
            if (matcherResultClean.group(1) != null) {
                this.out = matcherResultClean.group(1);
                this.outLen = this.out.length() + 1;
                this.start = building.length() - this.outLen;
            } else {
                this.out = matcherResultClean.group(2);
                this.outLen = this.out.length();
                this.start = building.length() - this.outLen;
            }
            Matcher matcherNum = SearchEngine.this.regularExpression.getPatNum().matcher(this.out);
            if (!matcherNum.lookingAt()) {
                return false;
            }
            this.out = this.out.substring(matcherNum.group().length());
            this.start += matcherNum.group().length();
            return false;
        }

        private void listIterator() {
            this.start += this.it.next().intValue();
            this.end = this.start + this.out.length();
            matchSubstring();
        }

        private void match52OrSub(Matcher matcher) {
            while (matcher.find()) {
                this.out = StorageManagerExt.INVALID_KEY_DESC;
                if (!SearchEngine.this.regularExpression.getPatRoad().matcher(matcher.group()).matches()) {
                    if (matcher.group(5) == null) {
                        Matcher matcherResultClean = SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher.group(1));
                        if (matcherResultClean.matches()) {
                            matchOutLength(matcherResultClean);
                            this.start = matcher.start(1) + (matcher.group(1).length() - this.outLen);
                            this.end = this.start + this.out.length();
                        }
                    } else if (matcher.group(6) != null) {
                        Matcher matcherResultClean2 = SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher.group());
                        if (matcherResultClean2.matches()) {
                            matchOutLength(matcherResultClean2);
                            this.start = matcher.start() + (matcher.group().length() - this.outLen);
                            this.end = this.start + this.out.length();
                        }
                    } else {
                        matchElse(matcher);
                    }
                    matchOut();
                }
            }
        }

        private void matchOut() {
            if (this.out.length() > 0) {
                Matcher matcherPreRoad = SearchEngine.this.regularExpression.getPatPreRoad().matcher(this.out);
                if (matcherPreRoad.find()) {
                    if (matcherPreRoad.group(2) == null) {
                        this.start += this.out.length() - matcherPreRoad.group(1).length();
                        this.out = matcherPreRoad.group(1);
                    } else {
                        this.out = StorageManagerExt.INVALID_KEY_DESC;
                    }
                }
                Matcher matcherNotRoad = SearchEngine.this.regularExpression.getPatNotRoad().matcher(this.out);
                if (matcherNotRoad.find()) {
                    if (matcherNotRoad.group(2) == null || matcherNotRoad.group(2).length() <= 0) {
                        this.out = StorageManagerExt.INVALID_KEY_DESC;
                    } else {
                        this.out = this.out.substring(matcherNotRoad.group(1).length());
                        this.start += matcherNotRoad.group(1).length();
                    }
                }
                if (this.out.length() > 0) {
                    this.nn.add(Integer.valueOf(this.start));
                    this.nn.add(Integer.valueOf(this.end));
                }
            }
        }

        private void matchElse(Matcher matcher) {
            String cut = SearchEngine.this.getCut(SearchEngine.this.regularExpression.getPatCut2().matcher(matcher.group(5)));
            this.city = SearchEngine.this.searCity(matcher.group(5).substring(cut.length()), 2);
            if (this.city == null) {
                cityIsNull(matcher);
                return;
            }
            this.city = cut + this.city;
            if (matcher.group(7) == null) {
                if (matcher.group(4) != null) {
                    this.city = matcher.group(4) + this.city;
                }
            } else if (matcher.group(4) != null) {
                this.city = matcher.group(4) + matcher.group(5) + matcher.group(7);
            } else {
                this.city = matcher.group(5) + matcher.group(7);
            }
            Pattern patResultClean = SearchEngine.this.regularExpression.getPatResultClean();
            Matcher matcherResultClean = patResultClean.matcher(matcher.group(1) + matcher.group(3) + this.city);
            if (matcherResultClean.matches()) {
                matchOutLength(matcherResultClean);
                int start2 = matcher.start(1);
                this.start = start2 + ((matcher.group(1) + matcher.group(3) + this.city).length() - this.outLen);
                this.end = this.start + this.out.length();
            }
        }

        private void cityIsNull(Matcher matcher) {
            if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(matcher.group(3)).lookingAt()) {
                if (!matchClean(SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher.group()))) {
                    this.start = matcher.start() + (matcher.group().length() - this.outLen);
                    this.end = this.start + this.out.length();
                }
            } else if (SearchEngine.this.regularExpression.getPatSingle().matcher(matcher.group(5)).matches()) {
                Matcher matcherResultClean = SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher.group());
                if (matcherResultClean.matches()) {
                    matchOutLength(matcherResultClean);
                    this.start = matcher.start() + (matcher.group().length() - this.outLen);
                    this.end = this.start + this.out.length();
                }
            } else if (!matchClean(SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher.group(1)))) {
                this.start = matcher.start(1) + (matcher.group(1).length() - this.outLen);
                this.end = this.start + this.out.length();
            }
        }

        private boolean matchClean(Matcher matcherResultClean) {
            if (!matcherResultClean.matches()) {
                return true;
            }
            matchOutLength(matcherResultClean);
            return false;
        }

        private void matchOutLength(Matcher matcherResultClean) {
            if (matcherResultClean.group(1) != null) {
                this.out = matcherResultClean.group(1);
                this.outLen = this.out.length() + 1;
                return;
            }
            this.out = matcherResultClean.group(2);
            this.outLen = this.out.length();
        }

        private void match28() {
            Matcher matcher28 = SearchEngine.this.regularExpression.getPat28().matcher(this.source);
            while (matcher28.find()) {
                if (matcher28.group(1) == null) {
                    group1IsNull(matcher28);
                } else if (matcher28.group(4) == null) {
                    group4IsNull(matcher28);
                } else {
                    Matcher matcherResultClean = SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher28.group());
                    if (matcherResultClean.matches()) {
                        matchOutLength(matcherResultClean);
                        this.start = matcher28.start() + (matcher28.group().length() - this.outLen);
                        this.end = this.start + this.out.length();
                        this.nn.add(Integer.valueOf(this.start));
                        this.nn.add(Integer.valueOf(this.end));
                        if (matcher28.group(2) != null) {
                            this.isBox = false;
                        }
                    }
                }
            }
            if (this.isBox) {
                Matcher matcherBox = SearchEngine.this.regularExpression.getPatBox().matcher(this.source);
                while (matcherBox.find()) {
                    this.start = matcherBox.start();
                    this.end = this.start + matcherBox.group().length();
                    this.nn.add(Integer.valueOf(this.start));
                    this.nn.add(Integer.valueOf(this.end));
                }
            }
        }

        private void group4IsNull(Matcher matcher28) {
            Matcher matcherResultClean;
            if (matcher28.group(2) != null) {
                matcherResultClean = SearchEngine.this.regularExpression.getPatResultClean().matcher(matcher28.group());
            } else {
                this.city = SearchEngine.this.searCity(matcher28.group(3), 1);
                if (this.city == null) {
                    this.city = StorageManagerExt.INVALID_KEY_DESC;
                }
                Pattern patResultClean = SearchEngine.this.regularExpression.getPatResultClean();
                matcherResultClean = patResultClean.matcher(this.city + matcher28.group(5) + matcher28.group(6));
            }
            if (!matchClean(matcherResultClean)) {
                int start2 = matcher28.start(5);
                this.start = start2 + ((matcher28.group(5) + matcher28.group(6)).length() - this.outLen);
                this.end = this.start + this.out.length();
                this.nn.add(Integer.valueOf(this.start));
                this.nn.add(Integer.valueOf(this.end));
                if (matcher28.group(2) != null) {
                    this.isBox = false;
                }
            }
        }

        private void group1IsNull(Matcher matcher28) {
            if (!SearchEngine.this.regularExpression.getPatCodeA().matcher(matcher28.group()).find()) {
                this.start = matcher28.start();
                this.end = this.start + matcher28.group().length();
                this.nn.add(Integer.valueOf(this.start));
                this.nn.add(Integer.valueOf(this.end));
            } else if (matcher28.group(6).indexOf(45) != -1) {
                this.start = matcher28.start(6);
                this.end = this.start + matcher28.group(6).length();
                this.nn.add(Integer.valueOf(this.start));
                this.nn.add(Integer.valueOf(this.end));
            } else if (matcher28.group(5) != null && matcher28.group(5).length() > 0) {
                this.start = matcher28.start(6);
                this.end = this.start + matcher28.group(6).length();
                this.nn.add(Integer.valueOf(this.start));
                this.nn.add(Integer.valueOf(this.end));
            }
        }
    }

    /* access modifiers changed from: private */
    public class SearchBuildingSuf {
        private String building = StorageManagerExt.INVALID_KEY_DESC;
        private int count = 0;
        private boolean flag;
        private int head;
        private int leftState;
        private Matcher matcherComma;
        private Matcher matcherLocation;
        private Matcher matcherPreCity;
        private Matcher matcherPreUni;
        private Matcher matcherSingle;
        private String[] result2 = new String[0];
        private String[] results = new String[8];
        private String[] results3 = new String[0];
        private String str;
        private String sub1 = StorageManagerExt.INVALID_KEY_DESC;
        private String sub2 = StorageManagerExt.INVALID_KEY_DESC;
        private String subLeft;
        private String subRight = StorageManagerExt.INVALID_KEY_DESC;
        private String[] temp;

        SearchBuildingSuf(String str2, String subLeft2, int leftState2, boolean flag2, int head2) {
            this.str = str2;
            this.subLeft = subLeft2;
            this.leftState = leftState2;
            this.flag = flag2;
            this.head = head2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String[] search() {
            matchNot1();
            this.matcherLocation = SearchEngine.this.regularExpression.getPatLocation().matcher(this.str);
            if (!this.matcherLocation.find()) {
                if (this.leftState == 1) {
                    this.str = this.subLeft + this.str;
                }
                this.result2 = SearchEngine.this.searBuildingDic(this.str, this.head - this.subLeft.length());
            } else {
                locationNotFind();
            }
            if (this.results3.length > 0) {
                int index = 0;
                while (true) {
                    String[] strArr = this.results3;
                    if (index >= strArr.length) {
                        break;
                    }
                    String[] strArr2 = this.results;
                    int i = this.count;
                    this.count = i + 1;
                    strArr2[i] = strArr[index];
                    index++;
                }
            }
            String[] strArr3 = this.result2;
            if (strArr3.length > 0) {
                for (String resul : strArr3) {
                    String[] strArr4 = this.results;
                    int i2 = this.count;
                    this.count = i2 + 1;
                    strArr4[i2] = resul;
                }
            }
            int i3 = this.count;
            if (i3 >= 8) {
                return this.results;
            }
            String[] re = new String[i3];
            System.arraycopy(this.results, 0, re, 0, i3);
            return re;
        }

        private void locationNotFind() {
            this.sub1 = this.matcherLocation.group(1);
            Matcher matcherNo = SearchEngine.this.regularExpression.getPatNo().matcher(this.sub1);
            if (this.sub1.length() <= 0 || !SearchEngine.this.noBlank(this.sub1)) {
                this.subLeft = this.matcherLocation.group();
                this.leftState = 1;
                this.subRight = this.str.substring(this.subLeft.length(), this.str.length());
                recursiveSearch(this.subRight);
            } else if (!matcherNo.matches() || this.matcherLocation.group(3) != null) {
                matchElse();
            } else {
                this.subLeft = this.matcherLocation.group();
                this.leftState = 1;
                this.subRight = this.str.substring(this.subLeft.length());
                recursiveSearch(this.subRight);
            }
        }

        private void matchElse() {
            matchComma();
            if (this.building.length() == 0 && this.matcherLocation.group(3) != null) {
                this.building = this.matcherLocation.group(2);
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + this.matcherLocation.group(1).length()));
            }
            if (this.building.length() <= 0) {
                this.leftState = 1;
                this.subLeft = this.matcherLocation.group();
                this.subRight = this.str.substring(this.subLeft.length());
                recursiveSearch(this.subRight);
                return;
            }
            buildingLenOver0();
        }

        private void buildingLenOver0() {
            this.sub2 = this.matcherLocation.group();
            String sub3 = this.sub2.substring(0, this.sub2.length() > this.building.length() ? this.sub2.length() - this.building.length() : 0);
            if (this.leftState == 1) {
                sub3 = this.subLeft + sub3;
            }
            if (SearchEngine.this.noBlank(sub3)) {
                this.results3 = SearchEngine.this.searBuildingDic(sub3, this.head - this.subLeft.length());
                this.leftState = 2;
                this.subLeft = StorageManagerExt.INVALID_KEY_DESC;
            }
            String sub32 = this.str.substring(this.sub2.length());
            if (!SearchEngine.this.noBlank(sub32)) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building;
                return;
            }
            rightHasContent(sub32);
        }

        private void rightHasContent(String sub3) {
            Matcher matcher52s = SearchEngine.this.regularExpression.getPat52s().matcher(sub3);
            if (matcher52s.lookingAt()) {
                match52sLookingAt(sub3, matcher52s);
                return;
            }
            Matcher matcher2s = SearchEngine.this.regularExpression.getPat2s().matcher(sub3);
            if (!matcher2s.lookingAt()) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building;
                this.result2 = SearchEngine.this.searBuildingSuf(sub3, this.subLeft, this.leftState, this.flag, this.head + (this.str.length() - sub3.length()));
            } else if (matcher2s.group(3) == null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building;
                recursiveSearch(sub3);
            } else {
                match2sGroup4(sub3, matcher2s);
            }
        }

        private void recursiveSearch(String sub3) {
            int headParam = this.head + (this.str.length() - sub3.length());
            if (SearchEngine.this.noBlank(sub3)) {
                this.result2 = SearchEngine.this.searBuildingSuf(sub3, this.subLeft, this.leftState, this.flag, headParam);
            }
        }

        private void match2sGroup4(String sub3, Matcher matcher2s) {
            if (matcher2s.group(4) != null) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher2s.group();
                recursiveSearch(sub3.substring(matcher2s.group().length()));
                return;
            }
            matchCity2(sub3, matcher2s);
        }

        private void matchCity2(String sub3, Matcher matcher2s) {
            String city;
            String cut = getCut(matcher2s, 3);
            String city2 = SearchEngine.this.searCity(matcher2s.group(3).substring(cut.length()), 2);
            if (city2 == null) {
                this.matcherPreCity = SearchEngine.this.regularExpression.getPatPreCity().matcher(matcher2s.group(1));
                if (this.matcherPreCity.lookingAt()) {
                    city = matcher2s.group();
                } else {
                    this.matcherSingle = SearchEngine.this.regularExpression.getPatSingle().matcher(matcher2s.group(3));
                    if (this.matcherSingle.matches()) {
                        city = matcher2s.group();
                    } else {
                        city = StorageManagerExt.INVALID_KEY_DESC;
                    }
                }
            } else {
                city = SearchEngine.this.getCity2s(matcher2s, cut, city2);
            }
            String[] strArr = this.results;
            int i = this.count;
            this.count = i + 1;
            strArr[i] = this.building + city;
            recursiveSearch(sub3.substring(city.length()));
        }

        private String getCut(Matcher matcher2s, int groupId) {
            return SearchEngine.this.getCut(SearchEngine.this.regularExpression.getPatCut().matcher(matcher2s.group(groupId)));
        }

        private void match52sLookingAt(String sub3, Matcher matcher52s) {
            if (matcher52s.group(6) == null) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher52s.group();
                String sub32 = sub3.substring(matcher52s.group().length());
                if (SearchEngine.this.noBlank(sub32)) {
                    this.result2 = SearchEngine.this.searBuildingSuf(sub32, this.subLeft, this.leftState, this.flag, this.head + this.sub2.length() + matcher52s.group().length());
                }
            } else if (matcher52s.group(7) != null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher52s.group();
                String sub33 = sub3.substring(matcher52s.group().length());
                if (SearchEngine.this.noBlank(sub33)) {
                    this.result2 = SearchEngine.this.searBuildingSuf(sub33, this.subLeft, this.leftState, this.flag, this.head + this.sub2.length() + matcher52s.group().length());
                }
            } else {
                matchCity(sub3, matcher52s);
            }
        }

        private void matchCity(String sub3, Matcher matcher52s) {
            String cut = getCut(matcher52s, 6);
            String city = SearchEngine.this.searCity(matcher52s.group(6).substring(cut.length()), 2);
            if (city == null) {
                cityIsNull(sub3, matcher52s);
                return;
            }
            String city2 = SearchEngine.this.getCity52s(matcher52s, cut, city);
            String[] strArr = this.results;
            int i = this.count;
            this.count = i + 1;
            strArr[i] = this.building + matcher52s.group(1) + matcher52s.group(2) + matcher52s.group(4) + city2;
            recursiveSearch(sub3.substring(matcher52s.group(1).length() + matcher52s.group(2).length() + matcher52s.group(4).length() + city2.length()));
        }

        private void cityIsNull(String sub3, Matcher matcher52s) {
            String sub32;
            this.matcherPreCity = SearchEngine.this.regularExpression.getPatPreCity().matcher(matcher52s.group(4));
            if (this.matcherPreCity.lookingAt()) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher52s.group();
                sub32 = sub3.substring(matcher52s.group().length());
            } else {
                this.matcherSingle = SearchEngine.this.regularExpression.getPatSingle().matcher(matcher52s.group(3));
                if (this.matcherSingle.matches()) {
                    String[] strArr2 = this.results;
                    int i2 = this.count;
                    this.count = i2 + 1;
                    strArr2[i2] = this.building + matcher52s.group();
                    sub32 = sub3.substring(matcher52s.group().length());
                } else {
                    String[] strArr3 = this.results;
                    int i3 = this.count;
                    this.count = i3 + 1;
                    strArr3[i3] = this.building + matcher52s.group(1) + matcher52s.group(2);
                    sub32 = sub3.substring(matcher52s.group(1).length() + matcher52s.group(2).length());
                }
            }
            recursiveSearch(sub32);
        }

        private void matchComma() {
            this.matcherComma = SearchEngine.this.regularExpression.getPatComma().matcher(this.sub1);
            if (this.matcherComma.find()) {
                commaFind();
                return;
            }
            dealWhile2();
            if (this.building.length() == 0) {
                this.temp = SearchEngine.this.divStr(this.sub1);
                int length = this.temp.length;
                if (length > 4) {
                    this.building = this.temp[length - 4] + this.temp[length - 3] + this.temp[length - 2] + this.temp[length - 1] + this.matcherLocation.group(2);
                    SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + (this.sub1.length() - (this.building.length() - this.matcherLocation.group(2).length()))));
                    return;
                }
                if (length > 0) {
                    this.building = this.sub1 + this.matcherLocation.group(2);
                }
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            }
        }

        private void dealWhile2() {
            String sub1Temp = this.sub1;
            boolean sub1Undone = true;
            while (sub1Undone) {
                this.matcherPreUni = SearchEngine.this.regularExpression.getPatPreUni().matcher(sub1Temp);
                if (!this.matcherPreUni.find()) {
                    sub1Undone = false;
                } else {
                    this.sub2 = this.matcherPreUni.group(2);
                    String str2 = this.sub2;
                    if (str2 == null || !SearchEngine.this.noBlank(str2)) {
                        sub1Undone = false;
                    } else {
                        this.temp = SearchEngine.this.divStr(this.sub2);
                        if (this.temp.length <= 4) {
                            this.building = this.sub2 + this.matcherLocation.group(2);
                            SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + (this.sub1.length() - this.sub2.length())));
                            sub1Undone = false;
                        } else {
                            sub1Temp = this.sub2;
                        }
                    }
                }
            }
        }

        private void commaFind() {
            this.sub2 = this.matcherComma.group(1);
            String str2 = this.sub2;
            if (str2 != null && SearchEngine.this.noBlank(str2)) {
                this.temp = SearchEngine.this.divStr(this.sub2);
                if (this.temp.length <= 4) {
                    this.building = this.sub2 + this.matcherLocation.group(2);
                    SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + this.matcherComma.start(1)));
                }
            }
            if (this.building.length() == 0 && this.flag) {
                dealWhile(this.sub1);
                if (this.building.length() == 0) {
                    this.temp = SearchEngine.this.divStr(this.sub1);
                    int length = this.temp.length;
                    if (length > 4) {
                        this.building = this.temp[length - 4] + this.temp[length - 3] + this.temp[length - 2] + this.temp[length - 1] + this.matcherLocation.group(2);
                        SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + (this.sub1.length() - (this.building.length() - this.matcherLocation.group(2).length()))));
                        return;
                    }
                    if (length > 0) {
                        this.building = this.sub1 + this.matcherLocation.group(2);
                    }
                    SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
                }
            }
        }

        private void dealWhile(String sub1Temp) {
            boolean sub1Undone = true;
            while (sub1Undone) {
                this.matcherPreUni = SearchEngine.this.regularExpression.getPatPreUni().matcher(sub1Temp);
                if (!this.matcherPreUni.find()) {
                    sub1Undone = false;
                } else {
                    this.sub2 = this.matcherPreUni.group(2);
                    String str2 = this.sub2;
                    if (str2 == null || !SearchEngine.this.noBlank(str2)) {
                        sub1Undone = false;
                    } else {
                        this.temp = SearchEngine.this.divStr(this.sub2);
                        if (this.temp.length <= 4) {
                            this.building = this.sub2 + this.matcherLocation.group(2);
                            SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head + (this.sub1.length() - this.sub2.length())));
                            sub1Undone = false;
                        } else {
                            sub1Temp = this.sub2;
                        }
                    }
                }
            }
        }

        private void matchNot1() {
            this.matcherLocation = SearchEngine.this.regularExpression.getPatNot1().matcher(this.str);
            if (this.matcherLocation.lookingAt()) {
                this.matcherLocation = SearchEngine.this.regularExpression.getPatNot2().matcher(this.matcherLocation.group(1));
                if (this.matcherLocation.lookingAt()) {
                    int length = this.matcherLocation.group().length();
                    this.str = this.str.substring(length);
                    this.head += length;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCity2s(Matcher matcher2s, String cut, String city) {
        String city2 = cut + city;
        if (matcher2s.group(6) == null) {
            if (matcher2s.group(2) != null) {
                city2 = matcher2s.group(2) + city2;
            }
        } else if (matcher2s.group(2) == null) {
            city2 = matcher2s.group(3) + matcher2s.group(5) + matcher2s.group(6);
        } else {
            city2 = matcher2s.group(2) + matcher2s.group(3) + matcher2s.group(5) + matcher2s.group(6);
        }
        return matcher2s.group(1) + city2;
    }

    /* access modifiers changed from: private */
    public class SearchBuildingDic {
        private String building = StorageManagerExt.INVALID_KEY_DESC;
        private int count = 0;
        private boolean flag = true;
        private int full;
        private int head;
        private int head0;
        private int length;
        private String[] results = new String[8];
        private String str;
        private String string;
        private String subLeft = StorageManagerExt.INVALID_KEY_DESC;

        SearchBuildingDic(String string2, int head2) {
            this.string = string2;
            this.head = head2;
            this.length = string2.length();
            this.head0 = head2;
            this.str = string2;
            this.full = this.str.length();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String[] search() {
            int index;
            int index2 = 0;
            while (true) {
                int i = this.length;
                if (index2 >= i) {
                    return dealResult();
                }
                this.str = this.str.substring(index2, i);
                this.head = this.head0 + (this.full - this.str.length());
                this.length -= index2;
                String str2 = this.string;
                this.subLeft = str2.substring(0, str2.length() - this.length);
                int position = DicSearch.dicsearch(1, this.str.toLowerCase(Locale.getDefault()));
                if (position == 0) {
                    index = SearchEngine.this.dealPosition0(0, this.length, this.str);
                } else {
                    this.building = this.str.substring(0, position);
                    String subRight = this.str.substring(position);
                    int lengthBracket = SearchEngine.this.searchBracket(subRight);
                    if (lengthBracket > 0) {
                        this.building += subRight.substring(0, lengthBracket);
                        subRight = subRight.substring(lengthBracket);
                    }
                    match52s(subRight);
                    if (this.flag) {
                        index = (this.results[this.count - 1].length() + 0) - 1;
                        this.string = this.str.substring(this.results[this.count - 1].length());
                    } else {
                        index = (this.building.length() + 0) - 1;
                        this.string = this.str.substring(this.building.length());
                        this.flag = true;
                    }
                }
                index2 = index + 1;
            }
        }

        private void match52s(String subRight) {
            Matcher matcher52s = SearchEngine.this.regularExpression.getPat52s().matcher(subRight);
            if (matcher52s.lookingAt()) {
                match52sLookingAt(matcher52s);
                return;
            }
            Matcher matcher2s = SearchEngine.this.regularExpression.getPat2s().matcher(subRight);
            if (matcher2s.lookingAt()) {
                match2sLookingAt(matcher2s);
            } else if (SearchEngine.this.regularExpression.getPatPreBuilding().matcher(this.subLeft).matches()) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                this.flag = false;
            }
        }

        private void match2sLookingAt(Matcher matcher2s) {
            if (matcher2s.group(3) == null) {
                if (SearchEngine.this.regularExpression.getPatPreBuilding().matcher(this.subLeft).matches()) {
                    String[] strArr = this.results;
                    int i = this.count;
                    this.count = i + 1;
                    strArr[i] = this.building;
                    SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
                    return;
                }
                this.flag = false;
            } else if (matcher2s.group(4) != null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                matchCityDic(matcher2s);
            }
        }

        private void matchCityDic(Matcher matcher2s) {
            String cut = SearchEngine.this.getCut(SearchEngine.this.regularExpression.getPatCut().matcher(matcher2s.group(3)));
            String city = SearchEngine.this.searCity(matcher2s.group(3).substring(cut.length()), 2);
            if (city != null) {
                String city2 = SearchEngine.this.getCity2s(matcher2s, cut, city);
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + city2;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
                return;
            }
            matchCityIsNull(matcher2s);
        }

        private void matchCityIsNull(Matcher matcher2s) {
            if (SearchEngine.this.regularExpression.getPatPreCity2().matcher(matcher2s.group(1)).matches()) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (SearchEngine.this.regularExpression.getPatSingle().matcher(matcher2s.group(3)).matches()) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (SearchEngine.this.regularExpression.getPatPreBuilding().matcher(this.subLeft).matches()) {
                String[] strArr3 = this.results;
                int i3 = this.count;
                this.count = i3 + 1;
                strArr3[i3] = this.building;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                this.flag = false;
            }
        }

        private void match52sLookingAt(Matcher matcher52s) {
            if (matcher52s.group(6) == null) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher52s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (matcher52s.group(7) != null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher52s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                SearchEngine.this.matchCitySpotOrDic(matcher52s, this.results, this.count, this.building, this.head);
            }
        }

        private String[] dealResult() {
            int i = this.count;
            if (i >= 8) {
                return this.results;
            }
            String[] re = new String[i];
            System.arraycopy(this.results, 0, re, 0, i);
            return re;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCity52s(Matcher matcher52s, String cut, String city) {
        String city2 = cut + city;
        if (matcher52s.group(8) == null) {
            if (matcher52s.group(5) == null) {
                return city2;
            }
            return matcher52s.group(5) + city2;
        } else if (matcher52s.group(5) == null) {
            return matcher52s.group(6) + matcher52s.group(8);
        } else {
            return matcher52s.group(5) + matcher52s.group(6) + matcher52s.group(8);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCut(Matcher matcherCut) {
        if (!matcherCut.matches() || matcherCut.group(1) == null) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        return matcherCut.group(1);
    }

    /* access modifiers changed from: private */
    public class SearchSpot {
        private String building = StorageManagerExt.INVALID_KEY_DESC;
        private int count = 0;
        private int full;
        private int head;
        private int head0;
        private int length;
        private String[] results = new String[8];
        private String str;
        private String subRight = StorageManagerExt.INVALID_KEY_DESC;

        SearchSpot(String string, int head2) {
            this.head = head2;
            this.length = string.length();
            this.head0 = head2;
            this.str = string;
            this.full = this.str.length();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String[] search() {
            int index;
            int index2 = 0;
            while (true) {
                int i = this.length;
                if (index2 >= i) {
                    return dealResult();
                }
                this.str = this.str.substring(index2, i);
                this.head = this.head0 + (this.full - this.str.length());
                this.length -= index2;
                int position = DicSearch.dicsearch(2, this.str.toLowerCase(Locale.getDefault()));
                if (position == 0) {
                    index = SearchEngine.this.dealPosition0(0, this.length, this.str);
                } else {
                    this.building = this.str.substring(0, position);
                    this.subRight = this.str.substring(position);
                    int lengthBracket = SearchEngine.this.searchBracket(this.subRight);
                    if (lengthBracket > 0) {
                        this.building += this.subRight.substring(0, lengthBracket);
                        this.subRight = this.subRight.substring(lengthBracket);
                    }
                    Matcher matcher52s = SearchEngine.this.regularExpression.getPat52s().matcher(this.subRight);
                    if (matcher52s.lookingAt()) {
                        match52sLookingAt(matcher52s);
                    } else {
                        match52sNotLookingAt();
                    }
                    index = (this.results[this.count - 1].length() + 0) - 1;
                }
                index2 = index + 1;
            }
        }

        private void match52sNotLookingAt() {
            Matcher matcher2s = SearchEngine.this.regularExpression.getPat2s().matcher(this.subRight);
            if (!matcher2s.lookingAt()) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (matcher2s.group(3) == null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (matcher2s.group(4) != null) {
                String[] strArr3 = this.results;
                int i3 = this.count;
                this.count = i3 + 1;
                strArr3[i3] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                matchCitySpot2(matcher2s);
            }
        }

        private void matchCitySpot2(Matcher matcher2s) {
            String cut = SearchEngine.this.getCut(SearchEngine.this.regularExpression.getPatCut().matcher(matcher2s.group(3)));
            String city = SearchEngine.this.searCity(matcher2s.group(3).substring(cut.length()), 2);
            if (city != null) {
                String city2 = SearchEngine.this.getCity2s(matcher2s, cut, city);
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + city2;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (SearchEngine.this.regularExpression.getPatPreCity2().matcher(matcher2s.group(1)).matches()) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (SearchEngine.this.regularExpression.getPatSingle().matcher(matcher2s.group(3)).matches()) {
                String[] strArr3 = this.results;
                int i3 = this.count;
                this.count = i3 + 1;
                strArr3[i3] = this.building + matcher2s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                String[] strArr4 = this.results;
                int i4 = this.count;
                this.count = i4 + 1;
                strArr4[i4] = this.building;
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            }
        }

        private void match52sLookingAt(Matcher matcher52s) {
            if (matcher52s.group(6) == null) {
                String[] strArr = this.results;
                int i = this.count;
                this.count = i + 1;
                strArr[i] = this.building + matcher52s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else if (matcher52s.group(7) != null) {
                String[] strArr2 = this.results;
                int i2 = this.count;
                this.count = i2 + 1;
                strArr2[i2] = this.building + matcher52s.group();
                SearchEngine.this.matchIndex2.add(Integer.valueOf(this.head));
            } else {
                SearchEngine.this.matchCitySpotOrDic(matcher52s, this.results, this.count, this.building, this.head);
            }
        }

        private String[] dealResult() {
            int i = this.count;
            if (i >= 8) {
                return this.results;
            }
            String[] re = new String[i];
            System.arraycopy(this.results, 0, re, 0, i);
            return re;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int dealPosition0(int index, int length, String str) {
        while (index < length && ((str.charAt(index) >= 'a' && str.charAt(index) <= 'z') || ((str.charAt(index) >= 'A' && str.charAt(index) <= 'Z') || (str.charAt(index) >= '0' && str.charAt(index) <= '9')))) {
            index++;
        }
        return index;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void matchCitySpotOrDic(Matcher matcher52s, String[] results, int count, String building, int head) {
        String cut = getCut(this.regularExpression.getPatCut().matcher(matcher52s.group(6)));
        String city = searCity(matcher52s.group(6).substring(cut.length()), 2);
        if (city != null) {
            String city2 = getCity52s(matcher52s, cut, city);
            int i = count + 1;
            results[count] = building + matcher52s.group(1) + matcher52s.group(2) + matcher52s.group(4) + city2;
            this.matchIndex2.add(Integer.valueOf(head));
        } else if (this.regularExpression.getPatPreCity2().matcher(matcher52s.group(4)).matches()) {
            int i2 = count + 1;
            results[count] = building + matcher52s.group();
            this.matchIndex2.add(Integer.valueOf(head));
        } else if (this.regularExpression.getPatSingle().matcher(matcher52s.group(3)).matches()) {
            int i3 = count + 1;
            results[count] = building + matcher52s.group();
            this.matchIndex2.add(Integer.valueOf(head));
        } else {
            int i4 = count + 1;
            results[count] = building + matcher52s.group(1) + matcher52s.group(2);
            this.matchIndex2.add(Integer.valueOf(head));
        }
    }
}
