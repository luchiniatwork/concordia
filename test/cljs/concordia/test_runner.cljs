(ns concordia.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [concordia.core-test]
   [concordia.parser-test]))

(enable-console-print!)

(doo-tests 'concordia.core-test
           'concordia.parser-test)
