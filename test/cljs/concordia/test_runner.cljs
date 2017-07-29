(ns concordia.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [concordia.core-test]
   [concordia.common-test]))

(enable-console-print!)

(doo-tests 'concordia.core-test
           'concordia.common-test)
