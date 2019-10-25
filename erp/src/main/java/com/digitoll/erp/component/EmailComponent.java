package com.digitoll.erp.component;

import com.digitoll.commons.exception.SaleIncompleteDataException;
import com.digitoll.commons.exception.SaleRowIncompleteDataException;
import com.digitoll.commons.kapsch.classes.EVignetteInventoryProduct;
import com.digitoll.commons.response.SaleDTO;
import com.digitoll.commons.response.SaleRowDTO;
import com.digitoll.erp.repository.KapschProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Component
public class EmailComponent {

	@Value("${digitoll.rest.mailer.host}")
	private String mailHost;

	@Value("${digitoll.rest.mailer.port}")
	private String mailPort;

	@Value("${digitoll.rest.mailer.user}")
	private String mailUser;

	@Value("${digitoll.rest.mailer.password}")
	private String mailPassword;

	@Value("${digitoll.rest.mailer.subject}")
	private String mailSubject;

	@Value("${digitoll.rest.mailer.sender}")
	private String mailSender;

	@Value("#{'${digitoll.partner.emails.bcc}'.split(',')}")
	private List<String> bccEmails;

	@Value("${digitoll.mailer.reports.subject.prefix}")
	private String reportMailSubjectPrefix;

	@Autowired
	private KapschProductRepository kapschProductRepository;

	@Autowired
	private TranslationComponent translationComponent;
	
	@Autowired
	private PdfComponent pdfComponent;

	private void sendEmailMessage(String recipient, String bodyText, File generatedPDF, String saleSequenceNumber)
			throws MessagingException, IOException {

		Session session = getSession();
		BodyPart messageBody = new MimeBodyPart();
		Multipart mailContet = new MimeMultipart();
		messageBody.setText(bodyText);
		messageBody.setHeader("Content-Type", "text/plain; charset=UTF-8");
		mailContet.addBodyPart(messageBody);
		messageBody = new MimeBodyPart();
		
		DataSource pdfFile = new FileDataSource(generatedPDF);
		messageBody.setDataHandler(new DataHandler(pdfFile));
		messageBody.setFileName("eVignette-Receipt-" + saleSequenceNumber);
		messageBody.setHeader("content-disposition", "inline;filename=" + pdfFile.getName());
		mailContet.addBodyPart(messageBody);
		MimeMessage message = new MimeMessage(session);

		message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		message.setSubject(mailSubject, "UTF-8");
		message.setFrom(new InternetAddress(mailSender));
		message.setContent(mailContet);

		Transport transport = session.getTransport("smtp");
		transport.connect(mailHost, mailUser, mailPassword);
		transport.sendMessage(message, message.getAllRecipients());

		pdfComponent.deleteFile(generatedPDF);

		transport.close();
	}

	private String addEmailText(SaleRowDTO saleRowDTO, String language) throws SaleRowIncompleteDataException {
		String emailText = "";
		
		language = (language == null) ? "bg" : language;
		// TODO country code to country name
		EVignetteInventoryProduct product = kapschProductRepository.findOneById(saleRowDTO.getKapschProductId());
		try {
			emailText = translationComponent.translateEmailMessage(product, saleRowDTO, language);
		} catch (NullPointerException e) {
			throw new SaleRowIncompleteDataException(saleRowDTO.toString());
		}
		return emailText;
	}

	public void sendEmail(SaleRowDTO saleRowDTO, File pdfAttachment)
			throws MessagingException, SaleRowIncompleteDataException, IOException {
		String emailAddress = saleRowDTO.getEmail();
		String emailText = addEmailText(saleRowDTO, null);
		String saleSequenceNumber = String.valueOf(saleRowDTO.getSaleSequence());
		sendEmailMessage(emailAddress, emailText, pdfAttachment, saleSequenceNumber);
	}

	public void sendEmail(SaleDTO saleDTO, File pdfAttachment)
			throws MessagingException, SaleIncompleteDataException, SaleRowIncompleteDataException, IOException {
		if (saleDTO.getSaleRows() == null || saleDTO.getSaleRows().isEmpty()) {
			throw new SaleIncompleteDataException(saleDTO.toString());
		}
		StringBuilder emailText = new StringBuilder();
		String emailAddress = saleDTO.getSaleRows().get(0).getEmail();
		String saleSequenceNumber = String.valueOf(saleDTO.getSaleSeq());

		if(!StringUtils.isEmpty(emailAddress)) {

			for (SaleRowDTO saleRowDTO : saleDTO.getSaleRows()) {
				emailAddress = saleRowDTO.getEmail();
				emailText.append(addEmailText(saleRowDTO, saleDTO.getLanguage()));
			}

			sendEmailMessage(emailAddress, emailText.toString(), pdfAttachment, saleSequenceNumber);
		}
	}

	private Session getSession() {
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.port", mailPort);
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		return Session.getDefaultInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailUser, mailPassword);
			}
		});
	}

	public void sendEmailToPartners(List<String> files, List<String> fileNames, List<String> emails, Date forDate)
			throws MessagingException {
		Session session = getSession();

		MimeMessage msg = new MimeMessage(session);
		MimeBodyPart attachFilePart = new MimeBodyPart();
		for (String email : emails) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		}

		for (String email : bccEmails) {
			msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
		}

		SimpleDateFormat subjectDateFormat = new SimpleDateFormat("dd.MM.yyyy");

		msg.setSubject(reportMailSubjectPrefix + " " + subjectDateFormat.format(forDate), "UTF-8");
		msg.setFrom(new InternetAddress(mailSender));

		Multipart multipart = new MimeMultipart();
		Integer index = 0;
		for (String file : files) {
			String fileName = (fileNames != null && fileNames.size() > index ? fileNames.get(index) : file);
			DataSource source = new FileDataSource(file);
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileName);
			multipart.addBodyPart(messageBodyPart);
			index++;
		}
		msg.setContent(multipart);

		Transport transport = session.getTransport("smtp");
		transport.connect(mailHost, mailUser, mailPassword);
		transport.sendMessage(msg, msg.getAllRecipients());
		transport.close();
	}
}
