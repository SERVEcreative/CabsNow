#!/usr/bin/env bash
# Run on a fresh Ubuntu 22.04/24.04 Oracle Cloud VM (after SSH login).
set -euo pipefail

echo "==> Installing Docker..."
sudo apt-get update -qq
sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -qq
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker "$USER"

echo "==> Opening port 80 in iptables (Oracle VM default rules)..."
if sudo iptables -C INPUT -p tcp --dport 80 -j ACCEPT 2>/dev/null; then
  echo "Port 80 rule already exists"
else
  sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT || true
fi

echo ""
echo "Docker installed. IMPORTANT:"
echo "  1. Log out and SSH back in (docker group)"
echo "  2. Clone repo: git clone https://github.com/SERVEcreative/CabsNow.git"
echo "  3. cd CabsNow && git checkout feature/cabsnow-full-stack"
echo "  4. cp .env.oracle.example .env && nano .env"
echo "  5. docker compose -f docker-compose.prod.yml up -d --build"
echo ""
echo "App URL: http://YOUR_VM_PUBLIC_IP"
