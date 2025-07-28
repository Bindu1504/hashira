import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class SecretSolver {

    public static void main(String[] args) throws Exception {
        System.out.println("Secret for Test Case 1:");
        findSecretFromFile("testcase1.json");

        System.out.println("\nSecret for Test Case 2:");
        findSecretFromFile("testcase2.json");
    }

    public static void findSecretFromFile(String filePath) throws Exception {
        Map<Integer, BigInteger> shares = new HashMap<>();
        int n = 0, k = 0;

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.contains("\"n\"")) {
                n = Integer.parseInt(line.split(":")[1].trim().replace(",", ""));
            } else if (line.contains("\"k\"")) {
                k = Integer.parseInt(line.split(":")[1].trim().replace(",", ""));
            } else if (line.matches("\"\\d+\"\\s*:\\s*\\{")) {
                int x = Integer.parseInt(line.split(":")[0].replace("\"", "").trim());
                String baseLine = reader.readLine().trim();
                String valueLine = reader.readLine().trim();
                int base = Integer.parseInt(baseLine.split(":")[1].replace("\"", "").replace(",", "").trim());
                String value = valueLine.split(":")[1].replace("\"", "").replace(",", "").replace("}", "").trim();
                BigInteger y = new BigInteger(value, base);
                shares.put(x, y);
            }
        }
        reader.close();

        
        List<List<Integer>> combinations = getCombinations(new ArrayList<>(shares.keySet()), k);
        Map<BigInteger, Integer> secretFreq = new HashMap<>();
        Map<BigInteger, List<Integer>> secretToShares = new HashMap<>();

        for (List<Integer> combo : combinations) {
            List<BigInteger> xVals = new ArrayList<>();
            List<BigInteger> yVals = new ArrayList<>();
            for (int i : combo) {
                xVals.add(BigInteger.valueOf(i));
                yVals.add(shares.get(i));
            }
            BigInteger secret = lagrangeInterpolation(BigInteger.ZERO, xVals, yVals);
            secretFreq.put(secret, secretFreq.getOrDefault(secret, 0) + 1);
            secretToShares.putIfAbsent(secret, new ArrayList<>());
            secretToShares.get(secret).addAll(combo);
        }

      
        BigInteger correctSecret = Collections.max(secretFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
        System.out.println("Secret (constant term c): " + correctSecret);

        
        Set<Integer> likelyGood = new HashSet<>(secretToShares.get(correctSecret));
        Set<Integer> allKeys = new HashSet<>(shares.keySet());
        allKeys.removeAll(likelyGood);

        if (!allKeys.isEmpty()) {
            System.out.println("Incorrect Share(s): " + allKeys);
        } else {
            System.out.println("All shares appear consistent.");
        }
    }

    static List<List<Integer>> getCombinations(List<Integer> list, int k) {
        List<List<Integer>> result = new ArrayList<>();
        combinationHelper(result, new ArrayList<>(), list, k, 0);
        return result;
    }

    static void combinationHelper(List<List<Integer>> result, List<Integer> temp, List<Integer> list, int k, int start) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            temp.add(list.get(i));
            combinationHelper(result, temp, list, k, i + 1);
            temp.remove(temp.size() - 1);
        }
    }

    static BigInteger lagrangeInterpolation(BigInteger x, List<BigInteger> xi, List<BigInteger> yi) {
        BigInteger result = BigInteger.ZERO;
        int size = xi.size();
        for (int i = 0; i < size; i++) {
            BigInteger term = yi.get(i);
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    BigInteger num = x.subtract(xi.get(j));
                    BigInteger den = xi.get(i).subtract(xi.get(j));
                    term = term.multiply(num).divide(den);
                }
            }
            result = result.add(term);
        }
        return result;
    }
}
