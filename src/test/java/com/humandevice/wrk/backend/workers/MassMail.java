package com.humandevice.wrk.backend.workers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.humandevice.wrk.backend.enums.MailStatus;
import com.humandevice.wrk.backend.pojos.MassMailPojo;

/**
 * @author michal Worker dodający maile do mass queue
 */
public class MassMail extends Worker {

	private ArrayList<MassMailPojo> queue = new ArrayList<MassMailPojo>();
	Logger logger = Logger.getLogger(MassMail.class);

	// czyszczenie Prepared Statement
	private PreparedStatement pstm = null, pstm2 = null;
	private ResultSet rs = null, rs2 = null;
	private MassMailPojo mail;
	private boolean flag = false;

	/**
	 * Pobiera maile, następnie pobiera e-maile użytkowników i wstawia kolejki
	 * przygotowane do wysyłki
	 */
	@Override
	public void process() {

		String getMails = "SELECT * FROM mass_mail WHERE mass_status = 0 LIMIT 1";
		String getUsers = null;
		if (flag == true){
			getUsers = "SELECT email FROM user WHERE is_active = 1 LIMIT 250";
			logger.info("Trwa przygotowanie bazy do wysyłki maili do wszystkich użytkowników");
		} else if (flag == false){
			getUsers = "SELECT email FROM user WHERE is_active = 1 AND id IN ";
			logger.info("Trwa przygotowanie bazy do wysyłki maili do wybranych użytkowników");
		}
		

		try {
			pstm = connection.prepareStatement(getMails);
			rs = pstm.executeQuery();
			int count = 0;

			while (rs.next()) {
				if (flag == false){
					getUsers += rs.getString("users_id").trim();
					pstm2 = connection.prepareStatement(getUsers);
				} else if (flag == true){
					pstm2 = connection.prepareStatement(getUsers);
				}
				rs2 = pstm2.executeQuery();
				

				while (rs2.next()) {
					mail = new MassMailPojo();
					mail.setId(rs.getInt("id"));
					mail.setTitle(rs.getString("title"));
					mail.setMessagePlain(rs.getString("message_plain"));
					mail.setMessageHTML(rs.getString("message_html"));
					mail.setEmail(rs2.getString("email"));
					mail.setUsersId(rs.getString("users_id"));
					queue.add(mail);
					count++;
				}

				rs2.close();
				pstm2.close();
			}

			rs.close();
			pstm.close();
			
			logger.info("Pobrano potrzebne dane");


			if (!queue.isEmpty()) {
				connection.setAutoCommit(false);
				String insert = "INSERT INTO mass_queue (email, mail_id, status) VALUES (?, ?, ?)";
				for (MassMailPojo mail : queue) {
					pstm = connection.prepareStatement(insert);
					pstm.setString(1, mail.getEmail());
					pstm.setInt(2, mail.getId());
					pstm.setString(3, MailStatus.PENDING.toString());
					pstm.executeUpdate();
				}
				connection.commit();
				queue.clear();
				logger.info("Dodano " + count + " nowych maili do kolejki");
				connection.setAutoCommit(true);
			}
			

			String update = "UPDATE mass_mail SET mass_status = 1 WHERE id = ?";
			pstm2 = connection.prepareStatement(update);
			pstm2.setInt(1, mail.getId());
			pstm2.executeUpdate();
			logger.info("Rozpoczęcie wysyłki w ciągu 1 minuty");
			pstm2.close();

		} catch (SQLException e) {
			logger.info("Bład sql");
			e.printStackTrace();
		}

	}

	/**
	 * Sprawdza czy pojawiły się maile do wysyłki masowej
	 */
	@Override
	public boolean checkConditions() {

		if (queue.isEmpty()) {
			String check = "SELECT count(*) FROM mass_mail WHERE mass_status = 0";
			String isAll = "SELECT users_id FROM mass_mail WHERE mass_status = 0 LIMIT 1";

			try {
				pstm = connection.prepareStatement(check);
				rs = pstm.executeQuery();
				workerActivityLogEntry("mass");

				if (rs.next()) {
					if (rs.getInt(1) > 0) {
						logger.info("Maile do wysłania masowego: " + rs.getInt(1));
						pstm2 = connection.prepareStatement(isAll);
						rs2 = pstm2.executeQuery();
						while(rs2.next()){
							if (rs2.getString("users_id").equals("0")){
								flag = true;
							} else if (!rs2.getString("users_id").equals("0")){
								flag = false;
							}
						}
						return true;
					} else {
						logger.info("Nie ma maili do przetworzenia");
						return false;
					}
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
		return MassMail.class.getSimpleName();
	}

}
