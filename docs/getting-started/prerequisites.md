# Prerequisites

This guide covers all the software and tools you need to install before developing Taskava.

## Required Software

### 1. Operating System Requirements

**Supported Operating Systems:**
- macOS 11+ (Big Sur or later)
- Ubuntu 20.04 LTS or later
- Windows 10/11 with WSL2
- Any Linux distribution with Docker support

**Minimum Hardware:**
- 8GB RAM (16GB recommended)
- 20GB free disk space
- 4-core CPU (Apple Silicon or Intel)

### 2. Docker Desktop

Docker is essential for running local services (database, cache, etc.).

**Installation:**

```bash
# macOS
brew install --cask docker

# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Windows
# Download from https://docs.docker.com/desktop/install/windows-install/
```

**Verify Installation:**
```bash
docker --version
docker-compose --version
docker run hello-world
```

**Configuration:**
- Allocate at least 4GB RAM to Docker
- Enable Kubernetes (optional)
- Configure file sharing for your project directory

### 3. Java Development Kit (JDK) 17

The backend requires Java 17 or later.

**Installation Options:**

```bash
# macOS with Homebrew
brew install openjdk@17
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Using SDKMAN (recommended for version management)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.9-tem
```

**Set JAVA_HOME:**
```bash
# macOS/Linux - Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Verify
java -version
javac -version
```

### 4. Node.js and npm

Frontend development requires Node.js 18 or later.

**Installation Options:**

```bash
# Using Node Version Manager (NVM) - Recommended
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18
nvm alias default 18

# macOS with Homebrew
brew install node

# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

**Verify Installation:**
```bash
node --version  # Should be 18.x or higher
npm --version   # Should be 9.x or higher
```

### 5. Git

Version control is managed with Git.

```bash
# macOS
brew install git

# Ubuntu/Debian
sudo apt-get install git

# Windows
# Download from https://git-scm.com/download/win
```

**Initial Configuration:**
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --global init.defaultBranch main
```

### 6. Maven (Optional)

The project includes Maven wrapper, but you can install it globally.

```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt-get install maven

# Verify
mvn -version
```

## Recommended Development Tools

### 1. Integrated Development Environments (IDEs)

**For Java Development:**

**IntelliJ IDEA** (Recommended)
- Download: https://www.jetbrains.com/idea/
- Community Edition is free and sufficient
- Ultimate Edition includes Spring support

Recommended Plugins:
- Spring Boot
- Lombok
- Docker
- Database Tools
- SonarLint

**For Full-Stack Development:**

**Visual Studio Code**
- Download: https://code.visualstudio.com/

Essential Extensions:
```bash
# Install via command line
code --install-extension vscjava.vscode-java-pack
code --install-extension vscjava.vscode-spring-boot-dashboard
code --install-extension dbaeumer.vscode-eslint
code --install-extension esbenp.prettier-vscode
code --install-extension bradlc.vscode-tailwindcss
code --install-extension dsznajder.es7-react-js-snippets
code --install-extension ms-vscode.vscode-typescript-react-plugin
```

### 2. Database Management Tools

Choose one of the following:

**DBeaver** (Free, Cross-platform)
```bash
# macOS
brew install --cask dbeaver-community

# Ubuntu
sudo snap install dbeaver-ce
```

**TablePlus** (Paid, macOS/Windows)
- Modern UI
- Multi-tab interface
- Download: https://tableplus.com/

**pgAdmin** (Free, PostgreSQL-specific)
```bash
# Already included in docker-compose.yml
# Access at http://localhost:5050
```

### 3. API Testing Tools

**Postman** (Recommended)
- Download: https://www.postman.com/downloads/
- Import the included Postman collection

**Insomnia**
- Download: https://insomnia.rest/download

**VS Code REST Client**
```bash
code --install-extension humao.rest-client
```

### 4. Browser Extensions

**React Developer Tools**
- Chrome: https://chrome.google.com/webstore/detail/react-developer-tools/
- Firefox: https://addons.mozilla.org/en-US/firefox/addon/react-devtools/

**Redux DevTools** (if using Redux)
- Chrome: https://chrome.google.com/webstore/detail/redux-devtools/

### 5. Terminal Tools

**Oh My Zsh** (macOS/Linux)
```bash
sh -c "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```

**Windows Terminal** (Windows)
- Download from Microsoft Store

