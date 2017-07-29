(ns concordia.mutations
  (:require [om.next :as om]))

;;;;;;;;;;
;; Mutations

(defmulti mutate-fn om/dispatch)

(defmethod mutate-fn :default
  [_ _ _]
  {:value nil})

#_(defmethod mutate-fn 'proxy/local-change
    [{:keys [state]} _ params]
    {:action #(proxy-change state params)})

#_(defmethod mutate-fn 'proxy/apply-settings
    [{:keys [state ast]} _ _]
    (let [server (get-in @state server-path)
          enabled? (get-in @state enabled?-path)]
      {:action #(swap! state assoc-in changed?-path false)
       :remote (assoc-in ast [:params]
                         {:proxy/server server
                          :proxy/enabled? enabled?})}))

#_(defmethod mutate-fn 'orders/change-state
    [{:keys [state]} _ {:keys [:db/id :order-node/status]}]
    {:action #()
     :remote true})
