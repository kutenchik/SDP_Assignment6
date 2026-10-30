package org.example;

import java.util.Locale;

public class ProjectStatisticsVisitor implements ProjectVisitor {
    private int totalFiles;
    private int linesOfCode;
    private int sourceFiles;
    private int testFiles;
    private int numberOfTests;
    private int dependencies;
    private int totalComplexity;

    @Override
    public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFiles++;
        linesOfCode += file.linesOfCode;
        totalComplexity += file.complexity;
    }

    @Override
    public void visit(TestFile file) {
        totalFiles++;
        testFiles++;
        linesOfCode += file.linesOfCode;
        numberOfTests += file.numberOfTests;
    }

    @Override
    public void visit(ConfigurationFile file) {
        totalFiles++;
    }

    @Override
    public void visit(DependencyFile file) {
        totalFiles++;
        dependencies += file.dependencies.size();
    }

    @Override
    public void visit(Directory directory) {
        // Directories are not files.
    }

    public int getTotalFiles() { return totalFiles; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getSourceFiles() { return sourceFiles; }
    public int getTestFiles() { return testFiles; }
    public int getNumberOfTests() { return numberOfTests; }
    public int getDependencies() { return dependencies; }

    public double getAverageComplexity() {
        return sourceFiles == 0 ? 0 : (double) totalComplexity / sourceFiles;
    }

    public double getTestSourceRatio() {
        return sourceFiles == 0 ? 0 : (double) testFiles / sourceFiles;
    }

    public String report() {
        return String.format(Locale.ROOT,
                "PROJECT STATISTICS%nTotal files: %d%nLines of code: %d%nSource files: %d"
                        + "%nTest files: %d%nNumber of tests: %d%nDependencies: %d"
                        + "%nAverage complexity: %.2f%nTest/source ratio: %.2f",
                totalFiles, linesOfCode, sourceFiles, testFiles, numberOfTests,
                dependencies, getAverageComplexity(), getTestSourceRatio());
    }
}
