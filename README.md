# Smart Code Inspection and Compliance Engine

Assignment 6 demonstrates the Visitor pattern in Java 17+. The application inspects
one in-memory fintech project with five operations. It uses plain Java classes;
JUnit 5 is the only dependency and is used only for tests.

## Build and run

Requirements: JDK 17 or newer and Maven 3.6.3 or newer.

```sh
mvn clean test
mvn package
java -cp target/classes org.example.Main
```

In IntelliJ IDEA, open `pom.xml` as a Maven project, select a JDK of at least 17,
and run `Main.main()`. Tests can also be run from `src/test/java`.

If Maven is not on PATH, PowerShell can use the Maven bundled with this computer's IntelliJ:

```powershell
$maven = 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2.1\plugins\maven\lib\maven3\bin\mvn.cmd'
& $maven '-Dmaven.repo.local=D:\pon\SDP_Assignment6\.maven-cache' clean test package
java -cp target/classes org.example.Main
```

The custom Maven cache is ignored by Git. The standard `mvn` commands work without it
when the user's normal Maven repository is writable.

## Project model and demo

`DemoProject.create()` constructs this object tree:

```text
fintech-project/
├── src/
│   ├── PaymentService.java
│   └── UserService.java
├── tests/
│   └── PaymentServiceTest.java
├── config/
│   └── application.properties
└── pom.xml
```

These are model objects, not files read from disk. File metadata and security flags
are supplied in constructors. Dependency names and vulnerability flags are demo data;
the engine does not contact a vulnerability database or parse Java code.

`Main` creates this tree once and runs a fresh instance of every visitor on it.
`Directory.accept()` visits the directory and recursively calls `accept()` on its
children. Visitors do not recurse, so each node is processed once per traversal.
Nested directories and empty directories use the same code.

The model uses public final fields to keep this small assignment easy to read.
Child lists, dependency maps, sets, and returned issue lists cannot be modified.
Negative source/test metrics and flags referring to absent dependencies are rejected.
Names and module identifiers are expected to be non-null, meaningful strings.
Use an empty or blank version string to represent an unspecified dependency version.
Visitors accumulate results: create a new visitor for each independent analysis.

## Rules and results

| Visitor | Rules or calculation | Demo result |
| --- | --- | --- |
| `SecurityAuditVisitor` | Hardcoded or exposed credentials, insecure configuration, vulnerable dependencies, source complexity > 20. Each finding is critical in this simplified model. | 5 critical issues |
| `ProjectStatisticsVisitor` | Count files, source/test LOC, test cases, dependencies, average source complexity, test/source file ratio. Directories are excluded. | 5 files, 410 LOC, 6 tests, 2 dependencies, complexity 16.00, ratio 0.50 |
| `QualityGateVisitor` | No critical issues, average complexity <= 10, test/source ratio >= 0.40 and at least one source file. Reuses the security and statistics visitors. | Security FAIL, Complexity FAIL, Tests PASS; final FAILED |
| `ComplianceVisitor` | No exposed credentials, all dependency versions specified, no prohibited dependencies, every production module has a test file with at least one test case. | 5 violations, including missing tests for `users` |
| `TechnicalDebtVisitor` | 30 minutes per complexity point above 10, 30 per empty test file, 60 per insecure configuration, 120 per vulnerable dependency. | 600 minutes |

LOC includes source and test files because those are the model types with LOC fields.
Dependencies count declarations across dependency files, without deduplication.
Average complexity uses only source files. The test/source ratio is the number of
test files divided by source files, not test cases or code coverage. With no source
files, average complexity and ratio are defined as zero; the release gate fails.
Technical debt is an illustrative estimate, not a measured repair duration.

The demo intentionally contains violations so that reports show the rules in action.
The complete expected console output is in [docs/demo-output.txt](docs/demo-output.txt).

## Visitor questions

**What problem does Visitor solve here?**
The same project tree needs several independent operations. Visitor keeps the data
model separate from security, statistics, quality, compliance, and debt rules.

