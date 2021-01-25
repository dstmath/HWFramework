package ohos.agp.window.wmc;

import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import com.android.internal.policy.PhoneWindow;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.agp.window.service.WindowManager;
import ohos.app.Context;
import ohos.global.icu.lang.UCharacterEnums;
import ohos.global.icu.text.Bidi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.codec.Codec;
import ohos.media.common.BufferInfo;
import ohos.media.common.Format;

public class AGPPresentationWindow extends AGPBaseDialogWindow {
    private static final int DEFAULT_THEME_ID = 0;
    private static final int DEFAULT_VIRTUAL_DISPLAY_DPI = 160;
    private static final int DEFAULT_VIRTUAL_DISPLAY_HEIGHT = 100;
    private static final int DEFAULT_VIRTUAL_DISPLAY_WIDTH = 100;
    private static final int IMAGE_READER_MAX_IMAGES = 2;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPPresentationWindow");
    private static final int MAX_QUEUE_SIZE = 3;
    private static final int TIME_INTERVAL = 20000000;
    private static final String VIRTUAL_DISPLAY_NAME_PREFIX = "Presentation_Display_";
    private LinkedBlockingQueue<byte[]> encoderQueue = new LinkedBlockingQueue<>(3);
    private WindowManagerImpl externalDWM;
    private LinkedBlockingQueue<byte[]> imageQueue = new LinkedBlockingQueue<>(3);
    private ImageReader imageReader;
    private boolean isRunning = false;
    private View mDecor;
    private VirtualDisplay mVirtualDisplay;
    private MediaEncoder mediaEncoder;
    private LinkedBlockingQueue<byte[]> packetsBuffer = new LinkedBlockingQueue<>(3);
    private long preTimestamp = (System.nanoTime() - 20000000);
    private LinkedBlockingQueue<byte[]> socketBuffer = new LinkedBlockingQueue<>(3);

    public AGPPresentationWindow(Context context, int i) {
        super(context, i);
        Optional<android.content.Context> createPresentationContext = createPresentationContext(context);
        if (createPresentationContext.isPresent()) {
            this.mAndroidContext = createPresentationContext.get();
            this.mAndroidWindow = new PhoneWindow(this.mAndroidContext);
            this.mAndroidWindow.requestFeature(1);
            Object systemService = this.mAndroidContext.getSystemService("window");
            if (systemService instanceof WindowManager) {
                this.mAndroidWindowManager = (WindowManager) systemService;
            }
            this.mAndroidWindow.setContentView(this.mSurfaceView);
            this.mAndroidWindow.setWindowManager(this.mAndroidWindowManager, null, null);
            this.mAndroidWindow.setType(WindowManager.LayoutConfig.MOD_PRESENTATION);
            this.mAndroidWindow.setGravity(119);
            this.mAndroidParam = this.mAndroidWindow.getAttributes();
            return;
        }
        HiLog.error(LABEL, "Create presentation context failed.", new Object[0]);
        throw new IllegalArgumentException("The Context can't be used to create presentation context");
    }

    private Optional<android.content.Context> createPresentationContext(Context context) {
        Optional<android.content.Context> androidContext = getAndroidContext(context);
        if (!androidContext.isPresent()) {
            return Optional.empty();
        }
        Object systemService = androidContext.get().getSystemService("display");
        if (systemService instanceof DisplayManager) {
            DisplayManager displayManager = (DisplayManager) systemService;
            this.mVirtualDisplay = createVirtualDisplay(displayManager);
            Optional<android.content.Context> generateVirtualDisplayContext = generateVirtualDisplayContext(androidContext.get(), displayManager);
            if (!generateVirtualDisplayContext.isPresent()) {
                return Optional.empty();
            }
            Object systemService2 = androidContext.get().getSystemService("window");
            if (systemService2 instanceof WindowManagerImpl) {
                this.externalDWM = ((WindowManagerImpl) systemService2).createPresentationWindowManager(generateVirtualDisplayContext.get());
                return Optional.of(generateContextThemeWrapper(generateVirtualDisplayContext.get()).get());
            }
            HiLog.error(LABEL, "get WINDOW_SERVICE is not WindowManagerImpl", new Object[0]);
            return Optional.empty();
        }
        HiLog.error(LABEL, "DisplayManager is null", new Object[0]);
        return Optional.empty();
    }

