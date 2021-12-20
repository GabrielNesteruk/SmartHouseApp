package com.example.smarthouse.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Recuperator {
    private boolean status;
    private int mode;
    private String modeName;
    private Double temperatureSet;
    private Double temperaturePanel;
    private Double temperatureExtract;
    private Double temperatureOutdoor;
    private Double temperatureDifference;
    private Double temperatureSupply;
}
