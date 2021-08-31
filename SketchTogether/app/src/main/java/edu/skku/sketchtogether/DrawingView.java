package edu.skku.sketchtogether;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.Nullable;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    public int paintColor;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    private ArrayList<Path> paths = new ArrayList<Path>();
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private static final float SMALL_BRUSH_SIZE = 20;
    private float penBrushSize = SMALL_BRUSH_SIZE;
    private float eraserBrushSize = SMALL_BRUSH_SIZE;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        paintColor = 0xFF000000;

        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(Color.BLACK);
        drawPaint.setStrokeWidth(SMALL_BRUSH_SIZE);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
        // paths.add(drawPath);
    }

    public void setPaintColor(int color) {
        this.drawPaint.setColor(color);
        this.paintColor = color;
        canvasPaint.setColor(paintColor);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(touchX, touchY);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(touchX, touchY);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void touchStart(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;

    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.add(drawPath);
        drawPath = new Path();
        //eraseAll(false);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void eraseArea(float left, float top, float right, float bottom) {
        drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        drawPaint.setStyle(Paint.Style.FILL);
        drawCanvas.drawRect(left, top, right, bottom, drawPaint);

        drawPaint.setXfermode(null);
        drawPaint.setStyle(Paint.Style.STROKE);
    }

    public void eraseAll() {
        drawPath = new Path();
        paths.clear();
        eraseArea(0, 0, this.getWidth(), this.getHeight());
        invalidate();
    }

    public void setPenMode() {
        drawPaint.setXfermode(null);
        drawPaint.setColor(paintColor);
        drawPaint.setStrokeWidth(penBrushSize);
        invalidate();
    }

    public void setEraserMode() {
        drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        drawPaint.setStrokeWidth(eraserBrushSize);
    }

    public void drawSticker(Bitmap bitmap) {
        drawCanvas.drawBitmap(bitmap, 0, 0, null);
    }

    public Paint getDrawPaint() { return drawPaint; }

    public void setPenBrushSize(float penBrushSize) {
        this.penBrushSize = penBrushSize;
        setPenMode();
    }

    public void setEraserBrushSize(float eraserBrushSize) {
        this.eraserBrushSize = eraserBrushSize;
        setEraserMode();
    }
}