package com.info6205.ga;

import java.util.List;

public interface Chromosome<T extends Chromosome<T>> {

    T mutate();
    T[] crossover(T other, double percentage);
    T clone();
}
