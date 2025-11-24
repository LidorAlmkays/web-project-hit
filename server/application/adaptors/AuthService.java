package server.application.adaptors;

import server.domain.employee.Employee;
import java.net.Socket;
import java.util.UUID;

public interface AuthService {

    /**
     * Attempts to authenticate an employee and associate their session with a socket.
     *
     * @param email The employee's email address.
     * @param password The employee's password.
     * @param socket The client socket to bind to the session.
     * @return The authenticated Employee object.
     * @throws IllegalArgumentException if the user is not found or credentials are incorrect.
     * @throws SecurityException if the user is already logged in elsewhere.
     */
    Employee login(String email, String password, Socket socket);

    /**
     * Logs out an employee and releases their session socket.
     *
     * @param employeeNumber The unique number of the employee to log out.
     */
    void logout(UUID employeeNumber);
}
