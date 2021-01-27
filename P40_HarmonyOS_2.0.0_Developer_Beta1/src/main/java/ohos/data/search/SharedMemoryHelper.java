package ohos.data.search;

import android.os.SharedMemory;
import android.system.ErrnoException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ohos.data.search.model.IndexData;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

class SharedMemoryHelper {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "SharedMemoryHelper");
    static final int LARGE_THRESHOLD = 10485760;
    static final int NORMAL_THRESHOLD = 153600;

    SharedMemoryHelper() {
    }

    static void releaseMemory(SharedMemory sharedMemory) {
        if (sharedMemory != null) {
            try {
                SharedMemory.unmap(sharedMemory.mapReadOnly());
            } catch (ErrnoException e) {
                HiLog.error(LABEL, "release sharedMemory failure. %{public}s", new Object[]{e.getMessage()});
            } catch (Throwable th) {
                sharedMemory.close();
                throw th;
            }
            sharedMemory.close();
        }
    }

    static int writeIndexDataList(List<IndexData> list, SharedMemory sharedMemory) throws BufferOverflowException, ErrnoException {
        if (list == null || list.isEmpty() || sharedMemory == null) {
            HiLog.error(LABEL, "writeIndexDatalist indexDatas or sharedMemory is null.", new Object[0]);
            return 0;
        }
        ByteBuffer mapReadWrite = sharedMemory.mapReadWrite();
        mapReadWrite.position(0);
        int size = list.size();
        mapReadWrite.putInt(size);
        for (int i = 0; i < size; i++) {
            Map<String, Object> values = list.get(i).getValues();
            mapReadWrite.putInt(values.size());
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String obj = entry.getValue().toString();
                byte[] bytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
                mapReadWrite.putInt(bytes.length);
                mapReadWrite.put(bytes);
                byte[] bytes2 = obj.getBytes(StandardCharsets.UTF_8);
                mapReadWrite.putInt(bytes2.length);
                mapReadWrite.put(bytes2);
            }
        }
        return mapReadWrite.position();
    }

    static List<IndexData> readIndexDataList(SharedMemory sharedMemory) throws ErrnoException {
        if (sharedMemory == null) {
            HiLog.error(LABEL, "readIndexDataList failed: sharedMemory is null", new Object[0]);
            return Collections.emptyList();
        }
        ByteBuffer mapReadOnly = sharedMemory.mapReadOnly();
        int i = mapReadOnly.getInt();
        ArrayList arrayList = new ArrayList(i);
        for (int i2 = 0; i2 < i; i2++) {
            arrayList.add(readIndexData(mapReadOnly));
        }
        return arrayList;
    }

    static IndexData readIndexData(ByteBuffer byteBuffer) {
        IndexData indexData = new IndexData();
        int i = byteBuffer.getInt();
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = byteBuffer.getInt();
            byte[] bArr = new byte[i3];
            byteBuffer.get(bArr, 0, i3);
            String str = new String(bArr, StandardCharsets.UTF_8);
            int i4 = byteBuffer.getInt();
            byte[] bArr2 = new byte[i4];
            byteBuffer.get(bArr2, 0, i4);
            indexData.put(str, new String(bArr2, StandardCharsets.UTF_8));
        }
        return indexData;
    }
}
