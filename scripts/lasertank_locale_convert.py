# Designed for Python 3.7. May not work on other versions.

# Imports
import argparse
import re
from io import StringIO

# Argument handler
arghandler = argparse.ArgumentParser(description='Convert LaserTank localization files for LOVE2D.')
arghandler.add_argument('--input', type=str, help='The input file', required=True)
arghandler.add_argument('--output', type=str, help='The output file', required=True)
arghandler.add_argument('--lang', type=str, help='The language code', required=True)

# Main function
def main(inpath, outpath, lang):
	print('Converting...')
	parsed = StringIO()
	parsed.write('return {\n\t' + lang + ' = {\n\t\t')
	with open(inpath, 'r', encoding='UTF-8') as infile:
		item_key = 0
		for in_line in infile:
			is_section = False
			# Remove leading/trailing whitespace
			in_line = in_line.strip().replace('"', '\\"')
			# Ignore empty lines
			if len(in_line) == 0:
				continue
			# Ignore comments
			if in_line.startswith('#'):
				continue
			# If we got here, there is something to parse
			parsed.write(str(item_key) + ' = "' + in_line + '",\n\t\t')
			item_key += 1
	# Fix up the parsed data
	parsed = parsed.getvalue()[:-4] + '\n\t}\n}'
	# Write the results
	with open(outpath, 'w', encoding='UTF-8') as outfile:
		outfile.write(parsed)
	# Tell the user we are done
	print('Conversion successful!')

# Get things going
if __name__ == '__main__':
	args = arghandler.parse_args()
	main(args.input, args.output, args.lang)
