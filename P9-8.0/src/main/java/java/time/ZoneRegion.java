package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

final class ZoneRegion extends ZoneId implements Serializable {
    private static final long serialVersionUID = 8386373296231747096L;
    private final String id;
    private final transient ZoneRules rules;

    static ZoneRegion ofId(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull((Object) zoneId, "zoneId");
        checkName(zoneId);
        ZoneRules rules = null;
        try {
            rules = ZoneRulesProvider.getRules(zoneId, true);
        } catch (ZoneRulesException ex) {
            if (checkAvailable) {
                throw ex;
            }
        }
        return new ZoneRegion(zoneId, rules);
    }

    private static void checkName(String zoneId) {
        int n = zoneId.length();
        if (n < 2) {
            throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
        }
        int i = 0;
        while (i < n) {
            char c = zoneId.charAt(i);
            if ((c < 'a' || c > 'z') && ((c < 'A' || c > 'Z') && ((c != '/' || i == 0) && ((c < '0' || c > '9' || i == 0) && ((c != '~' || i == 0) && ((c != '.' || i == 0) && ((c != '_' || i == 0) && ((c != '+' || i == 0) && (c != '-' || i == 0))))))))) {
                throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
            }
            i++;
        }
    }

    ZoneRegion(String id, ZoneRules rules) {
        this.id = id;
        this.rules = rules;
    }

    public String getId() {
        return this.id;
    }

    public ZoneRules getRules() {
        return this.rules != null ? this.rules : ZoneRulesProvider.getRules(this.id, false);
    }

    private Object writeReplace() {
        return new Ser((byte) 7, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void write(DataOutput out) throws IOException {
        out.writeByte(7);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(this.id);
    }

    static ZoneId readExternal(DataInput in) throws IOException {
        return ZoneId.of(in.readUTF(), false);
    }
}
