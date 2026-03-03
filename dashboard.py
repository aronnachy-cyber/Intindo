from flask import Flask, render_template, request, redirect, url_for
import os

app = Flask(__name__)

# বটের গ্লোবাল কনফিগারেশন - যা ড্যাশবোর্ড থেকে আপডেট হবে
config = {
    "prefix": "!",
    "nickname": "Sentinel Bot",
    "maintenance": "OFF",
    "log_channel": "",
    "mute_role": "ON",
    "anti_spam": "ON",
    "block_links": "OFF",
    "xp_rate": "10"
}

@app.route('/')
def index():
    # index.html ফাইলে বর্তমান কনফিগ পাঠানো হচ্ছে
    return render_template('index.html', config=config)

@app.route('/update', methods=['POST'])
def update():
    global config
    # ড্যাশবোর্ড থেকে আসা ডেটা রিসিভ করা
    config["prefix"] = request.form.get("prefix", config["prefix"])
    config["nickname"] = request.form.get("nickname", config["nickname"])
    config["log_channel"] = request.form.get("log_channel", config["log_channel"])
    config["xp_rate"] = request.form.get("xp_rate", config["xp_rate"])
    
    # টগল বাটনগুলোর লজিক (চেক করা না থাকলে ফর্মে আসে না)
    config["maintenance"] = "ON" if request.form.get("maintenance") else "OFF"
    config["mute_role"] = "ON" if request.form.get("mute_role") else "OFF"
    config["anti_spam"] = "ON" if request.form.get("anti_spam") else "OFF"
    config["block_links"] = "ON" if request.form.get("block_links") else "OFF"
    
    print(f"📡 Update Received: {config}")
    return redirect(url_for('index'))

def run_dashboard():
    # রেন্ডার বা অন্য হোস্টের পোর্টের জন্য ডাইনামিক বাইন্ডিং
    port = int(os.environ.get("PORT", 10000))
    app.run(host='0.0.0.0', port=port)
