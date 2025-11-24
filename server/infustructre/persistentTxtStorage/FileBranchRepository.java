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
    private final Map<UUID, Branch> cache = Collections.synchronizedMap(new HashMap<>());

    public FileBranchRepository() {
        super(Config.getBranchesDir());
        loadCache();
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
        return entity.encode();
    }

    @Override
    protected Branch decodeFromString(String content) {
        return Branch.decodeFromString(content);
    }

    @Override
    public void save(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch must not be null");
        }

        UUID branchId = branch.getBranchId();
        Object lock = getLock(branchId);

        synchronized (lock) {
            if (cache.containsKey(branchId)) {
                throw new IllegalArgumentException("Branch already exists: " + branchId);
            }

            String fileName = getFileName(branchId);
            if (fileExists(fileName)) {
                throw new IllegalArgumentException("Branch file already exists: " + fileName);
            }

            writeToFile(branch, fileName);
            cache.put(branchId, branch.createCopy());
        }
    }

    @Override
    public void update(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch must not be null");
        }

        UUID branchId = branch.getBranchId();
        Object lock = getLock(branchId);

        synchronized (lock) {
            Branch existingBranch = cache.get(branchId);
            if (existingBranch == null) {
                throw new IllegalArgumentException("Branch does not exist: " + branchId);
            }

            writeToFile(branch, getFileName(branchId));
            cache.put(branchId, branch.createCopy());
        }
    }

    @Override
    public void delete(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("BranchId must not be null");
        }

        Object lock = getLock(branchId);

        synchronized (lock) {
            if (!cache.containsKey(branchId)) {
                throw new IllegalArgumentException("Branch does not exist: " + branchId);
            }

            String fileName = getFileName(branchId);
            if (!fileExists(fileName)) {
                throw new IllegalArgumentException("Branch file does not exist: " + fileName);
            }

            deleteFile(fileName);
            cache.remove(branchId);
        }
    }

    @Override
    public Optional<Branch> findById(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("BranchId must not be null");
        }

        Object lock = getLock(branchId);
        synchronized (lock) {
            Branch branch = cache.get(branchId);
            if (branch == null) {
                return Optional.empty();
            }
            return Optional.of(branch.createCopy());
        }
    }

    private void loadCache() {
        java.util.List<Branch> branches = readAllFromDirectory();
        synchronized (cache) {
            for (Branch branch : branches) {
                cache.put(branch.getBranchId(), branch);
            }
        }
    }
}

