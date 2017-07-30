(ns concordia.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [om.next :as om]
            [concordia.parser :as parser]))

(defn graphql-post [url {:keys [remote headers] :or {remote :graphql}}]
  (fn [remotes cb]
    (if-let [query (get remotes remote)]
      (let [graphql {:query (parser/query->graphql query)}]
        (go (let [response (<! (http/post url {:json-params graphql
                                               :with-credentials? false
                                               :headers headers}))]
              (if (= 200 (:status response))
                (if-let [body (:body response)]
                  (do
                    (cb (parser/response->state query body)))))))))))
