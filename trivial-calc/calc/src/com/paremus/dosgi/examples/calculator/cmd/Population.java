
package com.paremus.dosgi.examples.calculator.cmd;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Population calculates various statistics from an array of long sample values.
 * Improved/extended version of the original by Kirk Pepperdine:
 * http://kirk.blog-city.com/populationjava.htm
 */
public class Population {

    public enum Statistic {
        AVERAGE {
            @Override
            public Number valueFor(Population p) {
                return p.average();
            }
        },
        MAX {
            @Override
            public Number valueFor(Population p) {
                return p.max();
            }
        },
        MEDIAN {
            @Override
            public Number valueFor(Population p) {
                return p.median();
            }
        },
        MIN {
            @Override
            public Number valueFor(Population p) {
                return p.min();
            }
        },
        SIZE {
            @Override
            public Number valueFor(Population p) {
                return p.size();
            }
        },
        STDDEV {
            @Override
            public Number valueFor(Population p) {
                return p.stddev();
            }
        },
        VARIANCE {
            @Override
            public Number valueFor(Population p) {
                return p.variance();
            }
        };

        public abstract Number valueFor(Population p);

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private long[] values;
    private int count = 0;

    public Population() {
        this(1000);
    }

    public Population(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be >= 0");
        }

        this.values = new long[size];
        this.count = 0;
    }

    public Population(long[] values) {
        this.values = values;
        this.count = values.length;
    }

    public void add(long value) {
        if (count >= values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }

        values[count++] = value;
    }

    public double average() {
        if (count == 0) {
            return 0.0f;
        }

        float sumOfX = 0;

        for (int i = 0; i < count; i++) {
            sumOfX += values[i];
        }

        return ((double)sumOfX) / this.size();
    }

    public long max() {
        if (count == 0) {
            return 0;
        }

        long max = Long.MIN_VALUE;

        for (int i = 0; i < count; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    public float median() {
        if (count == 0) {
            return 0.0f;
        }

        long[] sortedValues = Arrays.copyOf(values, count);
        Arrays.sort(sortedValues);
        return sortedValues[sortedValues.length / 2];
    }

    public long min() {
        if (count == 0) {
            return 0;
        }

        long min = Long.MAX_VALUE;

        for (int index = 0; index < count; index++) {
            if (values[index] < min) {
                min = values[index];
            }
        }

        return min;
    }

    public int size() {
        return count;
    }

    public Map<Statistic, Number> statistics() {
        return statistics(Statistic.values());
    }

    public Map<Statistic, Number> statistics(Statistic... stats) {
        Map<Statistic, Number> m = new EnumMap<Statistic, Number>(Statistic.class);

        for (Statistic s : stats) {
            m.put(s, s.valueFor(this));
        }

        return m;
    }

    public double stddev() {
        return Math.sqrt(this.variance());
    }

    @Override
    public String toString() {
        return statistics().toString();
    }

    public double variance() {
        if (this.size() < 2) {
            return 0.0;
        }

        double sumOfXSquared = 0;
        double sumOfX = 0;

        for (int i = 0; i < count; i++) {
            sumOfX += values[i];
            sumOfXSquared += (values[i] * values[i]);
        }

        return Math.abs(((count * sumOfXSquared) - (sumOfX * sumOfX)) / (count * (count - 1)));
    }

    public static void main(String[] args) {
        Population p = new Population();
        Random r = new Random();

        System.out.println(p);

        for (int i = 0; i < 10; i++) {
            long sample = r.nextInt(100);
            p.add(sample);
        }

        System.out.println(p);
    }

}
