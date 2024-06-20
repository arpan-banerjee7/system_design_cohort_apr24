
package com.application.slackpoc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public ModelAndView chat(@RequestParam String userId) {
        ModelAndView mav = new ModelAndView("chat");
        mav.addObject("userId", userId);
        return mav;
    }

}

