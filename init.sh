echo "🚀 Initializing Between Us Server Environment..."

sudo apt update
sudo apt upgrade

# Install dependencies
echo "\n\n\n"
echo "Installing dependencies...🏗️"
echo "Installing Java...☕"
sudo apt install default-jdk

echo "\n\n\n"
echo "Updating and installing Docker...🐳"
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update


sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "\n\n\n"

echo "Cloning Between Us Server repository...📦"
git clone https://github.com/Btw-Us/BetweenUsServer.git


echo "\n\n\n"
echo "Creating Folders for Databases 📂"

mkdir -p ${HOME}/databases/between-us/mysql
mkdir -p ${HOME}/databases/between-us/mongo1
mkdir -p ${HOME}/databases/between-us/mongo2
mkdir -p ${HOME}/databases/between-us/mongo3

sudo chown -R 999:999 ${HOME}/databases/between-us