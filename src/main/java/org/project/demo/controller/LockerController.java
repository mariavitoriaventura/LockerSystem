package org.project.demo.controller;

import org.project.demo.model.Locker;
import org.project.demo.service.LockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lockers")
public class LockerController {

    @Autowired
    private LockerService lockerService;

    @GetMapping
    public List<Locker> getAllLockers() {
        return lockerService.getAllLockers();
    }
}
