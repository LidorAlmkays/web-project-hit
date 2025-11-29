package server.infustructre.persistentTxtStorage;

import server.config.Config;
import server.domain.Branch;
import server.infustructre.adaptors.BranchRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileBranchRepository extends AbstractFileRepository<Branch>
        implements BranchRepository {
    private final Map<UUID, Object> locks = Collections.synchronizedMap(new HashMap<>());
    private final Object creationMutex = new Object();

    public FileBranchRepository() {
        super(Config.getBranchesDir());
    }

    private Object getLock(UUID branchId) {
        Object lock = locks.get(branchId);
        if (lock == null) {
            synchronized (creationMutex) {
                lock = locks.get(branchId);
                if (lock == null) {
                    lock = new Object();
                    locks.put(branchId, lock);
                }
            }
        }
        return lock;
    }

    private String getFileName(UUID branchId) {
        return branchId.toString();
    }

    @Override
    protected String encode(Branch entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getBranchId().toString()).append("\n");
        sb.append(entity.getBranchName()).append("\n");
        sb.append(entity.getAddress()).append("\n");
        sb.append(entity.getPhoneNumber()).append("\n");
        sb.append(entity.getTotalSold()).append("\n");
        sb.append(entity.getTotalMoneyEarned());
        return sb.toString();
    }

    @Override
    protected Branch decodeFromString(String content) {
        String[] lines = content.split("\n");

        if (lines.length < 6) {
            throw new IllegalArgumentException("Invalid branch data format: insufficient data");
        }

        UUID branchId = UUID.fromString(lines[0].trim());
        String branchName = lines[1].trim();
        String address = lines[2].trim();
        String phoneNumber = lines[3].trim();
        int totalSold = Integer.parseInt(lines[4].trim());
        double totalMoneyEarned = Double.parseDouble(lines[5].trim());

        return new Branch(branchId, branchName, address, phoneNumber, totalSold, totalMoneyEarned);
    }

    @Override
    public void save(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("cant save null id for branch");
        }

        UUID branchId = branch.getBranchId();
        Object lock = getLock(branchId);
        String fileName = getFileName(branchId);

        synchronized (lock) {
            if (fileExists(fileName)) {
                throw new IllegalArgumentException("cant save branch already exists: " + branchId);
            }
            writeToFile(branch, fileName);
        }
    }

    @Override
    public void update(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("cant update branch id cant be null");
        }

        UUID branchId = branch.getBranchId();
        Object lock = getLock(branchId);
        String fileName = getFileName(branchId);

        synchronized (lock) {
            if (!fileExists(fileName)) {
                throw new IllegalArgumentException("cant update branch not found: " + branchId);
            }
            writeToFile(branch, fileName);
        }
    }

    @Override
    public void delete(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("cant delete null branch id");
        }

        Object lock = getLock(branchId);
        String fileName = getFileName(branchId);

        synchronized (lock) {
            if (!fileExists(fileName)) {
                throw new IllegalArgumentException("cant delete didnt find branch: " + branchId);
            }
            deleteFile(fileName);
        }
    }

    @Override
    public Optional<Branch> findById(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("cant find branch id cant be null");
        }

        Object lock = getLock(branchId);
        String fileName = getFileName(branchId);

        synchronized (lock) {
            return Optional.ofNullable(readFromFile(fileName));
        }
    }
}