## Optional Tools

### 1. Kubernetes Tools (for production-like testing)

```bash
# kubectl
brew install kubectl

# k9s (Kubernetes CLI UI)
brew install k9s

# Lens (Kubernetes IDE)
brew install --cask lens
```

### 2. AWS CLI (for AWS deployment)

```bash
# macOS
brew install awscli

# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configure
aws configure
```

### 3. Terraform (for infrastructure)

```bash
# macOS
brew install terraform

# Linux
wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform
```

### 4. Performance Testing Tools

```bash
# Apache Bench
sudo apt-get install apache2-utils

# JMeter
brew install jmeter

# k6
brew install k6
```

## System Configuration

### 1. Increase File Descriptors (macOS/Linux)

```bash
# Check current limit
ulimit -n

# Increase temporarily
ulimit -n 10000

# Increase permanently - Add to ~/.bashrc or ~/.zshrc
echo "ulimit -n 10000" >> ~/.bashrc
```

### 2. Configure DNS (for local development)

Add to `/etc/hosts`:
```
127.0.0.1 taskava.local
127.0.0.1 api.taskava.local
```

### 3. Environment Variables

Create a `.env` file in your home directory:
```bash
# ~/.env
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export M2_HOME=~/.m2
export NODE_ENV=development
export DOCKER_DEFAULT_PLATFORM=linux/amd64  # For Apple Silicon
```

## Verification Script

Create a script to verify all prerequisites:

```bash
#!/bin/bash
# save as check-prerequisites.sh

echo "Checking prerequisites for Taskava development..."
echo "================================================"

# Check OS
echo -n "Operating System: "
uname -s

# Check Docker
echo -n "Docker: "
if command -v docker &> /dev/null; then
    docker --version
else
    echo "NOT INSTALLED ❌"
fi

# Check Docker Compose
echo -n "Docker Compose: "
if command -v docker-compose &> /dev/null; then
    docker-compose --version
else
    echo "NOT INSTALLED ❌"
fi

# Check Java
echo -n "Java: "
if command -v java &> /dev/null; then
    java -version 2>&1 | head -n 1
else
    echo "NOT INSTALLED ❌"
fi

# Check Node
echo -n "Node.js: "
if command -v node &> /dev/null; then
    node --version
else
    echo "NOT INSTALLED ❌"
fi

# Check npm
echo -n "npm: "
if command -v npm &> /dev/null; then
    npm --version
else
    echo "NOT INSTALLED ❌"
fi

# Check Git
echo -n "Git: "
if command -v git &> /dev/null; then
    git --version
else
    echo "NOT INSTALLED ❌"
fi

# Check Maven (optional)
echo -n "Maven (optional): "
if command -v mvn &> /dev/null; then
    mvn -version | head -n 1
else
    echo "Not installed (using wrapper)"
fi

# Check available memory
echo -n "Available Memory: "
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "$(( $(sysctl -n hw.memsize) / 1024 / 1024 / 1024 )) GB"
else
    echo "$(free -h | awk '/^Mem:/ {print $2}')"
fi

# Check disk space
echo -n "Free Disk Space: "
df -h . | awk 'NR==2 {print $4}'

echo "================================================"
echo "Setup complete! You're ready to start developing Taskava."
```

## Troubleshooting Common Installation Issues

### Java Version Conflicts

```bash
# List all Java versions (macOS)
/usr/libexec/java_home -V

# Switch Java version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Ubuntu/Debian
sudo update-alternatives --config java
```

### Node Version Issues

```bash
# Clear npm cache
npm cache clean --force

# Reinstall node modules
rm -rf node_modules package-lock.json
npm install
```

### Docker Permission Issues (Linux)

```bash
# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Test without sudo
docker run hello-world
```

### Port Conflicts

Check and kill processes using required ports:
```bash
# Check port usage
lsof -i :8080
lsof -i :5432
lsof -i :5173

# Kill process using port
kill -9 $(lsof -t -i:8080)
```

## Next Steps

Once all prerequisites are installed:

1. [Clone the repository](./quick-start.md)
2. [Set up local development](./local-development.md)
3. [Explore the codebase](../development/backend-guide.md)
4. [Start contributing](../../CONTRIBUTING.md)

For questions or issues with prerequisites, please check our [Troubleshooting Guide](../operations/troubleshooting.md) or open a GitHub issue.