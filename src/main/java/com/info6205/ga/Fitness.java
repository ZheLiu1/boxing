package com.info6205.ga;

public interface Fitness<C extends Chromosome<C>, T extends Comparable<T>> {

    T calculate(C chromosome);

}