(ns re-form.shared-test
  (:require [re-form.shared :as sut]
            [clojure.test :refer :all]
            [matcho.core :refer [match]]))

(def data
  {:resourceType "Patient"
   :id "patient-id"
   :contact [{:telecom [{:system "phone"
                         :value "8800100200"}
                        {:system "sms"
                         :use "work"
                         :value "88888900000"}
                        {:system "fax"
                         :value "+78219090"}]}]})

(deftest path-test
  (testing "Get value"
    (match
     (sut/getin [{:foo  "bar"
                  :value [{:bar "tar"
                           :system "42"}]}]
                  [{:get [:= :foo "bar"]}
                   :value
                   {:get [:= :bar "tar"]}
                   :system])
     "42")

    (match
     (sut/getin [{:foo {:bar {:baz "keywords seq"}}
                    :value 12}]
                  [{:get [:= "keywords seq" [:foo :bar :baz]]} :value])
     12)


    (match
     (sut/getin [{:foo 42 :value "42"}
                   {:value "10"}
                   {:foo -10 :value "-10"}]
                  [{:get [:not :foo]} :value])
     "10")


    (match
     (sut/getin [{:foo 42 :value "42"}
                   {:foo 10 :value "10"}
                   {:foo -10 :value "-10"}]
                  [{:get [:> :foo 20]} :value])
     "42")
    (match
     (sut/getin [{:foo 42 :value "42"}
                   {:foo 10 :value "10"}
                   {:foo -10 :value "-10"}]
                  [{:get [:> 0 :foo]} :value])
     "-10")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:= :system "phone"]
                                           :set {:system "phone" :value ""}} :value])
     "8800100200")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:= "fax" :system]
                                             :set {:system "phone" :value ""}} :value])
     "+78219090")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:and
                                                   [:= :system "sms"]
                                                   [:= "work" :use]]
                                            :set {:system "sms"
                                                  :use "work"
                                                  :value ""}} :value])
     "88888900000")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:= "email" :system]
                                             :set {:system "email" :value "superman@batma.com"}} :value])
     "superman@batma.com")
    (match
     (sut/getin data [:contact 0 :telecom {:get [:and
                                                   [:= :system "sms"]
                                                   [:= "home" :use]]
                                             :set {:system "sms"
                                                   :use "home"
                                                   :value "home_sms"}} :value])
     "home_sms")

    (match
     (sut/getin data [:contact 0 :telecom {:get [:or
                                                   [:= :system "fake_system"]
                                                   [:= "work" :use]]
                                             :set {:system "fake_system"
                                                   :use "work"
                                                   :value "home_sms"}} :value])
     "88888900000")


    )

  (testing "Set value"

    (testing "Simple insert"
      (match
       (sut/setin {:telecom "12"}
                  [:telecom ]
                  "42")
       {:telecom "42"})

      (match
       (sut/setin {:telecom {:system "phone"}}
                  [:telecom :system]
                  "email")
       {:telecom {:system "email"}})

      (match
       (sut/setin {:telecom {:system ["phone" "email" "fax"]}}
                  [:telecom :system 1]
                  "postal")
       {:telecom {:system ["phone" "postal" "fax"]}})


      (match
       (sut/setin nil
                  [:telecom :system 1]
                  "postal")
       {:telecom {:system [nil "postal"]}}))

    (testing "Insert by search"
      (testing "Insert exists item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :value nil}} :value]
                    "+7(912)-123-45-67")
         {:telecom [{:system "phone" :value "+7(912)-123-45-67"}]}))

      (testing "Insert new item item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "email"]
                               :set {:system "email" :rank 1 :value nil}} :value]
                    "e@mail.com")
         {:telecom [{:system "phone" :use "work" :value "+7(911)-189-12-12"}
                    {:system "email" :rank 1 :value "e@mail.com"}
                    ]}))

      (testing "Merge with existing item"
        (match
         (sut/setin {:telecom [{:system "phone" :use "work" :rank 1 :value "+7(911)-189-12-12"}]}
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :rank 2 :value nil}} :value]
                    "8800100200")
         {:telecom [{:system "phone" :use "work" :rank 1 :value "8800100200"}]}))


      )

    ))

#_(deftest basic-logic
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



#_(deftest validation-logic
  (def db {:forms {:myform {:value {:name "nicola"}
                            :meta {:properties {:name {:validators {:not-blank true
                                                                    :min-length 5}}}}}}})

  (matcho/match
   (sut/on-change db {:form {:path [:forms :myform]} :name :name} "")
   {:forms {:myform {:value {:name ""}
                     :state {:name {:errors {:not-blank "Could not be blank!"
                                             :min-length "Lenght should be more then 5"}}}}}}))

#_(deftest nested-form-logic
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

#_(deftest collection-form-logic
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
