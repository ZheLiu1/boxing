package com.info6205.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class GeneticAlgorithm<C extends Chromosome<C>, T extends Comparable<T>> {

    private static final Logger logger = LogManager.getLogger(GeneticAlgorithm.class), threadPoolLogger = LogManager.getLogger(logger.getName() + ".executor");

    private final Random random = new Random();

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final class Task implements Callable<Void> {

        private final boolean mutationOrCrossover;
        private final int index;
        private final Population<C> population, newPopulation;

        Task(boolean mutationOrCrossover, int index, Population<C> population, Population<C> newPopulation) {
            this.mutationOrCrossover = mutationOrCrossover;
            this.index = index;
            this.population = population;
            this.newPopulation = newPopulation;
        }

        @Override
        public Void call() {
            if (mutationOrCrossover) {
                threadPoolLogger.trace("Starting concurrent mutation with index: " + this.index);
                C chromosome = this.population.getChromosome(this.index);
                C mutated = chromosome.mutate();
                synchronized (newPopulation) {
                    newPopulation.addChromosome(mutated);
                }
            } else {
                threadPoolLogger.trace("Starting concurrent crossover with index: " + this.index);
                C chromosome = this.population.getChromosome(this.index);

                C otherChromosome = this.population.getChromosome();
                C[] crossovered = chromosome.crossover(otherChromosome, crossoverPercentage);

                for (C c : crossovered) {
                    synchronized (newPopulation) {
                        newPopulation.addChromosome(c);
                    }
                }
            }
            return null;
        }
    }

    private final class ChromosomesComparator implements Comparator<C> {

        private final Map<C, T> cache = new WeakHashMap<>();

        @Override
        public int compare(C chr1, C chr2) {
            T fit1 = this.fit(chr1);
            T fit2 = this.fit(chr2);
            return fit1.compareTo(fit2);
        }

        T fit(C chr) {
            T fit = this.cache.get(chr);
            if (fit == null) {
                fit = GeneticAlgorithm.this.fitnessFunc.calculate(chr);
                this.cache.put(chr, fit);
            }
            return fit;
        }

        void clearCache() {
            this.cache.clear();
        }
    }

    private final ChromosomesComparator chromosomesComparator;

    private final Fitness<C, T> fitnessFunc;

    private final List<Function<GeneticAlgorithm<C, T>, Void>> preIterationListeners = new LinkedList<>();

    private final List<Function<GeneticAlgorithm<C, T>, Void>> postIterationListeners = new LinkedList<>();

    private boolean terminated = false;

    private Population<C> population;

    private int iteration = 0;

    private double parentChromosomesSurvivePercentage;

    private double mutatePossibility;

    private double crossoverPercentage;

    public GeneticAlgorithm(Population<C> population, Fitness<C, T> fitnessFunc,
                            double parentChromosomesSurvivePercentage, double mutatePossibility, double crossoverPercentage) {
        this.population = population;
        this.fitnessFunc = fitnessFunc;
        this.chromosomesComparator = new ChromosomesComparator();
        this.population.sortPopulationByFitness(this.chromosomesComparator);
        this.parentChromosomesSurvivePercentage = parentChromosomesSurvivePercentage;
        this.mutatePossibility = mutatePossibility;
        this.crossoverPercentage = crossoverPercentage;

        // add default listeners
        this.addIterationListener((_ga) -> {
            C chromosome = _ga.getBest();
            T score = this.fitness(chromosome);
            logger.trace("Best score " + score + " after " + _ga.getIteration() + " iterations.");
            chromosome = _ga.getWorst();
            score = this.fitness(chromosome);
            logger.trace("Worst score " + score + " after " + _ga.getIteration() + " iterations.");
            return null;
        }, false);
    }

    public void evolve() {
        int parentPopulationSize = this.population.getSize();
        int parentChromosomesSurviveCount = (int) (parentPopulationSize * parentChromosomesSurvivePercentage);

        Population<C> newPopulation = new Population<>();

        for (int i = 0; (i < parentPopulationSize) && (i < parentChromosomesSurviveCount); i++) {
            newPopulation.addChromosome(this.population.getChromosome(i));
        }

        List<Future<Void>> taskList = new LinkedList<>();
        // Mutation
        int mutateNumber = Math.max((int) (parentPopulationSize * mutatePossibility), 1);
        for (int i = 0; i < mutateNumber; i++) {
            int index = random.nextInt(parentPopulationSize);
            taskList.add(executor.submit(new Task(true, index, this.population, newPopulation)));
        }

        // Crossover
        for (int i = 0; i < parentPopulationSize; i++) {
            taskList.add(executor.submit(new Task(false, i, this.population, newPopulation)));
        }

        for (Future<Void> task : taskList) {
            try {
                task.get();
            } catch (Exception exception) {
                logger.error("Something unexpected happens in mutation or crossover! Evolution will terminate!");
                exception.printStackTrace();
                this.terminate();
            }
        }

        newPopulation.sortPopulationByFitness(this.chromosomesComparator);
        newPopulation.trim(parentPopulationSize);
        this.population = newPopulation;
    }

    public void evolve(int count) {
        for (int i = 0; i < count; i++) {
            if (this.terminated) break;
            executeIterationListener(true);
            this.evolve();
            this.iteration++;
            executeIterationListener(false);
        }
    }

    private void executeIterationListener(boolean preOrPost) {
        List<Function<GeneticAlgorithm<C, T>, Void>> listeners = preOrPost ? this.preIterationListeners : this.postIterationListeners;
        for (Function<GeneticAlgorithm<C, T>, Void> listener : listeners) {
            listener.apply(this);
        }
    }

    public int getIteration() {
        return this.iteration;
    }

    public void reset() {
        this.terminated = false;
        this.iteration = 0;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        logger.debug("Evolution reset.");
    }

    public void terminate() {
        if (!this.terminated) {
            this.terminated = true;
            this.executor.shutdown();
            this.clearCache();
            logger.debug("Evolution terminates, threadpool has been shutdown.");
        }
    }

    public Population<C> getPopulation() {
        return this.population;
    }

    public C getBest() {
        return this.population.getChromosome(0);
    }

    public C getWorst() {
        return this.population.getChromosome(this.population.getSize() - 1);
    }

    public void setParentChromosomesSurvivePercentage(double parentChromosomesSurvivePercentage) {
        this.parentChromosomesSurvivePercentage = parentChromosomesSurvivePercentage;
    }

    public double getParentChromosomesSurvivePercentage() {
        return this.parentChromosomesSurvivePercentage;
    }

    public void addIterationListener(Function<GeneticAlgorithm<C, T>, Void> listener, boolean preOrPost) {
        List<Function<GeneticAlgorithm<C, T>, Void>> listeners = preOrPost ? this.preIterationListeners : this.postIterationListeners;
        if (!listeners.contains(listener))  // avoid adding duplicates
            listeners.add(listener);
    }

    public void removeIterationListener(Function<GeneticAlgorithm<C, T>, Void> listener, boolean preOrPost) {
        List<Function<GeneticAlgorithm<C, T>, Void>> listeners = preOrPost ? this.preIterationListeners : this.postIterationListeners;
        listeners.remove(listener);
    }

    public T fitness(C chromosome) {
        return this.chromosomesComparator.fit(chromosome);
    }

    public void clearCache() {
        this.chromosomesComparator.clearCache();
    }
}
