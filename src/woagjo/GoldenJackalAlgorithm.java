package cloudsim.woagjo;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.List;
import java.util.Random;

public class GoldenJackalAlgorithm {
    private int maxIterations = 100;
    private int searchAgents = 10;
    private double[][] positions;
    private double[] fitness;
    private int numCloudlets, numVMs;

    public GoldenJackalAlgorithm(int numCloudlets, int numVMs) {
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
                positions[i][j] = rand.nextInt(numVMs);
            }
        }
    }

    private double evaluateSolution(double[] solution, List<Cloudlet> cloudlets, List<Vm> vms) {
        double totalExecutionTime = 0.0;
        for (int i = 0; i < solution.length; i++) {
            int vmIndex = (int) solution[i];
            if (vmIndex < 0 || vmIndex >= vms.size()) continue;
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
        for (int j = 0; j < numCloudlets; j++) {
            double r1 = rand.nextDouble();
            double r2 = rand.nextDouble();
            
            positions[index][j] = positions[index][j] + r1 * (bestSolution[j] - positions[index][j]) + r2 * (positions[rand.nextInt(searchAgents)][j] - positions[index][j]);
            
            if (positions[index][j] < 0) positions[index][j] = 0;
            if (positions[index][j] >= numVMs) positions[index][j] = numVMs - 1;
        }
    }
}