package com.startsoft.gocareerapi.profiles.interfaces.rest;

import com.startsoft.gocareerapi.assessment.domain.model.queries.GetAllEvaluationsQuery;
import com.startsoft.gocareerapi.assessment.interfaces.rest.transform.EvaluationResourceFromEntityAssembler;
import com.startsoft.gocareerapi.profiles.domain.model.queries.GetAllProfilesQuery;
import com.startsoft.gocareerapi.profiles.domain.model.queries.GetProfileByIdQuery;
import com.startsoft.gocareerapi.profiles.domain.services.ProfileCommandService;
import com.startsoft.gocareerapi.profiles.domain.services.ProfileQueryService;
import com.startsoft.gocareerapi.profiles.interfaces.rest.resources.CreateProfileResource;
import com.startsoft.gocareerapi.profiles.interfaces.rest.resources.ProfileResource;
import com.startsoft.gocareerapi.profiles.interfaces.rest.transform.CreateProfileCommandFromResourceAssembler;
import com.startsoft.gocareerapi.profiles.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profiles Management Endpoints")
public class ProfilesController {

    private final ProfileCommandService profileCommandService;

    private final ProfileQueryService profileQueryService;


    public ProfilesController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService){
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }


    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileResource resource) {
        var createProfileCommand = CreateProfileCommandFromResourceAssembler.toCommandFromResource(resource);
        var profileId = profileCommandService.handle(createProfileCommand);
        if(profileId == 0L){
            return ResponseEntity.badRequest().build();
        }

        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);

        if(profile.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return new ResponseEntity<>(profileResource, HttpStatus.CREATED);

    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long profileId){
        var getProfileByIdQuery = new GetProfileByIdQuery(profileId);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);

    }


    @GetMapping
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var getAllProfilesQuery = new GetAllProfilesQuery();
        var profiles = profileQueryService.handle(getAllProfilesQuery);
        var profileResources = profiles.stream()
                .map(ProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(profileResources);
    }

}