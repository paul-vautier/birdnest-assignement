package org.pepdev.birdnest.model.xstream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class DroneInfo {
    String serialNumber;
    float positionX;
    float positionY;
}
