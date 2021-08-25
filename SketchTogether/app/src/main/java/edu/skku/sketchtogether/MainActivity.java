package edu.skku.sketchtogether;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class MainActivity extends AppCompatActivity {

    Context context;
    ConstraintLayout constraintLayout;
    DrawingView drawingView;
    CursorView cursorView;
    StickerView stickerView;
    FloatingActionButton checkButton;
    FloatingActionButton penButton;
    FloatingActionButton colorButton;
    FloatingActionButton eraserButton;
    FloatingActionButton cursorButton;
    FloatingActionButton suggestButton;
    FloatingActionButton deleteButton;
    LinearLayout imageViewLinearLayout;
    ImageView neighborImageView1;
    ImageView neighborImageView2;
    ImageView neighborImageView3;
    ImageView neighborImageView4;

    private static final int PEN_MODE = 1;
    private static final int ERASER_MODE = 2;
    private static final int CURSOR_MODE = 3;
    private static final int SUGGEST_MODE = 4;

    int displayX;
    int displayY;
    Bitmap croppedScreenshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        constraintLayout = findViewById(R.id.constraintLayout);
        stickerView = findViewById(R.id.stickerView);
        cursorView = findViewById(R.id.cursorView);
        drawingView = findViewById(R.id.drawingView);
        checkButton = findViewById(R.id.checkButton);
        penButton = findViewById(R.id.penButton);
        colorButton = findViewById(R.id.colorButton);
        eraserButton = findViewById(R.id.eraserButton);
        cursorButton = findViewById(R.id.cursorButton);
        suggestButton = findViewById(R.id.suggestButton);
        deleteButton = findViewById(R.id.deleteButton);
        imageViewLinearLayout = findViewById(R.id.imageViewLinearLayout);
        neighborImageView1 = findViewById(R.id.neighborImageView1);
        neighborImageView2 = findViewById(R.id.neighborImageView2);
        neighborImageView3 = findViewById(R.id.neighborImageView3);
        neighborImageView4 = findViewById(R.id.neighborImageView4);

        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        displayX = displaySize.x;
        displayY = displaySize.y;

        /* 도화지 리셋
        ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog();
            }
        });
        */

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                penButton.setVisibility(View.GONE);
                checkButton.setVisibility(View.INVISIBLE);
                cursorButton.setVisibility(View.INVISIBLE);
                suggestButton.setVisibility(View.INVISIBLE);
                colorButton.setVisibility(View.VISIBLE);
            }
        });

        penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.bringToFront();
                drawingView.setTouchEventMode(PEN_MODE);
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.bringToFront();
                drawingView.setTouchEventMode(PEN_MODE);
                openColorPicker();
            }
        });

        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.bringToFront();
                drawingView.setTouchEventMode(ERASER_MODE);
            }
        });

        cursorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursorView.bringToFront();
            }
        });

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerView.bringToFront();
                croppedScreenshot = getCroppedScreenshot(constraintLayout);
                if (croppedScreenshot != null) {
                    neighborImageView1.setImageBitmap(croppedScreenshot);
                }
                else {
                    Glide.with(context).load(R.drawable.icon_heart).into(neighborImageView1);
                }
                Glide.with(context).load(R.drawable.icon_heart).into(neighborImageView2);
                Glide.with(context).load(R.drawable.icon_heart).into(neighborImageView3);
                Glide.with(context).load(R.drawable.icon_heart).into(neighborImageView4);
                imageViewLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        neighborImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSticker(neighborImageView1);
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

        neighborImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSticker(neighborImageView2);
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

        neighborImageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSticker(neighborImageView3);
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

        neighborImageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSticker(neighborImageView4);
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
            }
        });

    }

    // 스티커뷰에서 스티커 추가
    protected void loadSticker(ImageView imageView){
        Drawable drawable = imageView.getDrawable();
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        stickerView.addSticker(drawableSticker);
    }

    // 스크린샷 부분캡쳐
    public Bitmap getCroppedScreenshot(View view) {

        float beginX = cursorView.getBeginCoordinate().x;
        float beginY = cursorView.getBeginCoordinate().y;
        float endX = cursorView.getEndCoordinate().x;
        float endY = cursorView.getEndCoordinate().y;

        int x = Math.round(beginX);
        int y = Math.round(beginY);
        int width = Math.abs(Math.round(endX - beginX));
        int height = Math.abs(Math.round(endY - beginY));

        if (width == 0 || height == 0) {
            return null;
        }

        Bitmap originBmp = Bitmap.createBitmap(displayX, displayY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(originBmp);
        view.draw(canvas);

        Bitmap croppedBmp = Bitmap.createBitmap(originBmp, x, y, width, height);
        return croppedBmp;
    }

    // 색상 선택
    private void openColorPicker() {
        final ColorPicker colorPicker = new ColorPicker(this);
        final ArrayList<String> colors = new ArrayList<>();
        colors.add("#258180");
        colors.add("#3C8D2F");
        colors.add("#20724F");
        colors.add("#6a3ab2");
        colors.add("#323299");
        colors.add("#800080");
        colors.add("#b79716");
        colors.add("#966d37");
        colors.add("#b77231");
        colors.add("#000000");

        colorPicker.setRoundColorButton(true).setColumns(5).setColorButtonTickColor(Color.parseColor("#000000"))
                .setDefaultColorButton(Color.parseColor("#000000")).setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                drawingView.setPaintColor(color);
            }

            @Override
            public void onCancel() {
            }
        }).show();
    }

}