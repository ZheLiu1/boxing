import com.info6205.ga.Chromosome;
import com.info6205.ga.GeneticAlgorithm;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class testGA {

    @Test
    public void testEvolution() {
        GeneticAlgorithm<BoxVector, Integer> ga = Main.getTestGA();

        ga.evolve(500);
        assertEquals(6, ga.getBest().getUsedBoxNum(), 1);
    }

    @Test
    public void testBoxVectorMutation() {
        GeneticAlgorithm<BoxVector, Integer> ga = Main.getTestGA();
        BoxVector chromosome = ga.getPopulation().getChromosome();
        BoxVector other = chromosome.mutate();
        int[] vector1 = chromosome.getVector(), vector2 = other.getVector();
        int difference = 0;
        for (int i = 0; i < vector1.length; i++) {
            difference += vector1[i] == vector2[i] ? 0 : 1;
        }
        assertTrue(difference <= 1);
    }

    @Test
    public void testBoxVectorCrossover() {
        GeneticAlgorithm<BoxVector, Integer> ga = Main.getTestGA();
        BoxVector chromosome = ga.getPopulation().getChromosome();
        BoxVector other = chromosome.mutate();
        BoxVector[] crossoverd = chromosome.crossover(other, 0.3);
        int[] vector1 = crossoverd[0].getVector(), vector2 = chromosome.getVector(), vector3 = other.getVector();
        boolean flag = true;
        for (int i = 0; i < vector1.length; i++) {
            if(!(vector1[i] == vector2[i] || vector1[i] == vector3[i])) flag = false;
        }
        assertTrue(flag);
    }

    @Test
    public void testBoxFitness() {
        BoxFitness fitness = new BoxFitness();
        BoxVector chromosome = new BoxVector(new double[]{1.0});

        assertEquals(1, fitness.calculate(chromosome).intValue());
        chromosome = new BoxVector(new double[]{1.1});
        assertEquals(Integer.MAX_VALUE, fitness.calculate(chromosome).intValue());
    }
}
