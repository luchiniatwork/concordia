(ns concordia.core
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom :include-macros true]
            [concordia.reads :as reads]
            [concordia.mutations :as mutations]
            [concordia.ui.root :as root]
            [concordia.utils :as utils]))

(enable-console-print!)

(def app-state (atom {}))

(def parser (om/parser {:read reads/read-fn
                        :mutate mutations/mutate-fn}))

(def reconciler
  (om/reconciler
   {:state app-state
    :parser parser
    :send (utils/graphql-post "https://api.github.com/graphql"
                              {:headers {"Authorization"
                                         "bearer 508ce13448d70ca2fae0e9cd8d02d9d1d68b3cd6"}})
    :remotes [:graphql]}))

(defn render []
  (om/add-root! reconciler root/RootComponent
                (js/document.getElementById "app")))
