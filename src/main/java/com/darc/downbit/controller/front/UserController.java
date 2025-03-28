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

    @GetMapping("/get_user_info")
    public Object getUserInfo() {
        return RestResp.ok(userService.getUserInfo());
    }

    @GetMapping("/get_upload_avatar_url")
    public Object getUploadAvatarUrl(@RequestParam("fileName") String fileName) {
        return RestResp.ok(userService.getUploadAvatarUrl(fileName));
    }

    @PostMapping("/update_user_info")
    public Object updateUserInfo(@RequestParam("nickname") String nickname,
                                 @RequestParam("intro") String intro,
                                 @RequestParam(value = "fileName", required = false) String fileName) {
        userService.updateUserInfo(nickname, intro, fileName);
        return RestResp.ok();
    }

    @GetMapping("/getTotalLikeCount")
    public Object getTotalLikeCount(@RequestParam("username") String username) {
        String likeTotal = userService.likeTotal(username);
        return RestResp.ok(likeTotal);
    }
}
