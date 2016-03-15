package uk.co.deanwild.marqueetextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import uk.co.deanwild.marqueetextview.R;

/**
 * Created by deanwild on 14/03/16.
 */
public class MarqueeTextView extends View {

    static final int DEFAULT_SPEED = 10;
    static int DEFAULT_PAUSE_DURATION = 10000;
    static final int DEFAULT_EDGE_EFFECT_WIDTH = 20;
    static final int DEFAULT_EDGE_EFFECT_COLOR = Color.WHITE;

    boolean marqueeEnabled = true;
    int textColor = Color.BLACK;
    float textSize = getResources().getDisplayMetrics().scaledDensity * 20.0f;
    int pauseDuration = DEFAULT_PAUSE_DURATION;
    int speed = DEFAULT_SPEED;
    boolean showEdgeEffect = false;
    int edgeEffectWidth = DEFAULT_EDGE_EFFECT_WIDTH;
    int edgeEffectColor = DEFAULT_EDGE_EFFECT_COLOR;

    CharSequence text;

    int xOffset;
    double wrapAroundPoint;
    boolean animationRunning = false;
    boolean paused = false;
    boolean wrapped = false;

    TextPaint textPaint;
    Paint leftPaint;
    Paint rightPaint;

    Rect textBounds;
    RectF leftRect;
    RectF rightRect;


    public static void setGlobalDefaultPauseDuration(int pauseDuration) {
        DEFAULT_PAUSE_DURATION = pauseDuration;
    }

    public MarqueeTextView(Context context) {
        super(context);
        init(null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    void init(AttributeSet attrs) {

        if (attrs != null) {
            readAttrs(attrs);
        }

        renewPaint();

        textBounds = new Rect();

        if (text != null) {
            setText(text);
        }

    }

    void readAttrs(AttributeSet attrs) {

        int[] attrsArray = new int[]{
                android.R.attr.textSize,
                android.R.attr.textColor,
                android.R.attr.text,
                R.attr.marqueeEnabled,
                R.attr.showEdgeEffect,
                R.attr.edgeEffectWidth,
                R.attr.edgeEffectColor,
                R.attr.pauseDuration
        };

        TypedArray ta = getContext().obtainStyledAttributes(attrs, attrsArray);

        textSize = ta.getDimension(0, textSize); // 2 is the index in the array of the textSize attribute
        textColor = ta.getColor(1, textColor); // 3 is the index of the array of the textColor attribute
        text = ta.getText(2);
        marqueeEnabled = ta.getBoolean(3, marqueeEnabled);
        showEdgeEffect = ta.getBoolean(4, showEdgeEffect);
        edgeEffectWidth = ta.getInt(5, edgeEffectWidth);
        edgeEffectColor = ta.getColor(6, edgeEffectColor);
        pauseDuration = ta.getInt(7, pauseDuration);

        ta.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (text != null) {

            float viewWidth = this.getWidth();

            int textWidth = textBounds.width();

            float topOffset = textBounds.height() - textBounds.bottom;

            if (textWidth < viewWidth) { // text can fit in view, no marquee needed

                animationRunning = false;

                float leftMargin = (viewWidth - textWidth) / 2;
                canvas.drawText(text.toString(), leftMargin, topOffset, textPaint);

            } else { // not enough room, we must animate it

                if (!animationRunning) {

                    xOffset = 0;
                    wrapAroundPoint = -(textWidth + (textWidth * 0.05));
                    animationRunning = true;
                    wrapped = true;
                    paused = false;

                }

                canvas.drawText(text.toString(), xOffset, topOffset, textPaint);

                if (showEdgeEffect) {

                    if (xOffset < 0 || pauseDuration <= 0) {
                        canvas.drawRect(leftRect, leftPaint);
                    }

                    canvas.drawRect(rightRect, rightPaint);

                }

                if (!paused) {

                    xOffset -= speed;

                    if (xOffset < wrapAroundPoint) {
                        xOffset = (int) viewWidth;
                        wrapped = true;
                    }

                    if (wrapped && xOffset <= 0) {
                        wrapped = false;

                        if(pauseDuration > 0) {
                            xOffset = 0;
                            pause();
                        }
                    }

                    invalidateAfter(20);

                }
            }
        }
    }

    synchronized void pause() {
        paused = true;
        removeCallbacks(pauseRunnable);
        postDelayed(pauseRunnable, pauseDuration);
    }

    Runnable pauseRunnable = new Runnable() {
        @Override
        public void run() {
            paused = false;
            invalidate();
        }
    };


    void invalidateAfter(long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, delay);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            width = this.getWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
        } else {
            height = (int) textSize;
        }

        setMeasuredDimension(width, height);

        renewPaint();
    }

    void renewPaint() {

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.density = getResources().getDisplayMetrics().density;
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);

        int absEdgeEffectWidth = (getMeasuredWidth() / 100) * edgeEffectWidth;

        Shader leftShader = new LinearGradient(
                0,
                0,
                absEdgeEffectWidth,
                0,
                edgeEffectColor,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP);

        leftPaint = new Paint();
        leftPaint.setShader(leftShader);

        int rightOffset = getMeasuredWidth() - absEdgeEffectWidth;

        Shader rightShader = new LinearGradient(
                rightOffset,
                0,
                getMeasuredWidth(),
                0,
                Color.TRANSPARENT,
                edgeEffectColor,
                Shader.TileMode.CLAMP);

        rightPaint = new Paint();
        rightPaint.setShader(rightShader);

        leftRect = new RectF(0, 0, absEdgeEffectWidth, getMeasuredHeight());
        rightRect = new RectF(rightOffset, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setText(CharSequence text) {
        this.text = text;
        textPaint.getTextBounds(text.toString(), 0, text.length(), textBounds);
        animationRunning = false;
        requestLayout();
    }

    public void setTextColor(int color) {
        textColor = color;
        renewPaint();
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        renewPaint();
        textPaint.getTextBounds(text.toString(), 0, text.length(), textBounds);
        animationRunning = false;
        requestLayout();
    }

}
