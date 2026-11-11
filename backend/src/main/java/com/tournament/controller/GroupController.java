
package com.tournament.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournament.dto.GroupResponse;
import com.tournament.service.GroupService;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<GroupResponse>> generateGroups(
            @PathVariable Long tournamentId) {

        List<GroupResponse> groups =
                groupService.generateGroups(tournamentId);

        return ResponseEntity.ok(groups);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups(
            @PathVariable Long tournamentId) {

        List<GroupResponse> groups =
                groupService.getGroups(tournamentId);

        return ResponseEntity.ok(groups);
    }
}
