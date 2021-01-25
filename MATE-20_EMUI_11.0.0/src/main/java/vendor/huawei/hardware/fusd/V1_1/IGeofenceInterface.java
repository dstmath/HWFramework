package vendor.huawei.hardware.fusd.V1_1;

import android.hardware.gnss.V1_0.IGnssGeofenceCallback;
import android.hardware.gnss.V1_0.IGnssGeofencing;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.server.appactcontrol.AppActConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IGeofenceInterface extends IGnssGeofencing {
    public static final String kInterfaceName = "vendor.huawei.hardware.fusd@1.1::IGeofenceInterface";

    void addGeofences(ArrayList<GeofenceRequest> arrayList) throws RemoteException;

    void addPolygonGeofences(ArrayList<GeofencePolygonRequest> arrayList) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    int getGeofenceStatus(int i) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    void removeGeofences(ArrayList<Integer> arrayList) throws RemoteException;

    void setGeofenceCallback(IGeofenceCallback iGeofenceCallback) throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IGeofenceInterface asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IGeofenceInterface)) {
            return (IGeofenceInterface) iface;
        }
        IGeofenceInterface proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IGeofenceInterface castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IGeofenceInterface getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IGeofenceInterface getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IGeofenceInterface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IGeofenceInterface getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class Point {
        public double latitudeDegrees;
        public double longitudeDegrees;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != Point.class) {
                return false;
            }
            Point other = (Point) otherObject;
            if (this.latitudeDegrees == other.latitudeDegrees && this.longitudeDegrees == other.longitudeDegrees) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.latitudeDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.longitudeDegrees))));
        }

        public final String toString() {
            return "{.latitudeDegrees = " + this.latitudeDegrees + ", .longitudeDegrees = " + this.longitudeDegrees + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(16), 0);
        }

        public static final ArrayList<Point> readVectorFromParcel(HwParcel parcel) {
            ArrayList<Point> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                Point _hidl_vec_element = new Point();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.latitudeDegrees = _hidl_blob.getDouble(0 + _hidl_offset);
            this.longitudeDegrees = _hidl_blob.getDouble(8 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(16);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Point> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putDouble(0 + _hidl_offset, this.latitudeDegrees);
            _hidl_blob.putDouble(8 + _hidl_offset, this.longitudeDegrees);
        }
    }

    public static final class GeofenceRequest {
        public int accuracy;
        public int geofenceId;
        public int lastTransition;
        public int monitorTransitions;
        public int notificationResponsivenessMs;
        public Point point = new Point();
        public double radius;
        public int sources_to_use;
        public int unknownTimerMs;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != GeofenceRequest.class) {
                return false;
            }
            GeofenceRequest other = (GeofenceRequest) otherObject;
            if (this.geofenceId == other.geofenceId && HidlSupport.deepEquals(this.point, other.point) && this.radius == other.radius && this.accuracy == other.accuracy && this.notificationResponsivenessMs == other.notificationResponsivenessMs && this.unknownTimerMs == other.unknownTimerMs && HidlSupport.deepEquals(Integer.valueOf(this.monitorTransitions), Integer.valueOf(other.monitorTransitions)) && this.lastTransition == other.lastTransition && this.sources_to_use == other.sources_to_use) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.geofenceId))), Integer.valueOf(HidlSupport.deepHashCode(this.point)), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.radius))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.accuracy))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.notificationResponsivenessMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.unknownTimerMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.monitorTransitions))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lastTransition))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sources_to_use))));
        }

        public final String toString() {
            return "{.geofenceId = " + this.geofenceId + ", .point = " + this.point + ", .radius = " + this.radius + ", .accuracy = " + this.accuracy + ", .notificationResponsivenessMs = " + this.notificationResponsivenessMs + ", .unknownTimerMs = " + this.unknownTimerMs + ", .monitorTransitions = " + IGnssGeofenceCallback.GeofenceTransition.dumpBitfield(this.monitorTransitions) + ", .lastTransition = " + IGnssGeofenceCallback.GeofenceTransition.toString(this.lastTransition) + ", .sources_to_use = " + this.sources_to_use + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
        }

        public static final ArrayList<GeofenceRequest> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GeofenceRequest> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GeofenceRequest _hidl_vec_element = new GeofenceRequest();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.geofenceId = _hidl_blob.getInt32(0 + _hidl_offset);
            this.point.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
            this.radius = _hidl_blob.getDouble(24 + _hidl_offset);
            this.accuracy = _hidl_blob.getInt32(32 + _hidl_offset);
            this.notificationResponsivenessMs = _hidl_blob.getInt32(36 + _hidl_offset);
            this.unknownTimerMs = _hidl_blob.getInt32(40 + _hidl_offset);
            this.monitorTransitions = _hidl_blob.getInt32(44 + _hidl_offset);
            this.lastTransition = _hidl_blob.getInt32(48 + _hidl_offset);
            this.sources_to_use = _hidl_blob.getInt32(52 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(56);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GeofenceRequest> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt32(0 + _hidl_offset, this.geofenceId);
            this.point.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
            _hidl_blob.putDouble(24 + _hidl_offset, this.radius);
            _hidl_blob.putInt32(32 + _hidl_offset, this.accuracy);
            _hidl_blob.putInt32(36 + _hidl_offset, this.notificationResponsivenessMs);
            _hidl_blob.putInt32(40 + _hidl_offset, this.unknownTimerMs);
            _hidl_blob.putInt32(44 + _hidl_offset, this.monitorTransitions);
            _hidl_blob.putInt32(48 + _hidl_offset, this.lastTransition);
            _hidl_blob.putInt32(52 + _hidl_offset, this.sources_to_use);
        }
    }

    public static final class GeofencePolygonRequest {
        public int accuracy;
        public int geofenceId;
        public int lastTransition;
        public int monitorTransitions;
        public int notificationResponsivenessMs;
        public ArrayList<Point> points = new ArrayList<>();
        public int sources_to_use;
        public int unknownTimerMs;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != GeofencePolygonRequest.class) {
                return false;
            }
            GeofencePolygonRequest other = (GeofencePolygonRequest) otherObject;
            if (this.geofenceId == other.geofenceId && HidlSupport.deepEquals(this.points, other.points) && this.accuracy == other.accuracy && this.notificationResponsivenessMs == other.notificationResponsivenessMs && this.unknownTimerMs == other.unknownTimerMs && HidlSupport.deepEquals(Integer.valueOf(this.monitorTransitions), Integer.valueOf(other.monitorTransitions)) && this.lastTransition == other.lastTransition && this.sources_to_use == other.sources_to_use) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.geofenceId))), Integer.valueOf(HidlSupport.deepHashCode(this.points)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.accuracy))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.notificationResponsivenessMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.unknownTimerMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.monitorTransitions))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lastTransition))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sources_to_use))));
        }

        public final String toString() {
            return "{.geofenceId = " + this.geofenceId + ", .points = " + this.points + ", .accuracy = " + this.accuracy + ", .notificationResponsivenessMs = " + this.notificationResponsivenessMs + ", .unknownTimerMs = " + this.unknownTimerMs + ", .monitorTransitions = " + IGnssGeofenceCallback.GeofenceTransition.dumpBitfield(this.monitorTransitions) + ", .lastTransition = " + IGnssGeofenceCallback.GeofenceTransition.toString(this.lastTransition) + ", .sources_to_use = " + this.sources_to_use + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
        }

        public static final ArrayList<GeofencePolygonRequest> readVectorFromParcel(HwParcel parcel) {
            ArrayList<GeofencePolygonRequest> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                GeofencePolygonRequest _hidl_vec_element = new GeofencePolygonRequest();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.geofenceId = _hidl_blob.getInt32(_hidl_offset + 0);
            int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
            this.points.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                Point _hidl_vec_element = new Point();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
                this.points.add(_hidl_vec_element);
            }
            this.accuracy = _hidl_blob.getInt32(_hidl_offset + 24);
            this.notificationResponsivenessMs = _hidl_blob.getInt32(_hidl_offset + 28);
            this.unknownTimerMs = _hidl_blob.getInt32(_hidl_offset + 32);
            this.monitorTransitions = _hidl_blob.getInt32(_hidl_offset + 36);
            this.lastTransition = _hidl_blob.getInt32(_hidl_offset + 40);
            this.sources_to_use = _hidl_blob.getInt32(_hidl_offset + 44);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(48);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GeofencePolygonRequest> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt32(_hidl_offset + 0, this.geofenceId);
            int _hidl_vec_size = this.points.size();
            _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
            _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.points.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
            }
            _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
            _hidl_blob.putInt32(24 + _hidl_offset, this.accuracy);
            _hidl_blob.putInt32(28 + _hidl_offset, this.notificationResponsivenessMs);
            _hidl_blob.putInt32(32 + _hidl_offset, this.unknownTimerMs);
            _hidl_blob.putInt32(36 + _hidl_offset, this.monitorTransitions);
            _hidl_blob.putInt32(40 + _hidl_offset, this.lastTransition);
            _hidl_blob.putInt32(44 + _hidl_offset, this.sources_to_use);
        }
    }

    public static final class Proxy implements IGeofenceInterface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.fusd@1.1::IGeofenceInterface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.gnss.V1_0.IGnssGeofencing
        public void setCallback(IGnssGeofenceCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssGeofencing.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssGeofencing
        public void addGeofence(int geofenceId, double latitudeDegrees, double longitudeDegrees, double radiusMeters, int lastTransition, int monitorTransitions, int notificationResponsivenessMs, int unknownTimerMs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssGeofencing.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            _hidl_request.writeDouble(latitudeDegrees);
            _hidl_request.writeDouble(longitudeDegrees);
            _hidl_request.writeDouble(radiusMeters);
            _hidl_request.writeInt32(lastTransition);
            _hidl_request.writeInt32(monitorTransitions);
            _hidl_request.writeInt32(notificationResponsivenessMs);
            _hidl_request.writeInt32(unknownTimerMs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssGeofencing
        public void pauseGeofence(int geofenceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssGeofencing.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssGeofencing
        public void resumeGeofence(int geofenceId, int monitorTransitions) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssGeofencing.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            _hidl_request.writeInt32(monitorTransitions);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.gnss.V1_0.IGnssGeofencing
        public void removeGeofence(int geofenceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGnssGeofencing.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface
        public void setGeofenceCallback(IGeofenceCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGeofenceInterface.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface
        public void addGeofences(ArrayList<GeofenceRequest> geofences) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGeofenceInterface.kInterfaceName);
            GeofenceRequest.writeVectorToParcel(_hidl_request, geofences);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface
        public void addPolygonGeofences(ArrayList<GeofencePolygonRequest> geofences) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGeofenceInterface.kInterfaceName);
            GeofencePolygonRequest.writeVectorToParcel(_hidl_request, geofences);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface
        public void removeGeofences(ArrayList<Integer> geofences) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGeofenceInterface.kInterfaceName);
            _hidl_request.writeInt32Vector(geofences);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface
        public int getGeofenceStatus(int geofenceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IGeofenceInterface.kInterfaceName);
            _hidl_request.writeInt32(geofenceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IGeofenceInterface {
        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IGeofenceInterface.kInterfaceName, IGnssGeofencing.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IGeofenceInterface.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{113, 7, -28, 14, -55, -96, 88, -24, 87, -25, -120, -110, 96, -87, 100, Byte.MIN_VALUE, -90, -120, 69, -65, 79, -46, -49, 5, -30, 23, 89, 25, -86, -52, 27, 10}, new byte[]{-7, 14, 77, -36, 101, 39, 6, 41, -99, -114, 61, -117, -95, -114, 7, 69, -61, -70, -23, -65, 77, 27, -26, -67, 6, -39, -63, -11, 14, -56, -46, -118}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IGeofenceInterface, android.hardware.gnss.V1_0.IGnssGeofencing, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IGeofenceInterface.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssGeofencing.kInterfaceName);
                    setCallback(IGnssGeofenceCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssGeofencing.kInterfaceName);
                    addGeofence(_hidl_request.readInt32(), _hidl_request.readDouble(), _hidl_request.readDouble(), _hidl_request.readDouble(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssGeofencing.kInterfaceName);
                    pauseGeofence(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssGeofencing.kInterfaceName);
                    resumeGeofence(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGnssGeofencing.kInterfaceName);
                    removeGeofence(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGeofenceInterface.kInterfaceName);
                    setGeofenceCallback(IGeofenceCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGeofenceInterface.kInterfaceName);
                    addGeofences(GeofenceRequest.readVectorFromParcel(_hidl_request));
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGeofenceInterface.kInterfaceName);
                    addPolygonGeofences(GeofencePolygonRequest.readVectorFromParcel(_hidl_request));
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGeofenceInterface.kInterfaceName);
                    removeGeofences(_hidl_request.readInt32Vector());
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IGeofenceInterface.kInterfaceName);
                    int _hidl_out_status = getGeofenceStatus(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            _hidl_reply.writeStatus(0);
                            _hidl_out_info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }
}
