package edu.skku.sketchtogether;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    List<String> pairedDevicesList;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    private static final UUID bluetoothUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    long downTime;
    long eventTime;
    float touchX = 0.0f;
    float touchY = 0.0f;

    private static final float PEN_BRUSH_SIZE = 10;
    private static final float SMALL_ERASER_BRUSH_SIZE = 20;
    private static final float MEDIUM_ERASER_BRUSH_SIZE = 60;
    private static final float LARGE_ERASER_BRUSH_SIZE = 100;
    private static final int PEN_MODE = 1;
    private static final int ERASER_MODE = 2;
    private static final int CURSOR_MODE = 3;
    private static final int SUGGEST_MODE = 4;
    private int mode;
    private boolean isSketchFinished = false;
    private boolean isTouchMode = false;
    private boolean isBrushViewSet = false;

    private final String textFileName = "textfile.txt";
    private final String screenshotFileName = "screenshot.jpg";

    private Bitmap sketchScreenshot; // 스케치 캡쳐
    private String internalFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        findViewsById();

        // OpenBTSocket();
        File internalFileDir = getFilesDir();
        internalFilePath = internalFileDir.getPath();

        PressButton(PEN_MODE);

        sketchLayout.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                touchX = event.getX();
                touchY = event.getY();
                if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    if (isTouchMode) {
                        downTime = SystemClock.uptimeMillis();
                        eventTime = SystemClock.uptimeMillis();
                        MotionEvent moveMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_MOVE, touchX, touchY, 0);
                        if (mode == PEN_MODE || mode == ERASER_MODE) {
                            if (isSketchFinished) {
                                coloringView.dispatchTouchEvent(moveMotionEvent);
                            } else {
                                sketchingView.dispatchTouchEvent(moveMotionEvent);
                            }
                        }
                        else if (mode == CURSOR_MODE) {
                            cursorView.dispatchTouchEvent(moveMotionEvent);
                        }
                    }
                }
                return false;
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("완료하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isSketchFinished) {
                            saveTextFile(textFileName);

                            Uri uri = FileProvider.getUriForFile(context, "edu.skku.sketchtogether.fileprovider",new File(context.getFilesDir(), textFileName));

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/*");    // 고정
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(shareIntent, "Sharing"));

                            Toast.makeText(getApplicationContext(), "채색이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            sketchScreenshot = getScreenshot(sketchingView);
                            saveImage(context, screenshotFileName, sketchScreenshot);
                            File sketchScreenShotFile = Bmp2File(sketchScreenshot, String.valueOf(getFilesDir()) + "sketch.bin");
//                            SendScreenshot2Server(sketchScreenShotFile);
                            Uri uri = FileProvider.getUriForFile(context, "edu.skku.sketchtogether.fileprovider",new File(context.getFilesDir(), screenshotFileName));

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");    // 고정
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                            startActivity(Intent.createChooser(shareIntent, "Sharing"));


                            isSketchFinished = true;
                            brushViewLinearLayout.setVisibility(View.INVISIBLE);
                            deleteButton.setVisibility(View.GONE);
                            penButton.setVisibility(View.GONE);
                            eraserButton.setVisibility(View.GONE);
                            cursorButton.setVisibility(View.GONE);
                            suggestButton.setVisibility(View.GONE);
                            colorButton.setVisibility(View.VISIBLE);
                            coloringView.setVisibility(View.VISIBLE);
                            coloringView.bringToFront();
                            coloringView.setPenBrushSize(20);
                            coloringView.setPenMode();
                            Toast.makeText(getApplicationContext(), "스케치가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
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
                        if (isSketchFinished) {
                            coloringView.eraseAll();
                        }
                        else {
                            sketchingView.eraseAll();
                            PressButton(PEN_MODE);
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
                PressButton(PEN_MODE);
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
                coloringView.bringToFront();
                coloringView.setPenMode();
            }
        });

        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBrushViewSet) {
                    setBrushView(smallBrushView, SMALL_ERASER_BRUSH_SIZE);
                    setBrushView(mediumBrushView, MEDIUM_ERASER_BRUSH_SIZE);
                    setBrushView(largeBrushView, LARGE_ERASER_BRUSH_SIZE);
                    PressBrushView(SMALL_ERASER_BRUSH_SIZE);
                    isBrushViewSet = true;
                }
                PressButton(ERASER_MODE);
            }
        });

        smallBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(SMALL_ERASER_BRUSH_SIZE);
            }
        });

        mediumBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(MEDIUM_ERASER_BRUSH_SIZE);
            }
        });

        largeBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(LARGE_ERASER_BRUSH_SIZE);
            }
        });

        cursorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressButton(CURSOR_MODE);
            }
        });

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { PressButton(SUGGEST_MODE); }
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
                PressButton(PEN_MODE);
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
                PressButton(PEN_MODE);
            }
        });
    }

    // 블루투스 연결
    public void OpenBTSocket() {
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(), "bluetoothAdapter error", Toast.LENGTH_LONG).show();
            return;
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("장치 선택");

            pairedDevicesList = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device.getName());
            }
            final CharSequence[] items = pairedDevicesList.toArray(new CharSequence[pairedDevicesList.size()]);
            pairedDevicesList.toArray(new CharSequence[pairedDevicesList.size()]);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (items[item].toString().equals(device.getName())) {
                            bluetoothDevice = device;
                            Log.d("BT!!", bluetoothDevice.getName());
                            break;
                        }
                    }
                    try {
                        Class<?> clazz = bluetoothDevice.getClass();
                        Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                        Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                        Object[] params = new Object[] {Integer.valueOf(1)};
                        Log.d("BT!!", "Created RFComm Connection");
                        bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, params);
                        bluetoothSocket.connect();
                        Log.d("BT!!", "Connected Succeed");
                    } catch (IOException e) {
                        Log.e("BT!!", "Connected Failed", e);
                    } catch (Exception e1) {
                        Log.e("BT!!", "Could not create RFComm Connection", e1);
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // 터치 ON/OFF
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 트랙볼 왼쪽 뒤로 가기
            isTouchMode ^= true;
            if (isTouchMode) {
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                MotionEvent downMotionEvent = MotionEvent.obtain(downTime, eventTime + 1000, MotionEvent.ACTION_DOWN, touchX, touchY, 0);
                if (mode == PEN_MODE || mode == ERASER_MODE) {
                    if (isSketchFinished) {
                        coloringView.dispatchTouchEvent(downMotionEvent);
                    } else {
                        sketchingView.dispatchTouchEvent(downMotionEvent);
                    }
                } else if (mode == CURSOR_MODE) {
                    cursorView.dispatchTouchEvent(downMotionEvent);
                }
            } else {
                MotionEvent upMotionEvent = MotionEvent.obtain(downTime, eventTime + 1000, MotionEvent.ACTION_UP, touchX, touchY, 0);
                if (mode == PEN_MODE || mode == ERASER_MODE) {
                    if (isSketchFinished) {
                        coloringView.dispatchTouchEvent(upMotionEvent);
                    } else {
                        sketchingView.dispatchTouchEvent(upMotionEvent);
                    }
                } else if (mode == CURSOR_MODE) {
                    cursorView.dispatchTouchEvent(upMotionEvent);
                }
            }
        }
        return false;
    }

    // 버튼 모드 표시
    protected  void PressButton(int mode) {
        cursorView.setDrawRectangle(false);
        cursorView.invalidate();
        switch (mode) {
            case PEN_MODE:
                penButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                eraserButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                cursorButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                sketchingView.bringToFront();
                sketchingView.setPenMode();
                break;
            case ERASER_MODE:
                penButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                eraserButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                cursorButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                brushViewLinearLayout.setVisibility(View.VISIBLE);
                sketchingView.bringToFront();
                sketchingView.setEraserMode();
                brushViewLinearLayout.bringToFront();
                break;
            case CURSOR_MODE:
                penButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                eraserButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
                cursorButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                cursorView.bringToFront();
                break;
            case SUGGEST_MODE:
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                buttonLinearLayout.setVisibility(View.INVISIBLE);
                imageViewLinearLayout.setVisibility(View.VISIBLE);
                neighborImageView1.setImageBitmap(null);
                neighborImageView2.setImageBitmap(null);
                neighborImageView3.setImageBitmap(null);
                neighborImageView4.setImageBitmap(null);
                Bitmap croppedScreenshot = getCroppedScreenshot(sketchLayout);
                File croppedScreenshotFile = Bmp2File(croppedScreenshot, String.valueOf(getFilesDir()) + "file.bin");
                SendData2Server(croppedScreenshotFile);
                stickerView.bringToFront();
                imageViewLinearLayout.bringToFront();
                neighborImageView1.setImageBitmap(croppedScreenshot);
                break;
            default:
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                break;
        }
        this.mode = mode;
    }

    // 브러쉬 사이즈 표시
    protected void setBrushView(ImageView view, float size) {
        int width = eraserButton.getWidth() * 4;
        int height = eraserButton.getHeight() * 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint drawPaint = coloringView.getDrawPaint();
        drawPaint.setColor(ContextCompat.getColor(context, R.color.black));
        drawPaint.setStrokeWidth(size);
        canvas.drawLine(width / 4, height / 2, width * 3 / 4, height / 2, drawPaint);
        view.setImageBitmap(bitmap);
    }

    // 브러쉬 모드 표시
    protected void PressBrushView(float size) {
        if (size == SMALL_ERASER_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(SMALL_ERASER_BRUSH_SIZE);
            sketchingView.setEraserMode();
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
        }
        else if (size == MEDIUM_ERASER_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(MEDIUM_ERASER_BRUSH_SIZE);
            sketchingView.setEraserMode();
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
        }
        else if (size == LARGE_ERASER_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(LARGE_ERASER_BRUSH_SIZE);
            sketchingView.setEraserMode();
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
        }
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
        colors.add("#000000");
        colors.add("#0D072E");
        colors.add("#190F3A");
        colors.add("#012362");
        colors.add("#354C96");
        colors.add("#1E468E");
        colors.add("#004547");
        colors.add("#03753A");
        colors.add("#4F93B8");
        colors.add("#95244D");
        colors.add("#690E20");
        colors.add("#461920");
        colors.add("#961D12");
        colors.add("#946314");
        colors.add("#B03402");
        colors.add("#C3732C");
        colors.add("#BEBC46");
        colors.add("#F2F2F2");

        colorPicker.setColors(colors).setColumns(6).setRoundColorButton(true).setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                coloringView.setPaintColor(color);
                colorButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            @Override
            public void onCancel() {
            }
        }).show();
    }

    // 비트맵 -> 파일
    public File Bmp2File(Bitmap bitmap, String strFilePath) {
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

    // 파일 서버 전송
    public void SendData2Server(File file){
        Log.d("TEST : ", "Request");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", "galleryfile", RequestBody.create(MultipartBody.FORM, file))
                .build();

        Request request = new Request.Builder()
                .url("http://10.221.71.95:4567/model")
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
                        }
                    });
                } catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
    public void SendScreenshot2Server(File file){
        Log.d("TEST : ", "Request");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", "sketchfile", RequestBody.create(MultipartBody.FORM, file))
                .build();

        Request request = new Request.Builder()
                .url("http://10.221.71.95:4567/conversion")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();



        client.newCall(request).enqueue(new Callback() {

//            private File directory;
//            private File fileToBeDownloaded;
//            public CallbackToDownloadFile(String directory, String fileName) {
//                this.directory = new File(directory);
//                this.fileToBeDownloaded = new File(this.directory.getAbsolutePath() + "/" + fileName);
//            }
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                MainActivity.this,
                                "파일을 다운로드할 수 없습니다. 인터넷 연결을 확인하세요.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TEST : ", "Response");
                String responseData = response.body().string();
                Log.d("TEST : ", responseData);

//                try {
//                    JSONObject json = new JSONObject(responseData);
//                    System.out.println("JSON : " + json);
//                    Toast.makeText(MainActivity.this, responseData, Toast.LENGTH_SHORT).show();
//
//                    if (!this.directory.exists()) {
//                        this.directory.mkdirs();
//                    }
//
//                    if (this.fileToBeDownloaded.exists()) {
//                        this.fileToBeDownloaded.delete();
//                    }
//
//                    try {
//                        this.fileToBeDownloaded.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Toast.makeText(
//                                        MainActivity.this,
//                                        "다운로드 파일을 생성할 수 없습니다.",
//                                        Toast.LENGTH_SHORT
//                                ).show();
//                            }
//                        });
//
//                        return;
//                    InputStream is = response.body().byteStream();
//                    OutputStream os = new FileOutputStream(this.fileToBeDownloaded);
//
//                    final int BUFFER_SIZE = 2048;
//                    byte[] data = new byte[BUFFER_SIZE];
//
//                    int count;
//                    long total = 0;
//
//                    while ((count = is.read(data)) != -1) {
//                        total += count;
//                        os.write(data, 0, count);
//                    }
//
//                    os.flush();
//                    os.close();
//                    is.close();
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(
//                                    MainActivity.this,
//                                    "다운로드가 완료되었습니다.",
//                                    Toast.LENGTH_SHORT
//                            ).show();
//                        }
//                    });
//


//                    saveImage(context, screenshotFileName, sketchScreenshot);
//                    Uri uri = FileProvider.getUriForFile(context, "edu.skku.sketchtogether.fileprovider",new File(context.getFilesDir(), "screenshot.png"));

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");    // 고정
                    shareIntent.putExtra(Intent.EXTRA_TEXT, responseData);

                    startActivity(Intent.createChooser(shareIntent, "Sharing"));
//
//                } catch(JSONException e){
//                    e.printStackTrace();
//                }
            }
        });
    }


    // txt 파일 내부 저장소 저장
    public void saveTextFile(String filename){
        try {
            FileOutputStream fos = new FileOutputStream(internalFilePath +"/"+filename, false);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(coloringView.getColors().toString());
            writer.write("\n");
            writer.write(coloringView.getAllPoints().toString());
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // 이미지 파일 내부 저장소 저장
    public void saveImage(Context context, String filename, Bitmap bitmap){
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
