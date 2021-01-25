package ohos.data.usage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public interface IDataUsage {
    void formatVolume(String str) throws RemoteException;

    MountState getDiskMountedStatus() throws RemoteException;

    MountState getDiskMountedStatus(File file) throws RemoteException;

    Optional<Volume> getPrimaryVolume() throws RemoteException;

    Optional<Volume> getVolume(File file) throws RemoteException;

    Optional<Volume> getVolume(Context context, Uri uri) throws RemoteException;

    Optional<List<VolumeView>> getVolumeViews() throws RemoteException;

    Optional<List<Volume>> getVolumes() throws RemoteException;

    void installVolume(String str) throws RemoteException;

    boolean isDiskEmulated() throws RemoteException;

    boolean isDiskEmulated(File file) throws RemoteException;

    boolean isDiskPluggable() throws RemoteException;

    boolean isDiskPluggable(File file) throws RemoteException;

    void partitionMixedDisk(String str, int i) throws RemoteException;

    void partitionPrivateDisk(String str) throws RemoteException;

    void partitionPublicDisk(String str) throws RemoteException;

    boolean registerCallback(IDataUsageCallback iDataUsageCallback) throws RemoteException;

    boolean unRegisterCallback(IDataUsageCallback iDataUsageCallback) throws RemoteException;

    void uninstallVolume(String str) throws RemoteException;
}
