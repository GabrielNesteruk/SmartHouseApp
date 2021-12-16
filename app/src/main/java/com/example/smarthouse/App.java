package com.example.smarthouse;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    public static ExecutorService executorService = Executors.newFixedThreadPool(2);
}
