package io.noties.tumbleweed.sample.easing;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.tumbleweed.Tween;
import io.noties.tumbleweed.TweenEquation;
import io.noties.tumbleweed.TweenManager;
import io.noties.tumbleweed.TweenType;
import io.noties.tumbleweed.android.DrawableTweenManager;
import io.noties.tumbleweed.equations.Linear;

public class EasingDrawable extends Drawable implements Animatable {

    private static final int PARTS = 100;

    private static final int NORMAL_ALPHA = 255;
    private static final int BORDER_ALPHA = 40;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rectF = new RectF();

    private final Rect rect = new Rect();

    private final TweenManager tweenManager = DrawableTweenManager.create(this);

    private TweenEquation equation;

    private int part;

    public EasingDrawable(@NonNull TweenEquation equation, @ColorInt int color) {
        this.equation = equation;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    public void equation(@NonNull TweenEquation equation) {
        this.equation = equation;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        final Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }

        final int w = bounds.width();
        final int h = bounds.height();

        final float side = (float) w / PARTS;

        // borders
        paint.setAlpha(BORDER_ALPHA);

        rect.set(bounds.left, bounds.top, bounds.right, (int) (bounds.top + side + .5F));
        canvas.drawRect(rect, paint);

        rect.set(bounds.left, (int) (bounds.bottom - side + .5F), bounds.right, bounds.bottom);
        canvas.drawRect(rect, paint);

        paint.setAlpha(NORMAL_ALPHA);

        rectF.set(0, 0, side, side);

        float ratio;
        int save;

        final int partsToDraw = part == 0
                ? PARTS
                : part;

        final float availableH = h - side;
        final float radius = rectF.width() / 2;

        for (int i = 0; i <= partsToDraw; i++) {
            ratio = equation.compute((float) i / PARTS);
            save = canvas.save();
            try {
                canvas.translate(side * i, availableH - (availableH * ratio));
                canvas.drawRoundRect(rectF, radius, radius, paint);
            } finally {
                canvas.restoreToCount(save);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        // no op
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // no op
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void start() {

        part = 0;

        if (tweenManager.tweenCount() == 0) {
            createTween();
        }

        if (!isRunning()) {
            tweenManager.resume();
        }
    }

    @Override
    public void stop() {
        part = 0;
        tweenManager.pause();
    }

    @Override
    public boolean isRunning() {
        return tweenManager.isRunning();
    }

    private void createTween() {
        Tween.to(this, new PartType(), 1.F)
                .target(PARTS)
                .ease(Linear.INOUT)
                .removeWhenFinished(true)
                .start(tweenManager);
    }

    private static class PartType implements TweenType<EasingDrawable> {

        @Override
        public int getValuesSize() {
            return 1;
        }

        @Override
        public void getValues(@NonNull EasingDrawable easingDrawable, @NonNull float[] values) {
            values[0] = easingDrawable.part;
        }

        @Override
        public void setValues(@NonNull EasingDrawable easingDrawable, @NonNull float[] values) {
            easingDrawable.part = (int) (values[0] + .5F);
        }
    }
}
