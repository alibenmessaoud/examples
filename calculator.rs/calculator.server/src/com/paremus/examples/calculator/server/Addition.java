package com.paremus.examples.calculator.server;

import static aQute.bnd.annotation.component.ConfigurationPolicy.require;
import aQute.bnd.annotation.component.Component;

import com.paremus.examples.calculator.api.BinaryOp;

@Component(name = "calculator.add", 
           provide = BinaryOp.class, 
           configurationPolicy = require)
public class Addition implements BinaryOp {

    @Override
    public int apply(int lhs, int rhs) {
        return lhs + rhs;
    }

    @Override
    public String getSymbol() {
        return "+";
    }

    @Override
    public String getOpName() {
        return "add";
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

}
