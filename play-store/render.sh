#!/bin/zsh
# Exporte les visuels Play Store (HTML -> PNG) avec Chrome headless
cd "$(dirname "$0")"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
render() { "$CHROME" --headless=new --disable-gpu --hide-scrollbars --force-device-scale-factor=1 \
  --virtual-time-budget=4000 --window-size=$2 --screenshot="$PWD/$3" "file://$PWD/src/$1" 2>/dev/null }
render icon.html 512,512 icon-512.png
render feature-graphic.html 1024,500 feature-graphic-1024x500.png
for i in 1 2 3 4; do render screenshot-$i.html 1080,1920 screenshot-$i.png; done
