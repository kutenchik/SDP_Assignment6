# UML diagrams

GitHub renders the Mermaid class and sequence diagrams below.

## Class diagram

```mermaid
classDiagram
    class ProjectElement {
        <<interface>>
        +accept(ProjectVisitor visitor) void
    }
    class ProjectVisitor {
        <<interface>>
        +visit(SourceCodeFile file) void
        +visit(TestFile file) void
        +visit(ConfigurationFile file) void
        +visit(DependencyFile file) void
        +visit(Directory directory) void
    }
    class SourceCodeFile {
        +String name
        +String module
        +int linesOfCode
        +int complexity
        +boolean hardcodedCredentials
        +accept(ProjectVisitor visitor) void
    }
    class TestFile {
        +String name
        +String module
        +int linesOfCode
        +int numberOfTests
        +accept(ProjectVisitor visitor) void
    }
    class ConfigurationFile {
        +String name
        +boolean exposedCredentials
        +boolean insecure
        +accept(ProjectVisitor visitor) void
    }
    class DependencyFile {
        +String name
        +Map dependencies
        +Set~String~ vulnerableDependencies
        +Set~String~ prohibitedDependencies
        +accept(ProjectVisitor visitor) void
    }
    class Directory {
        +String name
        +List~ProjectElement~ children
        +accept(ProjectVisitor visitor) void
    }
    class SecurityAuditVisitor {
        +getIssues() List~String~
        +getCriticalIssues() int
        +report() String
    }
    class ProjectStatisticsVisitor {
        +getTotalFiles() int
        +getLinesOfCode() int
        +getSourceFiles() int
        +getTestFiles() int
        +getNumberOfTests() int
        +getDependencies() int
        +getAverageComplexity() double
        +getTestSourceRatio() double
        +report() String
    }
    class QualityGateVisitor {
        +isSecurityPassed() boolean
        +isComplexityPassed() boolean
        +isTestsPassed() boolean
        +isPassed() boolean
        +report() String
    }
    class ComplianceVisitor {
        +getViolations() List~String~
        +isCompliant() boolean
        +report() String
    }
    class TechnicalDebtVisitor {
        +getDebtMinutes() int
        +report() String
    }
    class DemoProject {
        +create() Directory
    }
    class Main {
        +main(String[] args) void
    }
    ProjectElement <|.. SourceCodeFile
    ProjectElement <|.. TestFile
    ProjectElement <|.. ConfigurationFile
    ProjectElement <|.. DependencyFile
    ProjectElement <|.. Directory
    Directory "1" *-- "0..*" ProjectElement : children
    ProjectVisitor <|.. SecurityAuditVisitor
    ProjectVisitor <|.. ProjectStatisticsVisitor
    ProjectVisitor <|.. QualityGateVisitor
    ProjectVisitor <|.. ComplianceVisitor
    ProjectVisitor <|.. TechnicalDebtVisitor
    ProjectElement ..> ProjectVisitor : accept visitor
    ProjectVisitor ..> SourceCodeFile : visit
    ProjectVisitor ..> TestFile : visit
    ProjectVisitor ..> ConfigurationFile : visit
    ProjectVisitor ..> DependencyFile : visit
    ProjectVisitor ..> Directory : visit
    QualityGateVisitor *-- SecurityAuditVisitor : security
    QualityGateVisitor *-- ProjectStatisticsVisitor : statistics
    Main ..> DemoProject : create tree
    Main ..> ProjectElement : accept each visitor
    Main ..> ProjectVisitor : run five operations
    DemoProject ..> Directory : construct tree
```

Each concrete visitor implements all five `visit` overloads from `ProjectVisitor`;
they are omitted from the concrete boxes to keep the diagram readable.

## Sequence diagram

This shows the security traversal through the root and `src` directory to the first
source file in the demo. The remaining nodes follow the same `accept`/`visit` flow.

```mermaid
sequenceDiagram
    participant M as Main
    participant P as DemoProject
    participant R as root Directory
    participant S as src Directory
    participant F as PaymentService SourceCodeFile
    participant V as SecurityAuditVisitor
    M->>P: create()
    P-->>M: root Directory
    Note over M,V: Main creates a fresh SecurityAuditVisitor
    M->>R: accept(V)
    R->>V: visit(this Directory)
    V-->>R: return
    R->>S: accept(V)
    S->>V: visit(this Directory)
    V-->>S: return
    S->>F: accept(V)
    F->>V: visit(this SourceCodeFile)
    Note right of V: Record hardcoded credentials<br/>and complexity greater than 20
    V-->>F: return
    F-->>S: return
    Note over R,S: Continue with remaining children
    S-->>R: return
    R-->>M: return after full traversal
    M->>V: report()
    V-->>M: security report
```
