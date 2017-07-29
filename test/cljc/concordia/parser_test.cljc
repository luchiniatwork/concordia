(ns concordia.parser-test
  #? (:cljs (:require-macros [cljs.test :refer (is deftest testing)]))
  (:require [concordia.parser :as parser]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test])))


;; --------------------
;; Basic tests

(deftest basic-nodes
  (is (= "{basic-node-1 basic-node-2}"
         (parser/query->graphql '[:basic-node-1 :basic-node-2]))))

(deftest basic-nodes-with-subnodes
  (is (= "{node-a{subnode-1 subnode-2} node-b{subnode-3}}"
         (parser/query->graphql '[{:node-a [:subnode-1 :subnode-2]}
                                  {:node-b [:subnode-3]}]))))

;; --------------------
;; Tests with aliases

(deftest node-with-alias
  (is (= "{node-a alias:node-a another-alias:node-a}"
         (parser/query->graphql '[:node-a
                                  [:node-a :as :alias]
                                  [:node-a :as :another-alias]]))))

(deftest node-with-alias-and-subnodes
  (is (= "{node{subnode-1 subnode-2} alias:node{subnode-3}}"
         (parser/query->graphql '[{:node [:subnode-1 :subnode-2]}
                                  {[:node :as :alias] [:subnode-3]}]))))

;; --------------------
;; Tests with parameters

(deftest node-with-params
  (is (= "{node(p1:1 p2:\"a\") another-node(a:5)}"
         (parser/query->graphql '[(:node {:p1 1 :p2 "a"})
                                  (:another-node {:a 5})]))))

(deftest node-with-params-and-subnodes
  (is (= "{node(p:5){subnode-a subnode-b}}"
         (parser/query->graphql '[{(:node {:p 5}) [:subnode-a :subnode-b]}]))))


;; --------------------
;; A bit of everything just to wrap up

(deftest a-bit-of-everything
  (is (= "{basic-node basic-node-aliased:basic-node another-basic-node node-with-subnodes{simple-subnode subnode-with-params(p1:1 p2:\"a\")} node-with-params-and-subnodes(p1:\"b\"){subnode1 subnode2}}"
         (parser/query->graphql '[:basic-node
                                  [:basic-node :as :basic-node-aliased]
                                  :another-basic-node
                                  {:node-with-subnodes
                                   [:simple-subnode
                                    (:subnode-with-params {:p1 1 :p2 "a"})]}
                                  {(:node-with-params-and-subnodes {:p1 "b"})
                                   [:subnode1 :subnode2]}]))))
