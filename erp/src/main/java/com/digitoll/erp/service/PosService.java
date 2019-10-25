package com.digitoll.erp.service;

import com.digitoll.commons.dto.CashTerminalPosDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.commons.exception.CTPosDTOIncompleteDataException;
import com.digitoll.commons.model.CashTerminalPos;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.User;
import com.digitoll.erp.repository.PosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PosService {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PosRepository posRepository;

    @Autowired
    private UserService userService;
    //create and update
    public PosDTO savePos(PosDTO posDTO){
        Pos pos = new Pos(posDTO);
        pos = posRepository.save(pos);
        return new PosDTO(pos);
    }

    public List<PosDTO> getPoss() {
        List<PosDTO> result = new ArrayList<>();
        List<Pos> poss = posRepository.findAll();
        poss.forEach(p-> result.add(new PosDTO(p)));
        return result;
    }

    public PosDTO getPos(String posId) {
        Pos pos = posRepository.findOneById(posId);
        PosDTO result = new PosDTO(pos);
        return result;
    }

    public List<PosDTO> getPosForPartner(String partnerId) {
        List<Pos> pos = posRepository.findByPartnerId(partnerId);
        List<PosDTO> result = new ArrayList<>();
        pos.forEach(p-> result.add(new PosDTO(p)));
        return result;
    }

    public void importCashTerminalPosDb(CashTerminalPosDTO posDto, String partnerId) throws CTPosDTOIncompleteDataException {
        if (posDto == null || posDto.getTerminals() == null) {
            throw new CTPosDTOIncompleteDataException("Data miss or incomplete in CashTerminal pos!");
        }
        
        String code;
        String name;

        List<Pos> currentPosList = posRepository.findByPartnerId(partnerId);
        List<Pos> toInsert = new ArrayList<>();
        List<Pos> toUpdate = new ArrayList<>();
        Pos posToCheck;
        
        HashMap<String, Pos> currentPosMap = (HashMap<String, Pos>) currentPosList.stream().collect(Collectors.toMap(Pos::getCode, pos -> pos));
        
        /*
            Loop through the list of POS fetched from the Cashterminal website.
            Look for the code of each POS in the list of the currently available POSs in the DB.
            If it is found, check if its name is changed. If yes, add it to the
            collection containing POSs for update. If the code is not found, add
            this POS to the collection for insert.
        */
        for (HashMap<String, CashTerminalPos> pos: posDto.getTerminals()) {
            
            code = pos.keySet().iterator().next();              
            name = pos.get(code).getAddress();
            
            posToCheck = currentPosMap.get(code);
            
            if (posToCheck != null) {
                if (!posToCheck.getName().equals(name)) {
                    posToCheck.setName(name);
                    toUpdate.add(posToCheck);
                }
            }
            else {
                log.debug("code not found: " + code);
                posToCheck = new Pos();
                posToCheck.setPosIdInPartnersDb(code);
                posToCheck.setPartnerId(partnerId);
                posToCheck.setName(name);
                posToCheck.setCode(code);
                toInsert.add(posToCheck);
            }
        }
        
        if (!toInsert.isEmpty()) {
            posRepository.saveAll(toInsert);
        }
        
        if (!toUpdate.isEmpty()) {
            posRepository.saveAll(toUpdate);
        }
        
        log.info("Import summary of Cashterminal POSs: " + posDto.getTerminals().size() + " total for import, " + 
                 + toInsert.size() + " newly imported, " + toUpdate.size() + " updated");
        
    }

    public List<PosDTO> getPosForUser(String userName) {
        User user = userService.getUserDetails(userName);
        List<PosDTO> result = new ArrayList<>();
        if (user.getPosIds() != null) {
            posRepository.findByIdIn(user.getPosIds())
                    .forEach(pos -> result.add(new PosDTO(pos)));
        }
        return result;
    }
}
