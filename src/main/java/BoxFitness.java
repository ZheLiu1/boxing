import com.info6205.ga.Fitness;

public class BoxFitness implements Fitness<BoxVector, Integer> {
    @Override
    public Integer calculate(BoxVector chromosome) {
        return chromosome.getUsedBoxNum();
    }
}