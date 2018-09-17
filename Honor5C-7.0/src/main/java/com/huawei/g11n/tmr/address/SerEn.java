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
    private String location;
    ArrayList<Integer> match_index_2;
    private String not;
    private Pattern p1346;
    private Pattern p28;
    private Pattern p2s;
    private Pattern p52;
    private Pattern p52_sub;
    private Pattern p52s;
    private Pattern pCode_a;
    Pattern pComma;
    Pattern pCut;
    private Pattern pDir;
    Pattern pLocation;
    Pattern pNo;
    Pattern pNot_1;
    Pattern pNot_2;
    private Pattern pNum;
    Pattern pPre_city;
    Pattern pPre_uni;
    private Pattern pRoad;
    Pattern pSingle;
    private Pattern p_box;
    private Pattern p_resultclean;
    private ReguEx reguEx;
    private String road_suf;

    SerEn() {
        this.reguEx = new ReguEx();
        this.location = this.reguEx.location;
        this.p28 = this.reguEx.p28;
        this.p1346 = this.reguEx.p1346;
        this.p52 = this.reguEx.p52;
        this.p2s = this.reguEx.p2s;
        this.p52s = this.reguEx.p52s;
        this.p52_sub = this.reguEx.p52_sub;
        this.pRoad = Pattern.compile("(?i)(?:\\s*(?:(in|on|at)\\s+)?(?:the\\s+)?(boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Way|Fwy|Crescent|Highway))");
        this.pCode_a = Pattern.compile("(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d)");
        this.p_resultclean = Pattern.compile("(?:(?:[^0-9a-zA-Z]*)(?i)(?:(?:in|at|on|from|to|of|and)\\s+)?(?:(?:the)\\s+)?)(?:([\\s\\S]*)?,|([\\s\\S]*))");
        this.match_index_2 = new ArrayList();
        this.p_box = Pattern.compile(this.reguEx.post_box);
        this.road_suf = "(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))";
        this.pDir = Pattern.compile("\\s*(south|north|west|east)\\s*");
        this.not = "(?i)(?:my|your|his|her|its|their|our|this|that|the|a|an|what|which|whose)";
        this.pNot_1 = Pattern.compile("([\\s\\S]*?)(?<![a-zA-Z])" + this.location + "(?![a-zA-Z])");
        this.pNot_2 = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])" + this.not + "\\s+");
        this.pNum = Pattern.compile("(?:(?:\\s*[:,\\.\"-]\\s*|\\s*)\\d+(?:\\s*[,\\.\":-]\\s*|\\s+))+");
        this.pLocation = Pattern.compile("(?:([\\s\\S]*?)(?<![a-zA-Z])((?:" + this.location + ")((?:\\s+|\\s*&\\s*)(?:" + this.location + "))?" + ")(?![a-zA-Z]))");
        this.pComma = Pattern.compile("(?:(?:[\\s\\S]*)(?:,|\\.)([\\s\\S]*))");
        this.pPre_city = Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])");
        this.pPre_uni = Pattern.compile("(?:\\b(?i)(in|at|from|near|to|of|for)\\b([\\s\\S]*))");
        this.pNo = Pattern.compile("(?:[\\s\\S]*(?<![a-zA-Z])(?i)(the|in|on|at|from|to|of|for)(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+))");
        this.pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        this.pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
    }

    public ArrayList<Match> search(String str) {
        int length;
        Matcher matcher;
        int i;
        Matcher matcher2;
        ArrayList arrayList = new ArrayList();
        Pattern compile = Pattern.compile("[A-Z0-9]");
        String str2 = "";
        str2 = "";
        Pattern compile2 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(?:uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern compile3 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern compile4 = Pattern.compile("(?i)(?<![a-z])(?:(?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+)((?:[\\s\\S]+?)(?:(?<![a-z])((?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+))?" + this.road_suf + "(?![a-zA-Z])[\\s\\S]*)");
        Pattern compile5 = Pattern.compile("(?i)((?<![a-zA-Z])(?:a|what|which|whose|i|you|this|that|my|his|her|out|their|its)\\s+)([\\s\\S]+)?" + this.road_suf + "(?![a-zA-Z])");
        Pattern compile6 = Pattern.compile("(?:[^0-9a-zA-Z]*|\\s*(?:(?i)the|this|a|that)\\s*)(?:" + this.location + ")[^0-9a-zA-Z]*");
        int i2 = 0;
        str2 = "";
        arrayList.add(Integer.valueOf(0));
        Matcher matcher3 = this.p52.matcher(str);
        Matcher matcher4 = this.p28.matcher(str);
        Matcher matcher5 = this.p1346.matcher(str);
        Matcher matcher6 = this.p52_sub.matcher(str);
        this.match_index_2.clear();
        int i3 = 0;
        Object obj = TYPE_BUILDING;
        while (matcher4.find()) {
            if (matcher4.group(TYPE_BUILDING) == null) {
                if (!this.pCode_a.matcher(matcher4.group()).find()) {
                    i2 = matcher4.start();
                    length = matcher4.group().length() + i2;
                    arrayList.add(Integer.valueOf(i2));
                    arrayList.add(Integer.valueOf(length));
                    i3 = i2;
                    i2 = length;
                } else if (matcher4.group(6).indexOf(45) != -1) {
                    i2 = matcher4.start(6);
                    length = matcher4.group(6).length() + i2;
                    arrayList.add(Integer.valueOf(i2));
                    arrayList.add(Integer.valueOf(length));
                    i3 = i2;
                    i2 = length;
                } else if (matcher4.group(5) != null && matcher4.group(5).length() > 0) {
                    i2 = matcher4.start(6);
                    length = matcher4.group(6).length() + i2;
                    arrayList.add(Integer.valueOf(i2));
                    arrayList.add(Integer.valueOf(length));
                    i3 = i2;
                    i2 = length;
                }
            } else if (matcher4.group(4) == null) {
                if (matcher4.group(TYPE_BUILDING2) == null) {
                    Object searCity = searCity(matcher4.group(3), TYPE_BUILDING);
                    if (searCity == null) {
                        searCity = "";
                    }
                    Pattern pattern = this.p_resultclean;
                    StringBuilder stringBuilder = new StringBuilder(String.valueOf(searCity));
                    Pattern pattern2 = pattern;
                    matcher = pattern2.matcher(stringBuilder.append(matcher4.group(5)).append(matcher4.group(6)).toString());
                } else {
                    matcher = this.p_resultclean.matcher(matcher4.group());
                }
                if (matcher.matches()) {
                    if (matcher.group(TYPE_BUILDING) == null) {
                        str2 = matcher.group(TYPE_BUILDING2);
                        i2 = str2.length();
                    } else {
                        str2 = matcher.group(TYPE_BUILDING);
                        i2 = str2.length() + TYPE_BUILDING;
                    }
                    i3 = matcher4.start(5) + ((matcher4.group(5) + matcher4.group(6)).length() - i2);
                    i2 = i3 + str2.length();
                    arrayList.add(Integer.valueOf(i3));
                    arrayList.add(Integer.valueOf(i2));
                    if (matcher4.group(TYPE_BUILDING2) != null) {
                        obj = null;
                    }
                }
            } else {
                matcher = this.p_resultclean.matcher(matcher4.group());
                if (matcher.matches()) {
                    if (matcher.group(TYPE_BUILDING) == null) {
                        str2 = matcher.group(TYPE_BUILDING2);
                        i2 = str2.length();
                    } else {
                        str2 = matcher.group(TYPE_BUILDING);
                        i2 = str2.length() + TYPE_BUILDING;
                    }
                    i3 = matcher4.start() + (matcher4.group().length() - i2);
                    i2 = i3 + str2.length();
                    arrayList.add(Integer.valueOf(i3));
                    arrayList.add(Integer.valueOf(i2));
                    if (matcher4.group(TYPE_BUILDING2) != null) {
                        obj = null;
                    }
                }
            }
        }
        if (obj == null) {
            i = i2;
        } else {
            matcher = this.p_box.matcher(str);
            while (matcher.find()) {
                i3 = matcher.start();
                i2 = matcher.group().length() + i3;
                arrayList.add(Integer.valueOf(i3));
                arrayList.add(Integer.valueOf(i2));
            }
            i = i2;
        }
        while (matcher3.find()) {
            String str3 = "";
            if (!this.pRoad.matcher(matcher3.group()).matches()) {
                if (matcher3.group(5) == null) {
                    matcher = this.p_resultclean.matcher(matcher3.group(TYPE_BUILDING));
                    if (matcher.matches()) {
                        if (matcher.group(TYPE_BUILDING) == null) {
                            str2 = matcher.group(TYPE_BUILDING2);
                            i2 = str2.length();
                        } else {
                            str2 = matcher.group(TYPE_BUILDING);
                            i2 = str2.length() + TYPE_BUILDING;
                        }
                        i = matcher3.start(TYPE_BUILDING) + (matcher3.group(TYPE_BUILDING).length() - i2);
                        i2 = str2.length() + i;
                    } else {
                        str2 = str3;
                        i2 = i;
                        i = i3;
                    }
                } else if (matcher3.group(6) == null) {
                    matcher = compile2.matcher(matcher3.group(5));
                    if (!matcher.matches()) {
                        str2 = "";
                    } else if (matcher.group(TYPE_BUILDING) == null) {
                        str2 = "";
                    } else {
                        str2 = matcher.group(TYPE_BUILDING);
                    }
                    String searCity2 = searCity(matcher3.group(5).substring(str2.length(), matcher3.group(5).length()), TYPE_BUILDING2);
                    if (searCity2 != null) {
                        str2 = new StringBuilder(String.valueOf(str2)).append(searCity2).toString();
                        if (matcher3.group(7) != null) {
                            if (matcher3.group(4) == null) {
                                str2 = matcher3.group(5) + matcher3.group(7);
                            } else {
                                str2 = matcher3.group(4) + matcher3.group(5) + matcher3.group(7);
                            }
                        } else if (matcher3.group(4) != null) {
                            str2 = matcher3.group(4) + str2;
                        }
                        matcher4 = this.p_resultclean.matcher(matcher3.group(TYPE_BUILDING) + matcher3.group(3) + str2);
                        if (matcher4.matches()) {
                            if (matcher4.group(TYPE_BUILDING) == null) {
                                str3 = matcher4.group(TYPE_BUILDING2);
                                i = str3.length();
                            } else {
                                str3 = matcher4.group(TYPE_BUILDING);
                                i = str3.length() + TYPE_BUILDING;
                            }
                            String valueOf = String.valueOf(matcher3.group(TYPE_BUILDING));
                            i = matcher3.start(TYPE_BUILDING) + ((matcher3.group(3) + str2).length() - i);
                            String str4 = str3;
                            i2 = str3.length() + i;
                            str2 = str4;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(matcher3.group(3)).lookingAt()) {
                        matcher = this.p_resultclean.matcher(matcher3.group());
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher3.start() + (matcher3.group().length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else if (compile3.matcher(matcher3.group(5)).matches()) {
                        matcher = this.p_resultclean.matcher(matcher3.group());
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher3.start() + (matcher3.group().length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else {
                        matcher = this.p_resultclean.matcher(matcher3.group(TYPE_BUILDING));
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher3.start(TYPE_BUILDING) + (matcher3.group(TYPE_BUILDING).length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    }
                } else {
                    matcher = this.p_resultclean.matcher(matcher3.group());
                    if (matcher.matches()) {
                        if (matcher.group(TYPE_BUILDING) == null) {
                            str2 = matcher.group(TYPE_BUILDING2);
                            i2 = str2.length();
                        } else {
                            str2 = matcher.group(TYPE_BUILDING);
                            i2 = str2.length() + TYPE_BUILDING;
                        }
                        i = matcher3.start() + (matcher3.group().length() - i2);
                        i2 = str2.length() + i;
                    } else {
                        str2 = str3;
                        i2 = i;
                        i = i3;
                    }
                }
                if (str2.length() <= 0) {
                    i3 = i;
                    i = i2;
                } else {
                    matcher2 = compile4.matcher(str2);
                    if (matcher2.find()) {
                        if (matcher2.group(TYPE_BUILDING2) != null) {
                            str2 = "";
                        } else {
                            i += str2.length() - matcher2.group(TYPE_BUILDING).length();
                            str2 = matcher2.group(TYPE_BUILDING);
                        }
                    }
                    matcher2 = compile5.matcher(str2);
                    if (matcher2.find()) {
                        if (matcher2.group(TYPE_BUILDING2) != null && matcher2.group(TYPE_BUILDING2).length() > 0) {
                            str2 = str2.substring(matcher2.group(TYPE_BUILDING).length(), str2.length());
                            i += matcher2.group(TYPE_BUILDING).length();
                        } else {
                            str2 = "";
                        }
                    }
                    if (str2.length() <= 0) {
                        i3 = i;
                        i = i2;
                    } else {
                        arrayList.add(Integer.valueOf(i));
                        arrayList.add(Integer.valueOf(i2));
                        i3 = i;
                        i = i2;
                    }
                }
            }
        }
        while (matcher6.find()) {
            str3 = "";
            if (!this.pRoad.matcher(matcher6.group()).matches()) {
                if (matcher6.group(5) == null) {
                    matcher = this.p_resultclean.matcher(matcher6.group(TYPE_BUILDING));
                    if (matcher.matches()) {
                        if (matcher.group(TYPE_BUILDING) == null) {
                            str2 = matcher.group(TYPE_BUILDING2);
                            i2 = str2.length();
                        } else {
                            str2 = matcher.group(TYPE_BUILDING);
                            i2 = str2.length() + TYPE_BUILDING;
                        }
                        i = matcher6.start(TYPE_BUILDING) + (matcher6.group(TYPE_BUILDING).length() - i2);
                        i2 = str2.length() + i;
                    } else {
                        str2 = str3;
                        i2 = i;
                        i = i3;
                    }
                } else if (matcher6.group(6) == null) {
                    matcher = compile2.matcher(matcher6.group(5));
                    if (!matcher.matches()) {
                        str2 = "";
                    } else if (matcher.group(TYPE_BUILDING) == null) {
                        str2 = "";
                    } else {
                        str2 = matcher.group(TYPE_BUILDING);
                    }
                    String searCity3 = searCity(matcher6.group(5).substring(str2.length(), matcher6.group(5).length()), TYPE_BUILDING2);
                    if (searCity3 != null) {
                        str2 = new StringBuilder(String.valueOf(str2)).append(searCity3).toString();
                        if (matcher6.group(7) != null) {
                            if (matcher6.group(4) == null) {
                                str2 = matcher6.group(5) + matcher6.group(7);
                            } else {
                                str2 = matcher6.group(4) + matcher6.group(5) + matcher6.group(7);
                            }
                        } else if (matcher6.group(4) != null) {
                            str2 = matcher6.group(4) + str2;
                        }
                        valueOf = String.valueOf(matcher6.group(TYPE_BUILDING));
                        matcher3 = this.p_resultclean.matcher(matcher6.group(3) + str2);
                        if (matcher3.matches()) {
                            if (matcher3.group(TYPE_BUILDING) == null) {
                                str3 = matcher3.group(TYPE_BUILDING2);
                                i = str3.length();
                            } else {
                                str3 = matcher3.group(TYPE_BUILDING);
                                i = str3.length() + TYPE_BUILDING;
                            }
                            i = matcher6.start(TYPE_BUILDING) + ((matcher6.group(TYPE_BUILDING) + matcher6.group(3) + str2).length() - i);
                            str4 = str3;
                            i2 = str3.length() + i;
                            str2 = str4;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else if (Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])").matcher(matcher6.group(3)).lookingAt()) {
                        matcher = this.p_resultclean.matcher(matcher6.group());
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher6.start() + (matcher6.group().length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else if (compile3.matcher(matcher6.group(5)).matches()) {
                        matcher = this.p_resultclean.matcher(matcher6.group());
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher6.start() + (matcher6.group().length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    } else {
                        matcher = this.p_resultclean.matcher(matcher6.group(TYPE_BUILDING));
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = str2.length() + TYPE_BUILDING;
                            }
                            i = matcher6.start(TYPE_BUILDING) + (matcher6.group(TYPE_BUILDING).length() - i2);
                            i2 = str2.length() + i;
                        } else {
                            str2 = str3;
                            i2 = i;
                            i = i3;
                        }
                    }
                } else {
                    matcher = this.p_resultclean.matcher(matcher6.group());
                    if (matcher.matches()) {
                        if (matcher.group(TYPE_BUILDING) == null) {
                            str2 = matcher.group(TYPE_BUILDING2);
                            i2 = str2.length();
                        } else {
                            str2 = matcher.group(TYPE_BUILDING);
                            i2 = str2.length() + TYPE_BUILDING;
                        }
                        i = matcher6.start() + (matcher6.group().length() - i2);
                        i2 = str2.length() + i;
                    } else {
                        str2 = str3;
                        i2 = i;
                        i = i3;
                    }
                }
                if (str2.length() <= 0) {
                    i3 = i;
                    i = i2;
                } else {
                    matcher2 = compile4.matcher(str2);
                    if (matcher2.find()) {
                        if (matcher2.group(TYPE_BUILDING2) != null) {
                            str2 = "";
                        } else {
                            i += str2.length() - matcher2.group(TYPE_BUILDING).length();
                            str2 = matcher2.group(TYPE_BUILDING);
                        }
                    }
                    matcher2 = compile5.matcher(str2);
                    if (matcher2.find()) {
                        if (matcher2.group(TYPE_BUILDING2) != null && matcher2.group(TYPE_BUILDING2).length() > 0) {
                            str2 = str2.substring(matcher2.group(TYPE_BUILDING).length(), str2.length());
                            i += matcher2.group(TYPE_BUILDING).length();
                        } else {
                            str2 = "";
                        }
                    }
                    if (str2.length() <= 0) {
                        i3 = i;
                        i = i2;
                    } else {
                        arrayList.add(Integer.valueOf(i));
                        arrayList.add(Integer.valueOf(i2));
                        i3 = i;
                        i = i2;
                    }
                }
            }
        }
        while (matcher5.find()) {
            if (compile.matcher(matcher5.group()).find()) {
                Matcher matcher7;
                Object obj2;
                CharSequence charSequence;
                CharSequence substring;
                int start = matcher5.start();
                String[] strArr = new String[8];
                this.match_index_2.clear();
                String[] searBuilding = searBuilding(matcher5.group(), start);
                if (searBuilding != null) {
                    int length2 = searBuilding.length;
                    Iterator it = this.match_index_2.iterator();
                    i3 = 0;
                    while (i3 < length2 && searBuilding[i3] != null) {
                        matcher = this.p_resultclean.matcher(searBuilding[i3]);
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = searBuilding[i3].length() - str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = searBuilding[i3].length() - (str2.length() + TYPE_BUILDING);
                            }
                            matcher7 = this.pNum.matcher(str2);
                            if (matcher7.lookingAt()) {
                                str2 = str2.substring(matcher7.group().length(), str2.length());
                                i = i2 + matcher7.group().length();
                                obj2 = str2;
                            } else {
                                i = i2;
                                charSequence = str2;
                            }
                            if (it.hasNext()) {
                                length = ((Integer) it.next()).intValue() + i;
                                int length3 = length + charSequence.length();
                                try {
                                    substring = str.substring(length, length3);
                                    if (this.pDir.matcher(substring).lookingAt()) {
                                        charSequence = substring;
                                    } else {
                                        matcher7 = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(substring);
                                        if (matcher7.matches()) {
                                            length += matcher7.group(TYPE_BUILDING).length();
                                            charSequence = charSequence.substring(matcher7.group(TYPE_BUILDING).length(), charSequence.length());
                                        }
                                    }
                                    if (!compile6.matcher(charSequence).matches()) {
                                        arrayList.add(Integer.valueOf(length));
                                        arrayList.add(Integer.valueOf(length3));
                                    }
                                } catch (Exception e) {
                                    System.out.println(new StringBuilder(String.valueOf(length)).append("**").append(length3).toString());
                                }
                            }
                        }
                        i3 += TYPE_BUILDING;
                    }
                }
                this.match_index_2.clear();
                String[] searSpot = searSpot(matcher5.group(), start);
                if (searSpot != null) {
                    int length4 = searSpot.length;
                    Iterator it2 = this.match_index_2.iterator();
                    i3 = 0;
                    while (i3 < length4 && searSpot[i3] != null) {
                        matcher = this.p_resultclean.matcher(searSpot[i3]);
                        if (matcher.matches()) {
                            if (matcher.group(TYPE_BUILDING) == null) {
                                str2 = matcher.group(TYPE_BUILDING2);
                                i2 = searSpot[i3].length() - str2.length();
                            } else {
                                str2 = matcher.group(TYPE_BUILDING);
                                i2 = searSpot[i3].length() - (str2.length() + TYPE_BUILDING);
                            }
                            matcher7 = this.pNum.matcher(str2);
                            if (matcher7.lookingAt()) {
                                str2 = str2.substring(matcher7.group().length(), str2.length());
                                i = i2 + matcher7.group().length();
                                obj2 = str2;
                            } else {
                                i = i2;
                                charSequence = str2;
                            }
                            if (it2.hasNext()) {
                                length = ((Integer) it2.next()).intValue() + i;
                                int length5 = length + charSequence.length();
                                try {
                                    substring = str.substring(length, length5);
                                    if (this.pDir.matcher(substring).lookingAt()) {
                                        charSequence = substring;
                                    } else {
                                        matcher7 = Pattern.compile("((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)").matcher(substring);
                                        if (matcher7.matches()) {
                                            length += matcher7.group(TYPE_BUILDING).length();
                                            charSequence = charSequence.substring(matcher7.group(TYPE_BUILDING).length(), charSequence.length());
                                        }
                                    }
                                    if (!compile6.matcher(charSequence).matches()) {
                                        arrayList.add(Integer.valueOf(length));
                                        arrayList.add(Integer.valueOf(length5));
                                    }
                                } catch (Exception e2) {
                                    System.out.println(new StringBuilder(String.valueOf(length)).append("**").append(length5).toString());
                                }
                            }
                        }
                        i3 += TYPE_BUILDING;
                    }
                }
            }
        }
        i3 = arrayList.size();
        int[] iArr = new int[i3];
        for (i2 = 0; i2 < i3; i2 += TYPE_BUILDING) {
            iArr[i2] = ((Integer) arrayList.get(i2)).intValue();
        }
        if (i3 <= 4) {
            iArr[0] = (i3 - 1) / TYPE_BUILDING2;
            return createAddressResultData(iArr, str);
        }
        int[] iArr2 = new int[i3];
        for (i2 = TYPE_BUILDING; i2 < (i3 - 1) / TYPE_BUILDING2; i2 += TYPE_BUILDING) {
            for (length = i2 + TYPE_BUILDING; length < (i3 + TYPE_BUILDING) / TYPE_BUILDING2; length += TYPE_BUILDING) {
                if (iArr[(i2 * TYPE_BUILDING2) - 1] > iArr[(length * TYPE_BUILDING2) - 1]) {
                    start = (i2 * TYPE_BUILDING2) - 1;
                    iArr[start] = iArr[start] + iArr[(length * TYPE_BUILDING2) - 1];
                    iArr[(length * TYPE_BUILDING2) - 1] = iArr[(i2 * TYPE_BUILDING2) - 1] - iArr[(length * TYPE_BUILDING2) - 1];
                    iArr[(i2 * TYPE_BUILDING2) - 1] = iArr[(i2 * TYPE_BUILDING2) - 1] - iArr[(length * TYPE_BUILDING2) - 1];
                    start = i2 * TYPE_BUILDING2;
                    iArr[start] = iArr[start] + iArr[length * TYPE_BUILDING2];
                    iArr[length * TYPE_BUILDING2] = iArr[i2 * TYPE_BUILDING2] - iArr[length * TYPE_BUILDING2];
                    iArr[i2 * TYPE_BUILDING2] = iArr[i2 * TYPE_BUILDING2] - iArr[length * TYPE_BUILDING2];
                }
            }
        }
        length = TYPE_BUILDING;
        i2 = 0;
        while (length < (i3 + TYPE_BUILDING) / TYPE_BUILDING2) {
            i = i2 + TYPE_BUILDING;
            iArr2[(i * TYPE_BUILDING2) - 1] = iArr[(length * TYPE_BUILDING2) - 1];
            iArr2[i * TYPE_BUILDING2] = iArr[length * TYPE_BUILDING2];
            for (i2 = length + TYPE_BUILDING; i2 < (i3 + TYPE_BUILDING) / TYPE_BUILDING2; i2 += TYPE_BUILDING) {
                if (iArr[length * TYPE_BUILDING2] < iArr[(i2 * TYPE_BUILDING2) - 1]) {
                    length = i2 - 1;
                    break;
                }
                iArr[length * TYPE_BUILDING2] = max(iArr[length * TYPE_BUILDING2], iArr[i2 * TYPE_BUILDING2]);
                iArr2[i * TYPE_BUILDING2] = iArr[length * TYPE_BUILDING2];
                if (i2 == ((i3 + TYPE_BUILDING) / TYPE_BUILDING2) - 1) {
                    length = i2;
                }
            }
            length += TYPE_BUILDING;
            i2 = i;
        }
        iArr[0] = i2;
        iArr2[0] = i2;
        return createAddressResultData(iArr2, str);
    }

    private ArrayList<Match> createAddressResultData(int[] iArr, String str) {
        if (iArr.length == 0) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        int i = iArr[0];
        for (int i2 = TYPE_BUILDING; i2 < (i * TYPE_BUILDING2) + TYPE_BUILDING; i2 += TYPE_BUILDING2) {
            Match match = new Match();
            match.setMatchedAddr(str.substring(iArr[i2], iArr[i2 + TYPE_BUILDING]));
            match.setStartPos(Integer.valueOf(iArr[i2]));
            match.setEndPos(Integer.valueOf(iArr[i2 + TYPE_BUILDING]));
            arrayList.add(match);
        }
        return sortAndMergePosList(arrayList, str);
    }

    private String[] searSpot(String str, int i) {
        int i2 = 0;
        int length = str.length();
        String str2 = "";
        String[] strArr = new String[8];
        str2 = "";
        str2 = "";
        Pattern compile = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern compile2 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern compile3 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        str2 = "";
        int length2 = str.length();
        int i3 = 0;
        while (i3 < length) {
            str = str.substring(i3, length);
            int length3 = i + (length2 - str.length());
            int i4 = length - i3;
            i3 = 0;
            length = DicSearch.dicsearch(TYPE_BUILDING2, str.toLowerCase(Locale.getDefault()));
            if (length != 0) {
                Object substring = str.substring(0, length);
                CharSequence substring2 = str.substring(length, str.length());
                int searchBracket = searchBracket(substring2);
                if (searchBracket > 0) {
                    substring = new StringBuilder(String.valueOf(substring)).append(substring2.substring(0, searchBracket)).toString();
                    substring2 = substring2.substring(searchBracket, substring2.length());
                }
                String str3 = "";
                str3 = "";
                Matcher matcher = this.p52s.matcher(substring2);
                Matcher matcher2;
                String str4;
                if (!matcher.lookingAt()) {
                    Matcher matcher3 = this.p2s.matcher(substring2);
                    if (!matcher3.lookingAt()) {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = substring;
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    } else if (matcher3.group(3) == null) {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = substring;
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    } else if (matcher3.group(4) == null) {
                        matcher2 = compile.matcher(matcher3.group(3));
                        if (!matcher2.matches()) {
                            str4 = "";
                        } else if (matcher2.group(TYPE_BUILDING) == null) {
                            str4 = "";
                        } else {
                            str4 = matcher2.group(TYPE_BUILDING);
                        }
                        String searCity = searCity(matcher3.group(3).substring(str4.length(), matcher3.group(3).length()), TYPE_BUILDING2);
                        if (searCity != null) {
                            str4 = new StringBuilder(String.valueOf(str4)).append(searCity).toString();
                            if (matcher3.group(6) != null) {
                                if (matcher3.group(TYPE_BUILDING2) != null) {
                                    str4 = matcher3.group(TYPE_BUILDING2) + matcher3.group(3) + matcher3.group(5) + matcher3.group(6);
                                } else {
                                    str4 = matcher3.group(3) + matcher3.group(5) + matcher3.group(6);
                                }
                            } else if (matcher3.group(TYPE_BUILDING2) != null) {
                                str4 = matcher3.group(TYPE_BUILDING2) + str4;
                            }
                            str3 = matcher3.group(TYPE_BUILDING) + str4;
                            length = i2 + TYPE_BUILDING;
                            strArr[i2] = new StringBuilder(String.valueOf(substring)).append(str3).toString();
                            this.match_index_2.add(Integer.valueOf(length3));
                            i3 = length;
                        } else if (compile3.matcher(matcher3.group(TYPE_BUILDING)).matches()) {
                            length = i2 + TYPE_BUILDING;
                            strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher3.group()).toString();
                            this.match_index_2.add(Integer.valueOf(length3));
                            i3 = length;
                        } else if (compile2.matcher(matcher3.group(3)).matches()) {
                            length = i2 + TYPE_BUILDING;
                            strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher3.group()).toString();
                            this.match_index_2.add(Integer.valueOf(length3));
                            i3 = length;
                        } else {
                            length = i2 + TYPE_BUILDING;
                            strArr[i2] = substring;
                            this.match_index_2.add(Integer.valueOf(length3));
                            i3 = length;
                        }
                    } else {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher3.group()).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    }
                } else if (matcher.group(6) == null) {
                    length = i2 + TYPE_BUILDING;
                    strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group()).toString();
                    this.match_index_2.add(Integer.valueOf(length3));
                    i3 = length;
                } else if (matcher.group(7) == null) {
                    matcher2 = compile.matcher(matcher.group(6));
                    if (!matcher2.matches()) {
                        str4 = "";
                    } else if (matcher2.group(TYPE_BUILDING) == null) {
                        str4 = "";
                    } else {
                        str4 = matcher2.group(TYPE_BUILDING);
                    }
                    str3 = searCity(matcher.group(6).substring(str4.length(), matcher.group(6).length()), TYPE_BUILDING2);
                    if (str3 != null) {
                        str4 = new StringBuilder(String.valueOf(str4)).append(str3).toString();
                        if (matcher.group(8) != null) {
                            if (matcher.group(5) != null) {
                                str4 = matcher.group(5) + matcher.group(6) + matcher.group(8);
                            } else {
                                str4 = matcher.group(6) + matcher.group(8);
                            }
                        } else if (matcher.group(5) != null) {
                            str4 = matcher.group(5) + str4;
                        }
                        searchBracket = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group(TYPE_BUILDING)).append(matcher.group(TYPE_BUILDING2)).append(matcher.group(4)).append(str4).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = searchBracket;
                    } else if (compile3.matcher(matcher.group(4)).matches()) {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group()).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    } else if (compile2.matcher(matcher.group(3)).matches()) {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group()).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    } else {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group(TYPE_BUILDING)).append(matcher.group(TYPE_BUILDING2)).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        i3 = length;
                    }
                } else {
                    length = i2 + TYPE_BUILDING;
                    strArr[i2] = new StringBuilder(String.valueOf(substring)).append(matcher.group()).toString();
                    this.match_index_2.add(Integer.valueOf(length3));
                    i3 = length;
                }
                length = i3;
                i3 = (strArr[i3 - 1].length() + 0) - 1;
            } else {
                while (i3 < i4) {
                    if (str.charAt(i3) < 'a' || str.charAt(i3) > 'z') {
                        if (str.charAt(i3) < 'A' || str.charAt(i3) > 'Z') {
                            if (str.charAt(i3) < '0') {
                                length = i2;
                                break;
                            } else if (str.charAt(i3) > '9') {
                                length = i2;
                                break;
                            }
                        }
                    }
                    i3 += TYPE_BUILDING;
                }
                length = i2;
            }
            i3 += TYPE_BUILDING;
            i2 = length;
            length = i4;
        }
        if (i2 >= 8) {
            return strArr;
        }
        String[] strArr2 = new String[i2];
        for (i3 = 0; i3 < i2; i3 += TYPE_BUILDING) {
            strArr2[i3] = strArr[i3];
        }
        return strArr2;
    }

    private int max(int i, int i2) {
        if (i <= i2) {
            return i2;
        }
        return i;
    }

    public String[] searBuilding(String str, int i) {
        String str2 = "";
        boolean z = true;
        if (stanWri(str)) {
            z = false;
        }
        return searBuilding_suf(str, str2, 0, z, i);
    }

    private String[] searBuilding_suf(String str, String str2, int i, boolean z, int i2) {
        String[] searBuilding_suf;
        String[] divStr;
        int length;
        String str3 = "";
        String[] strArr = new String[8];
        String[] strArr2 = new String[0];
        String[] strArr3 = new String[0];
        int i3 = 0;
        str3 = "";
        str3 = "";
        str3 = "";
        str3 = "";
        String str4 = "";
        Matcher matcher = this.pNot_1.matcher(str);
        if (matcher.lookingAt()) {
            matcher = this.pNot_2.matcher(matcher.group(TYPE_BUILDING));
            if (matcher.lookingAt()) {
                int length2 = matcher.group().length();
                str = str.substring(length2, str.length());
                i2 += length2;
            }
        }
        Matcher matcher2 = this.pLocation.matcher(str);
        if (matcher2.find()) {
            String group = matcher2.group(TYPE_BUILDING);
            matcher = this.pNo.matcher(group);
            String group2;
            if (group.length() <= 0 || !noBlank(group)) {
                group2 = matcher2.group();
                str4 = str.substring(group2.length(), str.length());
                if (noBlank(str4)) {
                    searBuilding_suf = searBuilding_suf(str4, group2, TYPE_BUILDING, z, i2 + (str.length() - str4.length()));
                } else {
                    searBuilding_suf = strArr2;
                }
            } else if (matcher.matches() && matcher2.group(3) == null) {
                group2 = matcher2.group();
                str4 = str.substring(group2.length(), str.length());
                if (noBlank(str4)) {
                    searBuilding_suf = searBuilding_suf(str4, group2, TYPE_BUILDING, z, i2 + (str.length() - str4.length()));
                } else {
                    searBuilding_suf = strArr2;
                }
            } else {
                Matcher matcher3;
                String group3;
                matcher = this.pComma.matcher(group);
                Object obj;
                CharSequence charSequence;
                if (matcher.find()) {
                    group2 = matcher.group(TYPE_BUILDING);
                    if (group2 != null && noBlank(group2) && divStr(group2).length <= 4) {
                        str3 = new StringBuilder(String.valueOf(group2)).append(matcher2.group(TYPE_BUILDING2)).toString();
                        this.match_index_2.add(Integer.valueOf(matcher.start(TYPE_BUILDING) + i2));
                    }
                    if (str3.length() == 0 && z) {
                        obj = TYPE_BUILDING;
                        charSequence = group;
                        while (obj != null) {
                            matcher3 = this.pPre_uni.matcher(charSequence);
                            if (matcher3.find()) {
                                group3 = matcher3.group(TYPE_BUILDING2);
                                if (group3 == null || !noBlank(group3)) {
                                    obj = null;
                                } else if (divStr(group3).length > 4) {
                                    charSequence = group3;
                                } else {
                                    str4 = new StringBuilder(String.valueOf(group3)).append(matcher2.group(TYPE_BUILDING2)).toString();
                                    this.match_index_2.add(Integer.valueOf((group.length() - group3.length()) + i2));
                                    str3 = str4;
                                    obj = null;
                                }
                            } else {
                                obj = null;
                            }
                        }
                        if (str3.length() == 0) {
                            divStr = divStr(group);
                            length = divStr.length;
                            if (length <= 4) {
                                if (length > 0) {
                                    str3 = new StringBuilder(String.valueOf(group)).append(matcher2.group(TYPE_BUILDING2)).toString();
                                }
                                this.match_index_2.add(Integer.valueOf(i2));
                            } else {
                                str3 = divStr[length - 4] + divStr[length - 3] + divStr[length - 2] + divStr[length - 1] + matcher2.group(TYPE_BUILDING2);
                                this.match_index_2.add(Integer.valueOf((group.length() - (str3.length() - matcher2.group(TYPE_BUILDING2).length())) + i2));
                            }
                        }
                    }
                } else {
                    obj = TYPE_BUILDING;
                    charSequence = group;
                    while (obj != null) {
                        matcher3 = this.pPre_uni.matcher(charSequence);
                        if (matcher3.find()) {
                            group3 = matcher3.group(TYPE_BUILDING2);
                            if (group3 == null || !noBlank(group3)) {
                                obj = null;
                            } else if (divStr(group3).length > 4) {
                                charSequence = group3;
                            } else {
                                str4 = new StringBuilder(String.valueOf(group3)).append(matcher2.group(TYPE_BUILDING2)).toString();
                                this.match_index_2.add(Integer.valueOf((group.length() - group3.length()) + i2));
                                str3 = str4;
                                obj = null;
                            }
                        } else {
                            obj = null;
                        }
                    }
                    if (str3.length() == 0) {
                        divStr = divStr(group);
                        length = divStr.length;
                        if (length <= 4) {
                            if (length > 0) {
                                str3 = new StringBuilder(String.valueOf(group)).append(matcher2.group(TYPE_BUILDING2)).toString();
                            }
                            this.match_index_2.add(Integer.valueOf(i2));
                        } else {
                            str3 = divStr[length - 4] + divStr[length - 3] + divStr[length - 2] + divStr[length - 1] + matcher2.group(TYPE_BUILDING2);
                            this.match_index_2.add(Integer.valueOf((group.length() - (str3.length() - matcher2.group(TYPE_BUILDING2).length())) + i2));
                        }
                    }
                }
                if (str3.length() == 0 && matcher2.group(3) != null) {
                    str3 = matcher2.group(TYPE_BUILDING2);
                    this.match_index_2.add(Integer.valueOf(matcher2.group(TYPE_BUILDING).length() + i2));
                }
                if (str3.length() <= 0) {
                    group2 = matcher2.group();
                    str4 = str.substring(group2.length(), str.length());
                    if (noBlank(str4)) {
                        searBuilding_suf = searBuilding_suf(str4, group2, TYPE_BUILDING, z, i2 + (str.length() - str4.length()));
                    } else {
                        searBuilding_suf = strArr2;
                    }
                } else {
                    int i4;
                    group3 = matcher2.group();
                    if (group3.length() <= str3.length()) {
                        length2 = 0;
                    } else {
                        length2 = group3.length() - str3.length();
                    }
                    str4 = group3.substring(0, length2);
                    if (i == TYPE_BUILDING) {
                        str4 = new StringBuilder(String.valueOf(str2)).append(str4).toString();
                    }
                    if (noBlank(str4)) {
                        divStr = searBuilding_dic(str4, i2 - str2.length());
                        i4 = TYPE_BUILDING2;
                        group2 = "";
                        strArr3 = divStr;
                    } else {
                        i4 = i;
                        group2 = str2;
                    }
                    str4 = str.substring(group3.length(), str.length());
                    if (noBlank(str4)) {
                        String str5 = "";
                        str5 = "";
                        matcher2 = this.p52s.matcher(str4);
                        if (!matcher2.lookingAt()) {
                            matcher2 = this.p2s.matcher(str4);
                            if (!matcher2.lookingAt()) {
                                i3 = TYPE_BUILDING;
                                strArr[0] = str3;
                                searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                            } else if (matcher2.group(3) == null) {
                                i3 = TYPE_BUILDING;
                                strArr[0] = str3;
                                if (noBlank(str4)) {
                                    searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                                } else {
                                    searBuilding_suf = strArr2;
                                }
                            } else if (matcher2.group(4) == null) {
                                matcher3 = this.pCut.matcher(matcher2.group(3));
                                if (!matcher3.matches()) {
                                    group3 = "";
                                } else if (matcher3.group(TYPE_BUILDING) == null) {
                                    group3 = "";
                                } else {
                                    group3 = matcher3.group(TYPE_BUILDING);
                                }
                                str5 = searCity(matcher2.group(3).substring(group3.length(), matcher2.group(3).length()), TYPE_BUILDING2);
                                if (str5 != null) {
                                    group3 = new StringBuilder(String.valueOf(group3)).append(str5).toString();
                                    if (matcher2.group(6) != null) {
                                        if (matcher2.group(TYPE_BUILDING2) != null) {
                                            group3 = matcher2.group(TYPE_BUILDING2) + matcher2.group(3) + matcher2.group(5) + matcher2.group(6);
                                        } else {
                                            group3 = matcher2.group(3) + matcher2.group(5) + matcher2.group(6);
                                        }
                                    } else if (matcher2.group(TYPE_BUILDING2) != null) {
                                        group3 = matcher2.group(TYPE_BUILDING2) + group3;
                                    }
                                    group3 = matcher2.group(TYPE_BUILDING) + group3;
                                } else {
                                    group3 = !this.pPre_city.matcher(matcher2.group(TYPE_BUILDING)).lookingAt() ? !this.pSingle.matcher(matcher2.group(3)).matches() ? "" : matcher2.group() : matcher2.group();
                                }
                                i3 = TYPE_BUILDING;
                                strArr[0] = new StringBuilder(String.valueOf(str3)).append(group3).toString();
                                str4 = str4.substring(group3.length(), str4.length());
                                if (noBlank(str4)) {
                                    searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                                } else {
                                    searBuilding_suf = strArr2;
                                }
                            } else {
                                i3 = TYPE_BUILDING;
                                strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group()).toString();
                                str4 = str4.substring(matcher2.group().length(), str4.length());
                                if (noBlank(str4)) {
                                    searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                                } else {
                                    searBuilding_suf = strArr2;
                                }
                            }
                        } else if (matcher2.group(6) == null) {
                            i3 = TYPE_BUILDING;
                            strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group()).toString();
                            str4 = str4.substring(matcher2.group().length(), str4.length());
                            if (noBlank(str4)) {
                                searBuilding_suf = searBuilding_suf(str4, group2, i4, z, (group3.length() + i2) + matcher2.group().length());
                            } else {
                                searBuilding_suf = strArr2;
                            }
                        } else if (matcher2.group(7) == null) {
                            matcher3 = this.pCut.matcher(matcher2.group(6));
                            if (!matcher3.matches()) {
                                group3 = "";
                            } else if (matcher3.group(TYPE_BUILDING) == null) {
                                group3 = "";
                            } else {
                                group3 = matcher3.group(TYPE_BUILDING);
                            }
                            str5 = searCity(matcher2.group(6).substring(group3.length(), matcher2.group(6).length()), TYPE_BUILDING2);
                            if (str5 != null) {
                                group3 = new StringBuilder(String.valueOf(group3)).append(str5).toString();
                                if (matcher2.group(8) != null) {
                                    if (matcher2.group(5) != null) {
                                        group3 = matcher2.group(5) + matcher2.group(6) + matcher2.group(8);
                                    } else {
                                        group3 = matcher2.group(6) + matcher2.group(8);
                                    }
                                } else if (matcher2.group(5) != null) {
                                    group3 = matcher2.group(5) + group3;
                                }
                                i3 = TYPE_BUILDING;
                                strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group(TYPE_BUILDING)).append(matcher2.group(TYPE_BUILDING2)).append(matcher2.group(4)).append(group3).toString();
                                str4 = str4.substring(((matcher2.group(TYPE_BUILDING).length() + matcher2.group(TYPE_BUILDING2).length()) + matcher2.group(4).length()) + group3.length(), str4.length());
                                if (noBlank(str4)) {
                                    searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                                } else {
                                    searBuilding_suf = strArr2;
                                }
                            } else {
                                if (this.pPre_city.matcher(matcher2.group(4)).lookingAt()) {
                                    strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group()).toString();
                                    str4 = str4.substring(matcher2.group().length(), str4.length());
                                    i3 = TYPE_BUILDING;
                                } else if (this.pSingle.matcher(matcher2.group(3)).matches()) {
                                    strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group()).toString();
                                    str4 = str4.substring(matcher2.group().length(), str4.length());
                                    i3 = TYPE_BUILDING;
                                } else {
                                    strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group(TYPE_BUILDING)).append(matcher2.group(TYPE_BUILDING2)).toString();
                                    str4 = str4.substring(matcher2.group(TYPE_BUILDING).length() + matcher2.group(TYPE_BUILDING2).length(), str4.length());
                                    i3 = TYPE_BUILDING;
                                }
                                if (noBlank(str4)) {
                                    searBuilding_suf = searBuilding_suf(str4, group2, i4, z, i2 + (str.length() - str4.length()));
                                } else {
                                    searBuilding_suf = strArr2;
                                }
                            }
                        } else {
                            i3 = TYPE_BUILDING;
                            strArr[0] = new StringBuilder(String.valueOf(str3)).append(matcher2.group()).toString();
                            str4 = str4.substring(matcher2.group().length(), str4.length());
                            if (noBlank(str4)) {
                                searBuilding_suf = searBuilding_suf(str4, group2, i4, z, (group3.length() + i2) + matcher2.group().length());
                            } else {
                                searBuilding_suf = strArr2;
                            }
                        }
                    } else {
                        i3 = TYPE_BUILDING;
                        strArr[0] = str3;
                        searBuilding_suf = strArr2;
                    }
                }
            }
        } else {
            if (i == TYPE_BUILDING) {
                str = new StringBuilder(String.valueOf(str2)).append(str).toString();
            }
            searBuilding_suf = searBuilding_dic(str, i2 - str2.length());
        }
        if (strArr3.length > 0) {
            length2 = 0;
            while (length2 < strArr3.length) {
                length = i3 + TYPE_BUILDING;
                strArr[i3] = strArr3[length2];
                length2 += TYPE_BUILDING;
                i3 = length;
            }
        }
        if (searBuilding_suf.length > 0) {
            length2 = 0;
            while (length2 < searBuilding_suf.length) {
                length = i3 + TYPE_BUILDING;
                strArr[i3] = searBuilding_suf[length2];
                length2 += TYPE_BUILDING;
                i3 = length;
            }
        }
        if (i3 >= 8) {
            return strArr;
        }
        divStr = new String[i3];
        for (int i5 = 0; i5 < i3; i5 += TYPE_BUILDING) {
            divStr[i5] = strArr[i5];
        }
        return divStr;
    }

    private String[] searBuilding_dic(String str, int i) {
        int i2 = 0;
        int length = str.length();
        String str2 = "";
        String[] strArr = new String[8];
        str2 = "";
        str2 = "";
        str2 = "";
        Pattern compile = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])((?i)(in|at|from|near|to|reach))\\b(\\s+(?i)the\\b)?(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+)?");
        Pattern compile2 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern compile3 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern compile4 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        str2 = "";
        Object obj = TYPE_BUILDING;
        int length2 = str.length();
        int i3 = 0;
        int i4 = length;
        String str3 = str;
        while (i3 < i4) {
            Object obj2;
            int i5;
            Object obj3;
            String substring;
            str = str.substring(i3, i4);
            int length3 = i + (length2 - str.length());
            i4 -= i3;
            i3 = 0;
            CharSequence substring2 = str3.substring(0, str3.length() - i4);
            int dicsearch = DicSearch.dicsearch(TYPE_BUILDING, str.toLowerCase(Locale.getDefault()));
            if (dicsearch != 0) {
                str2 = str.substring(0, dicsearch);
                CharSequence substring3 = str.substring(dicsearch, str.length());
                dicsearch = searchBracket(substring3);
                if (dicsearch > 0) {
                    str2 = new StringBuilder(String.valueOf(str2)).append(substring3.substring(0, dicsearch)).toString();
                    substring3 = substring3.substring(dicsearch, substring3.length());
                }
                String str4 = "";
                str4 = "";
                Matcher matcher = this.p52s.matcher(substring3);
                Matcher matcher2;
                String searCity;
                String str5;
                String valueOf;
                if (!matcher.lookingAt()) {
                    matcher = this.p2s.matcher(substring3);
                    if (matcher.lookingAt()) {
                        if (matcher.group(3) != null) {
                            if (matcher.group(4) == null) {
                                matcher2 = compile2.matcher(matcher.group(3));
                                if (!matcher2.matches()) {
                                    str3 = "";
                                } else if (matcher2.group(TYPE_BUILDING) == null) {
                                    str3 = "";
                                } else {
                                    str3 = matcher2.group(TYPE_BUILDING);
                                }
                                searCity = searCity(matcher.group(3).substring(str3.length(), matcher.group(3).length()), TYPE_BUILDING2);
                                if (searCity != null) {
                                    str3 = new StringBuilder(String.valueOf(str3)).append(searCity).toString();
                                    if (matcher.group(6) != null) {
                                        if (matcher.group(TYPE_BUILDING2) != null) {
                                            str3 = matcher.group(TYPE_BUILDING2) + matcher.group(3) + matcher.group(5) + matcher.group(6);
                                        } else {
                                            str3 = matcher.group(3) + matcher.group(5) + matcher.group(6);
                                        }
                                    } else if (matcher.group(TYPE_BUILDING2) != null) {
                                        str3 = matcher.group(TYPE_BUILDING2) + str3;
                                    }
                                    str5 = matcher.group(TYPE_BUILDING) + str3;
                                    length = i2 + TYPE_BUILDING;
                                    strArr[i2] = new StringBuilder(String.valueOf(str2)).append(str5).toString();
                                    this.match_index_2.add(Integer.valueOf(length3));
                                    obj2 = obj;
                                    i5 = length;
                                    obj3 = obj2;
                                } else if (compile4.matcher(matcher.group(TYPE_BUILDING)).matches()) {
                                    length = i2 + TYPE_BUILDING;
                                    valueOf = String.valueOf(str2);
                                    strArr[i2] = matcher.group();
                                    this.match_index_2.add(Integer.valueOf(length3));
                                    obj2 = obj;
                                    i5 = length;
                                    obj3 = obj2;
                                } else if (compile3.matcher(matcher.group(3)).matches()) {
                                    length = i2 + TYPE_BUILDING;
                                    valueOf = String.valueOf(str2);
                                    strArr[i2] = matcher.group();
                                    this.match_index_2.add(Integer.valueOf(length3));
                                    obj2 = obj;
                                    i5 = length;
                                    obj3 = obj2;
                                } else if (compile.matcher(substring2).matches()) {
                                    length = i2 + TYPE_BUILDING;
                                    strArr[i2] = str2;
                                    this.match_index_2.add(Integer.valueOf(length3));
                                    obj2 = obj;
                                    i5 = length;
                                    obj3 = obj2;
                                } else {
                                    obj3 = null;
                                    i5 = i2;
                                }
                            } else {
                                length = i2 + TYPE_BUILDING;
                                valueOf = String.valueOf(str2);
                                strArr[i2] = matcher.group();
                                this.match_index_2.add(Integer.valueOf(length3));
                                obj2 = obj;
                                i5 = length;
                                obj3 = obj2;
                            }
                        } else if (compile.matcher(substring2).matches()) {
                            length = i2 + TYPE_BUILDING;
                            strArr[i2] = str2;
                            this.match_index_2.add(Integer.valueOf(length3));
                            obj2 = obj;
                            i5 = length;
                            obj3 = obj2;
                        } else {
                            obj3 = null;
                            i5 = i2;
                        }
                    } else if (compile.matcher(substring2).matches()) {
                        length = i2 + TYPE_BUILDING;
                        strArr[i2] = str2;
                        this.match_index_2.add(Integer.valueOf(length3));
                        obj2 = obj;
                        i5 = length;
                        obj3 = obj2;
                    } else {
                        obj3 = null;
                        i5 = i2;
                    }
                } else if (matcher.group(6) == null) {
                    length = i2 + TYPE_BUILDING;
                    valueOf = String.valueOf(str2);
                    strArr[i2] = matcher.group();
                    this.match_index_2.add(Integer.valueOf(length3));
                    obj2 = obj;
                    i5 = length;
                    obj3 = obj2;
                } else if (matcher.group(7) == null) {
                    matcher2 = compile2.matcher(matcher.group(6));
                    if (!matcher2.matches()) {
                        str3 = "";
                    } else if (matcher2.group(TYPE_BUILDING) == null) {
                        str3 = "";
                    } else {
                        str3 = matcher2.group(TYPE_BUILDING);
                    }
                    str5 = searCity(matcher.group(6).substring(str3.length(), matcher.group(6).length()), TYPE_BUILDING2);
                    if (str5 != null) {
                        str3 = new StringBuilder(String.valueOf(str3)).append(str5).toString();
                        if (matcher.group(8) != null) {
                            if (matcher.group(5) != null) {
                                str3 = matcher.group(5) + matcher.group(6) + matcher.group(8);
                            } else {
                                str3 = matcher.group(6) + matcher.group(8);
                            }
                        } else if (matcher.group(5) != null) {
                            str3 = matcher.group(5) + str3;
                        }
                        int i6 = i2 + TYPE_BUILDING;
                        strArr[i2] = new StringBuilder(String.valueOf(str2)).append(matcher.group(TYPE_BUILDING)).append(matcher.group(TYPE_BUILDING2)).append(matcher.group(4)).append(str3).toString();
                        this.match_index_2.add(Integer.valueOf(length3));
                        obj3 = obj;
                        i5 = i6;
                    } else if (compile4.matcher(matcher.group(4)).matches()) {
                        length = i2 + TYPE_BUILDING;
                        valueOf = String.valueOf(str2);
                        strArr[i2] = matcher.group();
                        this.match_index_2.add(Integer.valueOf(length3));
                        obj2 = obj;
                        i5 = length;
                        obj3 = obj2;
                    } else if (compile3.matcher(matcher.group(3)).matches()) {
                        length = i2 + TYPE_BUILDING;
                        valueOf = String.valueOf(str2);
                        strArr[i2] = matcher.group();
                        this.match_index_2.add(Integer.valueOf(length3));
                        obj2 = obj;
                        i5 = length;
                        obj3 = obj2;
                    } else {
                        length = i2 + TYPE_BUILDING;
                        valueOf = String.valueOf(str2);
                        valueOf = matcher.group(TYPE_BUILDING);
                        strArr[i2] = searCity + matcher.group(TYPE_BUILDING2);
                        this.match_index_2.add(Integer.valueOf(length3));
                        obj2 = obj;
                        i5 = length;
                        obj3 = obj2;
                    }
                } else {
                    length = i2 + TYPE_BUILDING;
                    valueOf = String.valueOf(str2);
                    strArr[i2] = matcher.group();
                    this.match_index_2.add(Integer.valueOf(length3));
                    obj2 = obj;
                    i5 = length;
                    obj3 = obj2;
                }
                if (obj3 == null) {
                    length = (str2.length() + 0) - 1;
                    substring = str.substring(str2.length(), str.length());
                    int i7 = length;
                    obj3 = TYPE_BUILDING;
                    i3 = i7;
                } else {
                    i3 = (strArr[i5 - 1].length() + 0) - 1;
                    substring = str.substring(strArr[i5 - 1].length(), str.length());
                }
            } else {
                while (i3 < i4) {
                    if (str.charAt(i3) < 'a' || str.charAt(i3) > 'z') {
                        if (str.charAt(i3) < 'A' || str.charAt(i3) > 'Z') {
                            if (str.charAt(i3) < '0') {
                                obj2 = obj;
                                i5 = i2;
                                substring = str3;
                                obj3 = obj2;
                                break;
                            } else if (str.charAt(i3) > '9') {
                                obj2 = obj;
                                i5 = i2;
                                substring = str3;
                                obj3 = obj2;
                                break;
                            }
                        }
                    }
                    i3 += TYPE_BUILDING;
                }
                obj2 = obj;
                i5 = i2;
                substring = str3;
                obj3 = obj2;
            }
            i3 += TYPE_BUILDING;
            obj2 = obj3;
            str3 = substring;
            i2 = i5;
            obj = obj2;
        }
        if (i2 >= 8) {
            return strArr;
        }
        String[] strArr2 = new String[i2];
        for (i3 = 0; i3 < i2; i3 += TYPE_BUILDING) {
            strArr2[i3] = strArr[i3];
        }
        return strArr2;
    }

    private boolean noBlank(String str) {
        int length = str.length();
        String toLowerCase = str.toLowerCase(Locale.getDefault());
        int i = 0;
        boolean z = TYPE_BUILDING;
        while (z && i < length) {
            if (toLowerCase.charAt(i) > 'z' || toLowerCase.charAt(i) < 'a') {
                if (toLowerCase.charAt(i) <= '9') {
                    if (toLowerCase.charAt(i) < '0') {
                    }
                }
                i += TYPE_BUILDING;
            }
            z = false;
            i += TYPE_BUILDING;
        }
        if (z) {
            return false;
        }
        return true;
    }

    private String[] divStr(String str) {
        int i;
        String[] strArr = new String[PduHeaders.SUBJECT];
        int length = str.length();
        strArr[0] = "";
        int i2 = 0;
        for (i = 0; i < length; i += TYPE_BUILDING) {
            char charAt = str.charAt(i);
            if (charAt > 'z' || charAt < 'a') {
                if (charAt > 'Z' || charAt < 'A') {
                    if (charAt <= '9') {
                        if (charAt < '0') {
                        }
                    }
                    if (strArr[i2].length() > 0) {
                        strArr[i2] = strArr[i2] + charAt;
                        i2 += TYPE_BUILDING;
                        strArr[i2] = "";
                    } else if (i2 > 0) {
                        int i3 = i2 - 1;
                        strArr[i3] = strArr[i3] + charAt;
                    }
                }
            }
            strArr[i2] = strArr[i2] + charAt;
        }
        if (strArr[i2].length() <= 0) {
            i = i2;
        } else {
            i = i2 + TYPE_BUILDING;
        }
        if (i >= PduHeaders.SUBJECT) {
            return strArr;
        }
        String[] strArr2 = new String[i];
        for (i2 = 0; i2 < i; i2 += TYPE_BUILDING) {
            strArr2[i2] = strArr[i2];
        }
        return strArr2;
    }

    private boolean stanWri(String str) {
        String[] divStr = divStr(str);
        int length = divStr.length;
        int i = 0;
        boolean z = true;
        while (z && i < length) {
            int length2 = divStr[i].length();
            int i2 = TYPE_BUILDING;
            while (z && i2 < length2) {
                char charAt = divStr[i].charAt(i2);
                if (charAt <= 'Z' && charAt >= 'A') {
                    z = false;
                }
                i2 += TYPE_BUILDING;
            }
            if (length > 3) {
                if (i == 0) {
                    i = (length / TYPE_BUILDING2) - 1;
                } else if (i == (length / TYPE_BUILDING2) - 1) {
                    i = length - 2;
                }
            }
            i += TYPE_BUILDING;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String searCity(String str, int i) {
        int length = str.length();
        Matcher matcher = Pattern.compile("([\\s\\S]*(?i)(town|city|county)\\b)(?:.*)").matcher(str);
        int dicsearch;
        if (i != TYPE_BUILDING) {
            if (matcher.find() && noBlank(matcher.group(TYPE_BUILDING).substring(0, matcher.group(TYPE_BUILDING2).length()))) {
                return matcher.group(TYPE_BUILDING);
            }
            dicsearch = DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault()));
            if (dicsearch > 0) {
                Matcher matcher2 = Pattern.compile("(\\s+(?i)(town|city|county))\\b.*").matcher(str.substring(dicsearch, length));
                if (matcher2.matches()) {
                    return str.substring(0, dicsearch) + matcher2.group(TYPE_BUILDING);
                }
                return str.substring(0, dicsearch);
            }
        } else if (matcher.find() && noBlank(matcher.group(TYPE_BUILDING).substring(0, matcher.group(TYPE_BUILDING2).length()))) {
            return str;
        } else {
            dicsearch = length;
            length = 0;
            while (length < dicsearch) {
                str = str.substring(length, dicsearch);
                dicsearch -= length;
                if (DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault())) != 0) {
                    return str;
                }
                length = 0;
                while (length < dicsearch) {
                    if (str.charAt(length) < 'a' || str.charAt(length) > 'z') {
                        if (str.charAt(length) < 'A' || str.charAt(length) > 'Z') {
                            if (str.charAt(length) >= '0' && str.charAt(length) <= '9') {
                            }
                        }
                    }
                    length += TYPE_BUILDING;
                }
                length += TYPE_BUILDING;
            }
        }
        return null;
    }

    public int searchBracket(String str) {
        Matcher matcher = Pattern.compile("(\\s*.?\\s*)\\)").matcher(str);
        if (matcher.lookingAt()) {
            return matcher.group().length();
        }
        return 0;
    }

    public String noShut(String str) {
        Matcher matcher = Pattern.compile("\\s*#").matcher(str);
        if (matcher.lookingAt()) {
            return str.substring(matcher.group().length(), str.length());
        }
        return str;
    }

    private ArrayList<Match> sortAndMergePosList(ArrayList<Match> arrayList, String str) {
        if (arrayList.isEmpty()) {
            return null;
        }
        Collections.sort(arrayList, new Comparator<Match>() {
            public int compare(Match match, Match match2) {
                if (match.getStartPos().compareTo(match2.getStartPos()) != 0) {
                    return match.getStartPos().compareTo(match2.getStartPos());
                }
                return match.getEndPos().compareTo(match2.getEndPos());
            }
        });
        int size = arrayList.size() - 1;
        while (size > 0) {
            if (((Match) arrayList.get(size - 1)).getStartPos().intValue() <= ((Match) arrayList.get(size)).getStartPos().intValue() && ((Match) arrayList.get(size)).getStartPos().intValue() <= ((Match) arrayList.get(size - 1)).getEndPos().intValue()) {
                if (((Match) arrayList.get(size - 1)).getEndPos().intValue() < ((Match) arrayList.get(size)).getEndPos().intValue()) {
                    ((Match) arrayList.get(size - 1)).setEndPos(((Match) arrayList.get(size)).getEndPos());
                    ((Match) arrayList.get(size - 1)).setMatchedAddr(str.substring(((Match) arrayList.get(size - 1)).getStartPos().intValue(), ((Match) arrayList.get(size - 1)).getEndPos().intValue()));
                    arrayList.remove(size);
                } else if (((Match) arrayList.get(size - 1)).getEndPos().intValue() >= ((Match) arrayList.get(size)).getEndPos().intValue()) {
                    arrayList.remove(size);
                }
            }
            size--;
        }
        return arrayList;
    }
}
