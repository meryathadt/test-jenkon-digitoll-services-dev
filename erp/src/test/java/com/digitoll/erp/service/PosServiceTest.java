package com.digitoll.erp.service;

import com.digitoll.commons.dto.CashTerminalPosDTO;
import com.digitoll.commons.dto.PosDTO;
import com.digitoll.commons.exception.CTPosDTOIncompleteDataException;
import com.digitoll.commons.model.CashTerminalPos;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.User;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = PosService.class)
@RunWith(SpringRunner.class)
public class PosServiceTest {
    @MockBean
    private PosRepository posRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private PosService posService;
    private ErpTestHelper erpTestHelper;
    private Pos pos;

    @Before
    public void init() {
        erpTestHelper = new ErpTestHelper();
        pos = erpTestHelper.createPos();
    }

    @Test
    public void testSavePos() {
        PosDTO posDTO = new PosDTO(pos);
        when(posRepository.save(Mockito.any(Pos.class))).thenReturn(pos);
        assertEquals(posService.savePos(posDTO).getName(), pos.getName());
        assertEquals(posService.savePos(posDTO).getCode(), pos.getCode());
    }

    @Test
    public void testSavePosFail() {
        when(posRepository.save(Mockito.any(Pos.class))).thenReturn(null);
        assertNull(posService.savePos(null).getId());
    }

    @Test
    public void testGetPoss() {
        List<Pos> posses = new ArrayList<>();
        posses.add(pos);
        when(posRepository.findAll()).thenReturn(posses);
        assertEquals(posService.getPoss().size(), posses.size());
    }

    @Test
    public void testGetEmptyPoss() {
        when(posRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(posService.getPoss().isEmpty());
    }

    @Test
    public void testGetPosById() {
        when(posRepository.findOneById(Mockito.anyString())).thenReturn(pos);
        assertEquals(posService.getPos(POS_ID).getId(), pos.getId());
    }

    @Test
    public void testGetPosByIdFail() {
        when(posRepository.findOneById(Mockito.anyString())).thenReturn(null);
        assertNull(posService.getPos(POS_ID).getId());
    }

    @Test
    public void testGetPossForPartner() {
        List<Pos> posses = new ArrayList<>();
        posses.add(pos);
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(posses);
        assertEquals(posService.getPosForPartner(PARTNER_ID).size(), posses.size());
    }

    @Test
    public void testGetEmptyPossForPartner() {
        when(posRepository.findByPartnerId(Mockito.anyString())).thenReturn(new ArrayList<>());
        assertTrue(posService.getPosForPartner(PARTNER_ID).isEmpty());
    }
    
    @Test
    public void testImportCashTerminalPosNoChanges() throws CTPosDTOIncompleteDataException {
        CashTerminalPosDTO ctPos = erpTestHelper.createCashterminalPosDTO(ErpTestHelper.CODE, ErpTestHelper.POS_NAME);
        Pos currentPos = erpTestHelper.createPos();
        PosDTO posDto = new PosDTO();
        posDto.setPartnerId(PARTNER_ID);
        posDto.setCode(ErpTestHelper.CODE);
        posDto.setName(ErpTestHelper.POS_NAME);
        
        when(posRepository.findByPartnerId(PARTNER_ID)).thenReturn(Arrays.asList(currentPos));
        posService.importCashTerminalPosDb(ctPos, PARTNER_ID);
        verify(posRepository, never()).saveAll(argThat(List<Pos>::isEmpty));
    }

    @Test
    public void testImportCashTerminalPosDbNewPos() throws CTPosDTOIncompleteDataException {
        CashTerminalPosDTO ctPos = erpTestHelper.createCashterminalPosDTO(ErpTestHelper.POS_TERMINAL_KEY, ErpTestHelper.POS_TERMINAL_ADDRESS);
        HashMap<String, CashTerminalPos> cashTerminalPosHashMap = ctPos.getTerminals().get(0);
        Pos currentPos = erpTestHelper.createPos();
        PosDTO posDto = new PosDTO();
        String code = cashTerminalPosHashMap.keySet().iterator().next();
        posDto.setCode(code);
        posDto.setPosIdInPartnersDb(code);
        posDto.setName(cashTerminalPosHashMap.get(code).getAddress());
        posDto.setPartnerId(PARTNER_ID);
        
        when(posRepository.findByPartnerId(PARTNER_ID)).thenReturn(Arrays.asList(currentPos));
        posService.importCashTerminalPosDb(ctPos, PARTNER_ID);
        
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(posRepository, times(1)).saveAll(argument.capture());
        assertEquals(1, argument.getValue().size());
    }
    
    @Test
    public void testImportCashTerminalPosDbUpdatePos() throws CTPosDTOIncompleteDataException {
        CashTerminalPosDTO ctPos = erpTestHelper.createCashterminalPosDTO(ErpTestHelper.CODE, ErpTestHelper.POS_TERMINAL_ADDRESS);
        HashMap<String, CashTerminalPos> cashTerminalPosHashMap = ctPos.getTerminals().get(0);
        Pos currentPos = erpTestHelper.createPos();
        PosDTO posDto = new PosDTO();
        String code = cashTerminalPosHashMap.keySet().iterator().next();
        posDto.setCode(code);
        posDto.setPosIdInPartnersDb(code);
        posDto.setName(cashTerminalPosHashMap.get(code).getAddress());
        posDto.setPartnerId(PARTNER_ID);
        
        when(posRepository.findByPartnerId(PARTNER_ID)).thenReturn(Arrays.asList(currentPos));
        posService.importCashTerminalPosDb(ctPos, PARTNER_ID);
        
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(posRepository, times(1)).saveAll(argument.capture());
        assertEquals(1, argument.getValue().size());
    }    

    @Test
    public void testGetPosForUser() throws ParseException {
        User user = erpTestHelper.createUser();
        List<String> posIds = new ArrayList<>();
        posIds.add(POS_ID);
        user.setPosIds(posIds);
        List<Pos> posList = new ArrayList<>();
        posList.add(pos);
        when(userService.getUserDetails(USERNAME)).thenReturn(user);
        when(posRepository.findByIdIn(user.getPosIds())).thenReturn(posList);
        assertEquals(posService.getPosForUser(USERNAME).size(), user.getPosIds().size());
    }

    @Test
    public void testGetPosForUserNoPosIds() throws ParseException {
        User user = erpTestHelper.createUser();
        user.setPosIds(new ArrayList<>());

        when(userService.getUserDetails(USERNAME)).thenReturn(user);
        when(posRepository.findByIdIn(user.getPosIds())).thenReturn(new ArrayList<>());
        assertTrue(posService.getPosForUser(USERNAME).isEmpty());
    }

    @Test(expected = CTPosDTOIncompleteDataException.class)
    public void testImportCashTerminalPosDbFail() throws CTPosDTOIncompleteDataException {
        CashTerminalPosDTO ctPos = erpTestHelper.createCashterminalPosDTO(ErpTestHelper.POS_TERMINAL_KEY, ErpTestHelper.POS_TERMINAL_ADDRESS);
        ctPos.setTerminals(null);
        when(posRepository.save(Mockito.any(PosDTO.class))).thenReturn(Mockito.mock(PosDTO.class));
        posService.importCashTerminalPosDb(ctPos, PARTNER_ID);
        verify(posRepository, never()).save(new PosDTO());
    }

    @Test(expected = CTPosDTOIncompleteDataException.class)
    public void testNullImportCashTerminalPosDb() throws CTPosDTOIncompleteDataException {
        posService.importCashTerminalPosDb(null, PARTNER_ID);
        verify(posRepository, never()).save(new PosDTO());
    }
}
