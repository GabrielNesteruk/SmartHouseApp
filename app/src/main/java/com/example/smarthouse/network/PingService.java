package com.example.smarthouse.network;

import android.content.SharedPreferences;
import android.os.Handler;

import com.example.smarthouse.App;
import com.example.smarthouse.R;

import java.io.IOException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PingService {

    private String ip;
    private Handler handler;

    public void pingServer(ResultCallback callback) {

        App.executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime runtime = Runtime.getRuntime();
                    Process pingIpProcess = runtime.exec("/system/bin/ping -c 1 " + ip);
                    int exitValue = pingIpProcess.waitFor();
                    if(exitValue > 0)
                        sendPingResult(callback, 1);
                    else
                        sendPingResult(callback, 0);
                } catch (InterruptedException e) {
                    sendPingResult(callback, 1);
                } catch (IOException e) {
                    sendPingResult(callback, 1);
                }
            }
        });
    }

    public void updateIp(String ip) {
        this.ip = ip;
    }

    private void sendPingResult(ResultCallback callback, int result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(result);
            }
        });
    }
}
