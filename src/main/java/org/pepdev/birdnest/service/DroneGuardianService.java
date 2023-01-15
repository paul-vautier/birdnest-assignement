package org.pepdev.birdnest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import org.pepdev.birdnest.dao.PilotRepository;
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

import java.time.LocalDateTime;
import java.util.*;

/**
 * Class representing the guardian that monitors the drones
 */
@Service
public class DroneGuardianService {

    private final XStream xStream;
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

    private final PilotRepository pilotRepository;

    private static final Comparator<PilotInfo> SORT_BY_LATEST_OBSERVATION = Comparator.nullsLast(Comparator.comparing(PilotInfo::getLatestObservation));

    @Autowired
    public DroneGuardianService(PilotRepository pilotRepository) {
        this.xStream = new XStream();
        this.xStream.processAnnotations(Report.class);
        this.xStream.processAnnotations(Capture.class);
        this.xStream.allowTypes(new Class[]{Report.class, Capture.class, DroneInfo.class});
        this.xStream.ignoreUnknownElements();
        this.xStream.alias("report", Report.class);
        this.pilotRepository = pilotRepository;
    }


    /**
     * Queries drone's data every 2 seconds and verifies if they are within the no fly zone. Adds they to database or updates them if they are closer to the nest
     */
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

    /**
     * Checks if a drone is within the no fly zone
     * @param droneInfo the drone's data
     * @return false if they are within {@link DroneGuardianService#radius} meters from the nest
     */
    private boolean isWithinBounds(DroneInfo droneInfo) {
        return this.getDistanceFromNest(droneInfo) <= radius;
    }

    /**
     * Calculates the distance from the nest
     * @param droneInfo the drone's data
     * @return the 2D distance from the nest position and the drone's position
     */
    private double getDistanceFromNest(DroneInfo droneInfo) {
        return Math.hypot(pX - droneInfo.getPositionX(), pY - droneInfo.getPositionY()) / 1000;
    }

    /**
     * Adds a drone to the database if they are not present. If they are present, updates the position if they are closer than their closest registered position
     * @param droneInfo the information of a drone detected in the no-fly zone
     */
    private void addOrUpdateDroneIfCloser(DroneInfo droneInfo) {
        double distance = this.getDistanceFromNest(droneInfo);

        Optional<PilotInfo> pilotInfo = pilotRepository.findById(droneInfo.getSerialNumber()).map(pilot -> {
            pilot.setClosestDistance(Math.min(pilot.getClosestDistance(), distance));
            return Optional.of(pilot);
        }).orElseGet(() -> this.getPilotInformation(droneInfo.getSerialNumber()).map(pilot -> {
            pilot.setClosestDistance(distance);
            return pilot;
        }));

        pilotInfo.ifPresent((pilot) -> {
            pilot.setLatestObservation(LocalDateTime.now());
            pilotRepository.save(pilot);
        });
    }

    /**
     * Parses drones position from XML stream
     * @param xml the XML input
     * @return a non-null {@link List} of drones
     */
    public List<DroneInfo> dronesFromXML(String xml) {
        Report report = (Report) xStream.fromXML(xml);
        if (report == null || report.getCapture() == null || report.getCapture().getDroneInfos() == null) {
            return Collections.emptyList();
        }
        return report.getCapture().getDroneInfos();
    }

    /**
     * Calls Birdnest API to return a drone pilot's information using his serial number.
     *
     * @param serialNumber the drone's serial number
     * @return An optional containing the drone's pilot data if the response is 200, otherwise returns an empty optional
     */
    public Optional<PilotInfo> getPilotInformation(String serialNumber) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> result = template.exchange(BIRDNEST_PILOTS_ENDPOINT + serialNumber, HttpMethod.GET, entity, String.class);
        if (result.getStatusCode() == HttpStatus.OK) {
            try {
                PilotInfo pilotInfo = new ObjectMapper().readValue(result.getBody(), PilotInfo.class);
                pilotInfo.setSerialNumber(serialNumber);
                return Optional.of(pilotInfo);
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }


    /**
     * Retrieves pilots informations from the database
     * @return a non null {@link List} of pilots from the database
     */
    public List<PilotInfo> getPilotInfos() {
        List<PilotInfo> pilotInfos = new ArrayList<>();
        pilotRepository.findAll().forEach(pilotInfos::add);
        pilotInfos.sort(SORT_BY_LATEST_OBSERVATION);
        // We have to filter non-null objects because of an ongoing issue on Spring Redis Data where phantom keys that have yet to expire may return null data
        return pilotInfos.stream().filter(Objects::nonNull).toList();
    }
}
