package org.pepdev.birdnest.controller;

import org.pepdev.birdnest.dao.DroneRepository;
import org.pepdev.birdnest.model.PilotInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = "/birdnest")
public class BirdnestController {
    private DroneRepository droneRepository;

    @Autowired
    public BirdnestController(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @GetMapping
    @ResponseBody
    public List<PilotInfo> getNonCompliantDrones() {
        List<PilotInfo> pilotInfos = new ArrayList<>();
        droneRepository.findAll().forEach(pilotInfos::add);
        return pilotInfos;
    }

    @GetMapping("/")
    public ModelAndView mainPage() {
        return new ModelAndView();
    }
}
