(ns concordia.parser
  (:require [om.next.impl.parser :as parser]))

;; GraphQL has two types of separators:

;; { and } that delimit details of nodes (space is a separator)
(defonce ^:private nodes-details-sep ["{" " " "}"])

;; ( and ) that delimit parameters (space is a separator)
(defonce ^:private params-seps ["(" " " ")"])

;; --------------------
;; Helper Functions

(declare ast->graphql)

(defn ^:private context
  "This function takes a tuple of separators and a function f.
  It delimits (in a string) whatever is returned by function f by the separators."
  [[start-mark sep end-mark] f]
  (apply str (concat start-mark (interpose sep (f)) end-mark)))

(defn ^:private process-children
  "Process children nodes and wraps then in a node details context."
  [children]
  (context nodes-details-sep
           (fn [] (map #(ast->graphql %) children))))

(defn ^:private sanitize-key
  "A key might have an :as for alias purposes. This functions returns the aliased
  and sanitized version of the key."
  [dispatch-key key]
  (if (= dispatch-key key)
    dispatch-key
    (do
      (assert (and (sequential? key) (-> key rest count even?))
              (str "The key " key " is in the wrong format"))
      (let [detail-map (apply assoc {} (rest key))
            {:keys [as] :or {as dispatch-key}} detail-map]
        as))))

(defn ^:private aliased-name
  "GraphQL has the concepts of aliases. This function takes care of that by finding
  the alias and returning `alias:original-name`."
  [dispatch-key key]
  (if (= dispatch-key key)
    (name dispatch-key)
    (str (name (sanitize-key dispatch-key key)) ":" (name dispatch-key))))

(defn ^:private process-params
  "Process the parameters and warps then in a parameter context."
  [params]
  (context params-seps
           (fn [] (map (fn [[%1 %2]] (str (name %1) ":" (pr-str %2))) params))))

;; --------------------
;; Om Next to GraphQL

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

;; --------------------
;; GraphQL Response to State

(defn ^:private reconcile-data
  "Given an Om Next AST and a GraphQL data payload return a reconciled data structure
  that complies with the query originating the AST."
  [ast data]
  (cond
    ;; when data is a map, reduce over AST's children searching for nodes and their
    ;; values and recurring where needed
    (map? data)
    (reduce (fn [accum {:keys [dispatch-key key] :as ast-node}]
              (let [sanitized-key (sanitize-key dispatch-key key)
                    alias-name (name sanitized-key)
                    data-node (get data (keyword alias-name))]
                (cond
                  (coll? data-node) (assoc accum sanitized-key
                                           (reconcile-data ast-node data-node))
                  :else (assoc accum sanitized-key data-node))))
            {}
            (:children ast))

    ;; when data is sequential, reduce over the data seq itself and recur into each
    (sequential? data)
    (let [attributes (:children ast)]
      (reduce (fn [accum data-node]
                (conj accum (reconcile-data ast data-node)))
              []
              data))))

(defn ^:private reconcile-errors
  "Given the errors returned from the GraphQL response, make sure they are in a vector."
  [errors]
  (if (sequential? errors) errors [errors]))

(defn response->state
  "Given an Om Next query and a GraphQL response body, returns a payload ready to be merged
  into Om Next's state."
  [query body]
  (cond-> (reconcile-data (parser/query->ast query) (:data body))
    (:errors body) (assoc :graphql/errors (reconcile-errors (:errors body)))))
