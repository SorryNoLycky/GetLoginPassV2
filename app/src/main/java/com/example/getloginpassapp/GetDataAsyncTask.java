package com.example.getloginpassapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class GetDataAsyncTask extends AsyncTask<String, Void, String>{

    public AsyncResponce delegate;

    public GetDataAsyncTask(AsyncResponce delegate){
        this.delegate = delegate;
    }
    public interface AsyncResponce{
        void processFinish(String output);
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            String login = strings[0];
            String pass = strings[1];
            String url_link = strings[2];

            String data = URLEncoder.encode("login", "UTF-8") + "=" + URLEncoder.encode(login, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");

            URL url = new URL (url_link);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());

            wr.write(data);
            wr.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                sb.append(line);
                break;
            }
            //Log.d(TAG, "doInBackground: ");
            return sb.toString();

        } catch (Exception e){
            return new String("Exeption:" + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(s);
        if(result != null && !result.equals("")){
            delegate.processFinish(result);
        }
    }
}