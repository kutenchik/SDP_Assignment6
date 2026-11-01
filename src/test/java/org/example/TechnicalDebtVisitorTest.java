package org.example;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TechnicalDebtVisitorTest {
    @Test
    void sumsDebtForDemoProject() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        DemoProject.create().accept(visitor);
        assertEquals(600, visitor.getDebtMinutes());
    }

    @Test
    void cleanElementsHaveZeroDebt() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new Directory("root", new SourceCodeFile("Service.java", "m", 10, 10, false),
                new TestFile("ServiceTest.java", "m", 10, 1),
                new ConfigurationFile("app", false, false),
                new DependencyFile("pom", Map.of("sdk", "1"), Set.of(), Set.of())).accept(visitor);
        assertEquals(0, visitor.getDebtMinutes());
    }

    @Test
    void emptyTestFileAddsThirtyMinutes() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new TestFile("EmptyTest.java", "m", 0, 0).accept(visitor);
        assertEquals(30, visitor.getDebtMinutes());
    }

    @Test
    void nestedDirectoriesDoNotDuplicateDebt() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new Directory("root", new Directory("src",
                new SourceCodeFile("Service.java", "m", 10, 11, false))).accept(visitor);
        assertEquals(30, visitor.getDebtMinutes());
    }

    @Test
    void emptyDirectoryHasNoDebt() {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        new Directory("empty").accept(visitor);
        assertEquals("TECHNICAL DEBT\nEstimated remediation time: 0 minutes", visitor.report());
    }
}
