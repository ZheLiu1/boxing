import com.info6205.ga.Fitness;
import com.info6205.ga.GeneticAlgorithm;
import com.info6205.ga.Population;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        GeneticAlgorithm<BoxVector, Integer> ga = getTestGA();

        addListener(ga);
        ga.evolve(500);
        ga.terminate();
    }

    private static void addListener(GeneticAlgorithm<BoxVector, Integer> ga) {
        int target = 6;

        ga.addIterationListener(_ga -> {
            int usedBoxes = _ga.getBest().getUsedBoxNum(), iterations = _ga.getIteration();
            logger.info("The best solution with " + iterations + " iterations uses " + usedBoxes + " boxes.");
            if (usedBoxes == target) ga.terminate();
            return null;
        }, false);
    }

    static GeneticAlgorithm<BoxVector, Integer> getTestGA() {
        int populationSize = 500;
        double parentChromosomesSurvivePercentage = 1,
                mutatePossibility = 0.4,
                crossoverPercentage = 0.5;

        double[] items = {
                1.0 / 7, 1.0 / 7, 1.0 / 7, 1.0 / 7, 1.0 / 7, 1.0 / 7,
                1.0 / 3, 1.0 / 3, 1.0 / 3, 1.0 / 3, 1.0 / 3, 1.0 / 3,
                1.0 / 2, 1.0 / 2, 1.0 / 2, 1.0 / 2, 1.0 / 2, 1.0 / 2
        };

        Population<BoxVector> population = new Population<>();
        for (int i = 0; i < populationSize; i++) {
            population.addChromosome(new BoxVector(items));
        }

        Fitness<BoxVector, Integer> fitness = new BoxFitness();

        return new GeneticAlgorithm<>(population, fitness, parentChromosomesSurvivePercentage, mutatePossibility, crossoverPercentage);
    }
}