    private VirtualDisplay createVirtualDisplay(DisplayManager displayManager) {
        int length = displayManager.getDisplays().length;
        return displayManager.createVirtualDisplay(VIRTUAL_DISPLAY_NAME_PREFIX + (length + 1), 100, 100, 160, null, 2);
    }

    private Optional<android.content.Context> getAndroidContext(Context context) {
        Object hostContext = context.getHostContext();
        if (hostContext instanceof android.content.Context) {
            return Optional.of((android.content.Context) hostContext);
        }
        HiLog.error(LABEL, "AGPPresentationWindow getDefaultDisplay androidContext is null", new Object[0]);
        return Optional.empty();
    }

    private Optional<android.content.Context> generateVirtualDisplayContext(android.content.Context context, DisplayManager displayManager) {
        android.content.Context createDisplayContext = context.createDisplayContext(this.mVirtualDisplay.getDisplay());
        if (createDisplayContext != null) {
            return Optional.of(createDisplayContext);
        }
        HiLog.error(LABEL, "Presentation context is null", new Object[0]);
        return Optional.empty();
    }

    private Optional<ContextThemeWrapper> generateContextThemeWrapper(android.content.Context context) {
        return Optional.of(new ContextThemeWrapper(context, 0) {
            /* class ohos.agp.window.wmc.AGPPresentationWindow.AnonymousClass1 */

            @Override // android.view.ContextThemeWrapper, android.content.Context, android.content.ContextWrapper
            public Resources.Theme getTheme() {
                return super.getTheme();
            }

            @Override // android.view.ContextThemeWrapper, android.content.Context, android.content.ContextWrapper
            public Object getSystemService(String str) {
                if (str == null) {
                    return null;
                }
                if (str.equals("window")) {
                    return AGPPresentationWindow.this.externalDWM;
                }
                return super.getSystemService(str);
            }
        });
    }

