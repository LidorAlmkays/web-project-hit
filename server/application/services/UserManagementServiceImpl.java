package server.application.services;

import server.application.adaptors.UserManagementService;
import server.domain.employee.Employee;
import server.infustructre.adaptors.LogRepository;

import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagementServiceImpl implements UserManagementService {
    private final LogRepository logRepository;

    // Map: email -> UserSession
    private final Map<String, UserSession> activeUsers = new ConcurrentHashMap<>();
    private final Map<String, Object> emailLocks = new ConcurrentHashMap<>();
    private final Object lockCreationMutex = new Object();

    public UserManagementServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    private Object getLockForEmail(String email) {
        Object lock = emailLocks.get(email);
        if (lock == null) {
            synchronized (lockCreationMutex) {
                lock = emailLocks.get(email);
                if (lock == null) {
                    lock = new Object();
                    emailLocks.put(email, lock);
                }
            }
        }
        return lock;
    }

    @Override
    public void addUser(String email, Employee employee, Socket socket) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (socket == null) {
            throw new IllegalArgumentException("Socket cannot be null");
        }

        Object lock = getLockForEmail(email);
        synchronized (lock) {
            if (activeUsers.containsKey(email)) {
                logRepository.info("User already logged in, replacing session for email: " + email);
            }
            activeUsers.put(email, new UserSession(employee, socket));
            logRepository.info("User added to session management: " + email);
        }
    }

    @Override
    public void removeUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        Object lock = getLockForEmail(email);
        synchronized (lock) {
            UserSession removed = activeUsers.remove(email);
            if (removed != null) {
                logRepository.info("User removed from session management: " + email);
                // Clean up lock if user is removed
                emailLocks.remove(email);
            } else {
                logRepository.info("User not found in session management: " + email);
            }
        }
    }

    @Override
    public Optional<Socket> getSocketByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        UserSession session = activeUsers.get(email);
        if (session != null) {
            return Optional.of(session.getSocket());
        }
        return Optional.empty();
    }

    private static class UserSession {
        private final Employee employee;
        private final Socket socket;

        public UserSession(Employee employee, Socket socket) {
            this.employee = employee;
            this.socket = socket;
        }

        public Employee getEmployee() {
            return employee;
        }

        public Socket getSocket() {
            return socket;
        }
    }
}
