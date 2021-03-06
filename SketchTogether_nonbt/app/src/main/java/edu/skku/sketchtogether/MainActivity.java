package edu.skku.sketchtogether;

import static androidx.core.content.FileProvider.getUriForFile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

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
    TextView textView;
    FloatingActionButton finishButton;
    FloatingActionButton removeButton;
    FloatingActionButton penButton;
    FloatingActionButton colorButton;
    FloatingActionButton eraserButton;
    FloatingActionButton cursorButton;
    FloatingActionButton suggestButton;
    FloatingActionButton deleteButton;
    FloatingActionButton checkButton;
    FloatingActionButton voiceButton;
    LinearLayout imageViewLinearLayout;
    ImageView neighborImageView1;
    ImageView neighborImageView2;
    ImageView neighborImageView3;
    ImageView neighborImageView4;
    ImageView imageView;

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
    private static final int PEN_MODE = 1;
    private static final int ERASER_MODE = 2;
    private static final int CURSOR_MODE = 3;
    private static final int SUGGEST_MODE = 4;
    private int mode;
    private boolean isSketchFinished = false;
    private boolean isTouchMode = false;
    private boolean isBrushViewSet = false;

    private final String pointsFileName = "points.txt";
    private final String colorsFileName = "colors.txt";
    private final String screenshotFileName = "screenshot.png";

    private Bitmap sketchScreenshot; // ????????? ??????
    private String filePath;
    SpeechRecognizer mRecognizer;
    Intent intent;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

        findViewsById();
        // OpenBTSocket();
        File fileDir = getFilesDir();
        filePath = fileDir.getPath();

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
                        MotionEvent moveMotionEvent = MotionEvent.obtain(downTime, eventTime + 1000, MotionEvent.ACTION_MOVE, touchX, touchY, 0);
                        if (mode == PEN_MODE || mode == ERASER_MODE) {
                            if (isSketchFinished) {
                                coloringView.dispatchTouchEvent(moveMotionEvent);
                            } else {
                                sketchingView.dispatchTouchEvent(moveMotionEvent);
                            }
                        } else if (mode == CURSOR_MODE) {
                            cursorView.dispatchTouchEvent(moveMotionEvent);
                        }
                    }
                }
                return false;
            }
        });
        System.out.println(Build.VERSION.SDK_INT);
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        // RecognizerIntent ??????
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName()); // ????????? ???
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); // ?????? ??????

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "???????????? ??????.", Toast.LENGTH_SHORT).show();
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this); // ??? SpeechRecognizer ??? ????????? ????????? ?????????
                mRecognizer.setRecognitionListener(listener); // ????????? ??????
                mRecognizer.startListening(intent); // ?????? ??????
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("?????????????????????????");
                builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isSketchFinished) {
                            // ???????????? txt ?????? ??????
                            saveTextFile(pointsFileName, coloringView.getAllPoints().toString());
                            saveTextFile(colorsFileName, coloringView.getAllColors().toString());
                            Toast.makeText(getApplicationContext(), "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        } else {
                            sketchScreenshot = getScreenshot(sketchingView);
                            saveImage(context, screenshotFileName, sketchScreenshot);

                            File sketchScreenShotFile = BitmapConvertFile(sketchScreenshot, String.valueOf(getFilesDir()) + "sketch.bin");
                            SendData2Server(sketchScreenShotFile);

//                            Uri uri = Uri.fromFile(new File(context.getFilesDir(), "screenshot.png"));
//                            Uri uri = Uri.parse(filePath + "/screenshot.png");
//                            Uri uri = Uri.fromFile(sketchScreenShotFile);
//                            Uri uri = FileProvider.getUriForFile(context, "com.test.android.test.filepsrovider", sketchScreenShotFile);
//                            Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + "/capture.jpeg");
//                            Uri uri = Uri.parse("file://data/data/edu.skku.stickerview/files/screenshot.png");
                            Uri uri = FileProvider.getUriForFile(context, "edu.skku.sketchtogether.fileprovider",new File(context.getFilesDir(), "screenshot.png"));

                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");    // ??????
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(shareIntent, "Sharing"));

                            // ???????????? svg ????????? ???????????? ??????

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
                            coloringView.setPenMode();
                            Toast.makeText(getApplicationContext(), "???????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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
                builder.setTitle("???????????? ????????????????????????????");
                builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        brushViewLinearLayout.setVisibility(View.INVISIBLE);
                        if (isSketchFinished) {
                            coloringView.eraseAll();
                        } else {
                            sketchingView.eraseAll();
                            PressButton(PEN_MODE);
                        }
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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
                    setBrushView(smallBrushView, SMALL_BRUSH_SIZE);
                    setBrushView(mediumBrushView, MEDIUM_BRUSH_SIZE);
                    setBrushView(largeBrushView, LARGE_BRUSH_SIZE);
                    PressBrushView(SMALL_BRUSH_SIZE);
                    isBrushViewSet = true;
                }
                PressButton(ERASER_MODE);
            }
        });

        smallBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(SMALL_BRUSH_SIZE);
            }
        });

        mediumBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(MEDIUM_BRUSH_SIZE);
            }
        });

        largeBrushView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressBrushView(LARGE_BRUSH_SIZE);
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
            public void onClick(View v) {
                PressButton(SUGGEST_MODE);
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
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // ????????? ????????? ??????????????? ??????
            Toast.makeText(getApplicationContext(),"???????????? ??????",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
            // ????????? ???????????? ??? ??????
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // ???????????? ????????? ????????? ?????????
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // ?????? ???????????? ????????? ??? ????????? buffer??? ??????
        }

        @Override
        public void onEndOfSpeech() {
            // ???????????? ???????????? ??????
        }

        @Override
        public void onError(int error) {
            // ???????????? ?????? ?????? ????????? ???????????? ??? ??????
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "??????????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "???????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "????????? ????????????";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "?????? ??? ??????";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER ??? ??????";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "????????? ?????????";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "????????? ????????????";
                    break;
                default:
                    message = "??? ??? ?????? ?????????";
                    break;
            }

            Toast.makeText(getApplicationContext(), "?????? ?????? : " + message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            // ?????? ????????? ???????????? ??????
            // ?????? ?????? ArrayList??? ????????? ?????? textView??? ????????? ?????????
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for(int i = 0; i < matches.size() ; i++){
                textView.setText(matches.get(i));
                System.out.println(matches.get(i));
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // ?????? ?????? ????????? ????????? ??? ?????? ??? ??????
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // ?????? ???????????? ???????????? ?????? ??????
        }
    };

    // ???????????? ??????
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
            builder.setTitle("?????? ??????");

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

    // ?????? ON/OFF & ???????????? ?????????
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A: // ????????? A
            case KeyEvent.KEYCODE_BACK: // ????????? ?????? ?????? ??????
                isTouchMode ^= true;
                if (isTouchMode) {
                    downTime = SystemClock.uptimeMillis();
                    eventTime = SystemClock.uptimeMillis();
                    MotionEvent downMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_DOWN, touchX, touchY, 0);
                    if (mode == PEN_MODE || mode == ERASER_MODE) {
                        if (isSketchFinished) {
                            coloringView.dispatchTouchEvent(downMotionEvent);
                        } else {
                            sketchingView.dispatchTouchEvent(downMotionEvent);
                        }
                    }
                    else if (mode == CURSOR_MODE) {
                        cursorView.dispatchTouchEvent(downMotionEvent);
                    }
                }
                else {
                    MotionEvent upMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_UP, touchX, touchY, 0);
                    if (mode == PEN_MODE || mode == ERASER_MODE) {
                        if (isSketchFinished) {
                            coloringView.dispatchTouchEvent(upMotionEvent);
                        } else {
                            sketchingView.dispatchTouchEvent(upMotionEvent);
                        }
                    }
                    else if (mode == CURSOR_MODE) {
                        cursorView.dispatchTouchEvent(upMotionEvent);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isTouchMode) {
                    keyboardTouchMove(keyCode);
                }
                return true;
        }
        return false;
    }

    // ????????? ?????? ?????????
    protected void keyboardTouchMove(int keyCode) {
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                touchY -= 5;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                touchY += 5;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                touchX -= 5;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                touchX += 5;
                break;
        }
        MotionEvent moveMotionEvent = MotionEvent.obtain(downTime, eventTime+1000, MotionEvent.ACTION_MOVE, touchX, touchY, 0);
        if (isSketchFinished) {
            coloringView.dispatchTouchEvent(moveMotionEvent);
        }
        else {
            sketchingView.dispatchTouchEvent(moveMotionEvent);
        }
    }

    // ?????? ?????? ??????
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
                Bitmap croppedScreenshot = getCroppedScreenshot(sketchLayout);
                File croppedScreenshotFile = BitmapConvertFile(croppedScreenshot, String.valueOf(getFilesDir()) + "file.bin");
                SendData2Server(croppedScreenshotFile);
                stickerView.bringToFront();
                imageViewLinearLayout.bringToFront();
                break;
            default:
                brushViewLinearLayout.setVisibility(View.INVISIBLE);
                break;
        }
        this.mode = mode;
    }

    // ????????? ????????? ??????
    protected void setBrushView(ImageView view, float size) {
        int width = eraserButton.getWidth() * 4;
        int height = eraserButton.getHeight() * 2;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint drawPaint = coloringView.getDrawPaint();
        drawPaint.setColor(ContextCompat.getColor(context, R.color.dark_gray));
        drawPaint.setStrokeWidth(size);
        canvas.drawLine(width / 4, height / 2, width * 3 / 4, height / 2, drawPaint);
        view.setImageBitmap(bitmap);
    }

    // ????????? ?????? ??????
    protected void PressBrushView(float size) {
        if (size == SMALL_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(SMALL_BRUSH_SIZE);
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
        }
        else if (size == MEDIUM_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(MEDIUM_BRUSH_SIZE);
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
        }
        else if (size == LARGE_BRUSH_SIZE) {
            sketchingView.setEraserBrushSize(LARGE_BRUSH_SIZE);
            smallBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            mediumBrushView.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
            largeBrushView.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }

    // ????????? ??????
    protected void loadSticker(ImageView imageView){
        Drawable drawable = imageView.getDrawable();
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        stickerView.addSticker(drawableSticker);
    }

    // ??? ?????? ??????
    protected Bitmap getScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // ??? ?????? ??????
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

    // ?????? ??????
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
                colorButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onCancel() {
            }
        }).show();
    }

    // ????????? -> ??????
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

    // ?????? ?????? ??????
    public void SendData2Server(File file){
        Log.d("TEST : ", "Request");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", "galleryfile", RequestBody.create(MultipartBody.FORM, file))
                .build();

        Request request = new Request.Builder()
                .url("http://blee.iptime.org:22222/model")
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

    // txt ?????? ?????? ????????? ??????
    public void saveTextFile(String filename, String contents){
        try {
            FileOutputStream fos = new FileOutputStream(filePath+"/"+filename, false);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(contents);
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // ????????? ?????? ?????? ????????? ??????
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
        voiceButton = findViewById(R.id.voiceButton);
        textView = findViewById(R.id.textView);
        imageViewLinearLayout = findViewById(R.id.imageViewLinearLayout);
        neighborImageView1 = findViewById(R.id.neighborImageView1);
        neighborImageView2 = findViewById(R.id.neighborImageView2);
        neighborImageView3 = findViewById(R.id.neighborImageView3);
        neighborImageView4 = findViewById(R.id.neighborImageView4);
    }

}