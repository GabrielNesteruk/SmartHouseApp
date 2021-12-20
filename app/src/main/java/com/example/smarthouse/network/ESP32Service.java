package com.example.smarthouse.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.smarthouse.MainActivity;
import com.example.smarthouse.R;
import com.example.smarthouse.SettingsActivity;
import com.example.smarthouse.model.HeatPump;
import com.example.smarthouse.model.Recuperator;
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void getHeatPumpData(ESP32ResponseListener listener) {
        String url = String.format("http://%s/sinclair",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    HeatPump heatPump = new HeatPump(
                            response.getBoolean("status"),
                            response.getInt("mode"),
                            response.getDouble("temperatureSet"),
                            response.getDouble("temperaturePanel"),
                            response.getDouble("temperatureExtract"),
                            response.getDouble("temperatureOutdoor"),
                            response.getDouble("temperatureDifference"),
                            response.getDouble("temperatureSupply"),
                            response.getInt("energyCurrent"),
                            response.getInt("energyHour"),
                            response.getInt("energyToday"),
                            response.getInt("fanSpeed")
                    );
                    listener.OnResponse(heatPump);
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

    public void turnHeatPump(boolean on, ESP32ResponseListener listener) {
        String url = String.format("http://%s/sinclair/status",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("status", on);
        } catch (JSONException e) {
            listener.OnError();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.OnResponse(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.OnError();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void setHeatpumpMode(int mode) {
        String url = String.format("http://%s/sinclair/mode?id=%d",
                mPreferences.getString(contex.getString(R.string.serverIp), ""), mode);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("mode", mode);
        } catch (JSONException e) {

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void setHeatpumpFanSpeed(int speed) {
        String url = String.format("http://%s/sinclair/fan",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("speed", speed);
        } catch (JSONException e) {

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void setHeatpumpTemperature(int temperature) {
        String url = String.format("http://%s/sinclair/temperature",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("temperature", temperature);
        } catch (JSONException e) {

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    // 0 - auto, 1 - manual
    public void setHeatpumpControl(int type) {
        String url = String.format("http://%s/sinclair/control",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("control", type);
        } catch (JSONException e) {

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void getRecuperatorData(ESP32ResponseListener listener) {
        String url = String.format("http://%s/domekt",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Recuperator recuperator = new Recuperator(
                            response.getBoolean("status"),
                            response.getInt("mode"),
                            response.getString("modeName"),
                            response.getDouble("temperatureSet"),
                            response.getDouble("temperaturePanel"),
                            response.getDouble("temperatureExtract"),
                            response.getDouble("temperatureOutdoor"),
                            response.getDouble("temperatureDifference"),
                            response.getDouble("temperatureSupply")
                    );
                    listener.OnResponse(recuperator);
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

    public void turnRecuperator(boolean on, ESP32ResponseListener listener) {
        String url = String.format("http://%s/domekt/status",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("status", on);
        } catch (JSONException e) {
            listener.OnError();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.OnResponse(null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.OnError();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

    public void getMode(ESP32ResponseListener listener) {
        String url = String.format("http://%s/mode",
                mPreferences.getString(contex.getString(R.string.serverIp), ""));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean mode = response.getBoolean("mode");
                    listener.OnResponse(mode);
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestSingleton.getInstance(contex).addToRequestQueue(request);
    }

}
