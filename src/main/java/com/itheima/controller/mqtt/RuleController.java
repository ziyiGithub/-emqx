package com.itheima.controller.mqtt;


import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/resource")
public class RuleController {


    @PostMapping("/process")
    public void process(@RequestBody Map<String, Object> map) {
        map.entrySet().stream().forEach(x ->{
            System.out.println(x.getKey() +""+x.getValue());
        });

    }
}
