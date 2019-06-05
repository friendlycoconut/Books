package ua.nure.order.shared;

import ua.nure.order.entity.user.User;

import java.util.HashMap;
import java.util.Map;

public class UserValidator<T extends User> implements Validator<T> {

	public String loginPattern = "";
	public String passPattern = "";
	public String errLoginMsg = "";
	public String errPassMsg = "";

	/* (non-Javadoc)
	 * @see ua.nure.order.shared.Validator#validate(java.lang.Object)
	 */
	public Map<String, String> validate(T obj) {
		Map<String, String> errors = new HashMap<>();
		if (obj == null || obj.getLogin() == null || !obj.getLogin().matches(loginPattern)) {
			errors.put("login", errLoginMsg);
		}
		if (obj.getPass() == null || !obj.getPass().matches(loginPattern)) {
			errors.put("password", errPassMsg);
		}
		return errors;
	}
}
