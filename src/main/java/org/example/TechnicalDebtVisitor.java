package org.example;

public class TechnicalDebtVisitor implements ProjectVisitor {
    private int debtMinutes;

    @Override
    public void visit(SourceCodeFile file) {
        debtMinutes += Math.max(0, file.complexity - 10) * 30;
    }

    @Override
    public void visit(TestFile file) {
        if (file.numberOfTests == 0) {
            debtMinutes += 30;
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.insecure) {
            debtMinutes += 60;
        }
    }

    @Override
    public void visit(DependencyFile file) {
        debtMinutes += file.vulnerableDependencies.size() * 120;
    }

    @Override
    public void visit(Directory directory) {
        // Directory.accept handles traversal.
    }

    public int getDebtMinutes() { return debtMinutes; }

    public String report() {
        return "TECHNICAL DEBT\nEstimated remediation time: " + debtMinutes + " minutes";
    }
}
