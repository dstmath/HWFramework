package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBufUtil;
import android_maps_conflict_avoidance.com.google.googlenav.proto.GmmMessageTypes;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ClientPropertiesRequest extends BaseDataRequest {
    private final ProtoBuf properties;

    public ClientPropertiesRequest(ProtoBuf properties) {
        this.properties = properties;
        if (!properties.has(1)) {
            String cohort = readCohortFromFlash();
            if (cohort != null) {
                properties.setString(1, cohort);
            }
        }
    }

    public int getRequestType() {
        return 62;
    }

    public boolean isImmediate() {
        return false;
    }

    public boolean isForeground() {
        return false;
    }

    public void writeRequestData(DataOutput dos) throws IOException {
        ProtoBufUtil.writeProtoBufToOutput(dos, this.properties);
    }

    public boolean readResponseData(DataInput dis) throws IOException {
        ProtoBuf response = ProtoBufUtil.readProtoBufResponse(GmmMessageTypes.CLIENT_PROPERTIES_RESPONSE_PROTO, dis);
        if (response.has(1)) {
            String newCohort = response.getString(1);
            this.properties.setString(1, newCohort);
            StaticUtil.savePreferenceAsString("Cohort", newCohort);
        }
        return true;
    }

    private String readCohortFromFlash() {
        return StaticUtil.readPreferenceAsString("Cohort");
    }
}
