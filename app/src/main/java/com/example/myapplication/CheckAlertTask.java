//package com.example.myapplication;
//
//import static com.example.myapplication.ControlFragment.BASE_URL;
//
//import android.content.Context;
//import android.media.MediaPlayer;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class CheckAlertTask extends AsyncTask<Void, Void, Boolean> {
//    private static final String CHECK_ALERT_URL = BASE_URL + "/act_get"; // כתובת ה-URL לבדיקה
//    private static final String TAG = "AlertChecker";
//    private  Context context;
//    public CheckAlertTask(Context context){
//        this.context = context;
//    }
//
//    @Override
//    protected Boolean doInBackground(Void... voids)
//    {
//        try {
//            Log.i(TAG, "Check if need alert");
//            URL url = new URL(CHECK_ALERT_URL);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String inputLine;
//
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                JSONObject jsonResponse = new JSONObject(response.toString());
//                boolean alertStatus = jsonResponse.getBoolean("act");
//                Log.i("TTTT", "Got alertStatus: " + alertStatus);
//
//                return alertStatus;
//
//            } else {
//                Log.d(TAG, "Failed to check alert status. Response code: " + responseCode);
//            }
//            conn.disconnect();
//        } catch (Exception e) {
//            Log.e(TAG, "Error checking alert status", e);
//        }
//        return false;
//    }
//
//    @Override
//    protected void onPostExecute(Boolean alertStatus) {
//        if (alertStatus) {
//            playVoiceAlert();
//        }
//    }
//
//    private void playVoiceAlert() {
//        MediaPlayer mediaPlayer = MediaPlayer.create(this.context, R.raw.emergancy);
//        mediaPlayer.start();
//    }
//
//}
//
