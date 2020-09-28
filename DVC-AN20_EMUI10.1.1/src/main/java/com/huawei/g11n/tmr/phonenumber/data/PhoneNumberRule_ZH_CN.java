package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule_ZH_CN extends PhoneNumberRule {
    private final String REGION = "CN";

    public PhoneNumberRule_ZH_CN(String country) {
        super(country);
        init();
    }

    @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        List<PhoneNumberRule.RegexRule> nRules = new ArrayList<>();
        List<PhoneNumberRule.RegexRule> pRules = new ArrayList<>();
        this.codesRules = new ArrayList();
        nRules.add(new PhoneNumberRule.RegexRule("(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])"));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{3,17}(g|G|k|kB|KB|GB|kg|千克|毫升|mL|(平|立)方米|(m²)|(m³)|((平方|立方)?分米)|((平方|立方)?厘米)|((平方|立方)?毫米)|(千米)|(英尺)|(公里)|(公斤))(?!\\p{Alpha})", 2));
        nRules.add(new PhoneNumberRule.RegexRule("第\\d{3,17}(只|次|页|条|个|句)"));
        nRules.add(new PhoneNumberRule.RegexRule("(\\d{1,16}\\p{Blank}*[.．~～]\\p{Blank}*)+\\d{1,16}"));
        nRules.add(new PhoneNumberRule.RegexRule("(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))"));
        nRules.add(new PhoneNumberRule.RegexRule("[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}"));
        nRules.add(new PhoneNumberRule.RegexRule("(代金券|(账|帐)户号?|ID|id|验证码|校验码|动态码|密码|卡号|票号|单号|订单号?|证号|身份证(号码?)?|学号|邮编|代号|编号|昵称|(账|帐)号名?)(是|为)?\\p{Blank}*[:：]?\\p{Blank}*[A-Za-z0-9_-]{1,30}"));
        nRules.add(new PhoneNumberRule.RegexRule("((WEIXIN|WeiBo|yy|qq)号?|群号|微博号?|微信号?|编(号|码))(是|为)?\\p{Blank}*[:：]?\\p{Blank}*\\d{4,17}", 2));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\d)201[0-9](0?[1-9]|1[0-2])(0?[1-9]|[1-2][0-9]|3[01])(?!\\d)"));
        nRules.add(new PhoneNumberRule.RegexRule("(\\d{1,16}[*.]{2,8})+(\\d{1,8})?"));
        nRules.add(new PhoneNumberRule.RegexRule("((\\d{1,16}(\\.)?\\d{1,10})(\\p{Sc}|印尼盾|美元|亿元|十万元?|百万元?|千万元?|万元|((港|澳|新?台|日)(币|元))|人民币))|((((港|澳|新?台|日)(币|元))|人民币|\\p{Sc}|标价为?|售价为?|价格为?)[:：]?(\\d{1,16}(\\.)?\\d{1,16}))"));
        nRules.add(new PhoneNumberRule.RegexRule("[A-Za-z]{1,20}(?<!(mobile|phone|tel(ephone(\\p{Blank}{1,4}number)?)?))[\\d-]{3,11}(?![-\\d])", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\{\\d{2,4}\\}(\\p{Blank})*\\d{1,4}"));
        nRules.add(new PhoneNumberRule.RegexRule("(?<![-\\d])(20|19)[0-9]{2}-?(1[0-2]|0?[1-9])-?([1-2][0-9]|3[0-1]|0?[1-9])(0?[0-9]|1[0-9]|2[0-4])(\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9])((\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9]))?"));
        nRules.add(new PhoneNumberRule.RegexRule("[@#][a-zA-Z_-]{0,20}[0-9]{4,}[a-zA-Z_-]{0,20}"));
        this.negativeRules = nRules;
        pRules.add(new PhoneNumberRule.RegexRule(this, "(?<![-\\d])\\d{5,6}[/|]\\d{5,6}(?![-\\d])") {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN.AnonymousClass1 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                List<MatchedNumberInfo> matchList = new ArrayList<>();
                MatchedNumberInfo temp1 = new MatchedNumberInfo();
                MatchedNumberInfo temp2 = new MatchedNumberInfo();
                String number = possibleNumber.rawString();
                Matcher m = getPattern().matcher(number);
                if (m.find()) {
                    int start = m.start();
                    try {
                        List<MatchedNumberInfo> tempList = PhoneNumberRule_ZH_CN.getNumbersWithSlant(number);
                        if (tempList.size() == 2 && start == 1) {
                            start = 0;
                        }
                        if (!tempList.isEmpty()) {
                            temp1.setBegin(tempList.get(0).getBegin() + start + possibleNumber.start());
                            temp1.setEnd(tempList.get(0).getEnd() + possibleNumber.start());
                            temp1.setContent(tempList.get(0).getContent());
                            matchList.add(temp1);
                            if (tempList.size() == 2) {
                                temp2.setBegin(tempList.get(1).getBegin() + start + possibleNumber.start());
                                temp2.setEnd(tempList.get(1).getEnd() + possibleNumber.start());
                                temp2.setContent(tempList.get(1).getContent());
                                matchList.add(temp2);
                            }
                        }
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
                return matchList;
            }
        });
        pRules.add(new PhoneNumberRule.RegexRule(this, "((?<!([-\\d])|(\\d\\p{Blank}{1,5}))[2-9](\\d{2}\\p{Blank}+\\d{4,5}|\\d{3}\\p{Blank}+\\d{3,4})(?!\\p{Blank}*\\d)|(?<![-\\d])[2-9]\\d{6,7}(?![\\d]))(;\\d{1})?") {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN.AnonymousClass2 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                List<MatchedNumberInfo> matchList = new ArrayList<>();
                MatchedNumberInfo matcher = new MatchedNumberInfo();
                String number = possibleNumber.rawString();
                Matcher m = getPattern().matcher(number);
                Matcher sm = Pattern.compile("(?<![-\\d])(23{6,7})(?![-\\d])").matcher(number);
                if (!m.find() || sm.find() || number.equals("5201314")) {
                    return matchList;
                }
                if (possibleNumber.rawString().charAt(0) == '(' || possibleNumber.rawString().charAt(0) == '[') {
                    matcher.setBegin(possibleNumber.start());
                } else {
                    matcher.setBegin(m.start() + possibleNumber.start());
                }
                matcher.setEnd(m.end() + possibleNumber.start());
                matcher.setContent(number);
                matchList.add(matcher);
                return matchList;
            }
        });
        pRules.add(new PhoneNumberRule.RegexRule(this, "(?<![-\\d])100\\d{4}(?![\\d])") {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN.AnonymousClass3 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                List<MatchedNumberInfo> matchList = new ArrayList<>();
                MatchedNumberInfo matcher = new MatchedNumberInfo();
                if (possibleNumber.rawString().startsWith("(") || possibleNumber.rawString().startsWith("[")) {
                    matcher.setBegin(possibleNumber.start() + 1);
                } else {
                    matcher.setBegin(possibleNumber.start());
                }
                matcher.setEnd(possibleNumber.end());
                matcher.setContent(msg);
                matchList.add(matcher);
                return matchList;
            }
        });
        this.positiveRules = pRules;
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN.AnonymousClass4 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                PhoneNumberMatch possibleNumber2;
                boolean isvalid = true;
                String number = possibleNumber.rawString();
                int ind = number.trim().indexOf(";ext=");
                if (ind != -1) {
                    number = number.trim().substring(0, ind);
                }
                if (number.startsWith("(") || number.startsWith("[")) {
                    String rStr = BuildConfig.FLAVOR;
                    if (number.startsWith("(")) {
                        rStr = ")";
                    }
                    if (number.startsWith("[")) {
                        rStr = "]";
                    }
                    int neind = number.indexOf(rStr);
                    int meind = msg.indexOf(rStr);
                    if (neind != -1) {
                        int phoneLength = PhoneNumberRule_ZH_CN.countDigits(number.substring(0, neind));
                        int extra = PhoneNumberRule_ZH_CN.countDigits(number.substring(neind));
                        if (phoneLength <= 4 || !(extra == 1 || extra == 2)) {
                            number = number.substring(1);
                            possibleNumber2 = possibleNumber;
                        } else {
                            String number2 = number.substring(1, neind);
                            Iterator<PhoneNumberMatch> it = PhoneNumberUtil.getInstance().findNumbers(msg.substring(0, meind + 1), "CN", PhoneNumberUtil.Leniency.POSSIBLE, Long.MAX_VALUE).iterator();
                            PhoneNumberMatch possibleNumber3 = possibleNumber;
                            while (it.hasNext()) {
                                possibleNumber3 = it.next();
                            }
                            possibleNumber2 = possibleNumber3;
                            number = number2;
                        }
                    } else {
                        number = number.substring(1);
                        possibleNumber2 = possibleNumber;
                    }
                } else {
                    possibleNumber2 = possibleNumber;
                }
                if (number.startsWith("1") && PhoneNumberRule_ZH_CN.countDigits(number) > 11) {
                    isvalid = number.startsWith("11808") || number.startsWith("17909") || number.startsWith("12593") || number.startsWith("17951") || number.startsWith("17911");
                } else if (number.startsWith("0") && PhoneNumberRule_ZH_CN.countDigits(number) > 12 && number.charAt(1) != '0') {
                    isvalid = false;
                } else if ((number.startsWith("400") || number.startsWith("800")) && PhoneNumberRule_ZH_CN.countDigits(number) != 10) {
                    isvalid = false;
                } else if (number.startsWith("1") || number.startsWith("0") || number.startsWith("400") || number.startsWith("800") || number.startsWith("+") || PhoneNumberRule_ZH_CN.countDigits(number) < 9) {
                    if (PhoneNumberRule_ZH_CN.countDigits(number) <= 4) {
                        isvalid = false;
                    }
                } else if (!number.trim().startsWith("9") && !number.trim().startsWith("1")) {
                    isvalid = false;
                } else if (number.contains("/") || number.contains("\\") || number.contains("|")) {
                    isvalid = true;
                }
                if (isvalid) {
                    return possibleNumber2;
                }
                return null;
            }
        });
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
        bRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,}|10{8,})", 2, 9));
        this.borderRules = bRules;
    }

    /* access modifiers changed from: private */
    public static int countDigits(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
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
        String number1 = BuildConfig.FLAVOR;
        String number2 = BuildConfig.FLAVOR;
        int slantIndex = 0;
        for (int i = 0; i < testStr.length(); i++) {
            if (testStr.charAt(i) == '/') {
                slantIndex = i;
                number1 = testStr.substring(0, i);
                number2 = testStr.substring(i + 1, testStr.length());
            }
        }
        Phonenumber.PhoneNumber phoneNumber1 = pnu.parse(number1, "CN");
        Phonenumber.PhoneNumber phoneNumber2 = pnu.parse(number2, "CN");
        if (shortInfo.isValidShortNumber(phoneNumber1)) {
            MatchedNumberInfo info1 = new MatchedNumberInfo();
            info1.setBegin(0);
            info1.setEnd(slantIndex);
            info1.setContent(number1);
            shortList.add(info1);
        }
        if (shortInfo.isValidShortNumber(phoneNumber2)) {
            MatchedNumberInfo info2 = new MatchedNumberInfo();
            info2.setBegin(slantIndex + 1);
            info2.setEnd(testStr.length());
            info2.setContent(number2);
            shortList.add(info2);
        }
        return shortList;
    }
}
