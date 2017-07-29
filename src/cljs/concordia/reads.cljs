(ns concordia.reads
  (:require [om.next :as om]))

;;;;;;;;;;
;; Reads

(defmulti read-fn om/dispatch)

#_(defmethod read-fn :productCatalog
    [{:keys [state] :as env} k params]
    (let [value (get-in @state [k])]
      {:value value
       :remote true}))

#_(defmethod read-fn :orders/orders
    [{:keys [state]} k _]
    {:value (get-in @state [k])
     :remote true})

(defmethod read-fn :default
  [{:keys [state ast] :as env} k params]
  {:value (get-in @state [k])
   :graphql ast})
