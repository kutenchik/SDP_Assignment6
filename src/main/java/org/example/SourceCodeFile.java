package org.example;

public class SourceCodeFile implements ProjectElement {
    public final String name;
    public final String module;
    public final int linesOfCode;
    public final int complexity;
    public final boolean hardcodedCredentials;

    public SourceCodeFile(String name, String module, int linesOfCode,
                          int complexity, boolean hardcodedCredentials) {
        if (linesOfCode < 0 || complexity < 0) {
            throw new IllegalArgumentException("Lines and complexity cannot be negative");
        }
        this.name = name;
        this.module = module;
        this.linesOfCode = linesOfCode;
        this.complexity = complexity;
        this.hardcodedCredentials = hardcodedCredentials;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
