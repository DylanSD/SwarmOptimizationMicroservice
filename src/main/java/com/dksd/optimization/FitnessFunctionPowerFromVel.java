package com.dksd.optimization;

/**
 * Cycling related fitness function for calculating power.
 * My pet project for Android App.
 *
 * @author dscottdawkins
 */
public class FitnessFunctionPowerFromVel implements FitnessFunction {

    private double power;
    private double cda;
    private double totalMass;
    private double slope;

    @Override
    public double calcFitness(Particle p) {
        Gene gene = p.getGene();
        return Math.abs(power - calcPower(gene.getValue(0), cda, totalMass, slope));
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double[] getDomain() {
        return new double[50];
    }

    public static double calcPower(double vms, double cda, double totalMass, double slope) {
        double driveEfficiency = 0.96;
        return (getPowerDrag(vms, cda)
                + getPowerRolling(vms, totalMass)
                + getPowerClimbing(vms, totalMass, slope)) / driveEfficiency;
    }

    private static double getPowerRolling(double vms, double riderMass) {
        return vms * riderMass * 9.8 * 0.005 /*Crr*/;
    }

    private static double getPowerClimbing(double vms, double riderMass, double slope) {
        return vms * riderMass * 9.8 * slope;
    }

    private static double getPowerDrag(double vms, double cda) {
        return 0.5 * vms * vms * vms * cda;
    }

}
