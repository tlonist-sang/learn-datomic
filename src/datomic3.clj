(ns datomic3
  (:require [datomic.client.api :as d]
            [java-time :as jt]))

;; https://gist.github.com/a2ndrade/5234370
;; https://docs.datomic.com/on-prem/overview/storage.html#sql-database
;; https://docs.datomic.com/on-prem/getting-started/dev-setup.html#:~:text=(def%20db%2Duri%20%22datomic%3Adev%3A//localhost%3A4334/hello%22)

;;https://docs.datomic.com/on-prem/getting-started/dev-setup.html#:~:text=in%20this%20guide.-,Creating%20a%20database,-In%20a%20separate


(def config {:server-type        :peer-server
             :access-key         "myaccesskey"
             :secret             "mysecret"
             :endpoint           "localhost:8998"
             :validate-hostnames false})

(def client (d/client config))
(def conn (d/connect client {:db-name "datomic"}))

(def weather-hourly-schema [{:db/ident       :hourly/temperature
                             :db/valueType   :db.type/long
                             :db/cardinality :db.cardinality/one
                             :db/doc         "온도"}

                            {:db/ident       :hourly/rain
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "강수량"}

                            {:db/ident       :hourly/humidity
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "습도"}

                            {:db/ident       :hourly/wind_ew
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "동서 바람 세기"}

                            {:db/ident       :hourly/wind_ns
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "남북 바람 세기"}

                            {:db/ident       :hourly/wind_direction
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "바람 방향"}

                            {:db/ident       :hourly/wind_speed
                             :db/valueType   :db.type/float
                             :db/cardinality :db.cardinality/one
                             :db/doc         "바람 속도"}

                            {:db/ident       :hourly/rain_type
                             :db/valueType   :db.type/string
                             :db/cardinality :db.cardinality/one
                             :db/doc         "강수 타입"}

                            {:db/ident       :hourly/nx
                             :db/valueType   :db.type/long
                             :db/cardinality :db.cardinality/one
                             :db/doc         "x 좌표"}

                            {:db/ident       :hourly/ny
                             :db/valueType   :db.type/long
                             :db/cardinality :db.cardinality/one
                             :db/doc         "y 좌표"}

                            {:db/ident       :hourly/request_time
                             :db/valueType   :db.type/string
                             :db/cardinality :db.cardinality/one
                             :db/doc         "관측 시각"}

                            {:db/ident       :hourly/created_at
                             :db/valueType   :db.type/string
                             :db/cardinality :db.cardinality/one
                             :db/doc         "생성 시각"}])

(defn generate-rand-weather-info [_]
  (zipmap
    (map :db/ident weather-hourly-schema)
    [(rand-int 30) (* 10 (rand 1)) (* 100 (rand 1)) 0.0 0.0 0.0 0.0 "none" (+ 60 (rand-int 20)) (+ 100 (rand-int 50)) "2021-12-22 00:00:00" (jt/format "yyyy-MM-dd HH:mm:ss" (jt/local-date-time))]))

(comment
  (def db (d/db conn))
  (def rand-generated2
    (->> (iterate generate-rand-weather-info (generate-rand-weather-info ""))
         (take 20)
         vec))
  (d/transact conn {:tx-data weather-hourly-schema})
  (d/transact conn {:tx-data rand-generated})

  (d/q '[:find ?e ?v
         :where
         [?e :hourly/nx ?v]]
       db)

  (d/q '[:find ?nx ?ny ?temperature ?created_at
         :where
         [?e :hourly/nx ?nx]
         [?e :hourly/ny ?ny]
         [?e :hourly/temperature ?temperature]
         [?e :hourly/created_at ?created_at]
         [(<= ?temperature 15)]]
       db)

  (d/transact conn {:tx-data rand-generated2})
  ;; 난 persist 했는데 왜 값이 그대로지?
  ;; db 도 불변객체, 새로 가져와야 함.

  (d/q '[:find ?nx ?ny ?temperature ?created_at
         :where
         [?e :hourly/nx ?nx]
         [?e :hourly/ny ?ny]
         [?e :hourly/temperature ?temperature]
         [?e :hourly/created_at ?created_at]
         [(<= ?temperature 15)]]
       (d/db conn))

  (def hdb (d/history db))
  (def old-db (d/as-of db 1001))

  (d/q '[:find ?temperature
         :where
         [?e :hourly/nx ?nx]
         [?e :hourly/ny ?ny]
         [?e :hourly/temperature ?temperature]
         [?e :hourly/created_at ?created_at]
         [(= 60 ?nx)]
         [(= 121 ?ny)]]
       hdb)

  ,)
