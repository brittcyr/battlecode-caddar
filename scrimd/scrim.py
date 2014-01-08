#!/usr/bin/python

import datetime
import os
from random import shuffle
import shutil
import subprocess

bots_file = "bots.txt"
maps_file = "maps.txt"

with open(bots_file) as f:
  bots = [bot.strip() for bot in f.readlines()]

with open(maps_file) as f:
  maps = [m.strip()[:-4] for m in f.readlines()]

def preparegame(a, b, m, gamedir):
  with open(os.path.join(gamedir, "bc.conf"), "a") as f:
    f.write("\n\n## BEGIN SCRIMD ##\n")
    f.write("bc.game.maps=%s\n" % m)
    f.write("bc.game.team-a=%s\n" % a)
    f.write("bc.game.team-a=%s\n" % b)

def playmatch(a, b, matchdir):
  print "%s vs %s... " % (a, b)
  for m in maps:
    gamedir = os.path.join(matchdir, m)
    os.mkdir(gamedir)
    shutil.copy("build.xml.proto", os.path.join(gamedir, "build.xml"))
    shutil.copy("bc.conf.proto", os.path.join(gamedir, "bc.conf"))
    preparegame(a, b, m, gamedir)
    cwd = os.getcwd()
    with open(os.path.join(gamedir, "console.log"), "w") as f:
      os.chdir(gamedir)
      subprocess.call(["ant", "file"], stdout=f, stderr=f)
    os.chdir(cwd)
  print "done\n"

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


