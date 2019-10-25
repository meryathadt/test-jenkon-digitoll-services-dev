package com.digitoll.erp.component;

import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.kapsch.classes.VignettePurchase;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.model.Sale;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.model.VignettePrice;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.component.PdfComponent;
import com.digitoll.erp.component.TranslationComponent;
import com.digitoll.erp.repository.KapschProductRepository;
import com.digitoll.erp.repository.SaleRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PdfComponentTest {

    @Value("${sale.id.mask}")
    private String saleIdMask;
    @Mock
    private KapschProductRepository kapschProductRepository;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private TranslationComponent translationComponent;
    @InjectMocks
    private PdfComponent pdfGenerator;
    private SaleRowDTO saleRowDTOMock;
    private Sale sale;
    private File pdf;
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Before
    public void init() {
        saleRowDTOMock = Mockito.mock(SaleRowDTO.class);
        sale = Mockito.mock(Sale.class);
        Mockito.when(sale.getCompanyName()).thenReturn("Test Co");
        Mockito.when(sale.getCompanyIdNumber()).thenReturn("QWERTY1234");
        mockSaleRowData();
        mockMethods();
    }

    private void mockMethods() {
        ProductsResponse productsResponse = new ProductsResponse();
        productsResponse.setVehicleType(VehicleType.car);
        productsResponse.setCategoryDescriptionText("Category 3");
        productsResponse.setValidityTypeText("Quarterly");
        Mockito.when(saleRepository.findOneById(saleRowDTOMock.getSaleId())).thenReturn(sale);
        Mockito.when(translationComponent.getTranslatedPdfLabel(Mockito.anyString(), ArgumentMatchers.eq(null))).thenReturn("text");
        Mockito.when(translationComponent.translateProduct(Mockito.any(), ArgumentMatchers.eq(null))).thenReturn(productsResponse);
    }

    private void mockSaleRowData() {
        VignetteRegistrationResponseContent kapschProp = new VignetteRegistrationResponseContent();
        VignettePurchase purchase = new VignettePurchase();
        VignettePrice price = new VignettePrice();
        price.setAmount(BigDecimal.valueOf(54));
        price.setCurrency(Currency.getInstance("BGN"));
        kapschProp.setVehicle(new Vehicle());
        kapschProp.getVehicle().setCountryCode("BG");
        kapschProp.getVehicle().setLpn("BP1234BP");
        purchase.setPurchaseDateTimeUTC(new Date());
        kapschProp.setPurchase(purchase);
        Mockito.when(saleRowDTOMock.getVignetteId()).thenReturn("19082681536246");
        Mockito.when(saleRowDTOMock.getValidityEndDate()).thenReturn(new Date());
        Mockito.when(saleRowDTOMock.getValidityStartDate()).thenReturn(new Date());
        Mockito.when(saleRowDTOMock.getKapschProperties()).thenReturn(Mockito.spy(kapschProp));
        Mockito.when(saleRowDTOMock.getPrice()).thenReturn(Mockito.spy(price));
        Mockito.when(saleRowDTOMock.getSaleIdWithMask(saleIdMask)).thenReturn("0000001234");
    }

    @Test
    public void testGeneratePdfWithCompany() throws IOException, SaleRowIncompleteDataException {
        pdf = pdfGenerator.generatePdfForSaleRow(null, saleRowDTOMock);

        assertTrue(pdf.exists());
        assertTrue(extractPdfText(pdf).contains(saleRowDTOMock.getVignetteId()));
        assertTrue(extractPdfText(pdf).contains(saleRowDTOMock.getKapschProperties().getVehicle().getLpn()));
        assertTrue(extractPdfText(pdf).contains(sale.getCompanyName()));
        assertTrue(extractPdfText(pdf).contains(sale.getCompanyIdNumber()));
    }

    @Test(expected = SaleRowIncompleteDataException.class)
    public void testEmptyContent() throws SaleRowIncompleteDataException, IOException {
        assertNull(pdfGenerator.generatePdfForSaleRow(null, Mockito.mock(SaleRowDTO.class)));

    }

    @Test
    public void testGeneratePdfWithNames() throws IOException, SaleRowIncompleteDataException {
        Mockito.when(sale.getNames()).thenReturn("Test Test");
        Mockito.when(sale.getCompanyIdNumber()).thenReturn(null);
        pdf = pdfGenerator.generatePdfForSaleRow(null, saleRowDTOMock);

        assertTrue(pdf.exists());
        assertTrue(extractPdfText(pdf).contains(saleRowDTOMock.getVignetteId()));
        assertTrue(extractPdfText(pdf).contains(saleRowDTOMock.getKapschProperties().getVehicle().getLpn()));
        assertTrue(extractPdfText(pdf).contains(sale.getNames()));
        assertNull(sale.getCompanyIdNumber());
    }

    private String extractPdfText(File pdf) throws IOException {
        PDDocument pdfDocument = PDDocument.load(pdf);
        return new PDFTextStripper().getText(pdfDocument);
    }

    @After
    public void deconstruct() {
        if (pdf != null && pdf.exists()) {
            pdf.delete();
        }
    }
}
