/*
 * Copyright 2006-2010 Paremus Limited. All rights reserved.
 * PAREMUS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.paremus.demo.fractal.equation;

import java.util.Random;

import aQute.bnd.annotation.component.Component;

import com.paremus.demo.fractal.api.ComplexNumber;
import com.paremus.demo.fractal.api.Equation;

/**
 * An {@link Equation} that uses a Julia generation function.
 * 
 * Note that service deliberately adds artificial latency when calculating.
 * This delay helps to make the rendering behaviour of the demo more obvious.
 */

@Component(properties = {Equation.EQUATION_TYPE + "=julia", "minX:Double=-1.65", 
		"maxX:Double=1.65", "minY:Double=-1.43", "maxY:Double=1.43", "iterations:Integer=200"})
public class JuliaEquation implements Equation {

    public JuliaEquation() {
        super();
    }

    @Override
	public int[][] execute(int width, int height, double startX, double deltaX,
			double startY, double deltaY, int maxIterations, int colourDepth) {
                
    	int[][] values = new int[width][height];

        for(int x = 0; x < width; x ++) {
        	// Add a five millisecond sleep for each column we calculate to
        	// simulate additional latency in the service
        	try {
        		Thread.sleep(5);
        	} catch (InterruptedException e) { }
        	
        	for(int y = 0; y < height; y++) {
        	
        		// Apply the Julia function
        		
        		ComplexNumber complex = new ComplexNumber(startX + ((double)x) * deltaX, 
        				startY - ((double)y) * deltaY);
        		
        		final ComplexNumber constant = new ComplexNumber(-0.8d, 0.156d);
        		
        		int iter = 0;
        		do {
        			iter++;
        			
        			if(complex.getMagnitude() > 2.0d) break;
        			
        			complex = complex.square().plus(constant);
        		} while (iter < maxIterations);
        	
        		values[x][y] = toColour(iter, colourDepth, maxIterations);
        	}
        }

        return values;                             
    }

    private final Random random = new Random();
    
    /**
     * Convert the number of iterations into a colour index
     * 
     * @param value - the number of iterations
     * @param colourDepth - the maximum colour depth
     * @param maxPossible - the maximum possible number of iterations
     * @return
     */
	private int toColour(int value, int colourDepth, int maxPossible) {
		
		//Add a bug by including some noise
		int noise = (int) ((random.nextGaussian() / 2.0d) * maxPossible);
		value = value + noise;
		value = (value < 0) ? 0 : (value > maxPossible) ? maxPossible : value;
				
		int i = value * colourDepth / maxPossible -1;
		return i < 0 ? 0 : i;
	}
}
