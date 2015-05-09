package com.mobilecnn;

import android.os.AsyncTask;
import android.widget.TextView;

import com.sh1r0.caffe_android_demo.CaffeMobile;

import java.io.File;

public class LocalCNNTask extends AsyncTask<File, Integer, Integer> {

    private CaffeMobile caffeMobile;
    private String[] imagenetClasses;
    private String result = "Waiting...";
    private TextView resultText;

    public LocalCNNTask(TextView resultText, CaffeMobile caffeMobile, String[] imagenetClasses){
        this.resultText = resultText;
        this.caffeMobile = caffeMobile;
        this.imagenetClasses = imagenetClasses;
        if (resultText != null){
           updateTextview();
        }
    }

    @Override
    protected Integer doInBackground(File... imageFile) {
        return caffeMobile.predictImage(imageFile[0].getAbsolutePath());
    }

    @Override
    public void onPostExecute(Integer result){
        this.result = imagenetClasses[result];
        updateTextview();
    }

    private void updateTextview() {
        resultText.setText(result);
    }
}
