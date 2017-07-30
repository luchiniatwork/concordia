(ns concordia.compile-environ
  (:require [environ.core :as environ]))

(defmacro env
  [var-name]
  (environ/env var-name))
