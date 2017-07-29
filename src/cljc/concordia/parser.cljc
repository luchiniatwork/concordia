(ns concordia.parser
  (:require [om.next.impl.parser :as parser]))

;; GraphQL has two types of separators:

;; { and } that delimit details of nodes
(defonce ^:private nodes-details-sep ["{" " " "}"])

;; ( and ) that delimit parameters
(defonce ^:private params-seps ["(" " " ")"])

(declare ast->graphql)

(defn ^:private context
  "This function takes a tuple of separators and a function f.
  It surrounds (in a string) whatever is returned by function f."
  [[start-mark sep end-mark] f]
  (apply str (concat start-mark (interpose sep (f)) end-mark)))

(defn ^:private process-children
  "Process children nodes and wraps then in a node details context."
  [children]
  (context nodes-details-sep
           (fn [] (map #(ast->graphql %) children))))

(defn ^:private aliased-name
  "GraphQL has the concepts of aliases. This function takes care of that."
  [dispatch-key key]
  (if (= dispatch-key key)
    (name dispatch-key)
    (do
      (assert (-> key rest count even?) (str "The key " key " is in the wrong format"))
      (let [detail-map (apply assoc {} (rest key))
            {:keys [as] :or {as dispatch-key}} detail-map]
        (str (name as) ":" (name dispatch-key))))))

(defn ^:private process-params
  "Process the parameters and warps then in a parameter context."
  [params]
  (context params-seps
           (fn [] (map (fn [[%1 %2]] (str (name %1) ":" (pr-str %2))) params))))


(defmulti ast->graphql
  "Converts an Om Next AST to GraphQL."
  (fn [node] (:type node)))

(defmethod ast->graphql :root
  [{:keys [children] :as node}]
  (process-children children))

(defmethod ast->graphql :prop
  [{:keys [dispatch-key key children params] :as node}]
  (cond-> (aliased-name dispatch-key key)
    (not (empty? params)) (str (process-params params))))

(defmethod ast->graphql :join
  [{:keys [dispatch-key key children params] :as node}]
  (cond-> (aliased-name dispatch-key key)
    (not (empty? params)) (str (process-params params))
    :always (str (process-children children))))

(defmethod ast->graphql :default
  [node]
  :unknown)

(defn query->graphql
  "Converts an Om Next query to GraphQL."
  [query]
  (-> query
      parser/query->ast
      ast->graphql))
