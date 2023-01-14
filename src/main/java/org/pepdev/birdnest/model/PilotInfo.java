package org.pepdev.birdnest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "Drone")
public class PilotInfo implements Serializable {
    @Id
    String serialNumber;
    String pilotId;
    String firstName;
    String lastName;
    String phoneNumber;
    String createdDt;
    String email;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    LocalDateTime latestObservation;
    double closestDistance;
}
