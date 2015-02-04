package com.humandevice.wrk.backend.pojos;

import lombok.Getter;
import lombok.Setter;

/**
 * @author michal Worker dodający maile przeznaczone do wysyłki masowej do
 *         kolejki mass queue
 */
public class MassMailPojo {

	@Getter
	@Setter
	private int id;

	
	/**
	 * Tytuł maila
	 */
	@Getter
	@Setter
	private String title;

	/**
	 * Treść maila
	 */
	@Getter
	@Setter
	private String messagePlain;

	/**
	 * Treść maila w html'u
	 */
	@Getter
	@Setter
	private String messageHTML;

	/**
	 * Email użytkownika
	 */
	@Getter
	@Setter
	private String email;

	/**
	 * Id użytkowników do których ma zostać wysłany mail (0 - jak do wszystkich)
	 */
	@Getter
	@Setter
	private String usersId;
}
