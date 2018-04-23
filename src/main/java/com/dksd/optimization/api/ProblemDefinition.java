package com.dksd.optimization.api;

/**
 * POJO that defines a problem that we want the service to control.
 *
 * @author dscottdawkins
 */
public class ProblemDefinition {

    private String name;

    private String description;

    private int numParticles;

    private String fitnessFunctionCode;

    private String fitnessFunctionClassName;

    public String getFitnessFunctionCode() {
        return fitnessFunctionCode;
    }

    public String getFitnessFunctionClassName() {
        return fitnessFunctionClassName;
    }

    public int getNumParticles() {
        return numParticles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFitnessFunctionClassName(String fitnessFunctionClassName) {
        this.fitnessFunctionClassName = fitnessFunctionClassName;
    }

    public void setNumParticles(int numParticles) {
        this.numParticles = numParticles;
    }

    public void setFitnessFunctionCode(String fitnessFunctionCode) {
        this.fitnessFunctionCode = fitnessFunctionCode;
    }

    public String getDescription() {
        return description;
    }
}
