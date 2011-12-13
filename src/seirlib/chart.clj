(ns seirlib.chart
  (:import [java.awt Font]
	   [org.jfree.chart.axis ExtendedCategoryAxis]))

(defn set-x-tick-interval
  "Given a chart object with a CategoryAxis domain axis, change it to an ExtendedCategoryAxis that sets the tick interval"
  [chart xaxis-categories xaxis-tick-interval]
  (let [category-plot (.getCategoryPlot chart)
	domain-axis (.getDomainAxis category-plot)
	extended-domain-axis (ExtendedCategoryAxis. (.getLabel domain-axis))]
    ;;Skip ticks by intervals
    (loop [i 0
	   categories xaxis-categories]
      (when-not (empty? categories)
	(let [show-tick? (zero? (rem i xaxis-tick-interval))
	      category (first categories)]
	  (.addSubLabel extended-domain-axis category (if show-tick? (str category) " ")))
	(recur (inc i) (rest categories))))
    ;;Set dummy font for the "real" categories
    (let [font (.getTickLabelFont extended-domain-axis)
	  dummy-font (Font. "Arial" Font/PLAIN 0)]
      (.setTickLabelFont extended-domain-axis dummy-font)
      (.setSubLabelFont extended-domain-axis font))
    ;;Set the domain axis
    (.setDomainAxis category-plot extended-domain-axis)))
	  
  
