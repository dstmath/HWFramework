package tmsdk.bg.module.network;

import java.io.File;
import tmsdkobf.ms;

/* compiled from: Unknown */
final class e extends d {

    /* compiled from: Unknown */
    private static final class a implements NetDataEntityFactory {
        private f xX;
        private String xY;

        public a(f fVar) {
            this.xY = g.dU();
            this.xX = fVar;
        }

        public NetDataEntity getNetDataEntity() {
            NetDataEntity netDataEntity = new NetDataEntity();
            long[] jArr = new long[]{0, 0, 0, 0};
            String[] b = ms.b(new File(this.xY));
            if (b != null) {
                try {
                    for (String trim : b) {
                        String[] split = trim.trim().split("[:\\s]+");
                        if (this.xX.ca(split[0].trim().toLowerCase())) {
                            jArr[0] = jArr[0] + Long.parseLong(split[1]);
                            jArr[1] = jArr[1] + Long.parseLong(split[2]);
                            jArr[2] = jArr[2] + Long.parseLong(split[9]);
                            jArr[3] = jArr[3] + Long.parseLong(split[10]);
                        }
                    }
                } catch (Exception e) {
                }
            }
            netDataEntity.mReceiver = jArr[0];
            netDataEntity.mReceiverPks = jArr[1];
            netDataEntity.mTranslate = jArr[2];
            netDataEntity.mTranslatePks = jArr[3];
            return netDataEntity;
        }

        public void networkConnectivityChangeNotify() {
        }
    }

    public e(INetworkInfoDao iNetworkInfoDao, f fVar) {
        super(new a(fVar), iNetworkInfoDao);
    }
}
