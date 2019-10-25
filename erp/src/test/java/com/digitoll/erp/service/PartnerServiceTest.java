package com.digitoll.erp.service;

import com.digitoll.commons.dto.PartnerDTO;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.Pos;
import com.digitoll.erp.repository.PartnerRepository;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.utils.ErpTestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.PARTNER_ID;
import static com.digitoll.erp.utils.ErpTestHelper.POS_ID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = PartnerService.class)
@RunWith(SpringRunner.class)
public class PartnerServiceTest {
    @MockBean
    private PosRepository posRepository;

    @MockBean
    private PartnerRepository partnerRepository;

    @Autowired
    private PartnerService partnerService;
    private ErpTestHelper erpTestHelper;

    @Before
    public void init() {
        erpTestHelper = new ErpTestHelper();
    }

    @Test
    public void testSavePartner() {
        PartnerDTO partner = erpTestHelper.createPartnerDTO();
        when(partnerRepository.save(Mockito.any(Partner.class))).thenReturn(partner);
        assertEquals(partnerService.savePartner(partner), partner);
    }

    @Test
    public void testGetPartners() {
        PartnerDTO partnerDTO = erpTestHelper.createPartnerDTO();
        List<Partner> partners = new ArrayList<>();
        partners.add(partnerDTO);
        when(partnerRepository.findAll()).thenReturn(partners);
        assertEquals(partnerService.getPartners().size(), partners.size());
    }

    @Test
    public void testGetEmptyPartners() {
        when(partnerRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(partnerService.getPartners().isEmpty());
    }

    @Test
    public void testGetPartnerById() {
        Partner partner = erpTestHelper.createPartnerDTO();
        when(partnerRepository.findOneById(Mockito.anyString())).thenReturn(partner);
        assertEquals(partnerService.getPartner(PARTNER_ID).getId(), partner.getId());
    }

    @Test
    public void testGetPartnerByIdFail() {
        when(partnerRepository.findOneById(Mockito.anyString())).thenReturn(null);
        assertNull(partnerService.getPartner(PARTNER_ID).getId());
    }

    @Test
    public void testGetPartnerPos() {
        Pos pos = erpTestHelper.createPos();
        List<Pos> poss = new ArrayList<>();
        poss.add(pos);
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(poss);
        assertEquals(partnerService.getPartnerPos(PARTNER_ID).size(), poss.size());
    }

    @Test
    public void testGetEmptyPartnerPos() {
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(new ArrayList<>());
        assertTrue(partnerService.getPartnerPos(PARTNER_ID).isEmpty());
    }

    @Test
    public void testGetPartnerPosIds() {
        Pos pos = erpTestHelper.createPos();
        List<Pos> poss = new ArrayList<>();
        poss.add(pos);
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(poss);
        assertEquals(partnerService.getPartnerPosIds(PARTNER_ID).size(), poss.size());
    }

    @Test
    public void testGetEmptyPartnerPosIds() {
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(new ArrayList<>());
        assertTrue(partnerService.getPartnerPosIds(PARTNER_ID).isEmpty());
    }
}
