# TelegramWhitelist
This is plugin for add player in whitelist via Telegram bot on proxy server

## Setup
1. Setup plugin on your proxy server
2. Restart server and go to plugin config
3. Enter your telegram bot token, allowed user id and whitelist command
4. Restart server and all done

## How it work
When you write bot nickname of player, bot execute your whitelist command on proxy server
<img width="539" height="107" alt="image" src="https://github.com/user-attachments/assets/0ad39b31-6ad9-4050-8f21-8661be55e692" />

<img width="502" height="42" alt="console" src="https://eblo.id/uploads/CPnmJ9u/Untitled.png" />

## FAQ
What if someone who is not a allowed user type writes to the bot?
> Bot just send to user "❌ You are not allowed" and type warning in console

## Support
If you have idea for new feauture to plugin, or you found a bug, [open issue](https://github.com/pixiemays/TelegramWhitelist/issues?q=sort%3Aupdated-desc+is%3Aissue+is%3Aopen) in plugin repository or go to [my discord](https://discord.gg/gU7XPHeqnW) for support

## Basic config
```
# Telegram bot token (get in @BotFather)
bot-token: "YOUR_BOT_TOKEN_HERE"

# Telegram user ID list, of allowed user
allowed-users:
  - 123456789
  - 987654321

# Execution command (paste nick)
# {nick} — player nick placeholder
commands:
  add: "swl add {nick}"
  remove: "swl remove {nick}"
```
