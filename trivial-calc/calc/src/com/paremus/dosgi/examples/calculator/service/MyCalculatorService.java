
package com.paremus.dosgi.examples.calculator.service;

import com.paremus.dosgi.examples.calculator.api.CalculatorService;

public class MyCalculatorService implements CalculatorService {

    public MyCalculatorService() {
        super();
    }

    public void shutdown() {
        // clean up state, save client session or whatnot
    }

    // CalculatorService API

    public int add(int a, int b) {
        return a + b;
    }

}
