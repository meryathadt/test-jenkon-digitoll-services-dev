package com.digitoll.erp.component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.exception.SaleIncompleteDataException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.kapsch.classes.VignettePurchase;
import com.digitoll.commons.kapsch.response.VignetteRegistrationResponseContent;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.model.VignettePrice;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.repository.KapschProductRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class EmailComponentTest {

	private static final String TEST_FILE_LOCATION = "/src/test/resources/test_Attachment.pdf";

	private static final String TEST_FILE_LOCATION_COPY = "/src/test/resources/test_Attachment_COPY.pdf";

	@Value("${test.mailer.host}")
	private String mailHost;

	@Value("${test.mailer.port}")
	private String mailPort;

	@Value("${test.mailer.user}")
	private String mailUser;

	@Value("${test.mailer.password}")
	private String mailPassword;

	@Value("${test.mailer.subject}")
	private String mailSubject;

	@Value("${test.mailer.sender}")
	private String mailSender;

	@Value("#{'${test.partner.emails.bcc}'.split(',')}")
	private List<String> bccEmails;

	@Value("${test.mailer.reports.subject.prefix}")
	private String reportMailSubjectPrefix;

	private File pdfAttachment;

	@Mock
	private TranslationComponent translationComponent;

	@Mock
	private KapschProductRepository kapschProductRepository;

	@InjectMocks
	@Autowired
	private EmailComponent emailComponent;
	private SaleRowDTO saleRowDTOMock;
	private SaleDTO sale;

	@Before
	public void init() {

		Path testFilePath = null;
		Path copiedFilePath = null;

		try {
			testFilePath = Paths.get(Paths.get("").toRealPath().toString() + TEST_FILE_LOCATION);
			copiedFilePath = Paths.get(Paths.get("").toRealPath().toString() + TEST_FILE_LOCATION_COPY);
			pdfAttachment = copiedFilePath.toFile();
			FileUtils.copyFile(testFilePath.toFile(), pdfAttachment);
		} catch (IOException e) {
			e.printStackTrace();
		}

		saleRowDTOMock = Mockito.mock(SaleRowDTO.class, Mockito.RETURNS_DEEP_STUBS);
		sale = Mockito.mock(SaleDTO.class);
		Mockito.when(sale.getCompanyName()).thenReturn("Test Co");
		Mockito.when(sale.getCompanyIdNumber()).thenReturn("QWERTY1234");
		Mockito.when(sale.getSaleRows()).thenReturn(Collections.singletonList(saleRowDTOMock));
		Mockito.when(kapschProductRepository.findOneById(Mockito.any()))
				.thenReturn(Mockito.mock(EVignetteInventoryProduct.class));
		Mockito.when(translationComponent.translateProduct(Mockito.any(), Mockito.anyString()))
				.thenReturn(Mockito.mock(ProductsResponse.class));
		mockSaleRowData();
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
		kapschProp.getVehicle().setType(VehicleType.car);
		kapschProp.setId("19082681536246");
		purchase.setPurchaseDateTimeUTC(new Date());
		kapschProp.setPurchase(purchase);
		Mockito.when(saleRowDTOMock.getVignetteId()).thenReturn("19082681536246");
		Mockito.when(saleRowDTOMock.getValidityEndDate()).thenReturn(new Date());
		Mockito.when(saleRowDTOMock.getEmail()).thenReturn("digitoll-test@hyperaspect.com");
		Mockito.when(saleRowDTOMock.getValidityStartDate()).thenReturn(new Date());
		Mockito.when(saleRowDTOMock.getKapschProperties()).thenReturn(Mockito.spy(kapschProp));
		Mockito.when(saleRowDTOMock.getPrice()).thenReturn(Mockito.spy(price));
	}

	@Test
	public void sendSaleRowEmail() throws MessagingException, SaleRowIncompleteDataException, IOException {

		emailComponent.sendEmail(saleRowDTOMock, pdfAttachment);
	}

	@Test(expected = SaleRowIncompleteDataException.class)
	public void sentIncompleteSaleRowEmail() throws SaleRowIncompleteDataException, MessagingException, IOException {
		emailComponent.sendEmail(new SaleRowDTO(), pdfAttachment);
	}

	@Test
	public void sendSaleEmail() throws MessagingException, SaleIncompleteDataException, SaleRowIncompleteDataException, IOException {
		emailComponent.sendEmail(sale, pdfAttachment);
	}

	@Test(expected = SaleIncompleteDataException.class)
	public void sendIncompleteSaleEmail()
			throws MessagingException, SaleIncompleteDataException, SaleRowIncompleteDataException, IOException {
		Mockito.when(sale.getSaleRows()).thenReturn(null);
		emailComponent.sendEmail(sale, pdfAttachment);
	}

}
