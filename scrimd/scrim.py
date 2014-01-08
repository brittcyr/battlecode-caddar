#!/usr/bin/python

import datetime
import os
from random import shuffle
import shutil

bots_file = "bots.txt"
maps_file = "maps.txt"

with open(bots_file) as f:
  bots = [bot.strip() for bot in f.readlines()]

with open(maps_file) as f:
  maps = [m.strip()[:-4] for m in f.readlines()]

def preparegame(a, b, m, gamedir):
  pass

def playmatch(a, b, matchdir):
  for m in maps:
    gamedir = os.path.join(matchdir, m)
    os.mkdir(gamedir)
    shutil.copy("build.xml.proto", os.path.join(gamedir, "build.xml"))
    shutil.copy("bc.conf.proto", os.path.join(gamedir, "bc.conf"))
    preparegame(a, b, m, gamedir)
    # play game

matchups = []
for a in range(len(bots)):
  for b in range(a+1, len(bots)):
    matchups.append([bots[a], bots[b]])
shuffle(matchups)

matchesdir = datetime.datetime.now().strftime("%y-%m-%d-%H.%M.%S")
os.mkdir(matchesdir)

for match in matchups:
  shuffle(match)
  a, b = match
  matchdir = os.path.join(matchesdir, "%s__VS__%s" % (a, b))
  os.mkdir(matchdir)
  playmatch(a, b, matchdir)


