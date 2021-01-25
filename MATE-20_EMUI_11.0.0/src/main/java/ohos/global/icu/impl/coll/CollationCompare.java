package ohos.global.icu.impl.coll;

public final class CollationCompare {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0141, code lost:
        if (r17 != 1) goto L_0x0144;
     */
    public static int compareUpToQuaternary(CollationIterator collationIterator, CollationIterator collationIterator2, CollationSettings collationSettings) {
        long j;
        boolean z;
        long j2;
        long j3;
        int i;
        int ce;
        int i2;
        int i3;
        long j4;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int ce2;
        char c;
        long j5;
        int i10;
        int i11;
        long j6;
        int i12;
        int ce3;
        long j7;
        long j8;
        int i13 = collationSettings.options;
        if ((i13 & 12) == 0) {
            j = 0;
        } else {
            j = collationSettings.variableTop + 1;
        }
        boolean z2 = false;
        while (true) {
            long nextCE = collationIterator.nextCE();
            char c2 = ' ';
            long j9 = nextCE >>> 32;
            if (j9 >= j || j9 <= Collation.MERGE_SEPARATOR_PRIMARY) {
                z = z2;
                j2 = j9;
            } else {
                do {
                    collationIterator.setCurrentCE(nextCE & -4294967296L);
                    while (true) {
                        nextCE = collationIterator.nextCE();
                        j8 = nextCE >>> 32;
                        if (j8 != 0) {
                            break;
                        }
                        collationIterator.setCurrentCE(0);
                    }
                    if (j8 >= j) {
                        break;
                    }
                } while (j8 > Collation.MERGE_SEPARATOR_PRIMARY);
                j2 = j8;
                z = true;
            }
            if (j2 != 0) {
                while (true) {
                    long nextCE2 = collationIterator2.nextCE();
                    long j10 = nextCE2 >>> 32;
                    if (j10 >= j || j10 <= Collation.MERGE_SEPARATOR_PRIMARY) {
                        j3 = j10;
                    } else {
                        do {
                            collationIterator2.setCurrentCE(nextCE2 & -4294967296L);
                            while (true) {
                                nextCE2 = collationIterator2.nextCE();
                                j7 = nextCE2 >>> 32;
                                if (j7 != 0) {
                                    break;
                                }
                                collationIterator2.setCurrentCE(0);
                            }
                            if (j7 >= j) {
                                break;
                            }
                        } while (j7 > Collation.MERGE_SEPARATOR_PRIMARY);
                        j3 = j7;
                        z = true;
                    }
                    if (j3 != 0) {
                        break;
                    }
                }
                if (j2 != j3) {
                    if (collationSettings.hasReordering()) {
                        j2 = collationSettings.reorder(j2);
                        j3 = collationSettings.reorder(j3);
                    }
                    if (j2 < j3) {
                        return -1;
                    }
                    return 1;
                } else if (j2 == 1) {
                    if (CollationSettings.getStrength(i13) >= 1) {
                        if ((i13 & 2048) == 0) {
                            int i14 = 0;
                            int i15 = 0;
                            while (true) {
                                int i16 = i14 + 1;
                                int ce4 = ((int) collationIterator.getCE(i14)) >>> 16;
                                if (ce4 != 0) {
                                    while (true) {
                                        i12 = i15 + 1;
                                        ce3 = ((int) collationIterator2.getCE(i15)) >>> 16;
                                        if (ce3 != 0) {
                                            break;
                                        }
                                        i15 = i12;
                                    }
                                    if (ce4 != ce3) {
                                        if (ce4 < ce3) {
                                            return -1;
                                        }
                                        return 1;
                                    } else if (ce4 == 256) {
                                        break;
                                    } else {
                                        i14 = i16;
                                        i15 = i12;
                                    }
                                } else {
                                    i14 = i16;
                                }
                            }
                        } else {
                            int i17 = 0;
                            int i18 = 0;
                            while (true) {
                                int i19 = i17;
                                while (true) {
                                    long ce5 = collationIterator.getCE(i19) >>> c2;
                                    if (ce5 <= Collation.MERGE_SEPARATOR_PRIMARY) {
                                        j6 = 0;
                                        if (ce5 != 0) {
                                            break;
                                        }
                                    }
                                    i19++;
                                    i18 = i18;
                                    c2 = ' ';
                                }
                                int i20 = i18;
                                while (true) {
                                    long ce6 = collationIterator2.getCE(i20) >>> c2;
                                    if (ce6 <= Collation.MERGE_SEPARATOR_PRIMARY && ce6 != j6) {
                                        break;
                                    }
                                    i20++;
                                    i18 = i18;
                                    j6 = 0;
                                    c2 = ' ';
                                }
                                int i21 = i19;
                                int i22 = i20;
                                while (true) {
                                    int i23 = i21;
                                    int i24 = 0;
                                    while (i24 == 0 && i23 > i17) {
                                        i23--;
                                        i24 = ((int) collationIterator.getCE(i23)) >>> 16;
                                    }
                                    int i25 = i22;
                                    int i26 = 0;
                                    while (i26 == 0 && i25 > i18) {
                                        i25--;
                                        i26 = ((int) collationIterator2.getCE(i25)) >>> 16;
                                        i18 = i18;
                                    }
                                    if (i24 != i26) {
                                        if (i24 < i26) {
                                            return -1;
                                        }
                                        return 1;
                                    } else if (i24 == 0) {
                                        break;
                                    } else {
                                        i21 = i23;
                                        i22 = i25;
                                        i18 = i18;
                                    }
                                }
                                i17 = i19 + 1;
                                i18 = i20 + 1;
                                c2 = ' ';
                            }
                        }
                    }
                    if ((i13 & 1024) != 0) {
                        int strength = CollationSettings.getStrength(i13);
                        int i27 = 0;
                        int i28 = 0;
                        while (true) {
                            if (strength == 0) {
                                while (true) {
                                    i5 = i27 + 1;
                                    long ce7 = collationIterator.getCE(i27);
                                    i7 = (int) ce7;
                                    c = ' ';
                                    j5 = 0;
                                    if ((ce7 >>> 32) != 0 && i7 != 0) {
                                        break;
                                    }
                                    i27 = i5;
                                }
                                i4 = i7 & Collation.CASE_MASK;
                                while (true) {
                                    i10 = i28 + 1;
                                    long ce8 = collationIterator2.getCE(i28);
                                    i11 = (int) ce8;
                                    if ((ce8 >>> c) != j5 && i11 != 0) {
                                        break;
                                    }
                                    i28 = i10;
                                    c = ' ';
                                    j5 = 0;
                                }
                                i6 = i11 & Collation.CASE_MASK;
                                i28 = i10;
                            } else {
                                while (true) {
                                    i8 = i27 + 1;
                                    i7 = (int) collationIterator.getCE(i27);
                                    if ((i7 & -65536) != 0) {
                                        break;
                                    }
                                    i27 = i8;
                                }
                                i4 = i7 & Collation.CASE_MASK;
                                while (true) {
                                    i9 = i28 + 1;
                                    ce2 = (int) collationIterator2.getCE(i28);
                                    if ((ce2 & -65536) != 0) {
                                        break;
                                    }
                                    i28 = i9;
                                }
                                i5 = i8;
                                i6 = ce2 & Collation.CASE_MASK;
                                i28 = i9;
                            }
                            if (i4 != i6) {
                                if ((i13 & 256) == 0) {
                                    if (i4 < i6) {
                                        return -1;
                                    }
                                    return 1;
                                } else if (i4 < i6) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            } else if ((i7 >>> 16) == 256) {
                                break;
                            } else {
                                i27 = i5;
                            }
                        }
                    }
                    if (CollationSettings.getStrength(i13) <= 1) {
                        return 0;
                    }
                    int tertiaryMask = CollationSettings.getTertiaryMask(i13);
                    int i29 = 0;
                    int i30 = 0;
                    int i31 = 0;
                    while (true) {
                        int i32 = i29 + 1;
                        int ce9 = (int) collationIterator.getCE(i29);
                        i30 |= ce9;
                        int i33 = ce9 & tertiaryMask;
                        if (i33 != 0) {
                            while (true) {
                                i = i31 + 1;
                                ce = (int) collationIterator2.getCE(i31);
                                i30 |= ce;
                                i2 = ce & tertiaryMask;
                                if (i2 != 0) {
                                    break;
                                }
                                i31 = i;
                            }
                            if (i33 != i2) {
                                if (CollationSettings.sortsTertiaryUpperCaseFirst(i13)) {
                                    if (i33 > 256) {
                                        i33 = (ce9 & -65536) != 0 ? i33 ^ Collation.CASE_MASK : i33 + 16384;
                                    }
                                    if (i2 > 256) {
                                        i2 = (ce & -65536) != 0 ? i2 ^ Collation.CASE_MASK : i2 + 16384;
                                    }
                                }
                                if (i33 < i2) {
                                    return -1;
                                }
                                return 1;
                            } else if (i33 != 256) {
                                i29 = i32;
                                i31 = i;
                            } else if (CollationSettings.getStrength(i13) <= 2) {
                                return 0;
                            } else {
                                if (!z && (i30 & 192) == 0) {
                                    return 0;
                                }
                                int i34 = 0;
                                int i35 = 0;
                                while (true) {
                                    int i36 = i34 + 1;
                                    long ce10 = collationIterator.getCE(i34);
                                    long j11 = ce10 & 65535;
                                    long j12 = j11 <= 256 ? ce10 >>> 32 : 4294967103L | j11;
                                    if (j12 != 0) {
                                        while (true) {
                                            i3 = i35 + 1;
                                            long ce11 = collationIterator2.getCE(i35);
                                            long j13 = ce11 & 65535;
                                            j4 = j13 <= 256 ? ce11 >>> 32 : 4294967103L | j13;
                                            if (j4 != 0) {
                                                break;
                                            }
                                            i35 = i3;
                                        }
                                        if (j12 != j4) {
                                            if (collationSettings.hasReordering()) {
                                                j12 = collationSettings.reorder(j12);
                                                j4 = collationSettings.reorder(j4);
                                            }
                                            if (j12 < j4) {
                                                return -1;
                                            }
                                            return 1;
                                        } else if (j12 == 1) {
                                            return 0;
                                        } else {
                                            i35 = i3;
                                        }
                                    }
                                    i34 = i36;
                                }
                            }
                        } else {
                            i29 = i32;
                        }
                    }
                } else {
                    z2 = z;
                }
            } else {
                z2 = z;
            }
        }
    }
}
