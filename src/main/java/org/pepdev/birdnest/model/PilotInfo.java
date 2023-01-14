package org.pepdev.birdnest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@RedisHash(value = "Drone", timeToLive = 600)
public class PilotInfo implements Serializable {
    String serialNumber;
    String pilotId;
    String firstName;
    String lastName;
    String phoneNumber;
    String createdDt;
    String email;
    double closestDistance;
}
