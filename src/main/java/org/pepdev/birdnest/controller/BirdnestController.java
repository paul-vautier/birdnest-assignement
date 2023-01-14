package org.pepdev.birdnest.controller;

import org.pepdev.birdnest.dao.PilotRepository;
import org.pepdev.birdnest.model.PilotInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping(path = "/")
public class BirdnestController {
    private final PilotRepository pilotRepository;

    private static final Comparator<PilotInfo> SORT_BY_LATEST_OBSERVATION = Comparator.nullsLast(Comparator.comparing(PilotInfo::getLatestObservation));

    @Autowired
    public BirdnestController(PilotRepository pilotRepository) {
        this.pilotRepository = pilotRepository;
    }

    @GetMapping("/drones")
    @ResponseBody
    public List<PilotInfo> getNonCompliantDrones() {
        return this.getPilotInfos();
    }

    @GetMapping("")
    public ModelAndView mainPage() {
        List<PilotInfo> pilotInfos = this.getPilotInfos();
        ModelAndView model = new ModelAndView("index");
        model.addObject("pilots", pilotInfos);
        return model;
    }

    private List<PilotInfo> getPilotInfos() {
        List<PilotInfo> pilotInfos = new ArrayList<>();
        pilotRepository.findAll().forEach(pilotInfos::add);
        pilotInfos.sort(SORT_BY_LATEST_OBSERVATION);
        return pilotInfos;
    }
}
