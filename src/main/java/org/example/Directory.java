package org.example;

import java.util.List;

public class Directory implements ProjectElement {
    public final String name;
    public final List<ProjectElement> children;

    public Directory(String name, ProjectElement... children) {
        this.name = name;
        this.children = List.of(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
        for (ProjectElement child : children) {
            child.accept(visitor);
        }
    }
}
