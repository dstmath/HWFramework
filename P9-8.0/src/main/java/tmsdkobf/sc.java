package tmsdkobf;

import android.os.Handler;
import android.os.Message;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.utils.f;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdkobf.ry.a;

public class sc {
    private AtomicBoolean JR = new AtomicBoolean();

    private List<ry> P(List<sa> list) {
        List arrayList = new ArrayList();
        for (int size = list.size() - 1; size >= 0; size--) {
            if (this.JR.get()) {
                return null;
            }
            sa saVar = (sa) list.get(size);
            if (!saVar.mIsScreenShot) {
                a(arrayList, new a(saVar));
            }
        }
        return arrayList;
    }

    private long[] Q(List<ry> list) {
        int i = 0;
        long j = 0;
        for (ry ryVar : list) {
            i += ryVar.mItemList.size();
            Iterator it = ryVar.mItemList.iterator();
            while (it.hasNext()) {
                j += ((a) it.next()).mSize;
            }
        }
        return new long[]{(long) i, j};
    }

    private List<ry> a(ry ryVar) {
        List<ry> arrayList = new ArrayList();
        ArrayList x = x(ryVar.mItemList);
        if (this.JR.get() || x == null) {
            return null;
        }
        for (int i = 0; i < x.size(); i++) {
            if (this.JR.get()) {
                return null;
            }
            ArrayList arrayList2 = (ArrayList) x.get(i);
            if (!(arrayList2 == null || arrayList2.isEmpty())) {
                ry ryVar2 = new ry(ryVar);
                Iterator it = arrayList2.iterator();
                while (it.hasNext()) {
                    ryVar2.mItemList.add((a) ryVar.mItemList.get(((Integer) it.next()).intValue()));
                }
                b(ryVar2);
                arrayList.add(ryVar2);
            }
        }
        return arrayList;
    }

    private void a(List<ry> list, a aVar) {
        for (ry ryVar : list) {
            Object obj;
            if (ryVar.mTime - aVar.mTime > 30000) {
                obj = 1;
                continue;
            } else {
                obj = null;
                continue;
            }
            if (obj == null) {
                ryVar.mItemList.add(aVar);
                return;
            }
        }
        ry ryVar2 = new ry(aVar.mTime);
        ryVar2.mTime = aVar.mTime;
        ryVar2.mItemList.add(aVar);
        list.add(ryVar2);
    }

    private ArrayList<Integer> b(int i, ArrayList<ArrayList<Integer>> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ArrayList<Integer> arrayList2 = (ArrayList) it.next();
            if (arrayList2.contains(Integer.valueOf(i))) {
                return arrayList2;
            }
        }
        ArrayList<Integer> arrayList3 = new ArrayList();
        arrayList.add(arrayList3);
        return arrayList3;
    }

    private void b(ry ryVar) {
        ArrayList arrayList = ryVar.mItemList;
        for (int i = 0; i < arrayList.size(); i++) {
            if (i == 0) {
                ((a) arrayList.get(i)).mSelected = false;
            } else {
                ((a) arrayList.get(i)).mSelected = true;
            }
        }
    }

    private ArrayList<ArrayList<Integer>> x(ArrayList<a> arrayList) {
        ArrayList<ArrayList<Integer>> arrayList2 = new ArrayList();
        HashSet hashSet = new HashSet();
        y(arrayList);
        int i = 0;
        while (i < arrayList.size()) {
            if (this.JR.get()) {
                return null;
            }
            ArrayList b = b(i, arrayList2);
            QQImageFeatureHSV qQImageFeatureHSV = ((a) arrayList.get(i)).Qd;
            int i2 = i + 1;
            while (i2 < arrayList.size()) {
                QQImageFeatureHSV qQImageFeatureHSV2 = ((a) arrayList.get(i2)).Qd;
                if (!(qQImageFeatureHSV == null || qQImageFeatureHSV2 == null || rx.a(qQImageFeatureHSV, qQImageFeatureHSV2) < 75 || hashSet.contains(Integer.valueOf(i2)))) {
                    hashSet.add(Integer.valueOf(i2));
                    b.add(Integer.valueOf(i2));
                }
                i2++;
            }
            if (!(b.isEmpty() || hashSet.contains(Integer.valueOf(i)))) {
                hashSet.add(Integer.valueOf(i));
                b.add(Integer.valueOf(i));
            }
            i++;
        }
        for (i = 0; i < arrayList.size(); i++) {
            a aVar = (a) arrayList.get(i);
            if (aVar.Qd != null) {
                aVar.Qd.finish();
                aVar.Qd = null;
            }
        }
        return arrayList2;
    }

    private void y(ArrayList<a> arrayList) {
        for (int i = 0; i < arrayList.size() && !this.JR.get(); i++) {
            a aVar = (a) arrayList.get(i);
            aVar.Qd = rx.a(aVar);
        }
    }

    public List<ry> a(ArrayList<sa> arrayList, Handler handler) {
        this.JR.set(false);
        if (handler == null || arrayList == null) {
            f.e("SimilarPhotoProcesser", "startScan parameter is null");
            return null;
        }
        List<ry> arrayList2 = new ArrayList();
        List P = P(arrayList);
        if (this.JR.get()) {
            handler.sendMessage(handler.obtainMessage(4356));
            return null;
        } else if (P != null) {
            int i = (int) Q(P)[0];
            int i2 = 0;
            for (int i3 = 0; i3 < P.size(); i3++) {
                if (this.JR.get()) {
                    handler.sendMessage(handler.obtainMessage(4356));
                    return null;
                }
                ry ryVar = (ry) P.get(i3);
                List a = a(ryVar);
                if (this.JR.get()) {
                    handler.sendMessage(handler.obtainMessage(4356));
                    return null;
                }
                Message obtainMessage;
                i2 += ryVar.mItemList.size();
                arrayList2.addAll(a);
                if (a.size() > 0) {
                    obtainMessage = handler.obtainMessage(4354);
                    obtainMessage.obj = ri.I(a);
                    handler.sendMessage(obtainMessage);
                }
                obtainMessage = handler.obtainMessage(4355);
                obtainMessage.arg1 = (i2 * 100) / i;
                handler.sendMessage(obtainMessage);
            }
            Message obtainMessage2 = handler.obtainMessage(4357);
            obtainMessage2.arg1 = 0;
            obtainMessage2.obj = ri.I(arrayList2);
            handler.sendMessage(obtainMessage2);
            return arrayList2;
        } else {
            f.e("SimilarPhotoProcesser", "startScan sort get null result");
            Message obtainMessage3 = handler.obtainMessage(4357);
            obtainMessage3.arg1 = SpaceManager.ERROR_CODE_UNKNOW;
            obtainMessage3.obj = null;
            handler.sendMessage(obtainMessage3);
            return null;
        }
    }

    public void cancel() {
        this.JR.set(true);
    }
}
