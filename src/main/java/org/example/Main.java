package org.example;

public class Main {
    public static void main(String[] args) {
        Directory project = DemoProject.create();

        SecurityAuditVisitor security = new SecurityAuditVisitor();
        project.accept(security);
        System.out.println(security.report());
        System.out.println();

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);
        System.out.println(statistics.report());
        System.out.println();

        QualityGateVisitor quality = new QualityGateVisitor();
        project.accept(quality);
        System.out.println(quality.report());
        System.out.println();

        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(compliance);
        System.out.println(compliance.report());
        System.out.println();

        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
        project.accept(debt);
        System.out.println(debt.report());
    }
}
