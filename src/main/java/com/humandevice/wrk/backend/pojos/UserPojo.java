package com.humandevice.wrk.backend.pojos;

/**
 * @author michal Worker dodający maile przeznaczone do wysyłki masowej do
 *         kolejki mass queue
 */

import lombok.Getter;
import lombok.Setter;

public class UserPojo {

	@Getter
	@Setter
	private int id;

	/**
	 * imie
	 */
	@Getter
	@Setter
	private String name;

	/**
	 * nazwisko
	 */
	@Getter
	@Setter
	private String lastName;

	/**
	 * Email użytkownika
	 */
	@Getter
	@Setter
	private String email;
}
