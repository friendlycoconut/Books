package ua.nure.order.server;

import org.apache.log4j.Logger;
import ua.nure.order.entity.user.User;
import ua.nure.order.server.dao.DAOException;
import ua.nure.order.server.dao.UserDAO;
import ua.nure.order.shared.Hash;
import ua.nure.order.shared.UserValidator;
import ua.nure.order.shared.Util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Add user into database.
 */
@WebServlet(
		urlPatterns = { "/adduser" }, 
		initParams = { 
				@WebInitParam(name = "loginPattern", value = "[a-zA-Z0-9\\-_.]{4,20}"),
				@WebInitParam(name = "passPattern", value = ""),
				@WebInitParam(name = "errLoginMsg", value = "Login must be more then 4 symbols"),
				@WebInitParam(name = "errPassMsg", value = "Pass must be more then 4 symbols"),
		})
public class AddUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(AddUser.class);
	private UserDAO dao = null;
	private static UserValidator<User> validator = new UserValidator<>();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddUser() {
        super();
    }

    @Override
	public void init() {
    	log.trace("Login init start");
    	ServletConfig cfg = getServletConfig();
    	dao = (UserDAO) getServletContext().getAttribute("UserDao");
    	String param = cfg.getInitParameter("loginPattern");
    	log.trace("loginPattern --> " + param);
    	if (param != null)
    		validator.loginPattern = param;
    	param = cfg.getInitParameter("passPattern");
    	log.trace("passPattern --> " + param);
    	if (param != null)
    		validator.passPattern = param;
    	param = cfg.getInitParameter("errLoginMsg");
    	log.trace("errLoginMsg --> " + param);
    	if (param != null)
    		validator.errLoginMsg = param;
    	param = cfg.getInitParameter("errPassMsg");
    	log.trace("errPassMsg --> " + param);
    	if (param != null)
    		validator.errPassMsg = param;
    	log.trace("Login init finish");
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		log.trace("doPost start");
		String name = Util.getOrElse(request.getParameter("login"), "").trim();
		log.debug("name --> " + name);
		
		String login = Util.getOrElse(request.getParameter("login"), "").trim();
		log.debug("login --> " + login);
		
		String pass = Util.getOrElse(request.getParameter("password"), "");
		// TODO remove in PRODUCTION
		log.debug("password --> " + pass);
		
		String pass2 = request.getParameter("password2");
		// TODO remove in PRODUCTION
		log.debug("password2 --> " + pass2);
		
		User user = new User(login, pass);
		user.setName(name);
		Map<String, String> errors = validator.validate(user);
		if (!errors.isEmpty()) {
			log.debug("Invalid user");
			goBack(request, response, user, errors);
			return;
		}
		if (!pass.equals(pass2)) {
			log.debug("Passwords not equals");
			errors.put("password2", "Пароли не совпадают");
			goBack(request, response, user, errors);
			return;
		}
		
		try {
			user = dao.getUser(login);
			if (user != null) {
				log.debug("User already exist");
				errors.put("login", "Такой пользователь уже существует");
				goBack(request, response, user, errors);
				return;
			}
			user = new User(login, Hash.encode(pass), "client");
			user.setName(name);
			log.debug("Try add User to db");
			user.setId(dao.addUser(user));
			log.debug("User added to db");
		} catch (DAOException e) {
			log.debug("DAOException", e);
			errors.put("dao", "Ошибка при добавлении пользователя");
			goBack(request, response, user, errors);
			return;
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e.getMessage());
		}
		session.removeAttribute("errors");
		session.setAttribute("user", user);
		log.info("Registered new user --> " + user.getLogin());
		log.debug("Set session attribute user --> " + user);
		log.debug("Redirect to --> list.jsp");
		response.sendRedirect("list.jsp");
	}

	private void goBack(HttpServletRequest request, HttpServletResponse response,
                        User user, Map<String, String> errors) throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		log.debug("Set user to the session");
		session.setAttribute("errors", errors);
		log.debug("Redirect to --> register.jsp");
		response.sendRedirect("register.jsp");
	}
}
