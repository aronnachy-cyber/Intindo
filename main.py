import discord
from discord.ext import commands
import threading
import datetime
import random
import os
from dashboard import run_dashboard, config  # ড্যাশবোর্ড লজিক ইমপোর্ট

# --- ⚙️ Bot Setup ---
TOKEN = "MTQ3MTc0NzY0NDc5OTA1ODA4Mw.GhVugs.1fiUcGAredLPZwp9KGKRx9ZCDXXJxs5tAHtWd0"
intents = discord.Intents.all()
bot = commands.Bot(command_prefix=lambda b, m: config['prefix'], intents=intents)

# --- 🛡️ 1. Manual Moderation & Hierarchy ---

@bot.command()
@commands.has_permissions(kick_members=True)
async def kick(ctx, member: discord.Member, *, reason="No reason provided"):
    if ctx.author.top_role <= member.top_role:
        return await ctx.send("❌ Tumi tomar cheye boro ba soman role er kauke kick korte parbe na, boss!")
    await member.kick(reason=reason)
    await ctx.send(f"✅ **{member}** ke kick kora hoyeche. | Reason: {reason}")

@bot.command()
@commands.has_permissions(ban_members=True)
async def ban(ctx, member: discord.Member, *, reason="No reason provided"):
    if ctx.author.top_role <= member.top_role:
        return await ctx.send("❌ Admin ba Higher role ke ban kora assobhov!")
    await member.ban(reason=reason)
    await ctx.send(f"🔨 **{member}** ke permanent ban kora hoyeche!")

# --- 🚪 2. Welcome System (Random GIF Support) ---

@bot.event
async def on_member_join(member):
    if config["welcome_system"] == "ON" and config["welcome_channel"]:
        channel = bot.get_channel(int(config["welcome_channel"]))
        if channel:
            gif_list = [g.strip() for g in config["welcome_gifs"].split(",")]
            selected_gif = random.choice(gif_list)

            welcome_text = config["welcome_msg"].replace("{user}", member.mention)

            embed = discord.Embed(
                description=welcome_text,
                color=discord.Color.blue(),
                timestamp=datetime.datetime.utcnow()
            )
            embed.set_image(url=selected_gif)
            embed.set_footer(text=f"Member Count: {len(member.guild.members)}")

            await channel.send(content=member.mention, embed=embed)

# --- 🤖 3. Automod & Social Notifier (MERGED VERSION) ---

@bot.event
async def on_message(message):
    if message.author.bot:
        return

    # --- 🚫 BANNED WORDS FILTER ---
    if config["banned_words"]:
        bad_words = [word.strip().lower() for word in config["banned_words"].split(",")]

        if any(bad_word in message.content.lower() for bad_word in bad_words):
            try:
                await message.delete()
                return await message.channel.send(
                    f"{message.author.mention}, The word is inapprotiate in this server.Please dont use it🙏",
                    delete_after=7
                )
            except:
                pass

    # --- 🔗 Anti-Link Filter ---
    if config["block_links"] == "ON" and ("http" in message.content or "www" in message.content):
        await message.delete()
        return await message.channel.send(
            f"🚫 {message.author.mention}, Links are not allowed here!",
            delete_after=5
        )

    # --- 📺 YouTube Notifier ---
    if "youtube.com" in message.content or "youtu.be" in message.content:
        if config["yt_notifier"] == "ON":
            embed = discord.Embed(title="📺 YouTube Link Detected", color=0xff0000)
            embed.description = f"User {message.author.mention} shared a video link!"
            await message.channel.send(embed=embed)

    # --- 🔵 Facebook Notifier ---
    if "facebook.com" in message.content and config["fb_notifier"] == "ON":
        embed = discord.Embed(title="🔵 Facebook Post Detected", color=0x1877F2)
        embed.description = f"Hey! {message.author.mention} Facebook er kichu share koreche."
        await message.channel.send(embed=embed)

    await bot.process_commands(message)

# --- 📝 4. Logging System ---

@bot.event
async def on_message_delete(message):
    if config["log_channel"]:
        log_chan = bot.get_channel(int(config["log_channel"]))
        if log_chan:
            embed = discord.Embed(
                title="🗑️ Message Deleted",
                color=0xffa500,
                timestamp=datetime.datetime.utcnow()
            )
            embed.add_field(name="Author", value=message.author.mention)
            embed.add_field(name="Channel", value=message.channel.mention)
            embed.add_field(name="Content", value=message.content or "No text content (Media?)")
            await log_chan.send(embed=embed)

# --- 🚀 5. Execution ---

@bot.event
async def on_ready():
    print(f'✅ Logged in as {bot.user.name} | Sentinel OS is Active!')

if __name__ == "__main__":
    t = threading.Thread(target=run_dashboard)
    t.daemon = True
    t.start()

    bot.run(TOKEN)
