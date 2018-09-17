package tmsdkobf;

import android.os.Handler;
import android.os.Message;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdkobf.ro.a;

/* compiled from: Unknown */
public class rs {
    private AtomicBoolean JP;

    public rs() {
        this.JP = new AtomicBoolean();
    }

    private ArrayList<ArrayList<Integer>> E(ArrayList<a> arrayList) {
        int i = 0;
        ArrayList<ArrayList<Integer>> arrayList2 = new ArrayList();
        HashSet hashSet = new HashSet();
        F(arrayList);
        int i2 = 0;
        while (i2 < arrayList.size()) {
            if (this.JP.get()) {
                return null;
            }
            ArrayList b = b(i2, arrayList2);
            QQImageFeatureHSV qQImageFeatureHSV = ((a) arrayList.get(i2)).Og;
            int i3 = i2 + 1;
            while (i3 < arrayList.size()) {
                QQImageFeatureHSV qQImageFeatureHSV2 = ((a) arrayList.get(i3)).Og;
                if (!(qQImageFeatureHSV == null || qQImageFeatureHSV2 == null || rn.a(qQImageFeatureHSV, qQImageFeatureHSV2) < 75 || hashSet.contains(Integer.valueOf(i3)))) {
                    hashSet.add(Integer.valueOf(i3));
                    b.add(Integer.valueOf(i3));
                }
                i3++;
            }
            if (!(b.isEmpty() || hashSet.contains(Integer.valueOf(i2)))) {
                hashSet.add(Integer.valueOf(i2));
                b.add(Integer.valueOf(i2));
            }
            i2++;
        }
        while (i < arrayList.size()) {
            a aVar = (a) arrayList.get(i);
            if (aVar.Og != null) {
                aVar.Og.finish();
                aVar.Og = null;
            }
            i++;
        }
        return arrayList2;
    }

    private void F(ArrayList<a> arrayList) {
        for (int i = 0; i < arrayList.size() && !this.JP.get(); i++) {
            a aVar = (a) arrayList.get(i);
            aVar.Og = rn.a(aVar);
        }
    }

    private List<ro> J(List<rq> list) {
        List arrayList = new ArrayList();
        for (int size = list.size() - 1; size >= 0; size--) {
            if (this.JP.get()) {
                return null;
            }
            rq rqVar = (rq) list.get(size);
            if (!rqVar.mIsScreenShot) {
                a(arrayList, new a(rqVar));
            }
        }
        return arrayList;
    }

    private long[] K(List<ro> list) {
        long j = 0;
        int i = 0;
        for (ro roVar : list) {
            i += roVar.mItemList.size();
            Iterator it = roVar.mItemList.iterator();
            while (it.hasNext()) {
                j += ((a) it.next()).mSize;
            }
        }
        return new long[]{(long) i, j};
    }

    private List<ro> a(ro roVar) {
        List<ro> arrayList = new ArrayList();
        ArrayList E = E(roVar.mItemList);
        if (this.JP.get() || E == null) {
            return null;
        }
        for (int i = 0; i < E.size(); i++) {
            if (this.JP.get()) {
                return null;
            }
            ArrayList arrayList2 = (ArrayList) E.get(i);
            if (!(arrayList2 == null || arrayList2.isEmpty())) {
                ro roVar2 = new ro(roVar);
                Iterator it = arrayList2.iterator();
                while (it.hasNext()) {
                    roVar2.mItemList.add((a) roVar.mItemList.get(((Integer) it.next()).intValue()));
                }
                b(roVar2);
                arrayList.add(roVar2);
            }
        }
        return arrayList;
    }

    private void a(List<ro> list, a aVar) {
        for (ro roVar : list) {
            Object obj;
            if (roVar.mTime - aVar.mTime > 30000) {
                obj = 1;
                continue;
            } else {
                obj = null;
                continue;
            }
            if (obj == null) {
                roVar.mItemList.add(aVar);
                return;
            }
        }
        ro roVar2 = new ro(aVar.mTime);
        roVar2.mTime = aVar.mTime;
        roVar2.mItemList.add(aVar);
        list.add(roVar2);
    }

    private ArrayList<Integer> b(int i, ArrayList<ArrayList<Integer>> arrayList) {
        ArrayList<Integer> arrayList2;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2 = (ArrayList) it.next();
            if (arrayList2.contains(Integer.valueOf(i))) {
                return arrayList2;
            }
        }
        arrayList2 = new ArrayList();
        arrayList.add(arrayList2);
        return arrayList2;
    }

    private void b(ro roVar) {
        ArrayList arrayList = roVar.mItemList;
        for (int i = 0; i < arrayList.size(); i++) {
            if (i == 0) {
                ((a) arrayList.get(i)).mSelected = false;
            } else {
                ((a) arrayList.get(i)).mSelected = true;
            }
        }
    }

    public List<ro> a(ArrayList<rq> arrayList, Handler handler) {
        this.JP.set(false);
        if (handler == null || arrayList == null) {
            d.c("SimilarPhotoProcesser", "startScan parameter is null");
            return null;
        }
        List<ro> arrayList2 = new ArrayList();
        List J = J(arrayList);
        if (this.JP.get()) {
            handler.sendMessage(handler.obtainMessage(4356));
            return null;
        }
        Message obtainMessage;
        if (J == null) {
            d.c("SimilarPhotoProcesser", "startScan sort get null result");
            obtainMessage = handler.obtainMessage(4357);
            obtainMessage.arg1 = SpaceManager.ERROR_CODE_UNKNOW;
            obtainMessage.obj = null;
            handler.sendMessage(obtainMessage);
        }
        int i = (int) K(J)[0];
        int i2 = 0;
        for (int i3 = 0; i3 < J.size(); i3++) {
            if (this.JP.get()) {
                handler.sendMessage(handler.obtainMessage(4356));
                return null;
            }
            ro roVar = (ro) J.get(i3);
            Collection a = a(roVar);
            if (this.JP.get()) {
                handler.sendMessage(handler.obtainMessage(4356));
                return null;
            }
            i2 += roVar.mItemList.size();
            arrayList2.addAll(a);
            obtainMessage = handler.obtainMessage(4354);
            obtainMessage.obj = a;
            handler.sendMessage(obtainMessage);
            obtainMessage = handler.obtainMessage(4355);
            obtainMessage.arg1 = (i2 * 100) / i;
            handler.sendMessage(obtainMessage);
        }
        obtainMessage = handler.obtainMessage(4357);
        obtainMessage.arg1 = 0;
        obtainMessage.obj = rd.G(arrayList2);
        handler.sendMessage(obtainMessage);
        return arrayList2;
    }

    public void cancel() {
        this.JP.set(true);
    }
}
