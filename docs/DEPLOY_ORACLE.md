# Deploy CabsNow on Oracle Cloud Free VM

Complete guide to host the **full stack** (MySQL + Redis + Kafka + Backend + Frontend) on Oracle Cloud Always Free tier.

**Result:** `http://YOUR_VM_IP` — single URL for app, API, and WebSocket.

---

## What you get

| Service | Access |
|---------|--------|
| React app | `http://YOUR_VM_IP/` |
| REST API | `http://YOUR_VM_IP/api/...` |
| WebSocket | `http://YOUR_VM_IP/ws` |
| Swagger | `http://YOUR_VM_IP/swagger-ui.html` |

MySQL, Redis, Kafka are **internal only** (not exposed to internet).

---

## Part 1 — Oracle Cloud Console (one-time)

### Step 1: Create Oracle Cloud account

1. Go to [https://www.oracle.com/cloud/free/](https://www.oracle.com/cloud/free/)
2. Sign up (credit card for verification — **Always Free** resources won't charge if you stay in free limits)
3. Complete email / phone verification

### Step 2: Create a VM instance

1. Menu → **Compute** → **Instances** → **Create instance**
2. **Name:** `cabsnow-demo`
3. **Image:** Ubuntu 22.04 or 24.04 (aarch64 / ARM — Always Free eligible)
4. **Shape:** `VM.Standard.A1.Flex` — **1 OCPU, 6 GB RAM** (enough for demo; max free is 4 OCPU / 24 GB)
5. **Networking:** Use default VCN
6. **Assign public IP:** ✅ Yes
7. **SSH keys:** Generate or upload your public key → **Download private key** (`.key` file)
8. Click **Create**

Wait until state = **Running**. Note the **Public IP address** (e.g. `129.146.xxx.xxx`).

### Step 3: Open firewall ports (Security List)

1. On instance page → click **Subnet** link → **Security List**
2. **Add Ingress Rules:**

| Source CIDR | Protocol | Dest Port | Description |
|-------------|----------|-----------|-------------|
| `0.0.0.0/0` | TCP | 22 | SSH |
| `0.0.0.0/0` | TCP | 80 | HTTP (app) |

> Do **not** open 3306, 6379, 8080, 9092 to the internet.

---

## Part 2 — SSH into VM

From your Mac terminal:

```bash
chmod 400 ~/Downloads/ssh-key-*.key
ssh -i ~/Downloads/ssh-key-*.key ubuntu@YOUR_VM_PUBLIC_IP
```

Replace `ubuntu` with `opc` if you chose Oracle Linux instead of Ubuntu.

---

## Part 3 — Install Docker on VM

```bash
curl -fsSL https://raw.githubusercontent.com/SERVEcreative/CabsNow/feature/cabsnow-full-stack/deploy/oracle/setup-vm.sh | bash
```

Or manually from cloned repo:

```bash
bash deploy/oracle/setup-vm.sh
```

**Log out and SSH back in** (docker group):

```bash
exit
ssh -i ~/Downloads/ssh-key-*.key ubuntu@YOUR_VM_PUBLIC_IP
docker --version
```

---

## Part 4 — Deploy CabsNow

```bash
git clone https://github.com/SERVEcreative/CabsNow.git
cd CabsNow
git checkout feature/cabsnow-full-stack

cp .env.oracle.example .env
nano .env
```

Edit `.env` — **replace `YOUR_VM_PUBLIC_IP`** with your real IP:

```env
PUBLIC_APP_URL=http://129.146.xxx.xxx
MYSQL_ROOT_PASSWORD=MyStrongRootPass123!
MYSQL_PASSWORD=MyStrongDbPass123!
JWT_SECRET=RandomJwtSecretKeyAtLeast32CharactersLong!!
ADMIN_PASSWORD=AdminDemo@12345
```

Build and start (first run takes **5–10 minutes**):

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Watch logs:

```bash
docker compose -f docker-compose.prod.yml logs -f backend
```

Wait for: `Started Main in ... seconds`

Check all containers:

```bash
docker compose -f docker-compose.prod.yml ps
```

All should show `Up`.

---

## Part 5 — Open in browser

```
http://YOUR_VM_PUBLIC_IP
```

### Test accounts

| Role | How to test |
|------|-------------|
| Rider | Sign up on home page |
| Driver | **Incognito window** → Driver signup/login |
| Admin | `admin@cabsnow.com` / password from `ADMIN_PASSWORD` in `.env` |

### GPS demo on phone

Open `http://YOUR_VM_IP` on phone → Allow location.

> **Note:** Browser GPS works on HTTP for many devices, but some phones prefer HTTPS. For interview demo, laptop browser is fine.

---

## Useful commands on VM

```bash
# Restart everything
docker compose -f docker-compose.prod.yml restart

# Rebuild after git pull
git pull
docker compose -f docker-compose.prod.yml up -d --build

# View logs
docker compose -f docker-compose.prod.yml logs -f nginx backend

# Stop
docker compose -f docker-compose.prod.yml down

# Stop and delete DB (fresh start)
docker compose -f docker-compose.prod.yml down -v
```

---

## Troubleshooting

### Page not loading (connection timeout)

1. **Oracle Security List** — port 80 open? (Part 1 Step 3)
2. **VM iptables** — run on VM:
   ```bash
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
   ```
3. Check nginx: `docker compose -f docker-compose.prod.yml logs nginx`

### Backend crash / Flyway error

```bash
docker compose -f docker-compose.prod.yml logs backend
```

Fresh DB: `docker compose -f docker-compose.prod.yml down -v` then up again.

### WebSocket not connecting

- Open browser DevTools → Network → WS — should connect to `http://YOUR_VM_IP/ws`
- Ensure nginx container is running

### Out of memory (Kafka)

If VM has only 1 GB RAM, disable Kafka in `.env` by editing `docker-compose.prod.yml` backend env:

```yaml
KAFKA_ENABLED: "false"
REDIS_ENABLED: "false"
```

Or use larger shape (6 GB RAM recommended for full stack).

### CORS errors

Ensure `PUBLIC_APP_URL` in `.env` matches exactly how you open the app (no trailing slash):

```env
PUBLIC_APP_URL=http://129.146.xxx.xxx
```

---

## Architecture on VM

```
Internet :80
    │
    ▼
┌─────────┐     ┌──────────┐     ┌───────┐
│  nginx  │────►│ backend  │────►│ mysql │
│ (React) │     │ :8080    │     └───────┘
└─────────┘     │          │     ┌───────┐
                │          │────►│ redis │
                │          │     └───────┘
                │          │     ┌───────┐
                └──────────┘────►│ kafka │
                                 └───────┘
```

---

## Optional: HTTPS (later)

For production HTTPS use **Caddy** or **Certbot** with a domain name pointing to VM IP. Free Oracle VM has no built-in domain — buy a cheap domain (~₹100/yr) or use interview demo on HTTP.

---

## Interview one-liner

> "I deployed the full Docker Compose stack on Oracle Cloud Always Free ARM VM with nginx reverse proxy — single public endpoint for React, REST API, and STOMP WebSocket."

---

## Quick checklist

- [ ] Oracle VM running (Ubuntu ARM)
- [ ] Security List: ports 22 + 80 open
- [ ] Docker installed (`docker compose version`)
- [ ] `.env` filled with VM public IP + strong passwords
- [ ] `docker compose -f docker-compose.prod.yml up -d --build`
- [ ] Backend log shows `Started Main`
- [ ] Browser opens `http://VM_IP`
- [ ] Rider + Driver (incognito) test ride flow

Good luck with your demo.
