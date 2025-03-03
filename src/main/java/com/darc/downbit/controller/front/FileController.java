package com.darc.downbit.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/9-02:01:52
 * @description
 */
@RestController
public class FileController {

    @GetMapping("/upload")
    public Object upload() {
        return "upload";
    }
}