**Where are Element and Visitor?**
`ProjectElement` is the Element interface. Its implementations are `SourceCodeFile`,
`TestFile`, `ConfigurationFile`, `DependencyFile`, and `Directory`.
`ProjectVisitor` is the Visitor interface with one `visit` overload per concrete
element. The five operation classes implement it. All classes are in `org.example`.

**What is double dispatch?**
First, `element.accept(visitor)` selects the implementation using the runtime type
of the element. Inside `SourceCodeFile.accept`, `this` has the concrete source-file
type, so Java selects `visit(SourceCodeFile)` at compile time. Then the runtime type
of the visitor selects its implementation of that method. Both object types determine
the operation. Java overload resolution by itself is not runtime double dispatch.
There is no `instanceof`, reflection, or type-selection switch.

**Why not put operations inside elements?**
Adding `checkSecurity()`, `calculateStatistics()`, and every future operation to each
element mixes unrelated responsibilities and repeatedly changes the model. Here the
elements contain data and dispatch/traversal only; each operation owns its rules.

**Is adding a Visitor or an Element easier?**
A new Visitor is easier: implement `ProjectVisitor`, then run it with `accept()`.
The technical-debt commit demonstrates this without changing any Element class or
the visitor interface. A new Element changes the interface and all existing visitors.

**What if we add `DatabaseMigrationFile`?**
Create a class implementing `ProjectElement`, give it meaningful metadata and an
`accept` method that calls `visitor.visit(this)`. Add
`void visit(DatabaseMigrationFile file)` to `ProjectVisitor`. Implement that method
in `SecurityAuditVisitor`, `ProjectStatisticsVisitor`, `QualityGateVisitor`,
`ComplianceVisitor`, and `TechnicalDebtVisitor`, even when an operation has nothing
to do for that type. `QualityGateVisitor` should delegate to its two helper visitors.
Update tests (including the anonymous recording visitor), demo and diagrams.
Without the overload, the new `accept()` cannot compile; after adding the abstract
overload, old visitor implementations cannot compile until updated.
`Directory` and the existing file elements need no changes because children already
use `ProjectElement`.

**When is Visitor a bad choice?**
When new element types appear frequently, every visitor needs frequent edits. For a
small model with one stable operation, direct methods may also be simpler. Visitor
fits a stable set of types with a growing set of operations.

**How does Visitor differ from Strategy?**
Strategy provides interchangeable algorithms behind one behavior interface, such as
different payment-fee calculations. Visitor provides a family of type-specific
operations over a heterogeneous object structure through `accept` and `visit`.
Strategy does not inherently require double dispatch or a visit method for each type.

**Which SOLID principles appear?**
Single Responsibility: model objects store data, each visitor owns an inspection
operation, and `Directory` owns traversal. Open/Closed: operations can be added
without editing elements, although adding element types is a known limitation.
Dependency Inversion: elements accept the `ProjectVisitor` interface, and directories
store `ProjectElement` children. Liskov Substitution: any element can be traversed
through the same `accept` contract. Interface Segregation is a tradeoff: visitors
must implement all overloads, including some empty methods.

## Tests

There are 30 JUnit 5 tests: 25 in `InspectionTest` and 5 in
`TechnicalDebtVisitorTest`. They verify all five dispatch overloads, source/config/
dependency security rules, exact statistics, nested traversal, empty directories,
quality PASS and independent FAIL conditions, exact threshold boundaries, compliance
violations, order-independent module matching, repeated reports, missing versions,
invalid metadata, and technical-debt totals. No test relies on `assertNotNull()`.

## UML and defense

The [class and sequence diagrams](docs/uml.md) describe the actual classes and calls.
For the defense, follow one source file through `accept()` and `visit()`, explain
why `Directory.accept()` owns recursion, and demonstrate a new operation with a new
visitor. Removing `accept()` removes the uniform entry point and typed dispatch:
calling `visitor.visit(element)` on a `ProjectElement` reference cannot select one of
the concrete overloads. Do not replace this with runtime type checks.

For a new security rule, update `SecurityAuditVisitor`, add a focused test, then run
`mvn test`; the quality gate automatically reuses that rule. For a new operation,
add a visitor and tests, then call it from `Main`. For a new element, use the change
list above. Individual defense and Moodle submission must be completed by the student.
