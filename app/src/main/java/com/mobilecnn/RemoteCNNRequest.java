package com.mobilecnn;

import android.os.AsyncTask;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;

/**
 * Created by daniel on 5/9/15.
 */
public class RemoteCNNRequest extends AsyncTask<File, Integer, String> {
    private static final String POST_URL = "http://54.81.96.73:5000";

    private String result = "Waiting...";

    private TextView resultText;

    public RemoteCNNRequest(TextView resultText){
        this.resultText = resultText;
        if (resultText != null){
            updateTextview();
        }
    }

    @Override
    protected String doInBackground(File... imageFile) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(POST_URL);

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody("model", "alex");

            FileBody fileBody = new FileBody(imageFile[0]);
            builder.addPart("image", fileBody);

            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);

            InputStream io = response.getEntity().getContent();
            String responseString = IOUtils.toString(io, "UTF-8");

            return responseString;
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return "No result found.";
    }

    @Override
    public void onPostExecute(String result){
        this.result = result;
        updateTextview();
    }

    private void updateTextview() {
        resultText.setText(result);
    }
}
