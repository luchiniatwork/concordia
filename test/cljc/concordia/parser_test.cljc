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

(deftest basic-nodes-with-namespaces
  (is (= "{node-1 node-2}"
         (parser/query->graphql '[:basic/node-1 :basic/node-2]))))

(deftest basic-nodes-with-subnodes-and-namespaces
  (is (= "{node-a{subnode-1 subnode-2} node-b{subnode-3}}"
         (parser/query->graphql '[{:basic/node-a [:sub/subnode-1 :sub/subnode-2]}
                                  {:basic/node-b [:sub/subnode-3]}]))))
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

(deftest node-with-alias-and-namespaces
  (is (= "{node-a alias:node-a another-alias:node-a}"
         (parser/query->graphql '[:basic/node-a
                                  [:basic/node-a :as :basic/alias]
                                  [:basic/node-a :as :basic/another-alias]]))))

(deftest node-with-alias-subnodes-and-namespaces
  (is (= "{node{subnode-1 subnode-2} alias:node{subnode-3}}"
         (parser/query->graphql '[{:basic/node [:sub/subnode-1 :sub/subnode-2]}
                                  {[:basic/node :as :basic/alias] [:sub/subnode-3]}]))))


;; --------------------
;; Tests with parameters

(deftest node-with-params
  (is (= "{node(p1:1 p2:\"a\") another-node(a:5)}"
         (parser/query->graphql '[(:node {:p1 1 :p2 "a"})
                                  (:another-node {:a 5})]))))

(deftest node-with-params-and-subnodes
  (is (= "{node(p:5){subnode-a subnode-b}}"
         (parser/query->graphql '[{(:node {:p 5}) [:subnode-a :subnode-b]}]))))

(deftest node-with-params-and-namespaces
  (is (= "{node(p1:1 p2:\"a\") another-node(a:5)}"
         (parser/query->graphql '[(:basic/node {:param/p1 1 :param/p2 "a"})
                                  (:basic/another-node {:param/a 5})]))))

(deftest node-with-params-subnodes-and-namespaces
  (is (= "{node(p:5){subnode-a subnode-b}}"
         (parser/query->graphql '[{(:basic/node {:param/p 5}) [:sub/subnode-a
                                                               :sub/subnode-b]}]))))

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

(deftest a-bit-of-everything-with-namespaces
  (is (= "{basic-node basic-node-aliased:basic-node another-basic-node node-with-subnodes{simple-subnode subnode-with-params(p1:1 p2:\"a\")} node-with-params-and-subnodes(p1:\"b\"){subnode1 subnode2}}"
         (parser/query->graphql '[:root/basic-node
                                  [:root/basic-node :as :root/basic-node-aliased]
                                  :root/another-basic-node
                                  {:root/node-with-subnodes
                                   [:sub/simple-subnode
                                    (:sub/subnode-with-params {:params/p1 1 :params/p2 "a"})]}
                                  {(:sub/node-with-params-and-subnodes {:params/p1 "b"})
                                   [:sub/subnode1 :sub/subnode2]}]))))

;; --------------------
;; Parsing response back to state

(deftest query-to-state
  (is (= '{:basic-node "basic-node-value"
           :basic-node-aliased "basic-node-aliased-value"
           :another-basic-node "another-value"
           :node-with-subnodes {:simple-subnode "simple-sub-value"
                                :subnode-with-params "sub-params-value"}
           :node-with-subnodes-and-params [{:subnode1 "sub1entry1"
                                            :subnode2 "sub2entry1"}
                                           {:subnode1 "sub1entry2"
                                            :subnode2 "sub2entry2"}]}
         (parser/response->state
          '[:basic-node
            [:basic-node :as :basic-node-aliased]
            :another-basic-node
            {:node-with-subnodes
             [:simple-subnode
              (:subnode-with-params {:p1 1 :p2 "a"})]}
            {(:node-with-subnodes-and-params {:p1 "b"})
             [:subnode1 :subnode2]}]
          '{:data {:basic-node "basic-node-value"
                   :basic-node-aliased "basic-node-aliased-value"
                   :another-basic-node "another-value"
                   :node-with-subnodes {:simple-subnode "simple-sub-value"
                                        :subnode-with-params "sub-params-value"}
                   :node-with-subnodes-and-params
                   [{:subnode1 "sub1entry1"
                     :subnode2 "sub2entry1"}
                    {:subnode1 "sub1entry2"
                     :subnode2 "sub2entry2"}]}}))))

(deftest query-to-state-with-namespace
  (is (= '{:root/basic-node "basic-node-value"
           :root/basic-node-aliased "basic-node-aliased-value"
           :root/another-basic-node "another-value"
           :root/node-with-subnodes {:sub1/simple-subnode "simple-sub-value"
                                     :sub1/subnode-with-params "sub-params-value"}
           :root/node-with-subnodes-and-params [{:sub2/subnode1 "sub1entry1"
                                                 :sub2/subnode2 "sub2entry1"}
                                                {:sub2/subnode1 "sub1entry2"
                                                 :sub2/subnode2 "sub2entry2"}]}
         (parser/response->state
          '[:root/basic-node
            [:root/basic-node :as :root/basic-node-aliased]
            :root/another-basic-node
            {:root/node-with-subnodes
             [:sub1/simple-subnode
              (:sub1/subnode-with-params {:p1 1 :p2 "a"})]}
            {(:root/node-with-subnodes-and-params {:p1 "b"})
             [:sub2/subnode1 :sub2/subnode2]}]
          '{:data {:basic-node "basic-node-value"
                   :basic-node-aliased "basic-node-aliased-value"
                   :another-basic-node "another-value"
                   :node-with-subnodes {:simple-subnode "simple-sub-value"
                                        :subnode-with-params "sub-params-value"}
                   :node-with-subnodes-and-params
                   [{:subnode1 "sub1entry1"
                     :subnode2 "sub2entry1"}
                    {:subnode1 "sub1entry2"
                     :subnode2 "sub2entry2"}]}}))))
