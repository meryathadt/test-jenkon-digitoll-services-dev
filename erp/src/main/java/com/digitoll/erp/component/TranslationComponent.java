package com.digitoll.erp.component;

import com.digitoll.commons.kapsch.classes.EVignetteProduct;
import com.digitoll.commons.response.ProductsResponse;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.commons.util.BasicUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class TranslationComponent {

    private static ResourceBundleMessageSource messageSource;
    
    private final String NEW_LINE = "\n";
    private final String LINE_SEPARATOR = ": ";
    
    private static final Logger log = LoggerFactory.getLogger(TranslationComponent.class);

    @Autowired
    TranslationComponent(ResourceBundleMessageSource messageSource) {
        TranslationComponent.messageSource = messageSource;
    }

    public ProductsResponse translateProduct(EVignetteProduct product, String language) {

        ProductsResponse result = new ProductsResponse();
        if (product == null) {
            return result;
        }
        String translation;
        Locale locale;
        
        if (language != null) {
            locale = Locale.lookup(Locale.LanguageRange.parse(language), Arrays.asList(
                 new Locale("en"),
                 new Locale("bg")));
        }
        else {
            locale = LocaleContextHolder.getLocale();
        }

        BasicUtils.copyNonNullProps(product, result);

        translation = messageSource.getMessage("vehicle.type." + product.getVehicleType().name(), null, locale);
        result.setVehicleTypeText(translation);

        translation = messageSource.getMessage("vignette.validity.type." + product.getValidityType().name(), null, locale);
        result.setValidityTypeText(translation);

        translation = messageSource.getMessage("vehicle.category." + product.getVehicleType().name(), null, locale);
        result.setCategoryDescriptionText(translation);

        if (product.getEmissionClass() != null) {
            translation = messageSource.getMessage("vehicle.emission.class." + product.getEmissionClass().name(), null, locale);
            result.setEmissionClassText(translation);
        }

        return result;
    }

    public String getTranslatedPdfLabel(String field, String language) {

        String translation;
        Locale locale;

        if (language != null) {
            locale = Locale.lookup(Locale.LanguageRange.parse(language), Arrays.asList(
                    new Locale("en"),
                    new Locale("bg")));
        }
        else {
            locale = LocaleContextHolder.getLocale();
        }

        translation = messageSource.getMessage("pdf."+field, null, locale);
        return translation;
    }

    public String translateEmailMessage(EVignetteProduct p, SaleRowDTO saleRowDTO, String language) {
        String emailText = "";
        String translation;
        Locale locale;
        
        String date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        ZoneId sofiaZoneId = ZoneId.of("Europe/Sofia");
        LocalDateTime dateTime;        
        
        if (language != null) {            
            locale = Locale.lookup(Locale.LanguageRange.parse(language), Arrays.asList(
                 new Locale("en"),
                 new Locale("bg")));
        }
        else {
            locale = LocaleContextHolder.getLocale();
        }
        
        //TODO country code to country name
        ProductsResponse product = this.translateProduct(p, language);
        
        translation = messageSource.getMessage("email.issuerLabel", null, locale);
        emailText += addLine(translation, messageSource.getMessage("email.sellerCompanyName", null, locale) + NEW_LINE);
        
        translation = messageSource.getMessage("email.title", null, locale);
        emailText += addTitleLine(translation);
        
        //TODO: add translation for companyName and EIK when they are available
        
        translation = messageSource.getMessage("email.lpn", null, locale);
        emailText += addLine(translation, saleRowDTO.getKapschProperties().getVehicle().getLpn());
        
        //TODO: get the translated country name
        translation = messageSource.getMessage("email.vehicleCountry", null, locale);
        emailText += addLine(translation, saleRowDTO.getKapschProperties().getVehicle().getCountryCode());
        
        translation = messageSource.getMessage("email.vehicleType", null, locale);
        emailText += addLine(translation, product.getVehicleTypeText());   
        
        translation = messageSource.getMessage("email.vehicleCategory", null, locale);
        emailText += addLine(translation, product.getCategoryDescriptionText());         
        
        if (product.getEmissionClassText() != null) {
            translation = messageSource.getMessage("email.vehicleEmissionClass", null, locale);
            emailText += addLine(translation, product.getEmissionClassText());          
        }
        
        translation = messageSource.getMessage("email.duration", null, locale);
        emailText += addLine(translation, product.getValidityTypeText()); 
        
        translation = messageSource.getMessage("email.validFrom", null, locale);
        dateTime = saleRowDTO.getValidityStartDate()
            .toInstant()
            .atZone(sofiaZoneId)
            .toLocalDateTime();          
        date = dateTime.format(formatter);
        emailText += addLine(translation, date);
        
        translation = messageSource.getMessage("email.validTo", null, locale);
        dateTime = saleRowDTO.getValidityEndDate()
            .toInstant()
            .atZone(sofiaZoneId)
            .toLocalDateTime();      
        //A workaround for the inconsistent date in UTC provided by Kapsch.
        dateTime = dateTime.minusSeconds(1);
        
        date = dateTime.format(formatter);
        emailText += addLine(translation, date);        
        
        translation = messageSource.getMessage("email.purchaseDate", null, locale);
        dateTime = saleRowDTO.getKapschProperties().getPurchase().getPurchaseDateTimeUTC()
            .toInstant()
            .atZone(sofiaZoneId)
            .toLocalDateTime();        
        date = dateTime.format(formatter);
        emailText += addLine(translation, date);
        
        translation = messageSource.getMessage("email.vignetteId", null, locale);
        emailText += addLine(translation, saleRowDTO.getKapschProperties().getId());
        
        translation = messageSource.getMessage("email.singlePrice", null, locale);
        emailText += addLine(translation, saleRowDTO.getPrice().getAmount() + " " + saleRowDTO.getPrice().getCurrency());         
        return emailText;        
    }
    
    private String addLine(String label, String text) {
        return new StringBuilder(label).append(LINE_SEPARATOR).append(text).append(NEW_LINE).toString();
    }
    
    private String addTitleLine(String text) {
        return new StringBuilder(text).append(NEW_LINE).append(NEW_LINE).toString();
    }

}
