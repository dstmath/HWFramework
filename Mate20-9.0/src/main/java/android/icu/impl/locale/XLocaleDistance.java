package android.icu.impl.locale;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Row;
import android.icu.impl.Utility;
import android.icu.impl.locale.XCldrStub;
import android.icu.impl.locale.XLikelySubtags;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.PluralRules;
import android.icu.util.LocaleMatcher;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class XLocaleDistance {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ABOVE_THRESHOLD = 100;
    /* access modifiers changed from: private */
    public static final Set<String> ALL_FINAL_REGIONS = XCldrStub.ImmutableSet.copyOf(CONTAINER_TO_CONTAINED_FINAL.get("001"));
    @Deprecated
    public static final String ANY = "�";
    static final XCldrStub.Multimap<String, String> CONTAINER_TO_CONTAINED = xGetContainment();
    static final XCldrStub.Multimap<String, String> CONTAINER_TO_CONTAINED_FINAL;
    private static final XLocaleDistance DEFAULT;
    static final boolean PRINT_OVERRIDES = false;
    static final LocaleDisplayNames english = LocaleDisplayNames.getInstance(ULocale.ENGLISH);
    private final int defaultLanguageDistance;
    private final int defaultRegionDistance;
    private final int defaultScriptDistance;
    private final DistanceTable languageDesired2Supported;
    private final RegionMapper regionMapper;

    static class AddSub implements XCldrStub.Predicate<DistanceNode> {
        private final String desiredSub;
        private final CopyIfEmpty r;
        private final String supportedSub;

        AddSub(String desiredSub2, String supportedSub2, StringDistanceTable distanceTableToCopy) {
            this.r = new CopyIfEmpty(distanceTableToCopy);
            this.desiredSub = desiredSub2;
            this.supportedSub = supportedSub2;
        }

        public boolean test(DistanceNode node) {
            if (node != null) {
                ((StringDistanceNode) node).addSubtables(this.desiredSub, this.supportedSub, this.r);
                return true;
            }
            throw new IllegalArgumentException("bad structure");
        }
    }

    static class CompactAndImmutablizer extends IdMakerFull<Object> {
        CompactAndImmutablizer() {
        }

        /* access modifiers changed from: package-private */
        public StringDistanceTable compact(StringDistanceTable item) {
            if (toId(item) != null) {
                return (StringDistanceTable) intern(item);
            }
            return new StringDistanceTable(compact(item.subtables, 0));
        }

        /* access modifiers changed from: package-private */
        public <K, T> Map<K, T> compact(Map<K, T> item, int level) {
            if (toId(item) != null) {
                return (Map) intern(item);
            }
            Map<K, T> copy = new LinkedHashMap<>();
            for (Map.Entry<K, T> entry : item.entrySet()) {
                T value = entry.getValue();
                if (value instanceof Map) {
                    copy.put(entry.getKey(), compact((Map) value, level + 1));
                } else {
                    copy.put(entry.getKey(), compact((DistanceNode) value));
                }
            }
            return XCldrStub.ImmutableMap.copyOf(copy);
        }

        /* access modifiers changed from: package-private */
        public DistanceNode compact(DistanceNode item) {
            if (toId(item) != null) {
                return (DistanceNode) intern(item);
            }
            DistanceTable distanceTable = item.getDistanceTable();
            if (distanceTable == null || distanceTable.isEmpty()) {
                return new DistanceNode(item.distance);
            }
            return new StringDistanceNode(item.distance, compact((StringDistanceTable) ((StringDistanceNode) item).distanceTable));
        }
    }

    static class CopyIfEmpty implements XCldrStub.Predicate<DistanceNode> {
        private final StringDistanceTable toCopy;

        CopyIfEmpty(StringDistanceTable resetIfNotNull) {
            this.toCopy = resetIfNotNull;
        }

        public boolean test(DistanceNode node) {
            StringDistanceTable subtables = (StringDistanceTable) node.getDistanceTable();
            if (subtables.subtables.isEmpty()) {
                subtables.copy(this.toCopy);
            }
            return true;
        }
    }

    @Deprecated
    public static class DistanceNode {
        final int distance;

        public DistanceNode(int distance2) {
            this.distance = distance2;
        }

        public DistanceTable getDistanceTable() {
            return null;
        }

        public boolean equals(Object obj) {
            return this == obj || (obj != null && obj.getClass() == getClass() && this.distance == ((DistanceNode) obj).distance);
        }

        public int hashCode() {
            return this.distance;
        }

        public String toString() {
            return "\ndistance: " + this.distance;
        }
    }

    public enum DistanceOption {
        NORMAL,
        SCRIPT_FIRST
    }

    @Deprecated
    public static abstract class DistanceTable {
        /* access modifiers changed from: package-private */
        public abstract Set<String> getCloser(int i);

        /* access modifiers changed from: package-private */
        public abstract int getDistance(String str, String str2, Output<DistanceTable> output, boolean z);

        /* access modifiers changed from: package-private */
        public abstract String toString(boolean z);

        public DistanceTable compact() {
            return this;
        }

        public DistanceNode getInternalNode(String any, String any2) {
            return null;
        }

        public Map<String, Set<String>> getInternalMatches() {
            return null;
        }

        public boolean isEmpty() {
            return true;
        }
    }

    static class IdMakerFull<T> implements IdMapper<T, Integer> {
        private final List<T> intToObject;
        final String name;
        private final Map<T, Integer> objectToInt;

        IdMakerFull(String name2) {
            this.objectToInt = new HashMap();
            this.intToObject = new ArrayList();
            this.name = name2;
        }

        IdMakerFull() {
            this("unnamed");
        }

        IdMakerFull(String name2, T zeroValue) {
            this(name2);
            add(zeroValue);
        }

        public Integer add(T source) {
            Integer result = this.objectToInt.get(source);
            if (result != null) {
                return result;
            }
            Integer newResult = Integer.valueOf(this.intToObject.size());
            this.objectToInt.put(source, newResult);
            this.intToObject.add(source);
            return newResult;
        }

        public Integer toId(T source) {
            return this.objectToInt.get(source);
        }

        public T fromId(int id) {
            return this.intToObject.get(id);
        }

        public T intern(T source) {
            return fromId(add(source).intValue());
        }

        public int size() {
            return this.intToObject.size();
        }

        public Integer getOldAndAdd(T source) {
            Integer result = this.objectToInt.get(source);
            if (result == null) {
                this.objectToInt.put(source, Integer.valueOf(this.intToObject.size()));
                this.intToObject.add(source);
            }
            return result;
        }

        public String toString() {
            return size() + PluralRules.KEYWORD_RULE_SEPARATOR + this.intToObject;
        }

        public boolean equals(Object obj) {
            return this == obj || (obj != null && obj.getClass() == getClass() && this.intToObject.equals(((IdMakerFull) obj).intToObject));
        }

        public int hashCode() {
            return this.intToObject.hashCode();
        }
    }

    private interface IdMapper<K, V> {
        V toId(K k);
    }

    static class RegionMapper implements IdMapper<String, String> {
        final XCldrStub.Multimap<String, String> macroToPartitions;
        final Set<ULocale> paradigms;
        final Map<String, String> regionToPartition;
        final XCldrStub.Multimap<String, String> variableToPartition;

        static class Builder {
            private final Set<ULocale> paradigms = new LinkedHashSet();
            private final RegionSet regionSet = new RegionSet();
            private final XCldrStub.Multimap<String, String> regionToRawPartition = XCldrStub.TreeMultimap.create();

            Builder() {
            }

            /* access modifiers changed from: package-private */
            public void add(String variable, String barString) {
                for (String region : this.regionSet.parseSet(barString)) {
                    this.regionToRawPartition.put(region, variable);
                }
                String inverseVariable = "$!" + variable.substring(1);
                for (String region2 : this.regionSet.inverse()) {
                    this.regionToRawPartition.put(region2, inverseVariable);
                }
            }

            public Builder addParadigms(String... paradigmRegions) {
                for (String paradigm : paradigmRegions) {
                    this.paradigms.add(new ULocale(paradigm));
                }
                return this;
            }

            /* access modifiers changed from: package-private */
            public RegionMapper build() {
                IdMakerFull<Collection<String>> id = new IdMakerFull<>("partition");
                XCldrStub.Multimap<String, String> variableToPartitions = XCldrStub.TreeMultimap.create();
                Map<String, String> regionToPartition = new TreeMap<>();
                XCldrStub.Multimap<String, String> partitionToRegions = XCldrStub.TreeMultimap.create();
                for (Map.Entry<String, Set<String>> e : this.regionToRawPartition.asMap().entrySet()) {
                    String region = e.getKey();
                    Collection<String> rawPartition = e.getValue();
                    String partition = String.valueOf((char) (945 + id.add(rawPartition).intValue()));
                    regionToPartition.put(region, partition);
                    partitionToRegions.put(partition, region);
                    for (String variable : rawPartition) {
                        variableToPartitions.put(variable, partition);
                    }
                }
                XCldrStub.Multimap<String, String> macroToPartitions = XCldrStub.TreeMultimap.create();
                for (Map.Entry<String, Set<String>> e2 : XLocaleDistance.CONTAINER_TO_CONTAINED.asMap().entrySet()) {
                    String macro = e2.getKey();
                    for (Map.Entry<String, Set<String>> e22 : partitionToRegions.asMap().entrySet()) {
                        String partition2 = e22.getKey();
                        if (!Collections.disjoint(e2.getValue(), e22.getValue())) {
                            macroToPartitions.put(macro, partition2);
                        }
                    }
                }
                RegionMapper regionMapper = new RegionMapper(variableToPartitions, regionToPartition, macroToPartitions, this.paradigms);
                return regionMapper;
            }
        }

        private RegionMapper(XCldrStub.Multimap<String, String> variableToPartitionIn, Map<String, String> regionToPartitionIn, XCldrStub.Multimap<String, String> macroToPartitionsIn, Set<ULocale> paradigmsIn) {
            this.variableToPartition = XCldrStub.ImmutableMultimap.copyOf(variableToPartitionIn);
            this.regionToPartition = XCldrStub.ImmutableMap.copyOf(regionToPartitionIn);
            this.macroToPartitions = XCldrStub.ImmutableMultimap.copyOf(macroToPartitionsIn);
            this.paradigms = XCldrStub.ImmutableSet.copyOf(paradigmsIn);
        }

        public String toId(String region) {
            String result = this.regionToPartition.get(region);
            return result == null ? "" : result;
        }

        public Collection<String> getIdsFromVariable(String variable) {
            if (variable.equals("*")) {
                return Collections.singleton("*");
            }
            Collection<String> result = this.variableToPartition.get(variable);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            throw new IllegalArgumentException("Variable not defined: " + variable);
        }

        public Set<String> regions() {
            return this.regionToPartition.keySet();
        }

        public Set<String> variables() {
            return this.variableToPartition.keySet();
        }

        public String toString() {
            XCldrStub.TreeMultimap<String, String> partitionToVariables = (XCldrStub.TreeMultimap) XCldrStub.Multimaps.invertFrom(this.variableToPartition, XCldrStub.TreeMultimap.create());
            XCldrStub.TreeMultimap<String, String> partitionToRegions = XCldrStub.TreeMultimap.create();
            for (Map.Entry<String, String> e : this.regionToPartition.entrySet()) {
                partitionToRegions.put(e.getValue(), e.getKey());
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append("Partition ➠ Variables ➠ Regions (final)");
            for (Map.Entry<String, Set<String>> e2 : partitionToVariables.asMap().entrySet()) {
                buffer.append(10);
                buffer.append(e2.getKey() + "\t" + e2.getValue() + "\t" + partitionToRegions.get(e2.getKey()));
            }
            buffer.append("\nMacro ➠ Partitions");
            for (Map.Entry<String, Set<String>> e3 : this.macroToPartitions.asMap().entrySet()) {
                buffer.append(10);
                buffer.append(e3.getKey() + "\t" + e3.getValue());
            }
            return buffer.toString();
        }
    }

    private static class RegionSet {
        private Operation operation;
        private final Set<String> tempRegions;

        private enum Operation {
            add,
            remove
        }

        private RegionSet() {
            this.tempRegions = new TreeSet();
            this.operation = null;
        }

        /* access modifiers changed from: private */
        public Set<String> parseSet(String barString) {
            this.operation = Operation.add;
            int last = 0;
            this.tempRegions.clear();
            int i = 0;
            while (i < barString.length()) {
                char c = barString.charAt(i);
                if (c == '+') {
                    add(barString, last, i);
                    last = i + 1;
                    this.operation = Operation.add;
                } else if (c == '-') {
                    add(barString, last, i);
                    last = i + 1;
                    this.operation = Operation.remove;
                }
                i++;
            }
            add(barString, last, i);
            return this.tempRegions;
        }

        /* access modifiers changed from: private */
        public Set<String> inverse() {
            TreeSet<String> result = new TreeSet<>(XLocaleDistance.ALL_FINAL_REGIONS);
            result.removeAll(this.tempRegions);
            return result;
        }

        private void add(String barString, int last, int i) {
            if (i > last) {
                changeSet(this.operation, barString.substring(last, i));
            }
        }

        private void changeSet(Operation operation2, String region) {
            Collection<String> contained = XLocaleDistance.CONTAINER_TO_CONTAINED_FINAL.get(region);
            if (contained == null || contained.isEmpty()) {
                if (Operation.add == operation2) {
                    this.tempRegions.add(region);
                } else {
                    this.tempRegions.remove(region);
                }
            } else if (Operation.add == operation2) {
                this.tempRegions.addAll(contained);
            } else {
                this.tempRegions.removeAll(contained);
            }
        }
    }

    static class StringDistanceNode extends DistanceNode {
        final DistanceTable distanceTable;

        public StringDistanceNode(int distance, DistanceTable distanceTable2) {
            super(distance);
            this.distanceTable = distanceTable2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0026, code lost:
            if (super.equals(r2) != false) goto L_0x002b;
         */
        public boolean equals(Object obj) {
            if (this != obj) {
                if (obj != null && obj.getClass() == getClass()) {
                    StringDistanceNode stringDistanceNode = (StringDistanceNode) obj;
                    StringDistanceNode other = stringDistanceNode;
                    if (this.distance == stringDistanceNode.distance) {
                        if (Utility.equals(this.distanceTable, other.distanceTable)) {
                        }
                    }
                }
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.distance ^ Utility.hashCode(this.distanceTable);
        }

        StringDistanceNode(int distance) {
            this(distance, new StringDistanceTable());
        }

        public void addSubtables(String desiredSub, String supportedSub, CopyIfEmpty r) {
            ((StringDistanceTable) this.distanceTable).addSubtables(desiredSub, supportedSub, r);
        }

        public String toString() {
            return "distance: " + this.distance + "\n" + this.distanceTable;
        }

        public void copyTables(StringDistanceTable value) {
            if (value != null) {
                ((StringDistanceTable) this.distanceTable).copy(value);
            }
        }

        public DistanceTable getDistanceTable() {
            return this.distanceTable;
        }
    }

    @Deprecated
    public static class StringDistanceTable extends DistanceTable {
        final Map<String, Map<String, DistanceNode>> subtables;

        StringDistanceTable(Map<String, Map<String, DistanceNode>> tables) {
            this.subtables = tables;
        }

        StringDistanceTable() {
            this(XLocaleDistance.newMap());
        }

        public boolean isEmpty() {
            return this.subtables.isEmpty();
        }

        public boolean equals(Object obj) {
            return this == obj || (obj != null && obj.getClass() == getClass() && this.subtables.equals(((StringDistanceTable) obj).subtables));
        }

        public int hashCode() {
            return this.subtables.hashCode();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: android.icu.impl.locale.XLocaleDistance$DistanceNode} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v10, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: android.icu.impl.locale.XLocaleDistance$DistanceNode} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v12, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: android.icu.impl.locale.XLocaleDistance$DistanceNode} */
        /* JADX WARNING: Multi-variable type inference failed */
        public int getDistance(String desired, String supported, Output<DistanceTable> distanceTable, boolean starEquals) {
            boolean star = false;
            Map<String, DistanceNode> sub2 = this.subtables.get(desired);
            if (sub2 == null) {
                sub2 = this.subtables.get(XLocaleDistance.ANY);
                star = true;
            }
            DistanceNode value = sub2.get(supported);
            if (value == null) {
                value = sub2.get(XLocaleDistance.ANY);
                if (value == null && !star) {
                    Map<String, DistanceNode> sub22 = this.subtables.get(XLocaleDistance.ANY);
                    value = sub22.get(supported);
                    if (value == null) {
                        value = sub22.get(XLocaleDistance.ANY);
                    }
                }
                star = true;
            }
            if (distanceTable != null) {
                distanceTable.value = ((StringDistanceNode) value).distanceTable;
            }
            if (!starEquals || !star || !desired.equals(supported)) {
                return value.distance;
            }
            return 0;
        }

        public void copy(StringDistanceTable other) {
            for (Map.Entry<String, Map<String, DistanceNode>> e1 : other.subtables.entrySet()) {
                for (Map.Entry<String, DistanceNode> e2 : e1.getValue().entrySet()) {
                    addSubtable(e1.getKey(), e2.getKey(), e2.getValue().distance);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public DistanceNode addSubtable(String desired, String supported, int distance) {
            Map<String, DistanceNode> sub2 = this.subtables.get(desired);
            if (sub2 == null) {
                Map<String, Map<String, DistanceNode>> map = this.subtables;
                Map<String, DistanceNode> access$000 = XLocaleDistance.newMap();
                sub2 = access$000;
                map.put(desired, access$000);
            }
            DistanceNode oldNode = sub2.get(supported);
            if (oldNode != null) {
                return oldNode;
            }
            StringDistanceNode newNode = new StringDistanceNode(distance);
            sub2.put(supported, newNode);
            return newNode;
        }

        private DistanceNode getNode(String desired, String supported) {
            Map<String, DistanceNode> sub2 = this.subtables.get(desired);
            if (sub2 == null) {
                return null;
            }
            return sub2.get(supported);
        }

        public void addSubtables(String desired, String supported, XCldrStub.Predicate<DistanceNode> action) {
            DistanceNode node = getNode(desired, supported);
            if (node == null) {
                Output<DistanceTable> node2 = new Output<>();
                node = addSubtable(desired, supported, getDistance(desired, supported, node2, true));
                if (node2.value != null) {
                    ((StringDistanceNode) node).copyTables((StringDistanceTable) node2.value);
                }
            }
            action.test(node);
        }

        public void addSubtables(String desiredLang, String supportedLang, String desiredScript, String supportedScript, int percentage) {
            boolean haveKeys;
            String str = desiredLang;
            String str2 = supportedLang;
            String str3 = desiredScript;
            String str4 = supportedScript;
            int i = percentage;
            boolean haveKeys2 = false;
            for (Map.Entry<String, Map<String, DistanceNode>> e1 : this.subtables.entrySet()) {
                boolean desiredIsKey = str.equals(e1.getKey());
                if (desiredIsKey || str.equals(XLocaleDistance.ANY)) {
                    for (Map.Entry<String, DistanceNode> e2 : e1.getValue().entrySet()) {
                        boolean supportedIsKey = str2.equals(e2.getKey());
                        boolean haveKeys3 = haveKeys2 | (desiredIsKey && supportedIsKey);
                        if (supportedIsKey || str2.equals(XLocaleDistance.ANY)) {
                            haveKeys = haveKeys3;
                            ((StringDistanceTable) e2.getValue().getDistanceTable()).addSubtable(str3, str4, i);
                        } else {
                            haveKeys = haveKeys3;
                        }
                        haveKeys2 = haveKeys;
                    }
                }
            }
            StringDistanceTable dt = new StringDistanceTable();
            dt.addSubtable(str3, str4, i);
            addSubtables(str, str2, new CopyIfEmpty(dt));
        }

        public void addSubtables(String desiredLang, String supportedLang, String desiredScript, String supportedScript, String desiredRegion, String supportedRegion, int percentage) {
            String str = desiredLang;
            String str2 = supportedLang;
            boolean haveKeys = false;
            for (Map.Entry<String, Map<String, DistanceNode>> e1 : this.subtables.entrySet()) {
                boolean desiredIsKey = str.equals(e1.getKey());
                if (desiredIsKey || str.equals(XLocaleDistance.ANY)) {
                    for (Map.Entry<String, DistanceNode> e2 : e1.getValue().entrySet()) {
                        boolean supportedIsKey = str2.equals(e2.getKey());
                        haveKeys |= desiredIsKey && supportedIsKey;
                        if (supportedIsKey || str2.equals(XLocaleDistance.ANY)) {
                            ((StringDistanceTable) ((StringDistanceNode) e2.getValue()).distanceTable).addSubtables(desiredScript, supportedScript, desiredRegion, supportedRegion, percentage);
                        }
                    }
                }
            }
            StringDistanceTable dt = new StringDistanceTable();
            dt.addSubtable(desiredRegion, supportedRegion, percentage);
            addSubtables(str, str2, new AddSub(desiredScript, supportedScript, dt));
        }

        public String toString() {
            return toString(false);
        }

        public String toString(boolean abbreviate) {
            return toString(abbreviate, "", new IdMakerFull("interner"), new StringBuilder()).toString();
        }

        public StringBuilder toString(boolean abbreviate, String indent, IdMakerFull<Object> intern, StringBuilder buffer) {
            String indent2;
            char c;
            boolean z = abbreviate;
            String str = indent;
            IdMakerFull<Object> idMakerFull = intern;
            StringBuilder sb = buffer;
            String indent22 = indent.isEmpty() ? "" : "\t";
            Integer id = z ? idMakerFull.getOldAndAdd(this.subtables) : null;
            char c2 = '#';
            char c3 = 10;
            if (id != null) {
                sb.append(indent22);
                sb.append('#');
                sb.append(id);
                sb.append(10);
            } else {
                for (Map.Entry<String, Map<String, DistanceNode>> e1 : this.subtables.entrySet()) {
                    Map<String, DistanceNode> subsubtable = e1.getValue();
                    sb.append(indent22);
                    sb.append(e1.getKey());
                    String indent3 = "\t";
                    Integer id2 = z ? idMakerFull.getOldAndAdd(subsubtable) : null;
                    if (id2 != null) {
                        sb.append(indent3);
                        sb.append(c2);
                        sb.append(id2);
                        sb.append(c3);
                    } else {
                        for (Map.Entry<String, DistanceNode> e2 : subsubtable.entrySet()) {
                            DistanceNode value = e2.getValue();
                            sb.append(indent3);
                            sb.append(e2.getKey());
                            Integer id3 = z ? idMakerFull.getOldAndAdd(value) : null;
                            if (id3 != null) {
                                sb.append(9);
                                sb.append(c2);
                                sb.append(id3);
                                sb.append(10);
                                indent2 = indent22;
                                c = 10;
                            } else {
                                sb.append(9);
                                sb.append(value.distance);
                                DistanceTable distanceTable = value.getDistanceTable();
                                if (distanceTable != null) {
                                    Integer id4 = z ? idMakerFull.getOldAndAdd(distanceTable) : null;
                                    if (id4 != null) {
                                        sb.append(9);
                                        sb.append('#');
                                        sb.append(id4);
                                        sb.append(10);
                                        indent2 = indent22;
                                    } else {
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append(str);
                                        indent2 = indent22;
                                        sb2.append("\t\t\t");
                                        ((StringDistanceTable) distanceTable).toString(z, sb2.toString(), idMakerFull, sb);
                                    }
                                    c = 10;
                                } else {
                                    indent2 = indent22;
                                    c = 10;
                                    sb.append(10);
                                }
                            }
                            indent3 = str + 9;
                            c3 = c;
                            indent22 = indent2;
                            c2 = '#';
                        }
                    }
                    indent22 = str;
                    c3 = c3;
                    c2 = '#';
                }
                String str2 = indent22;
            }
            return sb;
        }

        public StringDistanceTable compact() {
            return new CompactAndImmutablizer().compact(this);
        }

        public Set<String> getCloser(int threshold) {
            Set<String> result = new HashSet<>();
            for (Map.Entry<String, Map<String, DistanceNode>> e1 : this.subtables.entrySet()) {
                String desired = e1.getKey();
                Iterator it = e1.getValue().entrySet().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((Map.Entry) it.next()).getValue().distance < threshold) {
                            result.add(desired);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            return result;
        }

        public Integer getInternalDistance(String a, String b) {
            Map<String, DistanceNode> subsub = this.subtables.get(a);
            Integer num = null;
            if (subsub == null) {
                return null;
            }
            DistanceNode dnode = subsub.get(b);
            if (dnode != null) {
                num = Integer.valueOf(dnode.distance);
            }
            return num;
        }

        public DistanceNode getInternalNode(String a, String b) {
            Map<String, DistanceNode> subsub = this.subtables.get(a);
            if (subsub == null) {
                return null;
            }
            return subsub.get(b);
        }

        public Map<String, Set<String>> getInternalMatches() {
            Map<String, Set<String>> result = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, DistanceNode>> entry : this.subtables.entrySet()) {
                result.put(entry.getKey(), new LinkedHashSet(entry.getValue().keySet()));
            }
            return result;
        }
    }

    static {
        XCldrStub.Multimap<String, String> containerToFinalContainedBuilder = XCldrStub.TreeMultimap.create();
        for (Map.Entry<String, Set<String>> entry : CONTAINER_TO_CONTAINED.asMap().entrySet()) {
            String container = entry.getKey();
            for (String contained : entry.getValue()) {
                if (CONTAINER_TO_CONTAINED.get(contained) == null) {
                    containerToFinalContainedBuilder.put(container, contained);
                }
            }
        }
        CONTAINER_TO_CONTAINED_FINAL = XCldrStub.ImmutableMultimap.copyOf(containerToFinalContainedBuilder);
        String[][] variableOverrides = {new String[]{"$enUS", "AS+GU+MH+MP+PR+UM+US+VI"}, new String[]{"$cnsar", "HK+MO"}, new String[]{"$americas", "019"}, new String[]{"$maghreb", "MA+DZ+TN+LY+MR+EH"}};
        String[] paradigmRegions = {"en", "en-GB", "es", "es-419", "pt-BR", "pt-PT"};
        String[][] regionRuleOverrides = {new String[]{"ar_*_$maghreb", "ar_*_$maghreb", "96"}, new String[]{"ar_*_$!maghreb", "ar_*_$!maghreb", "96"}, new String[]{"ar_*_*", "ar_*_*", "95"}, new String[]{"en_*_$enUS", "en_*_$enUS", "96"}, new String[]{"en_*_$!enUS", "en_*_$!enUS", "96"}, new String[]{"en_*_*", "en_*_*", "95"}, new String[]{"es_*_$americas", "es_*_$americas", "96"}, new String[]{"es_*_$!americas", "es_*_$!americas", "96"}, new String[]{"es_*_*", "es_*_*", "95"}, new String[]{"pt_*_$americas", "pt_*_$americas", "96"}, new String[]{"pt_*_$!americas", "pt_*_$!americas", "96"}, new String[]{"pt_*_*", "pt_*_*", "95"}, new String[]{"zh_Hant_$cnsar", "zh_Hant_$cnsar", "96"}, new String[]{"zh_Hant_$!cnsar", "zh_Hant_$!cnsar", "96"}, new String[]{"zh_Hant_*", "zh_Hant_*", "95"}, new String[]{"*_*_*", "*_*_*", "96"}};
        RegionMapper.Builder rmb = new RegionMapper.Builder().addParadigms(paradigmRegions);
        for (String[] variableRule : variableOverrides) {
            rmb.add(variableRule[0], variableRule[1]);
        }
        StringDistanceTable defaultDistanceTable = new StringDistanceTable();
        RegionMapper defaultRegionMapper = rmb.build();
        XCldrStub.Splitter bar = XCldrStub.Splitter.on('_');
        List<Row.R4<List<String>, List<String>, Integer, Boolean>>[] sorted = {new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        for (Row.R4<String, String, Integer, Boolean> info : xGetLanguageMatcherData()) {
            String desiredRaw = info.get0();
            List<String> desired = bar.splitToList(desiredRaw);
            List<String> supported = bar.splitToList(info.get1());
            Boolean oneway = info.get3();
            int distance = desiredRaw.equals("*_*") ? 50 : info.get2().intValue();
            String[][] variableOverrides2 = variableOverrides;
            int size = desired.size();
            String[] paradigmRegions2 = paradigmRegions;
            if (size == 3) {
                variableOverrides = variableOverrides2;
                paradigmRegions = paradigmRegions2;
            } else {
                int i = size;
                sorted[size - 1].add(Row.of(desired, supported, Integer.valueOf(distance), oneway));
                variableOverrides = variableOverrides2;
                paradigmRegions = paradigmRegions2;
            }
        }
        String[] strArr = paradigmRegions;
        for (List<Row.R4<List<String>, List<String>, Integer, Boolean>> item1 : sorted) {
            for (Row.R4<List<String>, List<String>, Integer, Boolean> item2 : item1) {
                List<String> desired2 = item2.get0();
                List<String> supported2 = item2.get1();
                Integer distance2 = item2.get2();
                Boolean oneway2 = item2.get3();
                add(defaultDistanceTable, desired2, supported2, distance2.intValue());
                if (oneway2 != Boolean.TRUE && !desired2.equals(supported2)) {
                    add(defaultDistanceTable, supported2, desired2, distance2.intValue());
                }
                printMatchXml(desired2, supported2, distance2, oneway2);
            }
        }
        int length = regionRuleOverrides.length;
        int i2 = 0;
        while (i2 < length) {
            String[] rule = regionRuleOverrides[i2];
            List<String> desiredBase = new ArrayList<>(bar.splitToList(rule[0]));
            List<String> supportedBase = new ArrayList<>(bar.splitToList(rule[1]));
            Integer distance3 = Integer.valueOf(100 - Integer.parseInt(rule[2]));
            printMatchXml(desiredBase, supportedBase, distance3, false);
            Collection<String> desiredRegions = defaultRegionMapper.getIdsFromVariable(desiredBase.get(2));
            if (!desiredRegions.isEmpty()) {
                Collection<String> supportedRegions = defaultRegionMapper.getIdsFromVariable(supportedBase.get(2));
                if (!supportedRegions.isEmpty()) {
                    for (String desiredRegion2 : desiredRegions) {
                        String[][] regionRuleOverrides2 = regionRuleOverrides;
                        int i3 = length;
                        desiredBase.set(2, desiredRegion2.toString());
                        for (Iterator<String> it = supportedRegions.iterator(); it.hasNext(); it = it) {
                            String supportedRegion2 = it.next();
                            String str = supportedRegion2;
                            supportedBase.set(2, supportedRegion2.toString());
                            add(defaultDistanceTable, desiredBase, supportedBase, distance3.intValue());
                            add(defaultDistanceTable, supportedBase, desiredBase, distance3.intValue());
                        }
                        regionRuleOverrides = regionRuleOverrides2;
                        length = i3;
                    }
                    int i4 = length;
                    i2++;
                } else {
                    throw new IllegalArgumentException("Bad region variable: " + supportedBase.get(2));
                }
            } else {
                throw new IllegalArgumentException("Bad region variable: " + desiredBase.get(2));
            }
        }
        DEFAULT = new XLocaleDistance(defaultDistanceTable.compact(), defaultRegionMapper);
    }

    private static String fixAny(String string) {
        return "*".equals(string) ? ANY : string;
    }

    private static List<Row.R4<String, String, Integer, Boolean>> xGetLanguageMatcherData() {
        List<Row.R4<String, String, Integer, Boolean>> distanceList = new ArrayList<>();
        UResourceBundleIterator iter = ((ICUResourceBundle) LocaleMatcher.getICUSupplementalData().findTopLevel("languageMatchingNew").get("written")).getIterator();
        while (iter.hasNext()) {
            ICUResourceBundle item = (ICUResourceBundle) iter.next();
            distanceList.add((Row.R4) Row.of(item.getString(0), item.getString(1), Integer.valueOf(Integer.parseInt(item.getString(2))), Boolean.valueOf(item.getSize() > 3 && "1".equals(item.getString(3)))).freeze());
        }
        return Collections.unmodifiableList(distanceList);
    }

    private static Set<String> xGetParadigmLocales() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(((ICUResourceBundle) LocaleMatcher.getICUSupplementalData().findTopLevel("languageMatchingInfo").get("written").get("paradigmLocales")).getStringArray())));
    }

    private static Map<String, String> xGetMatchVariables() {
        ICUResourceBundle writtenMatchVariables = (ICUResourceBundle) LocaleMatcher.getICUSupplementalData().findTopLevel("languageMatchingInfo").get("written").get("matchVariable");
        HashMap<String, String> matchVariables = new HashMap<>();
        Enumeration<String> enumer = writtenMatchVariables.getKeys();
        while (enumer.hasMoreElements()) {
            String key = enumer.nextElement();
            matchVariables.put(key, writtenMatchVariables.getString(key));
        }
        return Collections.unmodifiableMap(matchVariables);
    }

    private static XCldrStub.Multimap<String, String> xGetContainment() {
        XCldrStub.TreeMultimap<String, String> containment = XCldrStub.TreeMultimap.create();
        containment.putAll("001", (V[]) new String[]{"019", "002", "150", "142", "009"}).putAll("011", (V[]) new String[]{"BF", "BJ", "CI", "CV", "GH", "GM", "GN", "GW", "LR", "ML", "MR", "NE", "NG", "SH", "SL", "SN", "TG"}).putAll("013", (V[]) new String[]{"BZ", "CR", "GT", "HN", "MX", "NI", "PA", "SV"}).putAll("014", (V[]) new String[]{"BI", "DJ", "ER", "ET", "KE", "KM", "MG", "MU", "MW", "MZ", "RE", "RW", "SC", "SO", "SS", "TZ", "UG", "YT", "ZM", "ZW"}).putAll("142", (V[]) new String[]{"145", "143", "030", "034", "035"}).putAll("143", (V[]) new String[]{"TM", "TJ", "KG", "KZ", "UZ"}).putAll("145", (V[]) new String[]{"AE", "AM", "AZ", "BH", "CY", "GE", "IL", "IQ", "JO", "KW", "LB", "OM", "PS", "QA", "SA", "SY", "TR", "YE", "NT", "YD"}).putAll("015", (V[]) new String[]{"DZ", "EG", "EH", "LY", "MA", "SD", "TN", "EA", "IC"}).putAll("150", (V[]) new String[]{"154", "155", "151", "039"}).putAll("151", (V[]) new String[]{"BG", "BY", "CZ", "HU", "MD", "PL", "RO", "RU", "SK", "UA", "SU"}).putAll("154", (V[]) new String[]{"GG", "IM", "JE", "AX", "DK", "EE", "FI", "FO", "GB", "IE", "IS", "LT", "LV", "NO", "SE", "SJ"}).putAll("155", (V[]) new String[]{"AT", "BE", "CH", "DE", "FR", "LI", "LU", "MC", "NL", "DD", "FX"}).putAll("017", (V[]) new String[]{"AO", "CD", "CF", "CG", "CM", "GA", "GQ", "ST", "TD", "ZR"}).putAll("018", (V[]) new String[]{"BW", "LS", "NA", "SZ", "ZA"}).putAll("019", (V[]) new String[]{"021", "013", "029", "005", "003", "419"}).putAll("002", (V[]) new String[]{"015", "011", "017", "014", "018"}).putAll("021", (V[]) new String[]{"BM", "CA", "GL", "PM", "US"}).putAll("029", (V[]) new String[]{"AG", "AI", "AW", "BB", "BL", "BQ", "BS", "CU", "CW", "DM", "DO", "GD", "GP", "HT", "JM", "KN", "KY", "LC", "MF", "MQ", "MS", "PR", "SX", "TC", "TT", "VC", "VG", "VI", "AN"}).putAll("003", (V[]) new String[]{"021", "013", "029"}).putAll("030", (V[]) new String[]{"CN", "HK", "JP", "KP", "KR", "MN", "MO", "TW"}).putAll("035", (V[]) new String[]{"BN", "ID", "KH", "LA", "MM", "MY", "PH", "SG", "TH", "TL", "VN", "BU", "TP"}).putAll("039", (V[]) new String[]{"AD", "AL", "BA", "ES", "GI", "GR", "HR", "IT", "ME", "MK", "MT", "RS", "PT", "SI", "SM", "VA", "XK", "CS", "YU"}).putAll("419", (V[]) new String[]{"013", "029", "005"}).putAll("005", (V[]) new String[]{"AR", "BO", "BR", "CL", "CO", "EC", "FK", "GF", "GY", "PE", "PY", "SR", "UY", "VE"}).putAll("053", (V[]) new String[]{"AU", "NF", "NZ"}).putAll("054", (V[]) new String[]{"FJ", "NC", "PG", "SB", "VU"}).putAll("057", (V[]) new String[]{"FM", "GU", "KI", "MH", "MP", "NR", "PW"}).putAll("061", (V[]) new String[]{"AS", "CK", "NU", "PF", "PN", "TK", "TO", "TV", "WF", "WS"}).putAll("034", (V[]) new String[]{"AF", "BD", "BT", "IN", "IR", "LK", "MV", "NP", "PK"}).putAll("009", (V[]) new String[]{"053", "054", "057", "061", "QO"}).putAll("QO", (V[]) new String[]{"AQ", "BV", "CC", "CX", "GS", "HM", "IO", "TF", "UM", "AC", "CP", "DG", "TA"});
        XCldrStub.TreeMultimap<String, String> containmentResolved = XCldrStub.TreeMultimap.create();
        fill("001", containment, containmentResolved);
        return XCldrStub.ImmutableMultimap.copyOf(containmentResolved);
    }

    private static Set<String> fill(String region, XCldrStub.TreeMultimap<String, String> containment, XCldrStub.Multimap<String, String> toAddTo) {
        Set<String> contained = containment.get(region);
        if (contained == null) {
            return Collections.emptySet();
        }
        toAddTo.putAll(region, (Collection<String>) contained);
        for (String subregion : contained) {
            toAddTo.putAll(region, (Collection<String>) fill(subregion, containment, toAddTo));
        }
        return toAddTo.get(region);
    }

    public XLocaleDistance(DistanceTable datadistancetable2, RegionMapper regionMapper2) {
        this.languageDesired2Supported = datadistancetable2;
        this.regionMapper = regionMapper2;
        StringDistanceNode languageNode = (StringDistanceNode) ((StringDistanceTable) this.languageDesired2Supported).subtables.get(ANY).get(ANY);
        this.defaultLanguageDistance = languageNode.distance;
        StringDistanceNode scriptNode = (StringDistanceNode) ((StringDistanceTable) languageNode.distanceTable).subtables.get(ANY).get(ANY);
        this.defaultScriptDistance = scriptNode.distance;
        this.defaultRegionDistance = ((DistanceNode) ((StringDistanceTable) scriptNode.distanceTable).subtables.get(ANY).get(ANY)).distance;
    }

    /* access modifiers changed from: private */
    public static Map newMap() {
        return new TreeMap();
    }

    public int distance(ULocale desired, ULocale supported, int threshold, DistanceOption distanceOption) {
        return distanceRaw(XLikelySubtags.LSR.fromMaximalized(desired), XLikelySubtags.LSR.fromMaximalized(supported), threshold, distanceOption);
    }

    public int distanceRaw(XLikelySubtags.LSR desired, XLikelySubtags.LSR supported, int threshold, DistanceOption distanceOption) {
        return distanceRaw(desired.language, supported.language, desired.script, supported.script, desired.region, supported.region, threshold, distanceOption);
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0101  */
    public int distanceRaw(String desiredLang, String supportedlang, String desiredScript, String supportedScript, String desiredRegion, String supportedRegion, int threshold, DistanceOption distanceOption) {
        int subdistance;
        String str = desiredRegion;
        String str2 = supportedRegion;
        int i = threshold;
        Output<DistanceTable> subtable = new Output<>();
        int distance = this.languageDesired2Supported.getDistance(desiredLang, supportedlang, subtable, true);
        boolean scriptFirst = distanceOption == DistanceOption.SCRIPT_FIRST;
        if (scriptFirst) {
            distance >>= 2;
        }
        if (distance < 0) {
            distance = 0;
        } else if (distance >= i) {
            return 100;
        }
        int scriptDistance = ((DistanceTable) subtable.value).getDistance(desiredScript, supportedScript, subtable, true);
        if (scriptFirst) {
            scriptDistance >>= 1;
        }
        int distance2 = distance + scriptDistance;
        if (distance2 >= i) {
            return 100;
        }
        if (desiredRegion.equals(supportedRegion)) {
            return distance2;
        }
        String desiredPartition = this.regionMapper.toId(str);
        String supportedPartition = this.regionMapper.toId(str2);
        Collection<String> desiredPartitions = desiredPartition.isEmpty() ? this.regionMapper.macroToPartitions.get(str) : null;
        Collection<String> supportedPartitions = supportedPartition.isEmpty() ? this.regionMapper.macroToPartitions.get(str2) : null;
        if (desiredPartitions != null) {
        } else if (supportedPartitions != null) {
            int i2 = scriptDistance;
        } else {
            int i3 = scriptDistance;
            subdistance = ((DistanceTable) subtable.value).getDistance(desiredPartition, supportedPartition, null, false);
            Set<String> set = supportedPartitions;
            Output<DistanceTable> output = subtable;
            int distance3 = distance2 + subdistance;
            return distance3 < i ? 100 : distance3;
        }
        int subdistance2 = 0;
        if (desiredPartitions == null) {
            desiredPartitions = Collections.singleton(desiredPartition);
        }
        if (supportedPartitions == null) {
            supportedPartitions = Collections.singleton(supportedPartition);
        }
        Iterator<String> it = desiredPartitions.iterator();
        while (it.hasNext()) {
            String desiredPartition2 = it.next();
            int subdistance3 = subdistance2;
            Iterator<String> it2 = supportedPartitions.iterator();
            Collection<String> supportedPartitions2 = supportedPartitions;
            int subdistance4 = subdistance3;
            while (it2.hasNext()) {
                Iterator<String> it3 = it2;
                Iterator<String> it4 = it;
                Output<DistanceTable> subtable2 = subtable;
                int tempSubdistance = ((DistanceTable) subtable.value).getDistance(desiredPartition2, it2.next(), null, false);
                if (subdistance4 < tempSubdistance) {
                    subdistance4 = tempSubdistance;
                }
                it2 = it3;
                it = it4;
                subtable = subtable2;
                String str3 = desiredLang;
            }
            Iterator<String> it5 = it;
            subdistance2 = subdistance4;
            supportedPartitions = supportedPartitions2;
            subtable = subtable;
            String str4 = desiredLang;
        }
        subdistance = subdistance2;
        Collection<String> collection = supportedPartitions;
        Output<DistanceTable> output2 = subtable;
        int distance32 = distance2 + subdistance;
        return distance32 < i ? 100 : distance32;
    }

    public static XLocaleDistance getDefault() {
        return DEFAULT;
    }

    private static void printMatchXml(List<String> list, List<String> list2, Integer distance, Boolean oneway) {
    }

    private static String fixedName(List<String> match) {
        List<String> alt = new ArrayList<>(match);
        int size = alt.size();
        StringBuilder result = new StringBuilder();
        if (size >= 3) {
            String region = alt.get(2);
            if (region.equals("*") || region.startsWith("$")) {
                result.append(region);
            } else {
                result.append(english.regionDisplayName(region));
            }
        }
        if (size >= 2) {
            String script = alt.get(1);
            if (script.equals("*")) {
                result.insert(0, script);
            } else {
                result.insert(0, english.scriptDisplayName(script));
            }
        }
        if (size >= 1) {
            String language = alt.get(0);
            if (language.equals("*")) {
                result.insert(0, language);
            } else {
                result.insert(0, english.languageDisplayName(language));
            }
        }
        return XCldrStub.CollectionUtilities.join(alt, "; ");
    }

    public static void add(StringDistanceTable languageDesired2Supported2, List<String> desired, List<String> supported, int percentage) {
        List<String> list = desired;
        List<String> list2 = supported;
        int size = desired.size();
        if (size != supported.size() || size < 1 || size > 3) {
            StringDistanceTable stringDistanceTable = languageDesired2Supported2;
            int i = percentage;
            throw new IllegalArgumentException();
        }
        String desiredLang = fixAny(list.get(0));
        String supportedLang = fixAny(list2.get(0));
        if (size == 1) {
            languageDesired2Supported2.addSubtable(desiredLang, supportedLang, percentage);
            return;
        }
        StringDistanceTable stringDistanceTable2 = languageDesired2Supported2;
        int i2 = percentage;
        String desiredScript = fixAny(list.get(1));
        String supportedScript = fixAny(list2.get(1));
        if (size == 2) {
            stringDistanceTable2.addSubtables(desiredLang, supportedLang, desiredScript, supportedScript, i2);
            return;
        }
        stringDistanceTable2.addSubtables(desiredLang, supportedLang, desiredScript, supportedScript, fixAny(list.get(2)), fixAny(list2.get(2)), i2);
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean abbreviate) {
        return this.regionMapper + "\n" + this.languageDesired2Supported.toString(abbreviate);
    }

    static Set<String> getContainingMacrosFor(Collection<String> input, Set<String> output) {
        output.clear();
        for (Map.Entry<String, Set<String>> entry : CONTAINER_TO_CONTAINED.asMap().entrySet()) {
            if (input.containsAll(entry.getValue())) {
                output.add(entry.getKey());
            }
        }
        return output;
    }

    public static <K, V> XCldrStub.Multimap<K, V> invertMap(Map<V, K> map) {
        return XCldrStub.Multimaps.invertFrom(XCldrStub.Multimaps.forMap(map), XCldrStub.LinkedHashMultimap.create());
    }

    public Set<ULocale> getParadigms() {
        return this.regionMapper.paradigms;
    }

    public int getDefaultLanguageDistance() {
        return this.defaultLanguageDistance;
    }

    public int getDefaultScriptDistance() {
        return this.defaultScriptDistance;
    }

    public int getDefaultRegionDistance() {
        return this.defaultRegionDistance;
    }

    @Deprecated
    public StringDistanceTable internalGetDistanceTable() {
        return (StringDistanceTable) this.languageDesired2Supported;
    }

    public static void main(String[] args) {
        DistanceTable table = getDefault().languageDesired2Supported;
        if (!table.equals(table.compact())) {
            throw new IllegalArgumentException("Compaction isn't equal");
        }
    }
}
