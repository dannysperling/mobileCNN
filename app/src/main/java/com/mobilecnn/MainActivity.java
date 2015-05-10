package com.mobilecnn;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import com.sh1r0.caffe_android_demo.CaffeMobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static String[] IMAGENET_CLASSES;

    private static File lastImage;
    private static final int imageSize = 256;

    private TextView resultText;

    private static Bitmap lastImageBitmap = null;
    private static ImageView imageTaken;

    private Uri fileUri;

    private CaffeMobile caffeMobile;
    private static final String DEPLOY_PROTO ="/sdcard/caffe_mobile/mobilecnn/mobilecnn_deploy.prototxt";
    private static final String CAFFE_MODEL = "/sdcard/caffe_mobile/mobilecnn/mobilecnn.caffemodel";
    private static final String CLASS_WORDS = "synset_words.txt";

    public static boolean useLocal;

    private AsyncTask<?, ?, ?> task;

    static {
        System.loadLibrary("caffe");
        System.loadLibrary("caffe_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });

        Button btnSelect = (Button) findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_SELECT);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.fragment_preference, false);
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        useLocal = appPreferences.getBoolean("useLocalCNN", false);


        resultText = (TextView) findViewById(R.id.tvLabel);
        imageTaken = (ImageView) findViewById(R.id.ivCaptured);


        //Get caffe mobile working!
        caffeMobile = new CaffeMobile();
        caffeMobile.enableLog(true);
        caffeMobile.loadModel(DEPLOY_PROTO, CAFFE_MODEL);

        AssetManager am = this.getAssets();
        try {
            InputStream is = am.open(CLASS_WORDS);
            Scanner sc = new Scanner(is);
            List<String> lines = new ArrayList<>();
            while (sc.hasNextLine()) {
                final String temp = sc.nextLine();
                lines.add(temp.substring(temp.indexOf(" ") + 1));
            }
            IMAGENET_CLASSES = lines.toArray(new String[lines.size()]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Successfully took an image - let's do stuff with it!
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_SELECT) && resultCode == RESULT_OK) {

            File image;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                image = lastImage;
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = MainActivity.this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgPath = cursor.getString(columnIndex);
                image = new File(imgPath);
                cursor.close();
            }

            resizeAndSaveImage(image);

            // Cancel previous task
            if (task != null) {
                task.cancel(true);
            }

            // Either send to the server or perform local check
            if (useLocal) {
                task = new LocalCNNTask(resultText, caffeMobile, IMAGENET_CLASSES).execute(image);
            } else {
                task = new RemoteCNNRequest(resultText).execute(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static void resizeAndSaveImage(File file){
        BitmapFactory.Options options = new BitmapFactory.Options();

        if (lastImageBitmap != null) {
            lastImageBitmap.recycle();
        }

        // Need to rotate the image taken, at least on this device. Can make modular
        Matrix matrix = new Matrix();
        matrix.postRotate(270);

        Bitmap bmo = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        lastImageBitmap =Bitmap.createBitmap(bmo, 0, 0, bmo.getWidth(), bmo.getHeight(), matrix, true);
        imageTaken.setImageBitmap(lastImageBitmap);
        Bitmap scaled = Bitmap.createScaledBitmap(lastImageBitmap, imageSize, imageSize, true);

        saveImageToTempFile(scaled);
        bmo.recycle();
        scaled.recycle();
    }

    //Saves the image to the given filename
    private static void saveImageToTempFile(Bitmap bmp){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MobileCNN");
        File tempFile =  new File(mediaStorageDir.getPath() + File.separator + "temp.png");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile.getAbsolutePath());
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent();
                intent.setClassName(this, "com.mobilecnn.CNNPreferences");
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        lastImage = getOutputMediaFile(type);
        return Uri.fromFile(lastImage);
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MobileCNN");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()) {
            if (! mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
