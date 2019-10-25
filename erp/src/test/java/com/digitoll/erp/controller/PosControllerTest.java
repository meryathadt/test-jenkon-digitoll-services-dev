package com.digitoll.erp.controller;

import com.digitoll.commons.dto.CashTerminalPosDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.erp.service.PosService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.USERNAME;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(secure = false)
@ContextConfiguration(classes = {
        PosController.class
})
public class PosControllerTest {
    @MockBean
    protected Principal principal;

    @MockBean
    private PosService posService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private ErpTestHelper erpTestHelper = new ErpTestHelper();

    private static final String CODE = "code";
    private static final String KAPSCH_POS_ID = "kapschPosId";
    private static final String NAME = "name";
    private static final String PARTNER_ID = "partnerId";
    private static final String POS_ID_IN_PARTNERS_DB = "posIdInPartnersDb";
    private static final String POS_ID = "posId";


    @Test
    public void testCreatePos() throws Exception {
        PosDTO posDTO = erpTestHelper.createPosDTO();

        PosDTO returnedPos = new PosDTO(posDTO);
        returnedPos.setId(POS_ID);
        when(posService.savePos(refEq(posDTO))).thenReturn(returnedPos);

        mvc.perform(post("/pos")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(returnedPos)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testCreatePosStatusIsBadRequest() throws Exception {

        mvc.perform(post("/pos")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllPos() throws Exception {
        List<PosDTO> posDTOList = new LinkedList<>();

        PosDTO posDTO = erpTestHelper.createPosDTO();
        posDTO.setId(POS_ID);

        posDTOList.add(posDTO);

        when(posService.getPoss()).thenReturn(posDTOList);

        mvc.perform(get("/pos")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code")
                        .value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testUpdatePos() throws Exception {
        PosDTO posDTO = erpTestHelper.createPosDTO();
        when(posService.savePos(posDTO)).thenReturn(posDTO);

        PosDTO posWithId = new PosDTO(posDTO);
        posWithId.setId(POS_ID);

        when(posService.savePos(refEq(posWithId))).thenReturn(posWithId);
        mvc.perform(put("/pos/" + POS_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(posDTO)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testUpdatePosStatusIsBadRequest() throws Exception {

        mvc.perform(put("/pos/" + POS_ID)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPosByID() throws Exception {
        PosDTO posDTO = erpTestHelper.createPosDTO();
        posDTO.setId(POS_ID);

        when(posService.getPos(POS_ID)).thenReturn(posDTO);

        mvc.perform(get("/pos/" + POS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$.partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testGetPosByPartnerId() throws Exception {
        List<PosDTO> posDTOList = new ArrayList<>(1);

        PosDTO posDTO = erpTestHelper.createPosDTO();
        posDTO.setId(POS_ID);
        posDTOList.add(posDTO);

        when(posService.getPosForPartner(PARTNER_ID)).thenReturn(posDTOList);

        mvc.perform(get("/pos/" + PARTNER_ID + "/all")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code")
                        .value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testGetPosForUser() throws Exception {
        List<PosDTO> posDTOList = new ArrayList<>(1);

        PosDTO posDTO = erpTestHelper.createPosDTO();
        posDTO.setId(POS_ID);
        posDTOList.add(posDTO);

        when(posService.getPosForUser(principal.getName())).thenReturn(posDTOList);

        mvc.perform(get("/pos/user/all")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).principal(principal))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code")
                        .value(CODE))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kapschPosId")
                        .value(ErpTestHelper.POS_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                        .value(NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].partnerId")
                        .value(PARTNER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].posIdInPartnersDb")
                        .value(ErpTestHelper.POS_ID));
    }

    @Test
    public void testImportCashTerminalPos() throws Exception {
        CashTerminalPosDTO cashTerminalPosDTO = new ErpTestHelper().createCashterminalPosDTO(ErpTestHelper.POS_TERMINAL_KEY, ErpTestHelper.POS_TERMINAL_ADDRESS);

        mvc.perform(post("/pos/import/cashTerminal/" + PARTNER_ID)
                .content(objectMapper.writeValueAsString(cashTerminalPosDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(posService).importCashTerminalPosDb(cashTerminalPosDTO, PARTNER_ID);
    }

    @Test
    public void testImportCashTerminalPosStatusIsBadRequest() throws Exception {

        mvc.perform(post("/pos/import/cashTerminal/" + PARTNER_ID)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }
}