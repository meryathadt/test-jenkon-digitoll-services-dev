package com.digitoll.erp.aspect;

import com.digitoll.commons.dto.TransactionIdDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.dto.VignetteIdDTO;
import com.digitoll.commons.enumeration.UserRole;
import com.digitoll.commons.exception.SaleNotFoundException;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.Sale;
import com.digitoll.commons.request.SaleRequest;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.repository.SaleRepository;
import com.digitoll.erp.service.UserService;
import com.digitoll.erp.utils.ErpTestHelper;
import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.digitoll.erp.utils.ErpTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ContextConfiguration(classes = SaleControllerRestrictions.class)
@RunWith(SpringRunner.class)
public class SaleControllerRestrictionsTest {
    @MockBean
    private UserService userService;

    @MockBean
    private PosRepository posRepository;

    @MockBean
    private SaleRepository saleRepository;

    @Autowired
    private SaleControllerRestrictions saleControllerRestrictions;
    private ErpTestHelper erpTestHelper;
    private Pos pos;
    private JoinPoint joinPoint;
    private UserDetailsDTO user;
    private Principal principal;

    @Before
    public void init() throws ParseException {
        erpTestHelper = new ErpTestHelper();
        pos = erpTestHelper.createPos();
        joinPoint = Mockito.mock(JoinPoint.class);
        user = new UserDetailsDTO();
        BasicUtils.copyPropsSkip(erpTestHelper.createUser(), user, Collections.singletonList("password"));
        principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);
    }

    @Test
    public void testBeforeRegisterPartnerAdmin() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(POS_ID);
        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId())).thenReturn(Optional.ofNullable(pos));

        saleControllerRestrictions.beforeRegister(joinPoint);
        verify(posRepository).findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId());
    }

    @Test
    public void testBeforeRegisterPartnerAdminFail() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId())).thenReturn(Optional.empty());

        try {
            saleControllerRestrictions.beforeRegister(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeRegisterAdmin() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        saleControllerRestrictions.beforeRegister(joinPoint);
        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId());
    }


    @Test
    public void testBeforeRegisterCashTerminalUser() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.NO_POS_USER.getRoleCode())));

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();
        saleRequest.setPosId("");

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        saleControllerRestrictions.beforeRegister(joinPoint);
        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId());
    }

    @Test
    public void testBeforeRegisterNonCashTerminalUser() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        saleControllerRestrictions.beforeRegister(joinPoint);
        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), saleRequest.getPosId());

    }


    @Test
    public void testBeforeRegisterUserEmptyUserPos() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        user.setPosIds(new ArrayList<>());

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        try {
            saleControllerRestrictions.beforeRegister(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeRegisterUserEmptySalePos() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);

        SaleRequest saleRequest = erpTestHelper.createSaleRequest();
        saleRequest.setPosId(POS_ID);

        when(joinPoint.getArgs()).thenReturn(new Object[]{saleRequest, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        try {
            saleControllerRestrictions.beforeRegister(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeActivateWithSaleIdPartnerAdmin() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        setPosIdToUser(pos.getId());
        Sale sale = erpTestHelper.createSale();

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), POS_ID)).thenReturn(Optional.of(pos));

        saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);

        verify(posRepository).findPosByPartnerIdAndId(user.getPartnerId(), POS_ID);
        verify(saleRepository).findOneById(SALE_ID);
    }

    @Test
    public void testBeforeActivateWithSaleIdPartnerAdminFail() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));

        Sale sale = erpTestHelper.createSale();

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), POS_ID)).thenReturn(Optional.empty());

        try {
            saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneById(SALE_ID);
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeActivateWithSaleIdAdmin() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));

        Sale sale = erpTestHelper.createSale();

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), POS_ID)).thenReturn(Optional.of(pos));

        saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), POS_ID);
        verify(saleRepository).findOneById(SALE_ID);
    }

    @Test
    public void testBeforeActivateWithSaleIdUser() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));

        Sale sale = erpTestHelper.createSale();

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);

        saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), POS_ID);
        verify(saleRepository).findOneById(SALE_ID);
    }

    @Test
    public void testBeforeActivateWithSaleIdUserNoPosId() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        sale.setPosId("");

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneById(SALE_ID);
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }


    @Test
    public void testBeforeActivateWithSaleIdUserNoUserPosId() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        user.setPosIds(null);

        Sale sale = erpTestHelper.createSale();

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneById(SALE_ID);
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeActivateWithSaleIdUserUnauthorized() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);

        Sale sale = erpTestHelper.createSale();
        sale.setPosId(POS_ID);

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneById(SALE_ID);
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test(expected = SaleNotFoundException.class)
    public void testBeforeActivateWithSaleIdNotFound() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);

        when(joinPoint.getArgs()).thenReturn(new Object[]{SALE_ID, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneById(eq(SALE_ID))).thenReturn(null);

        saleControllerRestrictions.beforeActivateWithSaleId(joinPoint);

        verify(saleRepository).findOneById(SALE_ID);
    }


    @Test
    public void testBeforeActivateWithTransactionIdPartnerAdmin() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId())).thenReturn(Optional.of(pos));

        saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);

        verify(posRepository).findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId());
        verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
    }

    @Test
    public void testBeforeActivateWithTransactionIdPartnerAdminFail() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId())).thenReturn(Optional.empty());

        try {
            saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeActivateWithTransactionIdAdmin() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId())).thenReturn(Optional.of(pos));

        saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId());
        verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
    }

    @Test
    public void testBeforeActivateWithTransactionIdUser() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);

        saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), sale.getPosId());
        verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
    }

    @Test
    public void testBeforeActivateWithTransactionIdUserNoPosId() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));

        Sale sale = erpTestHelper.createSale();
        sale.setPosId(null);
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeActivateWithTransactionIdUserNoUserPosId() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        user.setPosIds(null);

        Sale sale = erpTestHelper.createSale();
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeActivateWithTransactionIdUnauthorized() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);

        Sale sale = erpTestHelper.createSale();
        sale.setPosId(POS_ID);
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();

        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(sale);

        try {
            saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);
        } catch (HttpClientErrorException ex) {
            verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test(expected = SaleNotFoundException.class)
    public void testBeforeActivateWithTransactionIdNotFound() throws ParseException, SaleNotFoundException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);
        TransactionIdDTO transactionIdDTO = erpTestHelper.createTransactionIdDTO();
        when(joinPoint.getArgs()).thenReturn(new Object[]{transactionIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(saleRepository.findOneByBankTransactionId(transactionIdDTO.getTransactionId())).thenReturn(null);

        saleControllerRestrictions.beforeActivateWithTransactionId(joinPoint);

        verify(saleRepository).findOneByBankTransactionId(transactionIdDTO.getTransactionId());
    }

    private void setPosIdToUser(String posId) {
        List<String> posIds = new ArrayList<>();
        posIds.add(posId);
        user.setPosIds(posIds);
    }

    @Test
    public void testBeforeActivateWithVignetteIdPartnerAdmin() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();
        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), vignetteIdDTO.getPosId())).thenReturn(Optional.of(pos));

        saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);

        verify(posRepository).findPosByPartnerIdAndId(user.getPartnerId(), vignetteIdDTO.getPosId());
    }

    @Test
    public void testBeforeActivateWithVignetteIdPartnerAdminFail() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_ADMIN.getRoleCode())));
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();

        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);
        when(posRepository.findPosByPartnerIdAndId(user.getPartnerId(), vignetteIdDTO.getPosId())).thenReturn(Optional.empty());

        try {
            saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }

    @Test
    public void testBeforeActivateWithVignetteIdAdmin() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.ADMIN.getRoleCode())));
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();

        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), vignetteIdDTO.getPosId());
    }

    @Test
    public void testBeforeActivateWithVignetteIdUser() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();

        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);

        verify(posRepository, never()).findPosByPartnerIdAndId(user.getPartnerId(), vignetteIdDTO.getPosId());
    }

    @Test
    public void testBeforeActivateWithVignetteIdUserNoPosId() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();
        vignetteIdDTO.setPosId(null);

        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        try {
            saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeActivateWithVignetteIdUserNoUserPosId() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        user.setPosIds(null);
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();
        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        try {
            saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), BAD_REQUEST);
        }
    }

    @Test
    public void testBeforeActivateWithVignetteIdUserUnauthorized() throws ParseException {
        user.setRoles(Collections.singletonList(erpTestHelper.createRole(UserRole.PARTNER_EMPLOYEE.getRoleCode())));
        setPosIdToUser(POS_ID_2);
        VignetteIdDTO vignetteIdDTO = erpTestHelper.createVignetteIdDto();
        vignetteIdDTO.setPosId(POS_ID);
        when(joinPoint.getArgs()).thenReturn(new Object[]{vignetteIdDTO, principal});
        when(userService.getUserDetailsDto(anyString())).thenReturn(user);

        try {
            saleControllerRestrictions.beforeActivateWithVignetteId(joinPoint);
        } catch (HttpClientErrorException ex) {
            assertEquals(ex.getStatusCode(), UNAUTHORIZED);
        }
    }
}
