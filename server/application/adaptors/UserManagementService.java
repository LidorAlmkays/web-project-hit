package server.application.adaptors;

import server.domain.employee.Employee;

import java.net.Socket;
import java.util.Optional;

public interface UserManagementService {
    void addUser(String email, Employee employee, Socket socket);

    void removeUser(String email);

    Optional<Socket> getSocketByEmail(String email);
}
