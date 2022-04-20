package org.otcframework.web.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OtcErrorController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "Some error has occured - If you manually edited OTCL contents, please recheck.";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }


}
