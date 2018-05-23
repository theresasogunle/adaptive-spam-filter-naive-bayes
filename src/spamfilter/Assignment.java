/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spamfilter;

import java.util.Random;

/**
 *
 * @author Tess
 */

/**
 *
 * GROUP MEMBERS
 * SOGUNLE OMOWUNMI 2013/2991
 * LIPTROT TOMIWA 2013/3066
 * UKIM SAMUEL 2013/3195
 */

public class Assignment {
     public static void main(String[] args) {
          Random random = new Random();

            int randomNumbers[]=new int[101];// 100 random numbers
            int count[]=new int[21]; //between 1 and 20
            
        for(int i=1; i<101; ++i) {
            randomNumbers[i]=random.nextInt(21); //generates the random number
         
            }
        
        for (int number : randomNumbers)// for each number
            count[number]++;
        
        for (int i = 1; i <= 20; i++) {
            System.out.println( i + " occurs " + count[i] + " times"); // result
        }
     }
}
