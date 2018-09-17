package android.icu.impl.coll;

public final class CollationCompare {
    static final /* synthetic */ boolean -assertionsDisabled = (CollationCompare.class.desiredAssertionStatus() ^ 1);

    public static int compareUpToQuaternary(CollationIterator left, CollationIterator right, CollationSettings settings) {
        long variableTop;
        int options = settings.options;
        if ((options & 12) == 0) {
            variableTop = 0;
        } else {
            variableTop = settings.variableTop + 1;
        }
        boolean anyVariable = false;
        while (true) {
            long ce = left.nextCE();
            long leftPrimary = ce >>> 32;
            if (leftPrimary < variableTop && leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                anyVariable = true;
                do {
                    left.setCurrentCE(-4294967296L & ce);
                    while (true) {
                        ce = left.nextCE();
                        leftPrimary = ce >>> 32;
                        if (leftPrimary != 0) {
                            break;
                        }
                        left.setCurrentCE(0);
                    }
                    if (leftPrimary >= variableTop) {
                        break;
                    }
                } while (leftPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
            }
            if (leftPrimary != 0) {
                long rightPrimary;
                do {
                    ce = right.nextCE();
                    rightPrimary = ce >>> 32;
                    if (rightPrimary < variableTop && rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY) {
                        anyVariable = true;
                        do {
                            right.setCurrentCE(-4294967296L & ce);
                            while (true) {
                                ce = right.nextCE();
                                rightPrimary = ce >>> 32;
                                if (rightPrimary != 0) {
                                    break;
                                }
                                right.setCurrentCE(0);
                            }
                            if (rightPrimary >= variableTop) {
                                break;
                            }
                        } while (rightPrimary > Collation.MERGE_SEPARATOR_PRIMARY);
                    }
                } while (rightPrimary == 0);
                if (leftPrimary != rightPrimary) {
                    if (settings.hasReordering()) {
                        leftPrimary = settings.reorder(leftPrimary);
                        rightPrimary = settings.reorder(rightPrimary);
                    }
                    return leftPrimary < rightPrimary ? -1 : 1;
                } else if (leftPrimary == 1) {
                    int leftIndex;
                    int rightIndex;
                    int leftIndex2;
                    int rightIndex2;
                    int leftLower32;
                    int i;
                    if (CollationSettings.getStrength(options) >= 1) {
                        int leftSecondary;
                        int rightSecondary;
                        if ((options & 2048) == 0) {
                            leftIndex = 0;
                            rightIndex = 0;
                            while (true) {
                                leftIndex2 = leftIndex + 1;
                                leftSecondary = ((int) left.getCE(leftIndex)) >>> 16;
                                if (leftSecondary == 0) {
                                    leftIndex = leftIndex2;
                                } else {
                                    while (true) {
                                        rightIndex2 = rightIndex + 1;
                                        rightSecondary = ((int) right.getCE(rightIndex)) >>> 16;
                                        if (rightSecondary != 0) {
                                            break;
                                        }
                                        rightIndex = rightIndex2;
                                    }
                                    if (leftSecondary != rightSecondary) {
                                        return leftSecondary < rightSecondary ? -1 : 1;
                                    } else if (leftSecondary == 256) {
                                        break;
                                    } else {
                                        rightIndex = rightIndex2;
                                        leftIndex = leftIndex2;
                                    }
                                }
                            }
                        } else {
                            int leftStart = 0;
                            int rightStart = 0;
                            while (true) {
                                long p;
                                int leftLimit = leftStart;
                                while (true) {
                                    p = left.getCE(leftLimit) >>> 32;
                                    if (p <= Collation.MERGE_SEPARATOR_PRIMARY && p != 0) {
                                        break;
                                    }
                                    leftLimit++;
                                }
                                int rightLimit = rightStart;
                                while (true) {
                                    p = right.getCE(rightLimit) >>> 32;
                                    if (p <= Collation.MERGE_SEPARATOR_PRIMARY && p != 0) {
                                        break;
                                    }
                                    rightLimit++;
                                }
                                leftIndex = leftLimit;
                                rightIndex = rightLimit;
                                do {
                                    leftSecondary = 0;
                                    while (leftSecondary == 0 && leftIndex > leftStart) {
                                        leftIndex--;
                                        leftSecondary = ((int) left.getCE(leftIndex)) >>> 16;
                                    }
                                    rightSecondary = 0;
                                    while (rightSecondary == 0 && rightIndex > rightStart) {
                                        rightIndex--;
                                        rightSecondary = ((int) right.getCE(rightIndex)) >>> 16;
                                    }
                                    if (leftSecondary != rightSecondary) {
                                        return leftSecondary < rightSecondary ? -1 : 1;
                                    }
                                } while (leftSecondary != 0);
                                if (-assertionsDisabled || left.getCE(leftLimit) == right.getCE(rightLimit)) {
                                    if (p == 1) {
                                        break;
                                    }
                                    leftStart = leftLimit + 1;
                                    rightStart = rightLimit + 1;
                                } else {
                                    throw new AssertionError();
                                }
                            }
                        }
                    }
                    if ((options & 1024) != 0) {
                        int strength = CollationSettings.getStrength(options);
                        leftIndex = 0;
                        rightIndex = 0;
                        do {
                            int leftCase;
                            int rightCase;
                            if (strength == 0) {
                                while (true) {
                                    leftIndex2 = leftIndex + 1;
                                    ce = left.getCE(leftIndex);
                                    leftCase = (int) ce;
                                    if ((ce >>> 32) != 0 && leftCase != 0) {
                                        break;
                                    }
                                    leftIndex = leftIndex2;
                                }
                                leftLower32 = leftCase;
                                leftCase &= Collation.CASE_MASK;
                                while (true) {
                                    rightIndex2 = rightIndex + 1;
                                    ce = right.getCE(rightIndex);
                                    rightCase = (int) ce;
                                    if ((ce >>> 32) != 0 && rightCase != 0) {
                                        break;
                                    }
                                    rightIndex = rightIndex2;
                                }
                                rightCase &= Collation.CASE_MASK;
                                rightIndex = rightIndex2;
                                leftIndex = leftIndex2;
                            } else {
                                while (true) {
                                    leftIndex2 = leftIndex + 1;
                                    leftCase = (int) left.getCE(leftIndex);
                                    if ((-65536 & leftCase) != 0) {
                                        break;
                                    }
                                    leftIndex = leftIndex2;
                                }
                                leftLower32 = leftCase;
                                leftCase &= Collation.CASE_MASK;
                                while (true) {
                                    rightIndex2 = rightIndex + 1;
                                    rightCase = (int) right.getCE(rightIndex);
                                    if ((-65536 & rightCase) != 0) {
                                        break;
                                    }
                                    rightIndex = rightIndex2;
                                }
                                rightCase &= Collation.CASE_MASK;
                                rightIndex = rightIndex2;
                                leftIndex = leftIndex2;
                            }
                            if (leftCase != rightCase) {
                                if ((options & 256) == 0) {
                                    if (leftCase < rightCase) {
                                        i = -1;
                                    } else {
                                        i = 1;
                                    }
                                    return i;
                                }
                                return leftCase < rightCase ? 1 : -1;
                            }
                        } while ((leftLower32 >>> 16) != 256);
                    }
                    if (CollationSettings.getStrength(options) <= 1) {
                        return 0;
                    }
                    int tertiaryMask = CollationSettings.getTertiaryMask(options);
                    leftIndex = 0;
                    rightIndex = 0;
                    int anyQuaternaries = 0;
                    while (true) {
                        leftIndex2 = leftIndex + 1;
                        leftLower32 = (int) left.getCE(leftIndex);
                        anyQuaternaries |= leftLower32;
                        if (-assertionsDisabled || (leftLower32 & Collation.ONLY_TERTIARY_MASK) != 0 || (Collation.CASE_AND_QUATERNARY_MASK & leftLower32) == 0) {
                            int leftTertiary = leftLower32 & tertiaryMask;
                            if (leftTertiary == 0) {
                                leftIndex = leftIndex2;
                            } else {
                                while (true) {
                                    rightIndex2 = rightIndex + 1;
                                    int rightLower32 = (int) right.getCE(rightIndex);
                                    anyQuaternaries |= rightLower32;
                                    if (-assertionsDisabled || (rightLower32 & Collation.ONLY_TERTIARY_MASK) != 0 || (Collation.CASE_AND_QUATERNARY_MASK & rightLower32) == 0) {
                                        int rightTertiary = rightLower32 & tertiaryMask;
                                        if (rightTertiary == 0) {
                                            rightIndex = rightIndex2;
                                        } else if (leftTertiary != rightTertiary) {
                                            if (CollationSettings.sortsTertiaryUpperCaseFirst(options)) {
                                                if (leftTertiary > 256) {
                                                    if ((-65536 & leftLower32) != 0) {
                                                        leftTertiary ^= Collation.CASE_MASK;
                                                    } else {
                                                        leftTertiary += 16384;
                                                    }
                                                }
                                                if (rightTertiary > 256) {
                                                    rightTertiary = (-65536 & rightLower32) != 0 ? rightTertiary ^ Collation.CASE_MASK : rightTertiary + 16384;
                                                }
                                            }
                                            if (leftTertiary < rightTertiary) {
                                                i = -1;
                                            } else {
                                                i = 1;
                                            }
                                            return i;
                                        } else if (leftTertiary != 256) {
                                            rightIndex = rightIndex2;
                                            leftIndex = leftIndex2;
                                        } else if (CollationSettings.getStrength(options) <= 2) {
                                            return 0;
                                        } else {
                                            if (!anyVariable && (anyQuaternaries & 192) == 0) {
                                                return 0;
                                            }
                                            leftIndex = 0;
                                            rightIndex = 0;
                                            while (true) {
                                                leftIndex2 = leftIndex + 1;
                                                ce = left.getCE(leftIndex);
                                                long leftQuaternary = ce & 65535;
                                                if (leftQuaternary <= 256) {
                                                    leftQuaternary = ce >>> 32;
                                                } else {
                                                    leftQuaternary |= 4294967103L;
                                                }
                                                if (leftQuaternary == 0) {
                                                    leftIndex = leftIndex2;
                                                } else {
                                                    long rightQuaternary;
                                                    while (true) {
                                                        rightIndex2 = rightIndex + 1;
                                                        ce = right.getCE(rightIndex);
                                                        rightQuaternary = ce & 65535;
                                                        if (rightQuaternary <= 256) {
                                                            rightQuaternary = ce >>> 32;
                                                        } else {
                                                            rightQuaternary |= 4294967103L;
                                                        }
                                                        if (rightQuaternary != 0) {
                                                            break;
                                                        }
                                                        rightIndex = rightIndex2;
                                                    }
                                                    if (leftQuaternary != rightQuaternary) {
                                                        if (settings.hasReordering()) {
                                                            leftQuaternary = settings.reorder(leftQuaternary);
                                                            rightQuaternary = settings.reorder(rightQuaternary);
                                                        }
                                                        return leftQuaternary < rightQuaternary ? -1 : 1;
                                                    } else if (leftQuaternary == 1) {
                                                        return 0;
                                                    } else {
                                                        rightIndex = rightIndex2;
                                                        leftIndex = leftIndex2;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        throw new AssertionError();
                                    }
                                }
                            }
                        }
                        throw new AssertionError();
                    }
                }
            }
        }
    }
}
