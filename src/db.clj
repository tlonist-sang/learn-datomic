(ns db
  (:require [datomic.client.api :as d]))

(def config {:server-type        :peer-server
             :access-key         "myaccesskey"
             :secret             "mysecret"
             :endpoint           "localhost:8998"
             :validate-hostnames false})

(def client (d/client config))
(def conn (d/connect client {:db-name "hello"}))


;; fields:table = attribute:entity
;; :db/ident = unique name for attribute
;; :db/valueType = type of data that can be stored in the attribute
;; :db/cardinality = whether the attribute stores a single value or a collection of values
;; :db/doc = docstring of the attribute to be queried for later

(def movie-schema [{:db/ident       :movie/title
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The title of the movie"}

                   {:db/ident       :movie/genre
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The genre of the movie"}

                   {:db/ident       :movie/release-year
                    :db/valueType   :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc         "The year the movie was released in theaters"}])

(def first-movies [{:movie/title        "The Goonies"
                    :movie/genre        "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title        "Commando"
                    :movie/genre        "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title        "Repo Man"
                    :movie/genre        "punk dystopia"
                    :movie/release-year 1984}])

(def db (d/db conn))

(comment
  (d/transact conn {:tx-data movie-schema})
  (d/transact conn {:tx-data first-movies})

  (def all-movies-query '[:find ?e
                          :where [?e :movie/title] [?e :movie/genre] [?e :movie/release-year]])

  (def all-titles-query '[:find ?movie-title
                          :where [_ :movie/title ?movie-title]])

  (def titles-from-1985 '[:find ?title
                          :where [?e :movie/title ?title]
                          [?e :movie/release-year 1985]])

  (def all-data-from-1985 '[:find ?title ?year ?genre
                            :where [?e :movie/title ?title]
                            [?e :movie/release-year ?year]
                            [?e :movie/genre ?genre]
                            [?e :movie/release-year 1985]])

  (d/q '[:find ?e
         :where [?e :movie/title "Commando"]]
       db)

  (d/q all-movies-query db)
  (d/q all-titles-query db)
  (d/q titles-from-1985 db)
  (d/q all-data-from-1985 db)

  (def commando-id
    (ffirst (d/q '[:find ?e
                   :where [?e :movie/title "Commando"]]
                 db)))

  (d/transact conn {:tx-data [{:db/id commando-id :movie/genre "future governor"}]})
  (def old-db (d/as-of db 1004))

  (d/q all-data-from-1985 old-db)
  (def hdb (d/history db))

  (d/q '[:find ?genre
         :where [?e :movie/title "Commando"]
         [?e :movie/genre ?genre]]
       hdb)

  ,)
