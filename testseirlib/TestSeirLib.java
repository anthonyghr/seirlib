/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testseirlib;

import seirlib.SeirRunner;
import java.util.HashMap;
import java.util.Arrays;

/**
 *
 * @author anthonyg
 */
public class TestSeirLib {
    /**
     * Test the Clojure SEIR library
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HashMap<String, Number> seirParams = new HashMap<String, Number>();
        seirParams.put("r0", 1.7);
        seirParams.put("averageLatentPeriod", 2);
        seirParams.put("infectiousPeriodinfectiousPeriod", 6);
        seirParams.put("susceptible", 1200000.0);
        seirParams.put("exposed", 0.0);
        seirParams.put("infectious", 1.0);
        seirParams.put("recovered", 0.0);        
        
        // TODO code application logic here
        SeirRunner seirRunner = new SeirRunner(seirParams);
        
        HashMap<String, Object> seirResult = seirRunner.runSeir(100L);
        Number[] sCurve = (Number[])seirResult.get("sCurve");
        Number[] eCurve = (Number[])seirResult.get("eCurve");
        Number[] iCurve = (Number[])seirResult.get("iCurve");
        Number[] rCurve = (Number[])seirResult.get("rCurve");
        Number[] incidenceCurve = (Number[])seirResult.get("incidenceCurve");
        Number numberInfected = (Number)seirResult.get("numberInfected");
        System.out.println("sCurve(" + sCurve.length + "): " + Arrays.toString(sCurve));
        System.out.println("eCurve(" + eCurve.length + "): " + Arrays.toString(eCurve));
        System.out.println("iCurve(" + iCurve.length + "): " + Arrays.toString(iCurve));
        System.out.println("rCurve(" + rCurve.length + "): " + Arrays.toString(rCurve));
        System.out.println("incidenceCurve(" + incidenceCurve.length + "): " + Arrays.toString(incidenceCurve));
        System.out.println("numberInfected: " + numberInfected);
    }
}
