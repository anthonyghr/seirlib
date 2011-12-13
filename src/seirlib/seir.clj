(ns seirlib.core
  (:use [incanter core charts stats datasets]
	[seirlib.core :only (transition defcurve)]
	[seirlib.chart :only (set-x-tick-interval)])
  (:import [java.lang Math]))

;Default transition functions
(defn- seir-deltas
  "Transition the compartments"
  [seir-params current-state]
  (let [{:keys [alpha beta gamma epsilon mu p]} seir-params
	{:keys [s e i r in]} current-state]
    {:s (+ (* (- beta) (/ s p) i)
	   (* alpha r)
	   (* mu (- p s)))
     :e (+ (* beta (/ s p) i)
	   (* (- epsilon) e)
	   (* (- mu) e))
     :i (+ (* epsilon e)
	   (* (- gamma) i)
	   (* (- mu) i))
     :r (+ (* gamma i)
	   (* (- alpha) r)
	   (* (- mu) r))
     :in (+ ;(* beta (/ s p) i)
	    (* epsilon e)
	    (- in))}))

(defn- seir-seq
  "Return a lazy sequence of the states of a seir model as it progresses"
  [seir-params current-state transition-fns]
  (let [next-state (transition transition-fns
			       seir-params
			       current-state)]
    (lazy-seq
     (cons current-state (seir-seq seir-params next-state transition-fns)))))

(defn seir
  "Entry function to run a seir model based on the given parameters"
  [seir-params]
  (let [seir-params (merge seir-params
			   {:epsilon (/ 1 (:latent-period seir-params)) ;Latent rate
			    :gamma (/ 1 (:infectious-period seir-params)) ;Infectious rate
			    :beta (/ (:r0 seir-params) (:infectious-period seir-params)) ;Transmission rate
			    :mu (get seir-params :death-rate 0.0) ;Death rate
			    :alpha (get seir-params :immunity-loss-rate 0.0) ;Immunity loss rate
			    :p (+ (:s (:initial-state seir-params))
				  (:e (:initial-state seir-params))
				  (:i (:initial-state seir-params))
				  (:r (:initial-state seir-params)))
			    :initial-state (merge (:initial-state seir-params)
						  {:in 0.0})})
	initial-state (:initial-state seir-params)
	transition-fns (concat [ seir-deltas ] (get seir-params :extra-transition-fns []))]
    (seir-seq seir-params initial-state transition-fns)))

(defcurve s-curve :s "Method to get the susceptibles sequence")

(defcurve e-curve :e "Method to get the exposed sequence")

(defcurve i-curve :i "Method to get the infectious sequence")

(defcurve r-curve :r "Method to get the recovered sequence")

(defcurve incidence-curve :in "Method to get the incidence sequence")

(defmulti seir-plot
  "Generate a plot of the SEIR curves requested"
  (fn [_ params]
    (class params)))

(defmethod seir-plot clojure.lang.PersistentArrayMap
  [plot-params seir-params]
  (let [run-length (:length plot-params)
	_curve-labels (get plot-params :curves [ :in ])
	curve-labels (if (coll? _curve-labels) _curve-labels [ _curve-labels ])
	xlabel (get plot-params :xlabel "Day")
	ylabel (get plot-params :ylabel "Person count")
	domain (range run-length)
	number-of-ticks (get plot-params :number-of-ticks 10)
					;tick-interval (get plot-params :tick-interval (Math/round (/ run-length number-of-ticks)))
	tick-interval 50
	dataset (to-dataset (take run-length (seir seir-params)))]
    (when-not (empty? curve-labels)
      (let [first-curve-label (first curve-labels)
	    first-curve ($ first-curve-label dataset)
	    chart (line-chart domain first-curve :x-label xlabel :y-label ylabel :legend true :series-label first-curve-label)]
	(doseq [curve-label (rest curve-labels)]
	  (let [curve ($ curve-label dataset)]
	    (add-categories chart domain curve :series-label curve-label)))
	(set-x-tick-interval chart domain tick-interval)
	chart))))