    public void showOnRemoteScreen(String str, int i, int i2, int i3, int i4) {
        Handler handler = new Handler(Looper.getMainLooper());
        Optional.ofNullable(this.imageReader).ifPresent($$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY.INSTANCE);
        this.imageReader = ImageReader.newInstance(i2, i3, 1, 2);
        this.imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            /* class ohos.agp.window.wmc.$$Lambda$AGPPresentationWindow$m2k0yejtrhSflgReJOlSTxGly4 */

            @Override // android.media.ImageReader.OnImageAvailableListener
            public final void onImageAvailable(ImageReader imageReader) {
                AGPPresentationWindow.this.lambda$showOnRemoteScreen$0$AGPPresentationWindow(imageReader);
            }
        }, handler);
        this.mVirtualDisplay.resize(i2, i3, i4);
        this.mVirtualDisplay.setSurface(this.imageReader.getSurface());
        startTransformPipeline(str, i, i2, i3);
    }

    public /* synthetic */ void lambda$showOnRemoteScreen$0$AGPPresentationWindow(ImageReader imageReader2) {
        if (this.isRunning) {
            Image acquireNextImage = imageReader2.acquireNextImage();
            long nanoTime = System.nanoTime();
            if (nanoTime - this.preTimestamp < 20000000) {
                acquireNextImage.close();
            } else if (acquireNextImage != null) {
                try {
                    HiLog.debug(LABEL, "Image Reader get image", new Object[0]);
                    this.preTimestamp = nanoTime;
                    this.imageQueue.offer(convertToRGBA(acquireNextImage), 20000000, TimeUnit.NANOSECONDS);
                } catch (InterruptedException unused) {
                    HiLog.error(LABEL, "Put data into image queue occurs InterruptedException", new Object[0]);
                } catch (Throwable th) {
                    acquireNextImage.close();
                    throw th;
                }
                acquireNextImage.close();
            }
        }
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void show() {
        this.mDecor = this.mAndroidWindow.getDecorView();
        this.mAndroidParam.gravity = 119;
        this.mAndroidParam.width = -2;
        this.mAndroidParam.height = -2;
        this.mAndroidWindowManager.addView(this.mDecor, this.mAndroidParam);
        super.show();
    }

    private void startTransformPipeline(String str, int i, int i2, int i3) {
        this.isRunning = true;
        new RtpSocket().startSocket(str, i);
        new H264RtpPacketizer().startPacketizer();
        this.mediaEncoder = new MediaEncoder(i2, i3);
        this.mediaEncoder.start();
        new ImageFormatter(i2, i3).start();
    }

    @Override // ohos.agp.window.wmc.AGPWindow
    public void destroy() {
        stopTransformPipeline();
        Optional.ofNullable(this.imageReader).ifPresent($$Lambda$AGPPresentationWindow$hEaEWwXgJ0rB5xQp6ptqLxrDjY.INSTANCE);
        if (this.mDecor != null) {
            HiLog.debug(LABEL, "AGPPresentationWindow removeView", new Object[0]);
            this.mAndroidWindowManager.removeView(this.mDecor);
        }
        super.destroy();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopTransformPipeline() {
        this.isRunning = false;
        MediaEncoder mediaEncoder2 = this.mediaEncoder;
        if (mediaEncoder2 != null) {
            mediaEncoder2.stop();
        }
    }

    private byte[] convertToRGBA(Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length < 1) {
            return new byte[0];
        }
        if (planes[0].getBuffer() == null) {
            return new byte[0];
        }
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bArr = new byte[buffer.remaining()];
        buffer.get(bArr);
        return bArr;
    }

    /* access modifiers changed from: private */
    public class MediaEncoder {
        private static final String BITRATE_MODE = "bitrate-mode";
        private static final int BITREATE_MODE_DEFAULT = 1;
        private static final int COLOR_FOPMAT_YUV420SEMIPLANAR = 21;
        private static final int FRAME_RATE_DEFAULT = 45;
        private static final int I_FRAME_INTERVAL_DEFAULT = -1;
        private static final int TIME_DELAY_DEFAULT = 1000000;
        private static final int TIME_SLEEP_DEFAULT = 10;
        private Codec codec;
        private int height;
        private int width;

        MediaEncoder(int i, int i2) {
            this.width = i;
            this.height = i2;
            initCodec();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void start() {
            new Thread(new Runnable() {
                /* class ohos.agp.window.wmc.AGPPresentationWindow.MediaEncoder.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HiLog.debug(AGPPresentationWindow.LABEL, "come into initCodec", new Object[0]);
                    while (AGPPresentationWindow.this.isRunning) {
                        if (AGPPresentationWindow.this.encoderQueue.size() > 0) {
                            HiLog.debug(AGPPresentationWindow.LABEL, "queue time: start before packet queue", new Object[0]);
                            ByteBuffer availableBuffer = MediaEncoder.this.codec.getAvailableBuffer(1000000);
                            HiLog.debug(AGPPresentationWindow.LABEL, "apply for a dstBuf start", new Object[0]);
                            if (availableBuffer == null) {
                                HiLog.debug(AGPPresentationWindow.LABEL, "apply for a dstBuf is null", new Object[0]);
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException unused) {
                                    HiLog.error(AGPPresentationWindow.LABEL, "thread sleep error", new Object[0]);
                                }
                            } else {
                                HiLog.debug(AGPPresentationWindow.LABEL, "apply for a dstBuf finished", new Object[0]);
                                byte[] bArr = (byte[]) AGPPresentationWindow.this.encoderQueue.poll();
                                if (bArr != null) {
                                    availableBuffer.put(bArr);
                                    BufferInfo bufferInfo = new BufferInfo();
                                    bufferInfo.setInfo(0, bArr.length, System.currentTimeMillis(), 0);
                                    MediaEncoder.this.codec.writeBuffer(availableBuffer, bufferInfo);
                                    HiLog.debug(AGPPresentationWindow.LABEL, "after put data into encoder", new Object[0]);
                                }
                            }
                        }
                    }
                }
            }).start();
        }

        private void initCodec() {
            HiLog.debug(AGPPresentationWindow.LABEL, "come into initCodec()", new Object[0]);
            this.codec = Codec.createEncoder();
            Format format = new Format();
            format.putStringValue("mime", "video/avc");
            format.putIntValue(ViewAttrsConstants.WIDTH, this.width);
            format.putIntValue(ViewAttrsConstants.HEIGHT, this.height);
            format.putIntValue("bitrate", this.width * this.height);
            format.putIntValue("frame-rate", 45);
            format.putIntValue("i-frame-interval", -1);
            format.putIntValue(BITRATE_MODE, 1);
            format.putIntValue("color-format", 21);
            if (!this.codec.setCodecFormat(format)) {
                AGPPresentationWindow.this.stopTransformPipeline();
                HiLog.error(AGPPresentationWindow.LABEL, "mediacodec set code format false", new Object[0]);
            } else if (!this.codec.registerCodecListener(new EncoderListener())) {
                AGPPresentationWindow.this.stopTransformPipeline();
                HiLog.error(AGPPresentationWindow.LABEL, "mediacodec set register codec listener false", new Object[0]);
            } else {
                HiLog.debug(AGPPresentationWindow.LABEL, "come into encode register listener success", new Object[0]);
                if (!this.codec.start()) {
                    AGPPresentationWindow.this.stopTransformPipeline();
                    HiLog.error(AGPPresentationWindow.LABEL, "mediacodec start false", new Object[0]);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stop() {
            Codec codec2 = this.codec;
            if (codec2 != null) {
                codec2.stop();
                this.codec.release();
            }
        }

        /* access modifiers changed from: private */
        public class EncoderListener implements Codec.ICodecListener {
            private EncoderListener() {
            }

            public void onReadBuffer(ByteBuffer byteBuffer, BufferInfo bufferInfo, int i) {
                HiLog.debug(AGPPresentationWindow.LABEL, "output buffer", new Object[0]);
                byte[] bArr = new byte[bufferInfo.size];
                byteBuffer.get(bArr);
                try {
                    AGPPresentationWindow.this.packetsBuffer.put(bArr);
                    HiLog.debug(AGPPresentationWindow.LABEL, "queue time: end before packet queue", new Object[0]);
                } catch (InterruptedException unused) {
                    HiLog.error(AGPPresentationWindow.LABEL, "EncoderListener put data into package queue get InterruptedException ", new Object[0]);
                }
            }

            public void onError(int i, int i2, int i3) {
                HiLog.error(AGPPresentationWindow.LABEL, "encode error errorcode = %{private}d", new Object[]{Integer.valueOf(i)});
            }
        }
    }

    /* access modifiers changed from: private */
    public class ImageFormatter implements Runnable {
        private int height;
        private int width;

        ImageFormatter(int i, int i2) {
            this.width = i;
            this.height = i2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void start() {
            new Thread(this).start();
        }

        @Override // java.lang.Runnable
        public void run() {
            while (AGPPresentationWindow.this.isRunning) {
                if (AGPPresentationWindow.this.imageQueue.size() > 0) {
                    byte[] bArr = (byte[]) AGPPresentationWindow.this.imageQueue.poll();
                    if (bArr == null) {
                        HiLog.error(AGPPresentationWindow.LABEL, "run() rgba is null", new Object[0]);
                        return;
                    }
                    byte[] formatNv21ToNv12 = formatNv21ToNv12(encodeYuv420Sp(new byte[(((this.width * this.height) * 3) / 2)], bArr), this.width, this.height);
                    try {
                        HiLog.debug(AGPPresentationWindow.LABEL, "Come into fomat the image to yuv", new Object[0]);
                        AGPPresentationWindow.this.encoderQueue.put(formatNv21ToNv12);
                        HiLog.debug(AGPPresentationWindow.LABEL, "queue time: end before encode queue", new Object[0]);
                    } catch (InterruptedException unused) {
                        HiLog.error(AGPPresentationWindow.LABEL, "image format put data into encode queue occurs interruptedException", new Object[0]);
                    }
                }
            }
        }

        private byte[] encodeYuv420Sp(byte[] bArr, byte[] bArr2) {
            int i = this.width * this.height;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            while (i2 < this.height) {
                int i5 = i;
                int i6 = i4;
                int i7 = i3;
                int i8 = 0;
                while (i8 < this.width) {
                    int i9 = i7 * 4;
                    int i10 = 255;
                    int i11 = bArr2[i9] & 255;
                    int i12 = bArr2[i9 + 1] & 255;
                    int i13 = bArr2[i9 + 2] & 255;
                    int i14 = (((((i11 * 66) + (i12 * 129)) + (i13 * 25)) + 128) >> 8) + 16;
                    int i15 = (((((i11 * -38) - (i12 * 74)) + (i13 * 112)) + 128) >> 8) + 128;
                    int i16 = (((((i11 * 112) - (i12 * 94)) - (i13 * 18)) + 128) >> 8) + 128;
                    int i17 = i6 + 1;
                    if (i14 < 0) {
                        i14 = 0;
                    } else if (i14 > 255) {
                        i14 = 255;
                    }
                    bArr[i6] = (byte) i14;
                    if (i2 % 2 == 0 && i7 % 2 == 0) {
                        int i18 = i5 + 1;
                        if (i16 < 0) {
                            i16 = 0;
                        } else if (i16 > 255) {
                            i16 = 255;
                        }
                        bArr[i5] = (byte) i16;
                        i5 = i18 + 1;
                        if (i15 < 0) {
                            i10 = 0;
                        } else if (i15 <= 255) {
                            i10 = i15;
                        }
                        bArr[i18] = (byte) i10;
                    }
                    i7++;
                    i8++;
                    i6 = i17;
                }
                i2++;
                i3 = i7;
                i4 = i6;
                i = i5;
            }
            return bArr;
        }

        private byte[] formatNv21ToNv12(byte[] bArr, int i, int i2) {
            if (bArr == null || bArr.length == 0) {
                return new byte[0];
            }
            byte[] bArr2 = new byte[bArr.length];
            int i3 = i * i2;
            System.arraycopy(bArr, 0, bArr2, 0, i3);
            for (int i4 = 1; i4 < i3 / 2; i4 += 2) {
                int i5 = i3 + i4;
                int i6 = i5 - 1;
                bArr2[i6] = bArr[i5];
                bArr2[i5] = bArr[i6];
            }
            return bArr2;
        }
    }

    /* access modifiers changed from: protected */
    public abstract class AbstractRtpPacketizer implements Runnable {
        private static final int DEFAULT_BIT_RATE = 90000;
        private static final int DEFAULT_FRAME_RATE = 45;
        private static final int DYNAMIC_PAYLOAD_TYPE = 96;
        private static final int FIXED_HEAD_LENGTH = 12;
        private static final int MARKER = 1;
        private static final int MAX_SEQUENCE_NUMBER = 65535;
        private static final long MAX_TIMESTAMP = 4294967295L;
        private static final int MTU = 1400;
        protected static final int PREFIX_CODE_LENGTH = 4;
        private static final int SEQUENCE_NUMBER_BYTE_LENGTH = 2;
        private static final int SSRC_BYTE_LENGTH = 4;
        private static final int TIMESTAMP_BYTE_LENGTH = 4;
        private static final int VERSION = 2;
        protected int grammaticalElementLength;
        private int sequenceNumber;
        private byte[] ssrc;
        private long timestamp;
        private long timestampIncrement;

        /* access modifiers changed from: protected */
        public abstract byte[] getFragmentUnitHeader(byte[] bArr);

        /* access modifiers changed from: protected */
        public abstract byte[] getIndicator(byte[] bArr);

        AbstractRtpPacketizer() {
            byte[] generateRandomBytes = generateRandomBytes(2);
            this.sequenceNumber = (generateRandomBytes[0] << 8) + (generateRandomBytes[1] & 255);
            byte[] generateRandomBytes2 = generateRandomBytes(4);
            this.timestamp = (long) ((generateRandomBytes2[0] << UCharacterEnums.ECharacterCategory.MATH_SYMBOL) + (generateRandomBytes2[1] << 16) + (generateRandomBytes2[2] << 8) + (generateRandomBytes2[3] & 255));
            this.timestampIncrement = 2000;
            this.ssrc = generateRandomBytes(4);
        }

        AbstractRtpPacketizer(AGPPresentationWindow aGPPresentationWindow, int i, int i2) {
            this();
            if (i2 != 0) {
                this.timestampIncrement = (long) (i / i2);
            }
        }

        private byte[] generateRandomBytes(int i) {
            HiLog.debug(AGPPresentationWindow.LABEL, "Generating length of %{public}d random bytes.", new Object[]{Integer.valueOf(i)});
            byte[] bArr = new byte[i];
            new SecureRandom().nextBytes(bArr);
            return bArr;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startPacketizer() {
            HiLog.debug(AGPPresentationWindow.LABEL, "Start RTP packetizer.", new Object[0]);
            new Thread(this).start();
        }

        private byte[] addRtpHeader(byte[] bArr, boolean z) {
            HiLog.debug(AGPPresentationWindow.LABEL, "Adding RTP header in front of RTP payload.", new Object[0]);
            byte[] bArr2 = new byte[12];
            bArr2[0] = (byte) (bArr2[0] | Bidi.LEVEL_OVERRIDE);
            if (z) {
                bArr2[1] = (byte) (bArr2[1] | Bidi.LEVEL_OVERRIDE);
            }
            bArr2[1] = (byte) (bArr2[1] | 96);
            this.sequenceNumber = (this.sequenceNumber + 1) % 65535;
            byte b = bArr2[2];
            int i = this.sequenceNumber;
            bArr2[2] = (byte) (b | (i >>> 8));
            bArr2[3] = (byte) (bArr2[3] | i);
            long j = this.timestamp;
            bArr2[4] = (byte) ((int) (((long) bArr2[4]) | (j >>> 24)));
            bArr2[5] = (byte) ((int) (((long) bArr2[5]) | (j >>> 16)));
            bArr2[6] = (byte) ((int) (((long) bArr2[6]) | (j >>> 8)));
            bArr2[7] = (byte) ((int) (j | ((long) bArr2[7])));
            System.arraycopy(this.ssrc, 0, bArr2, 8, 4);
            byte[] bArr3 = new byte[(bArr2.length + bArr.length)];
            System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
            System.arraycopy(bArr, 0, bArr3, bArr2.length, bArr.length);
            return bArr3;
        }

        private List<byte[]> slicePacket(byte[] bArr) {
            HiLog.debug(AGPPresentationWindow.LABEL, "Slice NALU to fragment units.", new Object[0]);
            int length = bArr.length - 4;
            int i = this.grammaticalElementLength;
            byte[] bArr2 = new byte[(length - i)];
            System.arraycopy(bArr, i + 4, bArr2, 0, length - i);
            ArrayList arrayList = new ArrayList();
            byte[] indicator = getIndicator(bArr);
            byte[] fragmentUnitHeader = getFragmentUnitHeader(bArr);
            int length2 = bArr2.length;
            int length3 = indicator.length;
            int length4 = fragmentUnitHeader.length;
            int i2 = length3 + length4;
            int i3 = 0;
            while (i3 < length2) {
                int i4 = length2 - i3;
                int i5 = 1400 - i2;
                if (i4 > i5) {
                    i4 = i5;
                }
                byte[] bArr3 = new byte[(i2 + i4)];
                System.arraycopy(indicator, 0, bArr3, 0, length3);
                System.arraycopy(fragmentUnitHeader, 0, bArr3, length3, length4);
                System.arraycopy(bArr2, i3, bArr3, i2, i4);
                arrayList.add(bArr3);
                i3 += i4;
            }
            byte[] bArr4 = (byte[]) arrayList.get(0);
            bArr4[length3] = (byte) (bArr4[length3] | Bidi.LEVEL_OVERRIDE);
            byte[] bArr5 = (byte[]) arrayList.get(arrayList.size() - 1);
            bArr5[length3] = (byte) (bArr5[length3] | 64);
            HiLog.debug(AGPPresentationWindow.LABEL, "Sliced fragment units. Size is %{public}d.", new Object[]{Integer.valueOf(arrayList.size())});
            return arrayList;
        }

        @Override // java.lang.Runnable
        public void run() {
            byte[] bArr;
            while (AGPPresentationWindow.this.isRunning) {
                if (AGPPresentationWindow.this.packetsBuffer.size() > 0 && (bArr = (byte[]) AGPPresentationWindow.this.packetsBuffer.poll()) != null) {
                    this.timestamp = (this.timestamp + this.timestampIncrement) % MAX_TIMESTAMP;
                    if (bArr.length > 0 && bArr.length <= MTU) {
                        HiLog.debug(AGPPresentationWindow.LABEL, "NALU is shorter than MTU, use SRST.", new Object[0]);
                        byte[] bArr2 = new byte[(bArr.length - 4)];
                        System.arraycopy(bArr, 4, bArr2, 0, bArr.length - 4);
                        try {
                            AGPPresentationWindow.this.socketBuffer.put(addRtpHeader(bArr2, true));
                        } catch (InterruptedException e) {
                            HiLog.error(AGPPresentationWindow.LABEL, "RtpPacketizer InterruptedException in SRST: %{private}s", new Object[]{e.getMessage()});
                        }
                    } else if (bArr.length > MTU) {
                        HiLog.debug(AGPPresentationWindow.LABEL, "NALU is longer than MTU, use MRST.", new Object[0]);
                        List<byte[]> slicePacket = slicePacket(bArr);
                        int size = slicePacket.size();
                        int i = 0;
                        while (i < size) {
                            try {
                                AGPPresentationWindow.this.socketBuffer.put(addRtpHeader(slicePacket.get(i), i == size + -1));
                            } catch (InterruptedException e2) {
                                HiLog.error(AGPPresentationWindow.LABEL, "RtpPacketizer InterruptedException in MRST: %{private}s", new Object[]{e2.getMessage()});
                            }
                            i++;
                        }
                    } else {
                        HiLog.error(AGPPresentationWindow.LABEL, "Invalid NAL length.", new Object[0]);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class H264RtpPacketizer extends AbstractRtpPacketizer {
        private static final int FRAGMENT_INDICATOR_LENGTH = 1;
        private static final int FU_A_SLICE_TYPE = 28;
        private static final int H264_FRAGMENT_UNIT_HEADER_LENGTH = 1;
        private static final int H264_GRAMMATICAL_ELEMENT_LENGTH = 1;

        H264RtpPacketizer() {
            super();
            this.grammaticalElementLength = 1;
        }

        H264RtpPacketizer(int i, int i2) {
            super(AGPPresentationWindow.this, i, i2);
            this.grammaticalElementLength = 1;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.window.wmc.AGPPresentationWindow.AbstractRtpPacketizer
        public byte[] getIndicator(byte[] bArr) {
            byte[] bArr2 = {bArr[4]};
            bArr2[0] = (byte) ((bArr2[0] & 224) | 28);
            return bArr2;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.window.wmc.AGPPresentationWindow.AbstractRtpPacketizer
        public byte[] getFragmentUnitHeader(byte[] bArr) {
            return new byte[]{(byte) (bArr[4] & 31)};
        }
    }

    private class H265RtpPacketizer extends AbstractRtpPacketizer {
        private static final int H265_FRAGMENT_UNIT_HEADER_LENGTH = 1;
        private static final int H265_GRAMMATICAL_ELEMENT_LENGTH = 2;
        private static final int H265_PAYLOAD_HDR_LENGTH = 2;
        private static final int SLICE_TYPE = 49;
        private static final int TID = 1;

        H265RtpPacketizer() {
            super();
            this.grammaticalElementLength = 2;
        }

        H265RtpPacketizer(int i, int i2) {
            super(AGPPresentationWindow.this, i, i2);
            this.grammaticalElementLength = 2;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.window.wmc.AGPPresentationWindow.AbstractRtpPacketizer
        public byte[] getIndicator(byte[] bArr) {
            return new byte[]{98, 1};
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.window.wmc.AGPPresentationWindow.AbstractRtpPacketizer
        public byte[] getFragmentUnitHeader(byte[] bArr) {
            return new byte[]{(byte) ((bArr[4] >> 1) & 63)};
        }
    }

    /* access modifiers changed from: private */
    public class RtpSocket implements Runnable {
        private static final int DEFAULT_LOCAL_PORT = 65520;
        private static final int MAX_LOCAL_PORT = 65535;
        private String destIp;
        private int destPort;
        private int localPort;
        private DatagramSocket udpSocket;

        private RtpSocket() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startSocket(String str, int i) {
            if (AGPPresentationWindow.this.isRunning) {
                HiLog.debug(AGPPresentationWindow.LABEL, "Starting socket.", new Object[0]);
                this.localPort = DEFAULT_LOCAL_PORT;
                this.destIp = str;
                this.destPort = i;
                new Thread(this).start();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            while (true) {
                if (this.localPort >= 65535) {
                    break;
                }
                try {
                    HiLog.debug(AGPPresentationWindow.LABEL, "Creating socket.", new Object[0]);
                    this.udpSocket = new DatagramSocket(this.localPort);
                    HiLog.debug(AGPPresentationWindow.LABEL, "Created socket successfully.", new Object[0]);
                    sendPacket();
                    break;
                } catch (BindException e) {
                    if (this.localPort >= 65535) {
                        HiLog.error(AGPPresentationWindow.LABEL, "Create socket bindException: %{private}s. Retry failed.", new Object[]{e.getMessage()});
                        break;
                    } else {
                        HiLog.debug(AGPPresentationWindow.LABEL, "Create socket bindException: %{private}s, retry to create socket in large port.", new Object[]{e.getMessage()});
                        this.localPort++;
                    }
                } catch (SocketException e2) {
                    HiLog.error(AGPPresentationWindow.LABEL, "Create socket socketException: %{private}s.", new Object[]{e2.getMessage()});
                }
            }
            AGPPresentationWindow.this.stopTransformPipeline();
            DatagramSocket datagramSocket = this.udpSocket;
            if (datagramSocket != null) {
                datagramSocket.close();
                this.udpSocket = null;
            }
        }

        private void sendPacket() {
            HiLog.debug(AGPPresentationWindow.LABEL, "Start to send packet.", new Object[0]);
            while (AGPPresentationWindow.this.isRunning) {
                try {
                    if (AGPPresentationWindow.this.socketBuffer.size() > 0) {
                        byte[] bArr = (byte[]) AGPPresentationWindow.this.socketBuffer.poll();
                        this.udpSocket.send(new DatagramPacket(bArr, bArr.length, InetAddress.getByName(this.destIp), this.destPort));
                    }
                } catch (IOException e) {
                    HiLog.error(AGPPresentationWindow.LABEL, "Send packet IOException: %{private}s.", new Object[]{e.getMessage()});
                    return;
                }
            }
        }
    }
}
