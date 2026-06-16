package com.StackTrace.User_Service.Controller;

import com.StackTrace.User_Service.Service.UserService;
import com.StackTrace.User_Service.dto.UpdateProfileRequest;
import com.StackTrace.User_Service.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService us;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(us.getUser(id));
    }

        @PutMapping("/me/profile")
        public ResponseEntity<UserProfileResponse> updateProfile(
                @RequestBody UpdateProfileRequest request) {

            UserProfileResponse response =
                    us.update(request);

            return ResponseEntity.ok(response);
        }
}
