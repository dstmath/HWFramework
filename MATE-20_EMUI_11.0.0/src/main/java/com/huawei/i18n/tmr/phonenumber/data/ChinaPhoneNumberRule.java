package com.huawei.i18n.tmr.phonenumber.data;

import android.util.Log;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChinaPhoneNumberRule extends PhoneNumberRule {
    private static final String REGION = "CN";
    private static final String TAG = "ChinaPhoneNumberRule";
    private final String negativeRule1 = "(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])";
    private final String negativeRule10 = "(\\d{1,16}[*.]{2,8})+(\\d{1,8})?";
    private final String negativeRule11 = "((\\d{1,16}(\\.)?\\d{1,10})(\\p{Sc}|印尼盾|美元|亿元|十万元?|百万元?|千万元?|万元|((港|澳|新?台|日)(币|元))|人民币|元))|((((港|澳|新?台|日)(币|元))|人民币|\\p{Sc}|标价为?|售价为?|价格为?)[:：]?(\\d{1,16}(\\.)?\\d{1,16}))";
    private final String negativeRule12 = "[A-Za-z]{1,20}(?<!(mobile|phone|tel(ephone(\\p{Blank}{1,4}number)?)?))[\\d-]{3,11}(?![-\\d])";
    private final String negativeRule13 = "\\{\\d{2,4}\\}(\\p{Blank})*\\d{1,4}";
    private final String negativeRule14 = "(?<![-\\d])(20|19)[0-9]{2}-?(1[0-2]|0?[1-9])-?([1-2][0-9]|3[0-1]|0?[1-9])(0?[0-9]|1[0-9]|2[0-4])(\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9])((\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9]))?";
    private final String negativeRule15 = "[@#][a-zA-Z_-]{0,20}[0-9]{4,}[a-zA-Z_-]{0,20}";
    private final String negativeRule2 = "\\d{3,17}(g|G|k|kB|KB|GB|kg|千克|毫升|mL|(平|立)方米|(m²)|(m³)|((平方|立方)?分米)|((平方|立方)?厘米)|((平方|立方)?毫米)|(千米)|(英尺)|(公里)|(公斤))(?!\\p{Alpha})";
    private final String negativeRule3 = "第\\d{3,17}(只|次|页|条|个|句)";
    private final String negativeRule4 = "(\\d{1,16}\\p{Blank}*[.．~～]\\p{Blank}*)+\\d{1,16}";
    private final String negativeRule5 = "(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))";
    private final String negativeRule6 = "[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}";
    private final String negativeRule7 = "(代金券|(账|帐)户号?|ID|id|验证码|校验码|动态码|密码|卡号|票号|单号|订单号?|证号|身份证(号码?)?|学号|邮编|代号|编号|昵称|(账|帐)号名?)(是|为|\\()?\\p{Blank}*[:：]?\\p{Blank}*[A-Za-z0-9_-]{1,30}";
    private final String negativeRule8 = "((WEIXIN|WeiBo|yy|qq)号?|群号|微博号?|微信号?|编(号|码))(是|为)?\\p{Blank}*[:：]?\\p{Blank}*\\d{4,17}";
    private final String negativeRule9 = "(?<!\\d)201[0-9](0?[1-9]|1[0-2])(0?[1-9]|[1-2][0-9]|3[01])(?!\\d)";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initPositiveRules() {
        this.positiveRules.add(getPositiveSlantRules());
        this.positiveRules.add(getPositiveBlankRules());
        this.positiveRules.add(getPositiveOperatorRules());
    }

    private PhoneNumberRule.RegexRule getPositiveOperatorRules() {
        return new PhoneNumberRule.RegexRule("(?<![-\\d])100\\d{4}(?![\\d])") {
            /* class com.huawei.i18n.tmr.phonenumber.data.ChinaPhoneNumberRule.AnonymousClass1 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                MatchedNumberInfo matcher = new MatchedNumberInfo();
                if (possibleNumber.rawString().startsWith("(") || possibleNumber.rawString().startsWith("[")) {
                    matcher.setBegin(possibleNumber.start() + 1);
                } else {
                    matcher.setBegin(possibleNumber.start());
                }
                matcher.setEnd(possibleNumber.end());
                matcher.setContent(msg);
                List<MatchedNumberInfo> matchList = new ArrayList<>(1);
                matchList.add(matcher);
                return matchList;
            }
        };
    }

    private PhoneNumberRule.RegexRule getPositiveBlankRules() {
        return new PhoneNumberRule.RegexRule("((?<!([-\\d])|(\\d\\p{Blank}{1,5}))[2-9](\\d{2}\\p{Blank}+\\d{4,5}|\\d{3}\\p{Blank}+\\d{3,4})(?!\\p{Blank}*\\d)|(?<![-\\d])[2-9]\\d{6,7}(?![\\d]))(;\\d{1})?") {
            /* class com.huawei.i18n.tmr.phonenumber.data.ChinaPhoneNumberRule.AnonymousClass2 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                String number = possibleNumber.rawString();
                Matcher matcher = getPattern().matcher(number);
                Matcher negativeMatcher = Pattern.compile("(?<![-\\d])(23{6,7})(?![-\\d])").matcher(number);
                List<MatchedNumberInfo> matchList = new ArrayList<>();
                if (!matcher.find() || negativeMatcher.find() || number.equals("5201314")) {
                    return matchList;
                }
                if (possibleNumber.rawString().charAt(0) == '(' || possibleNumber.rawString().charAt(0) == '[') {
                    matchedNumberInfo.setBegin(possibleNumber.start());
                } else {
                    matchedNumberInfo.setBegin(matcher.start() + possibleNumber.start());
                }
                matchedNumberInfo.setEnd(matcher.end() + possibleNumber.start());
                matchedNumberInfo.setContent(number);
                matchList.add(matchedNumberInfo);
                return matchList;
            }
        };
    }

    private PhoneNumberRule.RegexRule getPositiveSlantRules() {
        return new PhoneNumberRule.RegexRule("(?<![-\\d])\\d{5,6}[/|]\\d{5,6}(?![-\\d])") {
            /* class com.huawei.i18n.tmr.phonenumber.data.ChinaPhoneNumberRule.AnonymousClass3 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                MatchedNumberInfo numberInfo = new MatchedNumberInfo();
                String number = possibleNumber.rawString();
                Matcher matcher = getPattern().matcher(number);
                List<MatchedNumberInfo> matchList = new ArrayList<>();
                if (matcher.find()) {
                    int start = matcher.start();
                    try {
                        List<MatchedNumberInfo> tempList = ChinaPhoneNumberRule.getNumbersWithSlant(number);
                        if (tempList.size() == 2 && start == 1) {
                            start = 0;
                        }
                        if (!tempList.isEmpty()) {
                            matchedNumberInfo.setBegin(tempList.get(0).getBegin() + start + possibleNumber.start());
                            matchedNumberInfo.setEnd(tempList.get(0).getEnd() + possibleNumber.start());
                            matchedNumberInfo.setContent(tempList.get(0).getContent());
                            matchList.add(matchedNumberInfo);
                            if (tempList.size() == 2) {
                                numberInfo.setBegin(tempList.get(1).getBegin() + start + possibleNumber.start());
                                numberInfo.setEnd(tempList.get(1).getEnd() + possibleNumber.start());
                                numberInfo.setContent(tempList.get(1).getContent());
                                matchList.add(numberInfo);
                            }
                        }
                    } catch (NumberParseException e) {
                        Log.e(ChinaPhoneNumberRule.TAG, "getPositiveSlantRules NumberParseException");
                    }
                }
                return matchList;
            }
        };
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{3,17}(g|G|k|kB|KB|GB|kg|千克|毫升|mL|(平|立)方米|(m²)|(m³)|((平方|立方)?分米)|((平方|立方)?厘米)|((平方|立方)?毫米)|(千米)|(英尺)|(公里)|(公斤))(?!\\p{Alpha})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "第\\d{3,17}(只|次|页|条|个|句)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(\\d{1,16}\\p{Blank}*[.．~～]\\p{Blank}*)+\\d{1,16}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(代金券|(账|帐)户号?|ID|id|验证码|校验码|动态码|密码|卡号|票号|单号|订单号?|证号|身份证(号码?)?|学号|邮编|代号|编号|昵称|(账|帐)号名?)(是|为|\\()?\\p{Blank}*[:：]?\\p{Blank}*[A-Za-z0-9_-]{1,30}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "((WEIXIN|WeiBo|yy|qq)号?|群号|微博号?|微信号?|编(号|码))(是|为)?\\p{Blank}*[:：]?\\p{Blank}*\\d{4,17}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\d)201[0-9](0?[1-9]|1[0-2])(0?[1-9]|[1-2][0-9]|3[01])(?!\\d)", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(\\d{1,16}[*.]{2,8})+(\\d{1,8})?"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "((\\d{1,16}(\\.)?\\d{1,10})(\\p{Sc}|印尼盾|美元|亿元|十万元?|百万元?|千万元?|万元|((港|澳|新?台|日)(币|元))|人民币|元))|((((港|澳|新?台|日)(币|元))|人民币|\\p{Sc}|标价为?|售价为?|价格为?)[:：]?(\\d{1,16}(\\.)?\\d{1,16}))"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[A-Za-z]{1,20}(?<!(mobile|phone|tel(ephone(\\p{Blank}{1,4}number)?)?))[\\d-]{3,11}(?![-\\d])", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\{\\d{2,4}\\}(\\p{Blank})*\\d{1,4}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![-\\d])(20|19)[0-9]{2}-?(1[0-2]|0?[1-9])-?([1-2][0-9]|3[0-1]|0?[1-9])(0?[0-9]|1[0-9]|2[0-4])(\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9])((\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9]))?"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[@#][a-zA-Z_-]{0,20}[0-9]{4,}[a-zA-Z_-]{0,20}"));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(new PhoneNumberRule.RegexRule(StorageManagerExt.INVALID_KEY_DESC) {
            /* class com.huawei.i18n.tmr.phonenumber.data.ChinaPhoneNumberRule.AnonymousClass4 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch numberMatch, String msg) {
                boolean isValid = true;
                String number = numberMatch.rawString();
                int ind = number.trim().indexOf(";ext=");
                if (ind != -1) {
                    number = number.trim().substring(0, ind);
                }
                if (number.startsWith("(") || number.startsWith("[")) {
                    number = ChinaPhoneNumberRule.this.startWithBrackets(number, msg, numberMatch);
                }
                if (!number.startsWith("1") || ChinaPhoneNumberRule.countDigits(number) <= 11) {
                    if (number.startsWith("0") && ChinaPhoneNumberRule.countDigits(number) > 12 && number.charAt(1) != '0') {
                        isValid = false;
                    } else if ((number.startsWith("400") || number.startsWith("800")) && ChinaPhoneNumberRule.countDigits(number) != 10) {
                        isValid = false;
                    } else if (number.startsWith("1") || number.startsWith("0") || number.startsWith("400") || number.startsWith("800") || number.startsWith("+") || ChinaPhoneNumberRule.countDigits(number) < 9) {
                        if (ChinaPhoneNumberRule.countDigits(number) <= 4) {
                            isValid = false;
                        } else {
                            isValid = true;
                        }
                    } else if (!number.trim().startsWith("9") && !number.trim().startsWith("1")) {
                        isValid = false;
                    } else if (number.contains("/") || number.contains("\\") || number.contains("|")) {
                        isValid = true;
                    }
                } else if (number.startsWith("11808") || number.startsWith("17909") || number.startsWith("12593") || number.startsWith("17951") || number.startsWith("17911")) {
                    isValid = true;
                } else {
                    isValid = false;
                }
                if (isValid) {
                    return Optional.ofNullable(numberMatch);
                }
                return Optional.empty();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String startWithBrackets(String phone, String msg, PhoneNumberMatch numberMatch) {
        String right = StorageManagerExt.INVALID_KEY_DESC;
        if (phone.startsWith("(")) {
            right = ")";
        }
        if (phone.startsWith("[")) {
            right = "]";
        }
        int neind = phone.indexOf(right);
        int meind = msg.indexOf(right);
        if (neind == -1) {
            return phone.substring(1);
        }
        int phoneLength = countDigits(phone.substring(0, neind));
        int extra = countDigits(phone.substring(neind));
        if (phoneLength <= 4 || !(extra == 1 || extra == 2)) {
            return phone.substring(1);
        }
        String number = phone.substring(1, neind);
        for (PhoneNumberMatch phoneNumberMatch : PhoneNumberUtil.getInstance().findNumbers(msg.substring(0, meind + 1), REGION, PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE)) {
        }
        return number;
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,}|10{8,})", 2, 9));
    }

    /* access modifiers changed from: private */
    public static int countDigits(String str) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (Character.isDigit(ch)) {
                count++;
            }
        }
        return count;
    }

    /* access modifiers changed from: private */
    public static List<MatchedNumberInfo> getNumbersWithSlant(String testStr) throws NumberParseException {
        List<MatchedNumberInfo> shortList = new ArrayList<>();
        PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
        ShortNumberInfo shortInfo = ShortNumberInfo.getInstance();
        String numberFirst = StorageManagerExt.INVALID_KEY_DESC;
        String numberEnd = StorageManagerExt.INVALID_KEY_DESC;
        int slantIndex = 0;
        for (int i = 0; i < testStr.length(); i++) {
            if (testStr.charAt(i) == '/') {
                slantIndex = i;
                numberFirst = testStr.substring(0, i);
                numberEnd = testStr.substring(i + 1);
            }
        }
        Phonenumber.PhoneNumber phoneNumberFirst = pnu.parse(numberFirst, REGION);
        Phonenumber.PhoneNumber phoneNumberEnd = pnu.parse(numberEnd, REGION);
        if (shortInfo.isValidShortNumber(phoneNumberFirst)) {
            MatchedNumberInfo matchedNumberInfoFirst = new MatchedNumberInfo();
            matchedNumberInfoFirst.setBegin(0);
            matchedNumberInfoFirst.setEnd(slantIndex);
            matchedNumberInfoFirst.setContent(numberFirst);
            shortList.add(matchedNumberInfoFirst);
        }
        if (shortInfo.isValidShortNumber(phoneNumberEnd)) {
            MatchedNumberInfo matchedNumberInfoEnd = new MatchedNumberInfo();
            matchedNumberInfoEnd.setBegin(slantIndex + 1);
            matchedNumberInfoEnd.setEnd(testStr.length());
            matchedNumberInfoEnd.setContent(numberEnd);
            shortList.add(matchedNumberInfoEnd);
        }
        return shortList;
    }
}
