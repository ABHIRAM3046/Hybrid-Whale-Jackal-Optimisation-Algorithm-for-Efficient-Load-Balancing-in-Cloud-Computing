package cloudsim.woagjo;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

public class WhaleOptimizationAlgorithm {
    private int maxIterations = 100;
    private int searchAgents = 10;
    private double a, A, C, l, p;
    private double[][] positions;
    private double[] fitness;
    private int numCloudlets, numVMs;

    public WhaleOptimizationAlgorithm(int numCloudlets, int numVMs) {
        this.numCloudlets = numCloudlets;
        this.numVMs = numVMs;
        this.positions = new double[searchAgents][numCloudlets];
        this.fitness = new double[searchAgents];
    }

    public void run(List<Cloudlet> cloudlets, List<Vm> vms) {
        initializePositions();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int i = 0; i < searchAgents; i++) {
                fitness[i] = evaluateSolution(positions[i], cloudlets, vms);
            }

            int bestIndex = getBestSolutionIndex();
            double[] bestSolution = positions[bestIndex];

            for (int i = 0; i < searchAgents; i++) {
                updatePosition(i, bestSolution);
            }
        }
    }

    private void initializePositions() {
        Random rand = new Random();
        for (int i = 0; i < searchAgents; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                positions[i][j] = rand.nextInt(numVMs);  // Ensure VM index is within bounds
            }
        }
    }

    private double evaluateSolution(double[] solution, List<Cloudlet> cloudlets, List<Vm> vms) {
        double totalExecutionTime = 0.0;
        for (int i = 0; i < solution.length; i++) {
            int vmIndex = (int) solution[i];

            // Fix: Ensure vmIndex is within bounds
            if (vmIndex < 0 || vmIndex >= vms.size()) {
                System.err.println("Warning: VM index out of bounds. Skipping cloudlet " + i);
                continue;
            }

            totalExecutionTime += cloudlets.get(i).getCloudletLength() / vms.get(vmIndex).getMips();
        }
        return totalExecutionTime;
    }

    private int getBestSolutionIndex() {
        int bestIndex = 0;
        double bestFitness = fitness[0];
        for (int i = 1; i < fitness.length; i++) {
            if (fitness[i] < bestFitness) {
                bestFitness = fitness[i];
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private void updatePosition(int index, double[] bestSolution) {
        Random rand = new Random();
        a = 2 - (index * (2.0 / maxIterations));
        for (int j = 0; j < numCloudlets; j++) {
            A = 2 * a * rand.nextDouble() - a;
            C = 2 * rand.nextDouble();
            l = -1 + (rand.nextDouble() * 2);
            p = rand.nextDouble();

            if (p < 0.5) {
                if (Math.abs(A) < 1) {
                    positions[index][j] = bestSolution[j] - A * Math.abs(C * bestSolution[j] - positions[index][j]);
                } else {
                    int randomAgent = rand.nextInt(searchAgents);
                    positions[index][j] = positions[randomAgent][j] - A * Math.abs(C * positions[randomAgent][j] - positions[index][j]);
                }
            } else {
                positions[index][j] = bestSolution[j] + l * Math.abs(bestSolution[j] - positions[index][j]);
            }

            // Fix: Ensure new VM index is within valid bounds
            if (positions[index][j] < 0) {
                positions[index][j] = 0;
            } else if (positions[index][j] >= numVMs) {
                positions[index][j] = numVMs - 1;
            }
        }
    }
}
