package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.map.Geometry;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import java.util.Hashtable;

public class Placemark {
    private static final EnhancedDataSource[] EMPTY_ENHANCED_DATA_SOURCE = null;
    private static final ImageResource[] EMPTY_IMAGE_SOURCE = null;
    private static final SnippetSource[] EMPTY_SNIPPET_SOURCE = null;
    private Hashtable events;
    private Geometry geometry;
    private byte iconClass;
    private boolean isSelectable;
    private String name;
    private final ProtoBuf proto;
    private int resultType;

    public static class EnhancedDataSource {
    }

    public static class ImageResource {
    }

    public static class SnippetSource {
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.Placemark.<clinit>():void");
    }

    protected Placemark(Geometry geometry, String name) {
        this.name = "";
        this.events = null;
        this.isSelectable = true;
        this.geometry = geometry;
        this.name = name;
        this.proto = null;
        this.iconClass = (byte) 0;
        this.resultType = 7;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }

    public MapPoint getLocation() {
        Geometry location = getGeometry();
        if (location != null) {
            return location.getDefiningPoint();
        }
        return null;
    }

    public String getAddressLine1() {
        return AddressUtil.getAddressLine(4, 0, this.proto);
    }

    public String getAddressLine2() {
        return AddressUtil.getAddressLine(4, 1, this.proto);
    }

    public boolean isKmlResult() {
        return this.resultType == 2;
    }

    public boolean isKmlPlacemark() {
        return this.resultType == 5;
    }

    public boolean isKml() {
        return isKmlPlacemark() || isKmlResult();
    }

    public String getTitle() {
        if (!TextUtil.isEmpty(this.name)) {
            return this.name;
        }
        if (isKml()) {
            return getKmlSupplementalDisplayLine();
        }
        String addressLine1 = getAddressLine1();
        String addressLine2 = getAddressLine2();
        if (Config.isChinaVersion()) {
            if (!addressLine2.equals("")) {
                addressLine1 = addressLine2;
            }
            return addressLine1;
        }
        if (!addressLine1.equals("")) {
            addressLine2 = addressLine1;
        }
        return addressLine2;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        Geometry geometry = getLocation();
        if (geometry != null) {
            str.append(geometry.toString());
        }
        str.append(":");
        str.append(getTitle());
        str.append(":");
        if (getAddressLine1() != null) {
            str.append(getAddressLine1());
        }
        str.append(":");
        if (getAddressLine1() != null) {
            str.append(getAddressLine2());
        }
        return str.toString();
    }

    public String getKmlSnippet() {
        return ProtoBufUtil.getSubProtoValueOrEmpty(this.proto, 90, 92);
    }

    public String getKmlSupplementalDisplayLine() {
        return TextUtil.isEmpty(getAddressLine1()) ? getKmlSnippet() : getAddressLine1();
    }
}
