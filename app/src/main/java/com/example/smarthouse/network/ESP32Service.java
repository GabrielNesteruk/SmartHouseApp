package com.example.smarthouse.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.smarthouse.MainActivity;
import com.example.smarthouse.R;
import com.example.smarthouse.SettingsActivity;
import com.example.smarthouse.utility.RoomCardView.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

public class ESP32Service {

    public interface ESP32ResponseListener {
        void OnError();
        void OnResponse(Object obj);
    }

    private Context contex;
    private SharedPreferences mPreferences;

    public ESP32Service(Context context) {
        this.contex = context;
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(contex);
    }

    public void getRoomsTemperature(List<Room> rooms, ESP32ResponseListener listener) {
        String url = "http://" + mPreferences.getString(contex.getString(R.string.serverIp), "") + "/temperatures";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                rooms.add(new Room(
                                        obj.getInt("id"),
                                        obj.getString("name"),
                                        "Temperatura:",
                                        String.format("%.1f °C", obj.getDouble("temperature")),
                                        "https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80"
                                ));
                            }
                            listener.OnResponse(null);
                        } catch (JSONException e) {
                            listener.OnError();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void getSetRoomTemperature(int roomId, ESP32ResponseListener listener) {
        String url = String.format("http://%s/temperature?id=%d",
                mPreferences.getString(contex.getString(R.string.serverIp), ""),
                roomId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Double temperature = response.getDouble("setTemperature");
                    listener.OnResponse(temperature);
                } catch (JSONException e) {
                    listener.OnError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.OnError();
            }
        });

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void setRoomTemperature(int roomId, Double temperature, ESP32ResponseListener listener) {
        String url = String.format("http://%s/temperature?id=%d",
                mPreferences.getString(contex.getString(R.string.serverIp), ""),
                roomId);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("temperature", temperature);
        } catch (JSONException e) {
            listener.OnError();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.OnResponse(null);
                Toast.makeText(contex, "Nastawiono temperaturę", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.OnError();
            }
        });

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }
}
