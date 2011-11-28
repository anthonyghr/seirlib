(ns seirlib.core)

;Default transition functions
(defn- seir-deltas
  "Transition the compartments"
  [seir-params current-state]
  (let [{:keys [alpha beta gamma epsilon mu p]} seir-params
	{:keys [s e i r]} current-state]
    {:ds (+ (* (- beta) (/ s p) i)
	    (* alpha r)
	    (* mu (- p s)))
     :de (+ (* beta (/ s p) i)
	    (* (- epsilon) e)
	    (* (- mu) e))
     :di (+ (* epsilon e)
	    (* (- gamma) i)
	    (* (- mu) i))
     :dr (+ (* gamma i)
	    (* (- alpha) r)
	    (* (- mu) r))}))

(defn- transition
  "Move from a current SEIR state to the next"
  [transition-fns seir-params current-state]
  (loop [s (:s current-state)
	 e (:e current-state)
	 i (:i current-state)
	 r (:r current-state)
	 transition-fns transition-fns]
    (if (empty? transition-fns)
      {:s s, :e e, :i i, :r r}
      (let [transition-fn (first transition-fns)
	    deltas (transition-fn seir-params current-state)
	    {:keys [ds de di dr]} deltas]
	(recur (+ s ds) (+ e de) (+ i di) (+ r dr) (rest transition-fns))))))

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
				  (:r (:initial-state seir-params)))})
	initial-state (:initial-state seir-params)
	transition-fns (concat [ seir-deltas ] (get seir-params :extra-transition-fns []))]
    (seir-seq seir-params initial-state transition-fns)))

(defmulti s-curve
  "Method to get the susceptibles sequence"
  class)

(defmethod s-curve clojure.lang.PersistentArrayMap
  [seir-params]
  (s-curve (seir seir-params)))

(defmethod s-curve clojure.lang.LazySeq
  [seir-lazyseq]
  (map :s seir-lazyseq))

(defmulti e-curve
  "Method to get the exposed sequence"
  class)

(defmethod e-curve clojure.lang.PersistentArrayMap
  [seir-params]
  (e-curve (seir seir-params)))

(defmethod e-curve clojure.lang.LazySeq
  [seir-lazyseq]
  (map :e seir-lazyseq))

(defmulti i-curve
  "Method to get the infectious sequence"
  class)

(defmethod i-curve clojure.lang.PersistentArrayMap
  [seir-params]
  (i-curve (seir seir-params)))

(defmethod i-curve clojure.lang.LazySeq
  [seir-lazyseq]
  (map :i seir-lazyseq))

(defmulti r-curve
  "Method to get the recovered sequence"
  class)

(defmethod r-curve clojure.lang.PersistentArrayMap
  [seir-params]
  (r-curve (seir seir-params)))

(defmethod r-curve clojure.lang.LazySeq
  [seir-lazyseq]
  (map :r seir-lazyseq))

