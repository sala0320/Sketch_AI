package edu.skku.sketchdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.skku.sketchdemo.ml.EfficientnetLite4Fp32;
import edu.skku.sketchdemo.ml.EfficientnetLite4Int82;

public class MainActivity extends AppCompatActivity {
    private final int GET_IMAGE_FOR_GALLERYVIEW = 201;
    private ImageView galleryImage;
    private ImageView suggestImage1;
    private ImageView suggestImage2;
    private ImageView suggestImage3;
    private ImageView suggestImage4;
    private Button suggestButton;
    private Bitmap galleryImageBmp;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        galleryImage = findViewById(R.id.galleryImageView);
        suggestImage1 = findViewById(R.id.suggestedImageView1);
        suggestImage2 = findViewById(R.id.suggestedImageView2);
        suggestImage3 = findViewById(R.id.suggestedImageView3);
        suggestImage4 = findViewById(R.id.suggestedImageView4);
        suggestButton = findViewById(R.id.suggestButton);
        suggestButton.setVisibility(View.INVISIBLE);

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
                Drawable galleryImageDrawable = galleryImage.getDrawable();
                galleryImageBmp = ((BitmapDrawable)galleryImageDrawable).getBitmap();
                Bitmap galleryImageBmpResized = Bitmap.createScaledBitmap(galleryImageBmp, 300, 300, false);

                try {
                    EfficientnetLite4Fp32 model = EfficientnetLite4Fp32.newInstance(MainActivity.this);
                    TensorImage image = TensorImage.fromBitmap(galleryImageBmpResized);

                    EfficientnetLite4Fp32.Outputs outputs = model.process(image);
                    TensorBuffer feature = outputs.getFeatureAsTensorBuffer();
                    System.out.println("feature : " + Arrays.toString(feature.getFloatArray()));

                    model.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<String> suggestedImageList = new ArrayList<String>();
                suggestedImageList.add("cat_0");
                suggestedImageList.add("cat_12");
                suggestedImageList.add("cat_42");
                suggestedImageList.add("cat_62");

                Resources resources = MainActivity.this.getResources();
                int resourceId1 = resources.getIdentifier(suggestedImageList.get(0), "drawable", MainActivity.this.getPackageName());
                int resourceId2 = resources.getIdentifier(suggestedImageList.get(1), "drawable", MainActivity.this.getPackageName());
                int resourceId3 = resources.getIdentifier(suggestedImageList.get(2), "drawable", MainActivity.this.getPackageName());
                int resourceId4 = resources.getIdentifier(suggestedImageList.get(3), "drawable", MainActivity.this.getPackageName());

                suggestImage1.setImageResource(resourceId1);
                suggestImage2.setImageResource(resourceId2);
                suggestImage3.setImageResource(resourceId3);
                suggestImage4.setImageResource(resourceId4);

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
            // Glide.with(getApplicationContext()).load(selectedImageUri).apply(option1).into(galleryImage);
            Glide.with(getApplicationContext()).asBitmap().load(selectedImageUri).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    galleryImage.setImageBitmap(resource);
                }
            });
            suggestButton.setVisibility(View.VISIBLE);
            suggestImage1.setImageResource(0);
            suggestImage2.setImageResource(0);
            suggestImage3.setImageResource(0);
            suggestImage4.setImageResource(0);
        }
    }
}