package com.digitoll.erp.controller;

import com.digitoll.commons.dto.PartnerDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.erp.service.PartnerService;
import com.digitoll.erp.utils.ErpTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest( secure = false)
@ContextConfiguration(classes = {
        PartnerController.class
})
public class PartnerControllerTest {
    private static final String PARTNER_ID = "partnerId";
    private static final String PARTNER_NAME = "partnerName";
    private static final String KAPSCH_PARTNER_ID = "kapschPartnerId";

    @MockBean
    private PartnerService partnerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testCreatePartner() throws Exception {
        PartnerDTO partnerDTOInput = createPartnerDTO();
        PartnerDTO partnerDTOOutput = createPartnerDTO();
        partnerDTOInput.setId(null);

        when(partnerService.savePartner(refEq(partnerDTOInput))).thenReturn(partnerDTOOutput);

        verifyPartner(mvc.perform(post("/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partnerDTOOutput))));
    }

    @Test
    public void testCreatePartnerStatusIsBadRequest() throws Exception {
        mvc.perform(post("/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPartners() throws Exception {
        List<PartnerDTO> partnerDTOs = new ArrayList<>(1);
        PartnerDTO partnerDTO = createPartnerDTO();

        partnerDTOs.add(partnerDTO);

        when(partnerService.getPartners()).thenReturn(partnerDTOs);

        verifyPartnerList(mvc.perform(get("/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)));
    }

    @Test
    public void testUpdatePartner() throws Exception {
        PartnerDTO serviceInputParameter = createPartnerDTO();
        PartnerDTO outputPartner = createPartnerDTO();
        PartnerDTO inputPartner = createPartnerDTO();
        inputPartner.setId(null);

        when(partnerService.savePartner(refEq(serviceInputParameter))).thenReturn(outputPartner);

        verifyPartner(mvc.perform(put("/partners/"+PARTNER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputPartner))));
    }

    @Test
    public void testUpdatePartnerStatusIsBadRequest() throws Exception {
        mvc.perform(put("/partners/"+PARTNER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPartnerByID() throws Exception {
        PartnerDTO partnerDTO = createPartnerDTO();
        when(partnerService.getPartner(PARTNER_ID)).thenReturn(partnerDTO);

        verifyPartner(mvc.perform(get("/partners/"+PARTNER_ID)));
    }

    @Test
    public void testGetPartnerPos() throws Exception {
        ErpTestHelper erpTestHelper = new ErpTestHelper();
        PosDTO posDTO = erpTestHelper.createPosDTO();

        List<PosDTO> posDTOList = new ArrayList<>(1);

        posDTOList.add(posDTO);

        when(partnerService.getPartnerPos(PARTNER_ID)).thenReturn(posDTOList);

        mvc.perform(get("/partners/"+PARTNER_ID+"/pos")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code")
                        .value(ErpTestHelper.CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value(ErpTestHelper.NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    private PartnerDTO createPartnerDTO() {
        PartnerDTO partnerDTO = new PartnerDTO();

        partnerDTO.setId(PARTNER_ID);
        partnerDTO.setKapschPartnerId(KAPSCH_PARTNER_ID);
        partnerDTO.setName(PARTNER_NAME);

        return partnerDTO;
    }

    private void verifyPartnerList(ResultActions andExpect) throws Exception {
        andExpect.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value(PARTNER_NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kapschPartnerId")
                        .value(KAPSCH_PARTNER_ID));
    }

    private void verifyPartner(ResultActions andExpect) throws Exception {
        andExpect.andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                .value(PARTNER_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                .value(PARTNER_NAME))
        .andExpect(MockMvcResultMatchers.jsonPath("$.kapschPartnerId")
                .value(KAPSCH_PARTNER_ID));
    }
}