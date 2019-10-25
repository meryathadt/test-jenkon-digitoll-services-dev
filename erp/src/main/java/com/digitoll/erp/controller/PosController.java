package com.digitoll.erp.controller;

import com.digitoll.commons.dto.CashTerminalPosDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.commons.exception.CTPosDTOIncompleteDataException;
import com.digitoll.erp.service.PosService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/pos")
public class PosController {

    @Autowired
    private PosService posService;

    /**
     *
     * @param posDTO
     * @param principal - Used for aspects
     * @return
     */
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PostMapping
    public PosDTO createPos( @RequestBody PosDTO posDTO, Principal principal) {
        if(posDTO.getId()!=null){
            posDTO.setId(null);
        }
        return posService.savePos(posDTO);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<PosDTO> testGetAllPos() {
        return posService.getPoss();
    }

    /**
     *
     * @param posDTO
     * @param principal - Used for aspects
     * @param posId
     * @return
     */
    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @PutMapping(value = "/{id}")
    public PosDTO updatePos(
            @RequestBody
                    PosDTO posDTO,
            Principal principal,
            @PathVariable(value = "id",required = true)
            @ApiParam(name = "id", example = "Pos id, e.g. 000019397114")
                    String posId) {
        posDTO.setId(posId);
        return posService.savePos(posDTO);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public PosDTO testGetPosById(
            @PathVariable(value = "id",required = true)
            @ApiParam(name = "id", example = "Pos id, e.g. 000019397114")
                    String posId) {
        return posService.getPos(posId);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN')")
    @GetMapping(value = "/{partner_id}/all")
    public List<PosDTO> getPosByPartnerId(
            @PathVariable(value = "partner_id")
            @ApiParam(name = "partner_id", example = "Partner id, e.g. 000019397114")
                    String partnerId) {
        return posService.getPosForPartner(partnerId);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_PARTNER_ADMIN') or hasAuthority('ROLE_C2')")
    @GetMapping(value = "/user/all")
    public List<PosDTO> getPosForUser(Principal principal) {
        return posService.getPosForUser(principal.getName());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(value = "/import/cashTerminal/{partnerId}")
    public void importCashTerminalPos(
            @RequestBody CashTerminalPosDTO posDTO,
            @PathVariable(value = "partnerId", required = true)
            @ApiParam(name = "partnerId", example = "partnerId, e.g. 1, 2")
                    String partnerId) throws CTPosDTOIncompleteDataException {
        posService.importCashTerminalPosDb(posDTO, partnerId);
    }        
}
