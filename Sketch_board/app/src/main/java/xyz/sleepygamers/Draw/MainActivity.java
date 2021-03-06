package xyz.sleepygamers.Draw;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.sketchtogether.board.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import petrov.kristiyan.colorpicker.ColorPicker;


public class MainActivity extends AppCompatActivity {
    Bitmap finalImage;
    CoordinatorLayout coordinatorLayout;
    //private BottomNavigationView bottomNavigationView;
    private Toolbar mToolbar_bottom;
    private Toolbar toolbar;
    private customView customView;

    public MainActivity() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        customView = findViewById(R.id.custom_view);

        setBottomBar();
    }

    private void setBottomBar() {
        final ImageButton ib_delete, ib_erase, ib_color, ib_noerase;
        ib_delete = findViewById(R.id.ib_delete);
        ib_erase = findViewById(R.id.ib_erase);
        ib_color = findViewById(R.id.ib_color);
        ib_noerase = findViewById(R.id.ib_noerase);


        ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog();
            }
        });
        ib_erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_noerase.setVisibility(View.VISIBLE);
                ib_erase.setVisibility(View.GONE);
                customView.erase(true);
                ib_color.setClickable(false);

            }
        });
        ib_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });
        ib_noerase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ib_color.setClickable(true);
                ib_noerase.setVisibility(View.GONE);
                ib_erase.setVisibility(View.VISIBLE);
                customView.noterase(true);
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_save:
                checkSavePermission();
                return true;
            case R.id.action_share:
                // customView.onClickUndo();
                shareDrawing();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.delete_drawing));
        deleteDialog.setMessage(getString(R.string.new_drawing_warning));
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                customView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteDialog.show();
    }

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

        colorPicker.setRoundColorButton(true).setColumns(5).setColorButtonTickColor(Color.parseColor("#000000")).setDefaultColorButton(Color.parseColor("#000000")).setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {
                customView.setPaintColor(color);
            }

            @Override
            public void onCancel() {
            }
        }).show();

    }

    public void saveDrawingDialog() {
        //save drawing attach to Notification Bar and let User Open Image to share.
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save drawing");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saveThisDrawing();
            }
        });
        saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        saveDialog.show();
    }


    private void saveThisDrawing() {
        String path = Environment.getExternalStorageDirectory().toString();
        path = path + "/" + getString(R.string.app_name);
        File dir = new File(path);
        //save drawing
        customView.setDrawingCacheEnabled(true);

        //attempt to save
        String imTitle = "Drawing" + "_" + System.currentTimeMillis() + ".png";
        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), customView.getDrawingCache(),
                imTitle, "a drawing");

        try {
            if (!dir.isDirectory() || !dir.exists()) {
                dir.mkdirs();
            }
            customView.setDrawingCacheEnabled(true);
            File file = new File(dir, imTitle);
            FileOutputStream fOut = new FileOutputStream(file);
            Bitmap bm = customView.getDrawingCache();
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Oops! Image could not be saved. Do you have enough space in your device?1");
            alert.setPositiveButton("OK", null);
            alert.show();

        }


        if (imgSaved != null) {
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();
        }

        customView.destroyDrawingCache();

    }

    private void checkSavePermission() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            saveThisDrawing();
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void shareDrawing() {
        customView.setDrawingCacheEnabled(true);
        customView.invalidate();

        Bitmap bitmap = Bitmap.createBitmap(customView.getWidth(), customView.getHeight(), Bitmap.Config.RGB_565);
        customView.draw(new Canvas(bitmap));

        try {

            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpg"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        File imagePath = new File(this.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.jpg");
        Uri contentUri = FileProvider.getUriForFile(this, "xyz.sleepygamers.scribble.fileprovider", newFile);


        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }

    }

}




