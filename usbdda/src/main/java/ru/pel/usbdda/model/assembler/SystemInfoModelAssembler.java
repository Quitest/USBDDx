package ru.pel.usbdda.model.assembler;

import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.pel.usbdda.controller.api.SystemInfoController;
import ru.pel.usbdda.entity.SystemInfo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SystemInfoModelAssembler implements RepresentationModelAssembler<SystemInfo, EntityModel<SystemInfo>> {
    @Override
    public EntityModel<SystemInfo> toModel(SystemInfo entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(SystemInfoController.class).getSystemInfo(entity.getId())).withSelfRel(),
                linkTo(methodOn(SystemInfoController.class).getAllSystemInfo(PageRequest.of(0, 10))).withRel("systemInfos")
        );
    }
}
