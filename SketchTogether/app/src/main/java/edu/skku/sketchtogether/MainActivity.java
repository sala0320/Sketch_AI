package edu.skku.sketchtogether;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
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

    private static final float SMALL_BRUSH_SIZE = 20;
    private static final float MEDIUM_BRUSH_SIZE = 60;
    private static final float LARGE_BRUSH_SIZE = 100;
    private boolean isSketchFinished = false;
    private boolean isEraserMode = false;
    private boolean isTouchMode = false;

    Bitmap sketchScreenShot; // 스케치 캡쳐
    Bitmap croppedScreenshot; // 인공지능에 넣을 부분 캡쳐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        findViewsById();
        OpenBTSocket();

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
                        if (isSketchFinished) {
                            coloringView.dispatchTouchEvent(moveMotionEvent);
                        }
                        else {
                            sketchingView.dispatchTouchEvent(moveMotionEvent);
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
                builder.setTitle("스케치를 완료하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isSketchFinished) {
                            // SendMessageByBT("Bluetooth!");
                            Toast.makeText(getApplicationContext(), "채색이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            sketchScreenShot = getScreenshot(sketchingView);

                            // 로봇팔에 sketchScreenShot 전송

                            isSketchFinished = true;
                            brushViewLinearLayout.setVisibility(View.INVISIBLE);
                            finishButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            penButton.setVisibility(View.GONE);
                            eraserButton.setVisibility(View.GONE);
                            cursorButton.setVisibility(View.GONE);
                            suggestButton.setVisibility(View.GONE);
                            colorButton.setVisibility(View.VISIBLE);
                            brushButton.setVisibility(View.VISIBLE);
                            coloringView.setVisibility(View.VISIBLE);
                            coloringView.bringToFront();
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
                if (!isSketchFinished) {
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
                if (!isEraserMode) {
                    coloringView.setPenBrushSize(SMALL_BRUSH_SIZE);
                }
                else if (!isSketchFinished) {
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
                if (!isEraserMode) {
                    coloringView.setPenBrushSize(MEDIUM_BRUSH_SIZE);
                }
                else if (!isSketchFinished) {
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
                if (!isEraserMode) {
                    coloringView.setPenBrushSize(LARGE_BRUSH_SIZE);
                }
                else if (!isSketchFinished) {
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
                File fileimg = BitmapConvertFile(croppedScreenshot, String.valueOf(getFilesDir()) + "file.bin");
                SendData2Server(fileimg);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("key pressed", String.valueOf(event.getKeyCode()));
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d("KeyUP Event", "볼륨업 키 down");
                isTouchMode ^= true;
                if (isTouchMode) {
                    downTime = SystemClock.uptimeMillis();
                    eventTime = SystemClock.uptimeMillis();
                    MotionEvent downMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_DOWN, touchX, touchY, 0);
                    if (isSketchFinished) {
                        coloringView.dispatchTouchEvent(downMotionEvent);
                    }
                    else {
                        sketchingView.dispatchTouchEvent(downMotionEvent);
                    }
                }
                else {
                    MotionEvent upMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_UP, touchX, touchY, 0);
                    if (isSketchFinished) {
                        coloringView.dispatchTouchEvent(upMotionEvent);
                    }
                    else {
                        sketchingView.dispatchTouchEvent(upMotionEvent);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d("KeyUP Event", "볼륨다운 키 down");
                return true;
        }
        return false;
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
                            System.out.println(bluetoothDevice);
                            break;
                        }
                    }

                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(bluetoothUUID);
                        System.out.println(bluetoothSocket);
                        if(bluetoothSocket.isConnected())
                            bluetoothSocket.close();
                        else
                            bluetoothSocket.connect();
                    } catch (IOException e) {
                        Log.e("BluetoothService", "Connect Fail");
                        Toast.makeText(getApplicationContext(), "bluetoothSocket.connect() error", Toast.LENGTH_LONG).show();
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            Toast.makeText(getApplicationContext(), "pairing error", Toast.LENGTH_LONG).show();
        }
    }

    // 블루투스로 텍스트 전송
    public void SendMessageByBT(String message) {
        OutputStream mmOutStream = null;
        try {
            mmOutStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "bluetoothSocket.getOutputStream() error", Toast.LENGTH_LONG).show();
        }

        byte[] bytes = message.getBytes();
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "mmOutStream.write() error", Toast.LENGTH_LONG).show();
        }
    }

    // 비트맵 -> 파일
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

    // 파일 서버 전송
    public void SendData2Server(File file){
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
        if (isEraserMode) {
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