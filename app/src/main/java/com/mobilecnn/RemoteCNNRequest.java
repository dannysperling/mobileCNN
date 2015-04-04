package com.mobilecnn;

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
import java.io.IOException;

public class RemoteCNNRequest extends Thread {

    private static final String POST_URL = "http://54.81.96.73:5000";

    private File imageFile;
    private String result = "Waiting...";

    private TextView resultText;

    public RemoteCNNRequest(TextView resultText){
        this.resultText = resultText;
        if (resultText != null){
            resultText.setText(result);
        }
    }

    public void setFile(File imageFile){
        this.imageFile = imageFile;
    }

    @Override
    public void run(){
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(POST_URL);

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody("model", "alex");

            FileBody fileBody = new FileBody(imageFile);
            builder.addPart("image", fileBody);

            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);

            //TODO: This will need to change a bit
            if (resultText != null){
                resultText.setText(response.getEntity().getContent().toString());
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
    }
}
