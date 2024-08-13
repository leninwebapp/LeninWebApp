package com.mycompany.webserverlenin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author khanny
 */
@RestController
public class ReleasingController {
    private final ReleasingService releasingService;

    @Autowired
    public ReleasingController(ReleasingService releasingService) {
        this.releasingService = releasingService;
    }

    @GetMapping("/release/{jobCode}")
    public String releaseJob(@PathVariable String jobCode) {
        releasingService.getDateReleased(jobCode);
        releasingService.getTimeReleased(jobCode);
        boolean success = releasingService.releaseJobCode(jobCode);
        return success ? "Release successful" : "Release failed";
    }
    
}
