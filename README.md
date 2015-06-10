Webservice API Project:
	archive: weather-demo_v.1.0_20150421-1.zip

	環境：
		Java 6
		Tomcat 6
		Eclipse 4.3 專案
		使用Maven管理library相依性

	匯入：
		1. File -> Import -> General: Existing Projects into Workspace
		2. Select archive file: 指定檔案所在
		3. 下方對話框會列出該檔案裡的專案
		4. 選取專案 weather demo v.1.0
		5. 匯入即可

	匯出為webapp：
		1. File -> Export -> Web: WAR file
		2. Web project 選擇專案
		3. Destination 選擇匯出目錄與檔名
		[4,5步驟為有設定 server runtime 時可使用]
		4. 勾選 Optimize for a specific server runtime
		5. 選擇目標 runtime (如：tomcat 6)
		[4,5步驟為有設定 server runtime 時可使用]
		6. Finish，即將專案打包成為一個 webapp 檔(war檔)

	API設定檔： res/com.jfetek.demo.weather/system.setup
		程式使用目錄與資料庫連線設定等都可以在裡面修改
		注意！
			每次啟動程式會找程式執行目錄內是否有 system.setup 檔案
			如果沒有，則會產生 system.setup 檔到執行目錄
			一般tomcat執行目錄即為安裝目錄之下的 bin/ 
			修改該檔案內容，再重啟程式即會讀入新的設定值


Webservice API webapp:
	file: weather-demo_v.1.0_20150421-1.war

	使用方法：
		放到tomcat的webapps目錄之下，即會自動解開並發布為webapp
		webapp名稱即war檔的名稱，比如 aaa.war 即會發布到網站根目錄之下 aaa 目錄
		即 /aaa/
		如果想要發布到網站根目錄，war檔名改為 ROOT.war 即會發布到網站根目錄
		即 /


Spider Project
	file: bigdata-spider_v.1.0_20150421-1.zip

	環境：
		Java 6
		Tomcat 6
		Eclipse 4.3 專案
		使用Maven管理library相依性
		MongoDB

	匯入：
		1. File -> Import -> General: Existing Projects into Workspace
		2. Select archive file: 指定檔案所在
		3. 下方對話框會列出該檔案裡的專案
		4. 選取專案 proj. bigdata spider
		5. 匯入即可

	打包：
		1. File -> Export -> Java: runnable JAR file
		2. Launch configuration: 選擇spider專案
		3. Export destination: 選擇輸出目錄與檔名
		4. Library handling: Extract required libraries into generated JAR
		5. Finish即可

	Spider設定檔： src/main/resources/com.jfetek.demo.weather/system.setup
		Spider使用目錄與資料庫連線設定等都可以在裡面修改
		注意！
			每次啟動程式會找程式執行目錄內是否有 system.setup 檔案
			如果沒有，則會產生 system.setup 檔到執行目錄
			執行目錄即為執行spider的目錄
			修改該檔案內容，再重啟程式即會讀入新的設定值

	Wiki Spider 執行方法：
		java -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders wiki
		或參考 wiki.sh 內容

	Weather Spider 執行方法：
		java -cp ./Spider.jar com.jfetek.demo.weather.spider.Spiders weather
		或參考 weather.sh 內容
		

### Task Database

  Program Path: /root/spders
  Data Path: /data/spiders


# Appendix

### /etc/init/mongod.conf

```sh
# Ubuntu upstart file at /etc/init/mongod.conf

# Recommended ulimit values for mongod or mongos
# See http://docs.mongodb.org/manual/reference/ulimit/#recommended-settings
#
limit fsize unlimited unlimited
limit cpu unlimited unlimited
limit as unlimited unlimited
limit nofile 64000 64000
limit rss unlimited unlimited
limit nproc 32000 32000

kill timeout 300 # wait 300s between SIGTERM and SIGKILL.

pre-start script
	mkdir -p /var/lib/mongodb/
	chown mongodb:mongodb /var/lib/mongodb/ -R
	mkdir -p /data2/mongodb/
	chown mongodb:mongodb /data2/mongodb/ -R
	mkdir -p /var/log/mongodb/data2/
	chown mongodb:mongodb /var/log/mongodb/data2/ -R
end script

start on runlevel [2345]
stop on runlevel [06]

script
  ENABLE_MONGOD="yes"
  CONF=/etc/mongod2.conf
  PID=/var/lib/mongodb/mongod2.pid
  NAME=mongod2
  DAEMON=/usr/bin/mongod
  DAEMONUSER=${DAEMONUSER:-mongodb}

  if [ -f /etc/default/mongod ]; then . /etc/default/mongod; fi

  # Handle NUMA access to CPUs (SERVER-3574)
  # This verifies the existence of numactl as well as testing that the command works
  NUMACTL_ARGS="--interleave=all"
  if which numactl >/dev/null 2>/dev/null && numactl $NUMACTL_ARGS ls / >/dev/null 2>/dev/null
  then
	NUMACTL="$(which numactl) -- $NUMACTL_ARGS"
	DAEMON_OPTS=${DAEMON_OPTS:-"--config $CONF"}
  else
	NUMACTL=""
	DAEMON_OPTS="-- "${DAEMON_OPTS:-"--config $CONF"}
  fi

  if [ "x$ENABLE_MONGOD" = "xyes" ]
  then
	exec start-stop-daemon --make-pidfile --pidfile $PID --start --chuid $DAEMONUSER --name $NAME --exec $NUMACTL $DAEMON $DAEMON_OPTS
  fi
end script
```