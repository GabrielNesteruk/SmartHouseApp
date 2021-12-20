package com.example.smarthouse.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class HeatPump {
    private boolean status;
    private int mode;
    private Double temperatureSet;
    private Double temperaturePanel;
    private Double temperatureExtract;
    private Double temperatureOutdoor;
    private Double temperatureDifference;
    private Double temperatureSupply;
    private int energyCurrent;
    private int energyHour;
    private int energyToday;
    private int fanSpeed;
}
