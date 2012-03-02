package com.paremus.examples.calculator.api;

/*
 * represents a function of the form (int,int) -> int  
 */
public interface BinaryOp {
    
    int apply(int lhs, int rhs);

    String getSymbol();

    String getOpName();
   
    int getPrecedence();
}
