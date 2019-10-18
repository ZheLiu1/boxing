import com.info6205.ga.Chromosome;

import java.util.*;

public class BoxVector implements Chromosome<BoxVector> {
    private static final Random random = new Random();
    private static final int boxCapacity = 1;
    // Map is used to calculate used boxes. It is not static because it may be used in parallel.
    private final Map<Integer, Double> map = new HashMap<>();
    private final double[] itemSize;
    private final int[] vector;

    BoxVector(BoxVector other) {
        this.vector = new int[other.vector.length];
        System.arraycopy(other.vector, 0, this.vector, 0, this.vector.length);
        this.itemSize = new double[other.itemSize.length];
        System.arraycopy(other.itemSize, 0, this.itemSize, 0, this.itemSize.length);
    }

    BoxVector(double[] items) {
        this.vector = new int[items.length];
        this.itemSize = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            this.vector[i] = random.nextInt(items.length);
            this.itemSize[i] = items[i];
        }
    }

    @Override
    public BoxVector mutate() {
        BoxVector result = this.clone();

        // mutate one value
        int index = random.nextInt(this.vector.length);
        int value = random.nextInt(this.vector.length);
        result.vector[index] = value;
        return result;
    }

    @Override
    public BoxVector[] crossover(BoxVector other, double percentage) {
        BoxVector thisClone = this.clone();
        BoxVector otherClone = other.clone();

        int crossoverNumber = Math.max((int) (percentage * this.vector.length), 1);
        for (int i = 0; i < crossoverNumber; i++) {
            int index = random.nextInt(this.vector.length);
            int tmp = thisClone.vector[index];
            thisClone.vector[index] = otherClone.vector[index];
            otherClone.vector[index] = tmp;
        }

        return new BoxVector[]{thisClone, otherClone};
    }

    @Override
    public BoxVector clone() {
        return new BoxVector(this);
    }

    public int getUsedBoxNum() {
        map.clear();
        for (int i = 0; i < vector.length; i++) {
            int boxIndex = vector[i];
            map.put(boxIndex, map.getOrDefault(boxIndex, 0.0) + itemSize[i]);
            if (map.get(boxIndex) > boxCapacity) return Integer.MAX_VALUE;
        }
        return map.size();
    }

    public int[] getVector() {
        return this.vector;
    }
}
