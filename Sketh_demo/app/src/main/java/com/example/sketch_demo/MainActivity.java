package com.example.sketch_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private final int GET_IMAGE_FOR_GALLERYVIEW = 1;
    private ImageView galleryImage;
    private ImageView suggestImage1;
    private ImageView suggestImage2;
    private ImageView suggestImage3;
    private ImageView suggestImage4;
    private Button suggestButton;
    Bitmap bitmap = null;
    Module module = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        galleryImage = findViewById(R.id.galleryImageView);
        suggestImage1 = findViewById(R.id.suggestedImageView1);
        suggestImage2 = findViewById(R.id.suggestedImageView2);
        suggestImage3 = findViewById(R.id.suggestedImageView3);
        suggestImage4 = findViewById(R.id.suggestedImageView4);
        suggestButton = findViewById(R.id.suggestButton);
        //suggestButton.setVisibility(View.INVISIBLE);

        galleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_IMAGE_FOR_GALLERYVIEW);
            }
        });

        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                suggestImage1.setImageResource(R.drawable.image_icon);
                suggestImage2.setImageResource(R.drawable.image_icon);
                suggestImage3.setImageResource(R.drawable.image_icon);
                suggestImage4.setImageResource(R.drawable.image_icon);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri;
        RequestOptions option1 = new RequestOptions().centerInside();
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            System.out.println(selectedImageUri);
            Log.d("logcheck", selectedImageUri.toString());
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                Log.d("logcheck", bitmap.toString());
                module = LiteModuleLoader.load(assetFilePath(this, "model.pt"));
                Log.d("logcheck", module.toString());
            } catch (IOException e) {
                Log.e("log", "Error reading assets", e);
                finish();
            }
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
            Log.d("logcheck", inputTensor.toString());
            final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
            Log.d("logcheck", outputTensor.toString());

            float[] scores = outputTensor.getDataAsFloatArray();

            float maxScore = -Float.MAX_VALUE;

            int maxScoreIdx = -1;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxScoreIdx = i;
                }
            }

            String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];
            Log.d("logcheck3", className.toString());
            // showing className on UI
            TextView textView = findViewById(R.id.textView);
            textView.setText(className);

            Glide.with(getApplicationContext()).load(selectedImageUri).apply(option1).into(galleryImage);
//            suggestImage1.setImageResource(0);
//            suggestImage2.setImageResource(0);
//            suggestImage3.setImageResource(0);
//            suggestImage4.setImageResource(0);
        }
    }
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}