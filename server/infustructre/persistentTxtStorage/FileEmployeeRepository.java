package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.employee.Employee;
import server.domain.employee.EmployeeRole;
import server.infustructre.adaptors.EmployeeRepository;

import java.util.*;

public class FileEmployeeRepository extends AbstractFileRepository<Employee> implements EmployeeRepository {

    private final Map<UUID, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();
    private final Map<UUID, Employee> cache = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, UUID> emailIndex = Collections.synchronizedMap(new HashMap<>());

    public FileEmployeeRepository() {
        super(Config.EMPLOYEES_DIR);
        loadCache();
    }

    private Object getLock(UUID employeeNumber) {
        Object lock = locks.get(employeeNumber);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = locks.get(employeeNumber);
                if (lock == null) {
                    lock = new Object();
                    locks.put(employeeNumber, lock);
                }
            }
        }
        return lock;
    }

    private String getFileName(UUID employeeNumber) {
        return employeeNumber.toString();
    }

    @Override
    protected String encode(Employee entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getEmployeeNumber().toString()).append("\n");
        sb.append(entity.getBranchId() != null ? entity.getBranchId().toString() : "null").append("\n");
        sb.append(entity.getFullName()).append("\n");
        sb.append(entity.getEmployeeId()).append("\n");
        sb.append(entity.getPhoneNumber()).append("\n");
        sb.append(entity.getBankAccountNumber()).append("\n");
        sb.append(entity.getRole().name()).append("\n");
        sb.append(entity.getEmail()).append("\n");
        sb.append(entity.getPassword()).append("\n");
        return sb.toString();
    }

    @Override
    protected Employee decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 9) {
            throw new IllegalArgumentException("Invalid employee data format: insufficient data");
        }

        UUID employeeNumber = UUID.fromString(lines[0].trim());
        UUID branchId = lines[1].trim().equals("null") ? null : UUID.fromString(lines[1].trim());
        String fullName = lines[2].trim();
        String employeeId = lines[3].trim();
        String phoneNumber = lines[4].trim();
        String bankAccountNumber = lines[5].trim();
        EmployeeRole role = EmployeeRole.valueOf(lines[6].trim());
        String email = lines[7].trim();
        String password = lines[8].trim();

        return new Employee(employeeNumber, branchId, fullName, employeeId, phoneNumber, bankAccountNumber, role, email,
                password);
    }

    @Override
    public void save(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("cant save, null employee");
        }

        UUID employeeNumber = employee.getEmployeeNumber();
        Object lock = getLock(employeeNumber);

        synchronized (lock) {
            if (cache.containsKey(employeeNumber)) {
                throw new IllegalArgumentException("cant save, employee number already exists: " + employeeNumber);
            }
            if (emailIndex.containsKey(employee.getEmail())) {
                throw new IllegalArgumentException("cant save, email already exists: " + employee.getEmail());
            }

            String fileName = getFileName(employeeNumber);
            writeToFile(employee, fileName);
            cache.put(employeeNumber, employee.createCopy());
            emailIndex.put(employee.getEmail(), employeeNumber);
        }
    }

    @Override
    public void update(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("cant update, null employee");
        }

        UUID employeeNumber = employee.getEmployeeNumber();
        Object lock = getLock(employeeNumber);

        synchronized (lock) {
            Employee existingEmployee = cache.get(employeeNumber);
            if (existingEmployee == null) {
                throw new IllegalArgumentException("cant update, not found employee");
            }

            // If email is being changed, check for uniqueness
            if (!existingEmployee.getEmail().equals(employee.getEmail())) {
                if (emailIndex.containsKey(employee.getEmail())) {
                    throw new IllegalArgumentException("cant update, email is already taken: " + employee.getEmail());
                }
                emailIndex.remove(existingEmployee.getEmail());
                emailIndex.put(employee.getEmail(), employeeNumber);
            }

            String fileName = getFileName(employeeNumber);
            writeToFile(employee, fileName);
            cache.put(employeeNumber, employee.createCopy());
        }
    }

    @Override
    public void delete(UUID employeeNumber) {
        if (employeeNumber == null) {
            throw new IllegalArgumentException("cant delete, no employee number given to delete");
        }

        Object lock = getLock(employeeNumber);
        synchronized (lock) {
            Employee employee = cache.get(employeeNumber);
            if (employee == null) {
                throw new IllegalArgumentException("cant delete, not found employee: " + employeeNumber);
            }

            String fileName = getFileName(employeeNumber);
            deleteFile(fileName);
            cache.remove(employeeNumber);
            emailIndex.remove(employee.getEmail());
        }
    }

    @Override
    public Optional<Employee> findByEmployeeNumber(UUID employeeNumber) {
        Employee employee = cache.get(employeeNumber);
        return Optional.ofNullable(employee != null ? employee.createCopy() : null);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        UUID employeeNumber = emailIndex.get(email);
        if (employeeNumber == null) {
            return Optional.empty();
        }
        return findByEmployeeNumber(employeeNumber);
    }

    @Override
    public List<Employee> findByBranchId(UUID branchId) {
        List<Employee> employees = new ArrayList<>();
        synchronized (cache) {
            for (Employee employee : cache.values()) {
                UUID employeeBranchId = employee.getBranchId();
                if (employeeBranchId != null && employeeBranchId.equals(branchId)) {
                    employees.add(employee.createCopy());
                }
            }
        }
        return employees;
    }

    private void loadCache() {
        List<Employee> employees = readAllFromDirectory();
        synchronized (cache) {
            for (Employee employee : employees) {
                cache.put(employee.getEmployeeNumber(), employee);
                emailIndex.put(employee.getEmail(), employee.getEmployeeNumber());
            }
        }
    }
}
