(ns concordia.ui.root
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui RepoComponent
  static om/IQuery
  (query [this]
         '[:name :url])
  Object
  (render [this]
          (let [{:keys [url name]} (om/props this)]
            (dom/li nil
                    (dom/a #js {:href url} name)))))

(def repo (om/factory RepoComponent {:keyfn :url}))

(defui PaginatorComponent
  static om/IQuery
  (query [this]
         '[:hasPreviousPage :hasNextPage :startCursor :endCursor])
  Object
  (render [this]
          (let [{:keys [hasPreviousPage hasNextPage startCursor endCursor]} (om/props this)]
            (dom/span nil
                      (dom/button #js {:disabled (not hasPreviousPage)} "<<")
                      (dom/button #js {:disabled (not hasNextPage)} ">>")))))

(def paginator (om/factory PaginatorComponent))


(defui RootComponent
  static om/IQuery
  (query [this]
         `[{:viewer [{(:repositories {:first 10})
                      [:totalCount
                       {:pageInfo ~(om/get-query PaginatorComponent)}
                       {:nodes ~(om/get-query RepoComponent)}]}]}])
  Object
  (render [this]
          (let [{:keys [viewer]} (om/props this)
                repositories (:repositories viewer)]
            (dom/div nil
                     (dom/h2 nil "Git repos:")
                     (dom/ul nil
                             (map repo (:nodes repositories)))
                     (paginator (:pageInfo repositories))))))

(def root (om/factory RootComponent))
