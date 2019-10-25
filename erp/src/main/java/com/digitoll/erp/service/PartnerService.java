package com.digitoll.erp.service;

import com.digitoll.commons.dto.PartnerDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.Pos;
import com.digitoll.erp.repository.PartnerRepository;
import com.digitoll.erp.repository.PosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PartnerService {
    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PosRepository posRepository;

    //create and update
    public PartnerDTO savePartner(PartnerDTO partnerDTO){
        Partner partner = new Partner(partnerDTO);
        partner = partnerRepository.save(partner);
        return new PartnerDTO(partner);
    }

    public List<PartnerDTO> getPartners() {
        List<PartnerDTO> result = new ArrayList<>();
        List<Partner> partners = partnerRepository.findAll();
        partners.forEach(p-> result.add(new PartnerDTO(p)));
        return result;
    }

    public PartnerDTO getPartner(String partnerId) {
        Partner partner = partnerRepository.findOneById(partnerId);
        return new PartnerDTO(partner);
    }

    public List<PosDTO> getPartnerPos(String partnerId) {
        List<Pos> pos = posRepository.findByPartnerId(partnerId);
        List<PosDTO> result = new ArrayList<>();
        pos.forEach(p-> result.add(new PosDTO(p)));
        return result;
    }

    // quick check for contains
    public List<String> getPartnerPosIds(String partnerId){
        List<String> ids = new ArrayList<>();
        List<PosDTO> pos = getPartnerPos(partnerId);
        pos.forEach(p-> ids.add(p.getId()));
        return ids;
    }
}
