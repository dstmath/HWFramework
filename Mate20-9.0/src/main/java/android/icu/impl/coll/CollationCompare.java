package android.icu.impl.coll;

public final class CollationCompare {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    /* JADX WARNING: Code restructure failed: missing block: B:99:0x017c, code lost:
        if (r15 != 1) goto L_0x017f;
     */
    public static int compareUpToQuaternary(CollationIterator left, CollationIterator right, CollationSettings settings) {
        long variableTop;
        long rightPrimary;
        long leftPrimary;
        long leftPrimary2;
        int rightIndex;
        int rightIndex2;
        int rightTertiary;
        long leftQuaternary;
        int rightIndex3;
        long rightQuaternary;
        int anyQuaternaries;
        long rightQuaternary2;
        long variableTop2;
        int leftLower32;
        int rightIndex4;
        int leftIndex;
        int leftCase;
        int rightCase;
        int leftCase2;
        int rightCase2;
        int leftCase3;
        int rightIndex5;
        int rightIndex6;
        int leftSecondary;
        int rightIndex7;
        int rightSecondary;
        CollationIterator collationIterator = left;
        CollationIterator collationIterator2 = right;
        CollationSettings collationSettings = settings;
        int options = collationSettings.options;
        if ((options & 12) == 0) {
            variableTop = 0;
        } else {
            variableTop = collationSettings.variableTop + 1;
        }
        boolean anyVariable = false;
        while (true) {
            long ce = left.nextCE();
            char c = ' ';
            long leftPrimary3 = ce >>> 32;
            if (leftPrimary3 < variableTop && leftPrimary3 > Collation.MERGE_SEPARATOR_PRIMARY) {
                do {
                    collationIterator.setCurrentCE(ce & -4294967296L);
                    while (true) {
                        ce = left.nextCE();
                        leftPrimary3 = ce >>> 32;
                        if (leftPrimary3 != 0) {
                            break;
                        }
                        collationIterator.setCurrentCE(0);
                    }
                    if (leftPrimary3 >= variableTop) {
                        break;
                    }
                } while (leftPrimary3 > Collation.MERGE_SEPARATOR_PRIMARY);
                anyVariable = true;
            }
            if (leftPrimary3 != 0) {
                while (true) {
                    long ce2 = right.nextCE();
                    rightPrimary = ce2 >>> c;
                    if (rightPrimary >= variableTop || rightPrimary <= Collation.MERGE_SEPARATOR_PRIMARY) {
                        leftPrimary = leftPrimary3;
                    } else {
                        while (true) {
                            leftPrimary = leftPrimary3;
                            collationIterator2.setCurrentCE(ce2 & -4294967296L);
                            while (true) {
                                ce2 = right.nextCE();
                                rightPrimary = ce2 >>> 32;
                                if (rightPrimary != 0) {
                                    break;
                                }
                                collationIterator2.setCurrentCE(0);
                            }
                            if (rightPrimary >= variableTop || rightPrimary <= Collation.MERGE_SEPARATOR_PRIMARY) {
                                anyVariable = true;
                            } else {
                                leftPrimary3 = leftPrimary;
                            }
                        }
                        anyVariable = true;
                    }
                    if (rightPrimary != 0) {
                        break;
                    }
                    c = ' ';
                    leftPrimary3 = leftPrimary;
                }
                if (leftPrimary != rightPrimary) {
                    if (settings.hasReordering()) {
                        leftPrimary2 = collationSettings.reorder(leftPrimary);
                        rightPrimary = collationSettings.reorder(rightPrimary);
                    } else {
                        leftPrimary2 = leftPrimary;
                    }
                    return leftPrimary2 < rightPrimary ? -1 : 1;
                } else if (leftPrimary == 1) {
                    if (CollationSettings.getStrength(options) >= 1) {
                        if ((options & 2048) == 0) {
                            int leftIndex2 = 0;
                            int rightIndex8 = 0;
                            while (true) {
                                int leftIndex3 = leftIndex2 + 1;
                                int leftSecondary2 = ((int) collationIterator.getCE(leftIndex2)) >>> 16;
                                if (leftSecondary2 != 0) {
                                    while (true) {
                                        rightIndex7 = rightIndex8 + 1;
                                        rightSecondary = ((int) collationIterator2.getCE(rightIndex8)) >>> 16;
                                        if (rightSecondary != 0) {
                                            break;
                                        }
                                        rightIndex8 = rightIndex7;
                                    }
                                    if (leftSecondary2 != rightSecondary) {
                                        return leftSecondary2 < rightSecondary ? -1 : 1;
                                    } else if (leftSecondary2 == 256) {
                                        break;
                                    } else {
                                        leftIndex2 = leftIndex3;
                                        rightIndex8 = rightIndex7;
                                    }
                                } else {
                                    leftIndex2 = leftIndex3;
                                }
                            }
                        } else {
                            int leftStart = 0;
                            int rightStart = 0;
                            while (true) {
                                int leftLimit = leftStart;
                                while (true) {
                                    long ce3 = collationIterator.getCE(leftLimit) >>> 32;
                                    long p = ce3;
                                    if (ce3 <= Collation.MERGE_SEPARATOR_PRIMARY && p != 0) {
                                        break;
                                    }
                                    leftLimit++;
                                    leftStart = leftStart;
                                    rightStart = rightStart;
                                }
                                int rightLimit = rightStart;
                                while (true) {
                                    long ce4 = collationIterator2.getCE(rightLimit) >>> 32;
                                    long p2 = ce4;
                                    if (ce4 <= Collation.MERGE_SEPARATOR_PRIMARY && p2 != 0) {
                                        break;
                                    }
                                    rightLimit++;
                                    leftStart = leftStart;
                                    rightStart = rightStart;
                                }
                                int leftIndex4 = leftLimit;
                                int rightIndex9 = rightLimit;
                                while (true) {
                                    int leftIndex5 = leftIndex4;
                                    int leftIndex6 = 0;
                                    while (true) {
                                        leftSecondary = leftIndex6;
                                        if (leftSecondary != 0 || leftIndex5 <= leftStart) {
                                            int leftStart2 = leftStart;
                                            int rightSecondary2 = 0;
                                        } else {
                                            leftIndex5--;
                                            leftIndex6 = ((int) collationIterator.getCE(leftIndex5)) >>> 16;
                                            leftStart = leftStart;
                                        }
                                    }
                                    int leftStart22 = leftStart;
                                    int rightSecondary22 = 0;
                                    while (rightSecondary22 == 0 && rightIndex9 > rightStart) {
                                        rightIndex9--;
                                        rightSecondary22 = ((int) collationIterator2.getCE(rightIndex9)) >>> 16;
                                        rightStart = rightStart;
                                        leftIndex5 = leftIndex5;
                                    }
                                    int rightStart2 = rightStart;
                                    int leftIndex7 = leftIndex5;
                                    if (leftSecondary != rightSecondary22) {
                                        return leftSecondary < rightSecondary22 ? -1 : 1;
                                    } else if (leftSecondary == 0) {
                                        break;
                                    } else {
                                        leftStart = leftStart22;
                                        rightStart = rightStart2;
                                        leftIndex4 = leftIndex7;
                                    }
                                }
                                leftStart = leftLimit + 1;
                                rightStart = rightLimit + 1;
                            }
                        }
                    }
                    int i = options & 1024;
                    int i2 = -65536;
                    int i3 = Collation.CASE_MASK;
                    if (i != 0) {
                        int strength = CollationSettings.getStrength(options);
                        int leftCase4 = 0;
                        int rightCase3 = 0;
                        while (true) {
                            if (strength == 0) {
                                while (true) {
                                    leftIndex = leftCase4 + 1;
                                    long ce5 = collationIterator.getCE(leftCase4);
                                    leftCase3 = (int) ce5;
                                    if ((ce5 >>> 32) != 0 && leftCase3 != 0) {
                                        break;
                                    }
                                    leftCase4 = leftIndex;
                                }
                                leftLower32 = leftCase3;
                                leftCase = leftCase3 & i3;
                                while (true) {
                                    rightIndex5 = rightCase3 + 1;
                                    long ce6 = collationIterator2.getCE(rightCase3);
                                    rightIndex6 = (int) ce6;
                                    if ((ce6 >>> 32) != 0 && rightIndex6 != 0) {
                                        break;
                                    }
                                    rightCase3 = rightIndex5;
                                }
                                rightCase = rightIndex6 & i3;
                                variableTop2 = variableTop;
                                rightIndex4 = rightIndex5;
                            } else {
                                while (true) {
                                    leftIndex = leftCase4 + 1;
                                    leftCase2 = (int) collationIterator.getCE(leftCase4);
                                    if ((leftCase2 & -65536) != 0) {
                                        break;
                                    }
                                    leftCase4 = leftIndex;
                                }
                                leftLower32 = leftCase2;
                                leftCase = leftCase2 & i3;
                                while (true) {
                                    rightIndex4 = rightCase3 + 1;
                                    variableTop2 = variableTop;
                                    rightCase2 = (int) collationIterator2.getCE(rightCase3);
                                    if ((rightCase2 & -65536) != 0) {
                                        break;
                                    }
                                    rightCase3 = rightIndex4;
                                    variableTop = variableTop2;
                                }
                                rightCase = rightCase2 & Collation.CASE_MASK;
                            }
                            if (leftCase != rightCase) {
                                if ((options & 256) == 0) {
                                    return leftCase < rightCase ? -1 : 1;
                                }
                                return leftCase < rightCase ? 1 : -1;
                            } else if ((leftLower32 >>> 16) == 256) {
                                break;
                            } else {
                                leftCase4 = leftIndex;
                                rightCase3 = rightIndex4;
                                variableTop = variableTop2;
                                i3 = Collation.CASE_MASK;
                            }
                        }
                    }
                    int i4 = 1;
                    if (CollationSettings.getStrength(options) <= 1) {
                        return 0;
                    }
                    int tertiaryMask = CollationSettings.getTertiaryMask(options);
                    int rightIndex10 = 0;
                    int rightIndex11 = 0;
                    int anyQuaternaries2 = 0;
                    while (true) {
                        int leftIndex8 = rightIndex11 + 1;
                        int leftLower322 = (int) collationIterator.getCE(rightIndex11);
                        anyQuaternaries2 |= leftLower322;
                        int leftTertiary = leftLower322 & tertiaryMask;
                        if (leftTertiary != 0) {
                            while (true) {
                                rightIndex = rightIndex10 + 1;
                                rightIndex2 = (int) collationIterator2.getCE(rightIndex10);
                                anyQuaternaries2 |= rightIndex2;
                                rightTertiary = rightIndex2 & tertiaryMask;
                                if (rightTertiary != 0) {
                                    break;
                                }
                                rightIndex10 = rightIndex;
                            }
                            if (leftTertiary != rightTertiary) {
                                if (CollationSettings.sortsTertiaryUpperCaseFirst(options)) {
                                    if (leftTertiary > 256) {
                                        if ((leftLower322 & i2) != 0) {
                                            leftTertiary ^= Collation.CASE_MASK;
                                        } else {
                                            leftTertiary += 16384;
                                        }
                                    }
                                    if (rightTertiary > 256) {
                                        rightTertiary = (i2 & rightIndex2) != 0 ? rightTertiary ^ Collation.CASE_MASK : rightTertiary + 16384;
                                    }
                                }
                                return leftTertiary < rightTertiary ? -1 : i4;
                            } else if (leftTertiary != 256) {
                                rightIndex11 = leftIndex8;
                                rightIndex10 = rightIndex;
                                i2 = -65536;
                                i4 = 1;
                            } else if (CollationSettings.getStrength(options) <= 2) {
                                return 0;
                            } else {
                                if (!anyVariable && (anyQuaternaries2 & 192) == 0) {
                                    return 0;
                                }
                                int rightIndex12 = 0;
                                int rightIndex13 = 0;
                                while (true) {
                                    int leftIndex9 = rightIndex12 + 1;
                                    long ce7 = collationIterator.getCE(rightIndex12);
                                    long leftQuaternary2 = ce7 & 65535;
                                    if (leftQuaternary2 <= 256) {
                                        leftQuaternary = ce7 >>> 32;
                                    } else {
                                        leftQuaternary = leftQuaternary2 | 4294967103L;
                                    }
                                    long leftQuaternary3 = leftQuaternary;
                                    if (leftQuaternary3 != 0) {
                                        while (true) {
                                            rightIndex3 = rightIndex13 + 1;
                                            long ce8 = collationIterator2.getCE(rightIndex13);
                                            long rightQuaternary3 = ce8 & 65535;
                                            if (rightQuaternary3 <= 256) {
                                                rightQuaternary = ce8 >>> 32;
                                            } else {
                                                rightQuaternary = rightQuaternary3 | 4294967103L;
                                            }
                                            anyQuaternaries = anyQuaternaries2;
                                            rightQuaternary2 = rightQuaternary;
                                            if (rightQuaternary2 != 0) {
                                                break;
                                            }
                                            rightIndex13 = rightIndex3;
                                            anyQuaternaries2 = anyQuaternaries;
                                        }
                                        if (leftQuaternary3 != rightQuaternary2) {
                                            if (settings.hasReordering()) {
                                                leftQuaternary3 = collationSettings.reorder(leftQuaternary3);
                                                rightQuaternary2 = collationSettings.reorder(rightQuaternary2);
                                            }
                                            return leftQuaternary3 < rightQuaternary2 ? -1 : 1;
                                        } else if (leftQuaternary3 == 1) {
                                            return 0;
                                        } else {
                                            rightIndex13 = rightIndex3;
                                            rightIndex12 = leftIndex9;
                                            anyQuaternaries2 = anyQuaternaries;
                                        }
                                    } else {
                                        rightIndex12 = leftIndex9;
                                    }
                                }
                            }
                        } else {
                            rightIndex11 = leftIndex8;
                        }
                    }
                }
            }
        }
    }
}
