package com.info6205.ga;

import java.util.*;

public class Population<C extends Chromosome<C>> implements Iterable<C> {

    private List<C> chromosomes = new ArrayList<>();

    private final Random random = new Random();

    public void addChromosome(C chromosome) {
        this.chromosomes.add(chromosome);
    }

    public int getSize() {
        return this.chromosomes.size();
    }

    public C getChromosome() {
        int numOfChromosomes = this.chromosomes.size();
        int index = this.random.nextInt(numOfChromosomes);
        return this.chromosomes.get(index);
    }

    public C getChromosome(int index) {
        return this.chromosomes.get(index);
    }

    public void sortPopulationByFitness(Comparator<C> chromosomesComparator) {
        this.chromosomes.sort(chromosomesComparator);
    }

    public void trim(int len) {
        this.chromosomes = this.chromosomes.subList(0, len);
    }

    @Override
    public Iterator<C> iterator() {
        return this.chromosomes.iterator();
    }

}