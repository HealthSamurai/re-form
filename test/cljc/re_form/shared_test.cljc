(ns re-form.shared-test
  (:require [re-form.shared :as sut]
            [clojure.test :refer :all]
            [matcho.core :as matcho]))


(deftest basic-logic
  #_(matcho/match
   (sut/init {} {:path [:forms :myform]
                 :meta {:properties {:name {:type :string}}}
                 :value {:name "nicola"}})
   {:forms {:myform {:value {:name "nicola"}}}})

  (def db {:forms {:myform {:value {:name "nicola"}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :name :name}
                  "nicola+")
   {:forms {:myform {:value {:name "nicola+"}
                     :dirty true
                     :touched true
                     :state {:name {:touched true :dirty true}}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :name :email}
                  "nicola@mail.com")
   {:forms {:myform {:value {:email "nicola@mail.com"
                             :name "nicola"}
                     :dirty true
                     :touched true
                     :state {:email {:touched true :dirty true}}}}})
  )



(deftest validation-logic
  (def db {:forms {:myform {:value {:name "nicola"}
                            :meta {:properties {:name {:validators {:not-blank true
                                                                    :min-length 5}}}}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :name :name} "")
   {:forms {:myform {:value {:name ""}
                     :state {:name {:errors {:not-blank "Could not be blank!"
                                             :min-length "Lenght should be more then 5"}}}}}}))

(deftest nested-form-logic
  (def db {:forms {:myform {:value {:address {:city "LA"}}
                            :meta {:properties {:address {:properties {:city {:validators {:not-blank true
                                                                                           :min-length 5}}}}}}}}})

  #_(matcho/match
   (sut/get-manifest db {:form {:path [:forms :myform]} :path [:address] :name :city})
   {:validators {:not-blank true}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :path [:address] :name :city} "")
   {:forms {:myform {:value {:address {:city ""}}
                     :state {:address {:city {:errors {:not-blank "Could not be blank!"
                                                       :min-length "Lenght should be more then 5"}}}}}}})

  (matcho/match
   (let [opts {:form {:path [:forms :myform]} :path [:address] :name :city}]
     (-> db
         (sut/on-change opts "")
         (sut/get-errors opts)))

   {:not-blank "Could not be blank!"
    :min-length "Lenght should be more then 5"})


  )

(deftest collection-form-logic
  (def db {:forms {:myform {:value {}
                            :meta {:properties {:address {:items {:properties {:city {:validators {:not-blank true
                                                                                                   :min-length 5}}}}}}}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :path [:address 0] :name :city} "LA")
   {:forms {:myform {:value {:address [{:city "LA"}]}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :path [:address 0] :name :city} "")
   {:forms {:myform {:value {:address [{:city ""}]}
                     :state {:address [{:city {:errors {:not-blank "Could not be blank!"
                                                       :min-length "Lenght should be more then 5"}}}]}}}})

  )
