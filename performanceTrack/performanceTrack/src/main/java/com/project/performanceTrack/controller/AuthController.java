package com.project.performanceTrack.controller;

import com.project.performanceTrack.dto.ApiResponse;
import com.project.performanceTrack.dto.LoginRequest;
import com.project.performanceTrack.dto.LoginResponse;
import com.project.performanceTrack.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Authentication controller - handles login/logout
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    // Login endpoint
    @Autowired // Tells Spring to automatically inject an instance of AuthService into this field
    private AuthService authSvc;

    // Maps HTTP POST requests sent to "/login" to this specific method
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid // Triggers Bean Validation on the 'req' object before the method runs
            @RequestBody // Tells Spring to deserialize the incoming JSON body into the 'LoginRequest' object
            LoginRequest req
    ) {
        // Calls the business logic in the service layer and stores the result
        LoginResponse resp = authSvc.login(req);

        // Returns a custom wrapper (ApiResponse) containing a success message and the login data
        // This will be automatically converted to JSON for the client
        return ApiResponse.success("Login successful", resp);
    }
    
    // Logout endpoint
    // Maps HTTP POST requests sent to "/logout" to this method
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            // Injecting the raw HttpServletRequest to access attributes set by Interceptors or Filters
            HttpServletRequest req
    ) {
        // Retrieves the "userId" that was previously saved in the request scope
        // (usually by a JWT/Security Filter after verifying the token)
        // We cast it to (Integer) because getAttribute returns a generic Object
        Integer userId = (Integer) req.getAttribute("userId");

        // Passes the userId to the service layer to handle the logout logic
        // (e.g., invalidating a session or blacklisting a JWT token in Redis)
        authSvc.logout(userId);

        // Returns a success response. <Void> indicates that no data object is
        // being returned in the "data" field of the JSON
        return ApiResponse.success("Logout successful");
    }
    
    // Change password endpoint
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        authSvc.changePassword(userId, oldPwd, newPwd);
        return ApiResponse.success("Password changed successfully");
    }
}
