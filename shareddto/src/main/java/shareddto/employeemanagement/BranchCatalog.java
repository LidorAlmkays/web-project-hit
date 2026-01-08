package shareddto.employeemanagement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BranchCatalog {
    public static final Map<String, String> KNOWN_BRANCHES = createKnownBranches();

    private static Map<String, String> createKnownBranches() {
        Map<String, String> branches = new LinkedHashMap<>();
        branches.put("550e8400-e29b-41d4-a716-446655440001", "Downtown Premium Store");
        branches.put("550e8400-e29b-41d4-a716-446655440002", "Mall Branch - High Traffic");
        branches.put("550e8400-e29b-41d4-a716-446655440003", "Industrial Outlet");
        return Collections.unmodifiableMap(branches);
    }

    private BranchCatalog() {
    }
}
