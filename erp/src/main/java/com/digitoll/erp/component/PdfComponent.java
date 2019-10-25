package com.digitoll.erp.component;

import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.model.Sale;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.repository.KapschProductRepository;
import com.digitoll.erp.repository.SaleRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class PdfComponent {
    private static final int FONT_SIZE_BIG = 12;
    private static final int FONT_SIZE_REGULAR = 10;
    private static final int FONT_SIZE_SMALL = 8;
    private static final int FONT_SIZE_MICRO = 6;
    private static final int OFFSET = 30;
    private static final int OFFSET_IMAGE_TOP = 65;
    private static final int NO_OFFSET = 0;
    private static final int OFFSET_TITLE_TEXT_TOP = 340;
    private static final float OFFSET_TEXT_LEFT = 45f;
    private static final float LEADING = 14.5f;
    private static final String SUFFIX_PDF = ".pdf";
    private static final String FILE_NAME = "eVignette-Receipt-";
    private static final int NEW_LINE_REDUCED_HEIGHT_NORMAL = -10;
    private static final float UNITS_OF_TEXT_SPACE = 1000f;
    private static final int NEW_LINE_REDUCED_HEIGHT_MEDIUM = -5;
    private static final int NEW_LINE_REDUCED_HEIGHT_SMALL = -2;
    private static final int IMAGE_WIDTH = 100;
    private static final int IMAGE_HEIGHT = 29;
    private static final String CLASSPATH_DIGITOLL_LOGO = "/assets/images/digitoll_logo.png";
    private static final String CLASSPATH_BOLD_FONT = "/assets/fonts/Roboto-Bold.ttf";
    private static final String CLASSPATH_LIGHT_FONT = "/assets/fonts/Roboto-Light.ttf";

    @Value("${sale.id.mask}")
    private String saleIdMask;

    @Autowired
    private KapschProductRepository kapschProductRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private TranslationComponent translationComponent;

    private PDFont boldFont;
    private PDFont lightFont;
    private Sale sale;

    public File generatePdfForSaleRow(String language, SaleRowDTO... saleRowDTOs) throws SaleRowIncompleteDataException, IOException {
        try (PDDocument doc = new PDDocument()) {
            String saleId = "";
            boldFont = PDType0Font.load(doc,  getClass().getResourceAsStream(CLASSPATH_BOLD_FONT));
            lightFont = PDType0Font.load(doc,  getClass().getResourceAsStream(CLASSPATH_LIGHT_FONT));
            for (SaleRowDTO saleRowDTO : saleRowDTOs) {
                saleId = saleRowDTO.getSaleIdWithMask(saleIdMask);
                sale = saleRepository.findOneById(saleRowDTO.getSaleId());
                ProductsResponse product = translationComponent.translateProduct(kapschProductRepository.findOneById(saleRowDTO.getKapschProductId()), language);
                PDPage page = new PDPage(PDRectangle.A4);
                addPage(saleRowDTO, language, product, doc, page);
            }

            File pdf = File.createTempFile(FILE_NAME + saleId, SUFFIX_PDF);
            doc.save(pdf);
            doc.close();
            return pdf;
        }
    }

    private void addPage(SaleRowDTO saleRowDTO, String language, ProductsResponse product, PDDocument doc, PDPage page) throws IOException, SaleRowIncompleteDataException {
        doc.addPage(page);

        try (PDPageContentStream cont = new PDPageContentStream(doc, page)) {
            addCenteredImage(cont, getFileFromClasspath(CLASSPATH_DIGITOLL_LOGO, "image", "png"), doc, page);
            drawContentBorder(cont, page);
            drawContent(saleRowDTO, language, product, page, cont);
        }
    }

    private File getFileFromClasspath(String classpath, String filename, String suffix) throws IOException {
        Resource resource = new ClassPathResource(classpath);
        //PDFBox is not happy with resource.getFile() and resource.getURI()
        InputStream input = resource.getInputStream();
        byte[] buffer = new byte[input.available()];
        input.read(buffer);

        File image = File.createTempFile(filename, suffix);
        OutputStream outStream = new FileOutputStream(image);
        outStream.write(buffer);
        return image;
    }

    private void drawContent(SaleRowDTO saleRowDTO, String language, ProductsResponse product, PDPage page, PDPageContentStream cont) throws IOException, SaleRowIncompleteDataException {
        try {
            cont.beginText();
            drawHeader(language, page, cont);
            drawReceiptInfo(saleRowDTO, language, product, page, cont);
            drawFooter(language, page, cont);
            cont.endText();
        } catch (NullPointerException e) {
            throw new SaleRowIncompleteDataException("Null element in saleRow: " + saleRowDTO.toString());
        }
    }

    private void drawFooter(String language, PDPage page, PDPageContentStream cont) throws IOException {
        addCenteredText(page, lightFont, FONT_SIZE_SMALL, cont, translationComponent.getTranslatedPdfLabel("trusteeLabel", language), 0);
        cont.newLineAtOffset(NO_OFFSET, NEW_LINE_REDUCED_HEIGHT_NORMAL);
        addCenteredText(page, lightFont, FONT_SIZE_SMALL, cont, translationComponent.getTranslatedPdfLabel("keepSaveLabel", language), 1);
        addCenteredText(page, lightFont, FONT_SIZE_MICRO, cont, translationComponent.getTranslatedPdfLabel("noteFirstRow", language), 0);
        cont.newLineAtOffset(NO_OFFSET, NEW_LINE_REDUCED_HEIGHT_MEDIUM);
        addCenteredText(page, lightFont, FONT_SIZE_MICRO, cont, translationComponent.getTranslatedPdfLabel("noteSecondRow", language), 0);
    }

    private void drawReceiptInfo(SaleRowDTO saleRowDTO, String language, ProductsResponse product, PDPage page, PDPageContentStream cont) throws IOException, NullPointerException {
        addRow(translationComponent.getTranslatedPdfLabel("clientNameLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, sale.getCompanyIdNumber() != null ? sale.getCompanyName() : sale.getNames());
        addRow(translationComponent.getTranslatedPdfLabel("clientEikLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, sale.getCompanyIdNumber());
        addRow(translationComponent.getTranslatedPdfLabel("lpnLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, saleRowDTO.getKapschProperties().getVehicle().getLpn());
        addRow(translationComponent.getTranslatedPdfLabel("countryLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, saleRowDTO.getKapschProperties().getVehicle().getCountryCode());
        addVehicleTypeRow(language, product, page, cont, lightFont);
        addRow(translationComponent.getTranslatedPdfLabel("categoryLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, product.getCategoryDescriptionText() + " *");
        addRow(translationComponent.getTranslatedPdfLabel("ecoCategoryLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, product.getEmissionClassText());
        addRow(translationComponent.getTranslatedPdfLabel("durationLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, product.getValidityTypeText());
        addRow(translationComponent.getTranslatedPdfLabel("validityFromLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, getFormattedDate(saleRowDTO.getValidityStartDate(), 0));
        addRow(translationComponent.getTranslatedPdfLabel("validityToLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, getFormattedDate(saleRowDTO.getValidityEndDate(), 1));
        addRow(translationComponent.getTranslatedPdfLabel("createdOnLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, getFormattedDate(saleRowDTO.getKapschProperties().getPurchase().getPurchaseDateTimeUTC(), 0));
        addRow(translationComponent.getTranslatedPdfLabel("docNumberLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, saleRowDTO.getSaleIdWithMask(saleIdMask));
        addRow(translationComponent.getTranslatedPdfLabel("idNumberLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, null);
        addRow(translationComponent.getTranslatedPdfLabel("ofEVignetteLabel", language), lightFont, FONT_SIZE_REGULAR, cont, page, saleRowDTO.getVignetteId());
        cont.newLineAtOffset(NO_OFFSET, product.getVehicleType() == VehicleType.hgvn2 ? NEW_LINE_REDUCED_HEIGHT_SMALL : NEW_LINE_REDUCED_HEIGHT_MEDIUM);
        addRow(translationComponent.getTranslatedPdfLabel("totalLabel", language), boldFont, FONT_SIZE_REGULAR, cont, page, String.format("%s %s", saleRowDTO.getPrice().getAmount(), saleRowDTO.getPrice().getCurrency()));
        cont.newLineAtOffset(NO_OFFSET, product.getVehicleType() == VehicleType.hgvn2 ? NEW_LINE_REDUCED_HEIGHT_SMALL : NEW_LINE_REDUCED_HEIGHT_MEDIUM);
    }

    private void drawHeader(String language, PDPage page, PDPageContentStream cont) throws IOException {
        cont.setLeading(LEADING);
        cont.newLineAtOffset(OFFSET_TEXT_LEFT, page.getMediaBox().getHeight() / 2 + OFFSET_TITLE_TEXT_TOP);
        addCenteredText(page, lightFont, FONT_SIZE_REGULAR, cont, translationComponent.getTranslatedPdfLabel("companyTitleLabel", language), 1);
        addCenteredText(page, lightFont, FONT_SIZE_REGULAR, cont, translationComponent.getTranslatedPdfLabel("eikAndVatLabel", language), 2);
        addCenteredText(page, boldFont, FONT_SIZE_BIG, cont, translationComponent.getTranslatedPdfLabel("titleLabel", language), 1);
        cont.newLineAtOffset(NO_OFFSET, NEW_LINE_REDUCED_HEIGHT_NORMAL);
    }

    private void addVehicleTypeRow(String language, ProductsResponse product, PDPage page, PDPageContentStream cont, PDFont font) throws IOException {
        if (product.getVehicleType() == VehicleType.hgvn2) {
            addRow(translationComponent.getTranslatedPdfLabel("typeLabel", language), font, FONT_SIZE_REGULAR, cont, page,
                    translationComponent.getTranslatedPdfLabel("category2FirstRow", language));
            addRow(null, font, FONT_SIZE_REGULAR, cont, page,
                    translationComponent.getTranslatedPdfLabel("category2SecondRow", language));
        } else {
            addRow(translationComponent.getTranslatedPdfLabel("typeLabel", language), font, FONT_SIZE_REGULAR, cont, page, product.getVehicleTypeText());
        }
    }

    private static String getFormattedDate(Date date, int minusSeconds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        ZoneId sofiaZoneId = ZoneId.of("Europe/Sofia");
        LocalDateTime dateTime;
        dateTime = date
                .toInstant()
                .atZone(sofiaZoneId)
                .toLocalDateTime();
        //A workaround for the inconsistent date in UTC provided by Kapsch.
        dateTime = dateTime.minusSeconds(minusSeconds);
        return dateTime.format(formatter);
    }

    private static void addRow(String title, PDFont font, int fontSize, PDPageContentStream cont, PDPage page, String text) throws IOException, NullPointerException {
        cont.setFont(font, fontSize);
        if (title != null && !title.isEmpty() || text != null && !text.isEmpty()) {
            if (title != null) {
                cont.showText(title);
            }
            if (text != null) {
                cont.newLineAtOffset(page.getMediaBox().getWidth() / 2 - OFFSET_TEXT_LEFT * 2 - getTextSizeInPixels(text, font, fontSize), NO_OFFSET);
                cont.showText(text);
                cont.newLine();
                cont.newLineAtOffset(-page.getMediaBox().getWidth() / 2 + getTextSizeInPixels(text, font, fontSize) + OFFSET_TEXT_LEFT * 2, NO_OFFSET);
            } else {
                cont.newLine();
            }
        }
    }

    private static void addCenteredText(PDPage page, PDFont font, int fontSize, PDPageContentStream cont, String text, int numberOfNewLines) throws IOException {
        cont.setFont(font, fontSize);
        cont.newLineAtOffset((page.getMediaBox().getWidth() / 2 - getTextSizeInPixels(text, font, fontSize)) / 2 - OFFSET_TEXT_LEFT, NO_OFFSET);
        cont.showText(text);
        for (int i = 0; i < numberOfNewLines; i++) {
            cont.newLine();
        }
        cont.newLineAtOffset(-(page.getMediaBox().getWidth() / 2 - getTextSizeInPixels(text, font, fontSize)) / 2 + OFFSET_TEXT_LEFT, NO_OFFSET);
    }

    private static void drawContentBorder(PDPageContentStream cont, PDPage page) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        cont.setStrokingColor(Color.BLACK);
        cont.addRect(OFFSET, mediaBox.getHeight() / 2, mediaBox.getWidth() / 2 - OFFSET * 2, mediaBox.getHeight() / 2 - OFFSET);
        cont.closeAndStroke();
    }

    private static void addCenteredImage(PDPageContentStream cont, File imgFile, PDDocument doc, PDPage page) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        PDImageXObject pdImage = PDImageXObject.createFromFileByContent(imgFile, doc);
        cont.drawImage(pdImage, (page.getMediaBox().getWidth() / 2 - IMAGE_WIDTH) / 2, mediaBox.getHeight() - OFFSET_IMAGE_TOP, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private static float getTextSizeInPixels(String text, PDFont font, float fontSize) throws IOException {
        return (font.getStringWidth(text) / UNITS_OF_TEXT_SPACE) * fontSize;
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }
}
