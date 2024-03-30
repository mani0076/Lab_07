package com.example.lab_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lab_1.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    protected String cityName;
    protected RequestQueue queue = null;
    protected Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView(binding.getRoot());

        binding.forecastButton.setOnClickListener(click -> {
            cityName = binding.cityTextField.getText().toString();
            String stringURL = null;
            try {
                stringURL = "https://api.openweathermap.org/data/2.5/weather?q="
                        + URLEncoder.encode(cityName,"UTF-8")
                        + "&appid=7e943c97096a9784391a981c4d878b22&units=metric";
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, stringURL, null,
                    (response) ->{

                            try {
                                JSONObject coord = response.getJSONObject( "coord" );
                                JSONArray weatherArray = response.getJSONArray ( "weather" );
                                int vis = response.getInt("visibility");
                                String name = response.getString( "name" );
                                JSONObject position0 = weatherArray.getJSONObject(0);
                                String description = position0.getString("description");
                                String iconName = position0.getString("icon");
                                JSONObject mainObject = response.getJSONObject( "main" );
                                double current = mainObject.getDouble("temp");
                                double minTemp = mainObject.getDouble("temp_min");
                                double maxTemp = mainObject.getDouble("temp_max");
                                int humidity = mainObject.getInt("humidity");

                                String imagePath = getFilesDir() + "/"+ iconName + ".png";
                                File imageFile = new File(imagePath);

                                if(imageFile.exists()){
                                    image = BitmapFactory.decodeFile(imagePath);
                                }
                                else {
                                    // The image file does not exists

                                    String imageUrl = "https://openweathermap.org/img/w/" + iconName + ".png";
                                    ImageRequest imgReq = new ImageRequest(imageUrl, new Response.Listener<Bitmap>(){

                                        public void onResponse(Bitmap bitmap) {
                                            // Do something with loaded bitmap...
                                            FileOutputStream fOut = null;
                                            try {
                                                fOut = openFileOutput( iconName + ".png", Context.MODE_PRIVATE);
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                                fOut.flush();
                                                fOut.close();
                                                image = bitmap;
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }, 1024, 1024, ImageView.ScaleType.CENTER, null, (error ) -> {
                                    });
                                    queue.add(imgReq);
                                }
                                runOnUiThread( ( ) -> {

                                binding.temp.setText("The current temperature is "+ current);
                                binding.temp.setVisibility(View.VISIBLE);

                                binding.minTemp.setText("The minimum temperature is "+ minTemp);
                                binding.minTemp.setVisibility(View.VISIBLE);

                                binding.maxTemp.setText("The maximum temperature is "+ maxTemp);
                                binding.maxTemp.setVisibility(View.VISIBLE);

                                binding.humitidy.setText("The humidity is "+ humidity);
                                binding.humitidy.setVisibility(View.VISIBLE);

                                binding.icon.setImageBitmap(image);  //Variable 'image' is accessed from within inner class, needs to be final or effectively final
                                binding.icon.setVisibility(View.VISIBLE);

                                binding.description.setText( description);
                                binding.description.setVisibility(View.VISIBLE);

                                });

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                    },
                    (error)->{});
            queue.add(request);
        });
    }
}