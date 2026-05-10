package com.portfolio.fintech.user;

import com.portfolio.fintech.common.ApiResponse;
import com.portfolio.fintech.user.dto.OnboardUserRequest;
import com.portfolio.fintech.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/onboard")
    public ApiResponse<UserResponse> onboard(@Valid @RequestBody OnboardUserRequest request) {
        return ApiResponse.ok("Customer onboarded and wallet opened", userService.onboard(request));
    }
}
