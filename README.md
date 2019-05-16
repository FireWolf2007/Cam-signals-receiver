# MVP Camera network integration with Telegram bot.

Main project aim: simple and unified access to cameras in one application.

# How to run

## Configuration

Setup yours cameras in the `src/main/resources/application.properties`.
1. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`.
1. Setup `cam.count`
1. Setup properties `cam1.*` and so on.
1. Setup `telegram.chat_id`
1. Setup `telegram.bot_id`
1. Setup `telegram.bot_token`
1. Tune `src/main/resources/logback.xml`.

## After run

Application will write logs into bot.log and by default truncate on 10Mb.

Application will write every processed id into file `processed/id.<NUMBER>`, where NUMBER is id from telegram.

# IPv6

Application tested with IPv6 tcp stack.

# Supported cameras

## Full support
1. Apexis J011WS
1. Dericam H502W

## Partial support
1. Sricam Sp017 - take snapshots over ffmpeg

# OS Support

Developed and tested under Ubuntu linux.