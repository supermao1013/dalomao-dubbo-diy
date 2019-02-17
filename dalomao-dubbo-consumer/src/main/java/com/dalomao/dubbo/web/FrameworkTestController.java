package com.dalomao.dubbo.web;

import com.dalomao.dubbo.test.FrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/framework")
public class FrameworkTestController {

    @Autowired
    private FrameworkService frameworkService;

    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        String result = frameworkService.eat("dalomao");
        System.out.println(result);
        return result;
    }
}
