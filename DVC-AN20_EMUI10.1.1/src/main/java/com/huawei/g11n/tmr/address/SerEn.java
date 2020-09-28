package com.huawei.g11n.tmr.address;

import com.huawei.g11n.tmr.address.jni.DicSearch;
import com.huawei.sidetouch.TpCommandConstant;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SerEn {
    private static final int TYPE_BUILDING = 1;
    private static final int TYPE_BUILDING2 = 2;
    private static final int TYPE_CITY = 0;
    private String location = this.reguEx.location;
    ArrayList<Integer> match_index_2 = new ArrayList<>();
    private String not = "(?i)(?:my|your|his|her|its|their|our|this|that|the|a|an|what|which|whose)";
    private Pattern p1346 = this.reguEx.p1346;
    private Pattern p28 = this.reguEx.p28;
    private Pattern p2s = this.reguEx.p2s;
    private Pattern p52 = this.reguEx.p52;
    private Pattern p52_sub = this.reguEx.p52_sub;
    private Pattern p52s = this.reguEx.p52s;
    private Pattern pCode_a = Pattern.compile("(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d)");
    Pattern pComma = Pattern.compile("(?:(?:[\\s\\S]*)(?:,|\\.)([\\s\\S]*))");
    Pattern pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
    private Pattern pDir = Pattern.compile("\\s*(south|north|west|east)\\s*");
    Pattern pLocation = Pattern.compile("(?:([\\s\\S]*?)(?<![a-zA-Z])((?:" + this.location + ")((?:\\s+|\\s*&\\s*)(?:" + this.location + "))?" + ")(?![a-zA-Z]))");
    Pattern pNo = Pattern.compile("(?:[\\s\\S]*(?<![a-zA-Z])(?i)(the|in|on|at|from|to|of|for)(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+))");
    Pattern pNot_1 = Pattern.compile("([\\s\\S]*?)(?<![a-zA-Z])" + this.location + "(?![a-zA-Z])");
    Pattern pNot_2 = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])" + this.not + "\\s+");
    private Pattern pNum = Pattern.compile("(?:(?:\\s*[:,\\.\"-]\\s*|\\s*)\\d+(?:\\s*[,\\.\":-]\\s*|\\s+))+");
    Pattern pPre_city = Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])");
    Pattern pPre_uni = Pattern.compile("(?:\\b(?i)(in|at|from|near|to|of|for)\\b([\\s\\S]*))");
    private Pattern pRoad = Pattern.compile("(?i)(?:\\s*(?:(in|on|at)\\s+)?(?:the\\s+)?(boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Way|Fwy|Crescent|Highway))");
    Pattern pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
    private Pattern p_box = Pattern.compile(this.reguEx.post_box);
    private Pattern p_resultclean = Pattern.compile("(?:(?:[^0-9a-zA-Z]*)(?i)(?:(?:in|at|on|from|to|of|and)\\s+)?(?:(?:the)\\s+)?)(?:([\\s\\S]*)?,|([\\s\\S]*))");
    private ReguEx reguEx = new ReguEx();
    private String road_suf = "(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))";

    SerEn() {
    }

    /* JADX INFO: Multiple debug info for r13v6 int[]: [D('start' int), D('nn_new2' int[])] */
    /* JADX INFO: Multiple debug info for r5v24 int: [D('end' int), D('mNum' java.util.regex.Matcher)] */
    /* JADX INFO: Multiple debug info for r2v29 'pSingle'  java.util.regex.Pattern: [D('pSingle' java.util.regex.Pattern), D('mRoad' java.util.regex.Matcher)] */
    public ArrayList<Match> search(String source) {
        int start;
        int end;
        int start2;
        int i;
        Pattern pPre_road;
        Pattern pNot_road;
        String str;
        int start3;
        String out;
        Matcher m28;
        Matcher m52;
        Pattern pNot_road2;
        Pattern pPre_road2;
        Pattern pSingle2;
        Pattern p_big;
        int start4;
        int head;
        String[] buildings;
        int outLen;
        String out2;
        String out3;
        Pattern pSingle3;
        String[] buildings2;
        int length_bui;
        int start5;
        String out4;
        String out5;
        Pattern pSingle4;
        Pattern pPre_road3;
        Pattern pNot_road3;
        Matcher m282;
        Matcher m522;
        Pattern pPre_road4;
        String str2;
        Pattern pSingle5;
        String cut;
        String out6;
        int outLen2;
        int outLen3;
        String out7;
        int outLen4;
        String out8;
        int outLen5;
        int outLen6;
        String out9;
        String out10;
        int outLen7;
        String str3;
        Pattern pPre_road5;
        Pattern pNot_road4;
        String city;
        Matcher m283;
        Pattern pPre_road6;
        Pattern pSingle6;
        Pattern pSingle7;
        Pattern pCut2;
        String cut2;
        String out11;
        int outLen8;
        int outLen9;
        String out12;
        int outLen10;
        String out13;
        int outLen11;
        int outLen12;
        String out14;
        int outLen13;
        String out15;
        Matcher m_resultclean;
        int outLen14;
        String out16;
        int outLen15;
        String str4 = source;
        ArrayList<Integer> nn = new ArrayList<>();
        Pattern pPre_road7 = Pattern.compile("[A-Z0-9]");
        Pattern pCut3 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(?:uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle8 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_road8 = Pattern.compile("(?i)(?<![a-z])(?:(?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+)((?:[\\s\\S]+?)(?:(?<![a-z])((?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+))?" + this.road_suf + "(?![a-zA-Z])[\\s\\S]*)");
        Pattern pNot_road5 = Pattern.compile("(?i)((?<![a-zA-Z])(?:a|what|which|whose|i|you|this|that|my|his|her|out|their|its)\\s+)([\\s\\S]+)?" + this.road_suf + "(?![a-zA-Z])");
        Pattern pBuilding = Pattern.compile("(?:[^0-9a-zA-Z]*|\\s*(?:(?i)the|this|a|that)\\s*)(?:" + this.location + ")[^0-9a-zA-Z]*");
        int start6 = 0;
        int end2 = 0;
        String out17 = BuildConfig.FLAVOR;
        nn.add(0);
        Matcher m523 = this.p52.matcher(str4);
        Matcher m284 = this.p28.matcher(str4);
        Matcher m1346 = this.p1346.matcher(str4);
        Matcher m_52sub = this.p52_sub.matcher(str4);
        boolean noBox = true;
        this.match_index_2.clear();
        String city2 = BuildConfig.FLAVOR;
        while (true) {
            start = start6;
            if (!m284.find()) {
                break;
            }
            String city3 = city2;
            if (m284.group(1) == null) {
                if (!this.pCode_a.matcher(m284.group()).find()) {
                    start6 = m284.start();
                    end2 = start6 + m284.group().length();
                    nn.add(Integer.valueOf(start6));
                    nn.add(Integer.valueOf(end2));
                    pPre_road8 = pPre_road8;
                    pNot_road5 = pNot_road5;
                    city2 = city3;
                    pCut3 = pCut3;
                    pPre_road7 = pPre_road7;
                    pSingle8 = pSingle8;
                    m284 = m284;
                    str4 = source;
                } else if (m284.group(6).indexOf(45) != -1) {
                    start6 = m284.start(6);
                    end2 = start6 + m284.group(6).length();
                    nn.add(Integer.valueOf(start6));
                    nn.add(Integer.valueOf(end2));
                    pPre_road8 = pPre_road8;
                    pNot_road5 = pNot_road5;
                    city2 = city3;
                    pCut3 = pCut3;
                    pPre_road7 = pPre_road7;
                    pSingle8 = pSingle8;
                    m284 = m284;
                    str4 = source;
                } else if (m284.group(5) != null && m284.group(5).length() > 0) {
                    start6 = m284.start(6);
                    end2 = start6 + m284.group(6).length();
                    nn.add(Integer.valueOf(start6));
                    nn.add(Integer.valueOf(end2));
                    pPre_road8 = pPre_road8;
                    pNot_road5 = pNot_road5;
                    city2 = city3;
                    pCut3 = pCut3;
                    pPre_road7 = pPre_road7;
                    pSingle8 = pSingle8;
                    m284 = m284;
                    str4 = source;
                }
            } else if (m284.group(4) != null) {
                Matcher m_resultclean2 = this.p_resultclean.matcher(m284.group());
                if (m_resultclean2.matches()) {
                    if (m_resultclean2.group(1) != null) {
                        String out18 = m_resultclean2.group(1);
                        out17 = out18;
                        outLen15 = out18.length() + 1;
                    } else {
                        String out19 = m_resultclean2.group(2);
                        outLen15 = out19.length();
                        out17 = out19;
                    }
                    start6 = m284.start() + (m284.group().length() - outLen15);
                    end2 = start6 + out17.length();
                    nn.add(Integer.valueOf(start6));
                    nn.add(Integer.valueOf(end2));
                    if (m284.group(2) != null) {
                        noBox = false;
                        pPre_road8 = pPre_road8;
                        pNot_road5 = pNot_road5;
                        city2 = city3;
                        pCut3 = pCut3;
                        pPre_road7 = pPre_road7;
                        pSingle8 = pSingle8;
                        m284 = m284;
                        str4 = source;
                    } else {
                        pPre_road8 = pPre_road8;
                        pNot_road5 = pNot_road5;
                        city2 = city3;
                        pCut3 = pCut3;
                        pPre_road7 = pPre_road7;
                        pSingle8 = pSingle8;
                        m284 = m284;
                        str4 = source;
                    }
                }
            } else {
                if (m284.group(2) != null) {
                    m_resultclean = this.p_resultclean.matcher(m284.group());
                } else {
                    String city4 = searCity(m284.group(3), 1);
                    if (city4 == null) {
                        city4 = BuildConfig.FLAVOR;
                    }
                    city3 = city4;
                    m_resultclean = this.p_resultclean.matcher(String.valueOf(city4) + m284.group(5) + m284.group(6));
                }
                if (m_resultclean.matches()) {
                    if (m_resultclean.group(1) != null) {
                        String out20 = m_resultclean.group(1);
                        out16 = out20;
                        outLen14 = out20.length() + 1;
                    } else {
                        String out21 = m_resultclean.group(2);
                        outLen14 = out21.length();
                        out16 = out21;
                    }
                    start6 = m284.start(5) + ((String.valueOf(m284.group(5)) + m284.group(6)).length() - outLen14);
                    end2 = start6 + out17.length();
                    nn.add(Integer.valueOf(start6));
                    nn.add(Integer.valueOf(end2));
                    if (m284.group(2) != null) {
                        noBox = false;
                        pPre_road8 = pPre_road8;
                        pNot_road5 = pNot_road5;
                        city2 = city3;
                        pCut3 = pCut3;
                        pPre_road7 = pPre_road7;
                        pSingle8 = pSingle8;
                        m284 = m284;
                        str4 = source;
                    } else {
                        pPre_road8 = pPre_road8;
                        pNot_road5 = pNot_road5;
                        city2 = city3;
                        pCut3 = pCut3;
                        pPre_road7 = pPre_road7;
                        pSingle8 = pSingle8;
                        m284 = m284;
                        str4 = source;
                    }
                } else {
                    pPre_road8 = pPre_road8;
                    pNot_road5 = pNot_road5;
                    start6 = start;
                    city2 = city3;
                    pCut3 = pCut3;
                    pPre_road7 = pPre_road7;
                    pSingle8 = pSingle8;
                    m284 = m284;
                    str4 = source;
                }
            }
            pPre_road8 = pPre_road8;
            pNot_road5 = pNot_road5;
            start6 = start;
            city2 = city3;
            pCut3 = pCut3;
            pPre_road7 = pPre_road7;
            pSingle8 = pSingle8;
            m284 = m284;
            str4 = source;
        }
        if (noBox) {
            Matcher m_box = this.p_box.matcher(str4);
            while (m_box.find()) {
                start = m_box.start();
                end2 = start + m_box.group().length();
                nn.add(Integer.valueOf(start));
                nn.add(Integer.valueOf(end2));
                city2 = city2;
            }
            start2 = start;
        } else {
            start2 = start;
        }
        while (m523.find()) {
            String out22 = BuildConfig.FLAVOR;
            if (!this.pRoad.matcher(m523.group()).matches()) {
                m523 = m523;
                if (m523.group(5) == null) {
                    Matcher m_resultclean3 = this.p_resultclean.matcher(m523.group(1));
                    if (m_resultclean3.matches()) {
                        if (m_resultclean3.group(1) != null) {
                            out15 = m_resultclean3.group(1);
                            outLen13 = out15.length() + 1;
                        } else {
                            String out23 = m_resultclean3.group(2);
                            outLen13 = out23.length();
                            out15 = out23;
                        }
                        int start7 = m523.start(1) + (m523.group(1).length() - outLen13);
                        pSingle7 = pSingle8;
                        pCut2 = pCut3;
                        end = start7 + out15.length();
                        start2 = start7;
                        city = city2;
                        out22 = out15;
                    } else {
                        pSingle7 = pSingle8;
                        pCut2 = pCut3;
                        city = city2;
                    }
                } else if (m523.group(6) != null) {
                    Matcher m_resultclean4 = this.p_resultclean.matcher(m523.group());
                    if (m_resultclean4.matches()) {
                        if (m_resultclean4.group(1) != null) {
                            out14 = m_resultclean4.group(1);
                            outLen12 = out14.length() + 1;
                        } else {
                            out14 = m_resultclean4.group(2);
                            outLen12 = out14.length();
                        }
                        int start8 = m523.start() + (m523.group().length() - outLen12);
                        pSingle7 = pSingle8;
                        pCut2 = pCut3;
                        end = out14.length() + start8;
                        city = city2;
                        start2 = start8;
                        out22 = out14;
                    } else {
                        pSingle7 = pSingle8;
                        pCut2 = pCut3;
                        city = city2;
                    }
                } else {
                    Matcher mCut = pCut3.matcher(m523.group(5));
                    if (!mCut.matches()) {
                        cut2 = BuildConfig.FLAVOR;
                    } else if (mCut.group(1) != null) {
                        cut2 = mCut.group(1);
                    } else {
                        cut2 = BuildConfig.FLAVOR;
                    }
                    pCut2 = pCut3;
                    city = searCity(m523.group(5).substring(cut2.length(), m523.group(5).length()), 2);
                    if (city != null) {
                        pSingle7 = pSingle8;
                        city = String.valueOf(cut2) + city;
                        if (m523.group(7) == null) {
                            if (m523.group(4) != null) {
                                city = String.valueOf(m523.group(4)) + city;
                            }
                        } else if (m523.group(4) != null) {
                            city = String.valueOf(m523.group(4)) + m523.group(5) + m523.group(7);
                        } else {
                            city = String.valueOf(m523.group(5)) + m523.group(7);
                        }
                        Matcher m_resultclean5 = this.p_resultclean.matcher(String.valueOf(m523.group(1)) + m523.group(3) + city);
                        if (m_resultclean5.matches()) {
                            if (m_resultclean5.group(1) != null) {
                                String out24 = m_resultclean5.group(1);
                                out11 = out24;
                                outLen8 = out24.length() + 1;
                            } else {
                                out11 = m_resultclean5.group(2);
                                outLen8 = out11.length();
                            }
                            int start9 = m523.start(1) + ((String.valueOf(m523.group(1)) + m523.group(3) + city).length() - outLen8);
                            end = out11.length() + start9;
                            start2 = start9;
                            out22 = out11;
                        }
                    } else if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(m523.group(3)).lookingAt()) {
                        Matcher m_resultclean6 = this.p_resultclean.matcher(m523.group());
                        if (m_resultclean6.matches()) {
                            if (m_resultclean6.group(1) != null) {
                                out22 = m_resultclean6.group(1);
                                outLen11 = out22.length() + 1;
                            } else {
                                out22 = m_resultclean6.group(2);
                                outLen11 = out22.length();
                            }
                            int start10 = m523.start() + (m523.group().length() - outLen11);
                            pSingle7 = pSingle8;
                            end = out22.length() + start10;
                            start2 = start10;
                        } else {
                            pSingle7 = pSingle8;
                        }
                    } else if (pSingle8.matcher(m523.group(5)).matches()) {
                        pSingle7 = pSingle8;
                        Matcher m_resultclean7 = this.p_resultclean.matcher(m523.group());
                        if (m_resultclean7.matches()) {
                            if (m_resultclean7.group(1) != null) {
                                out13 = m_resultclean7.group(1);
                                outLen10 = out13.length() + 1;
                            } else {
                                out13 = m_resultclean7.group(2);
                                outLen10 = out13.length();
                            }
                            int start11 = m523.start() + (m523.group().length() - outLen10);
                            end = out22.length() + start11;
                            start2 = start11;
                        }
                    } else {
                        pSingle7 = pSingle8;
                        Matcher m_resultclean8 = this.p_resultclean.matcher(m523.group(1));
                        if (m_resultclean8.matches()) {
                            if (m_resultclean8.group(1) != null) {
                                out12 = m_resultclean8.group(1);
                                outLen9 = out12.length() + 1;
                            } else {
                                String out25 = m_resultclean8.group(2);
                                outLen9 = out25.length();
                                out12 = out25;
                            }
                            int start12 = m523.start(1) + (m523.group(1).length() - outLen9);
                            end = start12 + out12.length();
                            start2 = start12;
                            out22 = out12;
                        }
                    }
                }
                if (out22.length() > 0) {
                    Matcher mPre_road = pPre_road8.matcher(out22);
                    if (mPre_road.find()) {
                        if (mPre_road.group(2) == null) {
                            start2 += out22.length() - mPre_road.group(1).length();
                            out22 = mPre_road.group(1);
                        } else {
                            out22 = BuildConfig.FLAVOR;
                        }
                    }
                    Matcher mNot_road = pNot_road5.matcher(out22);
                    if (mNot_road.find()) {
                        if (mNot_road.group(2) == null || mNot_road.group(2).length() <= 0) {
                            out22 = BuildConfig.FLAVOR;
                        } else {
                            String out26 = out22.substring(mNot_road.group(1).length(), out22.length());
                            start2 += mNot_road.group(1).length();
                            out22 = out26;
                        }
                    }
                    if (out22.length() > 0) {
                        nn.add(Integer.valueOf(start2));
                        nn.add(Integer.valueOf(end));
                    }
                    str3 = source;
                    pPre_road5 = pPre_road8;
                    pNot_road4 = pNot_road5;
                    m283 = m284;
                    pCut3 = pCut2;
                    pPre_road6 = pPre_road7;
                    pSingle6 = pSingle7;
                } else {
                    str3 = source;
                    pPre_road5 = pPre_road8;
                    pNot_road4 = pNot_road5;
                    m283 = m284;
                    pCut3 = pCut2;
                    pPre_road6 = pPre_road7;
                    pSingle6 = pSingle7;
                }
            } else {
                m523 = m523;
                str3 = source;
                pPre_road5 = pPre_road8;
                pNot_road4 = pNot_road5;
                city = city2;
                m283 = m284;
                pPre_road6 = pPre_road7;
                pSingle6 = pSingle8;
            }
        }
        while (m_52sub.find()) {
            String out27 = BuildConfig.FLAVOR;
            if (!this.pRoad.matcher(m_52sub.group()).matches()) {
                if (m_52sub.group(5) == null) {
                    Matcher m_resultclean9 = this.p_resultclean.matcher(m_52sub.group(1));
                    if (m_resultclean9.matches()) {
                        if (m_resultclean9.group(1) != null) {
                            String out28 = m_resultclean9.group(1);
                            out10 = out28;
                            outLen7 = out28.length() + 1;
                        } else {
                            out10 = m_resultclean9.group(2);
                            outLen7 = out10.length();
                        }
                        int start13 = m_52sub.start(1) + (m_52sub.group(1).length() - outLen7);
                        end = out10.length() + start13;
                        out27 = out10;
                        start2 = start13;
                        pSingle5 = pSingle8;
                    } else {
                        pSingle5 = pSingle8;
                    }
                } else if (m_52sub.group(6) != null) {
                    Matcher m_resultclean10 = this.p_resultclean.matcher(m_52sub.group());
                    if (m_resultclean10.matches()) {
                        if (m_resultclean10.group(1) != null) {
                            out9 = m_resultclean10.group(1);
                            outLen6 = out9.length() + 1;
                        } else {
                            out9 = m_resultclean10.group(2);
                            outLen6 = out9.length();
                        }
                        int start14 = m_52sub.start() + (m_52sub.group().length() - outLen6);
                        start2 = start14;
                        out27 = out9;
                        end = out9.length() + start14;
                        pSingle5 = pSingle8;
                    } else {
                        pSingle5 = pSingle8;
                    }
                } else {
                    Matcher mCut2 = pCut3.matcher(m_52sub.group(5));
                    if (!mCut2.matches()) {
                        cut = BuildConfig.FLAVOR;
                    } else if (mCut2.group(1) != null) {
                        cut = mCut2.group(1);
                    } else {
                        cut = BuildConfig.FLAVOR;
                    }
                    String city5 = searCity(m_52sub.group(5).substring(cut.length(), m_52sub.group(5).length()), 2);
                    if (city5 != null) {
                        pSingle5 = pSingle8;
                        String city6 = String.valueOf(cut) + city5;
                        if (m_52sub.group(7) == null) {
                            if (m_52sub.group(4) != null) {
                                city6 = String.valueOf(m_52sub.group(4)) + city6;
                            }
                        } else if (m_52sub.group(4) != null) {
                            city6 = String.valueOf(m_52sub.group(4)) + m_52sub.group(5) + m_52sub.group(7);
                        } else {
                            city6 = String.valueOf(m_52sub.group(5)) + m_52sub.group(7);
                        }
                        Matcher m_resultclean11 = this.p_resultclean.matcher(String.valueOf(m_52sub.group(1)) + m_52sub.group(3) + city6);
                        if (m_resultclean11.matches()) {
                            if (m_resultclean11.group(1) != null) {
                                String out29 = m_resultclean11.group(1);
                                out6 = out29;
                                outLen2 = out29.length() + 1;
                            } else {
                                out6 = m_resultclean11.group(2);
                                outLen2 = out6.length();
                            }
                            int start15 = m_52sub.start(1) + ((String.valueOf(m_52sub.group(1)) + m_52sub.group(3) + city6).length() - outLen2);
                            end = out6.length() + start15;
                            out27 = out6;
                            start2 = start15;
                        }
                    } else if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(m_52sub.group(3)).lookingAt()) {
                        Matcher m_resultclean12 = this.p_resultclean.matcher(m_52sub.group());
                        if (m_resultclean12.matches()) {
                            if (m_resultclean12.group(1) != null) {
                                out27 = m_resultclean12.group(1);
                                outLen5 = out27.length() + 1;
                            } else {
                                out27 = m_resultclean12.group(2);
                                outLen5 = out27.length();
                            }
                            int start16 = m_52sub.start() + (m_52sub.group().length() - outLen5);
                            end = out27.length() + start16;
                            pSingle5 = pSingle8;
                            start2 = start16;
                        } else {
                            pSingle5 = pSingle8;
                        }
                    } else {
                        pSingle5 = pSingle8;
                        if (pSingle5.matcher(m_52sub.group(5)).matches()) {
                            Matcher m_resultclean13 = this.p_resultclean.matcher(m_52sub.group());
                            if (m_resultclean13.matches()) {
                                if (m_resultclean13.group(1) != null) {
                                    out8 = m_resultclean13.group(1);
                                    outLen4 = out8.length() + 1;
                                } else {
                                    out8 = m_resultclean13.group(2);
                                    outLen4 = out8.length();
                                }
                                int start17 = m_52sub.start() + (m_52sub.group().length() - outLen4);
                                end = out27.length() + start17;
                                start2 = start17;
                            }
                        } else {
                            Matcher m_resultclean14 = this.p_resultclean.matcher(m_52sub.group(1));
                            if (m_resultclean14.matches()) {
                                if (m_resultclean14.group(1) != null) {
                                    out7 = m_resultclean14.group(1);
                                    outLen3 = out7.length() + 1;
                                } else {
                                    out7 = m_resultclean14.group(2);
                                    outLen3 = out7.length();
                                }
                                int start18 = m_52sub.start(1) + (m_52sub.group(1).length() - outLen3);
                                end = start18 + out7.length();
                                start2 = start18;
                                out27 = out7;
                            }
                        }
                    }
                }
                if (out27.length() > 0) {
                    Matcher mPre_road2 = pPre_road8.matcher(out27);
                    if (mPre_road2.find()) {
                        if (mPre_road2.group(2) == null) {
                            start2 += out27.length() - mPre_road2.group(1).length();
                            out27 = mPre_road2.group(1);
                        } else {
                            out27 = BuildConfig.FLAVOR;
                        }
                    }
                    Matcher mNot_road2 = pNot_road5.matcher(out27);
                    if (mNot_road2.find()) {
                        if (mNot_road2.group(2) == null || mNot_road2.group(2).length() <= 0) {
                            out27 = BuildConfig.FLAVOR;
                        } else {
                            String out30 = out27.substring(mNot_road2.group(1).length(), out27.length());
                            start2 += mNot_road2.group(1).length();
                            out27 = out30;
                        }
                    }
                    if (out27.length() > 0) {
                        nn.add(Integer.valueOf(start2));
                        nn.add(Integer.valueOf(end));
                    }
                    pSingle4 = pSingle5;
                    pPre_road3 = pPre_road8;
                    pNot_road3 = pNot_road5;
                    m282 = m284;
                    m522 = m523;
                    pPre_road4 = pPre_road7;
                    str2 = source;
                } else {
                    pSingle4 = pSingle5;
                    pPre_road3 = pPre_road8;
                    pNot_road3 = pNot_road5;
                    m282 = m284;
                    m522 = m523;
                    pPre_road4 = pPre_road7;
                    str2 = source;
                }
            } else {
                pSingle4 = pSingle8;
                pPre_road3 = pPre_road8;
                pNot_road3 = pNot_road5;
                m282 = m284;
                m522 = m523;
                pPre_road4 = pPre_road7;
                str2 = source;
            }
        }
        while (m1346.find()) {
            int start19 = start2;
            int end3 = end;
            String out31 = out17;
            if (pPre_road7.matcher(m1346.group()).find()) {
                int head2 = m1346.start();
                String[] strArr = new String[8];
                this.match_index_2.clear();
                String[] buildings3 = searBuilding(m1346.group(), head2);
                if (buildings3 != null) {
                    int length_bui2 = buildings3.length;
                    Iterator<Integer> it = this.match_index_2.iterator();
                    int pr = 0;
                    while (true) {
                        if (pr >= length_bui2) {
                            p_big = pPre_road7;
                            pSingle2 = pSingle8;
                            break;
                        } else if (buildings3[pr] == null) {
                            p_big = pPre_road7;
                            pSingle2 = pSingle8;
                            break;
                        } else {
                            Matcher m_resultclean15 = this.p_resultclean.matcher(buildings3[pr]);
                            if (m_resultclean15.matches()) {
                                length_bui = length_bui2;
                                if (m_resultclean15.group(1) != null) {
                                    out4 = m_resultclean15.group(1);
                                    start5 = buildings3[pr].length() - (out4.length() + 1);
                                } else {
                                    out4 = m_resultclean15.group(2);
                                    start5 = buildings3[pr].length() - out4.length();
                                }
                                Matcher mNum = this.pNum.matcher(out4);
                                if (mNum.lookingAt()) {
                                    buildings2 = buildings3;
                                    pSingle3 = pSingle8;
                                    String out32 = out4.substring(mNum.group().length(), out4.length());
                                    start5 += mNum.group().length();
                                    out5 = out32;
                                } else {
                                    pSingle3 = pSingle8;
                                    buildings2 = buildings3;
                                    out5 = out4;
                                }
                                if (it.hasNext()) {
                                    int start20 = start5 + it.next().intValue();
                                    int end4 = start20 + out5.length();
                                    try {
                                        String temp = str4.substring(start20, end4);
                                        try {
                                            if (this.pDir.matcher(temp).lookingAt()) {
                                                out5 = temp;
                                            } else {
                                                Matcher mClean = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(temp);
                                                if (mClean.matches()) {
                                                    start20 += mClean.group(1).length();
                                                    out5 = out5.substring(mClean.group(1).length(), out5.length());
                                                }
                                            }
                                            if (!pBuilding.matcher(out5).matches()) {
                                                nn.add(Integer.valueOf(start20));
                                                nn.add(Integer.valueOf(end4));
                                            }
                                            end3 = end4;
                                            out31 = out5;
                                            start19 = start20;
                                        } catch (Exception e) {
                                            e = e;
                                            System.out.println(String.valueOf(start20) + "**" + end4);
                                            end3 = end4;
                                            start19 = start20;
                                            out31 = out5;
                                            pr++;
                                            length_bui2 = length_bui;
                                            pPre_road7 = pPre_road7;
                                            buildings3 = buildings2;
                                            pSingle8 = pSingle3;
                                        }
                                    } catch (Exception e2) {
                                        e = e2;
                                        System.out.println(String.valueOf(start20) + "**" + end4);
                                        end3 = end4;
                                        start19 = start20;
                                        out31 = out5;
                                        pr++;
                                        length_bui2 = length_bui;
                                        pPre_road7 = pPre_road7;
                                        buildings3 = buildings2;
                                        pSingle8 = pSingle3;
                                    }
                                } else {
                                    out31 = out5;
                                    start19 = start5;
                                }
                            } else {
                                pSingle3 = pSingle8;
                                buildings2 = buildings3;
                                length_bui = length_bui2;
                            }
                            pr++;
                            length_bui2 = length_bui;
                            pPre_road7 = pPre_road7;
                            buildings3 = buildings2;
                            pSingle8 = pSingle3;
                        }
                    }
                    end = end3;
                    out = out31;
                } else {
                    p_big = pPre_road7;
                    pSingle2 = pSingle8;
                    end = end3;
                    out = out31;
                }
                this.match_index_2.clear();
                String[] buildings4 = searSpot(m1346.group(), head2);
                if (buildings4 != null) {
                    int length_bui3 = buildings4.length;
                    Iterator<Integer> it2 = this.match_index_2.iterator();
                    int pr2 = 0;
                    while (true) {
                        if (pr2 >= length_bui3) {
                            break;
                        } else if (buildings4[pr2] == null) {
                            break;
                        } else {
                            Matcher m_resultclean16 = this.p_resultclean.matcher(buildings4[pr2]);
                            if (m_resultclean16.matches()) {
                                head = head2;
                                if (m_resultclean16.group(1) != null) {
                                    out2 = m_resultclean16.group(1);
                                    outLen = buildings4[pr2].length() - (out2.length() + 1);
                                } else {
                                    out2 = m_resultclean16.group(2);
                                    outLen = buildings4[pr2].length() - out2.length();
                                }
                                Matcher mNum2 = this.pNum.matcher(out2);
                                if (mNum2.lookingAt()) {
                                    buildings = buildings4;
                                    String out33 = out2.substring(mNum2.group().length(), out2.length());
                                    start4 = outLen + mNum2.group().length();
                                    out3 = out33;
                                } else {
                                    buildings = buildings4;
                                    start4 = outLen;
                                    out3 = out2;
                                }
                                if (it2.hasNext()) {
                                    int start21 = it2.next().intValue() + start4;
                                    end = start21 + out.length();
                                    try {
                                        String temp2 = str4.substring(start21, end);
                                        if (this.pDir.matcher(temp2).lookingAt()) {
                                            out = temp2;
                                        } else {
                                            Matcher mClean2 = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(temp2);
                                            if (mClean2.matches()) {
                                                start21 += mClean2.group(1).length();
                                                out = out.substring(mClean2.group(1).length(), out.length());
                                            }
                                        }
                                        if (!pBuilding.matcher(out).matches()) {
                                            nn.add(Integer.valueOf(start21));
                                            nn.add(Integer.valueOf(end));
                                        }
                                        start4 = start21;
                                    } catch (Exception e3) {
                                        start4 = start21;
                                        System.out.println(String.valueOf(start4) + "**" + end);
                                    }
                                }
                            } else {
                                buildings = buildings4;
                                head = head2;
                            }
                            pr2++;
                            str4 = source;
                            length_bui3 = length_bui3;
                            buildings4 = buildings;
                            head2 = head;
                        }
                    }
                    str = source;
                    start3 = start4;
                    m28 = m284;
                    m52 = m523;
                    pNot_road2 = pNot_road5;
                    pPre_road2 = pPre_road8;
                    pPre_road7 = p_big;
                    pSingle8 = pSingle2;
                } else {
                    str = source;
                    start3 = start4;
                    m28 = m284;
                    m52 = m523;
                    pNot_road2 = pNot_road5;
                    pPre_road2 = pPre_road8;
                    pPre_road7 = p_big;
                    pSingle8 = pSingle2;
                }
            } else {
                str = source;
                start3 = start19;
                end = end3;
                out = out31;
                m28 = m284;
                m52 = m523;
                pNot_road2 = pNot_road5;
                pPre_road2 = pPre_road8;
            }
        }
        int num = nn.size();
        int[] nn_new2 = new int[num];
        int i2 = 0;
        while (i2 < num) {
            nn_new2[i2] = nn.get(i2).intValue();
            i2++;
            m523 = m523;
        }
        if (num > 4) {
            int[] nn_new = new int[num];
            int t = 0;
            int i3 = 1;
            while (i3 < (num - 1) / 2) {
                for (int j = i3 + 1; j < (num + 1) / 2; j++) {
                    if (nn_new2[(i3 * 2) - 1] > nn_new2[(j * 2) - 1]) {
                        int i4 = (i3 * 2) - 1;
                        nn_new2[i4] = nn_new2[i4] + nn_new2[(j * 2) - 1];
                        nn_new2[(j * 2) - 1] = nn_new2[(i3 * 2) - 1] - nn_new2[(j * 2) - 1];
                        nn_new2[(i3 * 2) - 1] = nn_new2[(i3 * 2) - 1] - nn_new2[(j * 2) - 1];
                        int i5 = i3 * 2;
                        nn_new2[i5] = nn_new2[i5] + nn_new2[j * 2];
                        nn_new2[j * 2] = nn_new2[i3 * 2] - nn_new2[j * 2];
                        nn_new2[i3 * 2] = nn_new2[i3 * 2] - nn_new2[j * 2];
                    }
                }
                i3++;
                m284 = m284;
                m523 = m523;
                pNot_road5 = pNot_road5;
                pPre_road8 = pPre_road8;
            }
            int i6 = 1;
            while (i6 < (num + 1) / 2) {
                t++;
                nn_new[(t * 2) - 1] = nn_new2[(i6 * 2) - 1];
                nn_new[t * 2] = nn_new2[i6 * 2];
                int j2 = i6 + 1;
                while (true) {
                    if (j2 >= (num + 1) / 2) {
                        i = i6;
                        pPre_road = pPre_road8;
                        pNot_road = pNot_road5;
                        break;
                    }
                    pNot_road = pNot_road5;
                    if (nn_new2[i6 * 2] < nn_new2[(j2 * 2) - 1]) {
                        pPre_road = pPre_road8;
                        i = j2 - 1;
                        break;
                    }
                    nn_new2[i6 * 2] = max(nn_new2[i6 * 2], nn_new2[j2 * 2]);
                    nn_new[t * 2] = nn_new2[i6 * 2];
                    if (j2 == ((num + 1) / 2) - 1) {
                        i6 = j2;
                    }
                    j2++;
                    m523 = m523;
                    pNot_road5 = pNot_road;
                    pPre_road8 = pPre_road8;
                }
                i6 = i + 1;
                m523 = m523;
                pNot_road5 = pNot_road;
                pPre_road8 = pPre_road;
            }
            nn_new2[0] = t;
            nn_new[0] = t;
            return createAddressResultData(nn_new, str4);
        }
        nn_new2[0] = (num - 1) / 2;
        return createAddressResultData(nn_new2, str4);
    }

    private ArrayList<Match> createAddressResultData(int[] addrArray, String source) {
        if (addrArray.length == 0) {
            return null;
        }
        ArrayList<Match> matchedList = new ArrayList<>();
        int count = addrArray[0];
        for (int i = 1; i < (count * 2) + 1; i += 2) {
            Match mu = new Match();
            mu.setMatchedAddr(source.substring(addrArray[i], addrArray[i + 1]));
            mu.setStartPos(Integer.valueOf(addrArray[i]));
            mu.setEndPos(Integer.valueOf(addrArray[i + 1]));
            matchedList.add(mu);
        }
        return sortAndMergePosList(matchedList, source);
    }

    /* JADX INFO: Multiple debug info for r4v9 int: [D('s_right' java.lang.String), D('count' int)] */
    /* JADX INFO: Multiple debug info for r4v16 int: [D('count' int), D('mSingle' java.util.regex.Matcher)] */
    /* JADX INFO: Multiple debug info for r8v32 int: [D('count' int), D('mSingle' java.util.regex.Matcher)] */
    private String[] searSpot(String string, int head) {
        int position;
        String str;
        int length;
        int i;
        int length_bracket;
        String cut;
        String s_right;
        int count;
        String cut2;
        String cut3;
        int length2 = string.length();
        int head_0 = head;
        int i2 = 8;
        String[] results = new String[8];
        String str2 = string;
        Pattern pCut2 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle2 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city2 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        int full = str2.length();
        int count2 = 0;
        int index = 0;
        while (index < length2) {
            String str3 = str2.substring(index, length2);
            int head2 = head_0 + (full - str3.length());
            int length3 = length2 - index;
            int index2 = 0;
            int position2 = DicSearch.dicsearch(2, str3.toLowerCase(Locale.getDefault()));
            if (position2 == 0) {
                while (index2 < length3 && ((str3.charAt(index2) >= 'a' && str3.charAt(index2) <= 'z') || ((str3.charAt(index2) >= 'A' && str3.charAt(index2) <= 'Z') || (str3.charAt(index2) >= '0' && str3.charAt(index2) <= '9')))) {
                    index2++;
                }
                length = length3;
                str = str3;
                position = position2;
                i = 1;
            } else {
                String building = str3.substring(0, position2);
                String s_right2 = str3.substring(position2, str3.length());
                int length_bracket2 = searchBracket(s_right2);
                if (length_bracket2 > 0) {
                    length = length3;
                    building = String.valueOf(building) + s_right2.substring(0, length_bracket2);
                    s_right2 = s_right2.substring(length_bracket2, s_right2.length());
                } else {
                    length = length3;
                }
                Matcher m52s = this.p52s.matcher(s_right2);
                boolean lookingAt = m52s.lookingAt();
                String city = BuildConfig.FLAVOR;
                if (!lookingAt) {
                    cut = BuildConfig.FLAVOR;
                    length_bracket = length_bracket2;
                    str = str3;
                    position = position2;
                    Matcher m2s = this.p2s.matcher(s_right2);
                    if (!m2s.lookingAt()) {
                        s_right = s_right2;
                        results[count2] = building;
                        this.match_index_2.add(Integer.valueOf(head2));
                        count = count2 + 1;
                    } else if (m2s.group(3) == null) {
                        results[count2] = building;
                        this.match_index_2.add(Integer.valueOf(head2));
                        s_right = s_right2;
                        count = count2 + 1;
                    } else if (m2s.group(4) != null) {
                        results[count2] = String.valueOf(building) + m2s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        s_right = s_right2;
                        count = count2 + 1;
                    } else {
                        Matcher mCut = pCut2.matcher(m2s.group(3));
                        if (!mCut.matches()) {
                            cut2 = BuildConfig.FLAVOR;
                        } else if (mCut.group(1) != null) {
                            cut2 = mCut.group(1);
                        } else {
                            cut2 = BuildConfig.FLAVOR;
                        }
                        s_right = s_right2;
                        String city2 = searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2);
                        if (city2 != null) {
                            String city3 = String.valueOf(cut2) + city2;
                            if (m2s.group(6) == null) {
                                if (m2s.group(2) != null) {
                                    city3 = String.valueOf(m2s.group(2)) + city3;
                                }
                            } else if (m2s.group(2) == null) {
                                city3 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                            } else {
                                city3 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                            }
                            String city4 = String.valueOf(m2s.group(1)) + city3;
                            count = count2 + 1;
                            results[count2] = String.valueOf(building) + city4;
                            this.match_index_2.add(Integer.valueOf(head2));
                            city = city4;
                            cut = cut2;
                        } else if (pPre_city2.matcher(m2s.group(1)).matches()) {
                            count = count2 + 1;
                            city = city2;
                            results[count2] = String.valueOf(building) + m2s.group();
                            this.match_index_2.add(Integer.valueOf(head2));
                            cut = cut2;
                        } else {
                            city = city2;
                            if (pSingle2.matcher(m2s.group(3)).matches()) {
                                count = count2 + 1;
                                results[count2] = String.valueOf(building) + m2s.group();
                                this.match_index_2.add(Integer.valueOf(head2));
                                cut = cut2;
                            } else {
                                results[count2] = building;
                                this.match_index_2.add(Integer.valueOf(head2));
                                count = count2 + 1;
                                cut = cut2;
                            }
                        }
                    }
                } else if (m52s.group(6) == null) {
                    cut = BuildConfig.FLAVOR;
                    results[count2] = String.valueOf(building) + m52s.group();
                    this.match_index_2.add(Integer.valueOf(head2));
                    s_right = s_right2;
                    length_bracket = length_bracket2;
                    str = str3;
                    position = position2;
                    count = count2 + 1;
                } else {
                    cut = BuildConfig.FLAVOR;
                    if (m52s.group(7) != null) {
                        results[count2] = String.valueOf(building) + m52s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        s_right = s_right2;
                        length_bracket = length_bracket2;
                        str = str3;
                        position = position2;
                        count = count2 + 1;
                    } else {
                        Matcher mCut2 = pCut2.matcher(m52s.group(6));
                        if (!mCut2.matches()) {
                            cut3 = BuildConfig.FLAVOR;
                        } else if (mCut2.group(1) != null) {
                            cut3 = mCut2.group(1);
                        } else {
                            cut3 = BuildConfig.FLAVOR;
                        }
                        length_bracket = length_bracket2;
                        str = str3;
                        String city5 = searCity(m52s.group(6).substring(cut3.length(), m52s.group(6).length()), 2);
                        if (city5 != null) {
                            position = position2;
                            String city6 = String.valueOf(cut3) + city5;
                            if (m52s.group(8) == null) {
                                if (m52s.group(5) != null) {
                                    city6 = String.valueOf(m52s.group(5)) + city6;
                                }
                            } else if (m52s.group(5) == null) {
                                city6 = String.valueOf(m52s.group(6)) + m52s.group(8);
                            } else {
                                city6 = String.valueOf(m52s.group(5)) + m52s.group(6) + m52s.group(8);
                            }
                            results[count2] = String.valueOf(building) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city6;
                            this.match_index_2.add(Integer.valueOf(head2));
                            s_right = s_right2;
                            city = city6;
                            count = count2 + 1;
                            cut = cut3;
                        } else if (pPre_city2.matcher(m52s.group(4)).matches()) {
                            results[count2] = String.valueOf(building) + m52s.group();
                            this.match_index_2.add(Integer.valueOf(head2));
                            cut = cut3;
                            s_right = s_right2;
                            position = position2;
                            count = count2 + 1;
                            city = city5;
                        } else if (pSingle2.matcher(m52s.group(3)).matches()) {
                            results[count2] = String.valueOf(building) + m52s.group();
                            this.match_index_2.add(Integer.valueOf(head2));
                            cut = cut3;
                            s_right = s_right2;
                            city = city5;
                            position = position2;
                            count = count2 + 1;
                        } else {
                            StringBuilder sb = new StringBuilder(String.valueOf(building));
                            position = position2;
                            sb.append(m52s.group(1));
                            sb.append(m52s.group(2));
                            results[count2] = sb.toString();
                            this.match_index_2.add(Integer.valueOf(head2));
                            cut = cut3;
                            city = city5;
                            count = count2 + 1;
                            s_right = s_right2;
                        }
                    }
                }
                i = 1;
                index2 = (results[count - 1].length() + 0) - 1;
                count2 = count;
            }
            index = index2 + i;
            head_0 = head_0;
            length2 = length;
            str2 = str;
            i2 = 8;
        }
        if (count2 >= i2) {
            return results;
        }
        String[] re = new String[count2];
        for (int index3 = 0; index3 < count2; index3++) {
            re[index3] = results[index3];
        }
        return re;
    }

    private int max(int i, int j) {
        if (i > j) {
            return i;
        }
        return j;
    }

    public String[] searBuilding(String string, int head) {
        boolean flag;
        if (stanWri(string)) {
            flag = false;
        } else {
            flag = true;
        }
        return searBuilding_suf(string, BuildConfig.FLAVOR, 0, flag, head);
    }

    /* JADX INFO: Multiple debug info for r0v128 int: [D('count' int), D('mSingle' java.util.regex.Matcher)] */
    private String[] searBuilding_suf(String str, String sub_left, int left_state, boolean flag, int head) {
        int head2;
        String str2;
        int left_state2;
        String[] results_2;
        int index;
        String[] results_22;
        Matcher mLocation;
        String cut;
        String[] results_3;
        int left_state3;
        String sub_left2;
        String cut2;
        String city;
        String cut3;
        String city2;
        String sub3;
        String[] results = new String[8];
        String[] results_23 = new String[0];
        String[] results_32 = new String[0];
        String sub2 = BuildConfig.FLAVOR;
        String building = BuildConfig.FLAVOR;
        Matcher mLocation2 = this.pNot_1.matcher(str);
        if (mLocation2.lookingAt()) {
            Matcher mLocation3 = this.pNot_2.matcher(mLocation2.group(1));
            if (mLocation3.lookingAt()) {
                int n = mLocation3.group().length();
                head2 = head + n;
                str2 = str.substring(n, str.length());
            } else {
                head2 = head;
                str2 = str;
            }
        } else {
            head2 = head;
            str2 = str;
        }
        Matcher mLocation4 = this.pLocation.matcher(str2);
        if (mLocation4.find()) {
            String sub1 = mLocation4.group(1);
            Matcher mNo = this.pNo.matcher(sub1);
            if (sub1.length() <= 0 || !noBlank(sub1)) {
                String sub_left3 = mLocation4.group();
                String sub_right = str2.substring(sub_left3.length(), str2.length());
                if (noBlank(sub_right)) {
                    results_2 = searBuilding_suf(sub_right, sub_left3, 1, flag, head2 + (str2.length() - sub_right.length()));
                    left_state2 = 0;
                    results_32 = results_32;
                } else {
                    left_state2 = 0;
                    results_2 = results_23;
                    results_32 = results_32;
                }
            } else {
                if (!mNo.matches()) {
                    results_22 = results_23;
                    cut = sub1;
                    mLocation = mLocation4;
                } else if (mLocation4.group(3) != null) {
                    results_22 = results_23;
                    cut = sub1;
                    mLocation = mLocation4;
                } else {
                    String sub_left4 = mLocation4.group();
                    String sub_right2 = str2.substring(sub_left4.length(), str2.length());
                    if (noBlank(sub_right2)) {
                        results_2 = searBuilding_suf(sub_right2, sub_left4, 1, flag, head2 + (str2.length() - sub_right2.length()));
                        left_state2 = 0;
                    } else {
                        results_2 = results_23;
                        left_state2 = 0;
                    }
                }
                Matcher mComma = this.pComma.matcher(cut);
                if (mComma.find()) {
                    sub2 = mComma.group(1);
                    if (sub2 != null && noBlank(sub2) && divStr(sub2).length <= 4) {
                        building = String.valueOf(sub2) + mLocation.group(2);
                        this.match_index_2.add(Integer.valueOf(head2 + mComma.start(1)));
                    }
                    if (building.length() == 0) {
                        if (flag) {
                            String sub1_temp = cut;
                            boolean sub1_undone = true;
                            while (sub1_undone) {
                                Matcher mPre_uni = this.pPre_uni.matcher(sub1_temp);
                                if (mPre_uni.find()) {
                                    sub2 = mPre_uni.group(2);
                                    if (sub2 == null || !noBlank(sub2)) {
                                        sub1_undone = false;
                                        sub1_temp = sub1_temp;
                                        mComma = mComma;
                                    } else if (divStr(sub2).length <= 4) {
                                        building = String.valueOf(sub2) + mLocation.group(2);
                                        this.match_index_2.add(Integer.valueOf(head2 + (cut.length() - sub2.length())));
                                        sub1_undone = false;
                                        sub1_temp = sub1_temp;
                                        mComma = mComma;
                                    } else {
                                        sub1_temp = sub2;
                                        sub1_undone = sub1_undone;
                                        mComma = mComma;
                                    }
                                } else {
                                    sub1_undone = false;
                                    sub1_temp = sub1_temp;
                                    mComma = mComma;
                                }
                            }
                            if (building.length() == 0) {
                                String[] temp = divStr(cut);
                                int length = temp.length;
                                if (length > 4) {
                                    building = String.valueOf(temp[length - 4]) + temp[length - 3] + temp[length - 2] + temp[length - 1] + mLocation.group(2);
                                    this.match_index_2.add(Integer.valueOf(head2 + (cut.length() - (building.length() - mLocation.group(2).length()))));
                                    results_3 = results_32;
                                } else {
                                    if (length > 0) {
                                        building = String.valueOf(cut) + mLocation.group(2);
                                    }
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    results_3 = results_32;
                                }
                            } else {
                                results_3 = results_32;
                            }
                        }
                    }
                    results_3 = results_32;
                } else {
                    String sub1_temp2 = cut;
                    boolean sub1_undone2 = true;
                    while (sub1_undone2) {
                        Matcher mPre_uni2 = this.pPre_uni.matcher(sub1_temp2);
                        if (mPre_uni2.find()) {
                            sub2 = mPre_uni2.group(2);
                            if (sub2 == null || !noBlank(sub2)) {
                                sub1_undone2 = false;
                                results_32 = results_32;
                            } else if (divStr(sub2).length <= 4) {
                                building = String.valueOf(sub2) + mLocation.group(2);
                                this.match_index_2.add(Integer.valueOf(head2 + (cut.length() - sub2.length())));
                                sub1_undone2 = false;
                                results_32 = results_32;
                            } else {
                                sub1_temp2 = sub2;
                                sub1_undone2 = sub1_undone2;
                                results_32 = results_32;
                            }
                        } else {
                            sub1_undone2 = false;
                            results_32 = results_32;
                        }
                    }
                    if (building.length() == 0) {
                        String[] temp2 = divStr(cut);
                        int length2 = temp2.length;
                        if (length2 > 4) {
                            building = String.valueOf(temp2[length2 - 4]) + temp2[length2 - 3] + temp2[length2 - 2] + temp2[length2 - 1] + mLocation.group(2);
                            results_3 = results_32;
                            this.match_index_2.add(Integer.valueOf(head2 + (cut.length() - (building.length() - mLocation.group(2).length()))));
                        } else {
                            results_3 = results_32;
                            if (length2 > 0) {
                                building = String.valueOf(cut) + mLocation.group(2);
                            }
                            this.match_index_2.add(Integer.valueOf(head2));
                        }
                    } else {
                        results_3 = results_32;
                    }
                }
                if (building.length() == 0 && mLocation.group(3) != null) {
                    String building2 = mLocation.group(2);
                    this.match_index_2.add(Integer.valueOf(head2 + mLocation.group(1).length()));
                    building = building2;
                }
                if (building.length() > 0) {
                    String sub22 = mLocation.group();
                    String sub32 = sub22.substring(0, sub22.length() > building.length() ? sub22.length() - building.length() : 0);
                    if (left_state == 1) {
                        sub32 = String.valueOf(sub_left) + sub32;
                    }
                    if (noBlank(sub32)) {
                        left_state3 = 2;
                        results_3 = searBuilding_dic(sub32, head2 - sub_left.length());
                        sub_left2 = BuildConfig.FLAVOR;
                    } else {
                        sub_left2 = sub_left;
                        left_state3 = left_state;
                    }
                    String sub33 = str2.substring(sub22.length(), str2.length());
                    if (noBlank(sub33)) {
                        Matcher m52s = this.p52s.matcher(sub33);
                        if (!m52s.lookingAt()) {
                            Matcher m2s = this.p2s.matcher(sub33);
                            if (!m2s.lookingAt()) {
                                left_state2 = 0 + 1;
                                results[0] = building;
                                results_2 = searBuilding_suf(sub33, sub_left2, left_state3, flag, head2 + (str2.length() - sub33.length()));
                                results_32 = results_3;
                            } else if (m2s.group(3) == null) {
                                left_state2 = 0 + 1;
                                results[0] = building;
                                if (noBlank(sub33)) {
                                    results_2 = searBuilding_suf(sub33, sub_left2, left_state3, flag, head2 + (str2.length() - sub33.length()));
                                    results_32 = results_3;
                                } else {
                                    results_2 = results_22;
                                    results_32 = results_3;
                                }
                            } else if (m2s.group(4) != null) {
                                left_state2 = 0 + 1;
                                results[0] = String.valueOf(building) + m2s.group();
                                String sub34 = sub33.substring(m2s.group().length(), sub33.length());
                                if (noBlank(sub34)) {
                                    results_2 = searBuilding_suf(sub34, sub_left2, left_state3, flag, head2 + (str2.length() - sub34.length()));
                                    results_32 = results_3;
                                } else {
                                    results_2 = results_22;
                                    results_32 = results_3;
                                }
                            } else {
                                Matcher mCut = this.pCut.matcher(m2s.group(3));
                                if (mCut.matches()) {
                                    cut2 = mCut.group(1) != null ? mCut.group(1) : BuildConfig.FLAVOR;
                                } else {
                                    cut2 = BuildConfig.FLAVOR;
                                }
                                String city3 = searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2);
                                if (city3 != null) {
                                    String city4 = String.valueOf(cut2) + city3;
                                    if (m2s.group(6) == null) {
                                        if (m2s.group(2) != null) {
                                            city4 = String.valueOf(m2s.group(2)) + city4;
                                        }
                                    } else if (m2s.group(2) == null) {
                                        city4 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                                    } else {
                                        city4 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                    }
                                    city = String.valueOf(m2s.group(1)) + city4;
                                } else if (this.pPre_city.matcher(m2s.group(1)).lookingAt()) {
                                    city = m2s.group();
                                } else {
                                    city = this.pSingle.matcher(m2s.group(3)).matches() ? m2s.group() : BuildConfig.FLAVOR;
                                }
                                int count = 0 + 1;
                                results[0] = String.valueOf(building) + city;
                                String sub35 = sub33.substring(city.length(), sub33.length());
                                if (noBlank(sub35)) {
                                    results_2 = searBuilding_suf(sub35, sub_left2, left_state3, flag, head2 + (str2.length() - sub35.length()));
                                    results_32 = results_3;
                                    left_state2 = count;
                                } else {
                                    results_2 = results_22;
                                    results_32 = results_3;
                                    left_state2 = count;
                                }
                            }
                        } else if (m52s.group(6) == null) {
                            left_state2 = 0 + 1;
                            results[0] = String.valueOf(building) + m52s.group();
                            String sub36 = sub33.substring(m52s.group().length(), sub33.length());
                            if (noBlank(sub36)) {
                                results_2 = searBuilding_suf(sub36, sub_left2, left_state3, flag, head2 + sub22.length() + m52s.group().length());
                                results_32 = results_3;
                            } else {
                                results_2 = results_22;
                                results_32 = results_3;
                            }
                        } else if (m52s.group(7) != null) {
                            left_state2 = 0 + 1;
                            results[0] = String.valueOf(building) + m52s.group();
                            String sub37 = sub33.substring(m52s.group().length(), sub33.length());
                            if (noBlank(sub37)) {
                                results_2 = searBuilding_suf(sub37, sub_left2, left_state3, flag, head2 + sub22.length() + m52s.group().length());
                                results_32 = results_3;
                            } else {
                                results_2 = results_22;
                                results_32 = results_3;
                            }
                        } else {
                            Matcher mCut2 = this.pCut.matcher(m52s.group(6));
                            if (mCut2.matches()) {
                                cut3 = mCut2.group(1) != null ? mCut2.group(1) : BuildConfig.FLAVOR;
                            } else {
                                cut3 = BuildConfig.FLAVOR;
                            }
                            String city5 = searCity(m52s.group(6).substring(cut3.length(), m52s.group(6).length()), 2);
                            if (city5 == null) {
                                if (this.pPre_city.matcher(m52s.group(4)).lookingAt()) {
                                    results[0] = String.valueOf(building) + m52s.group();
                                    left_state2 = 0 + 1;
                                    sub3 = sub33.substring(m52s.group().length(), sub33.length());
                                } else if (this.pSingle.matcher(m52s.group(3)).matches()) {
                                    results[0] = String.valueOf(building) + m52s.group();
                                    sub3 = sub33.substring(m52s.group().length(), sub33.length());
                                    left_state2 = 0 + 1;
                                } else {
                                    results[0] = String.valueOf(building) + m52s.group(1) + m52s.group(2);
                                    left_state2 = 0 + 1;
                                    sub3 = sub33.substring(m52s.group(1).length() + m52s.group(2).length(), sub33.length());
                                }
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left2, left_state3, flag, head2 + (str2.length() - sub3.length()));
                                    results_32 = results_3;
                                } else {
                                    results_2 = results_22;
                                    results_32 = results_3;
                                }
                            } else {
                                String city6 = String.valueOf(cut3) + city5;
                                if (m52s.group(8) == null) {
                                    city2 = m52s.group(5) != null ? String.valueOf(m52s.group(5)) + city6 : city6;
                                } else if (m52s.group(5) == null) {
                                    city2 = String.valueOf(m52s.group(6)) + m52s.group(8);
                                } else {
                                    city2 = String.valueOf(m52s.group(5)) + m52s.group(6) + m52s.group(8);
                                }
                                int count2 = 0 + 1;
                                results[0] = String.valueOf(building) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city2;
                                String sub38 = sub33.substring(m52s.group(1).length() + m52s.group(2).length() + m52s.group(4).length() + city2.length(), sub33.length());
                                if (noBlank(sub38)) {
                                    results_2 = searBuilding_suf(sub38, sub_left2, left_state3, flag, head2 + (str2.length() - sub38.length()));
                                    left_state2 = count2;
                                    results_32 = results_3;
                                } else {
                                    left_state2 = count2;
                                    results_2 = results_22;
                                    results_32 = results_3;
                                }
                            }
                        }
                    } else {
                        results[0] = building;
                        left_state2 = 0 + 1;
                        results_2 = results_22;
                        results_32 = results_3;
                    }
                } else {
                    String sub_left5 = mLocation.group();
                    String sub_right3 = str2.substring(sub_left5.length(), str2.length());
                    if (noBlank(sub_right3)) {
                        results_2 = searBuilding_suf(sub_right3, sub_left5, 1, flag, head2 + (str2.length() - sub_right3.length()));
                        left_state2 = 0;
                        results_32 = results_3;
                    } else {
                        left_state2 = 0;
                        results_2 = results_22;
                        results_32 = results_3;
                    }
                }
            }
        } else {
            if (left_state == 1) {
                str2 = String.valueOf(sub_left) + str2;
            }
            results_2 = searBuilding_dic(str2, head2 - sub_left.length());
            left_state2 = 0;
            results_32 = results_32;
        }
        if (results_32.length > 0) {
            int index2 = 0;
            while (index2 < results_32.length) {
                results[left_state2] = results_32[index2];
                index2++;
                left_state2++;
            }
        }
        if (results_2.length > 0) {
            int index3 = 0;
            while (index3 < results_2.length) {
                results[left_state2] = results_2[index3];
                index3++;
                left_state2++;
            }
            index = left_state2;
        } else {
            index = left_state2;
        }
        if (index >= 8) {
            return results;
        }
        String[] re = new String[index];
        for (int index4 = 0; index4 < index; index4++) {
            re[index4] = results[index4];
        }
        return re;
    }

    /* JADX INFO: Multiple debug info for r12v5 'pPre_city'  java.util.regex.Pattern: [D('position' int), D('pPre_city' java.util.regex.Pattern)] */
    /* JADX INFO: Multiple debug info for r1v26 int: [D('count' int), D('mSingle' java.util.regex.Matcher)] */
    private String[] searBuilding_dic(String string, int head) {
        int position;
        int length;
        Pattern pPre_city2;
        Pattern pSingle2;
        String building;
        String string2;
        String string3;
        int length2;
        int index;
        String str;
        int length_bracket;
        String s_right;
        String city;
        String cut;
        String cut2;
        int length3 = string.length();
        int head_0 = head;
        int i = 8;
        String[] results = new String[8];
        String str2 = string;
        Pattern pPre_building = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])((?i)(in|at|from|near|to|reach))\\b(\\s+(?i)the\\b)?(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+)?");
        Pattern pCut2 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle3 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city3 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        int full = str2.length();
        boolean flag = true;
        String s_right2 = BuildConfig.FLAVOR;
        int count = 0;
        int index2 = 0;
        String str3 = string;
        while (index2 < length3) {
            String str4 = str2.substring(index2, length3);
            int head2 = head_0 + (full - str4.length());
            int length4 = length3 - index2;
            int index3 = 0;
            String s_left = str3.substring(0, str3.length() - length4);
            int position2 = DicSearch.dicsearch(1, str4.toLowerCase(Locale.getDefault()));
            if (position2 == 0) {
                while (index3 < length4 && ((str4.charAt(index3) >= 'a' && str4.charAt(index3) <= 'z') || ((str4.charAt(index3) >= 'A' && str4.charAt(index3) <= 'Z') || (str4.charAt(index3) >= '0' && str4.charAt(index3) <= '9')))) {
                    index3++;
                }
                string3 = str3;
                string2 = str4;
                position = position2;
                pSingle2 = pSingle3;
                building = s_right2;
                pPre_city2 = pPre_city3;
                length = length4;
                length2 = 1;
            } else {
                String building2 = str4.substring(0, position2);
                String s_right3 = str4.substring(position2, str4.length());
                int length_bracket2 = searchBracket(s_right3);
                if (length_bracket2 > 0) {
                    length = length4;
                    building2 = String.valueOf(building2) + s_right3.substring(0, length_bracket2);
                    s_right3 = s_right3.substring(length_bracket2, s_right3.length());
                } else {
                    length = length4;
                }
                Matcher m52s = this.p52s.matcher(s_right3);
                if (!m52s.lookingAt()) {
                    index = 0;
                    length_bracket = length_bracket2;
                    str = str4;
                    position = position2;
                    pPre_city2 = pPre_city3;
                    Matcher m2s = this.p2s.matcher(s_right3);
                    if (!m2s.lookingAt()) {
                        s_right = s_right3;
                        pSingle2 = pSingle3;
                        if (pPre_building.matcher(s_left).matches()) {
                            results[count] = building2;
                            this.match_index_2.add(Integer.valueOf(head2));
                            city = BuildConfig.FLAVOR;
                            count++;
                        } else {
                            flag = false;
                            city = BuildConfig.FLAVOR;
                        }
                    } else if (m2s.group(3) == null) {
                        if (pPre_building.matcher(s_left).matches()) {
                            results[count] = building2;
                            this.match_index_2.add(Integer.valueOf(head2));
                            s_right = s_right3;
                            count++;
                            pSingle2 = pSingle3;
                            city = BuildConfig.FLAVOR;
                        } else {
                            flag = false;
                            city = BuildConfig.FLAVOR;
                            s_right = s_right3;
                            pSingle2 = pSingle3;
                        }
                    } else if (m2s.group(4) != null) {
                        results[count] = String.valueOf(building2) + m2s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        city = BuildConfig.FLAVOR;
                        s_right = s_right3;
                        count++;
                        pSingle2 = pSingle3;
                    } else {
                        Matcher mCut = pCut2.matcher(m2s.group(3));
                        if (!mCut.matches()) {
                            cut = BuildConfig.FLAVOR;
                        } else if (mCut.group(1) != null) {
                            cut = mCut.group(1);
                        } else {
                            cut = BuildConfig.FLAVOR;
                        }
                        String city2 = searCity(m2s.group(3).substring(cut.length(), m2s.group(3).length()), 2);
                        if (city2 != null) {
                            String city3 = String.valueOf(cut) + city2;
                            if (m2s.group(6) == null) {
                                if (m2s.group(2) != null) {
                                    city3 = String.valueOf(m2s.group(2)) + city3;
                                }
                            } else if (m2s.group(2) == null) {
                                city3 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                            } else {
                                city3 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                            }
                            String city4 = String.valueOf(m2s.group(1)) + city3;
                            s_right = s_right3;
                            results[count] = String.valueOf(building2) + city4;
                            this.match_index_2.add(Integer.valueOf(head2));
                            count++;
                            pSingle2 = pSingle3;
                            city = city4;
                        } else {
                            s_right = s_right3;
                            if (pPre_city2.matcher(m2s.group(1)).matches()) {
                                results[count] = String.valueOf(building2) + m2s.group();
                                this.match_index_2.add(Integer.valueOf(head2));
                                count++;
                                pSingle2 = pSingle3;
                                city = city2;
                            } else {
                                pSingle2 = pSingle3;
                                if (pSingle2.matcher(m2s.group(3)).matches()) {
                                    results[count] = String.valueOf(building2) + m2s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    count++;
                                    city = city2;
                                } else if (pPre_building.matcher(s_left).matches()) {
                                    results[count] = building2;
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    count++;
                                    city = city2;
                                } else {
                                    flag = false;
                                    city = city2;
                                }
                            }
                        }
                    }
                } else if (m52s.group(6) == null) {
                    length_bracket = length_bracket2;
                    results[count] = String.valueOf(building2) + m52s.group();
                    this.match_index_2.add(Integer.valueOf(head2));
                    city = BuildConfig.FLAVOR;
                    index = 0;
                    str = str4;
                    position = position2;
                    pSingle2 = pSingle3;
                    pPre_city2 = pPre_city3;
                    count++;
                    s_right = s_right3;
                } else {
                    length_bracket = length_bracket2;
                    if (m52s.group(7) != null) {
                        results[count] = String.valueOf(building2) + m52s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        city = BuildConfig.FLAVOR;
                        index = 0;
                        str = str4;
                        position = position2;
                        pSingle2 = pSingle3;
                        pPre_city2 = pPre_city3;
                        count++;
                        s_right = s_right3;
                    } else {
                        Matcher mCut2 = pCut2.matcher(m52s.group(6));
                        if (!mCut2.matches()) {
                            cut2 = BuildConfig.FLAVOR;
                        } else if (mCut2.group(1) != null) {
                            cut2 = mCut2.group(1);
                        } else {
                            cut2 = BuildConfig.FLAVOR;
                        }
                        position = position2;
                        str = str4;
                        city = searCity(m52s.group(6).substring(cut2.length(), m52s.group(6).length()), 2);
                        if (city == null) {
                            pPre_city2 = pPre_city3;
                            if (pPre_city2.matcher(m52s.group(4)).matches()) {
                                index = 0;
                                results[count] = String.valueOf(building2) + m52s.group();
                                this.match_index_2.add(Integer.valueOf(head2));
                                s_right = s_right3;
                                pSingle2 = pSingle3;
                                count++;
                            } else {
                                index = 0;
                                if (pSingle3.matcher(m52s.group(3)).matches()) {
                                    results[count] = String.valueOf(building2) + m52s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    pSingle2 = pSingle3;
                                    count++;
                                    s_right = s_right3;
                                } else {
                                    results[count] = String.valueOf(building2) + m52s.group(1) + m52s.group(2);
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    count++;
                                    pSingle2 = pSingle3;
                                    s_right = s_right3;
                                }
                            }
                        } else {
                            index = 0;
                            pPre_city2 = pPre_city3;
                            String city5 = String.valueOf(cut2) + city;
                            if (m52s.group(8) == null) {
                                if (m52s.group(5) != null) {
                                    city5 = String.valueOf(m52s.group(5)) + city5;
                                }
                            } else if (m52s.group(5) == null) {
                                city5 = String.valueOf(m52s.group(6)) + m52s.group(8);
                            } else {
                                city5 = String.valueOf(m52s.group(5)) + m52s.group(6) + m52s.group(8);
                            }
                            results[count] = String.valueOf(building2) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city5;
                            this.match_index_2.add(Integer.valueOf(head2));
                            s_right = s_right3;
                            count++;
                            pSingle2 = pSingle3;
                            city = city5;
                        }
                    }
                }
                if (flag) {
                    index3 = (index + results[count - 1].length()) - 1;
                    string2 = str;
                    string3 = string2.substring(results[count - 1].length(), str.length());
                    length2 = 1;
                    building = s_right;
                } else {
                    string2 = str;
                    length2 = 1;
                    index3 = (index + building2.length()) - 1;
                    string3 = string2.substring(building2.length(), string2.length());
                    flag = true;
                    building = s_right;
                }
            }
            index2 = index3 + length2;
            pSingle3 = pSingle2;
            pPre_city3 = pPre_city2;
            length3 = length;
            str2 = string2;
            s_right2 = building;
            i = 8;
            str3 = string3;
            head_0 = head_0;
        }
        if (count >= i) {
            return results;
        }
        String[] re = new String[count];
        for (int index4 = 0; index4 < count; index4++) {
            re[index4] = results[index4];
        }
        return re;
    }

    private boolean noBlank(String str) {
        int n = str.length();
        String str2 = str.toLowerCase(Locale.getDefault());
        boolean flag = true;
        int index = 0;
        while (flag && index < n) {
            if ((str2.charAt(index) <= 'z' && str2.charAt(index) >= 'a') || (str2.charAt(index) <= '9' && str2.charAt(index) >= '0')) {
                flag = false;
            }
            index++;
        }
        return !flag;
    }

    private String[] divStr(String str) {
        String[] strs = new String[TpCommandConstant.VOLUME_FLICK_THRESHOLD_MAX];
        int length = str.length();
        int pr = 0;
        strs[0] = BuildConfig.FLAVOR;
        for (int index = 0; index < length; index++) {
            char letter = str.charAt(index);
            if ((letter <= 'z' && letter >= 'a') || ((letter <= 'Z' && letter >= 'A') || (letter <= '9' && letter >= '0'))) {
                strs[pr] = String.valueOf(strs[pr]) + letter;
            } else if (strs[pr].length() > 0) {
                strs[pr] = String.valueOf(strs[pr]) + letter;
                pr++;
                strs[pr] = BuildConfig.FLAVOR;
            } else if (pr > 0) {
                int i = pr - 1;
                strs[i] = String.valueOf(strs[i]) + letter;
            }
        }
        if (strs[pr].length() > 0) {
            pr++;
        }
        if (pr >= 150) {
            return strs;
        }
        String[] re = new String[pr];
        for (int index2 = 0; index2 < pr; index2++) {
            re[index2] = strs[index2];
        }
        return re;
    }

    private boolean stanWri(String str) {
        String[] strs = divStr(str);
        int length = strs.length;
        boolean flag = true;
        int index = 0;
        while (flag && index < length) {
            int length_2 = strs[index].length();
            int index_2 = 1;
            while (flag && index_2 < length_2) {
                char letter = strs[index].charAt(index_2);
                if (letter <= 'Z' && letter >= 'A') {
                    flag = false;
                }
                index_2++;
            }
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

    public String searCity(String string, int mode) {
        int length = string.length();
        String str = string;
        Matcher mCity = Pattern.compile("([\\s\\S]*(?i)(town|city|county)\\b)(?:.*)").matcher(str);
        if (mode == 1) {
            if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
                return str;
            }
            int index = 0;
            while (index < length) {
                str = str.substring(index, length);
                length -= index;
                int index2 = 0;
                if (DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault())) != 0) {
                    return str;
                }
                while (index2 < length && ((str.charAt(index2) >= 'a' && str.charAt(index2) <= 'z') || ((str.charAt(index2) >= 'A' && str.charAt(index2) <= 'Z') || (str.charAt(index2) >= '0' && str.charAt(index2) <= '9')))) {
                    index2++;
                }
                index = index2 + 1;
            }
            return null;
        } else if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
            return mCity.group(1);
        } else {
            int position = DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault()));
            if (position <= 0) {
                return null;
            }
            Matcher mCity2 = Pattern.compile("(\\s+(?i)(town|city|county))\\b.*").matcher(str.substring(position, length));
            if (!mCity2.matches()) {
                return str.substring(0, position);
            }
            return String.valueOf(str.substring(0, position)) + mCity2.group(1);
        }
    }

    public int searchBracket(String str) {
        Matcher mBracket = Pattern.compile("(\\s*.?\\s*)\\)").matcher(str);
        if (mBracket.lookingAt()) {
            return mBracket.group().length();
        }
        return 0;
    }

    public String noShut(String str) {
        Matcher mShut = Pattern.compile("\\s*#").matcher(str);
        if (mShut.lookingAt()) {
            return str.substring(mShut.group().length(), str.length());
        }
        return str;
    }

    private ArrayList<Match> sortAndMergePosList(ArrayList<Match> posList, String sourceTxt) {
        if (posList.isEmpty()) {
            return null;
        }
        Collections.sort(posList, new Comparator<Match>() {
            /* class com.huawei.g11n.tmr.address.SerEn.AnonymousClass1 */

            public int compare(Match p1, Match p2) {
                if (p1.getStartPos().compareTo(p2.getStartPos()) == 0) {
                    return p1.getEndPos().compareTo(p2.getEndPos());
                }
                return p1.getStartPos().compareTo(p2.getStartPos());
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
}
