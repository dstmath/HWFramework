package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_ZH_CN extends PhoneNumberRule {
    private final String REGION = "CN";

    public PhoneNumberRule_ZH_CN(String country) {
        super(country);
        init();
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public void init() {
        /*
            r30 = this;
            r0 = r30
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r0.codesRules = r3
            java.lang.String r3 = "(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])"
            java.lang.String r4 = "\\d{3,17}(g|G|k|kB|KB|GB|kg|千克|毫升|mL|(平|立)方米|(m²)|(m³)|((平方|立方)?分米)|((平方|立方)?厘米)|((平方|立方)?毫米)|(千米)|(英尺)|(公里)|(公斤))(?!\\p{Alpha})"
            java.lang.String r5 = "第\\d{3,17}(只|次|页|条|个|句)"
            java.lang.String r6 = "(\\d{1,16}\\p{Blank}*[.．~～]\\p{Blank}*)+\\d{1,16}"
            java.lang.String r7 = "(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))"
            java.lang.String r8 = "[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}"
            java.lang.String r9 = "(代金券|(账|帐)户号?|ID|id|验证码|校验码|动态码|密码|卡号|票号|单号|订单号?|证号|身份证(号码?)?|学号|邮编|代号|编号|昵称|(账|帐)号名?)(是|为)?\\p{Blank}*[:：]?\\p{Blank}*[A-Za-z0-9_-]{1,30}"
            java.lang.String r10 = "((WEIXIN|WeiBo|yy|qq)号?|群号|微博号?|微信号?|编(号|码))(是|为)?\\p{Blank}*[:：]?\\p{Blank}*\\d{4,17}"
            java.lang.String r11 = "(?<!\\d)201[0-9](0?[1-9]|1[0-2])(0?[1-9]|[1-2][0-9]|3[01])(?!\\d)"
            java.lang.String r12 = "(\\d{1,16}[*.]{2,8})+(\\d{1,8})?"
            java.lang.String r13 = "((\\d{1,16}(\\.)?\\d{1,10})(\\p{Sc}|印尼盾|美元|亿元|十万元?|百万元?|千万元?|万元|((港|澳|新?台|日)(币|元))|人民币))|((((港|澳|新?台|日)(币|元))|人民币|\\p{Sc}|标价为?|售价为?|价格为?)[:：]?(\\d{1,16}(\\.)?\\d{1,16}))"
            java.lang.String r14 = "[A-Za-z]{1,20}(?<!(mobile|phone|tel(ephone(\\p{Blank}{1,4}number)?)?))[\\d-]{3,11}(?![-\\d])"
            java.lang.String r15 = "\\{\\d{2,4}\\}(\\p{Blank})*\\d{1,4}"
            r16 = r2
            java.lang.String r2 = "(?<![-\\d])(20|19)[0-9]{2}-?(1[0-2]|0?[1-9])-?([1-2][0-9]|3[0-1]|0?[1-9])(0?[0-9]|1[0-9]|2[0-4])(\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9])((\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9]))?"
            r17 = r2
            java.lang.String r2 = "[@#][a-zA-Z_-]{0,20}[0-9]{4,}[a-zA-Z_-]{0,20}"
            r18 = r2
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r3)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r19 = r3
            r3 = 2
            r2.<init>(r4, r3)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r5)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r6)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r7)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r8)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r9)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r10, r3)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r11)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r12)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r13)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r14, r3)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r2.<init>(r15)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r3 = r17
            r2.<init>(r3)
            r1.add(r2)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r2 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r20 = r3
            r3 = r18
            r2.<init>(r3)
            r1.add(r2)
            r0.negativeRules = r1
            java.lang.String r2 = "(?<![-\\d])\\d{5,6}[/|]\\d{5,6}(?![-\\d])"
            r21 = r1
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$1 r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$1
            r1.<init>(r0, r2)
            r22 = r2
            r2 = r16
            r2.add(r1)
            java.lang.String r1 = "((?<!([-\\d])|(\\d\\p{Blank}{1,5}))[2-9](\\d{2}\\p{Blank}+\\d{4,5}|\\d{3}\\p{Blank}+\\d{3,4})(?!\\p{Blank}*\\d)|(?<![-\\d])[2-9]\\d{6,7}(?![\\d]))(;\\d{1})?"
            r23 = r3
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$2 r3 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$2
            r3.<init>(r0, r1)
            r2.add(r3)
            java.lang.String r3 = "(?<![-\\d])100\\d{4}(?![\\d])"
            r24 = r1
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$3 r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$3
            r1.<init>(r0, r3)
            r2.add(r1)
            r0.positiveRules = r2
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$4 r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN$4
            r25 = r2
            java.lang.String r2 = ""
            r1.<init>(r0, r2)
            java.util.List r2 = r0.codesRules
            r2.add(r1)
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r26 = r1
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r27 = r3
            java.lang.String r3 = "(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,}|10{8,})"
            r28 = r4
            r4 = 9
            r29 = r5
            r5 = 2
            r1.<init>(r3, r5, r4)
            r2.add(r1)
            r0.borderRules = r2
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ZH_CN.init():void");
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
        String number1 = "";
        String number2 = "";
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
