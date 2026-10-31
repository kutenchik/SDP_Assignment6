package org.example;

import java.util.Map;
import java.util.Set;

public class DemoProject {
    public static Directory create() {
        return new Directory("fintech-project",
                new Directory("src",
                        new SourceCodeFile("PaymentService.java", "payments", 220, 24, true),
                        new SourceCodeFile("UserService.java", "users", 100, 8, false)),
                new Directory("tests",
                        new TestFile("PaymentServiceTest.java", "payments", 90, 6)),
                new Directory("config",
                        new ConfigurationFile("application.properties", true, true)),
                new DependencyFile("pom.xml",
                        Map.of("legacy-payment-sdk", "1.0", "logging-library", ""),
                        Set.of("legacy-payment-sdk"), Set.of("legacy-payment-sdk")));
    }
}
