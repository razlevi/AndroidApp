package project.raz.circles.services;


import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

/*
    Asynch task used to get the places images from Google Places API
 */

public class ImageTask extends AsyncTask<String, Integer, Bitmap> {


    @Override
    protected Bitmap doInBackground(String... params) {
        // TODO Auto-generated method stub

        String urlString = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=150&photoreference=" +
                params [0] +
                "&key=AIzaSyA4LzX5hK2kzTmvQay-_AzEF5qkg6nHoD8";

        Bitmap photo = uploadImage(urlString);
        return photo;
    }

    private Bitmap uploadImage(String urlString) {
        // TODO Auto-generated method stub
        URL url;
        try {

            url = new URL(urlString);

            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return image;

        } catch (Exception e) {
            Log.e("MYTAG", e.getMessage());
        }
        return null;
    }
}
