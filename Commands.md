Set JAVA 17 in terminal:
 set JAVA_HOME=C:\Program Files\Java\jdk-17         
 set PATH=%JAVA_HOME%\bin;%PATH%   
or
 $env:JAVA_HOME="C:\Program Files\Java\jdk-17"   
 $env:PATH="$env:JAVA_HOME\bin;$env:PATH"  

Start Postgresql Server:
  (On WSL)
  sudo service postgresql start
  sudo service postgresql restart

  psql -U postgres postgres

Start Redis Server:
  (On WSL)
  sudo service redis-server start

Start Kafka Server:
  (On WSL)
  cd ~/kafka_2.13-4.1.1
  bin/kafka-server-start.sh config/server.properties

POSTMAN substitute:
  Using curl in WSL

Start Nginx:
  cd eventverse/backend
  nginx -c $(pwd)/nginx/nginx.conf

Docker:
  cd eventverse/backend
  docker compose up --build
  docker compose build
  docker compose up -d
  docker compose down 
  (don't you -v it will clear postgres storage)