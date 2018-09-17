package com.huawei.g11n.tmr.address;

import com.huawei.g11n.tmr.address.jni.DicSearch;
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
    ArrayList<Integer> match_index_2 = new ArrayList();
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

    public ArrayList<Match> search(String source) {
        Matcher m_resultclean;
        int outLen;
        Matcher mCut;
        Matcher mPre_road;
        Matcher mNot_road;
        int i;
        ArrayList<Integer> nn = new ArrayList();
        Pattern p_big = Pattern.compile("[A-Z0-9]");
        String city = "";
        String cut = "";
        Pattern pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(?:uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_road = Pattern.compile("(?i)(?<![a-z])(?:(?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+)((?:[\\s\\S]+?)(?:(?<![a-z])((?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+))?" + this.road_suf + "(?![a-zA-Z])[\\s\\S]*)");
        Pattern pNot_road = Pattern.compile("(?i)((?<![a-zA-Z])(?:a|what|which|whose|i|you|this|that|my|his|her|out|their|its)\\s+)([\\s\\S]+)?" + this.road_suf + "(?![a-zA-Z])");
        Pattern pBuilding = Pattern.compile("(?:[^0-9a-zA-Z]*|\\s*(?:(?i)the|this|a|that)\\s*)(?:" + this.location + ")[^0-9a-zA-Z]*");
        int start = 0;
        int end = 0;
        String out = "";
        nn.add(Integer.valueOf(0));
        Matcher m52 = this.p52.matcher(source);
        Matcher m28 = this.p28.matcher(source);
        Matcher m1346 = this.p1346.matcher(source);
        Matcher m_52sub = this.p52_sub.matcher(source);
        boolean noBox = true;
        this.match_index_2.clear();
        while (m28.find()) {
            if (m28.group(1) != null) {
                if (m28.group(4) == null) {
                    if (m28.group(2) == null) {
                        city = searCity(m28.group(3), 1);
                        if (city == null) {
                            city = "";
                        }
                        m_resultclean = this.p_resultclean.matcher(new StringBuilder(String.valueOf(city)).append(m28.group(5)).append(m28.group(6)).toString());
                    } else {
                        m_resultclean = this.p_resultclean.matcher(m28.group());
                    }
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m28.start(5) + ((m28.group(5) + m28.group(6)).length() - outLen);
                        end = start + out.length();
                        nn.add(Integer.valueOf(start));
                        nn.add(Integer.valueOf(end));
                        if (m28.group(2) != null) {
                            noBox = false;
                        }
                    }
                } else {
                    m_resultclean = this.p_resultclean.matcher(m28.group());
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m28.start() + (m28.group().length() - outLen);
                        end = start + out.length();
                        nn.add(Integer.valueOf(start));
                        nn.add(Integer.valueOf(end));
                        if (m28.group(2) != null) {
                            noBox = false;
                        }
                    }
                }
            } else if (!this.pCode_a.matcher(m28.group()).find()) {
                start = m28.start();
                end = start + m28.group().length();
                nn.add(Integer.valueOf(start));
                nn.add(Integer.valueOf(end));
            } else if (m28.group(6).indexOf(45) != -1) {
                start = m28.start(6);
                end = start + m28.group(6).length();
                nn.add(Integer.valueOf(start));
                nn.add(Integer.valueOf(end));
            } else if (m28.group(5) != null && m28.group(5).length() > 0) {
                start = m28.start(6);
                end = start + m28.group(6).length();
                nn.add(Integer.valueOf(start));
                nn.add(Integer.valueOf(end));
            }
        }
        if (noBox) {
            Matcher m_box = this.p_box.matcher(source);
            while (m_box.find()) {
                start = m_box.start();
                end = start + m_box.group().length();
                nn.add(Integer.valueOf(start));
                nn.add(Integer.valueOf(end));
            }
        }
        while (m52.find()) {
            out = "";
            if (!this.pRoad.matcher(m52.group()).matches()) {
                if (m52.group(5) == null) {
                    m_resultclean = this.p_resultclean.matcher(m52.group(1));
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m52.start(1) + (m52.group(1).length() - outLen);
                        end = start + out.length();
                    }
                } else if (m52.group(6) == null) {
                    mCut = pCut.matcher(m52.group(5));
                    if (!mCut.matches()) {
                        cut = "";
                    } else if (mCut.group(1) == null) {
                        cut = "";
                    } else {
                        cut = mCut.group(1);
                    }
                    city = searCity(m52.group(5).substring(cut.length(), m52.group(5).length()), 2);
                    if (city != null) {
                        city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                        if (m52.group(7) != null) {
                            if (m52.group(4) == null) {
                                city = m52.group(5) + m52.group(7);
                            } else {
                                city = m52.group(4) + m52.group(5) + m52.group(7);
                            }
                        } else if (m52.group(4) != null) {
                            city = m52.group(4) + city;
                        }
                        m_resultclean = this.p_resultclean.matcher(m52.group(1) + m52.group(3) + city);
                        if (m_resultclean.matches()) {
                            if (m_resultclean.group(1) == null) {
                                out = m_resultclean.group(2);
                                outLen = out.length();
                            } else {
                                out = m_resultclean.group(1);
                                outLen = out.length() + 1;
                            }
                            start = m52.start(1) + ((m52.group(1) + m52.group(3) + city).length() - outLen);
                            end = start + out.length();
                        }
                    } else {
                        if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(m52.group(3)).lookingAt()) {
                            m_resultclean = this.p_resultclean.matcher(m52.group());
                            if (m_resultclean.matches()) {
                                if (m_resultclean.group(1) == null) {
                                    out = m_resultclean.group(2);
                                    outLen = out.length();
                                } else {
                                    out = m_resultclean.group(1);
                                    outLen = out.length() + 1;
                                }
                                start = m52.start() + (m52.group().length() - outLen);
                                end = start + out.length();
                            }
                        } else {
                            if (pSingle.matcher(m52.group(5)).matches()) {
                                m_resultclean = this.p_resultclean.matcher(m52.group());
                                if (m_resultclean.matches()) {
                                    if (m_resultclean.group(1) == null) {
                                        out = m_resultclean.group(2);
                                        outLen = out.length();
                                    } else {
                                        out = m_resultclean.group(1);
                                        outLen = out.length() + 1;
                                    }
                                    start = m52.start() + (m52.group().length() - outLen);
                                    end = start + out.length();
                                }
                            } else {
                                m_resultclean = this.p_resultclean.matcher(m52.group(1));
                                if (m_resultclean.matches()) {
                                    if (m_resultclean.group(1) == null) {
                                        out = m_resultclean.group(2);
                                        outLen = out.length();
                                    } else {
                                        out = m_resultclean.group(1);
                                        outLen = out.length() + 1;
                                    }
                                    start = m52.start(1) + (m52.group(1).length() - outLen);
                                    end = start + out.length();
                                }
                            }
                        }
                    }
                } else {
                    m_resultclean = this.p_resultclean.matcher(m52.group());
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m52.start() + (m52.group().length() - outLen);
                        end = start + out.length();
                    }
                }
                if (out.length() > 0) {
                    mPre_road = pPre_road.matcher(out);
                    if (mPre_road.find()) {
                        if (mPre_road.group(2) != null) {
                            out = "";
                        } else {
                            start += out.length() - mPre_road.group(1).length();
                            out = mPre_road.group(1);
                        }
                    }
                    mNot_road = pNot_road.matcher(out);
                    if (mNot_road.find()) {
                        if (mNot_road.group(2) != null && mNot_road.group(2).length() > 0) {
                            out = out.substring(mNot_road.group(1).length(), out.length());
                            start += mNot_road.group(1).length();
                        } else {
                            out = "";
                        }
                    }
                    if (out.length() > 0) {
                        nn.add(Integer.valueOf(start));
                        nn.add(Integer.valueOf(end));
                    }
                }
            }
        }
        while (m_52sub.find()) {
            out = "";
            if (!this.pRoad.matcher(m_52sub.group()).matches()) {
                if (m_52sub.group(5) == null) {
                    m_resultclean = this.p_resultclean.matcher(m_52sub.group(1));
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m_52sub.start(1) + (m_52sub.group(1).length() - outLen);
                        end = start + out.length();
                    }
                } else if (m_52sub.group(6) == null) {
                    mCut = pCut.matcher(m_52sub.group(5));
                    if (!mCut.matches()) {
                        cut = "";
                    } else if (mCut.group(1) == null) {
                        cut = "";
                    } else {
                        cut = mCut.group(1);
                    }
                    city = searCity(m_52sub.group(5).substring(cut.length(), m_52sub.group(5).length()), 2);
                    if (city != null) {
                        city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                        if (m_52sub.group(7) != null) {
                            if (m_52sub.group(4) == null) {
                                city = m_52sub.group(5) + m_52sub.group(7);
                            } else {
                                city = m_52sub.group(4) + m_52sub.group(5) + m_52sub.group(7);
                            }
                        } else if (m_52sub.group(4) != null) {
                            city = m_52sub.group(4) + city;
                        }
                        m_resultclean = this.p_resultclean.matcher(m_52sub.group(1) + m_52sub.group(3) + city);
                        if (m_resultclean.matches()) {
                            if (m_resultclean.group(1) == null) {
                                out = m_resultclean.group(2);
                                outLen = out.length();
                            } else {
                                out = m_resultclean.group(1);
                                outLen = out.length() + 1;
                            }
                            start = m_52sub.start(1) + ((m_52sub.group(1) + m_52sub.group(3) + city).length() - outLen);
                            end = start + out.length();
                        }
                    } else {
                        if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(m_52sub.group(3)).lookingAt()) {
                            m_resultclean = this.p_resultclean.matcher(m_52sub.group());
                            if (m_resultclean.matches()) {
                                if (m_resultclean.group(1) == null) {
                                    out = m_resultclean.group(2);
                                    outLen = out.length();
                                } else {
                                    out = m_resultclean.group(1);
                                    outLen = out.length() + 1;
                                }
                                start = m_52sub.start() + (m_52sub.group().length() - outLen);
                                end = start + out.length();
                            }
                        } else {
                            if (pSingle.matcher(m_52sub.group(5)).matches()) {
                                m_resultclean = this.p_resultclean.matcher(m_52sub.group());
                                if (m_resultclean.matches()) {
                                    if (m_resultclean.group(1) == null) {
                                        out = m_resultclean.group(2);
                                        outLen = out.length();
                                    } else {
                                        out = m_resultclean.group(1);
                                        outLen = out.length() + 1;
                                    }
                                    start = m_52sub.start() + (m_52sub.group().length() - outLen);
                                    end = start + out.length();
                                }
                            } else {
                                m_resultclean = this.p_resultclean.matcher(m_52sub.group(1));
                                if (m_resultclean.matches()) {
                                    if (m_resultclean.group(1) == null) {
                                        out = m_resultclean.group(2);
                                        outLen = out.length();
                                    } else {
                                        out = m_resultclean.group(1);
                                        outLen = out.length() + 1;
                                    }
                                    start = m_52sub.start(1) + (m_52sub.group(1).length() - outLen);
                                    end = start + out.length();
                                }
                            }
                        }
                    }
                } else {
                    m_resultclean = this.p_resultclean.matcher(m_52sub.group());
                    if (m_resultclean.matches()) {
                        if (m_resultclean.group(1) == null) {
                            out = m_resultclean.group(2);
                            outLen = out.length();
                        } else {
                            out = m_resultclean.group(1);
                            outLen = out.length() + 1;
                        }
                        start = m_52sub.start() + (m_52sub.group().length() - outLen);
                        end = start + out.length();
                    }
                }
                if (out.length() > 0) {
                    mPre_road = pPre_road.matcher(out);
                    if (mPre_road.find()) {
                        if (mPre_road.group(2) != null) {
                            out = "";
                        } else {
                            start += out.length() - mPre_road.group(1).length();
                            out = mPre_road.group(1);
                        }
                    }
                    mNot_road = pNot_road.matcher(out);
                    if (mNot_road.find()) {
                        if (mNot_road.group(2) != null && mNot_road.group(2).length() > 0) {
                            out = out.substring(mNot_road.group(1).length(), out.length());
                            start += mNot_road.group(1).length();
                        } else {
                            out = "";
                        }
                    }
                    if (out.length() > 0) {
                        nn.add(Integer.valueOf(start));
                        nn.add(Integer.valueOf(end));
                    }
                }
            }
        }
        while (m1346.find()) {
            if (p_big.matcher(m1346.group()).find()) {
                int length_bui;
                Iterator<Integer> it;
                int pr;
                Matcher mNum;
                String temp;
                Matcher mClean;
                int head = m1346.start();
                String[] buildings = new String[8];
                this.match_index_2.clear();
                buildings = searBuilding(m1346.group(), head);
                if (buildings != null) {
                    length_bui = buildings.length;
                    it = this.match_index_2.iterator();
                    pr = 0;
                    while (pr < length_bui && buildings[pr] != null) {
                        m_resultclean = this.p_resultclean.matcher(buildings[pr]);
                        if (m_resultclean.matches()) {
                            if (m_resultclean.group(1) == null) {
                                out = m_resultclean.group(2);
                                start = buildings[pr].length() - out.length();
                            } else {
                                out = m_resultclean.group(1);
                                start = buildings[pr].length() - (out.length() + 1);
                            }
                            mNum = this.pNum.matcher(out);
                            if (mNum.lookingAt()) {
                                out = out.substring(mNum.group().length(), out.length());
                                start += mNum.group().length();
                            }
                            if (it.hasNext()) {
                                start += ((Integer) it.next()).intValue();
                                end = start + out.length();
                                try {
                                    temp = source.substring(start, end);
                                    if (this.pDir.matcher(temp).lookingAt()) {
                                        out = temp;
                                    } else {
                                        mClean = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(temp);
                                        if (mClean.matches()) {
                                            start += mClean.group(1).length();
                                            out = out.substring(mClean.group(1).length(), out.length());
                                        }
                                    }
                                    if (!pBuilding.matcher(out).matches()) {
                                        nn.add(Integer.valueOf(start));
                                        nn.add(Integer.valueOf(end));
                                    }
                                } catch (Exception e) {
                                    System.out.println(new StringBuilder(String.valueOf(start)).append("**").append(end).toString());
                                }
                            }
                        }
                        pr++;
                    }
                }
                this.match_index_2.clear();
                buildings = searSpot(m1346.group(), head);
                if (buildings != null) {
                    length_bui = buildings.length;
                    it = this.match_index_2.iterator();
                    pr = 0;
                    while (pr < length_bui && buildings[pr] != null) {
                        m_resultclean = this.p_resultclean.matcher(buildings[pr]);
                        if (m_resultclean.matches()) {
                            if (m_resultclean.group(1) == null) {
                                out = m_resultclean.group(2);
                                start = buildings[pr].length() - out.length();
                            } else {
                                out = m_resultclean.group(1);
                                start = buildings[pr].length() - (out.length() + 1);
                            }
                            mNum = this.pNum.matcher(out);
                            if (mNum.lookingAt()) {
                                out = out.substring(mNum.group().length(), out.length());
                                start += mNum.group().length();
                            }
                            if (it.hasNext()) {
                                start += ((Integer) it.next()).intValue();
                                end = start + out.length();
                                try {
                                    temp = source.substring(start, end);
                                    if (this.pDir.matcher(temp).lookingAt()) {
                                        out = temp;
                                    } else {
                                        mClean = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(temp);
                                        if (mClean.matches()) {
                                            start += mClean.group(1).length();
                                            out = out.substring(mClean.group(1).length(), out.length());
                                        }
                                    }
                                    if (!pBuilding.matcher(out).matches()) {
                                        nn.add(Integer.valueOf(start));
                                        nn.add(Integer.valueOf(end));
                                    }
                                } catch (Exception e2) {
                                    System.out.println(new StringBuilder(String.valueOf(start)).append("**").append(end).toString());
                                }
                            }
                        }
                        pr++;
                    }
                }
            }
        }
        int num = nn.size();
        int[] nn_new2 = new int[num];
        for (i = 0; i < num; i++) {
            nn_new2[i] = ((Integer) nn.get(i)).intValue();
        }
        if (num <= 4) {
            nn_new2[0] = (num - 1) / 2;
            return createAddressResultData(nn_new2, source);
        }
        int j;
        int[] nn_new = new int[num];
        int t = 0;
        for (i = 1; i < (num - 1) / 2; i++) {
            for (j = i + 1; j < (num + 1) / 2; j++) {
                if (nn_new2[(i * 2) - 1] > nn_new2[(j * 2) - 1]) {
                    int i2 = (i * 2) - 1;
                    nn_new2[i2] = nn_new2[i2] + nn_new2[(j * 2) - 1];
                    nn_new2[(j * 2) - 1] = nn_new2[(i * 2) - 1] - nn_new2[(j * 2) - 1];
                    nn_new2[(i * 2) - 1] = nn_new2[(i * 2) - 1] - nn_new2[(j * 2) - 1];
                    i2 = i * 2;
                    nn_new2[i2] = nn_new2[i2] + nn_new2[j * 2];
                    nn_new2[j * 2] = nn_new2[i * 2] - nn_new2[j * 2];
                    nn_new2[i * 2] = nn_new2[i * 2] - nn_new2[j * 2];
                }
            }
        }
        i = 1;
        while (i < (num + 1) / 2) {
            t++;
            nn_new[(t * 2) - 1] = nn_new2[(i * 2) - 1];
            nn_new[t * 2] = nn_new2[i * 2];
            for (j = i + 1; j < (num + 1) / 2; j++) {
                if (nn_new2[i * 2] < nn_new2[(j * 2) - 1]) {
                    i = j - 1;
                    break;
                }
                nn_new2[i * 2] = max(nn_new2[i * 2], nn_new2[j * 2]);
                nn_new[t * 2] = nn_new2[i * 2];
                if (j == ((num + 1) / 2) - 1) {
                    i = j;
                }
            }
            i++;
        }
        nn_new2[0] = t;
        nn_new[0] = t;
        return createAddressResultData(nn_new, source);
    }

    private ArrayList<Match> createAddressResultData(int[] addrArray, String source) {
        if (addrArray.length == 0) {
            return null;
        }
        ArrayList<Match> matchedList = new ArrayList();
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

    private String[] searSpot(String string, int head) {
        int length = string.length();
        int head_0 = head;
        String city = "";
        String[] results = new String[8];
        String s_right = "";
        String building = "";
        String str = string;
        Pattern pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        String cut = "";
        int full = string.length();
        int index = 0;
        int count = 0;
        while (index < length) {
            int count2;
            str = str.substring(index, length);
            int head2 = head + (full - str.length());
            length -= index;
            index = 0;
            int position = DicSearch.dicsearch(2, str.toLowerCase(Locale.getDefault()));
            if (position != 0) {
                building = str.substring(0, position);
                s_right = str.substring(position, str.length());
                int length_bracket = searchBracket(s_right);
                if (length_bracket > 0) {
                    building = new StringBuilder(String.valueOf(building)).append(s_right.substring(0, length_bracket)).toString();
                    s_right = s_right.substring(length_bracket, s_right.length());
                }
                city = "";
                cut = "";
                Matcher m52s = this.p52s.matcher(s_right);
                Matcher mCut;
                if (!m52s.lookingAt()) {
                    Matcher m2s = this.p2s.matcher(s_right);
                    if (!m2s.lookingAt()) {
                        count2 = count + 1;
                        results[count] = building;
                        this.match_index_2.add(Integer.valueOf(head2));
                    } else if (m2s.group(3) == null) {
                        count2 = count + 1;
                        results[count] = building;
                        this.match_index_2.add(Integer.valueOf(head2));
                    } else if (m2s.group(4) == null) {
                        mCut = pCut.matcher(m2s.group(3));
                        if (!mCut.matches()) {
                            cut = "";
                        } else if (mCut.group(1) == null) {
                            cut = "";
                        } else {
                            cut = mCut.group(1);
                        }
                        city = searCity(m2s.group(3).substring(cut.length(), m2s.group(3).length()), 2);
                        if (city == null) {
                            if (pPre_city.matcher(m2s.group(1)).matches()) {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            } else {
                                if (pSingle.matcher(m2s.group(3)).matches()) {
                                    count2 = count + 1;
                                    results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                } else {
                                    count2 = count + 1;
                                    results[count] = building;
                                    this.match_index_2.add(Integer.valueOf(head2));
                                }
                            }
                        } else {
                            city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                            if (m2s.group(6) != null) {
                                if (m2s.group(2) != null) {
                                    city = m2s.group(2) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                } else {
                                    city = m2s.group(3) + m2s.group(5) + m2s.group(6);
                                }
                            } else if (m2s.group(2) != null) {
                                city = m2s.group(2) + city;
                            }
                            count2 = count + 1;
                            results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group(1) + city).toString();
                            this.match_index_2.add(Integer.valueOf(head2));
                        }
                    } else {
                        count2 = count + 1;
                        results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                        this.match_index_2.add(Integer.valueOf(head2));
                    }
                } else if (m52s.group(6) == null) {
                    count2 = count + 1;
                    results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                    this.match_index_2.add(Integer.valueOf(head2));
                } else if (m52s.group(7) == null) {
                    mCut = pCut.matcher(m52s.group(6));
                    if (!mCut.matches()) {
                        cut = "";
                    } else if (mCut.group(1) == null) {
                        cut = "";
                    } else {
                        cut = mCut.group(1);
                    }
                    city = searCity(m52s.group(6).substring(cut.length(), m52s.group(6).length()), 2);
                    if (city != null) {
                        city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                        if (m52s.group(8) != null) {
                            if (m52s.group(5) != null) {
                                city = m52s.group(5) + m52s.group(6) + m52s.group(8);
                            } else {
                                city = m52s.group(6) + m52s.group(8);
                            }
                        } else if (m52s.group(5) != null) {
                            city = m52s.group(5) + city;
                        }
                        count2 = count + 1;
                        results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).append(m52s.group(4)).append(city).toString();
                        this.match_index_2.add(Integer.valueOf(head2));
                    } else {
                        if (pPre_city.matcher(m52s.group(4)).matches()) {
                            count2 = count + 1;
                            results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                            this.match_index_2.add(Integer.valueOf(head2));
                        } else {
                            if (pSingle.matcher(m52s.group(3)).matches()) {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            } else {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            }
                        }
                    }
                } else {
                    count2 = count + 1;
                    results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                    this.match_index_2.add(Integer.valueOf(head2));
                }
                index = (results[count2 - 1].length() + 0) - 1;
            } else {
                while (index < length) {
                    if ((str.charAt(index) < 'a' || str.charAt(index) > 'z') && (str.charAt(index) < 'A' || str.charAt(index) > 'Z')) {
                        if (str.charAt(index) < '0') {
                            count2 = count;
                            break;
                        } else if (str.charAt(index) > '9') {
                            count2 = count;
                            break;
                        }
                    }
                    index++;
                }
                count2 = count;
            }
            index++;
            count = count2;
        }
        if (count >= 8) {
            return results;
        }
        String[] re = new String[count];
        for (index = 0; index < count; index++) {
            re[index] = results[index];
        }
        return re;
    }

    private int max(int i, int j) {
        if (i <= j) {
            return j;
        }
        return i;
    }

    public String[] searBuilding(String string, int head) {
        String sub_left = "";
        boolean flag = true;
        if (stanWri(string)) {
            flag = false;
        }
        return searBuilding_suf(string, sub_left, 0, flag, head);
    }

    private String[] searBuilding_suf(String str, String sub_left, int left_state, boolean flag, int head) {
        int index;
        int count;
        String cut = "";
        String[] results = new String[8];
        String[] results_2 = new String[0];
        String[] results_3 = new String[0];
        int count2 = 0;
        String sub1 = "";
        String sub2 = "";
        String sub_right = "";
        String building = "";
        String city = "";
        Matcher mLocation = this.pNot_1.matcher(str);
        if (mLocation.lookingAt()) {
            mLocation = this.pNot_2.matcher(mLocation.group(1));
            if (mLocation.lookingAt()) {
                int n = mLocation.group().length();
                str = str.substring(n, str.length());
                head += n;
            }
        }
        mLocation = this.pLocation.matcher(str);
        if (mLocation.find()) {
            sub1 = mLocation.group(1);
            Matcher mNo = this.pNo.matcher(sub1);
            String sub_right2;
            if (sub1.length() <= 0 || !noBlank(sub1)) {
                sub_left = mLocation.group();
                sub_right2 = str.substring(sub_left.length(), str.length());
                if (noBlank(sub_right2)) {
                    results_2 = searBuilding_suf(sub_right2, sub_left, 1, flag, head + (str.length() - sub_right2.length()));
                }
            } else if (mNo.matches() && mLocation.group(3) == null) {
                sub_left = mLocation.group();
                sub_right2 = str.substring(sub_left.length(), str.length());
                if (noBlank(sub_right2)) {
                    results_2 = searBuilding_suf(sub_right2, sub_left, 1, flag, head + (str.length() - sub_right2.length()));
                }
            } else {
                Matcher mComma = this.pComma.matcher(sub1);
                String sub1_temp;
                boolean sub1_undone;
                Matcher mPre_uni;
                String[] temp;
                int length;
                if (mComma.find()) {
                    sub2 = mComma.group(1);
                    if (sub2 != null && noBlank(sub2) && divStr(sub2).length <= 4) {
                        building = new StringBuilder(String.valueOf(sub2)).append(mLocation.group(2)).toString();
                        this.match_index_2.add(Integer.valueOf(mComma.start(1) + head));
                    }
                    if (building.length() == 0 && flag) {
                        sub1_temp = sub1;
                        sub1_undone = true;
                        while (sub1_undone) {
                            mPre_uni = this.pPre_uni.matcher(sub1_temp);
                            if (mPre_uni.find()) {
                                sub2 = mPre_uni.group(2);
                                if (sub2 == null || !noBlank(sub2)) {
                                    sub1_undone = false;
                                } else if (divStr(sub2).length > 4) {
                                    sub1_temp = sub2;
                                } else {
                                    building = new StringBuilder(String.valueOf(sub2)).append(mLocation.group(2)).toString();
                                    this.match_index_2.add(Integer.valueOf((sub1.length() - sub2.length()) + head));
                                    sub1_undone = false;
                                }
                            } else {
                                sub1_undone = false;
                            }
                        }
                        if (building.length() == 0) {
                            temp = divStr(sub1);
                            length = temp.length;
                            if (length <= 4) {
                                if (length > 0) {
                                    building = new StringBuilder(String.valueOf(sub1)).append(mLocation.group(2)).toString();
                                }
                                this.match_index_2.add(Integer.valueOf(head));
                            } else {
                                building = temp[length - 4] + temp[length - 3] + temp[length - 2] + temp[length - 1] + mLocation.group(2);
                                this.match_index_2.add(Integer.valueOf((sub1.length() - (building.length() - mLocation.group(2).length())) + head));
                            }
                        }
                    }
                } else {
                    sub1_temp = sub1;
                    sub1_undone = true;
                    while (sub1_undone) {
                        mPre_uni = this.pPre_uni.matcher(sub1_temp);
                        if (mPre_uni.find()) {
                            sub2 = mPre_uni.group(2);
                            if (sub2 == null || !noBlank(sub2)) {
                                sub1_undone = false;
                            } else if (divStr(sub2).length > 4) {
                                sub1_temp = sub2;
                            } else {
                                building = new StringBuilder(String.valueOf(sub2)).append(mLocation.group(2)).toString();
                                this.match_index_2.add(Integer.valueOf((sub1.length() - sub2.length()) + head));
                                sub1_undone = false;
                            }
                        } else {
                            sub1_undone = false;
                        }
                    }
                    if (building.length() == 0) {
                        temp = divStr(sub1);
                        length = temp.length;
                        if (length <= 4) {
                            if (length > 0) {
                                building = new StringBuilder(String.valueOf(sub1)).append(mLocation.group(2)).toString();
                            }
                            this.match_index_2.add(Integer.valueOf(head));
                        } else {
                            building = temp[length - 4] + temp[length - 3] + temp[length - 2] + temp[length - 1] + mLocation.group(2);
                            this.match_index_2.add(Integer.valueOf((sub1.length() - (building.length() - mLocation.group(2).length())) + head));
                        }
                    }
                }
                if (building.length() == 0 && mLocation.group(3) != null) {
                    building = mLocation.group(2);
                    this.match_index_2.add(Integer.valueOf(mLocation.group(1).length() + head));
                }
                if (building.length() <= 0) {
                    sub_left = mLocation.group();
                    sub_right2 = str.substring(sub_left.length(), str.length());
                    if (noBlank(sub_right2)) {
                        results_2 = searBuilding_suf(sub_right2, sub_left, 1, flag, head + (str.length() - sub_right2.length()));
                    }
                } else {
                    sub2 = mLocation.group();
                    String sub3 = sub2.substring(0, sub2.length() <= building.length() ? 0 : sub2.length() - building.length());
                    if (left_state == 1) {
                        sub3 = new StringBuilder(String.valueOf(sub_left)).append(sub3).toString();
                    }
                    if (noBlank(sub3)) {
                        results_3 = searBuilding_dic(sub3, head - sub_left.length());
                        left_state = 2;
                        sub_left = "";
                    }
                    sub3 = str.substring(sub2.length(), str.length());
                    if (noBlank(sub3)) {
                        city = "";
                        cut = "";
                        Matcher m52s = this.p52s.matcher(sub3);
                        Matcher mCut;
                        if (!m52s.lookingAt()) {
                            Matcher m2s = this.p2s.matcher(sub3);
                            if (!m2s.lookingAt()) {
                                count2 = 1;
                                results[0] = building;
                                results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                sub_right2 = sub_right;
                            } else if (m2s.group(3) == null) {
                                count2 = 1;
                                results[0] = building;
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                    sub_right2 = sub_right;
                                } else {
                                    sub_right2 = sub_right;
                                }
                            } else if (m2s.group(4) == null) {
                                mCut = this.pCut.matcher(m2s.group(3));
                                if (!mCut.matches()) {
                                    cut = "";
                                } else if (mCut.group(1) == null) {
                                    cut = "";
                                } else {
                                    cut = mCut.group(1);
                                }
                                city = searCity(m2s.group(3).substring(cut.length(), m2s.group(3).length()), 2);
                                if (city != null) {
                                    city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                                    if (m2s.group(6) != null) {
                                        if (m2s.group(2) != null) {
                                            city = m2s.group(2) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                        } else {
                                            city = m2s.group(3) + m2s.group(5) + m2s.group(6);
                                        }
                                    } else if (m2s.group(2) != null) {
                                        city = m2s.group(2) + city;
                                    }
                                    city = m2s.group(1) + city;
                                } else if (this.pPre_city.matcher(m2s.group(1)).lookingAt()) {
                                    city = m2s.group();
                                } else if (this.pSingle.matcher(m2s.group(3)).matches()) {
                                    city = m2s.group();
                                } else {
                                    city = "";
                                }
                                count2 = 1;
                                results[0] = new StringBuilder(String.valueOf(building)).append(city).toString();
                                sub3 = sub3.substring(city.length(), sub3.length());
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                    sub_right2 = sub_right;
                                } else {
                                    sub_right2 = sub_right;
                                }
                            } else {
                                count2 = 1;
                                results[0] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                sub3 = sub3.substring(m2s.group().length(), sub3.length());
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                    sub_right2 = sub_right;
                                } else {
                                    sub_right2 = sub_right;
                                }
                            }
                        } else if (m52s.group(6) == null) {
                            count2 = 1;
                            results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                            sub3 = sub3.substring(m52s.group().length(), sub3.length());
                            if (noBlank(sub3)) {
                                results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, (sub2.length() + head) + m52s.group().length());
                                sub_right2 = sub_right;
                            } else {
                                sub_right2 = sub_right;
                            }
                        } else if (m52s.group(7) == null) {
                            mCut = this.pCut.matcher(m52s.group(6));
                            if (!mCut.matches()) {
                                cut = "";
                            } else if (mCut.group(1) == null) {
                                cut = "";
                            } else {
                                cut = mCut.group(1);
                            }
                            city = searCity(m52s.group(6).substring(cut.length(), m52s.group(6).length()), 2);
                            if (city != null) {
                                city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                                if (m52s.group(8) != null) {
                                    if (m52s.group(5) != null) {
                                        city = m52s.group(5) + m52s.group(6) + m52s.group(8);
                                    } else {
                                        city = m52s.group(6) + m52s.group(8);
                                    }
                                } else if (m52s.group(5) != null) {
                                    city = m52s.group(5) + city;
                                }
                                count2 = 1;
                                results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).append(m52s.group(4)).append(city).toString();
                                sub3 = sub3.substring(((m52s.group(1).length() + m52s.group(2).length()) + m52s.group(4).length()) + city.length(), sub3.length());
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                    sub_right2 = sub_right;
                                } else {
                                    sub_right2 = sub_right;
                                }
                            } else {
                                if (this.pPre_city.matcher(m52s.group(4)).lookingAt()) {
                                    count2 = 1;
                                    results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                                    sub3 = sub3.substring(m52s.group().length(), sub3.length());
                                } else if (this.pSingle.matcher(m52s.group(3)).matches()) {
                                    count2 = 1;
                                    results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                                    sub3 = sub3.substring(m52s.group().length(), sub3.length());
                                } else {
                                    count2 = 1;
                                    results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).toString();
                                    sub3 = sub3.substring(m52s.group(1).length() + m52s.group(2).length(), sub3.length());
                                }
                                if (noBlank(sub3)) {
                                    results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, head + (str.length() - sub3.length()));
                                    sub_right2 = sub_right;
                                } else {
                                    sub_right2 = sub_right;
                                }
                            }
                        } else {
                            count2 = 1;
                            results[0] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                            sub3 = sub3.substring(m52s.group().length(), sub3.length());
                            if (noBlank(sub3)) {
                                results_2 = searBuilding_suf(sub3, sub_left, left_state, flag, (sub2.length() + head) + m52s.group().length());
                                sub_right2 = sub_right;
                            } else {
                                sub_right2 = sub_right;
                            }
                        }
                    } else {
                        count2 = 1;
                        results[0] = building;
                        sub_right2 = sub_right;
                    }
                }
            }
        } else {
            if (left_state == 1) {
                str = new StringBuilder(String.valueOf(sub_left)).append(str).toString();
            }
            results_2 = searBuilding_dic(str, head - sub_left.length());
        }
        if (results_3.length > 0) {
            index = 0;
            while (true) {
                count = count2;
                if (index >= results_3.length) {
                    break;
                }
                count2 = count + 1;
                results[count] = results_3[index];
                index++;
            }
            count2 = count;
        }
        if (results_2.length > 0) {
            index = 0;
            while (true) {
                count = count2;
                if (index >= results_2.length) {
                    break;
                }
                count2 = count + 1;
                results[count] = results_2[index];
                index++;
            }
            count2 = count;
        }
        if (count2 >= 8) {
            return results;
        }
        String[] re = new String[count2];
        for (index = 0; index < count2; index++) {
            re[index] = results[index];
        }
        return re;
    }

    private String[] searBuilding_dic(String string, int head) {
        int length = string.length();
        int head_0 = head;
        String city = "";
        String[] results = new String[8];
        String s_left = "";
        String s_right = "";
        String building = "";
        String str = string;
        Pattern pPre_building = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])((?i)(in|at|from|near|to|reach))\\b(\\s+(?i)the\\b)?(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+)?");
        Pattern pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        String cut = "";
        boolean flag = true;
        int full = string.length();
        int index = 0;
        int count = 0;
        while (index < length) {
            int count2;
            str = str.substring(index, length);
            int head2 = head + (full - str.length());
            length -= index;
            index = 0;
            s_left = string.substring(0, string.length() - length);
            int position = DicSearch.dicsearch(1, str.toLowerCase(Locale.getDefault()));
            if (position != 0) {
                building = str.substring(0, position);
                s_right = str.substring(position, str.length());
                int length_bracket = searchBracket(s_right);
                if (length_bracket > 0) {
                    building = new StringBuilder(String.valueOf(building)).append(s_right.substring(0, length_bracket)).toString();
                    s_right = s_right.substring(length_bracket, s_right.length());
                }
                city = "";
                cut = "";
                Matcher m52s = this.p52s.matcher(s_right);
                Matcher mCut;
                if (!m52s.lookingAt()) {
                    Matcher m2s = this.p2s.matcher(s_right);
                    if (m2s.lookingAt()) {
                        if (m2s.group(3) != null) {
                            if (m2s.group(4) == null) {
                                mCut = pCut.matcher(m2s.group(3));
                                if (!mCut.matches()) {
                                    cut = "";
                                } else if (mCut.group(1) == null) {
                                    cut = "";
                                } else {
                                    cut = mCut.group(1);
                                }
                                city = searCity(m2s.group(3).substring(cut.length(), m2s.group(3).length()), 2);
                                if (city == null) {
                                    if (pPre_city.matcher(m2s.group(1)).matches()) {
                                        count2 = count + 1;
                                        results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                        this.match_index_2.add(Integer.valueOf(head2));
                                    } else {
                                        if (pSingle.matcher(m2s.group(3)).matches()) {
                                            count2 = count + 1;
                                            results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                            this.match_index_2.add(Integer.valueOf(head2));
                                        } else if (pPre_building.matcher(s_left).matches()) {
                                            count2 = count + 1;
                                            results[count] = building;
                                            this.match_index_2.add(Integer.valueOf(head2));
                                        } else {
                                            flag = false;
                                            count2 = count;
                                        }
                                    }
                                } else {
                                    city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                                    if (m2s.group(6) != null) {
                                        if (m2s.group(2) != null) {
                                            city = m2s.group(2) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                        } else {
                                            city = m2s.group(3) + m2s.group(5) + m2s.group(6);
                                        }
                                    } else if (m2s.group(2) != null) {
                                        city = m2s.group(2) + city;
                                    }
                                    count2 = count + 1;
                                    results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group(1) + city).toString();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                }
                            } else {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m2s.group()).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            }
                        } else if (pPre_building.matcher(s_left).matches()) {
                            count2 = count + 1;
                            results[count] = building;
                            this.match_index_2.add(Integer.valueOf(head2));
                        } else {
                            flag = false;
                            count2 = count;
                        }
                    } else if (pPre_building.matcher(s_left).matches()) {
                        count2 = count + 1;
                        results[count] = building;
                        this.match_index_2.add(Integer.valueOf(head2));
                    } else {
                        flag = false;
                        count2 = count;
                    }
                } else if (m52s.group(6) == null) {
                    count2 = count + 1;
                    results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                    this.match_index_2.add(Integer.valueOf(head2));
                } else if (m52s.group(7) == null) {
                    mCut = pCut.matcher(m52s.group(6));
                    if (!mCut.matches()) {
                        cut = "";
                    } else if (mCut.group(1) == null) {
                        cut = "";
                    } else {
                        cut = mCut.group(1);
                    }
                    city = searCity(m52s.group(6).substring(cut.length(), m52s.group(6).length()), 2);
                    if (city != null) {
                        city = new StringBuilder(String.valueOf(cut)).append(city).toString();
                        if (m52s.group(8) != null) {
                            if (m52s.group(5) != null) {
                                city = m52s.group(5) + m52s.group(6) + m52s.group(8);
                            } else {
                                city = m52s.group(6) + m52s.group(8);
                            }
                        } else if (m52s.group(5) != null) {
                            city = m52s.group(5) + city;
                        }
                        count2 = count + 1;
                        results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).append(m52s.group(4)).append(city).toString();
                        this.match_index_2.add(Integer.valueOf(head2));
                    } else {
                        if (pPre_city.matcher(m52s.group(4)).matches()) {
                            count2 = count + 1;
                            results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                            this.match_index_2.add(Integer.valueOf(head2));
                        } else {
                            if (pSingle.matcher(m52s.group(3)).matches()) {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            } else {
                                count2 = count + 1;
                                results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group(1)).append(m52s.group(2)).toString();
                                this.match_index_2.add(Integer.valueOf(head2));
                            }
                        }
                    }
                } else {
                    count2 = count + 1;
                    results[count] = new StringBuilder(String.valueOf(building)).append(m52s.group()).toString();
                    this.match_index_2.add(Integer.valueOf(head2));
                }
                if (flag) {
                    index = (results[count2 - 1].length() + 0) - 1;
                    string = str.substring(results[count2 - 1].length(), str.length());
                } else {
                    index = (building.length() + 0) - 1;
                    string = str.substring(building.length(), str.length());
                    flag = true;
                }
            } else {
                while (index < length) {
                    if ((str.charAt(index) < 'a' || str.charAt(index) > 'z') && (str.charAt(index) < 'A' || str.charAt(index) > 'Z')) {
                        if (str.charAt(index) < '0') {
                            count2 = count;
                            break;
                        } else if (str.charAt(index) > '9') {
                            count2 = count;
                            break;
                        }
                    }
                    index++;
                }
                count2 = count;
            }
            index++;
            count = count2;
        }
        if (count >= 8) {
            return results;
        }
        String[] re = new String[count];
        for (index = 0; index < count; index++) {
            re[index] = results[index];
        }
        return re;
    }

    private boolean noBlank(String str) {
        int n = str.length();
        str = str.toLowerCase(Locale.getDefault());
        boolean flag = true;
        int index = 0;
        while (flag && index < n) {
            if ((str.charAt(index) <= 'z' && str.charAt(index) >= 'a') || (str.charAt(index) <= '9' && str.charAt(index) >= '0')) {
                flag = false;
            }
            index++;
        }
        if (flag) {
            return false;
        }
        return true;
    }

    private String[] divStr(String str) {
        int index;
        String[] strs = new String[150];
        int length = str.length();
        int pr = 0;
        strs[0] = "";
        for (index = 0; index < length; index++) {
            char letter = str.charAt(index);
            if ((letter <= 'z' && letter >= 'a') || ((letter <= 'Z' && letter >= 'A') || (letter <= '9' && letter >= '0'))) {
                strs[pr] = strs[pr] + letter;
            } else if (strs[pr].length() > 0) {
                strs[pr] = strs[pr] + letter;
                pr++;
                strs[pr] = "";
            } else if (pr > 0) {
                int i = pr - 1;
                strs[i] = strs[i] + letter;
            }
        }
        if (strs[pr].length() > 0) {
            pr++;
        }
        if (pr >= 150) {
            return strs;
        }
        String[] re = new String[pr];
        for (index = 0; index < pr; index++) {
            re[index] = strs[index];
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
        Matcher mCity = Pattern.compile("([\\s\\S]*(?i)(town|city|county)\\b)(?:.*)").matcher(string);
        if (mode != 1) {
            if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
                return mCity.group(1);
            }
            int position = DicSearch.dicsearch(0, string.toLowerCase(Locale.getDefault()));
            if (position > 0) {
                Matcher mCity2 = Pattern.compile("(\\s+(?i)(town|city|county))\\b.*").matcher(string.substring(position, length));
                if (mCity2.matches()) {
                    return string.substring(0, position) + mCity2.group(1);
                }
                return string.substring(0, position);
            }
        } else if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
            return string;
        } else {
            int index = 0;
            while (index < length) {
                str = str.substring(index, length);
                length -= index;
                index = 0;
                if (DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault())) != 0) {
                    return str;
                }
                while (index < length && ((str.charAt(index) >= 'a' && str.charAt(index) <= 'z') || ((str.charAt(index) >= 'A' && str.charAt(index) <= 'Z') || (str.charAt(index) >= '0' && str.charAt(index) <= '9')))) {
                    index++;
                }
                index++;
            }
        }
        return null;
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
            public int compare(Match p1, Match p2) {
                if (p1.getStartPos().compareTo(p2.getStartPos()) != 0) {
                    return p1.getStartPos().compareTo(p2.getStartPos());
                }
                return p1.getEndPos().compareTo(p2.getEndPos());
            }
        });
        int i = posList.size() - 1;
        while (i > 0) {
            if (((Match) posList.get(i - 1)).getStartPos().intValue() <= ((Match) posList.get(i)).getStartPos().intValue() && ((Match) posList.get(i)).getStartPos().intValue() <= ((Match) posList.get(i - 1)).getEndPos().intValue()) {
                if (((Match) posList.get(i - 1)).getEndPos().intValue() < ((Match) posList.get(i)).getEndPos().intValue()) {
                    ((Match) posList.get(i - 1)).setEndPos(((Match) posList.get(i)).getEndPos());
                    ((Match) posList.get(i - 1)).setMatchedAddr(sourceTxt.substring(((Match) posList.get(i - 1)).getStartPos().intValue(), ((Match) posList.get(i - 1)).getEndPos().intValue()));
                    posList.remove(i);
                } else if (((Match) posList.get(i - 1)).getEndPos().intValue() >= ((Match) posList.get(i)).getEndPos().intValue()) {
                    posList.remove(i);
                }
            }
            i--;
        }
        return posList;
    }
}
