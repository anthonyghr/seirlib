(ns seirlib.core)

(defn- transition
  "Move from a current epidemic state to the next"
  [transition-fns seir-params current-state]
  (let [state-keys (keys current-state)]
    (loop [state current-state
	   transition-fns transition-fns]
      (if (empty? transition-fns)
	state
	(let [transition-fn (first transition-fns)	    
	      new-deltas (transition-fn seir-params current-state)
	      total-deltas (map #(+ (- (get state %) (get current-state %))
				    (get new-deltas % 0.0))
				state-keys)
	      state-vals (map #(+ (get current-state %1) %2)				  
			      state-keys total-deltas)]
	  (recur (apply assoc (cons {} (interleave state-keys state-vals)))
		 (rest transition-fns)))))))

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
     :in (+ (* beta (/ s p) i)
	    ;(* epsilon e)
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

(defmacro defcurve
  "Macro to define the methods to extract a curve"
  [name curve-key doc-string]
  `(let [params# nil]
     (defmulti ~name
       ~doc-string
       class)
     
     (defmethod ~name clojure.lang.PersistentArrayMap
       [params#]
       (~name (seir params#)))

     (defmethod ~name clojure.lang.LazySeq
       [params#]
       (map ~curve-key params#))))

(defcurve s-curve :s "Method to get the susceptibles sequence")

(defcurve e-curve :e "Method to get the exposed sequence")

(defcurve i-curve :i "Method to get the infectious sequence")

(defcurve r-curve :r "Method to get the recovered sequence")

(defcurve incidence-curve :in "Method to get the incidence sequence")
