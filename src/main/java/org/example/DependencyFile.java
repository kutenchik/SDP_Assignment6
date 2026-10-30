package org.example;

import java.util.Map;
import java.util.Set;

public class DependencyFile implements ProjectElement {
    public final String name;
    public final Map<String, String> dependencies;
    public final Set<String> vulnerableDependencies;
    public final Set<String> prohibitedDependencies;

    public DependencyFile(String name, Map<String, String> dependencies,
                          Set<String> vulnerableDependencies, Set<String> prohibitedDependencies) {
        if (!dependencies.keySet().containsAll(vulnerableDependencies)
                || !dependencies.keySet().containsAll(prohibitedDependencies)) {
            throw new IllegalArgumentException("Flagged dependencies must exist in the file");
        }
        this.name = name;
        this.dependencies = Map.copyOf(dependencies);
        this.vulnerableDependencies = Set.copyOf(vulnerableDependencies);
        this.prohibitedDependencies = Set.copyOf(prohibitedDependencies);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
