package cloudsim.Hybrid;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.List;
import java.util.Random;

public class HybridGJO_WOA {
    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private Random random;

    public HybridGJO_WOA(List<Cloudlet> cloudletList, List<Vm> vmList) {
        this.cloudletList = cloudletList;
        this.vmList = vmList;
        this.random = new Random();
    }

    public void runOptimization() {
        int numIterations = 100;
        int numCloudlets = cloudletList.size();
        int numVMs = vmList.size();
        
        double[][] position = new double[numCloudlets][numVMs];
        double[][] velocity = new double[numCloudlets][numVMs];
        double[] fitness = new double[numCloudlets];

        // Step 1: Initialize positions and velocities randomly
        for (int i = 0; i < numCloudlets; i++) {
            for (int j = 0; j < numVMs; j++) {
                position[i][j] = random.nextDouble();
                velocity[i][j] = random.nextDouble();
            }
            fitness[i] = evaluate(position[i]);
        }

        for (int iter = 0; iter < numIterations; iter++) {
            for (int i = 0; i < numCloudlets; i++) {
                for (int j = 0; j < numVMs; j++) {
                    // Whale Optimization Component
                    double a = 2 - iter * (2.0 / numIterations);
                    double r1 = random.nextDouble();
                    double r2 = random.nextDouble();
                    double A = 2 * a * r1 - a;
                    double C = 2 * r2;
                    
                    double D = Math.abs(C * position[i][j] - position[i][j]);
                    position[i][j] = position[i][j] - A * D;

                    // Golden Jackal Optimization Component (Improvement)
                    double alpha = 0.5; // Adaptive coefficient
                    double beta = 1.5; // Convergence rate
                    double jackalFactor = alpha * Math.exp(-beta * iter / numIterations);
                    
                    position[i][j] += jackalFactor * (random.nextDouble() - 0.5);
                    
                    // Ensure values stay in [0,1] range
                    position[i][j] = Math.max(0, Math.min(1, position[i][j]));
                }
                fitness[i] = evaluate(position[i]);
            }
        }

        assignCloudlets(position);
    }

    private double evaluate(double[] position) {
        double totalLoad = 0;
        for (double val : position) {
            totalLoad += val;
        }
        return 1 / (1 + totalLoad); // Adjust based on VM execution time
    }

    private void assignCloudlets(double[][] position) {
        for (int i = 0; i < cloudletList.size(); i++) {
            int bestVm = 0;
            double minLoad = Double.MAX_VALUE;
            for (int j = 0; j < vmList.size(); j++) {
                if (position[i][j] < minLoad) {  // Assign to least loaded VM
                    minLoad = position[i][j];
                    bestVm = j;
                }
            }
            cloudletList.get(i).setVmId(vmList.get(bestVm).getId());
        }
    }
}
