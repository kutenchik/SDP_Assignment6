package org.example;

import java.util.ArrayList;
import java.util.List;

public class SecurityAuditVisitor implements ProjectVisitor {
    private final List<String> issues = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hardcodedCredentials) {
            issues.add(file.name + ": hardcoded credentials");
        }
        if (file.complexity > 20) {
            issues.add(file.name + ": high-risk source (complexity > 20)");
        }
    }

    @Override
    public void visit(TestFile file) {
        // Test metadata contains no security flags.
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.exposedCredentials) {
            issues.add(file.name + ": exposed credentials");
        }
        if (file.insecure) {
            issues.add(file.name + ": insecure configuration");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (String dependency : file.vulnerableDependencies.stream().sorted().toList()) {
            issues.add(file.name + ": vulnerable dependency " + dependency);
        }
    }

    @Override
    public void visit(Directory directory) {
        // Directory.accept handles traversal.
    }

    public List<String> getIssues() {
        return List.copyOf(issues);
    }

    public int getCriticalIssues() {
        return issues.size();
    }

    public String report() {
        return "SECURITY AUDIT\nCritical issues: " + getCriticalIssues()
                + (issues.isEmpty() ? "\nNo security issues." : "\n" + String.join("\n", issues));
    }
}
