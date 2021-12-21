(ns datomic1
  (:require [datomic.client.api :as d]))

(def config {:server-type        :peer-server
             :access-key         "myaccesskey"
             :secret             "mysecret"
             :endpoint           "localhost:8998"
             :validate-hostnames false})

(def client (d/client config))
(def conn (d/connect client {:db-name "hello"}))

(defn make-idents
  [x]
  (mapv #(hash-map :db/ident %) x))

(def schema-1
  [{:db/ident :inv/sku
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/color
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/size
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :inv/type
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}])

;;; Defined earlier, but repeated for clarity
(def colors [:red :green :blue :yellow])
(def sizes [:small :medium :large :xlarge])
(def types [:shirt :pants :dress :hat])

(def sample-data
  (->> (for [color colors
             size sizes
             type types]
         {:inv/color color
          :inv/size size
          :inv/type type})
       (map-indexed
         (fn [idx m]
           (assoc m :inv/sku (str "SKU-" idx))))
       vec))

;; db api returns the latest database value from a connection
(def db (d/db conn))

(comment
  (d/transact conn {:tx-data (make-idents sizes)})
  (d/transact conn {:tx-data (make-idents types)})
  (d/transact conn {:tx-data (make-idents colors)})
  (d/transact conn {:tx-data schema-1})
  (d/transact conn {:tx-data sample-data})

  (d/pull db
          [{:inv/color [:db/ident]}
           {:inv/size [:db/ident]}
           {:inv/type [:db/ident]}]
          [:inv/sku "SKU-46"])

  (d/q '[:find ?sku
         :where
         [?e :inv/sku "SKU-42"]
         [?e :inv/color ?specific-color]
         [?anything :inv/sku ?sku]
         [?anything :inv/color ?specific-color]]
       db)

  (d/q '[:find ?color
         :where [_ :inv/color ?color]]
       db)
  ,)