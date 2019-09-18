package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ZoneRules implements Serializable {
    private static final ZoneOffsetTransitionRule[] EMPTY_LASTRULES = new ZoneOffsetTransitionRule[0];
    private static final LocalDateTime[] EMPTY_LDT_ARRAY = new LocalDateTime[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final int LAST_CACHED_YEAR = 2100;
    private static final long serialVersionUID = 3044319355680032515L;
    private final ZoneOffsetTransitionRule[] lastRules;
    private final transient ConcurrentMap<Integer, ZoneOffsetTransition[]> lastRulesCache;
    private final long[] savingsInstantTransitions;
    private final LocalDateTime[] savingsLocalTransitions;
    private final ZoneOffset[] standardOffsets;
    private final long[] standardTransitions;
    private final ZoneOffset[] wallOffsets;

    public static ZoneRules of(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset, List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList, List<ZoneOffsetTransitionRule> lastRules2) {
        Objects.requireNonNull(baseStandardOffset, "baseStandardOffset");
        Objects.requireNonNull(baseWallOffset, "baseWallOffset");
        Objects.requireNonNull(standardOffsetTransitionList, "standardOffsetTransitionList");
        Objects.requireNonNull(transitionList, "transitionList");
        Objects.requireNonNull(lastRules2, "lastRules");
        ZoneRules zoneRules = new ZoneRules(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules2);
        return zoneRules;
    }

    public static ZoneRules of(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        return new ZoneRules(offset);
    }

    ZoneRules(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset, List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList, List<ZoneOffsetTransitionRule> lastRules2) {
        this.lastRulesCache = new ConcurrentHashMap();
        this.standardTransitions = new long[standardOffsetTransitionList.size()];
        this.standardOffsets = new ZoneOffset[(standardOffsetTransitionList.size() + 1)];
        this.standardOffsets[0] = baseStandardOffset;
        for (int i = 0; i < standardOffsetTransitionList.size(); i++) {
            this.standardTransitions[i] = standardOffsetTransitionList.get(i).toEpochSecond();
            this.standardOffsets[i + 1] = standardOffsetTransitionList.get(i).getOffsetAfter();
        }
        List<LocalDateTime> localTransitionList = new ArrayList<>();
        List<ZoneOffset> localTransitionOffsetList = new ArrayList<>();
        localTransitionOffsetList.add(baseWallOffset);
        for (ZoneOffsetTransition trans : transitionList) {
            if (trans.isGap()) {
                localTransitionList.add(trans.getDateTimeBefore());
                localTransitionList.add(trans.getDateTimeAfter());
            } else {
                localTransitionList.add(trans.getDateTimeAfter());
                localTransitionList.add(trans.getDateTimeBefore());
            }
            localTransitionOffsetList.add(trans.getOffsetAfter());
        }
        this.savingsLocalTransitions = (LocalDateTime[]) localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
        this.wallOffsets = (ZoneOffset[]) localTransitionOffsetList.toArray(new ZoneOffset[localTransitionOffsetList.size()]);
        this.savingsInstantTransitions = new long[transitionList.size()];
        for (int i2 = 0; i2 < transitionList.size(); i2++) {
            this.savingsInstantTransitions[i2] = transitionList.get(i2).toEpochSecond();
        }
        if (lastRules2.size() <= 16) {
            this.lastRules = (ZoneOffsetTransitionRule[]) lastRules2.toArray(new ZoneOffsetTransitionRule[lastRules2.size()]);
            return;
        }
        throw new IllegalArgumentException("Too many transition rules");
    }

    private ZoneRules(long[] standardTransitions2, ZoneOffset[] standardOffsets2, long[] savingsInstantTransitions2, ZoneOffset[] wallOffsets2, ZoneOffsetTransitionRule[] lastRules2) {
        this.lastRulesCache = new ConcurrentHashMap();
        this.standardTransitions = standardTransitions2;
        this.standardOffsets = standardOffsets2;
        this.savingsInstantTransitions = savingsInstantTransitions2;
        this.wallOffsets = wallOffsets2;
        this.lastRules = lastRules2;
        if (savingsInstantTransitions2.length == 0) {
            this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
            return;
        }
        List<LocalDateTime> localTransitionList = new ArrayList<>();
        for (int i = 0; i < savingsInstantTransitions2.length; i++) {
            ZoneOffsetTransition trans = new ZoneOffsetTransition(savingsInstantTransitions2[i], wallOffsets2[i], wallOffsets2[i + 1]);
            if (trans.isGap()) {
                localTransitionList.add(trans.getDateTimeBefore());
                localTransitionList.add(trans.getDateTimeAfter());
            } else {
                localTransitionList.add(trans.getDateTimeAfter());
                localTransitionList.add(trans.getDateTimeBefore());
            }
        }
        this.savingsLocalTransitions = (LocalDateTime[]) localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
    }

    private ZoneRules(ZoneOffset offset) {
        this.lastRulesCache = new ConcurrentHashMap();
        this.standardOffsets = new ZoneOffset[1];
        this.standardOffsets[0] = offset;
        this.standardTransitions = EMPTY_LONG_ARRAY;
        this.savingsInstantTransitions = EMPTY_LONG_ARRAY;
        this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
        this.wallOffsets = this.standardOffsets;
        this.lastRules = EMPTY_LASTRULES;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 1, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.standardTransitions.length);
        for (long trans : this.standardTransitions) {
            Ser.writeEpochSec(trans, out);
        }
        for (ZoneOffset offset : this.standardOffsets) {
            Ser.writeOffset(offset, out);
        }
        out.writeInt(this.savingsInstantTransitions.length);
        for (long trans2 : this.savingsInstantTransitions) {
            Ser.writeEpochSec(trans2, out);
        }
        for (ZoneOffset offset2 : this.wallOffsets) {
            Ser.writeOffset(offset2, out);
        }
        out.writeByte(this.lastRules.length);
        for (ZoneOffsetTransitionRule rule : this.lastRules) {
            rule.writeExternal(out);
        }
    }

    static ZoneRules readExternal(DataInput in) throws IOException, ClassNotFoundException {
        long[] stdTrans;
        long[] jArr;
        int stdSize = in.readInt();
        if (stdSize == 0) {
            stdTrans = EMPTY_LONG_ARRAY;
        } else {
            stdTrans = new long[stdSize];
        }
        for (int i = 0; i < stdSize; i++) {
            stdTrans[i] = Ser.readEpochSec(in);
        }
        ZoneOffset[] stdOffsets = new ZoneOffset[(stdSize + 1)];
        for (int i2 = 0; i2 < stdOffsets.length; i2++) {
            stdOffsets[i2] = Ser.readOffset(in);
        }
        int savSize = in.readInt();
        if (savSize == 0) {
            jArr = EMPTY_LONG_ARRAY;
        } else {
            jArr = new long[savSize];
        }
        long[] savTrans = jArr;
        for (int i3 = 0; i3 < savSize; i3++) {
            savTrans[i3] = Ser.readEpochSec(in);
        }
        ZoneOffset[] savOffsets = new ZoneOffset[(savSize + 1)];
        for (int i4 = 0; i4 < savOffsets.length; i4++) {
            savOffsets[i4] = Ser.readOffset(in);
        }
        int ruleSize = in.readByte();
        ZoneOffsetTransitionRule[] rules = ruleSize == 0 ? EMPTY_LASTRULES : new ZoneOffsetTransitionRule[ruleSize];
        for (int i5 = 0; i5 < ruleSize; i5++) {
            rules[i5] = ZoneOffsetTransitionRule.readExternal(in);
        }
        ZoneRules zoneRules = new ZoneRules(stdTrans, stdOffsets, savTrans, savOffsets, rules);
        return zoneRules;
    }

    public boolean isFixedOffset() {
        return this.savingsInstantTransitions.length == 0;
    }

    public ZoneOffset getOffset(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return this.standardOffsets[0];
        }
        long epochSec = instant.getEpochSecond();
        if (this.lastRules.length <= 0 || epochSec <= this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1]) {
            int index = Arrays.binarySearch(this.savingsInstantTransitions, epochSec);
            if (index < 0) {
                index = (-index) - 2;
            }
            return this.wallOffsets[index + 1];
        }
        ZoneOffsetTransition[] transArray = findTransitionArray(findYear(epochSec, this.wallOffsets[this.wallOffsets.length - 1]));
        ZoneOffsetTransition trans = null;
        for (int i = 0; i < transArray.length; i++) {
            trans = transArray[i];
            if (epochSec < trans.toEpochSecond()) {
                return trans.getOffsetBefore();
            }
        }
        return trans.getOffsetAfter();
    }

    public ZoneOffset getOffset(LocalDateTime localDateTime) {
        Object info = getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition) info).getOffsetBefore();
        }
        return (ZoneOffset) info;
    }

    public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
        Object info = getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return ((ZoneOffsetTransition) info).getValidOffsets();
        }
        return Collections.singletonList((ZoneOffset) info);
    }

    public ZoneOffsetTransition getTransition(LocalDateTime localDateTime) {
        Object info = getOffsetInfo(localDateTime);
        if (info instanceof ZoneOffsetTransition) {
            return (ZoneOffsetTransition) info;
        }
        return null;
    }

    private Object getOffsetInfo(LocalDateTime dt) {
        if (this.savingsInstantTransitions.length == 0) {
            return this.standardOffsets[0];
        }
        if (this.lastRules.length <= 0 || !dt.isAfter(this.savingsLocalTransitions[this.savingsLocalTransitions.length - 1])) {
            int index = Arrays.binarySearch((Object[]) this.savingsLocalTransitions, (Object) dt);
            if (index == -1) {
                return this.wallOffsets[0];
            }
            if (index < 0) {
                index = (-index) - 2;
            } else if (index < this.savingsLocalTransitions.length - 1 && this.savingsLocalTransitions[index].equals(this.savingsLocalTransitions[index + 1])) {
                index++;
            }
            if ((index & 1) != 0) {
                return this.wallOffsets[(index / 2) + 1];
            }
            LocalDateTime dtBefore = this.savingsLocalTransitions[index];
            LocalDateTime dtAfter = this.savingsLocalTransitions[index + 1];
            ZoneOffset offsetBefore = this.wallOffsets[index / 2];
            ZoneOffset offsetAfter = this.wallOffsets[(index / 2) + 1];
            if (offsetAfter.getTotalSeconds() > offsetBefore.getTotalSeconds()) {
                return new ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter);
            }
            return new ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter);
        }
        Object info = null;
        for (ZoneOffsetTransition trans : findTransitionArray(dt.getYear())) {
            info = findOffsetInfo(dt, trans);
            if ((info instanceof ZoneOffsetTransition) || info.equals(trans.getOffsetBefore())) {
                return info;
            }
        }
        return info;
    }

    private Object findOffsetInfo(LocalDateTime dt, ZoneOffsetTransition trans) {
        LocalDateTime localTransition = trans.getDateTimeBefore();
        if (trans.isGap()) {
            if (dt.isBefore(localTransition)) {
                return trans.getOffsetBefore();
            }
            if (dt.isBefore(trans.getDateTimeAfter())) {
                return trans;
            }
            return trans.getOffsetAfter();
        } else if (!dt.isBefore(localTransition)) {
            return trans.getOffsetAfter();
        } else {
            if (dt.isBefore(trans.getDateTimeAfter())) {
                return trans.getOffsetBefore();
            }
            return trans;
        }
    }

    private ZoneOffsetTransition[] findTransitionArray(int year) {
        Integer yearObj = Integer.valueOf(year);
        ZoneOffsetTransition[] transArray = this.lastRulesCache.get(yearObj);
        if (transArray != null) {
            return transArray;
        }
        ZoneOffsetTransitionRule[] ruleArray = this.lastRules;
        ZoneOffsetTransition[] transArray2 = new ZoneOffsetTransition[ruleArray.length];
        for (int i = 0; i < ruleArray.length; i++) {
            transArray2[i] = ruleArray[i].createTransition(year);
        }
        if (year < LAST_CACHED_YEAR) {
            this.lastRulesCache.putIfAbsent(yearObj, transArray2);
        }
        return transArray2;
    }

    public ZoneOffset getStandardOffset(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return this.standardOffsets[0];
        }
        int index = Arrays.binarySearch(this.standardTransitions, instant.getEpochSecond());
        if (index < 0) {
            index = (-index) - 2;
        }
        return this.standardOffsets[index + 1];
    }

    public Duration getDaylightSavings(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return Duration.ZERO;
        }
        return Duration.ofSeconds((long) (getOffset(instant).getTotalSeconds() - getStandardOffset(instant).getTotalSeconds()));
    }

    public boolean isDaylightSavings(Instant instant) {
        return !getStandardOffset(instant).equals(getOffset(instant));
    }

    public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
        return getValidOffsets(localDateTime).contains(offset);
    }

    public ZoneOffsetTransition nextTransition(Instant instant) {
        int index;
        if (this.savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        if (epochSec < this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1]) {
            int index2 = Arrays.binarySearch(this.savingsInstantTransitions, epochSec);
            if (index2 < 0) {
                index = (-index2) - 1;
            } else {
                index = index2 + 1;
            }
            return new ZoneOffsetTransition(this.savingsInstantTransitions[index], this.wallOffsets[index], this.wallOffsets[index + 1]);
        } else if (this.lastRules.length == 0) {
            return null;
        } else {
            int year = findYear(epochSec, this.wallOffsets[this.wallOffsets.length - 1]);
            for (ZoneOffsetTransition trans : findTransitionArray(year)) {
                if (epochSec < trans.toEpochSecond()) {
                    return trans;
                }
            }
            if (year < 999999999) {
                return findTransitionArray(year + 1)[0];
            }
            return null;
        }
    }

    public ZoneOffsetTransition previousTransition(Instant instant) {
        if (this.savingsInstantTransitions.length == 0) {
            return null;
        }
        long epochSec = instant.getEpochSecond();
        if (instant.getNano() > 0 && epochSec < Long.MAX_VALUE) {
            epochSec++;
        }
        long lastHistoric = this.savingsInstantTransitions[this.savingsInstantTransitions.length - 1];
        if (this.lastRules.length > 0 && epochSec > lastHistoric) {
            ZoneOffset lastHistoricOffset = this.wallOffsets[this.wallOffsets.length - 1];
            int year = findYear(epochSec, lastHistoricOffset);
            ZoneOffsetTransition[] transArray = findTransitionArray(year);
            for (int i = transArray.length - 1; i >= 0; i--) {
                if (epochSec > transArray[i].toEpochSecond()) {
                    return transArray[i];
                }
            }
            int year2 = year - 1;
            if (year2 > findYear(lastHistoric, lastHistoricOffset)) {
                return findTransitionArray(year2)[findTransitionArray(year2).length - 1];
            }
        }
        int index = Arrays.binarySearch(this.savingsInstantTransitions, epochSec);
        if (index < 0) {
            index = (-index) - 1;
        }
        if (index <= 0) {
            return null;
        }
        return new ZoneOffsetTransition(this.savingsInstantTransitions[index - 1], this.wallOffsets[index - 1], this.wallOffsets[index]);
    }

    private int findYear(long epochSecond, ZoneOffset offset) {
        return LocalDate.ofEpochDay(Math.floorDiv(((long) offset.getTotalSeconds()) + epochSecond, 86400)).getYear();
    }

    public List<ZoneOffsetTransition> getTransitions() {
        List<ZoneOffsetTransition> list = new ArrayList<>();
        for (int i = 0; i < this.savingsInstantTransitions.length; i++) {
            list.add(new ZoneOffsetTransition(this.savingsInstantTransitions[i], this.wallOffsets[i], this.wallOffsets[i + 1]));
        }
        return Collections.unmodifiableList(list);
    }

    public List<ZoneOffsetTransitionRule> getTransitionRules() {
        return Collections.unmodifiableList(Arrays.asList(this.lastRules));
    }

    public boolean equals(Object otherRules) {
        boolean z = true;
        if (this == otherRules) {
            return true;
        }
        if (!(otherRules instanceof ZoneRules)) {
            return false;
        }
        ZoneRules other = (ZoneRules) otherRules;
        if (!Arrays.equals(this.standardTransitions, other.standardTransitions) || !Arrays.equals((Object[]) this.standardOffsets, (Object[]) other.standardOffsets) || !Arrays.equals(this.savingsInstantTransitions, other.savingsInstantTransitions) || !Arrays.equals((Object[]) this.wallOffsets, (Object[]) other.wallOffsets) || !Arrays.equals((Object[]) this.lastRules, (Object[]) other.lastRules)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((Arrays.hashCode(this.standardTransitions) ^ Arrays.hashCode((Object[]) this.standardOffsets)) ^ Arrays.hashCode(this.savingsInstantTransitions)) ^ Arrays.hashCode((Object[]) this.wallOffsets)) ^ Arrays.hashCode((Object[]) this.lastRules);
    }

    public String toString() {
        return "ZoneRules[currentStandardOffset=" + this.standardOffsets[this.standardOffsets.length - 1] + "]";
    }
}
