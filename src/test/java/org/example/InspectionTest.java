package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InspectionTest {
    private SourceCodeFile source(int complexity, boolean credentials) {
        return new SourceCodeFile("Service.java", "payments", 100, complexity, credentials);
    }

    private TestFile tests(int count) {
        return new TestFile("ServiceTest.java", "payments", 40, count);
    }

    @Test
    void dispatchesAllFiveElementTypes() {
        List<String> calls = new ArrayList<>();
        ProjectVisitor visitor = new ProjectVisitor() {
            public void visit(SourceCodeFile file) { calls.add("source"); }
            public void visit(TestFile file) { calls.add("test"); }
            public void visit(ConfigurationFile file) { calls.add("config"); }
            public void visit(DependencyFile file) { calls.add("dependency"); }
            public void visit(Directory directory) { calls.add("directory"); }
        };
        new Directory("root", source(5, false), tests(2),
                new ConfigurationFile("app", false, false),
                new DependencyFile("pom", Map.of(), Set.of(), Set.of())).accept(visitor);
        assertEquals(List.of("directory", "source", "test", "config", "dependency"), calls);
    }

    @Test
    void detectsHardcodedCredentials() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        source(5, true).accept(visitor);
        assertEquals(List.of("Service.java: hardcoded credentials"), visitor.getIssues());
    }

    @Test
    void detectsHighRiskSource() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        source(21, false).accept(visitor);
        assertEquals(1, visitor.getCriticalIssues());
        assertTrue(visitor.getIssues().get(0).contains("high-risk source"));
    }

    @Test
    void complexityTwentyIsNotHighRisk() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        source(20, false).accept(visitor);
        assertEquals(0, visitor.getCriticalIssues());
    }

    @Test
    void detectsBothConfigurationProblems() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new ConfigurationFile("app", true, true).accept(visitor);
        assertEquals(List.of("app: exposed credentials", "app: insecure configuration"), visitor.getIssues());
    }

    @Test
    void detectsVulnerableDependency() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new DependencyFile("pom", Map.of("sdk", "1"), Set.of("sdk"), Set.of()).accept(visitor);
        assertEquals(List.of("pom: vulnerable dependency sdk"), visitor.getIssues());
    }

    @Test
    void cleanProjectHasNoSecurityIssues() {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        new Directory("clean", source(4, false), tests(3),
                new ConfigurationFile("app", false, false)).accept(visitor);
        assertEquals(0, visitor.getCriticalIssues());
    }

    @Test
    void calculatesDemoStatistics() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        DemoProject.create().accept(visitor);
        assertEquals(5, visitor.getTotalFiles());
        assertEquals(410, visitor.getLinesOfCode());
        assertEquals(2, visitor.getSourceFiles());
        assertEquals(1, visitor.getTestFiles());
        assertEquals(6, visitor.getNumberOfTests());
        assertEquals(2, visitor.getDependencies());
        assertEquals(16.0, visitor.getAverageComplexity(), 0.001);
        assertEquals(0.5, visitor.getTestSourceRatio(), 0.001);
    }

    @Test
    void visitsNestedDirectoriesExactlyOnce() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new Directory("root", new Directory("src", new Directory("payments", source(8, false))),
                new Directory("tests", tests(4))).accept(visitor);
        assertEquals(2, visitor.getTotalFiles());
        assertEquals(140, visitor.getLinesOfCode());
        assertEquals(8.0, visitor.getAverageComplexity(), 0.001);
    }

    @Test
    void emptyDirectoryHasZeroStatistics() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        new Directory("empty").accept(visitor);
        assertEquals(0, visitor.getTotalFiles());
        assertEquals(0.0, visitor.getAverageComplexity());
        assertEquals(0.0, visitor.getTestSourceRatio());
    }

    @Test
    void testOnlyProjectAvoidsDivisionByZero() {
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        tests(3).accept(visitor);
        assertEquals(3, visitor.getNumberOfTests());
        assertEquals(0.0, visitor.getTestSourceRatio());
    }

    @Test
    void qualityGatePassesHealthyProject() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("healthy", source(10, false), tests(2)).accept(visitor);
        assertTrue(visitor.isPassed());
        assertTrue(visitor.report().contains("FINAL RESULT: PASSED"));
    }

    @Test
    void qualityGateFailsForSecurityAlone() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("project", source(5, true), tests(2)).accept(visitor);
        assertFalse(visitor.isSecurityPassed());
        assertTrue(visitor.isComplexityPassed());
        assertTrue(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void qualityGateFailsForComplexityAlone() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("project", source(11, false), tests(2)).accept(visitor);
        assertTrue(visitor.isSecurityPassed());
        assertFalse(visitor.isComplexityPassed());
        assertTrue(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void qualityGateFailsWithoutTests() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        source(5, false).accept(visitor);
        assertTrue(visitor.isSecurityPassed());
        assertTrue(visitor.isComplexityPassed());
        assertFalse(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void qualityGateAcceptsExactRatioBoundary() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("project", source(10, false), source(10, false), source(10, false),
                source(10, false), source(10, false), tests(1), tests(1)).accept(visitor);
        assertTrue(visitor.isPassed());
    }

    @Test
    void qualityGateRejectsRatioBelowBoundary() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("project", source(5, false), source(5, false), source(5, false), tests(1)).accept(visitor);
        assertFalse(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void emptyProjectCannotPassReleaseGate() {
        QualityGateVisitor visitor = new QualityGateVisitor();
        new Directory("empty").accept(visitor);
        assertFalse(visitor.isPassed());
    }

    @Test
    void complianceReportsAllDemoViolations() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        DemoProject.create().accept(visitor);
        assertEquals(5, visitor.getViolations().size());
        assertTrue(visitor.getViolations().contains("Production module users has no tests"));
        assertFalse(visitor.isCompliant());
    }

    @Test
    void complianceDetectsMissingVersionAndProhibitedDependency() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new DependencyFile("pom", Map.of("sdk", "  "), Set.of(), Set.of("sdk")).accept(visitor);
        assertEquals(List.of("pom: missing version for sdk", "pom: prohibited dependency sdk"),
                visitor.getViolations());
    }

    @Test
    void moduleTestsAreRecognizedRegardlessOfTraversalOrder() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new Directory("project", tests(1), source(5, false)).accept(visitor);
        assertTrue(visitor.isCompliant());
        assertEquals(List.of(), visitor.getViolations());
    }

    @Test
    void emptyTestFileDoesNotSatisfyModuleCompliance() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        new Directory("project", source(5, false), tests(0)).accept(visitor);
        assertEquals(List.of("Production module payments has no tests"), visitor.getViolations());
    }

    @Test
    void repeatedReportsDoNotDuplicateComplianceViolations() {
        ComplianceVisitor visitor = new ComplianceVisitor();
        source(5, false).accept(visitor);
        assertEquals(visitor.report(), visitor.report());
        assertEquals(1, visitor.getViolations().size());
    }

    @Test
    void rejectsNegativeMetadata() {
        assertThrows(IllegalArgumentException.class, () -> source(-1, false));
        assertThrows(IllegalArgumentException.class, () -> new SourceCodeFile("a", "m", -1, 1, false));
        assertThrows(IllegalArgumentException.class, () -> tests(-1));
    }

    @Test
    void rejectsFlagsForMissingDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new DependencyFile("pom", Map.of(), Set.of("missing"), Set.of()));
    }
}
