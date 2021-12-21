(ns datomic2
  (:require [datomic.client.api :as d]))

(def config {:server-type        :peer-server
             :access-key         "myaccesskey"
             :secret             "mysecret"
             :endpoint           "localhost:8998"
             :validate-hostnames false})

(def client (d/client config))
(def conn (d/connect client {:db-name "hello"}))
(def order-schema
  [{:db/ident :order/items
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true}
   {:db/ident :item/id
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :item/count
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(def add-order
  {:order/items
   [{:item/id [:inv/sku "SKU-25"]
     :item/count 10}
    {:item/id [:inv/sku "SKU-26"]
     :item/count 20}]})

(comment
  (d/transact conn {:tx-data order-schema})
  (d/transact conn {:tx-data [add-order]}))
