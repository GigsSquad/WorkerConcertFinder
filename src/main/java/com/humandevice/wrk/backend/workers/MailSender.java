package com.humandevice.wrk.backend.workers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.humandevice.wrk.backend.enums.MailStatus;
import com.humandevice.wrk.backend.pojos.MailPojo;

public class MailSender extends Worker {
	/* w queue zapisane sa dane do maila */
	private ArrayList<MailPojo> queue = new ArrayList<MailPojo>();
	Logger logger = Logger.getLogger(MailSender.class);

	/* zeby nic zlego nie wdarlo sie do programu */
	PreparedStatement pstm = null, pstm2 = null;
	ResultSet rs = null, rs2 = null;

	private String template_pre, template_post;
	private int howMany;
	private int id;

	/**
	 * na poczatku zgrywane sa poprzednie wartosci z tabeli
	 */
	public void init() {
		template_pre = configuration.getParameter("mail_sender_template_before");
		template_post = configuration.getParameter("mail_sender_template_after");
		howMany = configuration.getIntParameter("mail_sender_limit");
	}

	/**
	 * jesli kolejka nie jest pusta, zgrywa dane potrzebne do maila nastepnie w
	 * petli for pisany jest mail wedlug wzoru, uzupelniany tymi danymi
	 */
	@Override
	public void process() {

		String get = "SELECT * FROM mass_queue LEFT JOIN mass_mail ON mass_queue.mail_id = mass_mail.id"
				+ " WHERE status = ? AND mass_status = 1 LIMIT " + howMany;

		try {

			pstm = connection.prepareStatement(get);
			pstm.setString(1, MailStatus.PENDING.toString());
			rs = pstm.executeQuery();

			if (rs.isBeforeFirst()) {

				logger.info("Trying to get some mails");
				
				while (rs.next()) {
					MailPojo mail = new MailPojo();
					mail.setId(rs.getLong("id"));
					mail.setContentHTML(rs.getString("message_html"));
					mail.setContentPlain(rs.getString("message_plain"));
					mail.setSubject(rs.getString("title"));
					mail.setEmail(rs.getString("email"));
					mail.setMail_id(rs.getInt("mail_id"));
					queue.add(mail);
				}

				rs.close();
				pstm.close();

			} else {

				logger.info("Nie było maili do wysyłki");
				String update = "UPDATE mass_mail SET mass_status = 2 WHERE id = ?";

				try {
					pstm = connection.prepareStatement(update);
					pstm.setInt(1, id);
					pstm.executeUpdate();
					pstm.close();
				} catch (SQLException e) {
					logger.info("Błąd sql");
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			logger.info("Błąd sql");
			e.printStackTrace();
		}
		if (!queue.isEmpty()) {
			final String user = configuration.getParameter("mail_sender_user");
			final String password = configuration.getParameter("mail_sender_password");
			String host = configuration.getParameter("mail_sender_host");
			String senderMail = configuration.getParameter("mail_sender_mail");
			String senderName = configuration.getParameter("mail_sender_name");
			String senderPort = configuration.getParameter("mail_sender_port");
			boolean doTLS = "1".equals(configuration.getParameter("mail_sender_tls"));
			boolean doAuth = "1".equals(configuration.getParameter("mail_sender_auth"));

			Properties properties = System.getProperties();

			properties.setProperty("mail.smtp.host", host);
			properties.put("mail.smtp.auth", ((doAuth) ? "true" : "false"));
			if (doTLS) {
				properties.put("mail.smtp.starttls.enable", "true");
			} else {
				properties.setProperty("mail.smtp.ssl.trust", "smtpserver");
			}
			properties.put("mail.smtp.port", senderPort);

			Session session = Session.getInstance(properties, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			});
			/* informacja w logu, ze połączenie zostało zautentyfikowane */
			logger.info("Auth accepted");

			for (MailPojo mail : queue) {
				try {

					MimeMessage message = new MimeMessage(session);

					message.setHeader("Content-Encoding", "utf-8");
					message.setFrom(new InternetAddress(senderMail, senderName));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getEmail()));
					message.addRecipient(RecipientType.BCC,
							new InternetAddress(configuration.getParameter("admin_email_address")));
					message.setSubject(mail.getSubject(), "utf-8");

					// Mix

					MimeMultipart contentPart = new MimeMultipart("related");

					// Message body html + plain

					Multipart partMessage = new MimeMultipart("alternative");

					// Plain content email
					MimeBodyPart plainMessage = new MimeBodyPart();
					plainMessage.setContent(mail.getContentPlain(), "text/plain; charset=utf-8");

					// HTML content email

					MimeBodyPart htmlMessage = new MimeBodyPart();
					htmlMessage.setContent(template_pre + mail.getContentHTML() + template_post,
							"text/html; charset=utf-8");

					// Add html + plain

					partMessage.addBodyPart(plainMessage);
					partMessage.addBodyPart(htmlMessage);

					// Wrapper for bodyTxt and bodyHtml

					MimeBodyPart bodycontent = new MimeBodyPart();
					bodycontent.setContent(partMessage);

					contentPart.addBodyPart(bodycontent);

					MimeBodyPart imagePart = new MimeBodyPart();
					// wstawia obrazek (logo)
					try {
						imagePart.attachFile(configuration.getParameter("mail_logo"));
						imagePart.setContentID("<wrk_logo>");
						imagePart.setDisposition(MimeBodyPart.INLINE);
						contentPart.addBodyPart(imagePart);
					} catch (IOException e) {
						logger.error("Could not find the image");
					}

					message.setContent(contentPart);

					logger.info("Sending email to " + mail.getEmail());
					Transport.send(message);

					// Changes queue status to 'sent'
					pstm = connection.prepareStatement("UPDATE mass_queue SET status = ? WHERE id = ?");
					pstm.setString(1, MailStatus.SENT.toString());
					pstm.setLong(2, mail.getId());
					pstm.executeUpdate();
					pstm.close();

					logger.info("Mail sent");

				} catch (MessagingException mex) {
					mex.printStackTrace();
				} catch (UnsupportedEncodingException ue) {
					ue.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						pstm.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// po wyslaniu czysci kolejke
			queue.clear();

		}
	}

	/**
	 * sprawdza czy kolejka wysylania jest pelna, jesli nie, to przepisuje dane
	 * z bazy (tabela mail_queue), ktore beda uzyte przy piasaniu maila, dla
	 * statusów pending
	 */
	@Override
	public boolean checkConditions() {

		if (queue.isEmpty()) {

			String check = "SELECT id FROM mass_mail WHERE mass_status = 1 LIMIT 1";
			try {
				pstm = connection.prepareStatement(check);
				rs = pstm.executeQuery();
				workerActivityLogEntry("sender");

				if (rs.next()) {
					logger.info("Maile do wysłania: " + rs.getInt("id"));
					id = rs.getInt("id");
					return true;
				} else {
					logger.info("Nie ma maili do wysłania");
					return false;
				}
			} catch (SQLException e) {
				logger.info("Błąd sql");
				e.printStackTrace();
			}
		}
		return !queue.isEmpty();
	}

	@Override
	public String toString() {
		return MailSender.class.getSimpleName();
	}

}
