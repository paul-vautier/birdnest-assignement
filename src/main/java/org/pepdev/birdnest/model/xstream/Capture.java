package org.pepdev.birdnest.model.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@XStreamAlias("capture")
public class Capture {

    @XStreamImplicit(itemFieldName = "drone")
    private List<DroneInfo> droneInfos = Collections.emptyList();
}
