package org.pepdev.birdnest.model.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XStreamAlias("report")
public class Report {
    private Capture capture;
}
