package org.pepdev.birdnest.controller;

import org.pepdev.birdnest.model.PilotInfo;
import org.pepdev.birdnest.service.DroneGuardianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping(path = "/")
public class BirdnestController {
    private final DroneGuardianService droneGuardianService;

    @Autowired
    public BirdnestController(DroneGuardianService droneGuardianService) {
        this.droneGuardianService = droneGuardianService;

    }

    @GetMapping("/drones")
    @ResponseBody
    public List<PilotInfo> getNonCompliantDrones() {
        return droneGuardianService.getPilotInfos();
    }

    @GetMapping("")
    public ModelAndView mainPage() {
        List<PilotInfo> pilotInfos = droneGuardianService.getPilotInfos();
        ModelAndView model = new ModelAndView("index");
        model.addObject("pilots", pilotInfos);
        return model;
    }
}
