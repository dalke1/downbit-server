package com.darc.downbit.controller.front;

import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/7/22-5:31:22
 * @description
 */

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;


    @PreAuthorize("@authValidator.hasRole('ADMIN')")
    @PostMapping("/add_user")
    public Object addUser(@RequestBody User user) {
        if (userService.addUser(user)) {
            return RestResp.ok("添加成功");
        }
        return RestResp.internalServerError("添加失败,服务器异常");
    }

    @GetMapping("/get_avatar")
    public Object getAvatar() {
        return RestResp.ok(userService.getAvatar());
    }
}
