package org.example;

public class TestFile implements ProjectElement {
    public final String name;
    public final String module;
    public final int linesOfCode;
    public final int numberOfTests;

    public TestFile(String name, String module, int linesOfCode, int numberOfTests) {
        if (linesOfCode < 0 || numberOfTests < 0) {
            throw new IllegalArgumentException("Lines and test count cannot be negative");
        }
        this.name = name;
        this.module = module;
        this.linesOfCode = linesOfCode;
        this.numberOfTests = numberOfTests;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
