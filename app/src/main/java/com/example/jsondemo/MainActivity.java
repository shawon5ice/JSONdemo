package com.example.jsondemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView temp,condition,desc;
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        temp = findViewById(R.id.temp);
        condition = findViewById(R.id.condition);
        desc = findViewById(R.id.desc);
        editText = findViewById(R.id.editText);
        go = findViewById(R.id.go);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    go(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void go(View view) {
        String cityName = editText.getText().toString();
        DownloadTask task = new DownloadTask();
        String result = null;
        hideKeyboard(MainActivity.this);
        try {
            result = task.execute("https://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid=e1470f5fa965dc0f76f66470907bff02").get();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.i("city", "go: "+cityName);
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data!=-1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                Log.i("TAG", "do: connection success");
                return result;

            } catch (Exception e) {
                Log.i("TAG", "do: connection failed");
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.isEmpty()){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String weatherInfo = jsonObject.getString("weather");
                    JSONObject  temperature = jsonObject.getJSONObject("main");
                    JSONArray jsonArray = new JSONArray(weatherInfo);
//                JSONArray jsonArray1 = new JSONArray(temperature);


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPart = jsonArray.getJSONObject(i);
                        condition.setText(jsonPart.getString("main"));
                        desc.setText(jsonPart.getString("description"));
                    }
                    temp.setText(String.valueOf(Math.round(temperature.getDouble("temp")-273))+"º");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}