package com.mobilecnn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static File lastImage;
    private static final int imageSize = 256;

    private static RemoteCNNRequest request = new RemoteCNNRequest(null);

    private Button btnCamera;
    private Button btnSelect;

    private TextView resultText;

    private static Bitmap lastImageBitmap = null;
    private static ImageView imageTaken;

    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });

        btnSelect = (Button) findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_SELECT);
            }
        });

        resultText = (TextView) findViewById(R.id.tvLabel);
        imageTaken = (ImageView) findViewById(R.id.ivCaptured);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Successfully took an image - let's do stuff with it!
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            resizeAndSaveImage(lastImage);

            request = new RemoteCNNRequest(resultText);
            request.setFile(lastImage);
            request.start();
        }
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
