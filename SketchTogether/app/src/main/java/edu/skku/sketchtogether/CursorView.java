package edu.skku.sketchtogether;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CursorView extends View {

    private boolean drawRectangle = false;
    private Paint drawPaint;
    private PointF beginCoordinate = new PointF(0, 0);
    private PointF endCoordinate = new PointF(0, 0);

    public CursorView(Context context) {
        this(context, null);
    }

    public CursorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        drawPaint = new Paint();

        drawPaint.setColor(getResources().getColor(R.color.black));
        drawPaint.setStrokeWidth(5);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        if(drawRectangle) {
            canvas.drawRect(beginCoordinate.x, beginCoordinate.y, endCoordinate.x, endCoordinate.y, drawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawRectangle = true; // Start drawing the rectangle
                beginCoordinate.x = event.getX();
                beginCoordinate.y = event.getY();
                endCoordinate.x = event.getX();
                endCoordinate.y = event.getY();
                invalidate(); // Tell View that the canvas needs to be redrawn
                break;
            case MotionEvent.ACTION_MOVE:
                endCoordinate.x = event.getX();
                endCoordinate.y = event.getY();
                invalidate();  // Tell View that the canvas needs to be redrawn
                break;
            case MotionEvent.ACTION_UP:
                drawRectangle = false; // Stop drawing the rectangle
                performClick();
                break;
        }
        return true;
    }

    public PointF getBeginCoordinate() {
        return beginCoordinate;
    }

    public PointF getEndCoordinate() {
        return endCoordinate;
    }

}
