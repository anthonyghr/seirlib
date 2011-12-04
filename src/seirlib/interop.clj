(ns seirlib.interop
  (:gen-class
   :name seirlib.SeirRunner
   :constructors {[java.util.HashMap] []}
   :methods [[runSeir [Long] java.util.HashMap]
	     [runSeir [Long Long] java.util.HashMap]]
   :state state
   :init init)
  (:use [seirlib.core :only (seir s-curve e-curve i-curve r-curve incidence-curve)])
  (:import [java.util HashMap]))

(defn -init
  "Initialization method for generated class. Gets called when the class is instantiated"
  [^HashMap params]
  [[] ;Passes nothing to the Object parent
   {:r0 (.get params "r0")
    :latent-period (.get params "averageLatentPeriod")
    :infectious-period (.get params "infectiousPeriodinfectiousPeriod")
    :initial-state {:s (.get params "susceptible")
		    :e (.get params "exposed")
		    :i (.get params "infectious")
		    :r (.get params "recovered")}}])

(defn ^HashMap -runSeir
  "Call the SEIR library with the given params"
  ([this ^Long length]
     (-runSeir this 0 length))
  ([this ^Long start ^Long length]
     (let [seir-seq (drop start (take (+ start length) (seir (.state this))))
	   incd-curve (incidence-curve seir-seq)]
       (HashMap. {"sCurve" (into-array Number (s-curve seir-seq))
		  "eCurve" (into-array Number (e-curve seir-seq))
		  "iCurve" (into-array Number (i-curve seir-seq))
		  "rCurve" (into-array Number (r-curve seir-seq))
		  "incidenceCurve" (into-array Number incd-curve)
		  "numberInfected" (reduce + incd-curve)}))))
       
  
  
