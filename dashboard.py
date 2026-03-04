from flask import Flask, render_template, request, redirect, url_for, session
import os

app = Flask(__name__)

# 🔐 Secret Key (Better to use env variable in production)
app.secret_key = os.environ.get("SECRET_KEY", "sentinel_ultra_private_key")

# --- 👤 Credentials ---
# Default: admin / admin123
ADMIN_USER = os.environ.get("ADMIN_USER", "admin")
ADMIN_PASS = os.environ.get("ADMIN_PASS", "admin123")

# --- ⚙️ Global Configuration ---
config = {
    "prefix": "!",
    "nickname": "Sentinel Bot",
    "maintenance": "OFF",
    "log_channel": "",
    "mute_role": "ON",
    "admin_role_id": "",
    "mod_role_id": "",
    "anti_spam": "ON",
    "block_links": "OFF",
    "yt_notifier": "OFF",
    "fb_notifier": "OFF",
    "xp_rate": "10",
    "welcome_system": "OFF",
    "welcome_channel": "",
    "welcome_msg": "Hey {user}, welcome!",
    "welcome_gifs": "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNHJueGZ3bmZ3bmZ3/l0MYC0LajBaCEgizu/giphy.gif"
}

# --- 🚪 Login/Logout Routes ---

@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')

        if username != ADMIN_USER or password != ADMIN_PASS:
            error = 'Invalid Credentials. Please try again.'
        else:
            session['logged_in'] = True
            return redirect(url_for('index'))

    return render_template('login.html', error=error)

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

# --- 🌐 Main Dashboard Route ---

@app.route('/')
def index():
    if not session.get('logged_in'):
        return redirect(url_for('login'))
    return render_template('index.html', config=config)

@app.route('/update', methods=['POST'])
def update():
    if not session.get('logged_in'):
        return redirect(url_for('login'))
        
    global config

    # 📝 Text fields
    fields = [
        "prefix", "nickname", "log_channel",
        "admin_role_id", "mod_role_id",
        "xp_rate", "welcome_channel",
        "welcome_msg", "welcome_gifs"
    ]

    for field in fields:
        config[field] = request.form.get(field, config[field])

    # 🔘 Toggle fields
    toggles = [
        "maintenance", "mute_role", "anti_spam",
        "block_links", "yt_notifier",
        "fb_notifier", "welcome_system"
    ]

    for toggle in toggles:
        config[toggle] = "ON" if request.form.get(toggle) else "OFF"

    print(f"📡 Settings Updated by Admin: {config}")
    return redirect(url_for('index'))

# --- 🚀 Runner ---
def run_dashboard():
    port = int(os.environ.get("PORT", 8000))
    app.run(host='0.0.0.0', port=port)

if __name__ == "__main__":
    run_dashboard()
