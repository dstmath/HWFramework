package com.android.server.broadcastradio.hal2;

import android.hardware.broadcastradio.V2_0.AmFmBandRange;
import android.hardware.broadcastradio.V2_0.AmFmRegionConfig;
import android.hardware.broadcastradio.V2_0.DabTableEntry;
import android.hardware.broadcastradio.V2_0.Metadata;
import android.hardware.broadcastradio.V2_0.MetadataKey;
import android.hardware.broadcastradio.V2_0.ProgramFilter;
import android.hardware.broadcastradio.V2_0.ProgramIdentifier;
import android.hardware.broadcastradio.V2_0.ProgramInfo;
import android.hardware.broadcastradio.V2_0.ProgramListChunk;
import android.hardware.broadcastradio.V2_0.Properties;
import android.hardware.broadcastradio.V2_0.VendorKeyValue;
import android.hardware.radio.Announcement;
import android.hardware.radio.ProgramList;
import android.hardware.radio.ProgramSelector;
import android.hardware.radio.RadioManager;
import android.hardware.radio.RadioMetadata;
import android.os.ParcelableException;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* access modifiers changed from: package-private */
public class Convert {
    private static final String TAG = "BcRadio2Srv.convert";
    private static final Map<Integer, MetadataDef> metadataKeys = new HashMap();

    /* access modifiers changed from: private */
    public enum MetadataType {
        INT,
        STRING
    }

    Convert() {
    }

    static void throwOnError(String action, int result) {
        switch (result) {
            case 0:
                return;
            case 1:
                throw new ParcelableException(new RuntimeException(action + ": UNKNOWN_ERROR"));
            case 2:
                throw new ParcelableException(new RuntimeException(action + ": INTERNAL_ERROR"));
            case 3:
                throw new IllegalArgumentException(action + ": INVALID_ARGUMENTS");
            case 4:
                throw new IllegalStateException(action + ": INVALID_STATE");
            case 5:
                throw new UnsupportedOperationException(action + ": NOT_SUPPORTED");
            case 6:
                throw new ParcelableException(new RuntimeException(action + ": TIMEOUT"));
            default:
                throw new ParcelableException(new RuntimeException(action + ": unknown error (" + result + ")"));
        }
    }

