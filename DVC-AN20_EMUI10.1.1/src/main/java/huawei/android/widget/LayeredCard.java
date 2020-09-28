package huawei.android.widget;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class LayeredCard extends FrameLayout {
    private List<SingleCard> mCardList = new ArrayList();

    public enum Direction {
        Left,
        Right
    }

    public LayeredCard(Context context) {
        super(context);
    }

    public LayeredCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayeredCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LayeredCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static class SingleCard {
        private View mContent;
        private TimeInterpolator mLeftInInterpolator;
        private TimeInterpolator mLeftOutInterpolator;
        private TimeInterpolator mRightInInterpolator;
        private TimeInterpolator mRightOutInterpolator;

        public enum TransitionMode {
            LeftIn,
            LeftOut,
            RightIn,
            RightOut
        }

        /* synthetic */ SingleCard(View x0, AnonymousClass1 x1) {
            this(x0);
        }

        private SingleCard(View content) {
            this.mContent = content;
        }

        public static class Build {
            private SingleCard mSingleCard;

            public Build(View content) {
                this.mSingleCard = new SingleCard(content, null);
            }

            public Build append(TimeInterpolator interpolator, TransitionMode mode) {
                int i = AnonymousClass1.$SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode[mode.ordinal()];
                if (i == 1) {
                    this.mSingleCard.mLeftInInterpolator = interpolator;
                } else if (i == 2) {
                    this.mSingleCard.mLeftOutInterpolator = interpolator;
                } else if (i == 3) {
                    this.mSingleCard.mRightInInterpolator = interpolator;
                } else if (i == 4) {
                    this.mSingleCard.mRightOutInterpolator = interpolator;
                }
                return this;
            }

            public SingleCard create() {
                return this.mSingleCard;
            }
        }
    }

    public void addCard(SingleCard card) {
        this.mCardList.add(card);
    }

    public void removeCard(SingleCard card) {
        this.mCardList.remove(card);
    }

    public void clearCard() {
        this.mCardList.clear();
    }

    public void updateTranslationX(float positionOffset, float pageOffset, boolean isEnter, Direction direction) {
        TimeInterpolator timeInterpolator;
        int width = getWidth();
        for (SingleCard card : this.mCardList) {
            if (!(card == null || card.mContent == null || (timeInterpolator = getInterpolation(card, isEnter, direction)) == null)) {
                card.mContent.setTranslationX((float) (((int) (timeInterpolator.getInterpolation(positionOffset) * ((float) width))) + ((int) (((float) width) * pageOffset))));
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.widget.LayeredCard$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$widget$LayeredCard$Direction = new int[Direction.values().length];
        static final /* synthetic */ int[] $SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode = new int[SingleCard.TransitionMode.values().length];

        static {
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$Direction[Direction.Left.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$Direction[Direction.Right.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode[SingleCard.TransitionMode.LeftIn.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode[SingleCard.TransitionMode.LeftOut.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode[SingleCard.TransitionMode.RightIn.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$huawei$android$widget$LayeredCard$SingleCard$TransitionMode[SingleCard.TransitionMode.RightOut.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private TimeInterpolator getInterpolation(SingleCard card, boolean isEnter, Direction direction) {
        int i = AnonymousClass1.$SwitchMap$huawei$android$widget$LayeredCard$Direction[direction.ordinal()];
        if (i == 1) {
            return isEnter ? card.mLeftInInterpolator : card.mLeftOutInterpolator;
        }
        if (i == 2) {
            return isEnter ? card.mRightInInterpolator : card.mRightOutInterpolator;
        }
        throw new RuntimeException("UnKnow");
    }

    public void updateX(float positionOffset, float pageOffset, boolean isEnter, Direction direction) {
        TimeInterpolator timeInterpolator;
        int width = getWidth();
        for (SingleCard card : this.mCardList) {
            if (!(card == null || card.mContent == null || (timeInterpolator = getInterpolation(card, isEnter, direction)) == null)) {
                card.mContent.setX((float) (((int) (timeInterpolator.getInterpolation(positionOffset) * ((float) width))) + ((int) (((float) width) * pageOffset))));
            }
        }
    }
}
