cd ~/Documents/datomic/datomic-pro-1.0.6344
#bin/run -m datomic.peer-server -h localhost -p 8998 -a myaccesskey,mysecret -d hello,datomic:mem://hello
bin/run -m datomic.peer-server -h localhost -p 8998 -a myaccesskey,mysecret -d datomic,datomic:sql://datomic?jdbc:mysql://localhost:3306/datomic?user=datomic\&password=datomic