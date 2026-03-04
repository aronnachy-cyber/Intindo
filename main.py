import discord
from discord.ext import commands
import threading
from dashboard import run_dashboard, config  # dashboard.py থেকে ইমপোর্ট করা

# তোর বটের টোকেন এখানে বসাবি
TOKEN = "YOUR_BOT_TOKEN_HERE"

# ইন্টেন্ট সেটআপ (সব পারমিশন অন রাখা ভালো)
intents = discord.Intents.all()

# ড্যাশবোর্ড থেকে পাওয়া প্রিফিক্স ব্যবহার করা হচ্ছে
bot = commands.Bot(command_prefix=lambda b, m: config['prefix'], intents=intents)

@bot.event
async def on_ready():
    # ড্যাশবোর্ডের নেকনেম অনুযায়ী বটের স্ট্যাটাস সেট করা
    print(f'🚀 {config["nickname"]} is online and connected to Sentinel OS!')
    await bot.change_presence(activity=discord.Game(name=f"Prefix: {config['prefix']}"))

# একটি সিম্পল কমান্ড টেস্ট করার জন্য
@bot.command()
async def ping(ctx):
    await ctx.send(f'🏓 Pong! Current Prefix is: {config["prefix"]}')

if __name__ == "__main__":
    # ১. ড্যাশবোর্ডকে আলাদা থ্রেডে রান করা (যাতে ওয়েব আর বট একসাথে চলে)
    t = threading.Thread(target=run_dashboard)
    t.daemon = True # মেইন প্রোগ্রাম বন্ধ হলে থ্রেডও বন্ধ হবে
    t.start()
    
    # ২. বট রান করা
    bot.run(TOKEN)
