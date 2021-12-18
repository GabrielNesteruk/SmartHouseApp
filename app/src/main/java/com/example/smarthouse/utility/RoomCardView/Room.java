package com.example.smarthouse.utility.RoomCardView;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Room {
    private int id;
    private String name;
    private String valueName;
    private String value;
    private String imageUrl;
}
