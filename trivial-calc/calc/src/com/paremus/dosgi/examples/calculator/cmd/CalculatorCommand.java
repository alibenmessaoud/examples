
package com.paremus.dosgi.examples.calculator.cmd;

import java.util.concurrent.TimeUnit;

import com.paremus.dosgi.examples.calculator.api.CalculatorService;

public class CalculatorCommand {

    private volatile CalculatorService calculator;

    public CalculatorCommand() {
        super();
    }

    public void setCalculatorService(CalculatorService service) {
        calculator = service;
    }

    public void shutdown() {
        // "disconnect" cleanly or otherwise clean up client-side state
    }

    public void help() {
        final String[] usage = {"calc - runs a calculation", "Usage: calc [function]",
            "  add <a> <b>      add two integers a, b",
            "  benchmark <n>    run benchmark (add i+i where i=0..n)",
            "  status           show availability", "  help             show help"};

        for (String s : usage) {
            System.out.println(s);
        }
    }

    public void status() {
        if (calculator != null) {
            System.out.println("Calculator service <0x" + Integer.toHexString(calculator.hashCode())
                               + "> is ready.");
        }
        else {
            System.out.println("Calculator service is currently not available.");
        }
    }

    public void add(int a, int b) {
        if (calculator != null) {
            int result = calculator.add(a, b);
            System.out.println("-> " + result);
        }
        else {
            status();
        }
    }

    public void benchmark(int count) {
        if (calculator != null) {
            Population p = new Population(count+1000);
            long result = 0;

            long all_start = System.nanoTime();

            for (int i = 1; i <= count; i++) {
                long rtt_start = System.nanoTime();
                result += calculator.add(i, i);
                p.add(System.nanoTime() - rtt_start);
            }

            long all_end = System.nanoTime();

            System.out.println("-> (1+1)+..+(n+n) = " + result);

            long rps = (count * TimeUnit.SECONDS.toNanos(1)) / (all_end - all_start);
            System.out.println("-> Requests/sec: " + rps);

            System.out.println("-> RTT statistics in microSeconds:");
            System.out.println("     min: " + TimeUnit.NANOSECONDS.toMicros(p.min()));
            System.out.println("     max: " + TimeUnit.NANOSECONDS.toMicros(p.max()));
            System.out.println("     med: " + TimeUnit.NANOSECONDS.toMicros((long)p.median()));
            System.out.println("     avg: " + TimeUnit.NANOSECONDS.toMicros((long)p.average()));
            System.out.println("  stddev: " + TimeUnit.NANOSECONDS.toMicros((long)p.stddev()));
        }
        else {
            status();
        }
    }

}
