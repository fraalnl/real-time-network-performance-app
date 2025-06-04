package com.ericsson.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import com.ericsson.dto.EngineerDto;
import com.ericsson.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
public class AdminController {
    private final UserService userService;
    public AdminController(final UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/engineers")
    public ResponseEntity<EntityModel<Map<String, String>>> createEngineer(@RequestBody final EngineerDto dto) {
        String token = userService.createEngineer(dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Engineer account created successfully with role ENGINEER");
        response.put("jwtToken", token);

        EntityModel<Map<String, String>> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(AdminController.class).createEngineer(dto)).withSelfRel());
        return ResponseEntity.ok(resource);
    }
}
