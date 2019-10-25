package com.digitoll.erp.controller;

import com.digitoll.commons.dto.PartnerDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.erp.service.PartnerService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/partners")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public PartnerDTO createPartner( @RequestBody PartnerDTO partnerDTO) {
        if(partnerDTO.getId()!=null){
            partnerDTO.setId(null);
        }
        return partnerService.savePartner(partnerDTO);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<PartnerDTO> getPartners() {
        return partnerService.getPartners();
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(value = "/{id}")
    public PartnerDTO updatePartner(
            @RequestBody
            PartnerDTO partnerDTO,
            @PathVariable(value = "id",required = true)
            @ApiParam(name = "id", example = "Partner id, e.g. 000019397114")
            String partnerId) {
        partnerDTO.setId(partnerId);
        return partnerService.savePartner(partnerDTO);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public PartnerDTO getPartners(
            @PathVariable(value = "id",required = true)
            @ApiParam(name = "id", example = "Partner id, e.g. 000019397114")
                    String partnerId) {
        return partnerService.getPartner(partnerId);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/{id}/pos")
    public List<PosDTO> getPartnerPos(
            @PathVariable(value = "id",required = true)
            @ApiParam(name = "id", example = "Partner id, e.g. 000019397114")
                    String partnerId) {
        return partnerService.getPartnerPos(partnerId);
    }
}
