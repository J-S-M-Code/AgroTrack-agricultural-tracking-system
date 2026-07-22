package com.agrotrack.application.mapper;

import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.application.dto.LotDto;
import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.application.dto.UserDto;
import com.agrotrack.application.dto.AlertDto;
import com.agrotrack.application.dto.CropDto;
import com.agrotrack.application.dto.AnimalDto;
import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import com.agrotrack.application.dto.PointDto;
import com.agrotrack.application.dto.PolygonDto;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ApplicationDtoMapper {

    FarmDto toFarmDto(Farm farm);
    List<FarmDto> toFarmDtoList(List<Farm> farms);

    LotDto toLotDto(Lot lot);
    List<LotDto> toLotDtoList(List<Lot> lots);

    @Mapping(target = "farmIds", expression = "java(mapFarmsToIds(user.getManagedFarms()))")
    UserDto toUserDto(User user);
    List<UserDto> toUserDtoList(List<User> users);
    
    default List<UUID> mapFarmsToIds(List<Farm> farms) {
        if (farms == null) return null;
        return farms.stream().map(Farm::getIdFarm).collect(java.util.stream.Collectors.toList());
    }

    @Mapping(source = "creator.idUser", target = "creatorId")
    @Mapping(source = "assigned.idUser", target = "assignedId")
    @Mapping(source = "relatedFarm.idFarm", target = "relatedFarmId")
    @Mapping(source = "relatedLot.idLot", target = "relatedLotId")
    TaskDto toTaskDto(Task task);
    List<TaskDto> toTaskDtoList(List<Task> tasks);

    @Mapping(source = "author.idUser", target = "authorId")
    @Mapping(source = "relatedLot.idLot", target = "relatedLotId")
    @Mapping(source = "relatedCrop.idCrop", target = "relatedCropId")
    @Mapping(source = "relatedAnimal.idAnimal", target = "relatedAnimalId")
    AlertDto toAlertDto(Alert alert);
    List<AlertDto> toAlertDtoList(List<Alert> alerts);

    @Mapping(source = "assignedLot.idLot", target = "lotId")
    CropDto toCropDto(Crop crop);
    List<CropDto> toCropDtoList(List<Crop> crops);

    @Mapping(source = "assignedLot.idLot", target = "assignedLotId")
    @Mapping(source = "collar.idCollar", target = "assignedCollarId")
    AnimalDto toAnimalDto(Animal animal);
    List<AnimalDto> toAnimalDtoList(List<Animal> animals);

    default PolygonDto mapPolygon(Polygon polygon) {
        return PolygonDto.fromJtsPolygon(polygon);
    }

    default Polygon mapPolygonDto(PolygonDto dto) {
        if (dto == null) return null;
        return dto.toJtsPolygon();
    }

    default PointDto mapPoint(Point point) {
        return PointDto.fromJtsPoint(point);
    }

    default Point mapPointDto(PointDto dto) {
        if (dto == null) return null;
        return dto.toJtsPoint();
    }
}
