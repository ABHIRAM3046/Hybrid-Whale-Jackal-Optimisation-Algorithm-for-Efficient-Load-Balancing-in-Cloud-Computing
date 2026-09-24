package cloudsim.Hybrid;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HybridMetricsAnalysis {

    // ============================================
    // 1. LOAD STANDARD DEVIATION
    // ============================================

    public static double calculateLoadStdDev(
            List<Cloudlet> cloudletList) {

        Map<Integer, Double> vmLoadMap =
                new HashMap<>();

        // Collect VM loads
        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                int vmId = cloudlet.getVmId();

                double load =
                        cloudlet.getActualCPUTime();

                vmLoadMap.put(
                        vmId,
                        vmLoadMap.getOrDefault(vmId, 0.0)
                                + load);
            }
        }

        double total = 0;

        for (double load : vmLoadMap.values()) {
            total += load;
        }

        double mean =
                total / vmLoadMap.size();

        double variance = 0;

        for (double load : vmLoadMap.values()) {

            // Normalized deviation
            double normalized =
                    (load - mean) / mean;

            variance += normalized * normalized;
        }

        variance /= vmLoadMap.size();

        return Math.sqrt(variance);
    }


    // ============================================
    // 2. JAIN FAIRNESS INDEX
    // ============================================

    public static double calculateFairnessIndex(
            List<Cloudlet> cloudletList) {

        Map<Integer, Double> vmLoadMap =
                new HashMap<>();

        // Collect VM loads
        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                int vmId = cloudlet.getVmId();

                double load =
                        cloudlet.getActualCPUTime();

                vmLoadMap.put(
                        vmId,
                        vmLoadMap.getOrDefault(vmId, 0.0)
                                + load);
            }
        }

        double sum = 0;
        double sumSquare = 0;

        for (double load : vmLoadMap.values()) {

            sum += load;

            sumSquare += (load * load);
        }

        int n = vmLoadMap.size();

        if (sumSquare == 0 || n == 0) {
            return 0;
        }

        return (sum * sum) / (n * sumSquare);
    }


    // ============================================
    // 3. SLA VIOLATION RATE
    // ============================================

    public static double calculateSLAViolation(
            List<Cloudlet> cloudletList) {

        double totalResponseTime = 0;

        // Calculate average response time
        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                totalResponseTime +=
                        cloudlet.getActualCPUTime();
            }
        }

        double averageResponseTime =
                totalResponseTime / cloudletList.size();

        int violations = 0;

        // SLA condition:
        // violation if response time exceeds
        // average by 20%

        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                double responseTime =
                        cloudlet.getActualCPUTime();

                if (responseTime >
                        averageResponseTime * 1.2) {

                    violations++;
                }
            }
        }

        return ((double) violations
                / cloudletList.size()) * 100.0;
    }


    // ============================================
    // 4. COST CALCULATION
    // ============================================

    public static double calculateCost(
            List<Cloudlet> cloudletList,
            List<Vm> vmList,
            double costPerSec) {

        double totalCost = 0;

        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                int vmId = cloudlet.getVmId();

                Vm vm = vmList.get(vmId);

                double mips = vm.getMips();

                double executionTime =
                        (double) cloudlet.getCloudletLength()
                                / mips;

                double cost =
                        executionTime * costPerSec;

                totalCost += cost;
            }
        }

        return totalCost;
    }


    // ============================================
    // 5. MAKESPAN
    // ============================================

    public static double calculateMakespan(
            List<Cloudlet> cloudletList) {

        double makespan = 0;

        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getCloudletStatus()
                    == Cloudlet.SUCCESS) {

                makespan = Math.max(
                        makespan,
                        cloudlet.getFinishTime());
            }
        }

        return makespan;
    }


    // ============================================
    // 6. THROUGHPUT
    // ============================================

    public static double calculateThroughput(
            List<Cloudlet> cloudletList) {

        double makespan =
                calculateMakespan(cloudletList);

        if (makespan == 0) {
            return 0;
        }

        return cloudletList.size() / makespan;
    }


    // ============================================
    // 7. PRINT METRICS
    // ============================================

    public static void printMetrics(
            List<Cloudlet> cloudletList,
            List<Vm> vmList) {

        double costPerSec = 3.0;

        double stdDev =
                calculateLoadStdDev(cloudletList);

        double fairness =
                calculateFairnessIndex(cloudletList);

        double slaViolation =
                calculateSLAViolation(cloudletList);

        double totalCost =
                calculateCost(
                        cloudletList,
                        vmList,
                        costPerSec);

        double makespan =
                calculateMakespan(cloudletList);

        double throughput =
                calculateThroughput(cloudletList);

        System.out.println(
                "\n========== ADDITIONAL METRICS ==========");

        System.out.printf(
                "Load Standard Deviation: %.5f%n",
                stdDev);

        System.out.printf(
                "Jain Fairness Index: %.5f%n",
                fairness);

        System.out.printf(
                "Throughput: %.5f Cloudlets/sec%n",
                throughput);

        System.out.printf(
                "Total Cost: %.2f%n",
                totalCost);
    }
}