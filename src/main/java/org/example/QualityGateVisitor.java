package org.example;

public class QualityGateVisitor implements ProjectVisitor {
    private final SecurityAuditVisitor security = new SecurityAuditVisitor();
    private final ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();

    @Override
    public void visit(SourceCodeFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(TestFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(ConfigurationFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(DependencyFile file) {
        security.visit(file);
        statistics.visit(file);
    }

    @Override
    public void visit(Directory directory) {
        // Children are visited exactly once by Directory.accept.
    }

    public boolean isSecurityPassed() { return security.getCriticalIssues() == 0; }
    public boolean isComplexityPassed() { return statistics.getAverageComplexity() <= 10; }
    public boolean isTestsPassed() {
        return statistics.getSourceFiles() > 0 && statistics.getTestSourceRatio() >= 0.40;
    }

    public boolean isPassed() {
        return isSecurityPassed() && isComplexityPassed() && isTestsPassed();
    }

    private String status(boolean passed) { return passed ? "PASS" : "FAIL"; }

    public String report() {
        return "QUALITY GATE\nSecurity: " + status(isSecurityPassed())
                + "\nComplexity: " + status(isComplexityPassed())
                + "\nTests: " + status(isTestsPassed())
                + "\nFINAL RESULT: " + (isPassed() ? "PASSED" : "FAILED");
    }
}
