package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.port.in.farm.CreateFarmUseCase;
import com.agrotrack.domain.port.in.farm.GetFarmByIdUseCase;
import com.agrotrack.domain.port.in.farm.GetFarmsUseCase;
import com.agrotrack.domain.port.in.farm.DeleteFarmUseCase;
import com.agrotrack.domain.port.in.user.AssignPersonnelUseCase;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms")
public class FarmController {

    private final CreateFarmUseCase createFarmUseCase;
    private final GetFarmsUseCase getFarmsUseCase;
    private final GetFarmByIdUseCase getFarmByIdUseCase;
    private final com.agrotrack.domain.port.in.farm.UpdateFarmUseCase updateFarmUseCase;
    private final DeleteFarmUseCase deleteFarmUseCase;
    private final AssignPersonnelUseCase assignPersonnelUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public FarmController(CreateFarmUseCase createFarmUseCase, GetFarmsUseCase getFarmsUseCase,
                          GetFarmByIdUseCase getFarmByIdUseCase, com.agrotrack.domain.port.in.farm.UpdateFarmUseCase updateFarmUseCase,
                          DeleteFarmUseCase deleteFarmUseCase, AssignPersonnelUseCase assignPersonnelUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.createFarmUseCase = createFarmUseCase;
        this.getFarmsUseCase = getFarmsUseCase;
        this.getFarmByIdUseCase = getFarmByIdUseCase;
        this.updateFarmUseCase = updateFarmUseCase;
        this.deleteFarmUseCase = deleteFarmUseCase;
        this.assignPersonnelUseCase = assignPersonnelUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Cualquier usuario logueado puede crear una finca, se convertirá en OWNER automáticamente.
    public ResponseEntity<FarmDto> createFarm(@RequestBody FarmDto farmDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Farm createdFarm = createFarmUseCase.executeCreateFarm(
                farmDto.getName(),
                farmDto.getCompanyName(),
                farmDto.getCuit(),
                farmDto.getNumberRENAPSA(),
                farmDto.getProductiveOrientation(),
                farmDto.getAddress(),
                farmDto.getPolygonLimit() != null ? farmDto.getPolygonLimit().toJtsPolygon() : null,
                farmDto.getSurface(),
                farmDto.getImageUrl()
        );

        // Assign the user as OWNER of the newly created farm
        assignPersonnelUseCase.executeAssignPersonnel(userDetails.getUsername(), "OWNER", List.of(createdFarm.getIdFarm()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdFarm.getIdFarm())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toFarmDto(createdFarm));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Farm', 'OWNER')")
    public ResponseEntity<FarmDto> updateFarm(@PathVariable UUID id, @RequestBody FarmDto farmDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Farm updatedFarm = updateFarmUseCase.executeUpdateFarm(
                id,
                farmDto.getName(),
                farmDto.getCompanyName(),
                farmDto.getCuit(),
                farmDto.getNumberRENAPSA(),
                farmDto.getProductiveOrientation(),
                farmDto.getAddress(),
                farmDto.getImageUrl()
        );
        return ResponseEntity.ok(mapper.toFarmDto(updatedFarm));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // La capa de servicio solo devuelve las fincas del usuario
    public ResponseEntity<List<FarmDto>> getMyFarms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FarmDto> userFarms = getFarmsUseCase.executeGetFarmsByUser(userDetails.getUser().getIdUser());
        return ResponseEntity.ok(userFarms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Farm', 'ANY')") // Requiere acceso a la finca
    public ResponseEntity<FarmDto> getFarmDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(getFarmByIdUseCase.executeGetFarmById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Farm', 'OWNER')")
    public ResponseEntity<Void> deleteFarm(@PathVariable UUID id, @RequestBody DeleteReasonDto reasonDto) {
        deleteFarmUseCase.executeDeleteFarm(id, reasonDto.getReason());
        return ResponseEntity.ok().build();
    }

    public static class DeleteReasonDto {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
