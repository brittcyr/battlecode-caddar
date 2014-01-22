
vals = ['X' + str(x) + 'Y' for x in range(1000)]
vals = [x + 'WWWWWWW' for x in vals]
vals = [x[:5] for x in vals]
#print '\"' + ''.join(vals) + '\"'

s = ""
for a in range(1,51):
	s = ""
	for b in range(1,51):
		s += ' + \"Z\" + (char)' + str(a) + ' + (char)' + str(b)
	s = s[3:]
	s = "data.append(" + s
	s += ");"
	print s

		

#chars = [['Z', chr(a), chr(b)] for a in range(50) for b in range(100)]
#chars = ''.join([''.join(x) for x in chars])
#print chars
