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
import android.util.Log;
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
    private ArrayList<String> points;
    private ArrayList<String> colors;
    private ArrayList<ArrayList<String>> allPoints;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private static final float PEN_BRUSH_SIZE = 5;
    private static final float ERASER_BRUSH_SIZE = 20;
    private float penBrushSize = PEN_BRUSH_SIZE;
    private float eraserBrushSize = ERASER_BRUSH_SIZE;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        paintColor = 0xFF000000;

        drawPath = new Path();
        drawPaint = new Paint();
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        drawPaint.setColor(Color.BLACK);
        drawPaint.setStrokeWidth(penBrushSize);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        colors = new ArrayList<>();
        allPoints = new ArrayList<>();
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
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
        points = new ArrayList<>();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
        colors.add(String.format("#%06X", (0xFFFFFF & drawPaint.getColor())));
        points.add("(" + x + ", " + y + ")");
    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.add(drawPath);
        allPoints.add(points);
        drawPath = new Path();
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            points.add("(" + x + ", " + y + ")");
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
        allPoints = new ArrayList<>();
        colors = new ArrayList<>();
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

    public ArrayList<String> getColors() { return colors; }

    public ArrayList<ArrayList<String>> getAllPoints() {
        return allPoints;
    }

    public Paint getDrawPaint() { return drawPaint; }

    public void setPenBrushSize(float penBrushSize) {
        this.penBrushSize = penBrushSize;
    }

    public void setEraserBrushSize(float eraserBrushSize) { this.eraserBrushSize = eraserBrushSize; }
}