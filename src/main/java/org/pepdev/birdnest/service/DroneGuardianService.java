package org.pepdev.birdnest.service;

import com.thoughtworks.xstream.XStream;
import org.pepdev.birdnest.dao.DroneRepository;
import org.pepdev.birdnest.model.PilotInfo;
import org.pepdev.birdnest.model.xstream.Capture;
import org.pepdev.birdnest.model.xstream.DroneInfo;
import org.pepdev.birdnest.model.xstream.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DroneGuardianService {

    private XStream xStream;
    @Value("${birdnest.api.endpoint.drones}")
    private String BIRDNEST_DRONES_ENDPOINT;

    @Value("${birdnest.api.endpoint.pilots}")
    private String BIRDNEST_PILOTS_ENDPOINT;

    @Value("${birdnest.guardian.x}")
    private float pX;

    @Value("${birdnest.guardian.y}")
    private float pY;

    @Value("${birdnest.guardian.radius}")
    private float radius;

    private DroneRepository droneRepository;

    @Autowired
    public DroneGuardianService(DroneRepository droneRepository) {
        this.xStream = new XStream();
        this.xStream.processAnnotations(Report.class);
        this.xStream.processAnnotations(Capture.class);
        this.xStream.allowTypes(new Class[] {Report.class, Capture.class, DroneInfo.class});
        this.xStream.ignoreUnknownElements();
        this.xStream.alias("report", Report.class);
        this.droneRepository = droneRepository;
    }


    @Scheduled(fixedRateString = "${birdnest.api_delay}")
    public void getCurrentDroneData() {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> result = template.exchange(BIRDNEST_DRONES_ENDPOINT, HttpMethod.GET, entity, String.class);
        if (result.getStatusCode() == HttpStatus.OK) {
            List<DroneInfo> detectedDrones = this.dronesFromXML(result.getBody());
            detectedDrones.stream().filter(this::isWithinBounds).forEach(this::addOrUpdateDroneIfCloser);
        }
    }
    private boolean isWithinBounds(DroneInfo droneInfo) {
        return Math.hypot(pX - droneInfo.getPositionX(), pY - droneInfo.getPositionY()) <= radius;
    }

    private void addOrUpdateDroneIfCloser(DroneInfo droneInfo) {
        double distance = Math.hypot(pX - droneInfo.getPositionX(), pY - droneInfo.getPositionY());

        Optional<PilotInfo> pilotInfo = droneRepository.findById(droneInfo.getSerialNumber()).map(pilot -> {
            pilot.setClosestDistance(Math.min(pilot.getClosestDistance(), distance));
            return Optional.of(pilot);
        }).orElseGet(() -> this.getPilotInformation(droneInfo.getSerialNumber()).map(pilot -> {
            pilot.setClosestDistance(distance);
            return pilot;
        }));

        pilotInfo.ifPresent(droneRepository::save);
    }
    public List<DroneInfo> dronesFromXML(String xml) {

        Report report = (Report) xStream.fromXML(xml);
        if (report == null || report.getCapture() == null) {
            return Collections.emptyList();
        }

        return report.getCapture().getDroneInfos();
    }

    /**
     * Calls Birdnest API to return a drone pilot's information using his serial number.
     *
     * @param serialNumbers the drone's serial number
     * @return An optional containing the drone's pilot data if the response is 200, otherwise returns an empty optional
     */
    public Optional<PilotInfo> getPilotInformation(String serialNumbers) {

        return Optional.empty();
    }
}
