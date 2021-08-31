package edu.skku.sketchtogether;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import petrov.kristiyan.colorpicker.ColorPicker;

public class MainActivity extends AppCompatActivity {

    Context context;
    FrameLayout sketchLayout;
    DrawingView sketchingView;
    DrawingView coloringView;
    CursorView cursorView;
    StickerView stickerView;
    LinearLayout brushViewLinearLayout;
    LinearLayout buttonLinearLayout;
    ImageView smallBrushView;
    ImageView mediumBrushView;
    ImageView largeBrushView;
    FloatingActionButton finishButton;
    FloatingActionButton removeButton;
    FloatingActionButton penButton;
    FloatingActionButton colorButton;
    FloatingActionButton brushButton;
    FloatingActionButton eraserButton;
    FloatingActionButton cursorButton;
    FloatingActionButton suggestButton;
    FloatingActionButton deleteButton;
    FloatingActionButton checkButton;
    LinearLayout imageViewLinearLayout;
    ImageView neighborImageView1;
    ImageView neighborImageView2;
    ImageView neighborImageView3;
    ImageView neighborImageView4;

    private static final float SMALL_BRUSH_SIZE = 20;
    private static final float MEDIUM_BRUSH_SIZE = 60;
    private static final float LARGE_BRUSH_SIZE = 100;
    private boolean isSketchFinished = false;
    private boolean isEraserMode = false;

    Bitmap sketchScreenShot; // 스케치 캡쳐
    Bitmap croppedScreenshot; // 인공지능에 넣을 부분 캡쳐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        findViewsById();

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("스케치를 완료하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sketchScreenShot = getScreenshot(sketchingView);

                        // 서버로 보내서 svg 변환

                        isSketchFinished = true;
                        brushViewLinearLayout.setVisibility(View.INVISIBLE);
                        cursorButton.setVisibility(View.INVISIBLE);
                        suggestButton.setVisibility(View.INVISIBLE);
                        finishButton.setVisibility(View.GONE);
                        penButton.setVisibility(View.GONE);
                        colorButton.setVisibility(View.VISIBLE);
                        brushButton.setVisibility(View.VISIBLE);
                        coloringView.setVisibility(View.VISIBLE);
                        coloringView.bringToFront();
                        Toast.makeText(getApplicationContext(), "스케치가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("캔버스를 초기화하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        brushViewLinearLayout.setVisibility(View.INVISIBLE);
                        if (isSketchFinished == true) {
                            coloringView.eraseAll();
                        }
                        else {
                            sketchingView.eraseAll();
                        }
                        Toast.makeText(getApplicationContext(), "캔버스가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchingView.bringToFront();
                sketchingView.setPenMode();
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        brushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraserMode = false;
                coloringView.bringToFront();
                coloringView.setPenMode();
                setBrushView(smallBrushView, SMALL_BRUSH_SIZE);
                setBrushView(mediumBrushView, MEDIUM_BRUSH_SIZE);
                setBrushView(largeBrushView, LARGE_BRUSH_SIZE);
                brushViewLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEraserMode = true;
                setBrushView(smallBrushView, SMALL_BRUSH_SIZE);
                setBrushView(mediumBrushView, MEDIUM_BRUSH_SIZE);
                setBrushView(largeBrushView, LARGE_BRUSH_SIZE);
                brushViewLinearLayout.setVisibility(View.VISIBLE);
                if (isSketchFinished == false) {
                    sketchingView.bringToFront();
                    sketchingView.setEraserMode();
                }
                else {
                    coloringView.bringToFront();
                    coloringView.setEraserMode();
                }
            }
        });

        smallBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEraserMode == false) {
                    coloringView.setPenBrushSize(SMALL_BRUSH_SIZE);
                }
                else if (isSketchFinished == false) {
                    sketchingView.setEraserBrushSize(SMALL_BRUSH_SIZE);
                }
                else {
                    coloringView.setEraserBrushSize(SMALL_BRUSH_SIZE);
                }
            }
        });

        mediumBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEraserMode == false) {
                    coloringView.setPenBrushSize(MEDIUM_BRUSH_SIZE);
                }
                else if (isSketchFinished == false) {
                    sketchingView.setEraserBrushSize(MEDIUM_BRUSH_SIZE);
                }
                else {
                    coloringView.setEraserBrushSize(MEDIUM_BRUSH_SIZE);
                }
            }
        });

        largeBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEraserMode == false) {
                    coloringView.setPenBrushSize(LARGE_BRUSH_SIZE);
                }
                else if (isSketchFinished == false) {
                    sketchingView.setEraserBrushSize(LARGE_BRUSH_SIZE);
                }
                else {
                    coloringView.setEraserBrushSize(LARGE_BRUSH_SIZE);
                }
            }
        });

        cursorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                cursorView.bringToFront();
            }
        });

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                buttonLinearLayout.setVisibility(View.INVISIBLE);
                stickerView.bringToFront();
                croppedScreenshot = getCroppedScreenshot(sketchLayout);

                // croppedScreenshot 서버 전송하는 코드
