package server.application.adaptors;

import server.domain.employee.Employee;
import java.net.Socket;
import java.util.UUID;

public interface AuthService {

    Employee login(String email, String password, Socket socket);

    void logout(UUID employeeNumber);
}
