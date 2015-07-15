apt-get update
apt-get install -y linux-image-generic-lts-trusty
wget -qO- https://get.docker.com/gpg | sudo apt-key add -
wget -qO- https://get.docker.com/ | sh

apt-get install -y default-jre
apt-get install -y default-jdk

apt-get install -y maven
#:wapt-get install -y tomcat7