    static ArrayList<VendorKeyValue> vendorInfoToHal(Map<String, String> info) {
        if (info == null) {
            return new ArrayList<>();
        }
        ArrayList<VendorKeyValue> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            VendorKeyValue elem = new VendorKeyValue();
            elem.key = entry.getKey();
            elem.value = entry.getValue();
            if (elem.key == null || elem.value == null) {
                Slog.w(TAG, "VendorKeyValue contains null pointers");
            } else {
                list.add(elem);
            }
        }
        return list;
    }

    static Map<String, String> vendorInfoFromHal(List<VendorKeyValue> info) {
        if (info == null) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (VendorKeyValue kvp : info) {
            if (kvp.key == null || kvp.value == null) {
                Slog.w(TAG, "VendorKeyValue contains null pointers");
            } else {
                map.put(kvp.key, kvp.value);
            }
        }
        return map;
    }

    private static int identifierTypeToProgramType(int idType) {
        switch (idType) {
            case 1:
            case 2:
                return 2;
            case 3:
                return 4;
            case 4:
            case 11:
            default:
                if (idType < 1000 || idType > 1999) {
                    return 0;
                }
                return idType;
            case 5:
            case 6:
            case 7:
            case 8:
                return 5;
            case 9:
            case 10:
                return 6;
            case 12:
            case 13:
                return 7;
        }
    }

    private static int[] identifierTypesToProgramTypes(int[] idTypes) {
        Set<Integer> pTypes = new HashSet<>();
        for (int idType : idTypes) {
            int pType = identifierTypeToProgramType(idType);
            if (pType != 0) {
                pTypes.add(Integer.valueOf(pType));
                if (pType == 2) {
                    pTypes.add(1);
                }
                if (pType == 4) {
                    pTypes.add(3);
                }
            }
        }
        return pTypes.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    private static RadioManager.BandDescriptor[] amfmConfigToBands(AmFmRegionConfig config) {
        if (config == null) {
            return new RadioManager.BandDescriptor[0];
        }
        List<RadioManager.BandDescriptor> bands = new ArrayList<>(config.ranges.size());
        Iterator<AmFmBandRange> it = config.ranges.iterator();
        while (it.hasNext()) {
            AmFmBandRange range = it.next();
            FrequencyBand bandType = Utils.getBand(range.lowerBound);
            if (bandType == FrequencyBand.UNKNOWN) {
                Slog.e(TAG, "Unknown frequency band at " + range.lowerBound + "kHz");
            } else if (bandType == FrequencyBand.FM) {
                bands.add(new RadioManager.FmBandDescriptor(0, 1, range.lowerBound, range.upperBound, range.spacing, true, true, true, true, true));
            } else {
                bands.add(new RadioManager.AmBandDescriptor(0, 0, range.lowerBound, range.upperBound, range.spacing, true));
            }
        }
        return (RadioManager.BandDescriptor[]) bands.toArray(new RadioManager.BandDescriptor[bands.size()]);
    }

    private static Map<String, Integer> dabConfigFromHal(List<DabTableEntry> config) {
        if (config == null) {
            return null;
        }
        return (Map) config.stream().collect(Collectors.toMap($$Lambda$Convert$0bmoVGH8L6ZLkm_awAwTERGOlZU.INSTANCE, $$Lambda$Convert$5i7ED5vyX8wi_iS2sa2DsapHYc0.INSTANCE));
    }

    static RadioManager.ModuleProperties propertiesFromHal(int id, String serviceName, Properties prop, AmFmRegionConfig amfmConfig, List<DabTableEntry> dabConfig) {
        Objects.requireNonNull(serviceName);
        Objects.requireNonNull(prop);
        int[] supportedIdentifierTypes = prop.supportedIdentifierTypes.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
        return new RadioManager.ModuleProperties(id, serviceName, 0, prop.maker, prop.product, prop.version, prop.serial, 1, 1, false, false, amfmConfigToBands(amfmConfig), true, identifierTypesToProgramTypes(supportedIdentifierTypes), supportedIdentifierTypes, dabConfigFromHal(dabConfig), vendorInfoFromHal(prop.vendorInfo));
    }

    static void programIdentifierToHal(ProgramIdentifier hwId, ProgramSelector.Identifier id) {
        hwId.type = id.getType();
        hwId.value = id.getValue();
    }

    static ProgramIdentifier programIdentifierToHal(ProgramSelector.Identifier id) {
        ProgramIdentifier hwId = new ProgramIdentifier();
        programIdentifierToHal(hwId, id);
        return hwId;
    }

    static ProgramSelector.Identifier programIdentifierFromHal(ProgramIdentifier id) {
        if (id.type == 0) {
            return null;
        }
        return new ProgramSelector.Identifier(id.type, id.value);
    }

    static android.hardware.broadcastradio.V2_0.ProgramSelector programSelectorToHal(ProgramSelector sel) {
        android.hardware.broadcastradio.V2_0.ProgramSelector hwSel = new android.hardware.broadcastradio.V2_0.ProgramSelector();
        programIdentifierToHal(hwSel.primaryId, sel.getPrimaryId());
        Stream map = Arrays.stream(sel.getSecondaryIds()).map($$Lambda$Wd4_5eHLstX9rw52AhlvWR6dfo.INSTANCE);
        ArrayList<ProgramIdentifier> arrayList = hwSel.secondaryIds;
        Objects.requireNonNull(arrayList);
        map.forEachOrdered(new Consumer(arrayList) {
            /* class com.android.server.broadcastradio.hal2.$$Lambda$pxxBeAmtGFx0TmOA6MMwqs_fi0 */
            private final /* synthetic */ ArrayList f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add((ProgramIdentifier) obj);
            }
        });
        return hwSel;
    }

    static ProgramSelector programSelectorFromHal(android.hardware.broadcastradio.V2_0.ProgramSelector sel) {
        return new ProgramSelector(identifierTypeToProgramType(sel.primaryId.type), (ProgramSelector.Identifier) Objects.requireNonNull(programIdentifierFromHal(sel.primaryId)), (ProgramSelector.Identifier[]) sel.secondaryIds.stream().map($$Lambda$LWyJkMwbnlAczPrMYjWRbiucHI4.INSTANCE).map($$Lambda$orrX1qQ1nXd8k5pLkjug2DaCbzI.INSTANCE).toArray($$Lambda$Convert$Dv81zZf4dKkZJxVC6zm8CtQ2wE.INSTANCE), (long[]) null);
    }

    static /* synthetic */ ProgramSelector.Identifier[] lambda$programSelectorFromHal$2(int x$0) {
        return new ProgramSelector.Identifier[x$0];
    }

    /* access modifiers changed from: private */
    public static class MetadataDef {
        private String key;
        private MetadataType type;

        private MetadataDef(MetadataType type2, String key2) {
            this.type = type2;
            this.key = key2;
        }
    }

    static {
        metadataKeys.put(1, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.RDS_PS"));
        metadataKeys.put(2, new MetadataDef(MetadataType.INT, "android.hardware.radio.metadata.RDS_PTY"));
        metadataKeys.put(3, new MetadataDef(MetadataType.INT, "android.hardware.radio.metadata.RBDS_PTY"));
        metadataKeys.put(4, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.RDS_RT"));
        metadataKeys.put(5, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.TITLE"));
        metadataKeys.put(6, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.ARTIST"));
        metadataKeys.put(7, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.ALBUM"));
        metadataKeys.put(8, new MetadataDef(MetadataType.INT, "android.hardware.radio.metadata.ICON"));
        metadataKeys.put(9, new MetadataDef(MetadataType.INT, "android.hardware.radio.metadata.ART"));
        metadataKeys.put(10, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.PROGRAM_NAME"));
        metadataKeys.put(11, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_ENSEMBLE_NAME"));
        metadataKeys.put(12, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_ENSEMBLE_NAME_SHORT"));
        metadataKeys.put(13, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_SERVICE_NAME"));
        metadataKeys.put(14, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_SERVICE_NAME_SHORT"));
        metadataKeys.put(15, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_COMPONENT_NAME"));
        metadataKeys.put(16, new MetadataDef(MetadataType.STRING, "android.hardware.radio.metadata.DAB_COMPONENT_NAME_SHORT"));
    }

    private static RadioMetadata metadataFromHal(ArrayList<Metadata> meta) {
        RadioMetadata.Builder builder = new RadioMetadata.Builder();
        Iterator<Metadata> it = meta.iterator();
        while (it.hasNext()) {
            Metadata entry = it.next();
            MetadataDef keyDef = metadataKeys.get(Integer.valueOf(entry.key));
            if (keyDef == null) {
                Slog.i(TAG, "Ignored unknown metadata entry: " + MetadataKey.toString(entry.key));
            } else if (keyDef.type == MetadataType.STRING) {
                builder.putString(keyDef.key, entry.stringValue);
            } else {
                builder.putInt(keyDef.key, (int) entry.intValue);
            }
        }
        return builder.build();
    }

    /* access modifiers changed from: package-private */
    public static RadioManager.ProgramInfo programInfoFromHal(ProgramInfo info) {
        return new RadioManager.ProgramInfo(programSelectorFromHal(info.selector), programIdentifierFromHal(info.logicallyTunedTo), programIdentifierFromHal(info.physicallyTunedTo), (Collection) info.relatedContent.stream().map($$Lambda$Convert$HR1t3HnLMLNA3jZqzjEAao66N98.INSTANCE).collect(Collectors.toList()), info.infoFlags, info.signalQuality, metadataFromHal(info.metadata), vendorInfoFromHal(info.vendorInfo));
    }

    static /* synthetic */ ProgramSelector.Identifier lambda$programInfoFromHal$3(ProgramIdentifier id) {
        return (ProgramSelector.Identifier) Objects.requireNonNull(programIdentifierFromHal(id));
    }

    static ProgramFilter programFilterToHal(ProgramList.Filter filter) {
        if (filter == null) {
            filter = new ProgramList.Filter();
        }
        ProgramFilter hwFilter = new ProgramFilter();
        Stream stream = filter.getIdentifierTypes().stream();
        ArrayList<Integer> arrayList = hwFilter.identifierTypes;
        Objects.requireNonNull(arrayList);
        stream.forEachOrdered(new Consumer(arrayList) {
            /* class com.android.server.broadcastradio.hal2.$$Lambda$A5EcwDaVFNoKb_4sV0_1Yu9f7d4 */
            private final /* synthetic */ ArrayList f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add((Integer) obj);
            }
        });
        filter.getIdentifiers().stream().forEachOrdered(new Consumer() {
            /* class com.android.server.broadcastradio.hal2.$$Lambda$Convert$4vXI6RX0VlxWE_lYnJTBSNvMM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ProgramFilter.this.identifiers.add(Convert.programIdentifierToHal((ProgramSelector.Identifier) obj));
            }
        });
        hwFilter.includeCategories = filter.areCategoriesIncluded();
        hwFilter.excludeModifications = filter.areModificationsExcluded();
        return hwFilter;
    }

    static ProgramList.Chunk programListChunkFromHal(ProgramListChunk chunk) {
        return new ProgramList.Chunk(chunk.purge, chunk.complete, (Set) chunk.modified.stream().map($$Lambda$Convert$P20z6nVni7Z0919gQM2S9sxbM.INSTANCE).collect(Collectors.toSet()), (Set) chunk.removed.stream().map($$Lambda$Convert$uleEnQPvLcMEC_sDr7j1KaT0.INSTANCE).collect(Collectors.toSet()));
    }

    static /* synthetic */ ProgramSelector.Identifier lambda$programListChunkFromHal$6(ProgramIdentifier id) {
        return (ProgramSelector.Identifier) Objects.requireNonNull(programIdentifierFromHal(id));
    }

    public static Announcement announcementFromHal(android.hardware.broadcastradio.V2_0.Announcement hwAnnouncement) {
        return new Announcement(programSelectorFromHal(hwAnnouncement.selector), hwAnnouncement.type, vendorInfoFromHal(hwAnnouncement.vendorInfo));
    }

    static <T> ArrayList<T> listToArrayList(List<T> list) {
        if (list == null) {
            return null;
        }
        if (list instanceof ArrayList) {
            return (ArrayList) list;
        }
        return new ArrayList<>(list);
    }
}
