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


      (testing "Insert into nil"
        (match
         (sut/setin nil
                    [:telecom {:get [:= :system "phone"]
                               :set {:system "phone" :value nil}} :value]
                    "+7(912)-123-45-67")
         {:telecom vector? }))

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