//                String filepath = " ";
                System.out.println("FilePath" + String.valueOf(getFilesDir()));
                File fileimg = BitmapConvertFile(croppedScreenshot, String.valueOf(getFilesDir()) + "file.bin");
                goSend(fileimg);
            }
        });

        neighborImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.GONE);
                checkButton.setVisibility(View.VISIBLE);
                sketchingView.eraseArea(cursorView.getBeginCoordinate().x, cursorView.getBeginCoordinate().y, cursorView.getEndCoordinate().x, cursorView.getEndCoordinate().y);
                loadSticker(neighborImageView1);
            }
        });

        neighborImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.GONE);
                checkButton.setVisibility(View.VISIBLE);
                sketchingView.eraseArea(cursorView.getBeginCoordinate().x, cursorView.getBeginCoordinate().y, cursorView.getEndCoordinate().x, cursorView.getEndCoordinate().y);
                loadSticker(neighborImageView2);
            }
        });

        neighborImageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.GONE);
                checkButton.setVisibility(View.VISIBLE);
                sketchingView.eraseArea(cursorView.getBeginCoordinate().x, cursorView.getBeginCoordinate().y, cursorView.getEndCoordinate().x, cursorView.getEndCoordinate().y);
                loadSticker(neighborImageView3);
            }
        });

        neighborImageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.GONE);
                checkButton.setVisibility(View.VISIBLE);
                sketchingView.eraseArea(cursorView.getBeginCoordinate().x, cursorView.getBeginCoordinate().y, cursorView.getEndCoordinate().x, cursorView.getEndCoordinate().y);
                loadSticker(neighborImageView4);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerView.removeAllStickers();
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
                buttonLinearLayout.setVisibility(View.VISIBLE);
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerView.releaseHandlingSticker();
                sketchingView.drawSticker(getScreenshot(stickerView));
                stickerView.removeAllStickers();
                imageViewLinearLayout.setVisibility(View.INVISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                checkButton.setVisibility(View.GONE);
                buttonLinearLayout.setVisibility(View.VISIBLE);
            }
        });

    }
    public File BitmapConvertFile(Bitmap bitmap, String strFilePath) {
        File file = new File(getFilesDir(), "file.bin") ;
        OutputStream out = null;
        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void goSend(File file){
        Log.d("TEST : ", "Request");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", "galleryfile", RequestBody.create(MultipartBody.FORM, file))
                .build();

        Request request = new Request.Builder()
                .url("http://blee.iptime.org:22222/haewon")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TEST : ", "Response");
                String responseData = response.body().string();
                Log.d("TEST : ", responseData);
                try {
                    JSONObject json = new JSONObject(responseData);
                    System.out.println(json);

                    List<String> suggestedImageList = new ArrayList<String>();
                    for (int i = 0; i < 4; i++) {
                        suggestedImageList.add(i, json.getString("img" + (i + 1)));
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Resources resources = MainActivity.this.getResources();
                            int resourceId1 = resources.getIdentifier(suggestedImageList.get(0), "drawable", MainActivity.this.getPackageName());
                            int resourceId2 = resources.getIdentifier(suggestedImageList.get(1), "drawable", MainActivity.this.getPackageName());
                            int resourceId3 = resources.getIdentifier(suggestedImageList.get(2), "drawable", MainActivity.this.getPackageName());
                            int resourceId4 = resources.getIdentifier(suggestedImageList.get(3), "drawable", MainActivity.this.getPackageName());

                            Glide.with(context).load(resourceId1).into(neighborImageView1);
                            Glide.with(context).load(resourceId2).into(neighborImageView2);
                            Glide.with(context).load(resourceId3).into(neighborImageView3);
                            Glide.with(context).load(resourceId4).into(neighborImageView4);
                            imageViewLinearLayout.setVisibility(View.VISIBLE);

//                            suggestImage1.setImageResource(resourceId1);
//                            suggestImage2.setImageResource(resourceId2);
//                            suggestImage3.setImageResource(resourceId3);
//                            suggestImage4.setImageResource(resourceId4);
                        }
                    });
                } catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
    // 브러쉬 사이즈 표시
    protected void setBrushView(ImageView view, float size) {
        int width = eraserButton.getWidth() * 2;
        int height = eraserButton.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint drawPaint = coloringView.getDrawPaint();
        if (isEraserMode == true) {
            drawPaint.setXfermode(null);
            drawPaint.setColor(Color.BLACK);
        }
        drawPaint.setStrokeWidth(size);
        canvas.drawLine(width / 4, height / 2, width * 3 / 4, height / 2, drawPaint);
        view.setImageBitmap(bitmap);
    }

    // 스티커 소환
    protected void loadSticker(ImageView imageView){
        Drawable drawable = imageView.getDrawable();
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        stickerView.addSticker(drawableSticker);
    }

    // 뷰 전체 캡쳐
    protected Bitmap getScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // 뷰 부분 캡쳐
    protected Bitmap getCroppedScreenshot(View view) {
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

        Bitmap bitmap = Bitmap.createBitmap(getScreenshot(view), x, y, width, height);
        return bitmap;
    }

    // 색상 선택
    protected void openColorPicker() {
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
                coloringView.setPaintColor(color);
                setBrushView(smallBrushView, SMALL_BRUSH_SIZE);
                setBrushView(mediumBrushView, MEDIUM_BRUSH_SIZE);
                setBrushView(largeBrushView, LARGE_BRUSH_SIZE);
            }

            @Override
            public void onCancel() {
            }
        }).show();
    }

    protected void findViewsById() {
        sketchLayout = findViewById(R.id.sketchLayout);
        sketchingView = findViewById(R.id.sketchingView);
        coloringView = findViewById(R.id.coloringView);
        cursorView = findViewById(R.id.cursorView);
        stickerView = findViewById(R.id.stickerView);
        brushViewLinearLayout = findViewById(R.id.brushViewLinearLayout);
        buttonLinearLayout = findViewById(R.id.buttonLinearLayout);
        smallBrushView = findViewById(R.id.smallBrushView);
        mediumBrushView = findViewById(R.id.mediumBrushView);
        largeBrushView = findViewById(R.id.largeBrushView);
        finishButton = findViewById(R.id.finishButton);
        removeButton = findViewById(R.id.removeButton);
        penButton = findViewById(R.id.penButton);
        colorButton = findViewById(R.id.colorButton);
        brushButton = findViewById(R.id.brushButton);
        eraserButton = findViewById(R.id.eraserButton);
        cursorButton = findViewById(R.id.cursorButton);
        suggestButton = findViewById(R.id.suggestButton);
        deleteButton = findViewById(R.id.deleteButton);
        checkButton = findViewById(R.id.checkButton);
        imageViewLinearLayout = findViewById(R.id.imageViewLinearLayout);
        neighborImageView1 = findViewById(R.id.neighborImageView1);
        neighborImageView2 = findViewById(R.id.neighborImageView2);
        neighborImageView3 = findViewById(R.id.neighborImageView3);
        neighborImageView4 = findViewById(R.id.neighborImageView4);
    }

}