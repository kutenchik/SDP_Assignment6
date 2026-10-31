package org.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComplianceVisitor implements ProjectVisitor {
    private final List<String> violations = new ArrayList<>();
    private final Set<String> productionModules = new HashSet<>();
    private final Set<String> testedModules = new HashSet<>();

    @Override
    public void visit(SourceCodeFile file) {
        productionModules.add(file.module);
        if (file.hardcodedCredentials) {
            violations.add(file.name + ": credentials must not be exposed");
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.numberOfTests > 0) {
            testedModules.add(file.module);
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials) {
            violations.add(file.name + ": credentials must not be exposed");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (String dependency : file.dependencies.keySet().stream().sorted().toList()) {
            if (file.dependencies.get(dependency).isBlank()) {
                violations.add(file.name + ": missing version for " + dependency);
            }
            if (file.prohibitedDependencies.contains(dependency)) {
                violations.add(file.name + ": prohibited dependency " + dependency);
            }
        }
    }

    @Override
    public void visit(Directory directory) {
        // Checks between modules are calculated after traversal in getViolations.
    }

    public List<String> getViolations() {
        List<String> result = new ArrayList<>(violations);
        for (String module : productionModules.stream().sorted().toList()) {
            if (!testedModules.contains(module)) {
                result.add("Production module " + module + " has no tests");
            }
        }
        return List.copyOf(result);
    }

    public boolean isCompliant() { return getViolations().isEmpty(); }

    public String report() {
        List<String> result = getViolations();
        return "COMPLIANCE CHECK\n" + (result.isEmpty() ? "PASS" : "FAIL\n" + String.join("\n", result));
    }
}
