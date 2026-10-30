package org.example;

public class ConfigurationFile implements ProjectElement {
    public final String name;
    public final boolean exposedCredentials;
    public final boolean insecure;

    public ConfigurationFile(String name, boolean exposedCredentials, boolean insecure) {
        this.name = name;
        this.exposedCredentials = exposedCredentials;
        this.insecure = insecure;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
