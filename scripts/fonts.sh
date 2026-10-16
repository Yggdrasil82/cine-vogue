#!/usr/bin/env bash
# Télécharge les polices libres (licence OFL) de l'appli : Bebas Neue et DM Sans.
set -euo pipefail
d=app/src/main/res/font
mkdir -p "$d"

get() { # url destination
  curl -fsSL --retry 3 -o "$2" "$1" && [ "$(head -c1 "$2")" != "<" ]
}

get "https://raw.githubusercontent.com/google/fonts/main/ofl/bebasneue/BebasNeue-Regular.ttf" "$d/bebas_neue.ttf"

for pair in 400:regular 700:bold; do
  w=${pair%%:*}; n=${pair##*:}
  get "https://cdn.jsdelivr.net/fontsource/fonts/dm-sans@latest/latin-$w-normal.ttf" "$d/dm_sans_$n.ttf" \
    || get "https://raw.githubusercontent.com/google/fonts/main/ofl/dmsans/DMSans%5Bopsz,wght%5D.ttf" "$d/dm_sans_$n.ttf"
done
ls -l "$d"
